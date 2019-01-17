package com.mihealth.db.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import javax.persistence.Query;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mihealth.db.model.AccountModel;
import com.mihealth.db.model.CampusModel;
import com.mihealth.db.model.StudentCareDataModel;
import com.mihealth.db.model.StudentMeasurementDataModel;
import com.mihealth.db.model.StudentModel;
import com.mihealth.db.repositories.StudentMeasurementDataRepository;
import com.mihealth.db.repositories.StudentRepository;
import com.mihealth.db.utils.EncodeUtils;
import com.ximpl.lib.db.EntityManagerHelper;
import com.ximpl.lib.util.XcDateTimeUtils;
import com.ximpl.lib.util.XcJsonUtils;
import com.ximpl.lib.util.XcStringUtils;
import com.ximpl.lib.util.XcUrlUtils;
import com.ximpl.lib.util.XcUuidUtils;

@Service
public class StudentService {
	@Value("#{filesProperties['files.root']}")
	private String filesRoot;
	
	@Autowired
	private EntityManagerHelper entityManagerHelper;
	
	@Autowired
	StudentRepository studentRepository;

	@Autowired
	StudentMeasurementDataRepository studentMeasurementRepository;

	@Autowired
	UserService userService;
	
	public StudentModel getByUserUid(String userUid){
		return studentRepository.findByUserUid(EncodeUtils.decrypt(userUid));
	}
	
	public List<Integer> getAllGrades(CampusModel campus){
		return studentRepository.getGrades(campus.getDecryptedUid());
	}
	
	public List<String> getClassIds(CampusModel campus, Integer grade){
		if (grade == null || grade == 99)
			return studentRepository.getClassIds(campus.getDecryptedUid());
		else
			return studentRepository.getClassIds(campus.getDecryptedUid(), grade);
	}

	public List<StudentModel> getAll(CampusModel campus){
		
		return studentRepository.findAll(campus.getDecryptedUid());
	}
	
	public List<StudentModel> getByGrade(CampusModel campus, int grade){
		return studentRepository.findByGrade(campus.getDecryptedUid(), grade);
	}

	/**
	 * Return students matches properties
	 * @param grade
	 * @param properties
	 * The properties must be JSON formatted
	 * @return
	 */
	public List<StudentModel> getByProperties(CampusModel campus, int grade, String properties){
		final JsonElement jElement = XcJsonUtils.toJsonElement(properties);
		if (jElement == null || !jElement.isJsonObject())
			return null;
		
		final JsonObject jProperties = jElement.getAsJsonObject();

		String queryText = "SELECT registerUid, campusUid, userUid, name, nationalId, birthDate, gender, regNo, schoolYear, grade, "
				+ "CAST(COLUMN_JSON(roles) AS CHAR) AS roles, "
				+ "CAST(COLUMN_JSON(registerProperties) AS CHAR) AS registerProperties, "
				+ "CAST(COLUMN_JSON(userProperties) AS CHAR) AS userProperties, "
				+ "CAST(COLUMN_JSON(settings) AS CHAR) AS settings, "
				+ "enabled, lastUpdated, registeredAt FROM tv_student WHERE ";
		StringBuilder sb = new StringBuilder();
		boolean added = false;
		for(Entry<String, JsonElement> jEntry : jProperties.entrySet()){
			final String key = jEntry.getKey();
			final String value = jEntry.getValue().getAsString();
			if (added)
				sb.append(" AND ");
			sb.append("COLUMN_GET(registerProperties, '");
			sb.append(key);
			sb.append("' AS CHAR) = '");
			sb.append(value);
			sb.append("'");
			sb.append(" AND campusUid = '" + campus.getDecryptedUid() + "'");
			added = true;
		}
		sb.insert(0, queryText);
		
		Query query = entityManagerHelper.query(sb.toString(), StudentModel.class);
		
		return query.getResultList();
	}
	
