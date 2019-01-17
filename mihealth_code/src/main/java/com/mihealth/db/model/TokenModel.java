package com.mihealth.db.model;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.joda.time.DateTime;

import com.google.gson.JsonObject;
import com.ximpl.lib.db.jpa.converter.JpaBytesUuidConverter;
import com.ximpl.lib.db.jpa.converter.JpaTokenTypeConverter;
import com.ximpl.lib.type.TOKEN_TYPE;
import com.ximpl.lib.util.XcUuidUtils;

@Entity
@Table(name="tb_token")
public class TokenModel {
	@Convert(converter = JpaBytesUuidConverter.class)
	private String tokenUid;
	@Convert(converter = JpaBytesUuidConverter.class)
	private String userUid;
	@Convert(converter = JpaTokenTypeConverter.class)
	private TOKEN_TYPE tokenType;
	@Id
	protected Date registeredAt;
	
	public TokenModel(){
	}
	
	public TokenModel(String uid, TOKEN_TYPE tokenType){
		this(uid, tokenType, UUID.randomUUID());
	}
	
	public TokenModel(StudentModel student, TOKEN_TYPE tokenType){
		this(student.getUserUid(), tokenType);
	}
	
	public TokenModel(String uid, TOKEN_TYPE tokenType, UUID tokenUuid){
		this.userUid = uid;
		this.tokenType = tokenType;
		this.setTokenUid(tokenUuid);
		
		final DateTime registeredAt = new DateTime(); 
		this.setRegisteredAt(registeredAt.toDate());
	}
	
	public String getTokenUid(){
		return this.tokenUid;
	}
	
	public void setTokenUid(String tokenUid){
		this.tokenUid = tokenUid;
	}
	
	public String getUserUid(){
		return this.userUid;
	}
	
	public void setUserUid(String userUid){
		this.userUid = userUid;
	}
	
	public TOKEN_TYPE getTokenType(){
		return this.tokenType;
	}
	
	public void setTokenType(TOKEN_TYPE tokenType){
		this.tokenType = tokenType;
	}
	
	public void setTokenUid(UUID tokenUuid){
		if (tokenUuid != null)
			setTokenUid(XcUuidUtils.encrypt(tokenUuid));
	}
	
	public Date getRegisteredAt(){
		return this.registeredAt;
	}
	
	public void setRegisteredAt(Date registeredAt){
		this.registeredAt = registeredAt;
	}

	public void refreshToken(){
		this.setTokenUid(UUID.randomUUID());
	}
	
	public boolean isExpired(long time){
		DateTime dateTime = new DateTime(this.registeredAt);
		return (new DateTime().getMillis() - time) > dateTime.getMillis();
	}
	
	public JsonObject toJsonToken(){
		JsonObject jToken = new JsonObject();
		jToken.addProperty("token", this.getTokenUid());
		
		return jToken;
	}
}
