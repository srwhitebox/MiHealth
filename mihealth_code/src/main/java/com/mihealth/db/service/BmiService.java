package com.mihealth.db.service;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mihealth.db.model.BmiModel;
import com.mihealth.db.repositories.BmiRepository;
import com.ximpl.lib.type.FAT_LEVEL;
import com.ximpl.lib.type.GENDER;

@Service
public class BmiService {
	@Autowired
	private BmiRepository bmiRepository;
	
	public void save(BmiModel bmi){
		bmiRepository.save(bmi);
	}
	
	public void delete(BmiModel bmi){
		bmiRepository.delete(bmi);
	}
	
	public List<BmiModel> findAll(){
		return bmiRepository.findAll();
	}
	
	public BmiModel find(Date birth){
		final DateTime birthDate = new DateTime(birth == null ? new DateTime(1960, 01, 01, 0, 0) : birth);
		Months years = Months.monthsBetween(birthDate, new DateTime());
		int months = years.getMonths();
		int age = months/12;
		String key = "" + (age < 18 ? age : 18);

		if (age < 18 && months % 12 > 5)
			key = key+".5";
		
		BmiModel bmi = bmiRepository.findOne(key);
		
		return bmi == null ? bmiRepository.findOne("18+") : bmi;
	}
	
	public FAT_LEVEL getLevel(GENDER gender, Date birthDate, float height, float weight){
		final double bmi = getBmi(height, weight);
		final BmiModel bmiBase = find(birthDate);
		return bmiBase.getLevel(gender == null ? GENDER.MALE : gender, bmi);
	}
	
	public double getBmi(float height, float weight){
		return weight / Math.pow(height/100, 2);
	}
}
