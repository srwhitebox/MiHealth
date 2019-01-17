package com.mihealth.db.model;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="tb_property")
public class PropertyModel extends DynamicDataModel{
	private String category;
	private String code;
	private String comment;
	private int displayOrder;
	private boolean enabled;
	
	/**
	 * Constructor
	 */
	public PropertyModel(){

	}
	
	/**
	 * Constructor
	 * @param category
	 * @param code
	 * @param languageTag
	 * @param displayName
	 */
	public PropertyModel(String category, String code, String languageTag, String displayName, String comment, Integer displayOrder, Boolean enabled){
		super.initUid();
		this.setCategory(category);
		this.setCode(code);
		this.setProperty(languageTag, displayName);
		this.setComment(comment);
		if (displayOrder != null)
			this.displayOrder = displayOrder;
		if (enabled != null)
			this.enabled = enabled;		
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

	/**
	 * Get Enabled
	 * @return
	 */
	public boolean getEnabled(){
		return this.enabled;
	}
	
	/**
	 * Set Enabled
	 * @param enabled
	 */
	public void setEnabled(boolean enabled){
		this.enabled = enabled;
	}
	
	/**
	 * Get Display order
	 * @return
	 */
	public int getDisplayOrder(){
		return this.displayOrder;
	}
	
	/**
	 * Set Display order
	 * @param sequence
	 */
	public void setDisplayOrder(int sequence){
		this.displayOrder = sequence;
	}	
}
