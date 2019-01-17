package com.mihealth.db.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.mihealth.db.model.BatchStudentModel;

public interface BatchStudentRepository extends JpaRepository<BatchStudentModel, String>{
	@Query(value = "SELECT * FROM tv_batch_student WHERE mikioUid = UNHEX(?1) ORDER BY measurementOrder", nativeQuery = true)
	List<BatchStudentModel> findAllByMikioUid(String mikioUid);
	
	@Query(value = "SELECT * FROM tv_batch_student WHERE mikioUid = UNHEX(?1) AND isAbsent = ?2 ORDER BY measurementOrder", nativeQuery = true)
	List<BatchStudentModel> findAllByMikioUidAndIsAbsent(String mikioUid, Boolean isAbsent);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM tb_batch_student WHERE mikioUid = UNHEX(?1)", nativeQuery = true)
	void deleteByMikioUid(String mikioUid);
}
