package com.mihealth.webservice;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.mihealth.db.model.CampusModel;
import com.mihealth.db.model.CareDataModel;
import com.mihealth.db.model.StudentCareDataModel;
import com.mihealth.db.model.StudentModel;
import com.mihealth.db.model.TokenModel;
import com.mihealth.db.model.TreatmentDataModel;
import com.mihealth.db.model.TreatmentDetailedModel;
import com.mihealth.db.model.UserModel;
import com.mihealth.db.service.CampusService;
import com.mihealth.db.service.CareDataService;
import com.mihealth.db.service.SettingsService;
import com.mihealth.db.service.StudentService;
import com.mihealth.db.service.TokenService;
import com.mihealth.security.UserPrincipal;
import com.ximpl.lib.constant.DbConst;
import com.ximpl.lib.doc.ExcelTemplate;
import com.ximpl.lib.doc.WordDocument;
import com.ximpl.lib.doc.WordTemplate;
import com.ximpl.lib.util.XcDateTimeUtils;
import com.ximpl.lib.util.XcJsonUtils;
import com.ximpl.lib.util.XcStringUtils;

@Controller
@RequestMapping(value = "/api/care")
public class CareApi {
	@Autowired
	private CampusService campusService;
	
	@Autowired
	private CareDataService careService;
	
	@Autowired
	private StudentService studentService;
	
	@Autowired
	private SettingsService settingsService;

	@Autowired
	TokenService tokenService;

