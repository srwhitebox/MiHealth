package com.mihealth.db.model;

import java.util.UUID;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.joda.time.DateTime;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mihealth.db.utils.EncodeUtils;
import com.ximpl.lib.db.jpa.converter.JpaBytesUuidConverter;
import com.ximpl.lib.db.jpa.converter.JpaJsonConverter;
import com.ximpl.lib.util.XcJsonUtils;

@Entity
@Table(name="tb_measurement_data")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MeasurementDataModel extends DynamicDataModel{
	@Convert(converter = JpaBytesUuidConverter.class)
	private String userUid;

	@Convert(converter = JpaBytesUuidConverter.class)
	private String campusUid;
	
	@Convert(converter = JpaJsonConverter.class)
	private JsonObject registeredBy;

	public MeasurementDataModel(){
	}
	
	public MeasurementDataModel(String userUid, DateTime registeredAt, JsonObject jProperties){
		this.setUid(UUID.randomUUID());
		this.setUserUid(userUid);
		this.setRegisteredAt(registeredAt);
		this.setProperties(jProperties);
	}

	public String getUserUid(){
		return this.userUid;
	}
	
	public String getDecryptedUserUid(){
		return EncodeUtils.decrypt(this.getUserUid());
	}
	
	public void setUserUid(String userUid){
		this.userUid = userUid;
	}	

	public String getCampusUid(){
		return this.campusUid;
	}
	
	public String getDecryptedCampusUid(){
		return EncodeUtils.decrypt(this.getCampusUid());
	}
	
	public void setCampusUid(String campusUid){
		this.campusUid = campusUid;
	}
	
	public JsonObject getRegisteredBy() {
		if (registeredBy == null)
			registeredBy = new JsonObject();
		
		return registeredBy;
	}

	public void setRegisteredBy(JsonObject registeredInfo) {
		this.registeredBy = registeredInfo;
	}

	public void setRegisteredBy(String properties){
		JsonElement jElement = XcJsonUtils.toJsonElement(properties);
		if (jElement != null && jElement.isJsonObject())
			this.setRegisteredBy(jElement.getAsJsonObject());
	}
	
	public void setRegisteredBy(String mikioUid, long batchId) {
		this.getRegisteredBy().addProperty("mikioUid", mikioUid);
		this.getRegisteredBy().addProperty("batchUid", batchId);
	}

}
