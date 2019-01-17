package com.mihealth.webservice;

import java.util.Collection;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mihealth.db.model.TokenModel;
import com.ximpl.lib.util.XcJsonUtils;

public class ApiResponse {
	public static final String MESSAGE_SUCCEED = "succeed";
	public static final String MESSAGE_FAILED = "failed";
	
	public static final int CODE_SUCCEED = 200;
	public static final int CODE_SUCCEED_LOGIN = 201;
	public static final int CODE_SUCCEED_LOGOUT = 202;

	public static final int CODE_MIKIO_UPDATED = 231;
	public static final int CODE_MIKIO_CONNECTED = 238;
	public static final int CODE_MIKIO_DISCONNECTED = 239;
	
	public static final int CODE_FAILED = 400;
	public static final int CODE_FAILED_LOGIN = 401;
	public static final int CODE_FAILED_LOGOUT = 402;
	public static final int CODE_FAILED_UNKNOWN_USER = 403;
	public static final int CODE_FAILED_PASSWORD_NOT_MATCHES = 404;
	public static final int CODE_FAILED_EXPIRED_TOKEN = 405;
	public static final int CODE_FAILED_NO_TOKEN = 406;
	public static final int CODE_FAILED_NO_USERS = 407;
	public static final int CODE_FAILED_NO_RECORD = 407;
	public static final int CODE_FAILED_ACCOUNT_NOT_AVAILABLE = 408;
	public static final int CODE_FAILED_NOT_FOUND = 421;
	public static final int CODE_FAILED_NOT_ENOUGH_INFO = 422;
	public static final int CODE_FAILED_NO_PARAMS = 423;
	
	public static final int CODE_FAILED_REGISTER_USER_EXISTS = 431;

	public static final int CODE_FAILED_STUDENT_EXISTS = 441;
	
	public static final int CODE_FAILED_CAMPUS_NOT_EXISTS = 451;
	
	public static final int CODE_FAILED_UNKNOWN_COMMAND = 491;
	
	public static final int CODE_UNKOWN = -1;
	
	private int code;
	private String message;
	private Object data;
	private Integer size;
	private Integer total;
	
	public ApiResponse(){
	}
	
	public ApiResponse(int code, String msg){
		this(code, msg, null, null);
	}
	
	public ApiResponse(int code, String msg, Object data){
		this(code, msg, data, null);
	}

	public ApiResponse(int code, String msg, Object data, Integer total){
		setCode(code);
		setMessage(msg);
		setData(data);
		if (total != null)
			setTotal(total);
	}

	public int getCode(){
		return this.code;
	}
	
	public void setCode(int code){
		this.code = code;
	}
	
	public String getMessage(){
		return this.message;
	}
	
	public void setMessage(String message){
		this.message = message;
	}
	
	public Object getData(){
		return this.data;
	}
	
	public void setData(Object data){
		this.data = data;
		if (data instanceof JsonArray){
			this.setSize(((JsonArray) data).size());
		}else if (data instanceof Collection ){
			this.setSize(((Collection<?>)data).size());
		}else{
			this.setSize(1);
		}
	}
	
	public Integer getSize(){
		return this.size;
	}
	
	public void setSize(Integer size){
		this.size = size;
	}

	public Integer getTotal(){
		return this.total;
	}
	
	public void setTotal(Integer total){
		this.total = total;
	}
	
	@Override
	public String toString(){
		return XcJsonUtils.getGson().toJson(this);
	}
	
	public static String getSucceedResponse(){
		return getMessage(CODE_SUCCEED, MESSAGE_SUCCEED);
	}
	
	public static String getSucceedResponse(Object data){
		return getResponse(CODE_SUCCEED, MESSAGE_SUCCEED, data);
	}

	public static String getSucceedResponse(Object data, int total){
		return getResponse(CODE_SUCCEED, MESSAGE_SUCCEED, data, total);
	}

	public static String getSucceedResponse(String succeedMessage){
		JsonObject jResult = new JsonObject();
		jResult.addProperty("result", succeedMessage);
		return getResponse(CODE_SUCCEED, MESSAGE_SUCCEED, jResult);
	}
	
	public static String getTokenResponse(TokenModel token){
		return getResponse(CODE_SUCCEED, MESSAGE_SUCCEED, token.toJsonToken());
	}
	
	public static String getFailedResponse(){
		return getMessage(CODE_FAILED, MESSAGE_FAILED);
	}

	public static String getFailedResponse(String failedMessage){
		JsonObject jResult = new JsonObject();
		jResult.addProperty("result", failedMessage);
		return getResponse(CODE_FAILED, MESSAGE_FAILED, jResult);
	}

	public static String getMessage(int code, String msg){
		return new ApiResponse(code, msg).toString();
	}
	
	public static String getResponse(int code, String msg, Object data){
		return new ApiResponse(code, msg, data).toString();
	}	
	
	public static String getResponse(int code, String msg, Object data, Integer total){
		return new ApiResponse(code, msg, data, total).toString();
	}	

}
