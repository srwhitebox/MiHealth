package com.mihealth.db.model;

import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.gson.JsonObject;
import com.ximpl.lib.db.jpa.converter.JpaJsonConverter;

@Entity
@Table(name="tb_settings")
public class SettingsModel{
	@Id
	private String item;
	@Convert(converter = JpaJsonConverter.class)
	private JsonObject settings;

	/**
	 * Constructor
	 */
	public SettingsModel(){
		
	}
	
	public SettingsModel(String item, JsonObject jSettings){
		this.setItem(item);
		this.setSettings(jSettings);
	}
	
	/**
	 * Constructor
	 * @param item
	 */
	public SettingsModel(String item){
		this.setItem(item);
	}
	
	/**
	 * Get item
	 * @return
	 */
	public String getItem() {
		return item;
	}
	
	/**
	 * Set item
	 * @param item
	 */
	public void setItem(String item) {
		this.item = item;
	}
	
	/**
	 * Get settings
	 * @return
	 */
	public JsonObject getSettings() {
		if (settings == null)
			this.settings = new JsonObject();
		return settings;
	}
	
	/**
	 * Set settings
	 * @param settings
	 */
	public void setSettings(JsonObject settings) {
		this.settings = settings;
	}

	/**
	 * Add setting for a given key & value
	 * @param key
	 * @param value
	 */
	public void add(String key, String value) {
		this.getSettings().addProperty(key, value);
	}
	
	/**
	 * Add settings with give map
	 * @param params
	 */
	public void addSetting(Map<String, String> params){
		for(Entry<String, String> entry : params.entrySet()){
			add(entry.getKey(), entry.getValue());
		}
	}
}
