package com.mihealth.db.model;

import java.util.Date;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ximpl.lib.db.jpa.converter.JpaBytesUuidConverter;
import com.ximpl.lib.db.jpa.converter.JpaGenderConverter;
import com.ximpl.lib.db.jpa.converter.JpaJsonConverter;
import com.ximpl.lib.db.jpa.converter.JpaTokenTypeConverter;
import com.ximpl.lib.type.GENDER;
@Entity
@Table(name="tv_student")
public class StudentModel{
	@Convert(converter = JpaBytesUuidConverter.class)
	private String registerUid;
	@Convert(converter = JpaBytesUuidConverter.class)
	private String campusUid;
	@Convert(converter = JpaBytesUuidConverter.class)
	private String userUid;
	private String name;
	private String nationalId;
	private Date birthDate;
	@Id
	private String studentNo;
	private int schoolYear;
	private int grade;
	@Convert(converter = JpaGenderConverter.class)
	private GENDER gender;
	@Convert(converter = JpaJsonConverter.class)
	private JsonObject roles;
	@Convert(converter = JpaJsonConverter.class)
	private JsonObject registerProperties;
	@Convert(converter = JpaJsonConverter.class)
	private JsonObject userProperties;
	@Convert(converter = JpaJsonConverter.class)
	private JsonObject settings;
	private Boolean userEnabled;
	private Boolean registerEnabled;
	protected Date userRegisteredAt;
	protected Date registerRegisteredAt;
	private Date userLastUpdated;
	private Date registerLastUpdated;
	
	public String getRegisterUid() {
		return registerUid;
	}

	public void setRegisterUid(String registerUid) {
		this.registerUid = registerUid;
	}
	
	public String getCampusUid() {
		return campusUid;
	}
	
	public void setCampusUid(String campusUid) {
		this.campusUid = campusUid;
	}
	
	public String getUserUid() {
		return userUid;
	}
	
	public void setUserUid(String userUid) {
		this.userUid = userUid;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getNationalId() {
		return nationalId;
	}
	
	public void setNationalId(String nationalId) {
		this.nationalId = nationalId;
	}
	
	public Date getBirthDate() {
		return birthDate;
	}
	
	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}
	
	public String getStudentNo() {
		return studentNo;
	}
	
	public void setStudentNo(String regNo) {
		this.studentNo = regNo;
	}
	
	public int getSchoolYear() {
		return schoolYear;
	}
	
	public void setSchoolYear(int schoolYear) {
		this.schoolYear = schoolYear;
	}
	
	public int getGrade() {
		return grade;
	}
	
	public void setGrade(int grade) {
		this.grade = grade;
	}
	
	public GENDER getGender() {
		return gender;
	}
	
	public void setGender(GENDER gender) {
		this.gender = gender;
	}
	
	public JsonObject getRoles() {
		return roles;
	}
	
	public void setRoles(JsonObject roles) {
		this.roles = roles;
	}
	
	public JsonObject getRegisterProperties() {
		return registerProperties;
	}
	
	public void setRegisterProperties(JsonObject registerProperties) {
		this.registerProperties = registerProperties;
	}
	
	public String getRegisterProperty(String key){
		JsonElement element = this.getRegisterProperties().get(key);
		return element == null ? null : element.getAsString();
	}
	
	public JsonObject getUserProperties() {
		return userProperties;
	}
	
	public void setUserProperties(JsonObject userProperties) {
		this.userProperties = userProperties;
	}
	
	public JsonObject getSettings() {
		return settings;
	}
	
	public void setSettings(JsonObject settings) {
		this.settings = settings;
	}
	
	public Boolean getUserEnabled() {
		return userEnabled;
	}
	
	public void setUserEnabled(Boolean enabled) {
		this.userEnabled = enabled;
	}

	public Boolean getRegisterEnabled() {
		return registerEnabled;
	}
	
	public void setRegisterEnabled(Boolean enabled) {
		this.registerEnabled = enabled;
	}

	public Date getUserRegisteredAt() {
		return userRegisteredAt;
	}
	
	public void setUserRegisteredAt(Date registeredAt) {
		this.userRegisteredAt = registeredAt;
	}
	
	public Date getRegisterRegisteredAt() {
		return registerRegisteredAt;
	}
	
	public void setRegisterRegisteredAt(Date registeredAt) {
		this.registerRegisteredAt = registeredAt;
	}

	
	public Date getUserLastUpdated() {
		return userLastUpdated;
	}
	
	public void setUserLastUpdated(Date lastUpdated) {
		this.userLastUpdated = lastUpdated;
	}
	
	public Date getRegisterLastUpdated() {
		return registerLastUpdated;
	}
	
	public void setRegisterLastUpdated(Date lastUpdated) {
		this.registerLastUpdated = lastUpdated;
	}

}
