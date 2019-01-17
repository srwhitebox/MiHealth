package com.mihealth.db.service;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.UUID;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.mihealth.db.model.CampusModel;
import com.mihealth.db.model.UserModel;
import com.mihealth.db.repositories.CampusRepository;
import com.mihealth.db.utils.EncodeUtils;
import com.mihealth.security.UserPrincipal;
import com.ximpl.lib.constant.DbConst;
import com.ximpl.lib.constant.GeneralConst;
import com.ximpl.lib.db.EntityManagerHelper;
import com.ximpl.lib.util.XcJsonUtils;
import com.ximpl.lib.util.XcStringUtils;
import com.ximpl.lib.util.XcUuidUtils;

@Service
public class CampusService {
	private static final String ITEM_CAMPUS = "campus";
	
	@Autowired
	private EntityManagerHelper entityManagerHelper;
	
	@Autowired
	private CampusRepository campusRepository;
	
	@Autowired
	private SettingsService settingsService;
	
	@Value("#{filesProperties['files.root']}")
	private String filesRoot;

	private String id;
	private String name;
	private String authKey;
	
	
	public void save(CampusModel campus){
		final String queryText = "REPLACE INTO tb_campus(uid, campusId, name, properties, settings, authKey, enabled, lastUpdated, registeredAt) "
				+ "values(UNHEX(:uid), :campusId, :name, "
				+ XcJsonUtils.toJsonCreate(campus.getProperties()) + ", " 
				+ XcJsonUtils.toJsonCreate(campus.getSettings()) 
				+ ", :authKey, :enabled, :lastUpdated, :registeredAt)";
		Query query = entityManagerHelper.query(queryText);
		query.setParameter(DbConst.FIELD_UID, campus.getDecryptedUid());
		query.setParameter(DbConst.FIELD_CAMPUS_ID, campus.getCampusId());
		query.setParameter(DbConst.FIELD_NAME, campus.getName());
		query.setParameter(DbConst.FIELD_AUTHKEY, campus.getAuthKey());
		query.setParameter(DbConst.FIELD_ENABLED, campus.getEnabled());
		query.setParameter(DbConst.FIELD_LAST_UPDATED, campus.getLastUpdated());
		query.setParameter(DbConst.FIELD_REGISTERED_AT, campus.getRegisteredAt());
		entityManagerHelper.execute(query);		
	}
	
	public CampusModel getCampusByAuthentication(Authentication authentication){
		UserModel user = UserPrincipal.getUser(authentication);
		return user==null ? null : getCampusByCampusUid(user.getCurCampus());
	}
	
	public CampusModel getCampusByCampusUid(String campusUid){
		String uuid = EncodeUtils.decrypt(campusUid);
		if (uuid != null)
			return campusRepository.findByUid(uuid);
		else
			return campusRepository.findByCampusId(campusUid);
	}

	public CampusModel getCampusByCampusId(String campusId){
		CampusModel campus = campusRepository.findByCampusId(campusId);
		try{
			if (campus == null)
				campus = getCampusByCampusUid(campusId);
		}catch(Exception ex){
			
		}
		return campus;
	}

	public List<CampusModel> findCampus(String campusId){
		return null;
	}

	public List<CampusModel> getAll(){
		return campusRepository.findAll();
	}
	
	public void delete(CampusModel campus){
		campusRepository.deleteByUid(EncodeUtils.decrypt(campus.getUid()));
	}
	
	public boolean hasSettings(){
		return settingsService.exists(ITEM_CAMPUS);
	}
	
	public String getId(){
		if (XcStringUtils.isNullOrEmpty(this.id))
			this.id = get(GeneralConst.KEY_ID);
		return id;
	}

	public void setId(String id){
		if (XcStringUtils.isNullOrEmpty(id))
			return;
		this.id = id;
		set(GeneralConst.KEY_ID, id);
	}

	public String getAuthKey(){
		if (XcStringUtils.isNullOrEmpty(this.authKey))
			this.authKey =  get(GeneralConst.KEY_AUTH_KEY);
		return this.authKey;
	}
	
	public void setAuthKey(String authKey){
		if (XcStringUtils.isNullOrEmpty(authKey))
			return;
		this.authKey = authKey;
		set(GeneralConst.KEY_AUTH_KEY, authKey);
	}
	
	public boolean isMatch(String authKey){
		if (XcStringUtils.isNullOrEmpty(authKey))
			return false;
		
		return authKey.equals(getAuthKey());
	}

	public String getName(){
		if (XcStringUtils.isNullOrEmpty(this.authKey))
			this.name =  get(GeneralConst.KEY_NAME);
		return this.name;
	}

	public void setName(String name){
		if (XcStringUtils.isNullOrEmpty(name))
			return;
		this.name = name;
		set(GeneralConst.KEY_NAME, name);
	}
	
	public String get(String key){
		return settingsService.getValue(ITEM_CAMPUS, key);
	}

	public void set(String key, String value){
		settingsService.save(ITEM_CAMPUS, key, value);
	}
	
	public String getProfilePath(String campusId, String itemType, String orgFileName){
		return Joiner.on('/').join(this.filesRoot, "campus/profile", campusId.toLowerCase() + "." + itemType.toLowerCase() + "." + Files.getFileExtension(orgFileName));
	}
	
	public String getProfilePath(String campusId, String itemType){
		File profileDir = new File(Joiner.on('/').join(this.filesRoot, "campus/profile"));
		if (XcStringUtils.isNullOrEmpty(itemType)){
			itemType = "profile";
		}
		final String fileName = campusId.toLowerCase() + "." + itemType.toLowerCase();
		File[] fileList = profileDir.listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(fileName);
			}
		});
		
		return fileList == null || fileList.length == 0 ? null : fileList[0].getPath();
	}
}
