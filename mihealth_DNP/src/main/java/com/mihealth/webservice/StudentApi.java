package com.mihealth.webservice;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpRequest;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.datetime.joda.LocalDateParser;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.api.client.json.JsonObjectParser;
import com.google.appengine.api.users.User;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mihealth.app.CampusProperty;
import com.mihealth.db.model.AccountModel;
import com.mihealth.db.model.CampusModel;
import com.mihealth.db.model.MeasurementDataModel;
import com.mihealth.db.model.PropertyModel;
import com.mihealth.db.model.SchoolRegisterModel;
import com.mihealth.db.model.StudentCareDataModel;
import com.mihealth.db.model.StudentMeasurementDataModel;
import com.mihealth.db.model.StudentModel;
import com.mihealth.db.model.TokenModel;
import com.mihealth.db.model.TreatmentDetailedModel;
import com.mihealth.db.model.UserModel;
import com.mihealth.db.service.BmiService;
import com.mihealth.db.service.CampusService;
import com.mihealth.db.service.CareDataService;
import com.mihealth.db.service.MeasurementDataService;
import com.mihealth.db.service.PropertyService;
import com.mihealth.db.service.SchoolService;
import com.mihealth.db.service.StudentService;
import com.mihealth.db.service.TokenService;
import com.mihealth.db.service.UserService;
import com.mihealth.db.type.COMMAND;
import com.mihealth.db.utils.EncodeUtils;
import com.mihealth.security.UserPrincipal;
import com.ximpl.lib.type.FAT_LEVEL;
import com.ximpl.lib.type.GENDER;
import com.ximpl.lib.type.ID_TYPE;
import com.ximpl.lib.type.TOKEN_TYPE;
import com.ximpl.lib.type.UserRole;
import com.ximpl.lib.util.XcBooleanUtils;
import com.ximpl.lib.util.XcDateTimeUtils;
import com.ximpl.lib.util.XcJsonUtils;
import com.ximpl.lib.util.XcStringUtils;
import com.ximpl.lib.constant.DateTimeConst;
import com.ximpl.lib.constant.GeneralConst;
import com.ximpl.lib.constant.PropertyConst;
import com.ximpl.lib.doc.ExcelCell;
import com.ximpl.lib.doc.ExcelReader;
import com.ximpl.lib.doc.ExcelRow;
import com.ximpl.lib.doc.ExcelTemplate;
import com.ximpl.lib.doc.WordDocument;
import com.ximpl.lib.doc.WordTemplate;
import com.ximpl.lib.io.XcFile;

@Controller
@RequestMapping(value = "/api/{campusId}")
public class StudentApi {
	@Resource(name = "authenticationManager")
	private AuthenticationManager authenticationManager;

	@Value("#{filesProperties['report.students']}")
	private String reportStudents;
	
	@Value("#{filesProperties['report.bmi']}")
	private String reportBmi;
	
	@Value("#{filesProperties['report.disease']}")
	private String reportDisease;
	
	@Value("#{filesProperties['report.vision']}")
	private String reportVision;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private StudentService studentService;
	
	@Autowired
	private MeasurementDataService measurementService;

	@Autowired
	private CareDataService careService;
	
	@Autowired
	private BmiService bmiService;
	
	@Autowired
	private CampusService campusService;
	
	@Autowired
	private SchoolService schoolService;

	@Autowired
	private TokenService tokenService;
	
	@Autowired
	private PropertyService propertyService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;	

	private Row headerRow = null;
	
