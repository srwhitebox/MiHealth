package com.mihealth.db.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mihealth.db.model.LocaleModel;
import com.mihealth.db.model.AltLocaleModel;
import com.mihealth.db.repositories.LocaleRepository;
import com.mihealth.db.repositories.AltLocaleRepository;

@Service
public class LocaleService {
	@Autowired
	private LocaleRepository localeRepository;

	@Autowired
	private AltLocaleRepository altLocaleRepository;

	public void save(LocaleModel localeModel) {
		localeRepository.save(localeModel);
	}

	public LocaleModel get(String localeTag) {
		AltLocaleModel altLocale = altLocaleRepository.findOne(localeTag);
		final String altLocaleTag = altLocale != null ? altLocale.getAltLocaleTag() : "en-US"; 
		return localeRepository.findOneByLocaleTag(altLocaleTag);
	}

	public List<LocaleModel> getList(Boolean enabled) {
		if (enabled != null)
			return localeRepository.findByEnabled(enabled);
		
		return localeRepository.findAll();
	}
	
	public void delete(LocaleModel locale){
		localeRepository.delete(locale);
	}
	
	public void save(AltLocaleModel localeModel) {
		altLocaleRepository.save(localeModel);
	}
	
	public List<AltLocaleModel> getAltLocales(){
		return altLocaleRepository.findAllByOrderByLocaleTag();
	}
	
	public void delete(AltLocaleModel locale){
		altLocaleRepository.delete(locale);
	}

}
