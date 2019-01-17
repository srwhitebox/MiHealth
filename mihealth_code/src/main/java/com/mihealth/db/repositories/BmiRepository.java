package com.mihealth.db.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mihealth.db.model.BmiModel;

public interface BmiRepository extends JpaRepository<BmiModel, String>{
	@Query(value = "SELECT age, CAST(COLUMN_JSON(male) AS CHAR) AS male, CAST(COLUMN_JSON(female) AS CHAR) AS female FROM tb_bmi ORDER BY CAST(age AS DOUBLE)", nativeQuery = true)
	List<BmiModel> findAll();

	@Query(value = "SELECT age, CAST(COLUMN_JSON(male) AS CHAR) AS male, CAST(COLUMN_JSON(female) AS CHAR) AS female FROM tb_bmi WHERE age = ?1", nativeQuery = true)
	BmiModel findOne(String age);
}
