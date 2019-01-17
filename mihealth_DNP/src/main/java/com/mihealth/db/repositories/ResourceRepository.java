package com.mihealth.db.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.mihealth.db.model.ResourceModel;

public interface ResourceRepository extends JpaRepository<ResourceModel, String>{
	@Query(value = "SELECT COLUMN_GET(properties, ?1 as CHAR) FROM tb_resource WHERE category = ?2 AND code = ?3", nativeQuery = true)
	String getResource(String language, String category, String code);

	@Query(value = "SELECT uid, category, code, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, comment, lastUpdated, registeredAt FROM tb_resource WHERE category = ?1 AND code = ?2", nativeQuery = true)
	ResourceModel findByCategoryAndCode(String category, String code);

	@Query(value = "SELECT uid, category, code, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, comment, lastUpdated, registeredAt FROM tb_resource ORDER BY category, code", nativeQuery = true)
	List<ResourceModel> findAll();

	@Query(value = "SELECT uid, category, code, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, comment, lastUpdated, registeredAt FROM tb_resource WHERE category = ?1", nativeQuery = true)
	List<ResourceModel> findByCategory(String category);
	
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM tb_resource WHERE category = ?1 AND code = ?2", nativeQuery = true)
	void deleteByCategoryAndCode(String category, String code);
}
