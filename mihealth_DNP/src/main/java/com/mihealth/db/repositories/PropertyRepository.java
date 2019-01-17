package com.mihealth.db.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.mihealth.db.model.PropertyModel;
import com.mihealth.db.model.ResourceModel;

public interface PropertyRepository extends JpaRepository<PropertyModel, String>{
	@Query(value = "SELECT uid, category, code, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, comment, displayOrder, enabled, lastUpdated, registeredAt FROM tb_property", nativeQuery = true)
	List<PropertyModel> findAll();

	@Query(value = "SELECT uid, category, code, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, comment, displayOrder, enabled, lastUpdated, registeredAt FROM tb_property where enabled = ?1", nativeQuery = true)
	List<PropertyModel> findByEnabled(boolean enabled);

	@Query(value = "SELECT uid, category, code, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, comment, displayOrder, enabled, lastUpdated, registeredAt FROM tb_property WHERE category = ?1", nativeQuery = true)
	List<PropertyModel> findByCategory(String category);

	@Query(value = "SELECT uid, category, code, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, comment, displayOrder, enabled, lastUpdated, registeredAt FROM tb_property WHERE category = ?1 AND code = ?2", nativeQuery = true)
	PropertyModel findByCategoryAndCode(String category, String code);
	
	@Query(value = "SELECT uid, category, code, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, comment, displayOrder, enabled, lastUpdated, registeredAt FROM tb_property WHERE category = ?1 AND enabled = ?2", nativeQuery = true)
	List<PropertyModel> findByCategoryAndEnabled(String category, boolean enabled);
	
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM tb_property WHERE category = ?1 AND code = ?2", nativeQuery = true)
	void deleteByCategoryAndCode(String category, String code);
}
