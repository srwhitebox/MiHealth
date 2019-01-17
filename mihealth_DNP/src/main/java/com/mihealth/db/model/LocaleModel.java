package com.mihealth.db.model;

import java.util.Locale;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.ximpl.lib.util.XcStringUtils;

@Entity
@Table(name="tb_locale")
public class LocaleModel {
	@Id
	private String localeTag;	// Locale language tag
	private String displayName;	// Display Language name
	private int displayOrder;	// Display Order
	private boolean enabled;	// Enable locale
	
	public LocaleModel(){
	}
	

	public LocaleModel(String tag, String displayName){
		Locale locale = Locale.forLanguageTag(tag);
		init(locale, displayName);
	}
	
	public void init(Locale locale, String displayName){
		setLocaleTag(locale.toLanguageTag());
		setDisplayName(XcStringUtils.isValid(displayName)? displayName : locale.getDisplayLanguage());
		enabled = true;
	}

	public String getLocaleTag(){
		return this.localeTag;
	}
	
	public void setLocaleTag(String localeTag){
		this.localeTag = localeTag;
	}	

	public String getDisplayName(){
		return this.displayName;
	}
	
	public void setDisplayName(String displayName){
		this.displayName = displayName;
	}	

	public int getDisplayOrder(){
		return this.displayOrder;
	}
	
	public void setDisplayOrder(int displayOrder){
		this.displayOrder = displayOrder;
	}
	
	public boolean getEnabled(){
		return this.enabled;
	}
	
	public void setEnabled(boolean enabled){
		this.enabled = enabled;
	}
	
	public Locale getLocale(){
		return Locale.forLanguageTag(getLocaleTag());
	}
}
