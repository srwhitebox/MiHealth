package com.mihealth.db.service;

import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mihealth.db.model.SettingsModel;
import com.mihealth.db.repositories.SettingsRepository;
import com.ximpl.lib.db.EntityManagerHelper;
import com.ximpl.lib.util.XcJsonUtils;

@Service
public class SettingsService {
	@Autowired
	private EntityManagerHelper entityManagerHelper;

	@Autowired 
	PasswordEncoder passwordEncoder;

	@Autowired
	SettingsRepository settingsRepository;
	
	public void save(SettingsModel settings){
		final String queryFormat = "REPLACE INTO tb_settings(item, settings) values (:item, " + XcJsonUtils.toJsonCreate(settings.getSettings()) + ")";
		Query query = entityManagerHelper.query(queryFormat);
		
		query.setParameter("item", settings.getItem());
		
		entityManagerHelper.execute(query);
	}
	
	public boolean exists(String item){
		return settingsRepository.exists(item);
	}
	
	public void save(String item, String key, String value){
		if (exists(item)){
			settingsRepository.addSetting(item, key, value);
		}else{
			SettingsModel settings = new SettingsModel(item);
			settings.add(key, value);
			save(settings);
		}
	}
	
	public SettingsModel getSetting(String item){
		return settingsRepository.getOne(item);
	}
	
	public List<SettingsModel> getAll(){
		return settingsRepository.findAll();
	}
	
	public String getValue(String item, String key){
		return settingsRepository.getValue(item,  key);
	}
	
	public void delete(String item){
		settingsRepository.deleteItem(item);
	}
	
	public void delete(String item, String key){
		settingsRepository.deleteKey(item, key);
	}

}
