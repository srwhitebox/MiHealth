package com.mihealth.webservice;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.JsonElement;
import com.mihealth.db.model.CampusModel;
import com.mihealth.db.model.CareDataModel;
import com.mihealth.db.model.TokenModel;
import com.mihealth.db.model.UserDetailedModel;
import com.mihealth.db.model.UserModel;
import com.mihealth.db.service.CampusService;
import com.mihealth.db.service.SchoolService;
import com.mihealth.db.service.UserService;
import com.mihealth.security.UserPrincipal;
import com.ximpl.lib.constant.DbConst;
import com.ximpl.lib.type.UserRole;
import com.ximpl.lib.util.XcJsonUtils;
import com.ximpl.lib.util.XcStringUtils;

@Controller
@RequestMapping(value = "/api/campus")
public class CampusApi {
	@Autowired
	SchoolService studentService;

	@Autowired
	CampusService campusService;
	
	@Autowired
	UserService userService;

	@RequestMapping(value = {"save"}, method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String save(HttpServletRequest request) {
		JsonElement jElement = XcJsonUtils.toJsonElement(request);
		CampusModel campus = (CampusModel)XcJsonUtils.toObject(jElement, CampusModel.class);

		if (campus.getUid() == null){	// if new record
			campus.initUid();
			campus.initRegisteredAt();
			campus.resetAuthKey();
		}
		campusService.save(campus);
		
		return ApiResponse.getSucceedResponse(campus);
	}

	@RequestMapping(value = {"resetAuthkey"}, method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String resetAuthKey(HttpServletRequest request) {
		JsonElement jElement = XcJsonUtils.toJsonElement(request);
		CampusModel campus = (CampusModel)XcJsonUtils.toObject(jElement, CampusModel.class);
		if (campus == null || campus.getUid() == null){
			return ApiResponse.getFailedResponse("Campus not found.");
		}else{
			campus.resetAuthKey();
			campusService.save(campus);
			return ApiResponse.getSucceedResponse(campus);
		}		
	}

	@RequestMapping(value = {"find"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String getCampus(@RequestParam(required = false) String uid, @RequestParam(required = false) String id) {
		CampusModel campus = null;
		if (XcStringUtils.isValid(uid)){
			campus = campusService.getCampusByCampusUid(uid);
		}else if  (XcStringUtils.isValid(id))
			campus = campusService.getCampusByCampusId(id);
		
		if (campus == null)
			return ApiResponse.getFailedResponse("No campus found.");
		return ApiResponse.getSucceedResponse(campus);
	}
	
	@RequestMapping(value = {"findAll"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String getAll(@RequestParam(required = false) String uid, @RequestParam(required = false) String id) {
		List<CampusModel> list = campusService.getAll();

		if (list == null || list.isEmpty())
			return ApiResponse.getFailedResponse("No campus found.");
		return ApiResponse.getSucceedResponse(list);
	}

	@RequestMapping(value = {"delete"}, method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String deleteCampus(HttpServletRequest request) {
		JsonElement jElement = XcJsonUtils.toJsonElement(request);
		CampusModel campus = (CampusModel)XcJsonUtils.toObject(jElement, CampusModel.class);

		if (campus==null || campus.getUid() == null){	// if new record
			return  ApiResponse.getFailedResponse("No campus found.");
		}
		campusService.delete(campus);
		
		return ApiResponse.getSucceedResponse(campus);
	}

	@RequestMapping(value = "/nurses", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String getNurses(@RequestParam(required = false) String uid, @RequestParam(required = false) String id, @RequestParam(value = DbConst.FIELD_ENABLED, required = false) Boolean enabled) {
		CampusModel campus = null;
		if (XcStringUtils.isValid(uid)){
			campus = campusService.getCampusByCampusUid(uid);
		}else if  (XcStringUtils.isValid(id))
			campus = campusService.getCampusByCampusId(id);

		String campusUid = campus.getUid();
		if (enabled == null)
			enabled = true;
		List<UserDetailedModel> list = userService.getUserList(campusUid, UserRole.NURSE, enabled);
		if (list == null || list.isEmpty()){
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_USERS, "No nurse found.");
		}else
			return ApiResponse.getSucceedResponse(list);
	}

	
	@RequestMapping(value = {"{key}"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String getId(@PathVariable String key) {
		return campusService.get(key);
	}
	
	@RequestMapping(value = {"set"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String setId(@RequestParam Map<String, String> params) {
		for(Entry<String, String> entry : params.entrySet()){
			campusService.set(entry.getKey(), entry.getValue());
		}

		return ApiResponse.getSucceedResponse(params);	
	}
	
	@RequestMapping(value="/profile/upload/{campusId}/{itemType}", method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody 
	public String upload(HttpServletRequest request, Authentication authentication, @PathVariable String campusId, @PathVariable String itemType, @RequestParam("file") MultipartFile file){
		String profilePath = campusService.getProfilePath(campusId, itemType, file.getOriginalFilename());
		
		File targetFile = new File(profilePath);
		
		if (!targetFile.getParentFile().exists())
			targetFile.getParentFile().mkdirs();
		
		try {
			BufferedInputStream bis = new BufferedInputStream(file.getInputStream());
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(targetFile));
			byte[] buffer = new byte [1024*1024*8];
			int count = 0;
			while((count = bis.read(buffer))!=-1){
				bos.write(buffer, 0, count);
			}
			bos.flush();
			bos.close();
			bis.close();
			
			return ApiResponse.getSucceedResponse(targetFile.getPath());
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		return null;
	}

}
