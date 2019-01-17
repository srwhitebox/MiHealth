package com.mihealth.db.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mihealth.db.model.StudentCareDataModel;

public interface StudentCareDataRepository extends JpaRepository<StudentCareDataModel, String>{
	@Query(value = "SELECT dataUid, userUid, name, nationalId, birthDate, gender, studentNo, schoolYear, grade, "
			+ "CAST(COLUMN_JSON(registerProperties) AS CHAR) AS registerProperties, "
			+ "deptId, nurseUid, nurseName, regNo, "
			+ "CAST(COLUMN_JSON(properties) AS CHAR) AS properties, "
			+ "comment, "
			+ "CAST(COLUMN_JSON(registeredBy) AS CHAR) AS registeredBy, "
			+ "lastUpdated, registeredAt FROM tv_measurement_data", nativeQuery = true)
	List<StudentCareDataModel> getMeasurementData();
	
	@Query(value = "SELECT dataUid, userUid, name, nationalId, birthDate, gender, studentNo, schoolYear, grade, "
			+ "CAST(COLUMN_JSON(registerProperties) AS CHAR) AS registerProperties, "
			+ "deptId, nurseUid, nurseName, regNo, "
			+ "CAST(COLUMN_JSON(properties) AS CHAR) AS properties, "
			+ "comment, "
			+ "CAST(COLUMN_JSON(registeredBy) AS CHAR) AS registeredBy, "
			+ "lastUpdated, registeredAt FROM tv_measurement_data WHERE COLUMN_EXISTS(properties, ?1)", nativeQuery = true)
	List<StudentCareDataModel> getMeasurementData(String item);

}
