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
@Table(name="tv_measurement_data")
public class StudentMeasurementDataModel{
	@Convert(converter = JpaBytesUuidConverter.class)
	private String dataUid;
	@Convert(converter = JpaBytesUuidConverter.class)
	private String userUid;
	private String name;
	private String nationalId;
	private Date birthDate;
	@Convert(converter = JpaGenderConverter.class)
	private GENDER gender;
	private String studentNo;
	private int schoolYear;
	private int grade;
	@Convert(converter = JpaJsonConverter.class)
	private JsonObject registerProperties;
	@Convert(converter = JpaJsonConverter.class)
	private JsonObject properties;
	@Convert(converter = JpaJsonConverter.class)
	private JsonObject registeredBy;

	private Date lastUpdated;
	@Id
	protected Date registeredAt;
	
	public String getDataUid() {
		return this.dataUid;
	}

	public void setDataUid(String dataUid) {
		this.dataUid = dataUid;
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
	
	public GENDER getGender() {
		return gender;
	}
	
	public void setGender(GENDER gender) {
		this.gender = gender;
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
	
	public JsonObject getRegisterProperties() {
		return registerProperties;
	}
	
	public void setRegisterProperties(JsonObject registerProperties) {
		this.registerProperties = registerProperties;
	}
	
	public JsonObject getProperties() {
		return properties;
	}
	
	public void setProperties(JsonObject properties) {
		this.properties = properties;
	}
	
	public Number getNumericProperty(String key){
		if (this.getProperties() == null)
			return null;
		
		JsonElement jValue = this.getProperties().get(key);

		return jValue == null ? null : jValue.getAsNumber();
	}
	
	public JsonObject getRegisteredBy() {
		return this.registeredBy;
	}
	
	public void setRegisteredBy(JsonObject registeredBy) {
		this.registeredBy = registeredBy;
	}
	
	public Date getRegisteredAt() {
		return registeredAt;
	}
	
	public void setRegisteredAt(Date registeredAt) {
		this.registeredAt = registeredAt;
	}
	
	public Date getLastUpdated() {
		return lastUpdated;
	}
	
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
}
