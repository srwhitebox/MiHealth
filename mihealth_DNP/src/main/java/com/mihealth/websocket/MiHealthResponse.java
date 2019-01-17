package com.mihealth.websocket;

import org.springframework.web.socket.TextMessage;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mihealth.db.model.CareDataModel;
import com.mihealth.db.model.MeasurementDataModel;
import com.mihealth.db.model.StudentModel;
import com.mihealth.db.model.TreatmentDataModel;
import com.ximpl.lib.util.XcJsonUtils;

public class MiHealthResponse {
	public static final String KEY_TYPE = "type"; 
	public static final String KEY_DATE = "date"; 
	public static final String KEY_STUDENT = "student"; 
	public static final String KEY_DATA = "data"; 
	public static final String KEY_CARE_DATA = "care_data"; 
	public static final String KEY_TREATMENT_DATA = "treatment_data"; 
	
	public static final int CODE_SUCCEED = 200;
	public static final int CODE_NOT_FOUND = 400;

	public static final String DATA_TYPE_MEASUREMENT = "measurement";
	public static final String DATA_TYPE_CARE = "care";
	public static final String DATA_TYPE_TREATMENT = "treatment";
	
	private int responseCode = CODE_SUCCEED;
	private int count;
	private JsonArray data;
	
	public MiHealthResponse(){
		
	}
	
	public MiHealthResponse(StudentModel student, MeasurementDataModel measurementData){
		if (measurementData == null)
			responseCode = CODE_NOT_FOUND;
		else{
			if (data == null)
				data = new JsonArray();

			count = 1;
			JsonObject jData = new JsonObject();
			jData.addProperty(KEY_TYPE, DATA_TYPE_MEASUREMENT);
			jData.addProperty(KEY_DATE, measurementData.getLastUpdated().getTime());
			jData.add(KEY_STUDENT, XcJsonUtils.getGson().toJsonTree(student));
			jData.add(KEY_DATA, measurementData.getProperties());
			data.add(jData);
		}
	}
	
	public MiHealthResponse(StudentModel student, CareDataModel careData){
		if (careData == null)
			responseCode = CODE_NOT_FOUND;
		else{
			if (data == null)
				data = new JsonArray();

			count = 1;
			JsonObject jData = new JsonObject();
			jData.addProperty(KEY_TYPE, DATA_TYPE_CARE);
			jData.addProperty(KEY_DATE, careData.getLastUpdated().getTime());
			jData.add(KEY_STUDENT, XcJsonUtils.getGson().toJsonTree(student));
			jData.add(KEY_DATA, careData.getProperties());
			data.add(jData);
		}
	}

	public MiHealthResponse(StudentModel student, CareDataModel careData, TreatmentDataModel treatmentData){
		if (treatmentData == null)
			responseCode = CODE_NOT_FOUND;
		else{
			if (data == null)
				data = new JsonArray();

			count = 1;
			JsonObject jData = new JsonObject();
			jData.addProperty(KEY_TYPE, DATA_TYPE_TREATMENT);
			jData.addProperty(KEY_DATE, treatmentData.getLastUpdated().getTime());
			jData.add(KEY_STUDENT, XcJsonUtils.getGson().toJsonTree(student));
			jData.add(KEY_CARE_DATA, careData.getProperties());
			jData.add(KEY_TREATMENT_DATA, treatmentData.getTreatment());
			data.add(jData);
		}
	}

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public JsonArray getData() {
		return data;
	}

	public void setData(JsonArray data) {
		this.data = data;
	}
	
	public TextMessage toTextMessage(){
		return new TextMessage(XcJsonUtils.toJsonElement(this).toString());
	}
	
	public static MiHealthResponse getInstance(String jResponse){
		return XcJsonUtils.toObject(jResponse, MiHealthResponse.class);
	}
	
	public static MiHealthResponse getInstance(byte[] jResponse){
		return XcJsonUtils.toObject(XcJsonUtils.toJson(jResponse), MiHealthResponse.class);
	}
}
