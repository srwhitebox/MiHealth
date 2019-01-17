package com.mihealth.db.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.mihealth.db.model.MikioModel;

public interface MikioRepository extends JpaRepository<MikioModel, String>{
	@Query(value = "SELECT uid, mikioId, campusUid, nurseUid, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, CAST(COLUMN_JSON(students) AS CHAR) AS students, enabled, lastUpdated, registeredAt FROM tb_mikio where campusUid = UNHEX(?1)", nativeQuery = true)
	List<MikioModel> findAllByCampusUid(String campusUid);
	
	@Query(value = "SELECT uid, mikioId, campusUid, nurseUid, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, CAST(COLUMN_JSON(students) AS CHAR) AS students, enabled, lastUpdated, registeredAt FROM tb_mikio where nurseUid = UNHEX(?1)", nativeQuery = true)
	List<MikioModel> findAllByNurseUid(String nurseUid);

	@Query(value = "SELECT uid, mikioId, campusUid, nurseUid, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, CAST(COLUMN_JSON(students) AS CHAR) AS students, enabled, lastUpdated, registeredAt FROM tb_mikio WHERE uid = UNHEX(?1)", nativeQuery = true)
	MikioModel findByUid(String uid);
	
	@Query(value = "SELECT uid, mikioId, campusUid, nurseUid, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, CAST(COLUMN_JSON(students) AS CHAR) AS students, enabled, lastUpdated, registeredAt FROM tb_mikio WHERE campusUid = UNHEX(?1) AND mikioId = ?2", nativeQuery = true)
	MikioModel findByMikioId(String campusUid, String id);
	
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM tb_mikio WHERE uid = UNHEX(?1)", nativeQuery = true)
	void deleteByUid(String uid);
		
}
