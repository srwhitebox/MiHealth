package com.mihealth.db.model;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="tb_resource")
public class ResourceModel extends DynamicDataModel {
	private String category;
	private String code;
	private String comment;
	
	public ResourceModel(){
		
	}
	
	public ResourceModel(String category, String code){
		this.initUid();
		this.setCategory(category);
		this.setCode(code);
	}
	
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}

	public static String getDefaultMessage(String code) {
		return "#" + code;
	}
}
