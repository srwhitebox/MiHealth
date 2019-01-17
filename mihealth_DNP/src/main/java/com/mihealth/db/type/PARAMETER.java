package com.mihealth.db.type;

public enum PARAMETER {
	uid,
	useruid,
	user_uid,
	nurseuid,
	nurse_uid,
	name,
	email,
	mail,
	id,
	campusid,
	campus_id,
	idtype,
	loginid,
	userid,
	rfid,
	nfc,
	nid,
	nationalid,
	national_id,
	pid,
	personalid,
	personal_id,
	password,
	birth,     
	birthdate,
	birth_date,
	birthday,
	birth_day, 
	gender,
	sex,
	blood,
	bloodtype,
	blood_type,
	schoolregister,
	school_register,
	register,
	registeredby,
	registered_by,
	regno,
	reg_no,
	studentno,
	student_no,
	data,
	properties,
	settings,
	roles,
	
	campusuid,
	campus_uid,
	schoolyear,
	school_year,
	grade,
	semester,
	
	care,
	careid,
	care_id,
	
	dept,
	deptid,
	dept_id,
	department,
	departmentuid,
	department_uid,

	place,
	placeuid,
	place_uid,
	
	comment,
	
	parentuid,
	parent_uid,
	
	boardid,
	board_id,
	
	boarduid,
	board_uid,
	
	categories,
	
	categoryid,
	category_id,
	
	categoryuid,
	category_uid,

	displayorder,
	display_order,

	writeruid,
	writer_uid,
	writer,
	
	title,
	content,
	
	enable,
	enabled,
	activatedat,
	activated_at,
	lastupdated,
	last_updated,
	registeredat,
	registered_at,
	unknown
	;
	
	public static PARAMETER get(String paramName){
		try{
			return PARAMETER.valueOf(paramName.toLowerCase());
		}catch(Exception e){
			return unknown;
		}
	}
}
