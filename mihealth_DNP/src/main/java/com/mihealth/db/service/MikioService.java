package com.mihealth.db.service;

import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.mihealth.db.model.BatchStudentModel;
import com.mihealth.db.model.MikioModel;
import com.mihealth.db.repositories.BatchStudentRepository;
import com.mihealth.db.repositories.MikioRepository;
import com.mihealth.db.utils.EncodeUtils;
import com.mihealth.websocket.SocketSessionManager;
import com.ximpl.lib.constant.DbConst;
import com.ximpl.lib.db.EntityManagerHelper;
import com.ximpl.lib.util.XcJsonUtils;

@Service
public class MikioService {
	@Autowired
	private EntityManagerHelper entityManagerHelper;
	
	@Autowired
	private MikioRepository mikioRepository;
	
	@Autowired
	private SocketSessionManager sessionManager;
	
	@Autowired
	private BatchStudentRepository batchStudentRepository;
	
	public void save(MikioModel mikio) {
		final String queryText = "REPLACE INTO tb_mikio(uid, mikioId, campusUid, nurseUid, properties, enabled, lastUpdated, registeredAt) "
				+ "values(UNHEX(:uid), :mikioId, UNHEX(:campusUid), UNHEX(:nurseUid), "
				+ XcJsonUtils.toJsonCreate(mikio.getProperties()) + ", :enabled, " 
				+ ":lastUpdated, :registeredAt)";
		Query query = entityManagerHelper.query(queryText);
		query.setParameter(DbConst.FIELD_UID, mikio.getDecryptedUid());
		query.setParameter(DbConst.FIELD_MIKIO_ID, mikio.getMikioId());
		query.setParameter(DbConst.FIELD_CAMPUS_UID, mikio.getDecryptedCampusUid());
		query.setParameter(DbConst.FIELD_NURSE_UID, mikio.getDecryptedNurseUid());
		query.setParameter(DbConst.FIELD_ENABLED, mikio.isEnabled());
		query.setParameter(DbConst.FIELD_LAST_UPDATED, mikio.getLastUpdated());
		query.setParameter(DbConst.FIELD_REGISTERED_AT, mikio.getRegisteredAt());
		entityManagerHelper.execute(query);
		
		// Notify to mikio data has been updated...
		sessionManager.updateMikio(mikio);
	}
	
	public List<MikioModel> getAllByCampusUid(String campusUid){
		return mikioRepository.findAllByCampusUid(EncodeUtils.decrypt(campusUid));
	}
	
	public List<MikioModel> getAllByNurseUid(String nurseUid){
		return mikioRepository.findAllByNurseUid(EncodeUtils.decrypt(nurseUid));
	}

	public MikioModel getByUid(String uid) {
		String decryptedUid = EncodeUtils.decrypt(uid);
		return Strings.isNullOrEmpty(decryptedUid) ? null :  mikioRepository.findByUid(decryptedUid);
	}
	
	public MikioModel getById(String campusUid, String id) {
		return mikioRepository.findByMikioId(EncodeUtils.decrypt(campusUid), id);
	}
	
	public void delete(String uid) {
		mikioRepository.deleteByUid(EncodeUtils.decrypt(uid));
	}
	
	public void save(BatchStudentModel student) {
		final String queryFormat = "REPLACE INTO tb_batch_student(mikioUid, studentUid, measurementOrder, isAbsent) "
				+ "values (UNHEX(:mikioUid), UNHEX(:studentUid), :measurementOrder, :isAbsent"
				+ ")";
		
		Query query = entityManagerHelper.query(queryFormat);
		query.setParameter("mikioUid", EncodeUtils.decrypt(student.getMikioUid()));
		query.setParameter("studentUid", EncodeUtils.decrypt(student.getStudentUid()));
		query.setParameter("measurementOrder", student.getMeasurementOrder());
		query.setParameter("isAbsent", student.getIsAbsent() == null ? false : student.getIsAbsent());
		
		entityManagerHelper.execute(query);
	}
	
	public void pushBatchStudent(List<BatchStudentModel> students) {
		if (students == null)
			return;
		
		for(BatchStudentModel student: students) {
			save(student);
		}
		
//		batchStudentRepository.save(students);
	}
	
	public List<BatchStudentModel> getStudents(String mikioUid){
		return batchStudentRepository.findAllByMikioUid(EncodeUtils.decrypt(mikioUid));
	}
	
	public List<BatchStudentModel> getStudents(String mikioUid, Boolean isAbsent){
		if (isAbsent == null)
			return getStudents(mikioUid);
		else
			return batchStudentRepository.findAllByMikioUidAndIsAbsent(EncodeUtils.decrypt(mikioUid), isAbsent);
	}
	
	public void resetStudents(String mikioUid) {
		MikioModel mikio = mikioRepository.findByUid(EncodeUtils.decrypt(mikioUid));
		mikio.getProperties().remove("grade");
		mikio.getProperties().remove("classId");
		this.save(mikio);
		batchStudentRepository.deleteByMikioUid(EncodeUtils.decrypt(mikioUid));
	}
}
