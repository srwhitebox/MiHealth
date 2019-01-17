package com.mihealth.db.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.mihealth.db.model.CampusModel;

public interface CampusRepository extends JpaRepository<CampusModel, String>{
	@Query(value = "SELECT uid, campusId, name, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, CAST(COLUMN_JSON(settings) AS CHAR) AS settings, authKey, enabled, lastUpdated, registeredAt FROM tb_campus", nativeQuery = true)
	List<CampusModel> findAll();
	
	@Query(value = "SELECT uid, campusId, name, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, CAST(COLUMN_JSON(settings) AS CHAR) AS settings, authKey, enabled, lastUpdated, registeredAt FROM tb_campus WHERE uid = UNHEX(?1)", nativeQuery = true)
	CampusModel findByUid(String uid);

	@Query(value = "SELECT uid, campusId, name, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, CAST(COLUMN_JSON(settings) AS CHAR) AS settings, authKey, enabled, lastUpdated, registeredAt FROM tb_campus WHERE campusId = ?1", nativeQuery = true)
	CampusModel findByCampusId(String campusId);

	
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM tb_campus WHERE uid = UNHEX(?1)", nativeQuery = true)
	void deleteByUid(String uid);

}
