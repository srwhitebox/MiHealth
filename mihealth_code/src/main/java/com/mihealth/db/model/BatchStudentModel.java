package com.mihealth.db.model;

import java.util.Date;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.mihealth.db.repositories.BatchStudentIdClass;
import com.ximpl.lib.db.jpa.converter.JpaBytesUuidConverter;

@Entity
@Table(name="tv_batch_student")
public class BatchStudentModel {
	@Convert(converter = JpaBytesUuidConverter.class)
	private String mikioUid;
	
	@Convert(converter = JpaBytesUuidConverter.class)
	private String studentUid;
	
	@Id
	private int measurementOrder;
	
	private String name;
	private String nationalId;
	private Date birthDate;
	
	private String studentNo;
	private Integer schoolYear;
	private Integer grade;
	private String classId;
	private Integer seat;
	
	private Boolean isAbsent;

	public String getMikioUid() {
		return mikioUid;
	}

	public void setMikioUid(String mikioUid) {
		this.mikioUid = mikioUid;
	}

	public String getStudentUid() {
		return studentUid;
	}

	public void setStudentUid(String studentUid) {
		this.studentUid = studentUid;
	}

	public int getMeasurementOrder() {
		return measurementOrder;
	}

	public void setMeasurementOrder(int order) {
		this.measurementOrder = order;
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

	public void setStudentNo(String studentNo) {
		this.studentNo = studentNo;
	}

	public Integer getSchoolYear() {
		return schoolYear;
	}

	public void setSchoolYear(Integer schoolYear) {
		this.schoolYear = schoolYear;
	}

	public Integer getGrade() {
		return grade;
	}

	public void setGrade(Integer grade) {
		this.grade = grade;
	}

	public String getClassId() {
		return classId;
	}

	public void setClassId(String classId) {
		this.classId = classId;
	}

	public Integer getSeat() {
		return seat;
	}

	public void setSeat(Integer seat) {
		this.seat = seat;
	}

	public Boolean getIsAbsent() {
		return isAbsent;
	}

	public void setIsAbsent(Boolean isAbsent) {
		this.isAbsent = isAbsent;
	}
	
	
}
