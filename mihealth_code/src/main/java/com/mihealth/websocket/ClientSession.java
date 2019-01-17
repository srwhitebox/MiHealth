package com.mihealth.websocket;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.appengine.repackaged.com.google.common.base.Strings;
import com.mihealth.db.model.UserModel;
import com.mihealth.security.UserPrincipal;
import com.ximpl.lib.net.XcCookies;

public class ClientSession {
	public enum CLIENT_TYPE{
		USER,
		CLIENT_SERVICE,
		MIKIO,
		UNKNOWN,
	}
	
	public static final String KEY_CAMPUS_ID = "campusId";
	public static final String KEY_MIKIO_ID = "mikioId";

	private CLIENT_TYPE clientType = CLIENT_TYPE.UNKNOWN;
	private String clientId;
	private WebSocketSession webSocketSession;
	private Map<String, String> cookies;
	
	public ClientSession(WebSocketSession session){
		this.setWebSocketSession(session);
		final UserModel user = this.getUesr();
		if (user != null) {
			this.setType(CLIENT_TYPE.USER);
			this.setClientId(user.getUserUid());
		}else {
			final String campusId = this.getCampusId();
			if (Strings.isNullOrEmpty(campusId))
				return;
			
			final String mikioId = this.getMikioId();
			if (!Strings.isNullOrEmpty(mikioId)) {
				this.setType(CLIENT_TYPE.MIKIO);
				this.setClientId(mikioId);
			}else {
				this.setType(CLIENT_TYPE.CLIENT_SERVICE);
				this.setClientId(campusId);
			}
		}
	}
	
	public CLIENT_TYPE getType() {
		return this.clientType;
	}
	
	public void setType(CLIENT_TYPE clientType) {
		this.clientType = clientType;
	}
	
	public boolean hasClientId() {
		return !Strings.isNullOrEmpty(this.clientId);
	}
	
	public String getClientId() {
		return this.clientId;
	}
	
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	public String getCampusId() {
		return getHeader(KEY_CAMPUS_ID);
	}
	
	public String getMikioId() {
		return getHeader(KEY_MIKIO_ID);
	}
	
	public WebSocketSession getWebSocketSession() {
		return this.webSocketSession;
	}
	
	public void setWebSocketSession(WebSocketSession webSocketSession) {
		this.webSocketSession = webSocketSession;
		this.patchCookies();
	}
	
	public void sendMessage(String message) {
		this.sendMessage(new TextMessage(message));
	}
	
	public void sendMessage(BinaryMessage message) {
		try {
			if (this.webSocketSession != null)
				this.webSocketSession.sendMessage(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean sendMessage(TextMessage message) {
		try {
			if (this.webSocketSession != null && this.webSocketSession.isOpen()) {
				this.webSocketSession.sendMessage(message);
				return true;
			}else
				return false;
		} catch (IOException e) {
			return false;
		}
	}
	
	public UserModel getUesr() {
		return UserPrincipal.getUser(webSocketSession);
	}
	
	public void patchCookies() {
		List<String> values = this.webSocketSession.getHandshakeHeaders().get("cookie");
		if (values == null || values.isEmpty())
			return;
		this.cookies = XcCookies.parse(values.get(0));
	}
	private String getHeader(String key){
		List<String> values = this.webSocketSession.getHandshakeHeaders().get(key);
		if (values == null || values.isEmpty()) {
			if(this.cookies != null) {
				return this.cookies.get(key);
			}else
				return null;
		} else
			return values.get(0);
	}
	
}
