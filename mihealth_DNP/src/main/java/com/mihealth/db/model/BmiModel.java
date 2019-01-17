package com.mihealth.db.model;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.gson.JsonObject;
import com.ximpl.lib.constant.PropertyConst;
import com.ximpl.lib.db.jpa.converter.JpaJsonConverter;
import com.ximpl.lib.type.FAT_LEVEL;
import com.ximpl.lib.type.GENDER;

@Entity
@Table(name="tb_bmi")
public class BmiModel {
	@Id
	private String age;
	
	@Convert(converter = JpaJsonConverter.class)
	private JsonObject male;
	
	@Convert(converter = JpaJsonConverter.class)
	private JsonObject female;

	public BmiModel(){
		
	}
	
	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public JsonObject getMale() {
		if (male == null)
			male = new JsonObject();
		return male;
	}

	public void setMale(JsonObject male) {
		this.male = male;
	}

	public JsonObject getFemale() {
		if (female == null)
			female = new JsonObject();
		return female;
	}

	public void setFemale(JsonObject female) {
		this.female = female;
	}

	public void set(GENDER gender, float normal, float over, float obesity){
		set(gender== GENDER.MALE ? this.getMale() : this.getFemale(), normal, over, obesity);
	}
	
	private void set(JsonObject jProperty, float normal, float over, float obesity){
		jProperty.addProperty(PropertyConst.normal, normal);
		jProperty.addProperty(PropertyConst.over, normal);
		jProperty.addProperty(PropertyConst.obesity, normal);
	}

	public FAT_LEVEL getLevel(GENDER gender, double bmi) {
		final JsonObject bmiBase = gender == GENDER.MALE ? this.getMale() : this.getFemale();
		
		final double normal = bmiBase.get("normal").getAsDouble();
		if (bmi < normal)
			return FAT_LEVEL.UNDER;
		
		final double over = bmiBase.get("over").getAsDouble();
		if (bmi < over)
			return FAT_LEVEL.NORMAL;
		
		final double obesity = bmiBase.get("obesity").getAsDouble();
		if (bmi < obesity)
			return FAT_LEVEL.OVER;
		
		return FAT_LEVEL.OBESITY;
	}
}
