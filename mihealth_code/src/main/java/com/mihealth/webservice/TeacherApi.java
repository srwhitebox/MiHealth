package com.mihealth.webservice;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mihealth.db.model.UserModel;
import com.mihealth.db.service.UserService;
import com.ximpl.lib.util.XcJsonUtils;

@Controller
@RequestMapping(value = "/api")
public class TeacherApi {
	@Autowired
	UserService userService;

	@RequestMapping(value = {"teacher/delete", "nurse/delete"}, method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String userCommand(HttpServletRequest request, Authentication authentication) {
		UserModel user = (UserModel) XcJsonUtils.toObject(request, UserModel.class);
		if (user.getUserUid() == null){
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_PARAMS, "Teacher UID must be added in the data.");
		}else{
			userService.delete(user);
			return ApiResponse.getMessage(ApiResponse.CODE_SUCCEED, "The teache and his data has been removed.");
		}
	}


}
