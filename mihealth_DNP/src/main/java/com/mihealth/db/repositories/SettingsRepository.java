package com.mihealth.db.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.mihealth.db.model.SettingsModel;

public interface SettingsRepository extends JpaRepository<SettingsModel, String>{
	@Query(value = "SELECT item, CAST(COLUMN_JSON(settings) AS CHAR) AS settings FROM tb_settings", nativeQuery = true)
	List<SettingsModel> findAll();

	@Query(value = "SELECT item, CAST(COLUMN_JSON(settings) AS CHAR) AS settings FROM tb_settings WHERE item = ?1", nativeQuery = true)
	SettingsModel getOne(String item);

	@Query(value = "SELECT COLUMN_GET(settings, ?2 as CHAR) as value  FROM tb_settings WHERE item = ?1", nativeQuery = true)
	String getValue(String item, String key);

	@Query(value = "SELECT COLUMN_GET(settings, ?2 as INT) as value  FROM tb_settings WHERE item = ?1", nativeQuery = true)
	int getValueAsInteger(String item, String key);

	@Modifying
	@Transactional
	@Query(value = "UPDATE tb_settings SET settings = COLUMN_ADD(settings, ?2, ?3) WHERE item = ?1", nativeQuery = true)
	void addSetting(String item, String key, String value);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM tb_settings WHERE item = ?1", nativeQuery = true)
	void deleteItem(String item);

	@Modifying
	@Transactional
	@Query(value = "UPDATE tb_settings SET settings = COLUMN_DELETE(settings, ?2) WHERE item = ?1", nativeQuery = true)
	void deleteKey(String item, String key);

}
