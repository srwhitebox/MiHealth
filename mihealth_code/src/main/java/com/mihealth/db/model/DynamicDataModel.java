package com.mihealth.db.model;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Convert;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mihealth.db.utils.EncodeUtils;
import com.ximpl.lib.db.jpa.converter.JpaBytesUuidConverter;
import com.ximpl.lib.db.jpa.converter.JpaJsonConverter;
import com.ximpl.lib.util.XcJsonUtils;
import com.ximpl.lib.util.XcUuidUtils;

@MappedSuperclass
public class DynamicDataModel {
	@Convert(converter = JpaBytesUuidConverter.class)
	protected String uid;
	
	@Convert(converter = JpaJsonConverter.class)
	protected JsonObject properties;
	private Date lastUpdated;
	
	@Id
	protected Date registeredAt;
	
	public String getUid(){
		return this.uid;
	}
	
	public void setUid(String uid){
		this.uid = uid;
	}

	public String getDecryptedUid(){
		return EncodeUtils.decrypt(this.getUid());
	}
	
	public void initUid(){
		setUid(UUID.randomUUID());
	}

	public void setUid(UUID uuid){
		this.uid = XcUuidUtils.encrypt(uuid);
	}
	
	public JsonObject getProperties(){
		if (properties == null)
			properties = new JsonObject();
		return this.properties;
	}
	
	public void setProperties(JsonObject jProperties){
		this.properties = jProperties;
	}

	public void setProperties(JsonElement jProperties){
		if (jProperties != null && jProperties.isJsonObject())
			this.setProperties(jProperties.getAsJsonObject());
	}

	public void setProperties(String properties){
		JsonElement jElement = XcJsonUtils.toJsonElement(properties);
		if (jElement.isJsonObject())
			this.setProperties(jElement.getAsJsonObject());
	}
	
	public Date getLastUpdated(){
		return this.lastUpdated;
	}
	
	public void setLastUpdated(Date lastUpdated){
		this.lastUpdated = lastUpdated;
	}

	public void setLastUpdated(DateTime lastUpdated){
		this.setLastUpdated(lastUpdated.toDate());
	}

	public Date getRegisteredAt(){
		return this.registeredAt;
	}
	
	public void setRegisteredAt(Date registeredAt){
		this.registeredAt = registeredAt;
	}

	public void setRegisteredAt(DateTime registeredAt){
		this.setRegisteredAt(registeredAt.toDate());
	}
	
	public void initRegisteredAt(){
		final DateTime registered = DateTime.now(DateTimeZone.UTC);
		this.setRegisteredAt(registered);
		this.setLastUpdated(registered);
	}

	public void setProperty(String property, String value){
		this.getProperties().addProperty(property, value);
	}

	public void setProperty(String property, Number value){
		this.getProperties().addProperty(property, value);
	}
	
	public JsonElement getProperty(String property){
		return properties == null ? null : properties.get(property);
	}
	
	public String getPropertyAsString(String property){
		final JsonElement jElement = getProperty(property);
		
		return jElement == null ? null : jElement.getAsString();
	}

	public Integer getPropertyAsInt(String property){
		final JsonElement jElement = getProperty(property);
		
		return jElement == null ? null : jElement.getAsInt();
	}

	public Long getPropertyAsLong(String property){
		final JsonElement jElement = getProperty(property);
		
		return jElement == null ? null : jElement.getAsLong();
	}


	public Float getPropertyAsFloat(String property){
		final Object value = getProperty(property);
		
		if (value instanceof Float)
			return (Float)value;
		else{
			return value == null ? null : Float.parseFloat((String)value);
		}
	}
	
	public Double getPropertyAsDouble(String property){
		final Object value = getProperty(property);
		
		if (value instanceof Float)
			return (Double)value;
		else{
			return value == null ? null : Double.parseDouble((String)value);
		}
	}

	public DateTime getDateTime(String property){
		final Object value = getProperty(property);
		
		if (value instanceof DateTime){
			return (DateTime)value;
		}else if (value instanceof Long){
			return new DateTime(value);
		}

		return value == null ? null : DateTime.parse((String)value);
	}	
}
