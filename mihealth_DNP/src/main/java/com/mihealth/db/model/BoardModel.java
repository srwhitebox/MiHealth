package com.mihealth.db.model;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.mihealth.db.type.PARAMETER;
import com.ximpl.lib.util.XcBooleanUtils;

@Entity
@Table(name="tb_board")
public class BoardModel extends DynamicDataModel{
	private String boardId;
	private String comment;
	private Boolean enabled;
	
	public String getBoardId(){
		return this.boardId;
	}
	
	public void setBoardId(String boardId){
		this.boardId = boardId;
	}
	
	public String getComment(){
		return this.comment;
	}
	
	public void setComment(String comment){
		this.comment = comment;
	}
	
	public Boolean getEnabled(){
		return this.enabled;
	}
	
	public void setEnabled(Boolean enabled){
		this.enabled = enabled;
	}
	
}