	@SuppressWarnings("null")
	@RequestMapping(value = {"save", "add", "update"}, method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String add(HttpServletRequest request, Principal principal){
		JsonElement jElement = XcJsonUtils.toJsonElement(request);
		
		JsonElement jCampusId = jElement.getAsJsonObject().get("campusId");
		CampusModel campus = null;
		if (jCampusId != null){
			campus = campusService.getCampusByCampusId(jCampusId.getAsString());
		}else
			return ApiResponse.getFailedResponse("Campus not found");
		
		
		CareDataModel careData = (CareDataModel)XcJsonUtils.toObject(jElement, CareDataModel.class);
		if (careData.getUid() == null){	// if new record
			careData.initUid();
			careData.initRegisteredAt();
			careData.setCampusUid(campus.getUid());
			if (principal != null){
				careData.setUid(principal.getName());
			}else{
				JsonElement jToken = jElement.getAsJsonObject().get("token");
				if (jToken != null){
					final TokenModel token = tokenService.get(jToken.getAsString());
					if (token!=null){
						careData.setUserUid(token.getUserUid());
					}else{
						return ApiResponse.getFailedResponse();
					}
				}else{
					return ApiResponse.getFailedResponse();
				}
			}			
		}
		careService.save(careData);
		return ApiResponse.getSucceedResponse(careData);
	}
	
	/**
	 * Get care data record details
	 * @param request
	 * @param uid
	 * @param principal
	 * @return
	 */
	@RequestMapping(value = "{uid}", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String get(HttpServletRequest request, @PathVariable String uid, Principal principal) {
		CareDataModel careData = careService.get(uid);
		if (careData == null){
			return ApiResponse.getFailedResponse("The record ID is not found.");
		}
		
		return ApiResponse.getSucceedResponse(careData);
	}

	/**
	 * Get all the care data list for the user
	 * @param userUid
	 * @param deptId
	 * @param diseaseCode
	 * @param from
	 * @param to
	 * @param dateFormat
	 * @param timeZone
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String messages(
			Authentication authentication,
			@RequestParam(value = "userUid", required = false) String userUid,
			@RequestParam(value = "token", required = false) String tokenUid,
			@RequestParam(value = "dept", required = false) String deptId,
			@RequestParam(value = "disease", required = false) String diseaseCode,
			@RequestParam(value = "from", required = false) String from, 
			@RequestParam(value = "to", required = false) String to, 
			@RequestParam(value = "dateFormat", required = false) String dateFormat,
			@RequestParam(value = "timeZone", required = false) String timeZone
			) {
		
		
		UserModel user = null;
		String campusUid = null;
		if (authentication != null){
			user = UserPrincipal.getUser(authentication);
			campusUid = user.getCurCampus();
			userUid = user.getUserUid();
		} else if (XcStringUtils.isValid(tokenUid)){
			TokenModel token = tokenService.get(tokenUid);
			if (token == null)
				return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_TOKEN, "Token is not available.");
			StudentModel student = studentService.getByUserUid(token.getUserUid());
			campusUid = student.getCampusUid();
			userUid = student.getUserUid();
		}
		
		CampusModel campus = campusService.getCampusByCampusUid(campusUid);
		
		DateTime fromDate = XcDateTimeUtils.parseDate(from, dateFormat, timeZone);
		DateTime toDate = XcDateTimeUtils.parseDate(to, dateFormat, timeZone);
		
		List<StudentCareDataModel> list = studentService.getCareData(campus, null, null, String.format("{userUid:\"%s\"}", userUid), null, null, null, null);
//		List<CareDataModel> list = careService.get(userUid, deptId, diseaseCode, fromDate == null ? null : fromDate.toDate(), toDate == null ? null : toDate.toLocalDate().plusDays(1).toDate());
		if (list== null || list.isEmpty())
			return ApiResponse.getFailedResponse("The data is not found.");
		return ApiResponse.getSucceedResponse(list);
	}	

	@RequestMapping(value = "delete", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String delete(HttpServletRequest request, Authentication authentication) {
		CareDataModel careData = (CareDataModel)XcJsonUtils.toObject(request, CareDataModel.class);
		careService.delete(careData);
		return ApiResponse.getSucceedResponse(careData);
	}
	
	@RequestMapping(value = {"regNo/new"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String getNewRegNo(@RequestParam(value = "token", required = false) String tokenId) {
		String regNo = settingsService.getValue("care", "regno");
		int no = 1;
		long curTime = DateTime.now().getMillis();
		long prevTime = 0;
		if (XcStringUtils.isValid(regNo)){
			String[] tokens = regNo.split("\\."); // Split by dot(.)
			no = Integer.parseInt(tokens[0]);
			prevTime = Long.parseLong(tokens[1]);
			// Reset the counter each day
			if (XcDateTimeUtils.isSameDate(prevTime, curTime)){
				no++;
			}else
				no = 1;
		}
		// Save cur registration No.
		prevTime = curTime;
		settingsService.save("care", "regno", Joiner.on(".").join(no, prevTime));
			
		return ApiResponse.getSucceedResponse(no);
	}
	
	@RequestMapping(value = {"nurse"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String getCurNurse(@RequestParam(value = "token", required = false) String tokenId) {
		return null;
	}
	
	@RequestMapping(value = {"treatment/save", "treatment/add", "treatment/update"}, method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String addTreatment(HttpServletRequest request, Authentication authentication){
		TreatmentDataModel treatment = (TreatmentDataModel)XcJsonUtils.toObject(request, TreatmentDataModel.class);
		UserModel user = UserPrincipal.getUser(authentication);
		if (XcStringUtils.isNullOrEmpty(treatment.getUid())){
			treatment.init();
		}
		
		if (XcStringUtils.isNullOrEmpty(treatment.getNurseUid()))
			treatment.setNurseUid(user.getUid());

		
		careService.saveTreatment(treatment);
		
		return ApiResponse.getSucceedResponse(treatment);
	}
	
	@RequestMapping(value = {"treatments/{careUid}"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String getTreatments(@PathVariable String careUid, @RequestParam(value = "token", required = false) String tokenId) {
		List<TreatmentDetailedModel> list = careService.getTreatments(careUid);
		
		return ApiResponse.getSucceedResponse(list);
	}
	
	@RequestMapping(value = {"treatment/delete/{dataUid}"}, method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String getDeleteTreatment(@PathVariable String dataUid, @RequestParam(value = "token", required = false) String tokenId) {
		careService.removeTreatment(dataUid);
		
		return ApiResponse.getSucceedResponse();
	}
	
	
	@RequestMapping(value = "/reports", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String reports(
			HttpServletResponse response,
			Authentication authentication,
			@RequestParam(value = "userUid", required = false) String userUid,
			@RequestParam(value = "token", required = false) String tokenUid,
			@RequestParam(value = "dept", required = false) String deptId,
			@RequestParam(value = "disease", required = false) String diseaseCode,
			@RequestParam(value = "from", required = false) String from, 
			@RequestParam(value = "to", required = false) String to, 
			@RequestParam(value = "dateFormat", required = false) String dateFormat,
			@RequestParam(value = "timeZone", required = false) String timeZone
			) {
		UserModel user = UserPrincipal.getUser(authentication);
		String campusUid = user.getCurCampus();
		CampusModel campus = campusService.getCampusByCampusUid(campusUid);
		
		if (XcStringUtils.isNullOrEmpty(userUid)){
			TokenModel token = null;
			if (XcStringUtils.isValid(tokenUid)){
				token = tokenService.get(tokenUid);
			}
			if (token == null)
				return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_TOKEN, "Token is not available.");
			
			userUid = token.getUserUid();
		}
		
		DateTime fromDate = XcDateTimeUtils.parseDate(from, dateFormat, timeZone);
		DateTime toDate = XcDateTimeUtils.parseDate(to, dateFormat, timeZone);
		
		List<StudentCareDataModel> list = studentService.getCareData(campus, null, null, String.format("{userUid:\"%s\"}", userUid), null, null, null, null);
		if (list== null || list.isEmpty())
			return ApiResponse.getFailedResponse("The data is not found.");
		
		final String filePath = studentService.getTemplatePath("disease report.xlsx");
		String outFilePath = String.format("%s-%s.xlsx", Files.getNameWithoutExtension(filePath), XcDateTimeUtils.toString(new DateTime(), "yyyyMMdd"));
		ExcelTemplate template = new ExcelTemplate();
		template.open(filePath);
		for(StudentCareDataModel careData : list){
			template.addRow(careData);
		}
		template.close();
		template.write(response, outFilePath);
		return ApiResponse.getSucceedResponse(list);
	}	

}
