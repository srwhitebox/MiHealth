package com.mihealth.db.model;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mihealth.db.utils.EncodeUtils;
import com.ximpl.lib.db.jpa.converter.JpaBytesUuidConverter;
import com.ximpl.lib.db.jpa.converter.JpaJsonConverter;

@Entity
@Table(name="tb_mikio")
public class MikioModel extends DynamicDataModel {
	@Convert(converter = JpaBytesUuidConverter.class)
	private String campusUid;

	private String mikioId;
	
	@Convert(converter = JpaBytesUuidConverter.class)
	private String nurseUid;
	
	@Convert(converter = JpaJsonConverter.class)
	protected JsonObject students;
	
	private boolean enabled;
	
	public String getCampusUid(){
		return this.campusUid;
	}
	
	public String getDecryptedCampusUid(){
		return EncodeUtils.decrypt(this.getCampusUid());
	}
	
	public void setCampusUid(String campusUid){
		this.campusUid = campusUid;
	}

	public String getMikioId() {
		return mikioId;
	}
	
	public void setMikioId(String id) {
		this.mikioId = id;
	}
	
	public String getNurseUid(){
		return this.nurseUid;
	}
	
	public void setNurseUid(String nurseUid){
		this.nurseUid = nurseUid;
	}
	
	public String getDecryptedNurseUid(){
		return EncodeUtils.decrypt(this.getNurseUid());
	}

	public JsonObject getStudents(){
		if (students == null)
			students = new JsonObject();
		return this.students;
	}
	
	public void setStudents(JsonObject jProperties){
		this.students = jProperties;
	}

	public void setStudents(int grade, String classId, JsonArray students) {
		this.getStudents().addProperty("grade", grade);
		this.getStudents().addProperty("classId", classId);
		this.getStudents().add("students", students);
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public String getModelNo() {
		return this.getPropertyAsString("modelNo");
	}
	
	public void setModelNo(String modelNo) {
		this.setProperty("modelNo", modelNo);
	}

	public String getSerialNo() {
		return this.getPropertyAsString("serialNo");
	}

	public void setSerialNo(String serialNo) {
		this.setProperty("serialNo", serialNo);
	}

	public String getSystemUuid() {
		return this.getPropertyAsString("systemUuid");
	}

	public void setSystemUuid(String systemUuid) {
		this.setProperty("systemUuid", systemUuid);
	}
	
	public String getLocation() {
		return this.getPropertyAsString("location");
	}

	public void setLocation(String location) {
		this.setProperty("location", location);
	}
}
