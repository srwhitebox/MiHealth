package com.mihealth.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.mihealth.db.model.TreatmentDataModel;

public interface TreatmentDataRepository extends JpaRepository<TreatmentDataModel, String>{
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM tb_treatment_data WHERE uid = UNHEX(?1)", nativeQuery = true)
	void deleteByUid(String uid);
}
