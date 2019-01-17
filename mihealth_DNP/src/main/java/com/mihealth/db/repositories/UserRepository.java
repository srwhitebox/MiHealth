package com.mihealth.db.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.mihealth.db.model.UserModel;

@Transactional(readOnly = true)
public interface UserRepository extends JpaRepository<UserModel, String>{
	@Query(value = "SELECT uid, name, nationalId, gender, birthDate, CAST(COLUMN_JSON(campuses) AS CHAR) AS campuses, CAST(COLUMN_JSON(roles) AS CHAR) AS roles, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, CAST(COLUMN_JSON(settings) AS CHAR) AS settings, enabled, lastUpdated, registeredAt FROM tb_user", nativeQuery = true)
	List<UserModel> getAll();
	
	@Query(value = "SELECT uid, name, nationalId, gender, birthDate, CAST(COLUMN_JSON(campuses) AS CHAR) AS campuses, CAST(COLUMN_JSON(roles) AS CHAR) AS roles, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, CAST(COLUMN_JSON(settings) AS CHAR) AS settings, enabled, lastUpdated, registeredAt FROM tb_user WHERE enabled = ?1", nativeQuery = true)
	List<UserModel> getByEnabled(boolean enabled);

	@Query(value = "SELECT uid, name, nationalId, gender, birthDate, CAST(COLUMN_JSON(campuses) AS CHAR) AS campuses, CAST(COLUMN_JSON(roles) AS CHAR) AS roles, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, CAST(COLUMN_JSON(settings) AS CHAR) AS settings, enabled, lastUpdated, registeredAt FROM tb_user WHERE COLUMN_EXISTS(roles, ?1)", nativeQuery = true)
	List<UserModel>  getByRole(String role);

	@Query(value = "SELECT uid, name, nationalId, gender, birthDate, CAST(COLUMN_JSON(campuses) AS CHAR) AS campuses, CAST(COLUMN_JSON(roles) AS CHAR) AS roles, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, CAST(COLUMN_JSON(settings) AS CHAR) AS settings, enabled, lastUpdated, registeredAt FROM tb_user WHERE COLUMN_EXISTS(roles, ?1) AND enabled = ?2", nativeQuery = true)
	List<UserModel>  getByRoleAndEnabled(String role, Boolean enabled);

	@Query(value = "SELECT uid, name, nationalId, gender, birthDate, CAST(COLUMN_JSON(campuses) AS CHAR) AS campuses, CAST(COLUMN_JSON(roles) AS CHAR) AS roles, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, CAST(COLUMN_JSON(settings) AS CHAR) AS settings, enabled, lastUpdated, registeredAt FROM tb_user WHERE uid = UNHEX(?1)", nativeQuery = true)
	UserModel getOne(String userUid);
	
	@Modifying
	@Query(value = "UPDATE tb_user SET enabled = ?2 WHERE uid = UNHEX(?1)", nativeQuery = true)
	void enableByUserUid(String userUid, boolean enabled);

	@Modifying
	@Query(value = "UPDATE tb_user SET settings=COLUMN_ADD(settings, ?2, ?3) WHERE uid = UNHEX(?1)", nativeQuery = true)
	void updateSetting(String userUid, String key, String value);

	@Modifying
	@Query(value = "UPDATE tb_user SET settings=COLUMN_DELETE(settings, ?2) WHERE uid = UNHEX(?1)", nativeQuery = true)
	void removeSetting(String userUid, String key);

	
	@Modifying
	@Query(value = "DELETE FROM tb_user WHERE uid = UNHEX(?1)", nativeQuery = true)
	void deleteByUserUid(String userUid);

	@Query(value = "SELECT uid, name, nationalId, gender, birthDate, CAST(COLUMN_JSON(campuses) AS CHAR) AS campuses, CAST(COLUMN_JSON(roles) AS CHAR) AS roles, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, CAST(COLUMN_JSON(settings) AS CHAR) AS settings, enabled, lastUpdated, registeredAt FROM tb_user WHERE COLUMN_EXISTS(campuses, ?1) AND COLUMN_GET(properties, 'dept' AS CHAR) like %?2% LIMIT 1", nativeQuery = true)
	UserModel findNurseByDept(String campusUid, String dept);
}
