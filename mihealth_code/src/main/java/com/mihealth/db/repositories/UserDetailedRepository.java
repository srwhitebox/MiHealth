package com.mihealth.db.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.mihealth.db.model.UserDetailedModel;
import com.mihealth.db.model.UserModel;

@Transactional(readOnly = true)
public interface UserDetailedRepository extends JpaRepository<UserDetailedModel, String>{
	@Query(value = "SELECT uid, name, nationalId, gender, birthDate, CAST(COLUMN_JSON(roles) AS CHAR) AS roles, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, CAST(COLUMN_JSON(settings) AS CHAR) AS settings, hasAccount, enabled, lastUpdated, registeredAt FROM tv_user", nativeQuery = true)
	List<UserDetailedModel> getAll();
	
	@Query(value = "SELECT uid, name, nationalId, gender, birthDate, CAST(COLUMN_JSON(roles) AS CHAR) AS roles, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, CAST(COLUMN_JSON(settings) AS CHAR) AS settings, hasAccount, enabled, lastUpdated, registeredAt FROM tv_user WHERE enabled = ?1", nativeQuery = true)
	List<UserDetailedModel> getByEnabled(boolean enabled);

	@Query(value = "SELECT uid, name, nationalId, gender, birthDate, CAST(COLUMN_JSON(campuses) AS CHAR) AS campuses, CAST(COLUMN_JSON(roles) AS CHAR) AS roles, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, CAST(COLUMN_JSON(settings) AS CHAR) AS settings, hasAccount, enabled, lastUpdated, registeredAt FROM tv_user WHERE COLUMN_EXISTS(campuses, ?1) AND COLUMN_EXISTS(roles, ?2)", nativeQuery = true)
	List<UserDetailedModel>  getByRole(String campusUid, String role);

	@Query(value = "SELECT uid, name, nationalId, gender, birthDate, CAST(COLUMN_JSON(campuses) AS CHAR) AS campuses, CAST(COLUMN_JSON(roles) AS CHAR) AS roles, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, CAST(COLUMN_JSON(settings) AS CHAR) AS settings, hasAccount, enabled, lastUpdated, registeredAt FROM tv_user WHERE  COLUMN_EXISTS(campuses, ?1) AND COLUMN_EXISTS(roles, ?2) AND enabled = ?3", nativeQuery = true)
	List<UserDetailedModel>  getByRoleAndEnabled(String campusUid, String role, Boolean enabled);

	@Query(value = "SELECT uid, name, nationalId, gender, birthDate, CAST(COLUMN_JSON(roles) AS CHAR) AS roles, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, CAST(COLUMN_JSON(settings) AS CHAR) AS settings, hasAccount, enabled, lastUpdated, registeredAt FROM tv_user WHERE uid = UNHEX(?1)", nativeQuery = true)
	UserDetailedModel getOne(String userUid);
	
	@Query(value = "SELECT uid, name, nationalId, gender, birthDate, CAST(COLUMN_JSON(roles) AS CHAR) AS roles, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, CAST(COLUMN_JSON(settings) AS CHAR) AS settings, hasAccount, enabled, lastUpdated, registeredAt FROM tv_user WHERE COLUMN_GET(properties, 'dept' AS CHAR) like %?1% LIMIT 1", nativeQuery = true)
	UserDetailedModel findNurseByDept(String dept);
}
