package com.mihealth.db.model;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mihealth.db.type.PARAMETER;
import com.mihealth.db.utils.EncodeUtils;
import com.ximpl.lib.db.jpa.converter.JpaBytesUuidConverter;
import com.ximpl.lib.db.jpa.converter.JpaJodaTimeConverter;
import com.ximpl.lib.util.XcStringUtils;
import com.ximpl.lib.util.XcUuidUtils;

@Entity
@Table(name="tb_account")
public class AccountModel {
	@Convert(converter = JpaBytesUuidConverter.class)
	private String uid;
	@Convert(converter = JpaBytesUuidConverter.class)
	private String userUid;
	private String id;
	private String idType;
	private String password;
	private boolean enabled;
	private Date activatedAt;
	@Id
	protected Date registeredAt;
	
	
	public void init(){
		initUid();
		this.setEnabled(true);
		this.initRegisteredAt();
	}

	public void init(String userUid){
		this.init();
		this.setUserUid(userUid);
	}
	
	public String getUid(){
		return this.uid;
	}
	
	public String getDecryptedUid(){
		return EncodeUtils.decrypt(this.getUid());
	}

	public void setUid(String uid){
		this.uid = uid;
	}

	public void initUid(){
		setUid(UUID.randomUUID());
	}

	public void setUid(UUID uuid){
		this.uid = XcUuidUtils.encrypt(uuid);
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
	
	public void setUserUid(UUID uuid){
		setUserUid(EncodeUtils.encrypt(XcUuidUtils.toHexString(uuid)));
	}

	public void initUserUid(){
		setUserUid(UUID.randomUUID());
	}
	
	public String getId(){
		return this.id;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public String getIdType(){
		return this.idType;
	}
	
	public void setIdType(String idType){
		this.idType = idType;
	}
	
	public String getPassword(){
		return this.password;
	}
	
	public void setPassword(String password){
		this.password = password;
	}
	
	private boolean hasPassword(){
		return !XcStringUtils.isNullOrEmpty(this.password);
	}
	public void encryptPassword(PasswordEncoder passwordEncoder){
		if (hasPassword())
			this.setPassword(passwordEncoder.encode(this.getPassword()));
	}

	public boolean getEnabled(){
		return this.enabled;
	}
	
	public void setEnabled(boolean enabled){
		this.enabled = enabled;
	}
	
	public Date getActivatedAt(){
		return this.activatedAt;
	}
	
	public void setActivatedAt(Date activatedAt){
		this.activatedAt = activatedAt;
	}

	public Date getRegisteredAt(){
		return this.registeredAt;
	}
	
	public void setRegisteredAt(Date registeredAt){
		this.registeredAt = registeredAt;
	}
	
	public boolean isAvailable(){
		return this.getEnabled() && this.getActivatedAt() != null;
	}
	
	public void initRegisteredAt(){
		this.setRegisteredAt(DateTime.now(DateTimeZone.UTC).toDate());
	}
	
	public boolean canRegist(){
		return XcStringUtils.isValid(this.id) && XcStringUtils.isValid(this.password);
	}
}
