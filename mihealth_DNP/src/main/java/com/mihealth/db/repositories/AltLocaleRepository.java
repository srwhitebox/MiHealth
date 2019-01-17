package com.mihealth.db.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mihealth.db.model.AltLocaleModel;

public interface AltLocaleRepository extends JpaRepository<AltLocaleModel, String>{
	AltLocaleModel findOneByLocaleTag(String localeTag);
	List<AltLocaleModel> findAllByOrderByLocaleTag();
}
