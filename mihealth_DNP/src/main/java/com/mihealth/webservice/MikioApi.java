package com.mihealth.webservice;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.mihealth.db.model.BatchStudentModel;
import com.mihealth.db.model.CampusModel;
import com.mihealth.db.model.CareDataModel;
import com.mihealth.db.model.MikioModel;
import com.mihealth.db.model.UserModel;
import com.mihealth.db.service.CampusService;
import com.mihealth.db.service.MikioService;
import com.mihealth.security.UserPrincipal;
import com.mihealth.websocket.SocketSessionManager;
import com.ximpl.lib.util.XcJsonUtils;

@Controller
@RequestMapping(value = "/api/mikio")
public class MikioApi {
	@Autowired
	private MikioService mikioService;
	
	@Autowired
	private SocketSessionManager sessionManager;

	@Autowired
	private CampusService campusService;
	
	@RequestMapping(value = {"save", "add", "update"}, method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String save(HttpServletRequest request, Authentication authentication) {
		if (authentication == null)
			return ApiResponse.getFailedResponse("No user logged in.");
		
		UserModel user = UserPrincipal.getUser(authentication);
		String campusUid = user.getCurCampus();
		String userUid = user.getUserUid();

		JsonElement jElement = XcJsonUtils.toJsonElement(request);
		MikioModel mikio = (MikioModel)XcJsonUtils.toObject(jElement, MikioModel.class);
		if (mikio.getUid() == null) {
			mikio.initUid();
			mikio.initRegisteredAt();
			mikio.setEnabled(true);
		}
		
		if (mikio.getCampusUid() == null)
			mikio.setCampusUid(campusUid);
		
		mikioService.save(mikio);
		
		return ApiResponse.getSucceedResponse(mikio);
	}

	
	@RequestMapping(value = "{uid}", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String get(HttpServletRequest request, @PathVariable String uid, Authentication authentication) {
		if (authentication == null)
			return ApiResponse.getFailedResponse("No user logged in.");

		UserModel user = UserPrincipal.getUser(authentication);
		String campusUid = user.getCurCampus();
		String userUid = user.getUserUid();
		
		MikioModel mikio = mikioService.getByUid(uid);
		if (mikio == null) {
			mikio = mikioService.getById(campusUid, uid);
			if (mikio == null)
				return ApiResponse.getFailedResponse("No MiKIO found.");
		}
		
		return ApiResponse.getSucceedResponse(mikio);
	}

	@RequestMapping(value = "{campusId}/{mikioId}", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String get(HttpServletRequest request, @PathVariable String campusId, @PathVariable String mikioId) {
		CampusModel campus = campusService.getCampusByCampusId(campusId);
		MikioModel mikio = mikioService.getById(campus.getUid(), mikioId);
		if (mikio == null) {
			return ApiResponse.getFailedResponse("No MiKIO found.");
		}
		
		return ApiResponse.getSucceedResponse(mikio);
	}

	
	@RequestMapping(value = "list", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String getAll(HttpServletRequest request, Authentication authentication) {
		if (authentication == null)
			return ApiResponse.getFailedResponse("No user logged in.");

		UserModel user = UserPrincipal.getUser(authentication);
		String campusUid = user.getCurCampus();
		String userUid = user.getUserUid();
		
		List<MikioModel> mikioList = mikioService.getAllByCampusUid(campusUid);
		if (mikioList == null || mikioList.isEmpty())
			return ApiResponse.getFailedResponse("No MiKIO found.");
		else
			return ApiResponse.getSucceedResponse(mikioList);
	}

	@RequestMapping(value = "/delete/{uid}", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String delete(HttpServletRequest request, @PathVariable String uid, Authentication authentication) {
		if (authentication == null)
			return ApiResponse.getFailedResponse("No user logged in.");

		UserModel user = UserPrincipal.getUser(authentication);
		String campusUid = user.getCurCampus();
		String userUid = user.getUserUid();
		
		mikioService.delete(uid);

		return ApiResponse.getSucceedResponse();
	}

	@RequestMapping(value = "/students/{campusId}/{mikioId}", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String getStudents(HttpServletRequest request, @PathVariable String campusId, @PathVariable String mikioId, @RequestParam(required = false) Boolean isAbsent) {
		CampusModel campus = campusService.getCampusByCampusId(campusId);
		if (campus == null)
			campus = campusService.getCampusByCampusId(campusId);
		if (campus == null)
			return ApiResponse.getFailedResponse("No Campus found.");
		
		MikioModel mikio = mikioService.getByUid(mikioId);
		if (mikio == null) 
			mikio = mikioService.getById(campus.getUid(), mikioId);
		
		if (mikio == null)
			return ApiResponse.getFailedResponse("No MiKIO found." + " -- " + mikioId);
		
		
		List<BatchStudentModel> students = mikioService.getStudents(mikio.getUid(), isAbsent);
		return ApiResponse.getSucceedResponse(students);
	}

	
	@RequestMapping(value = {"pushStudents/{mikioUid}"}, method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String pushStudents(HttpServletRequest request, Authentication authentication, @PathVariable String mikioUid) {
		if (authentication == null)
			return ApiResponse.getFailedResponse("No user logged in.");
		
		mikioService.resetStudents(mikioUid);

		JsonElement jElement = XcJsonUtils.toJsonElement(request);

		List<BatchStudentModel> students = new Gson().fromJson(jElement, new TypeToken<List<BatchStudentModel>>(){}.getType());
		
		if (students != null && !students.isEmpty())
			mikioService.pushBatchStudent(students);
		
		return ApiResponse.getSucceedResponse();
	}

	@RequestMapping(value = {"resetStudents"}, method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String resetStudents(HttpServletRequest request, Authentication authentication, @RequestParam String mikioUid) {
		if (authentication == null)
			return ApiResponse.getFailedResponse("No user logged in.");
		
		mikioService.resetStudents(mikioUid);
		
		return ApiResponse.getSucceedResponse();
	}

}
