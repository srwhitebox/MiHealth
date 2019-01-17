package com.mihealth.db.model;

import java.util.UUID;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.joda.time.DateTime;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mihealth.db.utils.EncodeUtils;
import com.ximpl.lib.db.jpa.converter.JpaBytesUuidConverter;
import com.ximpl.lib.db.jpa.converter.JpaJsonConverter;
import com.ximpl.lib.util.XcJsonUtils;
import com.ximpl.lib.util.XcUuidUtils;

@Entity
@Table(name="tb_care_data")
public class CareDataModel extends DynamicDataModel{
	@Convert(converter = JpaBytesUuidConverter.class)
	private String userUid;
	@Convert(converter = JpaBytesUuidConverter.class)
	private String campusUid;
	private String deptId;	// department Id
	@Convert(converter = JpaBytesUuidConverter.class)
	private String nurseUid;
	private String regNo;
	private String comment;
	@Convert(converter = JpaJsonConverter.class)
	private JsonObject registeredBy;
	
	public CareDataModel(){
		
	}
	
	public CareDataModel(String userUid, String department, JsonObject jProperties, DateTime registeredAt){
		this.setUid(UUID.randomUUID());
		this.setUserUid(userUid);
		this.setDept(department);
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
	
	public void setUserUid(UUID userUuid){
		this.userUid = XcUuidUtils.encrypt(userUuid);
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

	public String getDeptId(){
		return this.deptId;
	}
	
	public void setDept(String deptId){
		this.deptId = deptId;
	}

	public String getNurseUid(){
		return this.nurseUid;
	}
	
	public void setNurseUid(String managerUid){
		this.nurseUid = managerUid;
	}
	
	public String getDecryptedManagerUid(){
		return EncodeUtils.decrypt(this.getNurseUid());
	}
	
	
	public void setManagerUid(UUID managerUid){
		this.setNurseUid(EncodeUtils.encrypt(managerUid));
	}
	
	public String getRegNo(){
		return this.regNo;
	}
	
	public void setRegNo(String regNo) {
		this.regNo = regNo;
	}
	
	public JsonObject getRegisteredBy() {
		return registeredBy;
	}

	public void setRegisteredBy(JsonObject registeredInfo) {
		this.registeredBy = registeredInfo;
	}

	public void setRegisteredBy(String registerInfo) {
		JsonElement jElement = XcJsonUtils.toJsonElement(registerInfo);
		if (jElement != null && jElement.isJsonObject())
			this.setRegisteredBy(jElement.getAsJsonObject());
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
}
