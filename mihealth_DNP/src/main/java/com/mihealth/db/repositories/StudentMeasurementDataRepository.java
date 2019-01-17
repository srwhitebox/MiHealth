package com.mihealth.db.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mihealth.db.model.StudentMeasurementDataModel;
import com.mihealth.db.model.StudentModel;

public interface StudentMeasurementDataRepository extends JpaRepository<StudentMeasurementDataModel, String>{
	@Query(value = "SELECT dataUid, userUid, name, nationalId, birthDate, gender, studentNo, schoolYear, grade, "
			+ "CAST(COLUMN_JSON(registerProperties) AS CHAR) AS registerProperties, "
			+ "CAST(COLUMN_JSON(properties) AS CHAR) AS properties, "
			+ "CAST(COLUMN_JSON(registeredBy) AS CHAR) AS registeredBy, "
			+ "lastUpdated, registeredAt FROM tv_measurement_data", nativeQuery = true)
	List<StudentMeasurementDataModel> getMeasurementData();
	
	@Query(value = "SELECT dataUid, userUid, name, nationalId, birthDate, gender, studentNo, schoolYear, grade, "
			+ "CAST(COLUMN_JSON(registerProperties) AS CHAR) AS registerProperties, "
			+ "CAST(COLUMN_JSON(properties) AS CHAR) AS properties, "
			+ "CAST(COLUMN_JSON(registeredBy) AS CHAR) AS registeredBy, "
			+ "lastUpdated, registeredAt FROM tv_measurement_data WHERE COLUMN_EXISTS(properties, ?1)", nativeQuery = true)
	List<StudentMeasurementDataModel> getMeasurementData(String item);

}
