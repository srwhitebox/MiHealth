package com.mihealth.db.model;

import javax.persistence.Convert;
import javax.persistence.Entity;
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
@Table(name="tb_board_content")
public class BoardContentModel extends DynamicDataModel {
	@Convert(converter = JpaBytesUuidConverter.class)
	private String parentUid;
	@Convert(converter = JpaBytesUuidConverter.class)
	private String boardUid;
	@Convert(converter = JpaBytesUuidConverter.class)
	private String categoryUid;
	@Convert(converter = JpaBytesUuidConverter.class)
	private String writerUid;	// Writer UID
	private String title;
	private String content;
	private boolean enabled;

	public void init(){
		this.setCategoryUid();
		this.setEnabled(true);
		this.initRegisteredAt();		
	}

	public void setCategoryUid(){
		this.initUid();
	}
	
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
	
	public String getDecryptedBoardUid(){
		return XcStringUtils.isNullOrEmpty(this.getBoardUid()) ? null : EncodeUtils.decrypt(this.getBoardUid());
	}

	public void setBoardUid(String boardUid){
		this.boardUid = boardUid;
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
	
	public String getDocumentNo(){
		return this.getPropertyAsString(PropertyConst.DOCUMENT_NO);
	}
	
	public void setDocumentNo(String documentNo){
		this.setProperty(PropertyConst.DOCUMENT_NO, documentNo);
	}

	public String getReference(){
		return this.getPropertyAsString(PropertyConst.REFERENCE);
	}
	
	public void setReference(String reference){
		this.setProperty(PropertyConst.REFERENCE, reference);
	}
	
	public long getDisplayFrom(){
		return this.getPropertyAsLong(PropertyConst.POST_FROM);
	}
	
	public void setDisplayFrom(DateTime displayFrom){
		this.setProperty(PropertyConst.POST_FROM, displayFrom.getMillis());
	}
	
	public long getDisplayTo(){
		return this.getPropertyAsLong(PropertyConst.POST_TO);
	}
	
	public void setDisplayTo(DateTime displayTo){
		this.setProperty(PropertyConst.POST_TO, displayTo.getMillis());
	}

	public boolean getEnabled(){
		return this.enabled;
	}
	
	public void setEnabled(boolean enabled){
		this.enabled = enabled;
	}
	
	public boolean canRegister(){
		return !XcStringUtils.isNullOrEmpty(this.getTitle()); 
	}
	
	public String getCategoryUid(){
		return this.categoryUid;
	}
	
	public void setCategoryUid(String categoryUid){
		this.categoryUid = categoryUid;
	}
	
	public String getDecryptedCategoryUid(){
		return XcStringUtils.isNullOrEmpty(this.getCategoryUid()) ? null : EncodeUtils.decrypt(this.getCategoryUid());
	}

}
