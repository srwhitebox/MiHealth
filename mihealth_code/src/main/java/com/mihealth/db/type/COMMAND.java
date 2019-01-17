package com.mihealth.db.type;

public enum COMMAND {
	update,
	edit,
	save,
	delete,
	remove,
	enable,
	disable,
	activate,
	deactivate,
	id,
	password,
	
	unknown
	;
	public static COMMAND get(String paramName){
		try{
			return COMMAND.valueOf(paramName.toLowerCase());
		}catch(Exception e){
			return unknown;
		}
	}
}
