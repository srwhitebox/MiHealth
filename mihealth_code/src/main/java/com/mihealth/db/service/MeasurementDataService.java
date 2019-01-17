package com.mihealth.db.service;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.persistence.Query;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.mihealth.db.model.MeasurementDataModel;
import com.mihealth.db.repositories.MeasurementDataRepository;
import com.mihealth.db.repositories.PropertyRepository;
import com.mihealth.db.utils.EncodeUtils;
import com.mihealth.websocket.SocketSessionManager;
import com.ximpl.lib.constant.DbConst;
import com.ximpl.lib.constant.GeneralConst;
import com.ximpl.lib.db.EntityManagerHelper;
import com.ximpl.lib.util.XcJsonUtils;
import com.ximpl.lib.util.XcStringUtils;

@Service
public class MeasurementDataService {
	@Autowired
	private EntityManagerHelper entityManagerHelper;

	@Autowired
	PropertyRepository propertyRepository;

	@Autowired
	MeasurementDataRepository measurementRepository;

	@Autowired
	private SocketSessionManager sessionManager;
	
	@Autowired
	private ResourceService resourceService;

	public void save(MeasurementDataModel measurementData){
		final String queryText = "REPLACE INTO tb_measurement_data(uid, campusUid, userUid, properties, registeredBy, lastUpdated, registeredAt) "
				+ "values (UNHEX(:uid), UNHEX(:campusUid), UNHEX(:userUid),"
				+ XcJsonUtils.toJsonCreate(measurementData.getProperties()) + ", " + XcJsonUtils.toJsonCreate(measurementData.getRegisteredBy()) 
				+ ", :lastUpdated, :registeredAt)";
		Query query = entityManagerHelper.query(queryText);
		String campusUid = measurementData.getDecryptedCampusUid();
		query.setParameter(DbConst.FIELD_CAMPUS_UID, campusUid);
		query.setParameter(DbConst.FIELD_UID, measurementData.getDecryptedUid());
		query.setParameter(DbConst.FIELD_USER_UID, measurementData.getDecryptedUserUid());
		query.setParameter(DbConst.FIELD_LAST_UPDATED, DateTime.now(DateTimeZone.UTC).toDate());
		query.setParameter(DbConst.FIELD_REGISTERED_AT, measurementData.getRegisteredAt());
		entityManagerHelper.execute(query);
		
		sessionManager.onNewRecord(measurementData);
	}
	
	public void save(String uid, JsonObject jProperties){
		final String queryText = "UPDATE tb_measurement_data SET properties =  " + XcJsonUtils.toJsonCreate(jProperties) + ", lastUpdated = :lastUpdated where uid = UNHEX(:uid)";
		Query query = entityManagerHelper.query(queryText);
		query.setParameter(DbConst.FIELD_UID, EncodeUtils.decrypt(uid));
		query.setParameter(DbConst.FIELD_LAST_UPDATED, DateTime.now(DateTimeZone.UTC).toDate());
		
		entityManagerHelper.execute(query);
	}
	
	/**
	 * Get a record for given UID
	 * @param uid
	 * @return
	 */
	public MeasurementDataModel get(String uid){
		return measurementRepository.findByUid(EncodeUtils.decrypt(uid));
	}
	
	/**
	 * Return records for given user UID with the conditions item, from, to
	 * @param userUid
	 * @param itemCode
	 * @param fromDate
	 * @param toDate
	 * @return       
	 */
	@SuppressWarnings("unchecked")
	public List<MeasurementDataModel> get(String userUid, String itemCode, Date fromDate, Date toDate){
		final String selectQuery = "SELECT uid, campusUid, userUid, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, CAST(COLUMN_JSON(registeredBy) AS CHAR) AS registeredBy, lastUpdated, registeredAt FROM tb_measurement_data";

		final Query query = getQuery(selectQuery, userUid, itemCode, fromDate, toDate);
		
		return query.getResultList();
	}
	
	public Double getLatest(String userUid, String itemCode){
		final String selectQuery = "SELECT COLUMN_GET(properties, :itemCode AS DOUBLE) as value FROM tb_measurement_data WHERE userUid = UNHEX(:userUid) AND COLUMN_EXISTS(properties, :itemCode) ORDER BY registeredAt LIMIT 1";
		
		final Query query = entityManagerHelper.query(String.format(selectQuery, userUid, itemCode));
		query.setParameter("itemCode", itemCode);
		query.setParameter(DbConst.FIELD_USER_UID, EncodeUtils.decrypt(userUid));
		try{
			return (Double)query.getSingleResult();
		}catch(Exception ex){
			return null;
		}
	}

	/**
	 * Delete a record for give UID
	 * @param uid
	 */
	public void deleteByUid(String uid){
		measurementRepository.deleteByUid(EncodeUtils.decrypt(uid));
	}
	
	/**
	 * Delete all records for give user UID
	 * @param userUid
	 */
	public void deleteByUser(String userUid){
		measurementRepository.deleteByUserUid(EncodeUtils.decrypt(userUid));
	}
	
	private Query getQuery(String selectQuery, String userUid, String itemCode, Date fromDate, Date toDate){
		final StringBuilder sb = new StringBuilder(selectQuery);
		
		boolean hasCondition = false;
		
		if (XcStringUtils.isValid(userUid)){
			if (!hasCondition){
				sb.append(" WHERE ");
			}else{
				sb.append(" AND ");
			}
			hasCondition = true;
			sb.append(" userUid = UNHEX(:userUid) ");
		}
		// If the item which will be searched is defined..
		if (!XcStringUtils.isNullOrEmpty(itemCode)){
			if (!hasCondition){
				sb.append(" WHERE ");
				hasCondition = true;
			}else{
				sb.append(" AND ");
			}
			hasCondition = true;
			sb.append(" COLUMN_EXISTS(properties, :itemCode)");
		}
		
		if (fromDate != null){
			if (!hasCondition){
				sb.append(" WHERE ");
				hasCondition = true;
			}else{
				sb.append(" AND ");
			}
			hasCondition = true;
			sb.append(" registeredAt > :fromDate");
		}

		if (toDate != null){
			if (!hasCondition){
				sb.append(" WHERE ");
			}else{
				sb.append(" AND ");
			}
			hasCondition = true;
			sb.append(" registeredAt < :toDate");
		}
		
		sb.append(" ORDER BY registeredAt");

		final Query query = entityManagerHelper.query(sb.toString(), MeasurementDataModel.class);
		if (XcStringUtils.isValid(userUid)){
			query.setParameter(DbConst.FIELD_USER_UID, EncodeUtils.decrypt(userUid));
		}
		if (XcStringUtils.isValid(itemCode))
			query.setParameter("itemCode", itemCode);
		if (fromDate != null)
			query.setParameter("fromDate", fromDate);
		if (toDate != null)
			query.setParameter("toDate", toDate);
		
		return query;

	}
	
	public String getBmiLevelDesc(String language, int level) {
		//final String language = resourceService.getLanguageCode(locale);
		final String category = GeneralConst.CATEGORY_APP;
		
		switch(level) {
		case 0:
			return resourceService.get(language, category, "bmi_under");
		case 1:
			return resourceService.get(language, category, "bmi_normal");
		case 2:
			return resourceService.get(language, category, "bmi_over");
		case 3:
			return resourceService.get(language, category, "bmi_obesity");
		case 4:
			return resourceService.get(language, category, "bmi_obese");
		default:
			return "";
		}
	}
}
