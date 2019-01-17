package com.mihealth.db.service;

import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mihealth.db.model.SchoolRegisterModel;
import com.mihealth.db.model.StudentModel;
import com.mihealth.db.repositories.SchoolRegisterRepository;
import com.mihealth.db.repositories.StudentRepository;
import com.mihealth.db.utils.EncodeUtils;
import com.ximpl.lib.constant.DbConst;
import com.ximpl.lib.db.EntityManagerHelper;
import com.ximpl.lib.util.XcJsonUtils;
import com.ximpl.lib.util.XcStringUtils;

@Service
public class SchoolService {
	@Autowired
	private EntityManagerHelper entityManagerHelper;

	@Autowired
	SchoolRegisterRepository schoolRegisterRepository;

	@Autowired
	StudentRepository studentRepository;

	public SchoolRegisterModel save(SchoolRegisterModel schoolRegister){
		final String queryFormat = "REPLACE INTO tb_school_register(uid, campusUid, userUid, studentNo, schoolYear, grade, properties, enabled, lastUpdated, registeredAt) "
				+ "values (UNHEX(:uid), UNHEX(:campusUuid), UNHEX(:userUid), :studentNo, :schoolYear, :grade, " 
				+ XcJsonUtils.toJsonCreate(schoolRegister.getProperties())
				+ ", :enabled, :lastUpdated, :registeredAt)";
		Query query = entityManagerHelper.query(queryFormat);
		
		query.setParameter(DbConst.FIELD_UID, schoolRegister.getDecryptedUid());
		query.setParameter("campusUuid", schoolRegister.getDecryptedCampusUid());
		query.setParameter(DbConst.FIELD_USER_UID, schoolRegister.getDecryptedUserUid());
		query.setParameter("studentNo", schoolRegister.getStudentNo());
		query.setParameter("schoolYear", schoolRegister.getSchoolYear());
		query.setParameter(DbConst.FIELD_GRADE, schoolRegister.getGrade());
		query.setParameter(DbConst.FIELD_ENABLED, schoolRegister.getEnabled());
		query.setParameter(DbConst.FIELD_LAST_UPDATED, schoolRegister.getLastUpdated());
		query.setParameter(DbConst.FIELD_REGISTERED_AT, schoolRegister.getRegisteredAt());
		entityManagerHelper.execute(query);
		
		return schoolRegister;
	}

	public SchoolRegisterModel getByUid(String uid){
		return schoolRegisterRepository.getByUid(EncodeUtils.decrypt(uid));
	}

	public SchoolRegisterModel getByUserUid(String userUid){
		return schoolRegisterRepository.getByUserUid(EncodeUtils.decrypt(userUid));
	}

	public SchoolRegisterModel getByRegNo(String regNo){
		return schoolRegisterRepository.getByRegNo(regNo);
	}

	public List<SchoolRegisterModel> getSchoolRegistersByCampusId(String campusUid, Boolean enabled){
		if (enabled != null)
			return schoolRegisterRepository.findByCampusUidAndEnabled(campusUid, enabled);
		else
			return schoolRegisterRepository.findByCampusUid(campusUid);
	}

	public List<SchoolRegisterModel> getSchoolRegistersBySchoolYear(String campusUid, Integer schoolYear, Boolean enabled){
		if (enabled != null)
			return schoolRegisterRepository.findByCampusUidAndSchoolYearAndEnabled(campusUid, schoolYear, enabled);
		else
			return schoolRegisterRepository.findByCampusUidAndSchoolYear(campusUid, schoolYear);
	}

	public List<SchoolRegisterModel> getSchoolRegistersByGrade(String campusUid, Integer grade, Boolean enabled){
		if (enabled != null)
			return schoolRegisterRepository.findByCampusUidAndGradeAndEnabled(campusUid, grade, enabled);
		else
			return schoolRegisterRepository.findByCampusUidAndGrade(campusUid, grade);
	}
	
	public void enable(SchoolRegisterModel schoolRegister, boolean enabled){
		schoolRegisterRepository.enableByUid(schoolRegister.getDecryptedUid(), enabled);
	}
	
	public void delete(SchoolRegisterModel schoolRegister){
		schoolRegisterRepository.deleteByUid(schoolRegister.getDecryptedUid());		
	}
	
	public void deleteByUserUid(String userUid){
		if (XcStringUtils.isValid(userUid)){
			schoolRegisterRepository.deleteByUserUid( EncodeUtils.decrypt(userUid));
		}
	}
	
	public List<StudentModel> getAllStudents(){
		
		return studentRepository.findAll();
	}
}
