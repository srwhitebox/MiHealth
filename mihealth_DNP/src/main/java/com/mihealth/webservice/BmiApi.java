package com.mihealth.webservice;

import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mihealth.db.model.BmiModel;
import com.mihealth.db.service.BmiService;
import com.mihealth.websocket.QueryHandler;
import com.mihealth.websocket.SocketSessionManager;
import com.ximpl.lib.util.XcJsonUtils;

@Controller
@RequestMapping(value = "/api/bmi")
public class BmiApi {
	@Autowired
	private BmiService bmiService;
	
	@RequestMapping(value = "save", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String save(HttpServletRequest request, Authentication authentication){
		BmiModel bmi = (BmiModel)XcJsonUtils.toObject(request,BmiModel.class);
		if (bmi != null){
			bmiService.save(bmi);
			return ApiResponse.getSucceedResponse(bmi);
		}else{
			return ApiResponse.getFailedResponse("BMI base seems not correct.");
		}
	}
	
	@RequestMapping(value = "list", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String list(){
		return ApiResponse.getSucceedResponse(bmiService.findAll());
	}

	@RequestMapping(value = "find", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String find(){
		Calendar date = Calendar.getInstance();
		date.set(2000, 8, 15);
		
		return ApiResponse.getSucceedResponse(bmiService.find(date.getTime()));
	}
}
