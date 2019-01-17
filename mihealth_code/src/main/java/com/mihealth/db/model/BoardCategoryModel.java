package com.mihealth.db.model;

import java.util.UUID;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.mihealth.db.type.PARAMETER;
import com.mihealth.db.utils.EncodeUtils;
import com.ximpl.lib.db.jpa.converter.JpaBytesUuidConverter;
import com.ximpl.lib.util.XcBooleanUtils;

@Entity
@Table(name="tb_board_category")
public class BoardCategoryModel extends DynamicDataModel{
	@Convert(converter = JpaBytesUuidConverter.class)
	private String parentUid;	// Parent Category UUID
	@Convert(converter = JpaBytesUuidConverter.class)
	private String boardUid;
	private String categoryId;
	private int displayOrder;
	private boolean enabled;

	public String getCategoryUid(){
		return this.getUid();
	}
	
	public void setCategoryUid(UUID categoryUuid){
		this.setUid(categoryUuid);
	}
	
	public void setCategoryUid(){
		initUid();
	}

	public String getParentUid(){
		return this.parentUid;
	}
	
	public String getDecryptedParentUid(){
		return this.getParentUid() == null ? null : EncodeUtils.decrypt(this.getParentUid());
	}
	
	public void setParentUid(String parentUid){
		this.parentUid = parentUid;
	}
	
	public String getBoardUid(){
		return this.boardUid;
	}
	
	public String getDecryptedBoardUid(){
		return this.getBoardUid() == null ? null : EncodeUtils.decrypt(this.getBoardUid());
	}
	
	public void setBoardUid(String boardUid){
		this.boardUid = boardUid;
	}
	
	public String getCategoryId(){
		return this.categoryId;
	}
	
	public void setCategoryId(String categoryId){
		this.categoryId = categoryId;
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
}
