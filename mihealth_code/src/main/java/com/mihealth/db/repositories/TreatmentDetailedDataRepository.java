package com.mihealth.db.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mihealth.db.model.LocaleModel;
import com.mihealth.db.model.TreatmentDetailedModel;
import com.mihealth.db.model.TreatmentDataModel;

public interface TreatmentDetailedDataRepository extends JpaRepository<TreatmentDetailedModel, String>{

	@Query(value = "SELECT uid, careUid, nurseUid, nurseName, CAST(COLUMN_JSON(treatment) AS CHAR) AS treatment, status, lastUpdated, registeredAt FROM tv_treatment_data WHERE careUid = UNHEX(?1)", nativeQuery = true)
	List<TreatmentDetailedModel> findByCareUid(String careUid);
	
}
