package com.mihealth.db.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.mihealth.db.model.SchoolRegisterModel;

public interface SchoolRegisterRepository extends JpaRepository<SchoolRegisterModel, String>{

	@Query(value = "SELECT uid, campusUid, userUid, studentNo, schoolYear, grade, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, enabled, lastUpdated, registeredAt FROM tb_school_register WHERE uid = UNHEX(?1)", nativeQuery = true)
	SchoolRegisterModel getByUid(String decrypt);

	@Query(value = "SELECT uid, campusUid, userUid, studentNo, schoolYear, grade, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, enabled, lastUpdated, registeredAt FROM tb_school_register WHERE userUid = UNHEX(?1)", nativeQuery = true)
	SchoolRegisterModel getByUserUid(String userUid);

	@Query(value = "SELECT uid, campusUid, userUid, studentNo, schoolYear, grade, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, enabled, lastUpdated, registeredAt FROM tb_school_register WHERE studentNo = ?1 LIMIT 1", nativeQuery = true)
	SchoolRegisterModel getByRegNo(String studentNo);

	@Query(value = "SELECT uid, campusUid, userUid, studentNo, schoolYear, grade, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, enabled, lastUpdated, registeredAt FROM tb_school_register WHERE campusUid = UNHEX(?1)", nativeQuery = true)
	List<SchoolRegisterModel> findByCampusUid(String campusUid);

	@Query(value = "SELECT uid, campusUid, userUid, studentNo, schoolYear, grade, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, enabled, lastUpdated, registeredAt FROM tb_school_register WHERE campusUid = UNHEX(?1) AND enabled = ?2", nativeQuery = true)
	List<SchoolRegisterModel> findByCampusUidAndEnabled(String campusUid, Boolean enabled);

	@Query(value = "SELECT uid, campusUid, userUid, studentNo, schoolYear, grade, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, enabled, lastUpdated, registeredAt FROM tb_school_register WHERE campusUid = UNHEX(?1) AND schoolYear = ?2", nativeQuery = true)
	List<SchoolRegisterModel> findByCampusUidAndSchoolYear(String campusUid, Integer schoolYear);

	@Query(value = "SELECT uid, campusUid, userUid, studentNo, schoolYear, grade, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, enabled, lastUpdated, registeredAt FROM tb_school_register WHERE campusUid = UNHEX(?1) AND schoolYear = ?2 AND enabled = ?3", nativeQuery = true)
	List<SchoolRegisterModel> findByCampusUidAndSchoolYearAndEnabled(String campusUid, Integer schoolYear, Boolean enabled);

	@Query(value = "SELECT uid, campusUid, userUid, studentNo, schoolYear, grade, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, enabled, lastUpdated, registeredAt FROM tb_school_register WHERE campusUid = UNHEX(?1) AND grade = ?2", nativeQuery = true)
	List<SchoolRegisterModel> findByCampusUidAndGrade(String campusUid, Integer grade);

	@Query(value = "SELECT uid, campusUid, userUid, studentNo, schoolYear, grade, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, enabled, lastUpdated, registeredAt FROM tb_school_register WHERE campusUid = UNHEX(?1) AND grade = ?2 AND enabled = ?3", nativeQuery = true)
	List<SchoolRegisterModel> findByCampusUidAndGradeAndEnabled(String campusUid, Integer grade, Boolean enabled);

	@Modifying
	@Transactional
	@Query(value = "UPDATE tb_user SET enabled = ?2 WHERE uid = UNHEX(?1)", nativeQuery = true)
	void enableByUid(String uid, boolean enabled);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM tb_school_register WHERE uid = UNHEX(?1)", nativeQuery = true)
	void deleteByUid(String uid);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM tb_school_register WHERE userUid = UNHEX(?1)", nativeQuery = true)
	void deleteByUserUid(String userUid);
	
}
