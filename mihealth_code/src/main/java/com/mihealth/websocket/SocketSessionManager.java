package com.mihealth.websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonObject;
import com.mihealth.db.model.CampusModel;
import com.mihealth.db.model.CareDataModel;
import com.mihealth.db.model.MeasurementDataModel;
import com.mihealth.db.model.MikioModel;
import com.mihealth.db.model.StudentModel;
import com.mihealth.db.model.TreatmentDataModel;
import com.mihealth.db.model.UserModel;
import com.mihealth.db.service.CampusService;
import com.mihealth.db.service.CareDataService;
import com.mihealth.db.service.MeasurementDataService;
import com.mihealth.db.service.MikioService;
import com.mihealth.db.service.StudentService;
import com.mihealth.db.service.UserService;
import com.mihealth.webservice.ApiResponse;
import com.ximpl.lib.util.XcJsonUtils;

@Service
public class SocketSessionManager {
	@Autowired
	private MeasurementDataService measurementDataService;

	@Autowired
	private CareDataService careDataService;

	@Autowired
	private ThreadPoolTaskExecutor taskExcutor;
	
	@Autowired
	private CampusService campusService;
	
	@Autowired
	private StudentService studentService;
	
	@Autowired
	private MikioService mikioService;
	
	@Autowired
	private UserService userService;

	private Map<ClientSession.CLIENT_TYPE, Map<String, Set<ClientSession>>> sessionMap = new HashMap<ClientSession.CLIENT_TYPE, Map<String, Set<ClientSession>>>();
	
	public boolean add(WebSocketSession session){
		final ClientSession clientSession = new ClientSession(session);
		final CampusModel campus = campusService.getCampusByCampusId(clientSession.getCampusId());
		switch(clientSession.getType()) {
		case USER:
			break;
		case CLIENT_SERVICE:
			if (campus == null)
				return false;
			clientSession.setClientId(campus.getUid());
			break;
		case MIKIO:
			final MikioModel mikio = mikioService.getById(campus.getUid(), clientSession.getMikioId());
			if (mikio == null)
				return false;
			break;
		default:
			break;
		}
		
		if (clientSession.getType() != ClientSession.CLIENT_TYPE.UNKNOWN) {
			return getSessions(clientSession).add(clientSession);
		}
		try {
			session.close(CloseStatus.NOT_ACCEPTABLE);
		} catch (IOException e) {
		}
		return false;
	}
	
	public void remove(WebSocketSession session){
		final ClientSession clientSession = new ClientSession(session);
		if (clientSession.getType() != ClientSession.CLIENT_TYPE.UNKNOWN) {
			getSessions(clientSession).remove(clientSession);
		}
	}
	
	public void onNewRecord(MeasurementDataModel data){
		taskExcutor.execute(new BroadcastToClientServiceTask(data));
	}

	public void onNewRecord(CareDataModel data){
		taskExcutor.execute(new BroadcastToClientServiceTask(data));
	}

	public void onNewRecord(TreatmentDataModel data){
		taskExcutor.execute(new BroadcastToClientServiceTask(data));
	}

	public void onMikioOnlineStatusChanged(ClientSession clientSession, boolean isConnected) {
		String message = ApiResponse.getResponse(isConnected? ApiResponse.CODE_MIKIO_CONNECTED : ApiResponse.CODE_MIKIO_DISCONNECTED, "mikio_online_status_changed", clientSession.getMikioId());
		Map<String, Set<ClientSession>> userSessionMap = this.getMap(ClientSession.CLIENT_TYPE.USER);
		for(Set<ClientSession> clientSet : userSessionMap.values()) {
			for(ClientSession client : clientSet) {
				if (client.getCampusId() == clientSession.getCampusId()) {
					client.sendMessage(message);
				}
			}
		}
		
	}
	
	public void updateMikio(MikioModel mikio) {
		this.messageToMikio(mikio.getMikioId(), ApiResponse.getResponse(ApiResponse.CODE_MIKIO_UPDATED, "mikio_updated", null));
	}
	
	public void messageToMikio(String mikioId, String message) {
		this.messageTo(ClientSession.CLIENT_TYPE.MIKIO, mikioId, message);
	}
	
	public void messageTo(ClientSession.CLIENT_TYPE clientType, String clientId, String message) {
		TextMessage textMessage = new TextMessage(message);
		for(ClientSession session :this.getSessions(clientType, clientId)) {
			if (session.getWebSocketSession().isOpen())
				session.sendMessage(textMessage);
		}
	}
	
	public Set<ClientSession> getClientServiceSessions(String campusUid){
		return this.getSessions(ClientSession.CLIENT_TYPE.CLIENT_SERVICE, campusUid);
	}

	private Map<String, Set<ClientSession>> getMap(ClientSession.CLIENT_TYPE clientType){
		Map<String, Set<ClientSession>> map = (Map<String, Set<ClientSession>>)this.sessionMap.get(clientType);
		if (map == null) {
			map = new HashMap<String, Set<ClientSession>>();
			this.sessionMap.put(clientType, map);
		}
		
		return map;
	}
	
	private Set<ClientSession> getSessions(ClientSession.CLIENT_TYPE clientType, String clientId){
		Map<String, Set<ClientSession>> map = this.getMap(clientType);
		Set<ClientSession> setSession = map.get(clientId);
		if (setSession == null) {
			setSession = new HashSet<ClientSession>();
		}
		//Only one MiKIO by ID allowed.
//		if (clientType == ClientSession.CLIENT_TYPE.MIKIO)
//			setSession.clear();
		
		map.put(clientId, setSession);

		return setSession;
	}

	private Set<ClientSession> getSessions(ClientSession clientSession){
		return this.getSessions(clientSession.getType(), clientSession.getClientId());
	}
	
	private class BroadcastToClientServiceTask implements Runnable{
		private String campusUid;
		private StudentModel student ;
		private TextMessage message;

		public BroadcastToClientServiceTask(MeasurementDataModel data){
			setCampus(data.getCampusUid());
			setStudent(data.getUserUid());
			MiHealthResponse response = new MiHealthResponse(student, data);
			message = response.toTextMessage();
		}
		
		public BroadcastToClientServiceTask(CareDataModel data){
			setCampus(data.getCampusUid());
			setStudent(data.getUserUid());
			MiHealthResponse response = new MiHealthResponse(student, data);
			message = response.toTextMessage();
		}

		public BroadcastToClientServiceTask(TreatmentDataModel treatmentData){
			CareDataModel careData = careDataService.get(treatmentData.getCareUid());
			setCampus(careData.getCampusUid());
			setStudent(careData.getUserUid());
			MiHealthResponse response = new MiHealthResponse(student, careData, treatmentData);
			message = response.toTextMessage();
		}
		
		public void setCampus(String campusUid){
			this.campusUid = campusUid;
		}
		
		public void setStudent(String studentUid){
			student = studentService.getByUserUid(studentUid);
		}
		
		@Override
		public void run() {
			for(ClientSession session : getSessions(ClientSession.CLIENT_TYPE.CLIENT_SERVICE, campusUid)){
				session.sendMessage(message);
			}
		}
		
	}
}
