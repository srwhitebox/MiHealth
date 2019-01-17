package com.mihealth.db.model;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.gson.JsonObject;
import com.ximpl.lib.db.jpa.converter.JpaBytesUuidConverter;
import com.ximpl.lib.db.jpa.converter.JpaJsonConverter;
import com.ximpl.lib.util.XcUuidUtils;

@Entity
@Table(name="tv_treatment")
public class TreatmentDetailedModel{
	@Convert(converter = JpaBytesUuidConverter.class)
	private String uid;
	@Convert(converter = JpaBytesUuidConverter.class)
	private String careUid;
	@Convert(converter = JpaBytesUuidConverter.class)
	private String nurseUid;
	private String nurseName;
	@Convert(converter = JpaJsonConverter.class)
	private JsonObject treatment;
	private String status;
	private Date lastUpdated;
	@Id
	private Date registeredAt;

	public String getUid() {
		return uid;
	}
	
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public String getCareUid() {
		return careUid;
	}
	
	public void setCareUid(String careUid) {
		this.careUid = careUid;
	}
	
	public String getNurseUid() {
		return nurseUid;
	}
	
	public void setNurseUid(String nurseUid) {
		this.nurseUid = nurseUid;
	}
	
	public String getNurseName() {
		return nurseName;
	}

	public void setNurseName(String nurseName) {
		this.nurseName = nurseName;
	}

	public JsonObject getTreatment() {
		return treatment;
	}
	
	public void setTreatment(JsonObject treatment) {
		this.treatment = treatment;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public Date getLastUpdated() {
		return lastUpdated;
	}
	
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public Date getRegisteredAt() {
		return registeredAt;
	}
	
	public void setRegisteredAt(Date registeredAt) {
		this.registeredAt = registeredAt;
	}

}