	@RequestMapping(value = {"student/save", "student/register", "student/update", "student/insert"}, method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String save(HttpServletRequest request, Authentication authentication, @PathVariable String campusId) {
		CampusModel campus = campusService.getCampusByCampusId(campusId);
		if (campus == null && authentication != null)
			campus = campusService.getCampusByAuthentication(authentication);
		
		StudentModel student = (StudentModel) XcJsonUtils.toObject(request, StudentModel.class);
		UserModel user = null;
		if (student.getUserUid() == null){
			user = new UserModel();
			user.init();
			user.addRole(UserRole.STUDENT);
		}else{
			user = userService.getUser(student.getUserUid());
			user.setLastUpdated(new Date());
		}
		
		user.setName(student.getName());
		user.setNationalId(student.getNationalId());
		user.setGender(student.getGender());
		user.setProperties(student.getUserProperties());
		user.setBirthDate(student.getBirthDate());
		user.setEnabled(student.getUserEnabled());
		
		SchoolRegisterModel register = schoolService.getByUserUid(user.getUid());
		if (register == null){
			register = new SchoolRegisterModel();
			register.initUid();
			register.initRegisteredAt();
			register.setEnabled(true);
			register.setCampusUid(campus.getCampusUid());
			register.setUserUid(user.getUid());
		}
		register.setGrade(student.getGrade());
		register.setSchoolYear(student.getSchoolYear());
		register.setStudentNo(student.getStudentNo());
		register.setProperties(student.getRegisterProperties());

		userService.save(user);
		schoolService.save(register);
		
		return null;
	}
	
	@RequestMapping(value = {"student"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String studentInfo(@PathVariable String campusId, 
			@RequestParam(value = "token", required = false) String tokenUid,
			@RequestParam(value = "uid", required = false) String userUid) {
		if (XcStringUtils.isValid(tokenUid)){
			TokenModel token = tokenService.get(tokenUid);
			if (token != null){
				if (token.isExpired(GeneralConst.DEFAULT_TOKEN_LIFE_CYCLE)){
					tokenService.delete(token);
					return ApiResponse.getMessage(ApiResponse.CODE_FAILED_EXPIRED_TOKEN, "Token is expired.");
				}
				userUid = token.getUserUid();
			}else
				return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_TOKEN, "Token is not available.");	
		}
		
		if (XcStringUtils.isValid(userUid)){
			StudentModel student = studentService.getByUserUid(userUid);
			return ApiResponse.getSucceedResponse(student);
		}else{
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NOT_ENOUGH_INFO, "Token or UserUID are not available.");
		}
		
	}
	
	@RequestMapping(value = {"student/delete", "student/remove"}, method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String deleteStudent(@PathVariable String campusId, String userUid) {
		userService.deleteByUserUid(userUid);
 		schoolService.deleteByUserUid(userUid);
		
		return null;
	}

	@RequestMapping(value = {"student/report"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public void reportStudent(HttpServletResponse response, @PathVariable String campusId, String userUid) {
		StudentModel student = studentService.getByUserUid(userUid);
		final String filePath = studentService.getTemplatePath(reportStudents);
		String outFilePath = String.format("%s-%s-%s-%s-%s.docx", Files.getNameWithoutExtension(filePath), XcDateTimeUtils.toString(new DateTime(), "yyyyMMdd"), student.getRegisterProperty("classId"), student.getRegisterProperty("seat"), student.getName());
		WordTemplate template = new WordTemplate(filePath);
		template.setModel("campus.name", campusService.get("name"));
		template.setModel("student.name", student.getName());
		template.setModel("student.classId", student.getRegisterProperty("classId"));
		template.setModel("student.seat", student.getRegisterProperty("seat"));
		template.setModel("measurement.height", measurementService.getLatest(userUid, "height"));
		template.setModel("measurement.weight", measurementService.getLatest(userUid, "weight"));
		template.setModel("measurement.leftEye", measurementService.getLatest(userUid, "leftEye"));
		template.setModel("measurement.rightEye", measurementService.getLatest(userUid, "rightEye"));
		template.setModel("date", new Date());
		template.patch();
		template.write(response, outFilePath);
		template.close();
		
	}

	
	/**
	 * Get all available(existing) grades
	 * @param campusId
	 * @return
	 */
	@RequestMapping(value = {"grades"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String grades(Authentication authentication, @PathVariable String campusId) {
		CampusModel campus = campusService.getCampusByCampusId(campusId);
		if (campus == null && authentication != null)
			campus = campusService.getCampusByAuthentication(authentication);
		if (campus == null)
			return ApiResponse.getFailedResponse("Campus not found");
		List<Integer> list = studentService.getAllGrades(campus);
		
		return ApiResponse.getSucceedResponse(list);
	}

	/**
	 * Get all class IDs
	 * If grade is defined,it'll return the class IDs in the grade.
	 * @param campusId
	 * @param grade
	 * @return
	 */
	@RequestMapping(value = {"classIds"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String classIds(Authentication authentication, @PathVariable String campusId, @RequestParam(required = false) Integer grade) {
		CampusModel campus = campusService.getCampusByCampusId(campusId);
		if (campus == null && authentication != null)
			campus = campusService.getCampusByAuthentication(authentication);
		if (campus == null)
			return ApiResponse.getFailedResponse("Campus not found");
		List<String> list = studentService.getClassIds(campus, grade);
		
		return ApiResponse.getSucceedResponse(list);
	}

	/**
	 * Get all students
	 * If class ID is defined, it'll return the students in the classroom with class ID.
	 * If grade is defined, it'll return the students in the grade
	 * If both is defined, it'll return for class ID.
	 * @param campusId
	 * @param grade
	 * @param classId
	 * @return
	 */
	@RequestMapping(value = {"students"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String students(Authentication authentication, @PathVariable String campusId, @RequestParam(required = false) Integer grade, @RequestParam(required = false) String classId, @RequestParam(required = false, defaultValue="true") Boolean enabledOnly) {
		CampusModel campus = campusService.getCampusByCampusId(campusId);
		if (campus == null && authentication != null)
			campus = campusService.getCampusByAuthentication(authentication);
		if (campus == null)
			return ApiResponse.getFailedResponse("Campus not found");

		List<StudentModel> list = null;

		UserModel user = UserPrincipal.getUser(authentication);
		if (user != null && user.hasRole(UserRole.TEACHER))
			classId = user.getPropertyAsString("classId");
		
		list = studentService.getByClassId(campus, classId, enabledOnly);

//		if (XcStringUtils.isValid(classId))
//			list = studentService.getByClassId(campus, classId);
//		else if (grade != null)
//			list = studentService.getByGrade(campus, grade);
//		else if (filter != null)
//			list = studentService.findWithFilter(campus, classId, filter, count);
//		else
//			list = studentService.getAll(campus);
		
		return ApiResponse.getSucceedResponse(list);
	}

	@RequestMapping(value = {"student/list"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String students(
			Authentication authentication, 
			@PathVariable String campusId, 
			@RequestParam(required=false) String orderBy, 
			@RequestParam(required=false) String filter,
			@RequestParam(required=false) Date from,
			@RequestParam(required=false) Date to,
			@RequestParam(required=false) Integer count, 
			@RequestParam(required=false) Integer page
			) {
		
		CampusModel campus = campusService.getCampusByCampusId(campusId);
		if (campus == null && authentication != null)
			campus = campusService.getCampusByAuthentication(authentication);
		if (campus == null)
			return ApiResponse.getFailedResponse("Campus not found");

		List<StudentModel> list = studentService.getStudents(campus, orderBy, filter, from, to, count, page);
		int total = studentService.getStudentsNumber(campus, filter, from, to);

		return  ApiResponse.getSucceedResponse(list, total);
	}

	@RequestMapping(value = {"student/reports"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public void studentReports(
			HttpServletResponse response, 
			Authentication authentication,
			@PathVariable String campusId, 
			@RequestParam(required=false) String orderBy, 
			@RequestParam(required=false) String filter,
			@RequestParam(required=false) Date from,
			@RequestParam(required=false) Date to,
			@RequestParam(required=false) Integer count, 
			@RequestParam(required=false) Integer page
			) {
		
		CampusModel campus = campusService.getCampusByCampusId(campusId);
		if (campus == null && authentication != null)
			campus = campusService.getCampusByAuthentication(authentication);
		
		if (campus == null)
			return;

		List<StudentModel> list = studentService.getStudents(campus, orderBy, filter, from, to, count, page);
		
		final String filePath = studentService.getTemplatePath(reportStudents);
		if (!new XcFile(filePath).exists())
			return;
		
		
		String outFilePath = String.format("%s-%s.docx", Files.getNameWithoutExtension(filePath), XcDateTimeUtils.toString(new DateTime(), "yyyyMMdd"));
		WordDocument doc = new WordDocument();
		WordTemplate template = new WordTemplate(filePath);
		for(StudentModel student : list){
			final String userUid = student.getUserUid();
			template.setModel("campus.name", campusService.get("name"));
			template.setModel("student.name", student.getName());
			template.setModel("student.classId", student.getRegisterProperty("classId"));
			template.setModel("student.seat", student.getRegisterProperty("seat"));
			template.setModel("measurement.height", measurementService.getLatest(userUid, "height"));
			template.setModel("measurement.weight", measurementService.getLatest(userUid, "weight"));
			template.setModel("measurement.leftEye", measurementService.getLatest(userUid, "leftEye"));
			template.setModel("measurement.rightEye", measurementService.getLatest(userUid, "rightEye"));
			template.setModel("date", new Date());
			template.patch();
			doc.append(template.getDocument());
			template.reset();
		}
		template.close();
		doc.write(response, outFilePath);
		doc.close();
	}

	@RequestMapping(value = {"measurement"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String measurement(
			Authentication authentication,
			@PathVariable String campusId, 
			@RequestParam(required=false) String orderBy, 
			@RequestParam(required=false) String filter,
			@RequestParam(required=false) Date from,
			@RequestParam(required=false) Date to,
			@RequestParam(required=false) Integer count, 
			@RequestParam(required=false) Integer page, 
			@RequestParam(required = false) String item) {
		
		CampusModel campus = campusService.getCampusByCampusId(campusId);
		if (campus == null && authentication != null)
			campus = campusService.getCampusByAuthentication(authentication);
		if (campus == null)
			return ApiResponse.getFailedResponse("Campus not found");

		List<StudentMeasurementDataModel> list = studentService.getMeasurementData(campus, item, orderBy, filter, from, to, count, page);
		
		return ApiResponse.getSucceedResponse(list);
	}

	@RequestMapping(value = {"bmiUpdate"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String updateBmi(
			Authentication authentication,
			@PathVariable String campusId, 
			@RequestParam(required=false) String orderBy, 
			@RequestParam(required=false) String filter,
			@RequestParam(required=false) Date from,
			@RequestParam(required=false) Date to,
			@RequestParam(required=false) Integer count, 
			@RequestParam(required=false) Integer page, 
			@RequestParam(required = false) String item) {

		CampusModel campus = campusService.getCampusByCampusId(campusId);
		if (campus == null && authentication != null)
			campus = campusService.getCampusByAuthentication(authentication);
		if (campus == null)
			return ApiResponse.getFailedResponse("Campus not found");
		List<StudentMeasurementDataModel> list = studentService.getMeasurementData(campus, item, orderBy, filter, from, to, count, page);
		
		for(StudentMeasurementDataModel student : list){
			final JsonElement jHeight = student.getProperties().get("height");
			if (jHeight != null){
				float height = jHeight.getAsFloat();
				final JsonElement jWeight = student.getProperties().get("weight");
				float weight = jWeight.getAsFloat();
				final double bmi = bmiService.getBmi(height, weight);
				final GENDER gender = student.getGender();
				final Date birthDate = student.getBirthDate();
				final FAT_LEVEL fatLevel = bmiService.getLevel(gender, birthDate, height, weight);
				
				MeasurementDataModel measurementData = measurementService.get(student.getDataUid());
				measurementData.setProperty("bmi", String.format("%.2f", bmi));
				measurementData.setProperty("fatLevel", fatLevel.name());				
				
				measurementService.save(measurementData);
			}
		}
		
		return ApiResponse.getSucceedResponse(list);
	}

	
	@RequestMapping(value = {"measurement/bmiReport"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String bmiReport(
			Authentication authentication,
			@PathVariable String campusId, 
			@RequestParam(required=false) String classId, 
			@RequestParam(required=false) String gender) {

		CampusModel campus = campusService.getCampusByCampusId(campusId);
		if (campus == null && authentication != null)
			campus = campusService.getCampusByAuthentication(authentication);
		if (campus == null)
			return ApiResponse.getFailedResponse();
		JsonObject result = studentService.getBmiReport(campus.getCampusUid(), classId, gender);
		
		return ApiResponse.getSucceedResponse(result);
	}

	@RequestMapping(value = {"care/report/{deptId}"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String internalDiseaseReport(
			Authentication authentication,
			@PathVariable String campusId,
			@PathVariable String deptId,
			@RequestParam(value="from", required=false) @DateTimeFormat(pattern="yyyy-MM-dd") Date fromDate,
			@RequestParam(value="to", required=false) @DateTimeFormat(pattern="yyyy-MM-dd") Date toDate,
			@RequestParam(required=false) String classId, 
			@RequestParam(required=false) String gender) {
		
		CampusModel campus = campusService.getCampusByCampusId(campusId);
		if (campus == null && authentication != null)
			campus = campusService.getCampusByAuthentication(authentication);
		JsonObject result = 
				deptId.equals("injuredPlace") ? studentService.getPlaceReport(campus.getCampusUid(), fromDate, toDate, classId, gender) : studentService.getDiseaseReport(campus.getCampusUid(), deptId, fromDate, toDate, classId, gender);
				
		return ApiResponse.getSucceedResponse(result);
	}

	@RequestMapping(value = {"care"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String care(
			Authentication authentication,
			@PathVariable String campusId, 
			@RequestParam(required=false) String orderBy, 
			@RequestParam(required=false) String filter,
			@RequestParam(required=false) Date from,
			@RequestParam(required=false) Date to,
			@RequestParam(required=false) Integer count, 
			@RequestParam(required=false) Integer page, 
			@RequestParam(required = false) String item) {
		
		CampusModel campus = campusService.getCampusByCampusId(campusId);
		if (campus == null && authentication != null)
			campus = campusService.getCampusByAuthentication(authentication);
		if (campus == null)
			return ApiResponse.getFailedResponse();
		List<StudentCareDataModel> list = studentService.getCareData(campus, item, orderBy, filter, from, to, count, page);
		int total = studentService.getCareNumber(campus, item, filter, from, to);

		return ApiResponse.getSucceedResponse(list, total);
//		return ApiResponse.getSucceedResponse(list);
	}

	@RequestMapping(value = {"reports/measurement/{reportType}"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String reportsMeasurement(
			HttpServletResponse response, Authentication authentication,
			@PathVariable String campusId,
			@PathVariable String reportType, 
			@RequestParam(required=false) String orderBy, 
			@RequestParam(required=false) String filter,
			@RequestParam(required=false) Date from,
			@RequestParam(required=false) Date to,
			@RequestParam(required=false) Integer count, 
			@RequestParam(required=false) Integer page, 
			@RequestParam(required = false) String item,
			@RequestParam(required = false) String timeZone) {
		
		DateTimeZone zone = DateTimeZone.getDefault();
		if (XcStringUtils.isValid(timeZone)){
			zone = DateTimeZone.forID(timeZone);
		}
		CampusModel campus = campusService.getCampusByCampusId(campusId);
		if (campus == null && authentication != null)
			campus = campusService.getCampusByAuthentication(authentication);
		if (campus == null)
			return ApiResponse.getFailedResponse("Campus not found");

		String localeTag = null;
		UserModel user = UserPrincipal.getUser(authentication);
		if (user != null)
			localeTag = user.getSettingAsString(GeneralConst.KEY_LANGUAGE);
		if (XcStringUtils.isNullOrEmpty(localeTag))
			localeTag = "en-US";
		
		List<StudentMeasurementDataModel> list = studentService.getMeasurementData(campus, item, orderBy, filter, from, to, count, page);
		
		final String filePath;
		if (reportType.equals("bmi")){
			filePath = studentService.getTemplatePath(reportBmi);
		}else if (reportType.equals("vision")){
			filePath = studentService.getTemplatePath(reportVision);
		}else
			return null;
		
		String outFilePath = String.format("%s-%s.xlsx", Files.getNameWithoutExtension(filePath), XcDateTimeUtils.toString(new DateTime(), "yyyyMMdd"));
		ExcelTemplate template = new ExcelTemplate();
		template.open(filePath);
		for(StudentMeasurementDataModel measurementData : list){
			if (reportType.equals("bmi") && measurementData.getNumericProperty("height") == null)
				continue;
			if (reportType.equals("vision") && measurementData.getNumericProperty("leftEye") == null)
				continue;
			
			Map<String, Object> model = new HashMap<String, Object>();
//			DateTime registeredAt = new DateTime(measurementData.getRegisteredAt(), DateTimeZone.UTC);
			Date registeredAt = new Date(zone.convertUTCToLocal(measurementData.getRegisteredAt().getTime()));
			model.put("date", registeredAt);
			model.put("student.seat", measurementData.getRegisterProperties().get("seat").getAsString());
			model.put("student.classId", measurementData.getRegisterProperties().get("classId").getAsString());
			model.put("student.name", measurementData.getName());
			
			final Number height = measurementData.getNumericProperty("height");
			if (height != null){
				model.put("measurement.height", height);
			}

			final Number weight = measurementData.getNumericProperty("weight");
			if (weight != null){
				model.put("measurement.weight", weight);
			}
	
			//$scope.bmiDesc = ['[(#{bmi_under})]', '[(#{bmi_normal})]', '[(#{bmi_over})]', '[(#{bmi_obesity})]', '[(#{bmi_obese})]'];
			
			final Number bmi = measurementData.getNumericProperty("bmi");
			if (bmi != null){
				model.put("measurement.bmi", bmi);
				final Date birthDate = measurementData.getBirthDate();
				final GENDER gender = measurementData.getGender();
				final FAT_LEVEL fatLevel = bmiService.getLevel(gender, birthDate, height.floatValue(), weight.floatValue());
				String desc = measurementService.getBmiLevelDesc(localeTag, fatLevel.ordinal());
				model.put("measurement.bmiComment", desc);
			}
			final Number leftEye = measurementData.getNumericProperty("leftEye");
			if (leftEye != null){
				model.put("measurement.leftEye", leftEye);
			}
			Number rightEye = measurementData.getNumericProperty("rightEye");
			if (rightEye != null){
				model.put("measurement.rightEye", rightEye);
			}
			template.addRow(model);
		}
		template.write(response, outFilePath);
		template.close();		
		
		return ApiResponse.getSucceedResponse(list);
	}
	
	@RequestMapping(value = {"reports/care"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String careReport(
			HttpServletResponse response, Authentication authentication,
			@PathVariable String campusId, 
			@RequestParam(required=false) String orderBy, 
			@RequestParam(required=false) String filter,
			@RequestParam(required=false) Date from,
			@RequestParam(required=false) Date to,
			@RequestParam(required=false) Integer count, 
			@RequestParam(required=false) Integer page, 
			@RequestParam(required = false) String item,
			@RequestParam(required = false) String timeZone) {
		
		DateTimeZone zone = DateTimeZone.getDefault();
		if (XcStringUtils.isValid(timeZone)){
			zone = DateTimeZone.forID(timeZone);
		}
		CampusModel campus = campusService.getCampusByCampusId(campusId);
		if (campus == null && authentication != null)
			campus = campusService.getCampusByAuthentication(authentication);
		if (campus == null)
			return ApiResponse.getFailedResponse();
		
		String localeTag = null;
		UserModel user = UserPrincipal.getUser(authentication);
		if (user != null)
			localeTag = user.getSettingAsString(GeneralConst.KEY_LANGUAGE);
		if (XcStringUtils.isNullOrEmpty(localeTag))
			localeTag = "en-US";
		
		List<StudentCareDataModel> list = studentService.getCareData(campus, item, orderBy, filter, from, to, count, page);
		
		final String filePath = studentService.getTemplatePath(reportDisease);
		String outFilePath = String.format("%s-%s.xlsx", Files.getNameWithoutExtension(filePath), XcDateTimeUtils.toString(new DateTime(), "yyyyMMdd"));
		ExcelTemplate template = new ExcelTemplate();
		template.open(filePath);
		for(StudentCareDataModel careData : list){
			Map<String, Object> model = new HashMap<String, Object>();
			Date registeredAt = new Date(zone.convertUTCToLocal(careData.getRegisteredAt().getTime()));
			model.put("date", registeredAt);
			model.put("student.seat", careData.getRegisterProperties().get("seat").getAsString());
			model.put("student.classId", careData.getRegisterProperties().get("classId").getAsString());
			model.put("student.name", careData.getName());
			
			final String deptId = careData.getDeptId();
			PropertyModel property = propertyService.get("medicalDept", deptId);
			final String dept = property.getPropertyAsString(localeTag);
			model.put("disease.dept", dept);
			
			String diseaseCode =  careData.getProperties().get("disease").getAsString();
			String disease = propertyService.getValues(deptId.startsWith("surgery") ? "surgeryDisease" : "internalDisease", diseaseCode, localeTag);
			if (disease.length() > 0) {
				model.put("disease.name", disease);
			}
			
			if (careData.getProperties().has("injuredPart")) {
				String injuredPartCode =  careData.getProperties().get("injuredPart").getAsString();
				String injuredPart = propertyService.getValues("injuredPart", injuredPartCode, localeTag);
				if (injuredPart.length() > 0) {
					model.put("injured.part", injuredPart);
				}
			}
			
			if (careData.getProperties().has("injuredPlace")) {
				String injuredPlaceCode =  careData.getProperties().get("injuredPlace").getAsString();
				String injuredPlace = propertyService.getValues("injuredPlace", injuredPlaceCode, localeTag);
				if (injuredPlace.length() > 0) {
					model.put("injured.place", injuredPlace);
				}
			}
			
			if (careData.isHasTreatment()){
				List<TreatmentDetailedModel> treatments = careService.getTreatments(careData.getDataUid());
				StringBuilder treatmentValue = new StringBuilder();
				for(TreatmentDetailedModel treatment : treatments){
					if (treatmentValue.length() > 0)
						treatmentValue.append("\n");
					JsonObject jTreatment = treatment.getTreatment();
					if (jTreatment.has("treatment")) {
						String treatmentCode = jTreatment.get("treatment").getAsString();
						String value = propertyService.getValues(deptId.startsWith("surgery") ? "surgeryTreatment" : "internalTreatment", treatmentCode, localeTag);
						if (value != null)
							treatmentValue.append(value);
						if (jTreatment.has("comment")) {
							String comment = jTreatment.get("comment").getAsString();
							if (comment != null) {
								treatmentValue.append(": ");
								treatmentValue.append(comment);
							}
						}
					}				
				}
				model.put("disease.treatment", treatmentValue.toString());
			}
			template.addRow(model);
		}
		template.write(response, outFilePath);
		template.close();
		return ApiResponse.getSucceedResponse(list);
	}

	
	/**
	 * Login with register properties with given birth date(yyMM) as password
	 * If it finds multiple student, it'll fail to login.
	 * @param campusId
	 * @param grade
	 * @param properties
	 * Properties must be GSON formatted. 
	 * @param password
	 * @return
	 */
	@RequestMapping(value = {"login"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String loginWithProperties(
			Authentication authentication,
			@PathVariable String campusId, @RequestParam int grade, @RequestParam String properties, @RequestParam String password) {
		CampusModel campus = campusService.getCampusByCampusId(campusId);
		if (campus == null && authentication != null)
			campus = campusService.getCampusByAuthentication(authentication);
		if (campus == null)
			return ApiResponse.getFailedResponse("Campus not found");

		List<StudentModel> list = studentService.getByProperties(campus, grade, properties);
		return login(list, password);
	}

	@RequestMapping(value = {"login/{classId}/{seat}/{password}"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String loginWithProperties(
			Authentication authentication,
			@PathVariable String campusId, @PathVariable String seat, @PathVariable  String classId, @PathVariable  String password) {
		CampusModel campus = campusService.getCampusByCampusId(campusId);
		if (campus == null && authentication != null)
			campus = campusService.getCampusByAuthentication(authentication);
		if (campus == null)
			return ApiResponse.getFailedResponse("Campus not found");

		List<StudentModel> list = studentService.getByClassIdAndSeat(campus, classId, seat);
		return login(list, password);
	}
	
	public static final String POST_URL = "http://10.11.10.68/iostpf-api-backend-encrypt/service/v1/9999999901/Decryption.json";

	@RequestMapping(value = {"easyCard/{cardId}"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String loginWithEasyCard(
			Authentication authentication,
			@PathVariable String campusId, @PathVariable String cardId) throws IOException {
		System.out.println("================testcard===============" + cardId);
		if(cardId.length()>15){
		String plainData = null;
		String plainData_c = "";
		URL url = new URL(POST_URL);
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("POST");
		httpCon.setRequestProperty("Content-Type", "application/json");
		httpCon.setRequestProperty("Accept", "application/json");
		httpCon.setRequestProperty("Authorization",
				"Basic bWl0YWNfc2l0ZTp5UTlsaEh5b01DbmJISEpDWW1JWThZVGZJOFhtZ1ZJUg==");
		OutputStream os = httpCon.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
		osw.write(cardId);
		osw.flush();
		osw.close();
		os.close();
		httpCon.connect();
		int responseCode = httpCon.getResponseCode();
		if (responseCode == 400) {
			System.out.println("error");
			InputStream is = httpCon.getErrorStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			System.out.println(rd.readLine());
			rd.close();
			is.close();
		}

		String result;
		BufferedInputStream bis = new BufferedInputStream(httpCon.getInputStream());
		BufferedReader rd = new BufferedReader(new InputStreamReader(bis));
		StringBuilder response = new StringBuilder();
		
		String line;
		while ((line = rd.readLine()) != null) {
			response.append(line);
			response.append('\n');
		}
		result = response.toString();
		System.out.println(result);
		rd.close();
		bis.close();
		
		JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(result).getAsJsonObject();
		
		//JsonObject json = new JsonObject();
		plainData = json.getAsJsonObject("content").get("plainData").toString();
		plainData = plainData.replaceAll("\"", "");
//		System.out.println("==============plainData========" + plainData);
//		System.out.println("==============plainData.length()========" + plainData.length());
//		plainData.trim();
//		System.out.println("==============plainData.trim().length()========" + plainData.trim().length());
		for (int i = 0; i < plainData.trim().length(); i+=2)
        {
			String str = plainData.substring(i, i+2);
			//System.out.println("=========str========="+str);
			plainData_c = plainData_c + (char) Integer.parseInt(str, 16);
        }
		
		System.out.println("======================plaindata_c.toString()=========" + plainData_c.toString());
		cardId = plainData_c;
		}
		System.out.println("======================cardId=========" + cardId);

		
		//­ì¥»ªº
		CampusModel campus = campusService.getCampusByCampusId(campusId);
		if (campus == null && authentication != null)
			campus = campusService.getCampusByAuthentication(authentication);
		if (campus == null)
			return ApiResponse.getFailedResponse("Campus not found");

		List<StudentModel> list = studentService.getByEasyCard(campus, cardId);
		if (list != null && list.size() > 0){
			TokenModel token = authorize(list.get(0));
			return ApiResponse.getTokenResponse(token);
		}else{
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_RECORD, "No student found.");
		}
	}

	@RequestMapping(value = {"nhiCard/{nationalId}/{birthDate}"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String loginWithNhiCard(
			Authentication authentication,
			@PathVariable String campusId, @PathVariable String nationalId,  @PathVariable String birthDate) {
		CampusModel campus = campusService.getCampusByCampusId(campusId);
		if (campus == null && authentication != null)
			campus = campusService.getCampusByAuthentication(authentication);
		if (campus == null)
			return ApiResponse.getFailedResponse("Campus not found");

		List<StudentModel> list = studentService.getByNationalId(campus, nationalId);
		return login(list, birthDate);
	}

	@RequestMapping(value = {"login/{tokenUid}"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String loginWithToken(
			Authentication authentication,
			@PathVariable String campusId, @PathVariable String tokenUid) {
		TokenModel token = tokenService.get(tokenUid);
		if (token != null){
			if (token.isExpired(GeneralConst.DEFAULT_TOKEN_LIFE_CYCLE)){
				tokenService.delete(token);
				return ApiResponse.getMessage(ApiResponse.CODE_FAILED_EXPIRED_TOKEN, "Token is expired.");
			}else{
				UserModel user = userService.getUser(token.getUserUid());
				TokenModel newToken = authorize(user);
				
				return ApiResponse.getTokenResponse(newToken);
			}
		}else
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_TOKEN, "Token is not available.");		
	}
	
	private String login(List<StudentModel> list, String password){
		if (list == null || list.size() == 0)
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_RECORD, "No student found.");
		else if (list.size() != 1)
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_LOGIN, "Too many students found.");
		
		final StudentModel student = list.get(0);
		
		boolean isBirthMatch = XcDateTimeUtils.isSameDay(student.getBirthDate(), password, DateTimeConst.FORMAT_DATE_4_DIGIT_PASSWORD );
		
		if (isBirthMatch){
			// Authorization procedure..
			TokenModel token = authorize(student);
			
			return ApiResponse.getTokenResponse(token);
		}else
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_LOGIN, "Wrong password.");
	}
	
	
	private TokenModel authorize(StudentModel student){
		UserModel user = userService.getUser(student.getUserUid());
		
		return authorize(user);
	}

	/**
	 * Authorize role of user
	 * @param user
	 * @return
	 */
	private TokenModel authorize(UserModel user){		
        TokenModel token = tokenService.get(user.getUid(), TOKEN_TYPE.LOGGED_IN);
        if ( token== null)
        	token = new TokenModel(user.getUid(), TOKEN_TYPE.LOGGED_IN);
        token.setRegisteredAt(new Date());
        tokenService.save(token);
        
		Authentication authentication = new UsernamePasswordAuthenticationToken(new UserPrincipal(user), token.getTokenUid(), user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return token;
	}	
	
	@RequestMapping(value = {"logout"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String logout(Authentication authentication, @PathVariable String campusId) {
		String token = (String)authentication.getCredentials();
		return logout(campusId, token);
	}

	@RequestMapping(value = {"logout/{tokenUid}"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String logout(@PathVariable String campusId, @PathVariable String tokenUid) {
		TokenModel token = tokenService.get(tokenUid);
		if (token != null){
			if (token.getTokenType() != TOKEN_TYPE.LOGGED_IN)
				return ApiResponse.getMessage(ApiResponse.CODE_FAILED_LOGOUT, "Token is not for logout.");
			
			tokenService.delete(token);
			
			if (token.isExpired(GeneralConst.DEFAULT_TOKEN_LIFE_CYCLE)){
				return ApiResponse.getMessage(ApiResponse.CODE_FAILED_EXPIRED_TOKEN, "Token is expired.");
			}else{
				SecurityContextHolder.getContext().setAuthentication(null);
				return ApiResponse.getMessage(ApiResponse.CODE_SUCCEED_LOGOUT, " User has been logged out.");
			}
		}else
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_TOKEN, "Token is not available.");
	}
	
	@RequestMapping(value="student/upload", method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody 
	public String upload(HttpServletRequest request, @PathVariable("campusId") String campusId, @RequestParam("file") MultipartFile file, Principal principal){
		CampusModel campus = campusService.getCampusByCampusId(campusId);
		if (campus == null)
			return ApiResponse.getFailedResponse("Campus not found");

		final int schoolYear = 2018;
		final String appPath = file.getOriginalFilename();
		final String fileType = file.getContentType();
		try {
			if (fileType.equals("application/vnd.ms-excel")){
				parseXls(campus, schoolYear, file.getInputStream());
			}else if (fileType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")){
				//parseXlsx(campus, schoolYear, file.getInputStream());
				parseXlsxEvent(campus, schoolYear, file.getInputStream());
			}
			return null;
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		return null;
	}

	private void parseRow(CampusModel campus, int schoolYear, Row row){
		final int cellNum =  row.getLastCellNum();
		if (row.getRowNum() == 0){
			headerRow = row;
			return;
		}
		
		UserModel user = null;
		SchoolRegisterModel schoolRegister = null; 
		AccountModel account = null;
		StudentModel student = null;
		
		int newClassIdIndex = -1;
		int newSeatIndex = -1;
		List<StudentModel> students = null;
		String classId = null; 
		String seatNo = null;
		for (int i=0; i <= headerRow.getLastCellNum(); i++) {
			Cell cell = headerRow.getCell(i);
			String header = cell.getStringCellValue();
			if (!Strings.isNullOrEmpty(header)) {
				header = header.trim().toLowerCase();
				String value = row.getCell(i).getStringCellValue();
				if (header.equals("uid") || header.equals("guyid")){
					schoolRegister = schoolService.getByRegNo(value);
					if (schoolRegister != null) {
						user = userService.getUser(schoolRegister.getUserUid());
						student = studentService.getByUserUid(schoolRegister.getUserUid());
					}
				}else if (header.equals("pid") || header.equals("nationalid") || header.equals("national_id") || header.equals("socialid") || header.equals("social_id")){
					students = studentService.getByNationalId(campus, value);					
				}else if (header.equals("classid") || header.equals("class_id") || header.equals("class")){
					classId = value; 
				} else if (header.contains("seat") || header.contains("student_no") || header.contains("student no") || header.contains("student num")){
					seatNo = value;
				}
			}
		}
		
		if (user == null || schoolRegister == null) {
			if (students == null || students.size() == 0) {
				if (seatNo != null && classId != null) {
					students = studentService.getByClassIdAndSeat(campus, classId, seatNo);
				}
			}
			
			if (seatNo != null && students.size()> 0 && classId != null && students != null) {
				for(StudentModel studentEntry: students) {
					SchoolRegisterModel register = schoolService.getByUid(studentEntry.getRegisterUid());
					if (seatNo.equals(register.getPropertyAsString("seat"))){
						if (classId.equals(register.getPropertyAsString("seat"))){
							user = userService.getUser(studentEntry.getUserUid());
							student = studentEntry;
							schoolRegister = register;
							break;
						}
					}
				}
			}else {
				user = new UserModel();
				user.init();
				user.addCampus(campus.getDecryptedUid());
				user.addRole(UserRole.STUDENT);
				account = new AccountModel();
				account.init(user.getUserUid());
				schoolRegister = new SchoolRegisterModel(campus.getUid(), user.getUserUid(), schoolYear);
			}
		}
		
		Iterator<Cell> cells = row.cellIterator();
		while(cells.hasNext()){
			Cell cell = cells.next();
			
			String header = headerRow.getCell(cell.getColumnIndex()).getStringCellValue();
			if (Strings.isNullOrEmpty(header)){
				continue;
			}
			header = header.trim().toLowerCase();
			if (Strings.isNullOrEmpty(header)){
				continue;
			}
			
			String value = cellToString(cell);
			
			if (Strings.isNullOrEmpty(value)){
				continue;
			}
			
			value = value.trim();
			if (Strings.isNullOrEmpty(value)){
				continue;
			}
			
			
			if (header.equals("uid") || header.equals("guyid")){
				schoolRegister.setStudentNo(value);
			} else if (header.equals("pid") || header.equals("nationalid") || header.equals("national_id") || header.equals("socialid") || header.equals("social_id")){
				user.setNationalId(value);
			} else if (header.equals("rfid") || header.equals("nfc")){
				schoolRegister.setProperty("easyCardId", value);
//				account.setIdType(ID_TYPE.RFID.name());
//				account.setId(value);
//				account.setActivatedAt(new DateTime(DateTimeZone.UTC).toDate());
			} else if (header.equals("name") || header.equals("studentname") || header.equals("student_name") || header.equals("student name") || header.equals("guy")){
				user.setName(value);
			} else if (header.equals("gender") || header.equals("sex") || header.equals("sexid") || header.equals("sex_id")){
				user.setGender(GENDER.get(cell.getStringCellValue()));
			} else if (header.equals("years") || header.equals("schoolyear") || header.equals("school_year")){
				schoolRegister.setProperty("enteredYear", (int)cell.getNumericCellValue());
			} else if (header.equals("grade")){
				schoolRegister.setGrade(Integer.parseInt(value));
			} else if (header.equals("classid") || header.equals("class_id") || header.equals("class")){
				classId = value;
				schoolRegister.setProperty("classId", classId);
				if (schoolRegister.getGrade() == null){
					final String[] tokens = classId.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
					if (tokens.length > 1)
						schoolRegister.setGrade(Integer.parseInt(tokens[0]));
				}
			} else if (header.contains("seat") || header.contains("student_no") || header.contains("student no") || header.contains("student num")){
				schoolRegister.setProperty("seat", value);
			} else if (header.equals("birth") || header.equals("birthday") || header.equals("birthdate") || header.equals("birth_date")){
				Date date = cellToDate(cell);
				if (date != null)
					user.setBirthDate(date);
			} else if (header.equals("dad") || header.equals("father")){
				user.setProperty("dad", value);
			} else if (header.equals("mom") || header.equals("mother")){
				user.setProperty("mom", value);
			} else if (header.equals("guardian")){
				user.setProperty("guardian", value);
			} else if (header.equals("zip")){
				user.setProperty("zip", value);
			} else if (header.contains("tel")){
				user.setProperty("tel", value);
			} else if (header.contains("mobile") || header.contains("cellular")){
				user.setProperty("mobile", value);
			} else if (header.startsWith("addr")){
				user.setProperty("address", value);
			} else if (header.contains("emergency")){
				user.setProperty("emergencyCall", value);
			} else if (header.startsWith("blood")){
				user.setProperty("bloodType", value);
			} else if (header.equals("enabled")){
				Boolean enabled = null;
				try {
					enabled = cell.getBooleanCellValue();
				}catch(Exception ex) {
					
				}
				if (!Strings.isNullOrEmpty(value))
					enabled = Boolean.parseBoolean(value);
				if (enabled != null)
					user.setEnabled(enabled);
			} else if (header.equals("newclassid") || header.equals("new_class_id") || header.equals("newclass")){
				classId = value;
				schoolRegister.setProperty("classId", classId);
				if (schoolRegister.getGrade() == null){
					final String[] tokens = classId.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
					if (tokens.length > 1)
						schoolRegister.setGrade(Integer.parseInt(tokens[0]));
				}
			} else if (header.contains("newseat") || header.contains("new_student_no") || header.contains("new_student no") || header.contains("new_student num")){
				schoolRegister.setProperty("seat", value);
			}	
		}
		
		userService.save(user);
		
		if (account !=null && !Strings.isNullOrEmpty(account.getIdType()) && XcStringUtils.isValid(account.getUserUid()))
			userService.save(account);
		
		if (XcStringUtils.isValid(schoolRegister.getStudentNo()))
			schoolService.save(schoolRegister);
	}
	
	private Date cellToDate(Cell cell){
		try{
			return cell.getDateCellValue();
		}catch(Exception ex){
			String dateText = cell.getStringCellValue();
			Date date = null;
			String[] formats = {"yyyyMMdd", "yyyy-M-d", "d.M.yyyy", "d/M/yyyy"};
			for(String format : formats){
				try {
					SimpleDateFormat df = new SimpleDateFormat( format );
					date = df.parse(dateText);
					break;
				} catch (ParseException e) {
				}
			}
			return date;
		}
	}
	
	private String cellToString(Cell cell){
		return cell.getCellType() == Cell.CELL_TYPE_STRING ? cell.getStringCellValue() : String.format("%d", (long)cell.getNumericCellValue());
	}
	
	private void parseXls(CampusModel campus, int schoolYear, InputStream is){
		try {
			HSSFWorkbook wb = new HSSFWorkbook(is);
			Iterator<Sheet> sheets = wb.sheetIterator();
			while(sheets.hasNext()){
				Sheet sheet = sheets.next();
				Iterator<Row> rows = sheet.rowIterator();

				while (rows.hasNext()){
					parseRow(campus, schoolYear, rows.next());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void parseXlsx(CampusModel campus, int schoolYear, InputStream is){
		try {
			XSSFWorkbook  wb = new XSSFWorkbook(is);
			Iterator<Sheet> sheets = wb.sheetIterator();
			while(sheets.hasNext()){
				Sheet sheet = sheets.next();
				Iterator<Row> rows = sheet.rowIterator();

				while (rows.hasNext()){
					parseRow(campus, schoolYear, rows.next());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private ExcelRow headerExcelRow;
	private void parseRow(CampusModel campus, int schoolYear, ExcelRow row){
		final int cellNum =  row.size();
		if (row.getRowIndex() == 0){
			headerExcelRow = row;
			return;
		}
		
		UserModel user = null;
		SchoolRegisterModel schoolRegister = null; 
		AccountModel account = null;
		StudentModel student = null;
		
		int newClassIdIndex = -1;
		int newSeatIndex = -1;
		List<StudentModel> students = null;
		String classId = null; 
		String seatNo = null;
		for (int i=0; i <= headerExcelRow.size(); i++) {
			String header = headerExcelRow.getString(i);
			if (!Strings.isNullOrEmpty(header)) {
				header = header.trim().toLowerCase();
				String value = row.getString(i);
				if (header.equals("uid") || header.equals("guyid")){
					schoolRegister = schoolService.getByRegNo(value);
					if (schoolRegister != null) {
						user = userService.getUser(schoolRegister.getUserUid());
						student = studentService.getByUserUid(schoolRegister.getUserUid());
					}
				}else if (header.equals("pid") || header.equals("nationalid") || header.equals("national_id") || header.equals("socialid") || header.equals("social_id")){
					students = studentService.getByNationalId(campus, value);					
				}else if (header.equals("classid") || header.equals("class_id") || header.equals("class")){
					classId = value; 
				} else if (header.contains("seat") || header.contains("student_no") || header.contains("student no") || header.contains("student num")){
					seatNo = value;
				}
				if (user != null && schoolRegister != null) {
					break;
				}
			}
		}
		
		if (user == null || schoolRegister == null) {
			if (students == null || students.size() == 0) {
				if (seatNo != null && classId != null) {
					students = studentService.getByClassIdAndSeat(campus, classId, seatNo);
				}
			}
			
			if (seatNo != null && students.size()> 0 && classId != null && students != null) {
				for(StudentModel studentEntry: students) {
					SchoolRegisterModel register = schoolService.getByUid(studentEntry.getRegisterUid());
					if (seatNo.equals(register.getPropertyAsString("seat"))){
						if (classId.equals(register.getPropertyAsString("seat"))){
							user = userService.getUser(studentEntry.getUserUid());
							student = studentEntry;
							schoolRegister = register;
							break;
						}
					}
				}
			}else {
				user = new UserModel();
				user.init();
				user.addCampus(campus.getDecryptedUid());
				user.addRole(UserRole.STUDENT);
				account = new AccountModel();
				account.init(user.getUserUid());
				schoolRegister = new SchoolRegisterModel(campus.getUid(), user.getUserUid(), schoolYear);
			}
		}

		for(Map.Entry<Integer, ExcelCell> entry : row.entrySet()) {
			String header = headerExcelRow.getString(entry.getKey());
			if (Strings.isNullOrEmpty(header)){
				continue;
			}

			header = header.trim().toLowerCase();
			String value = entry.getValue().toString();
			if (Strings.isNullOrEmpty(value)){
				continue;
			}
			
			value = value.trim();
			
			if (header.equals("uid") || header.equals("guyid")){
				schoolRegister.setStudentNo(value);
			} else if (header.equals("pid") || header.equals("nationalid") || header.equals("national_id") || header.equals("socialid") || header.equals("social_id")){
				user.setNationalId(value);
			} else if (header.equals("rfid") || header.equals("nfc")){
				schoolRegister.setProperty("easyCardId", value);
//				account.setIdType(ID_TYPE.RFID.name());
//				account.setId(value);
//				account.setActivatedAt(new DateTime(DateTimeZone.UTC).toDate());
			} else if (header.equals("name") || header.equals("studentname") || header.equals("student_name") || header.equals("student name") || header.equals("guy")){
				user.setName(value);
			} else if (header.contains("gender") || header.contains("sex")|| header.equals("sexid") || header.equals("sex_id")){
				//user.setGender(GENDER.get(value));
				user.setGender(GENDER.get(entry.getValue().toInteger()));
			} else if (header.equals("years") || header.equals("schoolyear") || header.equals("school_year")){
				//schoolRegister.setProperty("enteredYear", entry.getValue().toInteger());
				schoolRegister.setSchoolYear(entry.getValue().toInteger());
			} else if (header.equals("grade")){
				schoolRegister.setGrade(Integer.parseInt(value));
			} else if (header.equals("classid") || header.equals("class_id") || header.equals("class")){
				classId = value;
				schoolRegister.setProperty("classId", classId);
				if (schoolRegister.getGrade() == null){
					final String[] tokens = classId.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
					if (tokens.length > 1)
						schoolRegister.setGrade(Integer.parseInt(tokens[0]));
				}
			} else if (header.contains("seat") || header.contains("student_no") || header.contains("student no") || header.contains("student num")){
				schoolRegister.setProperty("seat", value);
			} else if (header.contains("birth")){
				Date date = header.contains("tw") || header.contains("roc") ? entry.getValue().toTwDate() : entry.getValue().toDate();
				if (date != null)
					user.setBirthDate(date);
			} else if (header.equals("dad") || header.equals("father")){
				user.setProperty("dad", value);
			} else if (header.equals("mom") || header.equals("mother")){
				user.setProperty("mom", value);
			} else if (header.equals("guardian")){
				user.setProperty("guardian", value);
			} else if (header.equals("zip")){
				user.setProperty("zip", value);
			} else if (header.contains("tel")){
				user.setProperty("tel", value);
			} else if (header.contains("mobile") || header.contains("cellular")){
				user.setProperty("mobile", value);
			} else if (header.startsWith("addr")){
				user.setProperty("address", value);
			} else if (header.contains("emergency")){
				user.setProperty("emergencyCall", value);
			} else if (header.startsWith("blood")){
				user.setProperty("bloodType", value);
			}  else if (header.equals("enabled")){
				Boolean enabled = null;
				if (!Strings.isNullOrEmpty(value))
					enabled = Boolean.parseBoolean(value);
				if (enabled != null)
					user.setEnabled(enabled);
			} else if (header.equals("newclassid") || header.equals("new_class_id") || header.equals("newclass")){
				classId = value;
				schoolRegister.setProperty("classId", classId);
				if (schoolRegister.getGrade() == null){
					final String[] tokens = classId.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
					if (tokens.length > 1)
						schoolRegister.setGrade(Integer.parseInt(tokens[0]));
				}
			} else if (header.contains("newseat") || header.contains("new_student_no") || header.contains("new_student no") || header.contains("new_student num")){
				schoolRegister.setProperty("seat", value);
			} 	
			
		}
		
		userService.save(user);
		if (account != null && !Strings.isNullOrEmpty(account.getIdType()) && XcStringUtils.isValid(account.getUserUid()))
			userService.save(account);
		if (XcStringUtils.isValid(schoolRegister.getStudentNo()))
			schoolService.save(schoolRegister);
	}

	
	private void parseXlsxEvent(final CampusModel campus, final int schoolYear, InputStream is){
		ExcelReader reader = new ExcelReader(is){
			@Override
			public void onSheetDataStart() {
				System.out.println("SheetData started");
			}

			@Override
			public void onSheetDataEnd() {
			}

			@Override
			public boolean onNewRow(ExcelRow row) {
				parseRow(campus, schoolYear, row);
				return true;
			}
		};
		
		reader.parseAll(); //.parceSheet("rId1");
	}
	
}
