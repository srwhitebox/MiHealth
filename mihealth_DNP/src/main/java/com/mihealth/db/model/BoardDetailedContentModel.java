package com.mihealth.db.model;

import java.util.Date;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.joda.time.DateTime;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mihealth.db.type.PARAMETER;
import com.mihealth.db.utils.EncodeUtils;
import com.ximpl.lib.constant.PropertyConst;
import com.ximpl.lib.db.jpa.converter.JpaBytesUuidConverter;
import com.ximpl.lib.db.jpa.converter.JpaJsonConverter;
import com.ximpl.lib.util.XcBooleanUtils;
import com.ximpl.lib.util.XcJsonUtils;
import com.ximpl.lib.util.XcStringUtils;

@Entity
@Table(name="tv_board_content")
public class BoardDetailedContentModel {
	@Convert(converter = JpaBytesUuidConverter.class)
	private String boardUid;
	private String boardId;
	@Convert(converter = JpaJsonConverter.class)
	private JsonObject boardProperties;

	@Convert(converter = JpaBytesUuidConverter.class)
	private String writerUid;
	private String writerName;
	@Convert(converter = JpaJsonConverter.class)
	private JsonObject writerProperties;

	
	@Convert(converter = JpaBytesUuidConverter.class)
	private String contentUid;
	@Convert(converter = JpaBytesUuidConverter.class)
	private String parentUid;
	private String title;
	private String content;
	@Convert(converter = JpaJsonConverter.class)
	private JsonObject contentProperties;

	private boolean enabled;
	private Date lastUpdated;
	@Id
	protected Date registeredAt;
	
	public String getParentUid(){
		return this.parentUid;
	}
	
	public void setParentUid(String parentUid){
		this.parentUid = parentUid;
	}
	
	public String getDecryptedParentUid(){
		return XcStringUtils.isNullOrEmpty(this.getParentUid()) ? null : EncodeUtils.decrypt(this.getParentUid());
	}
	
	public String getBoardUid(){
		return this.boardUid;
	}
	
	public void setBoardUid(String boardUid){
		this.boardUid = boardUid;
	}
	
	public String getDecryptedBoardUid(){
		return XcStringUtils.isNullOrEmpty(this.getBoardUid()) ? null : EncodeUtils.decrypt(this.getBoardUid());
	}

	public String getBoardId() {
		return boardId;
	}

	public void setBoardId(String boardId) {
		this.boardId = boardId;
	}

	public JsonObject getBoardProperties() {
		return boardProperties;
	}

	public void setBoardProperties(JsonObject boardProperties) {
		this.boardProperties = boardProperties;
	}

	public String getWriterName() {
		return writerName;
	}

	public void setWriterName(String writerName) {
		this.writerName = writerName;
	}

	public JsonObject getWriterProperties() {
		return writerProperties;
	}

	public void setWriterProperties(JsonObject writerProperties) {
		this.writerProperties = writerProperties;
	}

	public String getContentUid() {
		return contentUid;
	}

	public void setContentUid(String contentUid) {
		this.contentUid = contentUid;
	}

	public JsonObject getContentProperties() {
		return contentProperties;
	}

	public void setContentProperties(JsonObject contentProperties) {
		this.contentProperties = contentProperties;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public Date getRegisteredAt() {
		return registeredAt;
	}

	public void setRegisteredAt(Date registeredAt) {
		this.registeredAt = registeredAt;
	}

	public String getWriterUid(){
		return this.writerUid;
	}
	
	public void setWriterUid(String writerUid){
		this.writerUid = writerUid;
	}
	
	public void setWriterUid(UserModel user){
		if (user != null)
			this.setWriterUid(user.getUid());
	}
	
	public String getDecryptedWriterUid(){
		return XcStringUtils.isNullOrEmpty(this.getWriterUid()) ? null : EncodeUtils.decrypt(this.getWriterUid());
	}

	public String getTitle(){
		return this.title;
	}
	
	public void setTitle(String title){
		this.title = title;
	}

	public String getContent(){
		return this.content;
	}
	
	public void setContent(String content){
		this.content = content;
	}
	

	public boolean getEnabled(){
		return this.enabled;
	}
	
	public void setEnabled(boolean enabled){
		this.enabled = enabled;
	}	
}
