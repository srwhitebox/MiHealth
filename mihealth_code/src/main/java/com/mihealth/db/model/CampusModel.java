package com.mihealth.db.model;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Set;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.joda.time.DateTime;

import com.google.common.io.BaseEncoding;
import com.google.gson.JsonObject;
import com.ximpl.lib.db.jpa.converter.JpaJsonConverter;
import com.ximpl.lib.type.XcMap;
import com.ximpl.lib.util.XcBooleanUtils;
import com.ximpl.lib.util.XcJsonUtils;
import com.ximpl.lib.util.XcStringUtils;

import com.mihealth.db.type.PARAMETER;

@Entity
@Table(name="tb_campus")
public class CampusModel extends DynamicDataModel{
	private String campusId;
	private String name;
	@Convert(converter = JpaJsonConverter.class)
	private JsonObject settings;
	private String authKey;
	private Boolean enabled;

	public void init(){
		this.initUid();
		this.resetAuthKey();
		this.setEnabled(true);
		this.initRegisteredAt();
		this.setLastUpdated(this.getRegisteredAt());
	}
	
	public String getCampusUid(){
		return this.getUid();
	}
	
	public void setCampusUid(String campusUid){
		this.setUid(campusUid);
	}
	
	public String getCampusId(){
		return this.campusId;
	}
	
	public void setCampusId(String campusId){
		this.campusId = campusId;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public boolean getEnabled(){
		return this.enabled;
	}
	
	public void setEnabled(boolean enabled){
		this.enabled = enabled;
	}
	
	public JsonObject getSettings(){
		return this.settings;
	}
	
	public void setSettings(JsonObject settings){
		this.settings = settings;
	}

	
	public String getAuthKey(){
		return this.authKey;
	}
	
	public void setAuthKey(String authKey){
		this.authKey = authKey;
	}
	
	public void resetAuthKey(){
		this.setAuthKey(generateAuthKey());
	}
	
	public static String generateAuthKey(){
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(new SecureRandom());
			keyGen.init(256); // for example
			SecretKey secretKey = keyGen.generateKey();
			
			return BaseEncoding.base64().omitPadding().encode(secretKey.getEncoded());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean canRegister(){
		return XcStringUtils.isValid(this.getCampusId()) && XcStringUtils.isValid(this.name);
	}
	
}
