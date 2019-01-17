package com.mihealth.db.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mihealth.db.type.PARAMETER;
import com.ximpl.lib.constant.GeneralConst;
import com.ximpl.lib.db.jpa.converter.JpaGenderConverter;
import com.ximpl.lib.db.jpa.converter.JpaJsonConverter;
import com.ximpl.lib.type.GENDER;
import com.ximpl.lib.type.UserRole;
import com.ximpl.lib.util.XcBooleanUtils;
import com.ximpl.lib.util.XcJsonUtils;
import com.ximpl.lib.util.XcStringUtils;

@SuppressWarnings("serial")
@Entity
@Table(name="tv_user")
public class UserDetailedModel extends DynamicDataModel implements Serializable {
	private String name;
	private String nationalId;
	@Convert(converter = JpaGenderConverter.class)
	private GENDER gender;
	private Date birthDate;
	@Convert(converter = JpaJsonConverter.class)
	private JsonObject campuses;
	@Convert(converter = JpaJsonConverter.class)
	private JsonObject roles;
	@Convert(converter = JpaJsonConverter.class)
	private JsonObject settings;
	private Boolean hasAccount;
	private Boolean enabled;
	
	public UserDetailedModel(){
	}
	
	public void init(){
		this.initUserUuid();
		this.setEnabled(true);
		this.initRegisteredAt();
	}
	
	public String getUserUid(){
		return getUid();
	}
	
	public void setUserUid(UUID userUuid){
		setUid(userUuid);
	}
	
	public void setUserUid(String userUuid){
		setUid(userUuid);
	}

	public void initUserUuid(){
		setUid(UUID.randomUUID());
	}

	public String getName(){
		return this.name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getNationalId(){
		return this.nationalId;
	}
	
	public void setNationalId(String nationalId){
		this.nationalId = nationalId;
	}

	public GENDER getGender(){
		return this.gender;
	}
	
	public void setGender(GENDER gender){
		this.gender = gender;
	}
	
	public Date getBirthDate(){
		return this.birthDate;
	}
	
	public void setBirthDate(Date birthDate){
		this.birthDate = birthDate;
	}
	
	public void setBirthDate(DateTime birthDate){
		this.setBirthDate(birthDate.toDate());
	}

	public boolean getEnabled(){
		return this.enabled;
	}
	
	public void setEnabled(boolean enabled){
		this.enabled = enabled;
	}
	
	public Boolean getHasAccount() {
		return hasAccount;
	}

	public void setHasAccount(Boolean hasAccount) {
		this.hasAccount = hasAccount;
	}

	public JsonObject getSettings(){
		if (this.settings == null)
			this.settings = new JsonObject();
		return this.settings;
	}
	
	public void setSettings(JsonObject settings){
		this.settings = settings;
	}

	public void setSettings(JsonElement jSettings){
		if (jSettings != null && jSettings.isJsonObject())
			this.setSettings(jSettings.getAsJsonObject());
	}
	
	public void setSettings(String settings){
		this.setSettings(XcJsonUtils.toJsonElement(settings));
	}

	public String getSettingAsString(String key){
		JsonElement jValue = this.getSettings().get(key);
		
		return jValue == null ? null : jValue.getAsString();
	}
	
	public void setSetting(String key, String value){
		this.getSettings().addProperty(key,  value);
	}

	public String getTheme(){
		final String theme = getSettingAsString(GeneralConst.KEY_THEME);
		return XcStringUtils.isNullOrEmpty(theme) ? GeneralConst.VALUE_DEFAULT : theme;
	}
	
	public JsonObject getCampuses(){
		if (this.campuses == null)
			this.campuses = new JsonObject();
		return this.campuses;
	}
	
	public void setCampuses(JsonObject jCampuses){
		this.campuses = jCampuses;
	}
	
	public JsonObject getRoles(){
		if (this.roles == null)
			this.roles = new JsonObject();
		return this.roles;
	}
	
	public void setRoles(JsonObject jRoles){
		this.roles = jRoles;
	}
	public void setRoles(JsonElement jRoles){
		if (jRoles != null && jRoles.isJsonObject())
			this.setRoles(jRoles.getAsJsonObject());
	}

	public void setRoles(String roles){
		setRoles(XcJsonUtils.toJsonElement(roles));
	}

	public void addRole(String role){
		this.getRoles().addProperty(role, true);
	}
	
	public void removeRole(String role){
		this.getRoles().remove(role);
	}
	
	public boolean hasRole(String role){
		return this.getRoles().has(role);
	}
	
	public boolean hasPermission(String userUid){
		if (XcStringUtils.isNullOrEmpty(this.getUserUid()))
			return false;
		
		return this.getUserUid().equals(userUid) || this.hasRole(UserRole.ADMIN);
	}
	
	public List<GrantedAuthority> getAuthorities(){
		if (this.getRoles().isJsonNull()){
			this.addRole(UserRole.USER);
		}

		return UserRole.getAuthorities(this.getRoles());
	}
	
}
