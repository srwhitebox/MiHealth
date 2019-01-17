package com.mihealth.db.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mihealth.db.model.StudentModel;

public interface StudentRepository extends JpaRepository<StudentModel, String>{
	@Query(value = "SELECT registerUid, campusUid, userUid, name, nationalId, birthDate, gender, studentNo, schoolYear, grade, "
			+ "CAST(COLUMN_JSON(roles) AS CHAR) AS roles, "
			+ "CAST(COLUMN_JSON(registerProperties) AS CHAR) AS registerProperties, "
			+ "CAST(COLUMN_JSON(userProperties) AS CHAR) AS userProperties, "
			+ "CAST(COLUMN_JSON(settings) AS CHAR) AS settings, "
			+ "userEnabled, registerEnabled, userLastUpdated, registerLastUpdated, userRegisteredAt, registerRegisteredAt FROM tv_student "
			+ "WHERE userUid = UNHEX(?1)", nativeQuery = true)
	StudentModel findByUserUid(String decrypt);

	@Query(value = "?1", nativeQuery = true)
	Integer getStudentsNumber(String query);

	@Query(value = "SELECT DISTINCT(grade) FROM tb_school_register WHERE campusUid = UNHEX(?1) ORDER BY grade", nativeQuery = true)
	List<Integer> getGrades(String campusUid);

	@Query(value = "SELECT DISTINCT(COLUMN_GET(properties, 'classId' AS CHAR)) FROM tb_school_register WHERE campusUid = UNHEX(?1) ORDER BY COLUMN_GET(properties, 'classId' AS CHAR)", nativeQuery = true)
	List<String> getClassIds(String campusUid);

	@Query(value = "SELECT DISTINCT(COLUMN_GET(properties, 'classId' AS CHAR)) FROM tb_school_register WHERE campusUid = UNHEX(?1) AND grade = ?2 AND COLUMN_GET(properties, 'classId' AS CHAR) IS NOT NULL ORDER BY COLUMN_GET(properties, 'classId' AS CHAR)", nativeQuery = true)
	List<String> getClassIds(String campusUid, Integer grade);

	@Query(value = "SELECT registerUid, campusUid, userUid, name, nationalId, birthDate, gender, studentNo, schoolYear, grade, "
			+ "CAST(COLUMN_JSON(roles) AS CHAR) AS roles, "
			+ "CAST(COLUMN_JSON(registerProperties) AS CHAR) AS registerProperties, "
			+ "CAST(COLUMN_JSON(userProperties) AS CHAR) AS userProperties, "
			+ "CAST(COLUMN_JSON(settings) AS CHAR) AS settings, "
			+ "userEnabled, registerEnabled, userLastUpdated, registerLastUpdated, userRegisteredAt, registerRegisteredAt FROM tv_student WHERE campusUid = UNHEX(?1)", nativeQuery = true)
	List<StudentModel> findAll(String campusUid);

	@Query(value = "SELECT registerUid, campusUid, userUid, name, nationalId, birthDate, gender, studentNo, schoolYear, grade, "
			+ "CAST(COLUMN_JSON(roles) AS CHAR) AS roles, "
			+ "CAST(COLUMN_JSON(registerProperties) AS CHAR) AS registerProperties, "
			+ "CAST(COLUMN_JSON(userProperties) AS CHAR) AS userProperties, "
			+ "CAST(COLUMN_JSON(settings) AS CHAR) AS settings, "
			+ "userEnabled, registerEnabled, userLastUpdated, registerLastUpdated, userRegisteredAt, registerRegisteredAt FROM tv_student "
			+ "WHERE campusUid = UNHEX(?1) AND grade = ?2", nativeQuery = true)
	List<StudentModel> findByGrade(String campusUid, int grade);
	
	@Query(value = "SELECT registerUid, campusUid, userUid, name, nationalId, birthDate, gender, studentNo, schoolYear, grade, "
			+ "CAST(COLUMN_JSON(roles) AS CHAR) AS roles, "
			+ "CAST(COLUMN_JSON(registerProperties) AS CHAR) AS registerProperties, "
			+ "CAST(COLUMN_JSON(userProperties) AS CHAR) AS userProperties, "
			+ "CAST(COLUMN_JSON(settings) AS CHAR) AS settings, "
			+ "userEnabled, registerEnabled, userLastUpdated, registerLastUpdated, userRegisteredAt, registerRegisteredAt FROM tv_student "
			+ "WHERE campusUid = UNHEX(?1) AND COLUMN_GET(registerProperties, 'classId' AS CHAR) = ?2", nativeQuery = true)
	List<StudentModel> findByClassId(String campusUid, String classId);

