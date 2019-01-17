package com.mihealth.db.service;

import java.util.List;
import java.util.Map.Entry;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mihealth.db.model.PropertyModel;
import com.mihealth.db.repositories.PropertyRepository;
import com.ximpl.lib.constant.DbConst;
import com.ximpl.lib.db.EntityManagerHelper;
import com.ximpl.lib.util.XcJsonUtils;
import com.ximpl.lib.util.XcStringUtils;
import com.ximpl.lib.util.XcUrlUtils;

@Service
public class PropertyService {
	@Autowired
	private EntityManagerHelper entityManagerHelper;
	
	@Autowired
	private PropertyRepository propertyRepository;
	
	/**
	 * Save property
	 * @param property
	 */
	public void save(PropertyModel property) {
		final String queryText = "REPLACE INTO tb_property(uid, category, code, properties, comment, displayOrder, enabled) VALUES (UNHEX(:uid), :category, :code, "
				+ XcJsonUtils.toJsonCreate(property.getProperties())
				+ ", :comment, :displayOrder, :enabled)";
		Query query = entityManagerHelper.query(queryText);
		query.setParameter(DbConst.FIELD_UID, property.getDecryptedUid());
		query.setParameter("category", property.getCategory());
		query.setParameter("code", property.getCode());
		query.setParameter(DbConst.FIELD_COMMENT, property.getComment());
		query.setParameter("displayOrder", property.getDisplayOrder());
		query.setParameter("enabled", property.getEnabled());

		entityManagerHelper.execute(query);
	}

	/**
	 * Get property
	 * @param category
	 * @param code
	 * @return
	 */
	public PropertyModel get(String category, String code) {
		return propertyRepository.findByCategoryAndCode(category, code);
	}
	
	public String getValues(String category, String codesValue, String localeTag) {
		String[] codes = codesValue.split(",");
		StringBuilder localizedValue = new StringBuilder();
		for(String code : codes) {
			if (localizedValue.length() > 0)
				localizedValue.append(", ");
			code = code.toLowerCase().trim();
			if (!XcStringUtils.isNullOrEmpty(code)) {
				PropertyModel property = get(category, code);
				if (property != null) {
					String name = property.getPropertyAsString(localeTag);
					localizedValue.append(XcStringUtils.isNullOrEmpty(name)? code : name);
				}else
					localizedValue.append(code);
			}
		}
		
		return localizedValue.toString();
	}

	/**
	 * Get property list
	 * @param category
	 * @param enabled
	 * @return
	 */
	public List<PropertyModel> getList(String category, Boolean enabled) {
		if (XcStringUtils.isValid(category)){
			if (enabled == null)
				return propertyRepository.findByCategory(category);
			else
				return propertyRepository.findByCategoryAndEnabled(category, enabled);
		}else{
			if (enabled == null)
				return propertyRepository.findAll();
			else
				return propertyRepository.findByEnabled(enabled);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<PropertyModel> getList(String filter, String orderBy, Boolean enabled, Integer count, Integer page){
		StringBuilder sb = new StringBuilder("SELECT uid, category, code, CAST(COLUMN_JSON(properties) AS CHAR) AS properties, comment, displayOrder, enabled, lastUpdated, registeredAt FROM tb_property ");
		JsonElement jElement = XcJsonUtils.toJsonElement(filter);
		if (jElement != null && jElement.isJsonObject()){
			JsonObject jFilter = jElement.getAsJsonObject();
			StringBuilder sbWhere = new StringBuilder();
			for(Entry<String, JsonElement> entry : jFilter.entrySet()){
				final String value = entry.getValue().getAsString();
				if (XcStringUtils.isNullOrEmpty(value))
					continue;
				if (sbWhere.length() == 0){
					sbWhere.append(" WHERE ");
					if (enabled != null){
						sbWhere.append(" enabled = ");
						sbWhere.append(enabled);
						sbWhere.append(" ");
					}
				}else
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
			
			sb.append(sbWhere);
		}
		
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
		
		//Page & Count 
//		if (page != null && page>=1 && count != null && count >=0){
//			sb.append(" LIMIT ");
//			sb.append(page - 1);
//			sb.append(", ");
//			sb.append(count);
//			sb.append(" ");
//		}
		
		Query query = entityManagerHelper.query(sb.toString(), PropertyModel.class);		
		return query.getResultList();
	}


	/**
	 * Delete property
	 * @param property
	 */
	public void delete(PropertyModel property) {
		delete(property.getCategory(), property.getCode());
	}
	
	public void delete(String category, String code){
		propertyRepository.deleteByCategoryAndCode(category, code);
	}
	
}