	public List<StudentModel> getByClassIdAndSeat(CampusModel campus, String classId, String seat){
		return studentRepository.findByClassIdAndSeat(campus.getDecryptedUid(), classId, seat);
	}

	public List<StudentModel> getByNationalId(CampusModel campus, String nationalId){
		return studentRepository.findByNationalId(campus.getDecryptedUid(), nationalId);
	}

	public List<StudentModel> getByEasyCard(CampusModel campus, String cardId){
		List<StudentModel> students = studentRepository.findByEasyCardId(campus.getDecryptedUid(), cardId);
		if (!students.isEmpty())
			return students;
		
		AccountModel account = userService.getAccount(cardId);
		if (account != null){
			StudentModel student = studentRepository.findByUserUid(account.getDecryptedUserUid());
			if (student != null){
				students.add(student);
			}
		}
		return students;
	}

	public List<StudentModel> getByClassId(CampusModel campus, String classId, boolean enabledOnly) {
		List<StudentModel> students = enabledOnly ? studentRepository.findByClassIdEnabledOnly(campus.getDecryptedUid(), classId) : studentRepository.findByClassId(campus.getDecryptedUid(), classId);
		return students;
	}

	public List<StudentModel> findWithFilter(CampusModel campus, String classId, String nameFilter, Integer rowCount) {
		if (nameFilter == null)
			nameFilter = "";
		return studentRepository.findWithFilter(campus.getDecryptedUid(), classId, nameFilter, rowCount == null || rowCount == Integer.MAX_VALUE ? 10 : rowCount);
	}

