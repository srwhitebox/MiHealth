package com.mihealth.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.mihealth.db.model.MeasurementDataModel;

public interface MeasurementDataRepository extends JpaRepository<MeasurementDataModel, String>{
	@Query(value = "SELECT uid, userUid, campusUid, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, CAST(COLUMN_JSON(registeredBy) AS CHAR) AS registeredBy, lastUpdated, registeredAt FROM tb_measurement_data WHERE uid = UNHEX(?1)", nativeQuery = true)
	MeasurementDataModel findByUid(String uid);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM tb_measurement_data WHERE uid = UNHEX(?1)", nativeQuery = true)
	void deleteByUid(String uid);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM tb_measurement_data WHERE userUid = UNHEX(?1)", nativeQuery = true)
	void deleteByUserUid(String userUid);
}
