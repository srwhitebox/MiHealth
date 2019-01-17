package com.mihealth.webservice;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonElement;
import com.mihealth.db.model.CampusModel;
import com.mihealth.db.model.MeasurementDataModel;
import com.mihealth.db.model.PropertyModel;
import com.mihealth.db.model.TokenModel;
import com.mihealth.db.model.UserModel;
import com.mihealth.db.service.BmiService;
import com.mihealth.db.service.CampusService;
import com.mihealth.db.service.MeasurementDataService;
import com.mihealth.db.service.TokenService;
import com.mihealth.db.service.UserService;
import com.ximpl.lib.constant.DbConst;
import com.ximpl.lib.type.FAT_LEVEL;
import com.ximpl.lib.type.GENDER;
import com.ximpl.lib.util.XcDateTimeUtils;
import com.ximpl.lib.util.XcStringUtils;

@Controller
@RequestMapping(value = "/api/measurement")
public class MeasurementApi {
	
	@Autowired
	private CampusService campusService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	TokenService tokenService;
	
	@Autowired
	private MeasurementDataService measurementService;
	
	@Autowired
	private BmiService bmiService;
	
	@RequestMapping(value = {"save", "update"}, method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String add(HttpServletRequest request, 
			@RequestParam String campusId, @RequestParam(value="token", required = false) String tokenUid, @RequestParam(required = false) String dataUid, @RequestParam(required = false) String userUid, @RequestParam String properties, @RequestParam(required = false) Date registeredAt,
			Principal principal) {
		
		CampusModel campus = campusService.getCampusByCampusId(campusId);
		if (campus == null)
			return ApiResponse.getFailedResponse("Campus not found");
		
		// If the user defined
		UserModel user = null;
		if (XcStringUtils.isValid(userUid) && (user=userService.getUser(userUid)) == null){
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_USERS, "Measurement data require the user UID or token.");
		}
		
		if (XcStringUtils.isNullOrEmpty(userUid) && XcStringUtils.isValid(tokenUid)){
			TokenModel token = tokenService.get(tokenUid);
			if (token != null)
				userUid = token.getUserUid();
			else
				return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_TOKEN, "Token is not available.");
		}
		
		if (XcStringUtils.isNullOrEmpty(userUid)){
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_USERS, "Measurement data is not exist to udpdate.");
		}

		if (user == null)
			user = userService.getUser(userUid);
		
		MeasurementDataModel measurementData = null;

		if (XcStringUtils.isValid(dataUid)){	// if the dataUID is exist, save for current
			measurementData = measurementService.get(dataUid);
			if (measurementData == null){
				return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_RECORD, "Measurement data is not exist to udpdate.");
			}
		}else{ // if no data UID, it means new record.
			measurementData = new MeasurementDataModel();
			measurementData.initUid();
			measurementData.initRegisteredAt();
			measurementData.setUserUid(userUid);
			measurementData.setCampusUid(campus.getUid());
		}
		
		measurementData.setProperties(properties);
		final JsonElement jHeight = measurementData.getProperty("height"); 
		if (jHeight != null){
			user.getBirthDate();
			
			float height = jHeight.getAsFloat();
			final JsonElement jWeight = measurementData.getProperties().get("weight");
			float weight = jWeight.getAsFloat();
			final double bmi = bmiService.getBmi(height, weight);
			if (bmi > 0){
				measurementData.setProperty("bmi", String.format("%.2f", bmi));

				final GENDER gender = user.getGender();
				final Date birthDate = user.getBirthDate();
				final FAT_LEVEL fatLevel = bmiService.getLevel(gender, birthDate, height, weight);
				
				measurementData.setProperty("fatLevel", fatLevel.name());
			}
		}
		
		
		measurementService.save(measurementData);
		
		return ApiResponse.getSucceedResponse(measurementData);
	}

	
	/**
	 * Get detailed data information for give record UID.
	 * @param request
	 * @param uid
	 * @param principal
	 * @return
	 */
	@RequestMapping(value = "date/{uid}", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String get(HttpServletRequest request, @PathVariable String uid, Principal principal) {
		MeasurementDataModel measurementData = measurementService.get(uid);
		if (measurementData == null){
			return ApiResponse.getFailedResponse("The record ID is not found.");
		}
		
		return ApiResponse.getSucceedResponse(measurementData);
	}

	/**
	 * Return the records for the user.
	 * If the item, date range are defined, it'll return the depend on the condition.
	 * @param userUid
	 * @param item
	 * @param from
	 * @param to
	 * @param dateFormat
	 * @param timeZone
	 * @return
	 */
	@RequestMapping(value = "list/{userUid}", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String list(
			@PathVariable(DbConst.FIELD_USER_UID) String userUid,
			@RequestParam(value = "item", required = false) String item,
			@RequestParam(value = "from", required = false) String from, 
			@RequestParam(value = "to", required = false) String to, 
			@RequestParam(value = "dateFormat", required = false) String dateFormat,
			@RequestParam(value = "timeZone", required = false) String timeZone
			) {

		DateTime fromDate = XcDateTimeUtils.parseDate(from, dateFormat, timeZone);
		DateTime toDate = XcDateTimeUtils.parseDate(to, dateFormat, timeZone);
		List<MeasurementDataModel> list = measurementService.get(userUid, item, fromDate == null ? null : fromDate.toDate(), toDate == null ? null : toDate.toLocalDate().plusDays(1).toDate());
		if (list== null || list.isEmpty())
			return ApiResponse.getFailedResponse("The data is not found.");
		return ApiResponse.getSucceedResponse(list);
	}	

	@RequestMapping(value = "list", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String listByToken(
			@RequestParam(value = "token", required = false) String tokenUid,
			@RequestParam(value = "item", required = false) String item,
			@RequestParam(value = "from", required = false) String from, 
			@RequestParam(value = "to", required = false) String to, 
			@RequestParam(value = "dateFormat", required = false) String dateFormat,
			@RequestParam(value = "timeZone", required = false) String timeZone
			) {
		String userUid = null;
		if (XcStringUtils.isValid(tokenUid)){
			TokenModel token = tokenService.get(tokenUid);
			if (token == null)
				return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_TOKEN, "Token is not available.");
			
			userUid = token.getUserUid();
		
		}
		
		return list(userUid, item, from, to, dateFormat, timeZone);
	}
	
	/**
	 * Delete measurement data record.
	 * One of Record UID and User UID must be defined.
	 * If both are defined, only record UID will be deleted.
	 * @param request
	 * @param uid
	 * @param userUid
	 * @return
	 */
	@RequestMapping(value = "delete", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String delete(HttpServletRequest request, @RequestParam(value = DbConst.FIELD_UID, required = false) String uid, @RequestParam(value = DbConst.FIELD_USER_UID, required = false) String userUid) {
		if (XcStringUtils.isValid(uid)){
			MeasurementDataModel measurementData = measurementService.get(uid);
			if (measurementData == null){
				return ApiResponse.getFailedResponse("No record found.");
			}else{
				measurementService.deleteByUid(uid);
			}
			return  ApiResponse.getSucceedResponse("The record has been removed.");
		}else if (XcStringUtils.isValid(userUid)){
			measurementService.deleteByUser(userUid);
			return  ApiResponse.getSucceedResponse("All the records of the user has been cleaned.");
		}
		
		return ApiResponse.getFailedResponse("Record UID(uid) or User UID(userUid) is not defined.");
	}

}
