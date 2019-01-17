package com.mihealth.db.model;

import java.util.Locale;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="tb_alt_locale")
public class AltLocaleModel {
	@Id
	private String localeTag;	// Locale language tag
	private String altLocaleTag;		// Tag to substitute
	
	public AltLocaleModel(){
	}
	
	public AltLocaleModel(Locale locale, Locale altLocale){
		init(locale, altLocale);
	}

	public AltLocaleModel(String tag, String subsTag){
		Locale locale = Locale.forLanguageTag(tag);
		Locale altLocale = Locale.forLanguageTag(subsTag);
		init(locale, altLocale);
	}
	
	public void init(Locale locale, Locale altLocale){
		setLocaleTag(locale.toLanguageTag());
		setAltLocaleTag(altLocale.toLanguageTag());
	}

	public String getLocaleTag(){
		return this.localeTag;
	}
	
	public void setLocaleTag(String localeTag){
		this.localeTag = localeTag;
	}	

	public String getAltLocaleTag(){
		return this.altLocaleTag;
	}
	
	public void setAltLocaleTag(String altLocaleTag){
		this.altLocaleTag = altLocaleTag;
	}
	
	public Locale getLocale(){
		return Locale.forLanguageTag(this.altLocaleTag);
	}
	
	public String getDisplayName(Locale locale){
		return getLocale().getDisplayLanguage(locale);
	}
}