	@SuppressWarnings("unchecked")
	public List<StudentModel> getStudents(CampusModel campus, String orderBy, String filter, Date from, Date to, Integer count, Integer page){
		final String selectStatement = 
				"SELECT registerUid, campusUid, userUid, name, nationalId, birthDate, gender, studentNo, schoolYear, grade, "
						+ "CAST(COLUMN_JSON(roles) AS CHAR) AS roles, "
						+ "CAST(COLUMN_JSON(registerProperties) AS CHAR) AS registerProperties, "
						+ "CAST(COLUMN_JSON(userProperties) AS CHAR) AS userProperties, "
						+ "CAST(COLUMN_JSON(settings) AS CHAR) AS settings, "
						+ "userEnabled, registerEnabled, userLastUpdated, registerLastUpdated, userRegisteredAt, registerRegisteredAt FROM tv_student";
		
		StringBuilder sb = new StringBuilder(selectStatement);
		
		StringBuilder sbWhere = new StringBuilder(" WHERE campusUid = UNHEX('"+campus.getDecryptedUid()+"')");
		
		JsonElement jElement = XcJsonUtils.toJsonElement(filter);
		if (jElement != null && jElement.isJsonObject()){
			JsonObject jFilter = jElement.getAsJsonObject();
			for(Entry<String, JsonElement> entry : jFilter.entrySet()){
				if (entry.getValue() == null || entry.getValue().isJsonNull())
					continue;
				final String value = entry.getValue().getAsString();
				if (XcStringUtils.isNullOrEmpty(value))
					continue;
				if (sbWhere.length() == 0){
					sbWhere.append(" WHERE ");
				}else
					sbWhere.append(" AND ");
				if (entry.getKey().startsWith("userProperties")){
					String key = "COLUMN_GET(userProperties, '"+XcUrlUtils.getPropertyKey(entry.getKey())+"' AS CHAR)";
					sbWhere.append(key);
				}else if (entry.getKey().startsWith("registerProperties")){
					String key = "COLUMN_GET(registerProperties, '"+XcUrlUtils.getPropertyKey(entry.getKey())+"' AS CHAR)";
					sbWhere.append(key);
				}else{
					sbWhere.append(entry.getKey());
				}
				sbWhere.append(" like '%");
				sbWhere.append(value);
				sbWhere.append("%' ");
			}			
		}
		if (sbWhere.length() > 0)
			sb.append(sbWhere);
		
		jElement = XcJsonUtils.toJsonElement(orderBy);
		if (jElement != null && jElement.isJsonObject()){
			JsonObject jFilter = jElement.getAsJsonObject();
			StringBuilder sbOrderBy = new StringBuilder();
			for(Entry<String, JsonElement> entry : jFilter.entrySet()){
				if (sbOrderBy.length() == 0)
					sbOrderBy.append(" ORDER BY ");
				else
					sbOrderBy.append(", ");
				if (entry.getKey().startsWith("properties")){
					String key = "COLUMN_GET(properties, '"+ XcUrlUtils.getPropertyKey(entry.getKey())+"' AS CHAR)";
					sbOrderBy.append(key);
				}else if (entry.getKey().startsWith("registerProperties")){
					String key = "COLUMN_GET(registerProperties, '"+ XcUrlUtils.getPropertyKey(entry.getKey())+"' AS CHAR)";
					sbOrderBy.append(key);
				}else{
					sbOrderBy.append(entry.getKey());
				}
				sbOrderBy.append(" ");
				sbOrderBy.append(entry.getValue().getAsString());
			}
			if(sbOrderBy.length() > 0)
				sb.append(sbOrderBy);
		}
		
		//Page & Count 
		if (page != null && page>=1 && count != null && count >=0){
			sb.append(" LIMIT ");
			sb.append(count*(page - 1));
			sb.append(", ");
			sb.append(count);
			sb.append(" ");
		}
		
		Query query = entityManagerHelper.query(sb.toString(), StudentModel.class);		
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public Integer getStudentsNumber(CampusModel campus, String filter, Date from, Date to){
		final String selectStatement = 
				"SELECT CAST(COUNT(*) AS INTEGER) AS total FROM tv_student";
		
		StringBuilder sb = new StringBuilder(selectStatement);
		
		StringBuilder sbWhere = new StringBuilder(" WHERE campusUid = UNHEX('"+campus.getDecryptedUid()+"')");
		
		JsonElement jElement = XcJsonUtils.toJsonElement(filter);
		if (jElement != null && jElement.isJsonObject()){
			JsonObject jFilter = jElement.getAsJsonObject();
			for(Entry<String, JsonElement> entry : jFilter.entrySet()){
				if (entry.getValue() == null || entry.getValue().isJsonNull())
					continue;
				final String value = entry.getValue().getAsString();
				if (XcStringUtils.isNullOrEmpty(value))
					continue;
				if (sbWhere.length() == 0){
					sbWhere.append(" WHERE ");
				}else
					sbWhere.append(" AND ");
				if (entry.getKey().startsWith("userProperties")){
					String key = "COLUMN_GET(userProperties, '"+XcUrlUtils.getPropertyKey(entry.getKey())+"' AS CHAR)";
					sbWhere.append(key);
				}else if (entry.getKey().startsWith("registerProperties")){
					String key = "COLUMN_GET(registerProperties, '"+XcUrlUtils.getPropertyKey(entry.getKey())+"' AS CHAR)";
					sbWhere.append(key);
				}else{
					sbWhere.append(entry.getKey());
				}
				sbWhere.append(" like '%");
				sbWhere.append(value);
				sbWhere.append("%' ");
			}			
		}
		if (sbWhere.length() > 0)
			sb.append(sbWhere);
		
		Query query = this.entityManagerHelper.query(sb.toString());
		Object object = query.getSingleResult();
		Number number = (Number)(object);
		return number.intValue();
	}

	public List<StudentMeasurementDataModel> getMeasurementData(String item){
		if (XcStringUtils.isValid(item))
			return studentMeasurementRepository.getMeasurementData(item);
		return studentMeasurementRepository.getMeasurementData();
	}
	

	@SuppressWarnings("unchecked")
	public List<StudentMeasurementDataModel> getMeasurementData(CampusModel campus, String item, String orderBy, String filter, Date from, Date to, Integer count, Integer page){
		final String selectStatement = "SELECT dataUid, userUid, name, nationalId, birthDate, gender, studentNo, schoolYear, grade, "
				+ "CAST(COLUMN_JSON(registerProperties) AS CHAR) AS registerProperties, "
				+ "CAST(COLUMN_JSON(properties) AS CHAR) AS properties, "
				+ "CAST(COLUMN_JSON(registeredBy) AS CHAR) AS registeredBy, "
				+ "lastUpdated, registeredAt FROM tv_measurement_data"; // WHERE COLUMN_EXISTS(properties, ?1)"

		
		StringBuilder sb = new StringBuilder(selectStatement);
		
		StringBuilder sbWhere = new StringBuilder();
		if (item!=null){
			sbWhere.append(" WHERE COLUMN_EXISTS(properties, '");
			sbWhere.append(item);
			sbWhere.append("') ");
		}
		if (sbWhere.length() == 0){
			sbWhere.append(" WHERE ");
		}
		
		sbWhere.append(" campusUid = UNHEX('"+campus.getDecryptedUid()+"') ");
		
		JsonElement jElement = XcJsonUtils.toJsonElement(filter);
		if (jElement != null && jElement.isJsonObject()){
			JsonObject jFilter = jElement.getAsJsonObject();
			for(Entry<String, JsonElement> entry : jFilter.entrySet()){
				final String value = entry.getValue().getAsString();
				if (XcStringUtils.isNullOrEmpty(value))
					continue;
				if (sbWhere.length() == 0){
					sbWhere.append(" WHERE ");
				}else
					sbWhere.append(" AND ");
				final String entryKey = entry.getKey(); 
				if (entryKey.equals("userUid")){
					sbWhere.append(String.format("userUid = UNHEX('%s') ", EncodeUtils.decrypt(value)));
					continue;
				}
				if (entryKey.startsWith("properties")){
					String key = "COLUMN_GET(properties, '"+XcUrlUtils.getPropertyKey(entryKey)+"' AS CHAR)";
					sbWhere.append(key);
				}else if (entryKey.startsWith("registerProperties")){
					String key = "COLUMN_GET(registerProperties, '"+XcUrlUtils.getPropertyKey(entryKey)+"' AS CHAR)";
					sbWhere.append(key);
				}else{
					sbWhere.append(entryKey);
				}
				sbWhere.append(" like '%");
				sbWhere.append(value);
				sbWhere.append("%' ");
			}			
		}
		if (from != null){
			if (sbWhere.length() == 0){
				sbWhere.append(" WHERE ");
			}else
				sbWhere.append(" AND ");
			sbWhere.append(" registeredAt >= '");
			sbWhere.append(XcDateTimeUtils.toString(new DateTime(from), "yyyy-MM-dd"));
			sbWhere.append("' ");
		}
		
		if (to != null){
			if (sbWhere.length() == 0){
				sbWhere.append(" WHERE ");
			}else
				sbWhere.append(" AND ");
			sbWhere.append(" registeredAt <= '");
			sbWhere.append(XcDateTimeUtils.toString(new DateTime(to), "yyyy-MM-dd"));
			sbWhere.append("' ");
		}
		
		if (sbWhere.length() > 0)
			sb.append(sbWhere);
		
		jElement = XcJsonUtils.toJsonElement(orderBy);
		if (jElement != null && jElement.isJsonObject()){
			JsonObject jFilter = jElement.getAsJsonObject();
			StringBuilder sbOrderBy = new StringBuilder();
			for(Entry<String, JsonElement> entry : jFilter.entrySet()){
				if (sbOrderBy.length() == 0)
					sbOrderBy.append(" ORDER BY ");
				else
					sbOrderBy.append(", ");
				if (entry.getKey().startsWith("properties")){
					String key = "COLUMN_GET(properties, '"+ XcUrlUtils.getPropertyKey(entry.getKey())+"' AS CHAR)";
					sbOrderBy.append(key);
				}else if (entry.getKey().startsWith("registerProperties")){
					String key = "COLUMN_GET(registerProperties, '"+ XcUrlUtils.getPropertyKey(entry.getKey())+"' AS CHAR)";
					sbOrderBy.append(key);
				}else{
					sbOrderBy.append(entry.getKey());
				}
				sbOrderBy.append(" ");
				sbOrderBy.append(entry.getValue().getAsString());
			}
			if(sbOrderBy.length() > 0)
				sb.append(sbOrderBy);
		}
		
		Query query = entityManagerHelper.query(sb.toString(), StudentMeasurementDataModel.class);		
		return query.getResultList();
	}

	
	@SuppressWarnings("unchecked")
	public List<StudentCareDataModel> getCareData(CampusModel campus, String item, String orderBy, String filter, Date from, Date to, Integer count, Integer page){
		final String selectStatement = "SELECT dataUid, userUid, name, nationalId, birthDate, gender, studentNo, schoolYear, grade, deptId, nurseUid, nurseName, regNo, comment, hasTreatment, "
				+ "CAST(COLUMN_JSON(registerProperties) AS CHAR) AS registerProperties, "
				+ "CAST(COLUMN_JSON(properties) AS CHAR) AS properties, "
				+ "CAST(COLUMN_JSON(registeredBy) AS CHAR) AS registeredBy, "
				+ "lastUpdated, registeredAt FROM tv_care_data"; // WHERE COLUMN_EXISTS(properties, ?1)"

		
		StringBuilder sb = new StringBuilder(selectStatement);
		
		StringBuilder sbWhere = new StringBuilder();
		if (item!=null){
			sbWhere.append(" WHERE COLUMN_EXISTS(properties, '");
			sbWhere.append(item);
			sbWhere.append("') ");
		}
		
		if (sbWhere.length() == 0){
			sbWhere.append(" WHERE ");
		}
		
		sbWhere.append(" campusUid = UNHEX('"+campus.getDecryptedUid()+"') ");
		
		JsonElement jElement = XcJsonUtils.toJsonElement(filter);
		if (jElement != null && jElement.isJsonObject()){
			JsonObject jFilter = jElement.getAsJsonObject();
			for(Entry<String, JsonElement> entry : jFilter.entrySet()){
				final String value = entry.getValue().getAsString();
				if (XcStringUtils.isNullOrEmpty(value))
					continue;
				if (sbWhere.length() == 0){
					sbWhere.append(" WHERE ");
				}else
					sbWhere.append(" AND ");
				
				final String entryKey = entry.getKey(); 
				if (entryKey.equals("userUid")){
					sbWhere.append(String.format("userUid = UNHEX('%s') ", EncodeUtils.decrypt(value)));
					continue;
				}
				if (entryKey.startsWith("properties")){
					String key = "COLUMN_GET(properties, '"+XcUrlUtils.getPropertyKey(entryKey)+"' AS CHAR)";
					sbWhere.append(key);
				}else if (entryKey.startsWith("registerProperties")){
					String key = "COLUMN_GET(registerProperties, '"+XcUrlUtils.getPropertyKey(entryKey)+"' AS CHAR)";
					sbWhere.append(key);
				}else{
					sbWhere.append(entryKey);
				}
				sbWhere.append(" like '%");
				sbWhere.append(value);
				sbWhere.append("%' ");
			}			
		}
		if (sbWhere.length() > 0)
			sb.append(sbWhere);
		
		jElement = XcJsonUtils.toJsonElement(orderBy);
		if (jElement != null && jElement.isJsonObject()){
			JsonObject jFilter = jElement.getAsJsonObject();
			StringBuilder sbOrderBy = new StringBuilder();
			for(Entry<String, JsonElement> entry : jFilter.entrySet()){
				if (sbOrderBy.length() == 0)
					sbOrderBy.append(" ORDER BY ");
				else
					sbOrderBy.append(", ");
				if (entry.getKey().startsWith("properties")){
					String key = "COLUMN_GET(properties, '"+ XcUrlUtils.getPropertyKey(entry.getKey())+"' AS CHAR)";
					sbOrderBy.append(key);
				}else if (entry.getKey().startsWith("registerProperties")){
					String key = "COLUMN_GET(registerProperties, '"+ XcUrlUtils.getPropertyKey(entry.getKey())+"' AS CHAR)";
					sbOrderBy.append(key);
				}else{
					sbOrderBy.append(entry.getKey());
				}
				sbOrderBy.append(" ");
				sbOrderBy.append(entry.getValue().getAsString());
			}
			if(sbOrderBy.length() > 0)
				sb.append(sbOrderBy);
		}else{
			sb.append(" ORDER BY registeredAt DESC");
		}
		
		//Page & Count 
		if (page != null && page>=1 && count != null && count >=0){
			sb.append(" LIMIT ");
			sb.append(count*(page - 1));
			sb.append(", ");
			sb.append(count);
			sb.append(" ");
		}
		
		Query query = entityManagerHelper.query(sb.toString(), StudentCareDataModel.class);
		List<StudentCareDataModel> list = query.getResultList();
		for(StudentCareDataModel careData : list){
			if (careData.getNurseUid() != null){
				careData.setNurseName(userService.getUser(careData.getNurseUid()).getName());
			}
		}
		return list;
	}
	
	public Integer getCareNumber(CampusModel campus, String item, String filter, Date from, Date to){
		final String selectStatement = 
				"SELECT CAST(COUNT(*) AS INTEGER) AS total FROM tv_care_data";
				
		StringBuilder sb = new StringBuilder(selectStatement);
		
		StringBuilder sbWhere = new StringBuilder();
		if (item!=null){
			sbWhere.append(" WHERE COLUMN_EXISTS(properties, '");
			sbWhere.append(item);
			sbWhere.append("') ");
		}
		
		if (sbWhere.length() == 0){
			sbWhere.append(" WHERE ");
		}
		
		sbWhere.append(" campusUid = UNHEX('"+campus.getDecryptedUid()+"') ");
		
		JsonElement jElement = XcJsonUtils.toJsonElement(filter);
		if (jElement != null && jElement.isJsonObject()){
			JsonObject jFilter = jElement.getAsJsonObject();
			for(Entry<String, JsonElement> entry : jFilter.entrySet()){
				final String value = entry.getValue().getAsString();
				if (XcStringUtils.isNullOrEmpty(value))
					continue;
				if (sbWhere.length() == 0){
					sbWhere.append(" WHERE ");
				}else
					sbWhere.append(" AND ");
				
				final String entryKey = entry.getKey(); 
				if (entryKey.equals("userUid")){
					sbWhere.append(String.format("userUid = UNHEX('%s') ", EncodeUtils.decrypt(value)));
					continue;
				}
				if (entryKey.startsWith("properties")){
					String key = "COLUMN_GET(properties, '"+XcUrlUtils.getPropertyKey(entryKey)+"' AS CHAR)";
					sbWhere.append(key);
				}else if (entryKey.startsWith("registerProperties")){
					String key = "COLUMN_GET(registerProperties, '"+XcUrlUtils.getPropertyKey(entryKey)+"' AS CHAR)";
					sbWhere.append(key);
				}else{
					sbWhere.append(entryKey);
				}
				sbWhere.append(" like '%");
				sbWhere.append(value);
				sbWhere.append("%' ");
			}			
		}
		if (sbWhere.length() > 0)
			sb.append(sbWhere);
		
		Query query = entityManagerHelper.query(sb.toString());
		Object object = query.getSingleResult();
		Number number = (Number)(object);
		
		return number.intValue();
	}
	
	public JsonObject getBmiReport(String campusId, String classId, String gender){
		final String selectStatement = "SELECT category, count(category) AS count FROM "
				+ "(SELECT *, "
				+ "CASE WHEN fatLevel IS NULL THEN 'unknown' "
				+ "WHEN fatLevel IS NOT NULL THEN fatLevel "
				+ "END AS category "
				+ "FROM "
				+ "( "
				+ "SELECT "
				+ "	student.gender AS gender, " 
				+ "	student.grade AS grade, "
				+ "	student.schoolYear AS schoolYear, "
				+ "	COLUMN_GET(student.registerProperties, 'classId' AS CHAR) AS classId, "
				+ "	LOWER(COLUMN_GET(measurement.properties, 'fatLevel' AS CHAR)) AS fatLevel,  "
				+ "	MAX(measurement.registeredAt) AS registeredAt  "
				+ "FROM  "
				+ "tv_student student "
				+ "LEFT JOIN  "
				+ "(SELECT * FROM tb_measurement_data WHERE COLUMN_EXISTS(properties, 'fatLevel')) measurement on measurement.userUid = student.userUid "
				+ "where student.campusUid = UNHEX('" + EncodeUtils.decrypt(campusId) + "') "
				+ "GROUP BY student.userUid "
				+ ") studentMeasurement "
				+ ") statistics "
				;
		final StringBuilder sb = new StringBuilder(selectStatement);
		StringBuilder sbWhere = this.getWhereClause(null,  null,  null,  classId, gender);

		if (sbWhere.length() > 0)
			sb.append(sbWhere);
		sb.append(" GROUP BY category");
		
		final Query query = entityManagerHelper.query(sb.toString());
		final List<?> list = query.getResultList();
		final JsonObject jResult = new JsonObject();
		for(Object element : list){
			Object[] data = (Object[])element;
			jResult.addProperty((String)data[0], (Number)data[1]);
		}
		return jResult;
	}
	
	public JsonObject getDiseaseReport(String campusUid, String deptId, Date fromDate, Date toDate, String classId, String gender){
		final String selectStatement = "SELECT code, "
				+ "CASE "
				+ "WHEN properties IS NULL THEN 0 "
				+ "ELSE count(*) "
				+ "END AS count, "
				+ "CAST(COLUMN_JSON(resource) AS CHAR) AS reseource "
				+ "FROM tv_disease_statistics "
				;
		
		final StringBuilder sb = new StringBuilder(selectStatement);
	
		final StringBuilder sbWhere = this.getWhereClause(deptId, fromDate, toDate, classId, gender);
		if (sbWhere.length() > 0){
			sb.append(sbWhere);
			sb.append(" and ");
		}else{
			sb.append(" where ");
		}
		
		sb.append(" campusUid = UNHEX('");
		sb.append(EncodeUtils.decrypt(campusUid));
		sb.append("') ");
		sb.append(" GROUP BY code ORDER BY count DESC");
		
		Query query = entityManagerHelper.query(sb.toString());
		List<?> list = query.getResultList();
		JsonObject jResult = new JsonObject();
		for(Object element : list){
			Object[] data = (Object[])element;

			JsonObject jData = new JsonObject();
			jData.addProperty("count", (Number)data[1]);
			jData.add("resource", XcJsonUtils.toJsonElement((String)data[2]));
			
			jResult.add((String)data[0], jData);
		}
		return jResult;
	}

	public JsonObject getPlaceReport(String campusUid, Date fromDate, Date toDate, String classId, String gender){
		final String selectStatement = "SELECT code, "
				+ "CASE "
				+ "WHEN properties IS NULL THEN 0 "
				+ "ELSE count(*) "
				+ "END AS count, "
				+ "CAST(COLUMN_JSON(resource) AS CHAR) AS reseource "
				+ "FROM tv_place_statistics "
				;
		
		final StringBuilder sb = new StringBuilder(selectStatement);
	
		final StringBuilder sbWhere = this.getWhereClause(null, fromDate, toDate, classId, gender);
		if (sbWhere.length() > 0){
			sb.append(sbWhere);
			sb.append(" and ");
		}else{
			sb.append(" where ");
		}
		
		sb.append(" campusUid = UNHEX('");
		sb.append(EncodeUtils.decrypt(campusUid));
		sb.append("') ");
		
		sb.append(" GROUP BY code ORDER BY count DESC");
		
		Query query = entityManagerHelper.query(sb.toString());
		List<?> list = query.getResultList();
		JsonObject jResult = new JsonObject();
		for(Object element : list){
			Object[] data = (Object[])element;

			JsonObject jData = new JsonObject();
			jData.addProperty("count", (Number)data[1]);
			jData.add("resource", XcJsonUtils.toJsonElement((String)data[2]));
			
			jResult.add((String)data[0], jData);
		}
		return jResult;
	}
	
	public String getTemplatePath(String templateFileName){
		return this.filesRoot + "/template/" + templateFileName;
	}

	private StringBuilder getWhereClause(String category, Date fromDate, Date toDate, String classId, String gender){
		final StringBuilder sbWhere = new StringBuilder();
		if (XcStringUtils.isValid(category)){
			if (sbWhere.length() == 0){
				sbWhere.append(" WHERE ");
			}else{
				sbWhere.append(" AND ");
			}
			sbWhere.append(" category = '");
			sbWhere.append(category);
			sbWhere.append("'");
		}
		
		final StringBuilder sbPeriod = new StringBuilder();
		if (fromDate != null){
			if (sbPeriod.length() == 0){
				sbPeriod.append(" (");
			}else{
				sbPeriod.append(" AND ");
			}
			sbPeriod.append(" registeredAt >= '");
			sbPeriod.append(new SimpleDateFormat("yyyy-MM-dd").format(fromDate));
			sbPeriod.append("'");
		}

//		if (toDate != null){
//			if (sbPeriod.length() == 0){
//				sbPeriod.append(" ( ");
//			}else{
//				sbPeriod.append(" AND ");
//			}
//			sbPeriod.append(" registeredAt < '");
//			sbPeriod.append(new SimpleDateFormat("yyyy-MM-dd").format(toDate));
//			sbPeriod.append("'");
//		}
//		if (sbPeriod.length() > 0){
//			sbPeriod.append(" OR registeredAt IS NULL) ");
//			
//			if (sbWhere.length() == 0){
//				sbWhere.append(" WHERE ");
//			}else{
//				sbWhere.append(" AND ");
//			}
//			
//			sbWhere.append(sbPeriod);
//		}
//		
		
		if (fromDate != null){
			if (sbWhere.length() == 0){
				sbWhere.append(" WHERE ");
			}else{
				sbWhere.append(" AND ");
			}
			sbWhere.append(" registeredAt >= '");
			sbWhere.append(new SimpleDateFormat("yyyy-MM-dd").format(fromDate));
			sbWhere.append("'");
		}

		if (toDate != null){
			if (sbWhere.length() == 0){
				sbWhere.append(" WHERE ");
			}else{
				sbWhere.append(" AND ");
			}
			sbWhere.append(" registeredAt < '");
			sbWhere.append(new SimpleDateFormat("yyyy-MM-dd").format(new DateTime(toDate).plusDays(1).toDate()));
			sbWhere.append("'");
		}

		
		
		if (XcStringUtils.isValid(classId)){
			if (sbWhere.length() == 0){
				sbWhere.append(" WHERE ");
			}else{
				sbWhere.append(" AND ");
			}
			sbWhere.append(" classId = '");
			sbWhere.append(classId);
			sbWhere.append("'");
		}
		
		if (XcStringUtils.isValid(gender)){
			if (sbWhere.length() == 0){
				sbWhere.append(" WHERE ");
			}else{
				sbWhere.append(" AND ");
			}
			sbWhere.append("gender = '");
			sbWhere.append(gender);
			sbWhere.append("'");
		}
		
		return sbWhere;
	}
}
