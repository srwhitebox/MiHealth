package com.mihealth.db.service;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mihealth.db.model.CareDataModel;
import com.mihealth.db.model.TreatmentDetailedModel;
import com.mihealth.db.model.TreatmentDataModel;
import com.mihealth.db.repositories.CareDataRepository;
import com.mihealth.db.repositories.TreatmentDetailedDataRepository;
import com.mihealth.db.repositories.TreatmentDataRepository;
import com.mihealth.db.utils.EncodeUtils;
import com.mihealth.websocket.SocketSessionManager;
import com.ximpl.lib.constant.DbConst;
import com.ximpl.lib.db.EntityManagerHelper;
import com.ximpl.lib.util.XcJsonUtils;
import com.ximpl.lib.util.XcStringUtils;

@Service
public class CareDataService {
	@Autowired
	private EntityManagerHelper entityManagerHelper;

	@Autowired
	CareDataRepository careDataRepository;
	
	@Autowired
	TreatmentDataRepository treatmentDataRepository;
	
	@Autowired
	TreatmentDetailedDataRepository treatmentDetailsRepository;

	@Autowired
	private SocketSessionManager sessionManager;
	
	public void save(CareDataModel careData){
		final String queryText = "REPLACE INTO tb_care_data(uid, userUid, campusUid, deptId, nurseUid, regNo, properties, registeredBy, comment, lastUpdated, registeredAt) "
				+ "values(UNHEX(:uid), UNHEX(:userUid), UNHEX(:campusUid), :deptId, UNHEX(:nurseUid), :regNo, "
				+ XcJsonUtils.toJsonCreate(careData.getProperties()) + ", " 
				+ XcJsonUtils.toJsonCreate(careData.getRegisteredBy()) 
				+ ", :comment, :lastUpdated, :registeredAt)";
		Query query = entityManagerHelper.query(queryText);
		query.setParameter(DbConst.FIELD_UID, careData.getDecryptedUid());
		query.setParameter(DbConst.FIELD_USER_UID, careData.getDecryptedUserUid());
		query.setParameter(DbConst.FIELD_CAMPUS_UID, careData.getDecryptedCampusUid());
		query.setParameter(DbConst.FIELD_DEPT_ID, careData.getDeptId());
		query.setParameter(DbConst.FIELD_NURSE_UID, careData.getDecryptedManagerUid());
		query.setParameter(DbConst.FIELD_REG_NO, careData.getRegNo());
		query.setParameter(DbConst.FIELD_COMMENT, careData.getComment());
		query.setParameter(DbConst.FIELD_LAST_UPDATED, careData.getLastUpdated());
		query.setParameter(DbConst.FIELD_REGISTERED_AT, careData.getRegisteredAt());
		entityManagerHelper.execute(query);
		
		sessionManager.onNewRecord(careData);
	}

	
	public CareDataModel get(String uid){
		return careDataRepository.findByUid(EncodeUtils.decrypt(uid));
	}
	
	@SuppressWarnings("unchecked")
	public List<CareDataModel> get(String userUid, String deptId, String diseaseCode, Date fromDate, Date toDate){
		StringBuilder sb = new StringBuilder("SELECT uid, userUid, deptId, nurseUid, regNo, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, CAST(COLUMN_JSON(registeredBy) AS CHAR) AS registeredBy, comment, lastUpdated, registeredAt FROM tb_care_data");
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

		if (!XcStringUtils.isNullOrEmpty(deptId)){
			if (!hasCondition){
				sb.append(" WHERE ");
			}else{
				sb.append(" AND ");
			}
			hasCondition = true;
			sb.append(" deptId = :deptId");
		}

		// If the item which will be searched is defined..
		if (!XcStringUtils.isNullOrEmpty(diseaseCode)){
			if (!hasCondition){
				sb.append(" WHERE ");
			}else{
				sb.append(" AND ");
			}
			hasCondition = true;
			sb.append(" COLUMN_GET(properties, 'diseaseCode' AS CHAR) = :diseaseCode");
		}
		
		if (fromDate != null){
			if (!hasCondition){
				sb.append(" WHERE ");
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
		
		sb.append(" ORDER BY registeredAt DESC");

		Query query = entityManagerHelper.query(sb.toString(), CareDataModel.class);
		if (XcStringUtils.isValid(userUid)){
			query.setParameter(DbConst.FIELD_USER_UID, EncodeUtils.decrypt(userUid));
		}
		if (XcStringUtils.isValid(deptId))
			query.setParameter(DbConst.FIELD_DEPT_ID, deptId);
		if (XcStringUtils.isValid(diseaseCode))
			query.setParameter("diseaseCode", diseaseCode);
		if (fromDate != null)
			query.setParameter("fromDate", fromDate);
		if (toDate != null)
			query.setParameter("toDate", toDate);
		
		return query.getResultList();
	}
	
	public void delete(CareDataModel careData){
		final String uid = careData.getUid();
		deleteByUid(uid);
		removeTreatment(uid);
	}
	
	public void deleteByUid(String uid){
		careDataRepository.deleteByUid(EncodeUtils.decrypt(uid));
	}
	
	public void deleteByUser(String userUid){
		careDataRepository.deleteByUserUid(EncodeUtils.decrypt(userUid));
	}
	
	public void saveTreatment(TreatmentDataModel treatment){
		final String queryText = "REPLACE INTO tb_treatment_data(uid, careUid, nurseUid, treatment, status, lastUpdated, registeredAt) "
				+ "values(UNHEX(:uid), UNHEX(:careUid), UNHEX(:nurseUid), "
				+ XcJsonUtils.toJsonCreate(treatment.getTreatment()) +", :status, "
				+ ":lastUpdated, :registeredAt)";
		Query query = entityManagerHelper.query(queryText);
		query.setParameter(DbConst.FIELD_UID,  EncodeUtils.decrypt(treatment.getUid()));
		query.setParameter(DbConst.FIELD_CARE_UID, EncodeUtils.decrypt(treatment.getCareUid()));
		query.setParameter(DbConst.FIELD_NURSE_UID, EncodeUtils.decrypt(treatment.getNurseUid()));
//		query.setParameter(DbConst.FIELD_TREATMENT, treatment.getTreatment());
		query.setParameter(DbConst.FIELD_STATUS, treatment.getStatus());
		query.setParameter(DbConst.FIELD_LAST_UPDATED, treatment.getLastUpdated());
		query.setParameter(DbConst.FIELD_REGISTERED_AT, treatment.getRegisteredAt());
		entityManagerHelper.execute(query);
		
		sessionManager.onNewRecord(treatment);
//		treatmentDataRepository.save(treatment);
	}

	public List<TreatmentDetailedModel> getTreatments(String careUid){
		final String decryptedUid = EncodeUtils.decrypt(careUid);
		return treatmentDetailsRepository.findByCareUid(decryptedUid);
	}
	
	public void removeTreatment(TreatmentDataModel treatment){
		treatmentDataRepository.delete(treatment);
	}


	public void removeTreatment(String dataUid) {
		final String decryptedUid = EncodeUtils.decrypt(dataUid);
		treatmentDataRepository.deleteByUid(decryptedUid);
	}
}
