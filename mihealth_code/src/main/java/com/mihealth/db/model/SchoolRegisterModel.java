package com.mihealth.db.model;

import java.util.Map;
import java.util.Set;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.joda.time.DateTime;
import com.mihealth.db.type.PARAMETER;
import com.mihealth.db.utils.EncodeUtils;
import com.ximpl.lib.db.jpa.converter.JpaBytesUuidConverter;
import com.ximpl.lib.util.XcBooleanUtils;

@Entity
@Table(name="tb_school_register")
public class SchoolRegisterModel extends DynamicDataModel{
	@Convert(converter = JpaBytesUuidConverter.class)
	private String campusUid;
	@Convert(converter = JpaBytesUuidConverter.class)
	private String userUid;
	private String studentNo;
	private Integer schoolYear;
	private Integer grade;
	private Boolean enabled;
	
	public SchoolRegisterModel(){
		
	}
	
	public SchoolRegisterModel(String campusUid){
		this.initUid();
		this.setCampusUid(campusUid);
		this.setEnabled(true);
		this.initRegisteredAt();
	}

	public SchoolRegisterModel(String campusUid, String userUid, Integer schoolYear){
		this.initUid();
		this.setCampusUid(campusUid);
		this.setUserUid(userUid);
		this.setSchoolYear(schoolYear);
		this.setEnabled(true);
		this.initRegisteredAt();
	}
	
	public String getStudentNo(){
		return this.studentNo;
	}
	
	public void setStudentNo(String studentNo){
		this.studentNo = studentNo;
	}
	
	public String getCampusUid(){
		return this.campusUid;
	}
	
	public void setCampusUid(String campusUid){
		this.campusUid = campusUid;
	}
	
	public String getDecryptedCampusUid(){
		try{
			return EncodeUtils.decrypt(this.getCampusUid());
		}catch(Exception ex){
			return null;
		}
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
	
	public int getSchoolYear(){
		return this.schoolYear;
	}
	
	public void setSchoolYear(int schoolYear){
		this.schoolYear = schoolYear;
	}

	public Integer getGrade(){
		return this.grade;
	}
	
	public void setGrade(int grade){
		this.grade = grade;
	}
	
	public boolean getEnabled(){
		return this.enabled;
	}
	
	public void setEnabled(boolean enabled){
		this.enabled = enabled;
	}
	
}