	@Query(value = "SELECT registerUid, campusUid, userUid, name, nationalId, birthDate, gender, studentNo, schoolYear, grade, "
			+ "CAST(COLUMN_JSON(roles) AS CHAR) AS roles, "
			+ "CAST(COLUMN_JSON(registerProperties) AS CHAR) AS registerProperties, "
			+ "CAST(COLUMN_JSON(userProperties) AS CHAR) AS userProperties, "
			+ "CAST(COLUMN_JSON(settings) AS CHAR) AS settings, "
			+ "userEnabled, registerEnabled, userLastUpdated, registerLastUpdated, userRegisteredAt, registerRegisteredAt FROM tv_student "
			+ "WHERE campusUid = UNHEX(?1) AND userEnabled = true AND COLUMN_GET(registerProperties, 'classId' AS CHAR) = ?2", nativeQuery = true)
	List<StudentModel> findByClassIdEnabledOnly(String campusUid, String classId);

	@Query(value = "SELECT registerUid, campusUid, userUid, name, nationalId, birthDate, gender, studentNo, schoolYear, grade, "
			+ "CAST(COLUMN_JSON(roles) AS CHAR) AS roles, "
			+ "CAST(COLUMN_JSON(registerProperties) AS CHAR) AS registerProperties, "
			+ "CAST(COLUMN_JSON(userProperties) AS CHAR) AS userProperties, "
			+ "CAST(COLUMN_JSON(settings) AS CHAR) AS settings, "
			+ "userEnabled, registerEnabled, userLastUpdated, registerLastUpdated, userRegisteredAt, registerRegisteredAt FROM tv_student "
			+ "WHERE campusUid = UNHEX(?1) AND COLUMN_GET(registerProperties, 'classId' AS CHAR) = ?2 AND COLUMN_GET(registerProperties, 'seat' AS CHAR) = ?3", nativeQuery = true)
	List<StudentModel> findByClassIdAndSeat(String campusUid, String classId, String seat);

	@Query(value = "SELECT registerUid, campusUid, userUid, name, nationalId, birthDate, gender, studentNo, schoolYear, grade, "
			+ "CAST(COLUMN_JSON(roles) AS CHAR) AS roles, "
			+ "CAST(COLUMN_JSON(registerProperties) AS CHAR) AS registerProperties, "
			+ "CAST(COLUMN_JSON(userProperties) AS CHAR) AS userProperties, "
			+ "CAST(COLUMN_JSON(settings) AS CHAR) AS settings, "
			+ "userEnabled, registerEnabled, userLastUpdated, registerLastUpdated, userRegisteredAt, registerRegisteredAt FROM tv_student "
			+ "WHERE campusUid = UNHEX(?1) AND nationalId = ?2", nativeQuery = true)
	List<StudentModel> findByNationalId(String campusUid, String nationalId);	

	@Query(value = "SELECT registerUid, campusUid, userUid, name, nationalId, birthDate, gender, studentNo, schoolYear, grade, "
			+ "CAST(COLUMN_JSON(roles) AS CHAR) AS roles, "
			+ "CAST(COLUMN_JSON(registerProperties) AS CHAR) AS registerProperties, "
			+ "CAST(COLUMN_JSON(userProperties) AS CHAR) AS userProperties, "
			+ "CAST(COLUMN_JSON(settings) AS CHAR) AS settings, "
			+ "userEnabled, registerEnabled, userLastUpdated, registerLastUpdated, userRegisteredAt, registerRegisteredAt FROM tv_student "
			+ "WHERE campusUid = UNHEX(?1) AND COLUMN_GET(registerProperties, 'easyCardId' AS CHAR) = ?2", nativeQuery = true)
	List<StudentModel> findByEasyCardId(String campusUid, String cardId);
	
	
	@Query(value = "SELECT registerUid, campusUid, userUid, name, nationalId, birthDate, gender, studentNo, schoolYear, grade, "
			+ "CAST(COLUMN_JSON(roles) AS CHAR) AS roles, "
			+ "CAST(COLUMN_JSON(registerProperties) AS CHAR) AS registerProperties, "
			+ "CAST(COLUMN_JSON(userProperties) AS CHAR) AS userProperties, "
			+ "CAST(COLUMN_JSON(settings) AS CHAR) AS settings, "
			+ "userEnabled, registerEnabled, userLastUpdated, registerLastUpdated, userRegisteredAt, registerRegisteredAt "
			+ "FROM tv_student "
			+ "WHERE campusUid = UNHEX(?1) AND COLUMN_GET(registerProperties, 'classId' AS CHAR) like %?2% AND name like %?3%  LIMIT ?4", nativeQuery = true)
	List<StudentModel> findWithFilter(String campusUid, String classId, String filter, int rowCount);

}
