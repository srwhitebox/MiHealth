package com.mihealth.db.service;

import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mihealth.db.model.AltLocaleModel;
import com.mihealth.db.model.CareDataModel;
import com.mihealth.db.model.ResourceModel;
import com.mihealth.db.repositories.AltLocaleRepository;
import com.mihealth.db.repositories.ResourceRepository;
import com.ximpl.lib.constant.DbConst;
import com.ximpl.lib.db.EntityManagerHelper;
import com.ximpl.lib.util.XcJsonUtils;
import com.ximpl.lib.util.XcStringUtils;
import com.ximpl.lib.util.XcUrlUtils;

@Service
public class ResourceService {
	private static final String defaultLanguage = Locale.US.toLanguageTag();
	
	@Autowired
	private EntityManagerHelper entityManagerHelper;
	
	@Autowired
	private ResourceRepository resourceRepository;
	
	@Autowired
	private AltLocaleRepository altLocaleRepository;
	
	public void save(ResourceModel resource){
		final String queryText = "REPLACE INTO tb_resource(uid, category, code, comment, properties) values (UNHEX(:uid), :category, :code, :comment, "
				+ XcJsonUtils.toJsonCreate(resource.getProperties()) + ")";
		Query query = entityManagerHelper.query(queryText);
		query.setParameter(DbConst.FIELD_UID, resource.getDecryptedUid());
		query.setParameter("category", resource.getCategory());
		query.setParameter("code", resource.getCode());
		query.setParameter(DbConst.FIELD_COMMENT, resource.getComment());
		entityManagerHelper.execute(query);
	}

	public String get(String language, String category, String code){
		return resourceRepository.getResource(language, category, code);
	}
	
	public ResourceModel get(String category, String code) {
		return resourceRepository.findByCategoryAndCode(category, code);
	}
	
	public List<ResourceModel> getAll(){
		return resourceRepository.findAll();
	}
	
	@SuppressWarnings("unchecked")
	public List<ResourceModel> getAll(String category, String filter, String orderBy){
		StringBuilder sb = new StringBuilder("SELECT uid, category, code, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, comment, lastUpdated, registeredAt FROM tb_resource ");
		JsonElement jElement = XcJsonUtils.toJsonElement(filter);
		StringBuilder sbWhere = new StringBuilder();
		if (XcStringUtils.isValid(category)){
			sbWhere.append(" WHERE category = '");
			sbWhere.append(category);
			sbWhere.append("'");
		}
		if (jElement != null && jElement.isJsonObject()){
			JsonObject jFilter = jElement.getAsJsonObject();
			for(Entry<String, JsonElement> entry : jFilter.entrySet()){
				final String value = entry.getValue().getAsString();
				if (XcStringUtils.isNullOrEmpty(value))
					continue;
				if (sbWhere.length() == 0)
					sbWhere.append(" WHERE ");
				else
					sbWhere.append(" AND ");
				if (entry.getKey().startsWith("properties")){
					String key = "COLUMN_GET(properties, '"+XcUrlUtils.getPropertyKey(entry.getKey())+"' AS CHAR)";
					sbWhere.append(key);
				}else{
					sbWhere.append(entry.getKey());
				}
				sbWhere.append(" like '%");
				sbWhere.append(value);
				sbWhere.append("%' ");
			}
		}
		sb.append(sbWhere);
		
		jElement = XcJsonUtils.toJsonElement(orderBy);
		if (jElement != null && jElement.isJsonObject()){
			JsonObject jFilter = jElement.getAsJsonObject();
			StringBuilder sbOrderBy = new StringBuilder();
			for(Entry<String, JsonElement> entry : jFilter.entrySet()){
				if (sbOrderBy.length() == 0)
					sbOrderBy.append(" ORDER BY ");
				else
					sbOrderBy.append(", ");
				if (entry.getKey().startsWith("properties")){
					String key = "COLUMN_GET(properties, '"+ XcUrlUtils.getPropertyKey(entry.getKey())+"' AS CHAR)";
					sbOrderBy.append(key);
				}else{
					sbOrderBy.append(entry.getKey());
				}
				sbOrderBy.append(" ");
				sbOrderBy.append(entry.getValue().getAsString());
			}
			if(sbOrderBy.length() > 0)
				sb.append(sbOrderBy);
		}
		
		Query query = entityManagerHelper.query(sb.toString(), ResourceModel.class);		
		return query.getResultList();
	}
	
	public List<ResourceModel> getAll(String category){
		return resourceRepository.findByCategory(category);
	}

	public void delete(String category, String code) {
		resourceRepository.deleteByCategoryAndCode(category, code);
	}

	public String getLanguageCode(Locale locale){
		final AltLocaleModel localeModel = altLocaleRepository.findOneByLocaleTag(locale.toLanguageTag());
		return localeModel == null ? defaultLanguage : localeModel.getAltLocaleTag();
	}
}
