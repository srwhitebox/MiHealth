package com.mihealth.db.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mihealth.db.model.LocaleModel;

public interface LocaleRepository extends JpaRepository<LocaleModel, String>{
	LocaleModel findOneByLocaleTag(String localeTag);

	List<LocaleModel> findByEnabled(boolean enabled);
}
