package com.mihealth.db.service;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.UUID;

import javax.persistence.Query;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.google.gson.JsonObject;
import com.mihealth.db.model.AccountModel;
import com.mihealth.db.model.UserDetailedModel;
import com.mihealth.db.model.UserModel;
import com.mihealth.db.repositories.AccountRepository;
import com.mihealth.db.repositories.UserDetailedRepository;
import com.mihealth.db.repositories.UserRepository;
import com.mihealth.db.utils.EncodeUtils;
import com.ximpl.lib.constant.DbConst;
import com.ximpl.lib.db.EntityManagerHelper;
import com.ximpl.lib.type.UserRole;
import com.ximpl.lib.util.XcJsonUtils;
import com.ximpl.lib.util.XcStringUtils;

@Service
public class UserService {
	@Value("#{filesProperties['files.root']}")
	private String filesRoot;

	@Autowired
	private EntityManagerHelper entityManagerHelper;

	
	@Autowired 
	PasswordEncoder passwordEncoder;
	
	@Autowired
	AccountRepository accountRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	UserDetailedRepository userDetailedRepository;

	public boolean exists(String id){
		return accountRepository.countById(id) > 0;
	}
	
	/**
	 * Save account info
	 * @param account
	 * @return
	 */
	public AccountModel save(AccountModel account){
		final String queryText = "REPLACE INTO tb_account(uid, userUid, idType, id, password, enabled, activatedAt) values (UNHEX(:uid), UNHEX(:userUid), :idType, :id, :password, :enabled, :activatedAt)";
		Query query = entityManagerHelper.query(queryText);
		query.setParameter(DbConst.FIELD_UID, account.getDecryptedUid());
		query.setParameter(DbConst.FIELD_USER_UID, account.getDecryptedUserUid());
		query.setParameter("idType", account.getIdType());
		query.setParameter("id", account.getId());
		query.setParameter("password", account.getPassword());
		query.setParameter(DbConst.FIELD_ENABLED, account.getEnabled());
		query.setParameter("activatedAt", account.getActivatedAt());
		entityManagerHelper.execute(query);
		
		return account;
	}

	/**
	 * Update ID of the given account
	 * @param account
	 * @param newId
	 */
	public void updateId(AccountModel account, String newId){
		if (XcStringUtils.isNullOrEmpty(newId))
			return;
		
		accountRepository.updateId(account.getId(), newId);
	}
	
	/**
	 * Update password of the given account
	 * @param account
	 * @param newPassword
	 */
	public void updatePassword(AccountModel account, String newPassword){
		updatePassword(account.getId(), newPassword);
	}

	public void updatePassword(String id, String newPassword){
		if (XcStringUtils.isNullOrEmpty(newPassword))
			return;
		final String encodedPassword = passwordEncoder.encode(newPassword);
		accountRepository.updatePassword(id, encodedPassword);
	}

	/**
	 * Enable account
	 * @param account
	 * @param enabled
	 */
	public void enable(AccountModel account, boolean enabled){
		accountRepository.enableByUserUid(account.getDecryptedUid(), enabled);
	}
	
	/**
	 * Activate a given account
	 * @param account
	 */
	public void activate(AccountModel account){
		activate(account, DateTime.now(DateTimeZone.UTC));
	}
	
	/**
	 * Deactivate a given account
	 * @param account
	 */
	public void deactivate(AccountModel account){
		activate(account, null);
	}
	
	/**
	 * Activate account
	 * @param account
	 */
	public void activate(AccountModel account, DateTime dateTime){
		accountRepository.activateById(account.getId(), dateTime == null ? null : dateTime.toDate());
	}

	/**
	 * Delete account
	 * @param account
	 */
	public void delete(AccountModel account){
		accountRepository.delete(account);
	}
	
	public void deleteById(String id){
		accountRepository.deleteById(id);
	}

	/**
	 * Get account info for the given ID
	 * @param id
	 * @return
	 */
	public AccountModel getAccount(String id){
		return accountRepository.findById(id);
	}

	/**
	 * Get all the accounts list
	 * @return
	 */
	public List<AccountModel> getAllAccounts(){
		return accountRepository.findAll();
	}

	/**
	 * Get all the account list for the given user UID
	 * @param userUid
	 * @param enabled
	 * @return
	 */
	public List<AccountModel> getAccountList(String userUid, Boolean enabled){
		return enabled == null ? getAccountList(userUid) : accountRepository.findByUserUidAndEnabled(userUid, enabled);
	}

	/**
	 * Get all the account list for the give user UID
	 * @param userUid
	 * @return
	 */
	public List<AccountModel> getAccountList(String userUid){
		return accountRepository.findByUserUid(userUid);
	}

	/**
	 * Save user
	 * @param user
	 * @return
	 */
	public UserModel save(UserModel user){
		final String queryFormat = "INSERT INTO tb_user(uid, campuses, name, nationalId, gender, birthDate, enabled, lastUpdated, registeredAt, roles, properties, settings) "
				+ "values (UNHEX(:uid), " + XcJsonUtils.toJsonCreate(user.getCampuses()) + ", :name, :nationalId, :gender, :birthDate, :enabled, :lastUpdated, :registeredAt, " + XcJsonUtils.toJsonCreate(user.getRoles()) + ", " + XcJsonUtils.toJsonCreate(user.getProperties()) + ", " + XcJsonUtils.toJsonCreate(user.getSettings()) + ") "
				+ " ON DUPLICATE KEY UPDATE campuses = VALUES(campuses), name = VALUES(name), nationalId = VALUES(nationalId), gender = VALUES(gender), birthDate = VALUES(birthDate), enabled = VALUES(enabled), lastUpdated = VALUES(lastUpdated),"
				+ " registeredAt = VALUES(registeredAt), roles = VALUES(roles), properties = VALUES(properties), settings = VALUES(settings)";

		Query query = entityManagerHelper.query(queryFormat);
		
		query.setParameter(DbConst.FIELD_UID, user.getDecryptedUid());
		query.setParameter("name", user.getName());
		query.setParameter("nationalId", user.getNationalId());
		query.setParameter("gender", user.getGender() == null ? null : user.getGender().getChar());
		query.setParameter("birthDate", user.getBirthDate());
		query.setParameter(DbConst.FIELD_ENABLED, user.getEnabled());
		query.setParameter(DbConst.FIELD_LAST_UPDATED, user.getLastUpdated());
		query.setParameter(DbConst.FIELD_REGISTERED_AT, user.getRegisteredAt());
		
		entityManagerHelper.execute(query);
		
		return user;
	}
	
	/**
	 * Get User info with given user UID
	 * @param userUid
	 * @return
	 */
	public UserModel getUser(String userUid){
		return userRepository.getOne(EncodeUtils.decrypt(userUid));
	}

	/**
	 * Get all the user list
	 * @param enabled
	 * @return
	 */
	public List<UserDetailedModel> getUserList(Boolean enabled){
		return enabled == null ? userDetailedRepository.getAll() : userDetailedRepository.getByEnabled(enabled);
	}

	public List<UserDetailedModel> getUserList(String campusUid, String role, Boolean enabled){
		if (role == null)
			return getUserList(enabled);
		else{
			campusUid = EncodeUtils.decrypt(campusUid);
			return enabled == null ? userDetailedRepository.getByRole(campusUid.toUpperCase(), role) : userDetailedRepository.getByRoleAndEnabled(campusUid.toUpperCase(), role, enabled);
		}
	}

	public UserModel getNurseByDept(String campusUid, String nurse, String dept) {
		return userRepository.findNurseByDept(campusUid, dept);
	}


	/**
	 * Remove user record and its accounts
	 * @param user
	 */
	public void delete(UserModel user){
		deleteByUserUid(user.getUid());
	}
	
	public void deleteByUserUid(String userUid){
		if (XcStringUtils.isNullOrEmpty(userUid))
			return;
		
		final String decryptedUid = EncodeUtils.decrypt(userUid);
		// Delete user
		userRepository.deleteByUserUid(decryptedUid);

		// Delete accounts for the user
		// accountRepository.deleteByUserUid(decryptedUid);
	}

	/**
	 * 
	 * @param user
	 * @param enabled
	 */
	public void enable(UserModel user, boolean enabled){
		userRepository.enableByUserUid(user.getDecryptedUid(), enabled);
	}
	
	public void updateRoles(UserModel user, JsonObject newRole){
		if (newRole == null)
			return;
		String queryText = "UPDATE tb_user SET roles = %s WHERE uid = UNHEX(:uid)";
		String formattedQuery = String.format(queryText, XcJsonUtils.toJsonCreate(newRole));
		Query query = entityManagerHelper.query(formattedQuery);
		query.setParameter(DbConst.FIELD_UID, user.getDecryptedUid());
		entityManagerHelper.execute(query);
	}
	
	public void updateProperties(UserModel user, JsonObject newProperties){
		if (newProperties == null)
			return;
		String queryText = "UPDATE tb_user SET properties = %s WHERE uid = UNHEX(:uid)";
		String formattedQuery = String.format(queryText, XcJsonUtils.toJsonCreate(newProperties));
		Query query = entityManagerHelper.query(formattedQuery);
		query.setParameter(DbConst.FIELD_UID, user.getDecryptedUid());
		entityManagerHelper.execute(query);
	}

	public void updateSettings(UserModel user, JsonObject newSettings){
		if (newSettings == null)
			return;
		String queryText = "UPDATE tb_user SET settings = %s WHERE uid = UNHEX(:uid)";
		String formattedQuery = String.format(queryText, XcJsonUtils.toJsonCreate(newSettings));
		Query query = entityManagerHelper.query(formattedQuery);
		query.setParameter(DbConst.FIELD_UID, user.getDecryptedUid());
		entityManagerHelper.execute(query);
	}

	public void updateSettings(UserModel user, String key, String value){
		if (XcStringUtils.isValid(value)){
			userRepository.updateSetting(user.getDecryptedUid(), key, value);
		}else{
			userRepository.removeSetting(user.getDecryptedUid(), key);
		}
	}

	public String getProfilePath(UserModel user, String appFileName){
		return Joiner.on('/').join(this.filesRoot, "profile", user.getUserUid()+"."+Files.getFileExtension(appFileName));
	}

	public String getProfilePath(UserModel user){
		if (user != null)
			return getProfilePath(user.getUid());
		return null;
	}

	public String getProfilePath(final String userUid){
		File profileDir = new File(Joiner.on('/').join(this.filesRoot, "profile"));
		File[] fileList = profileDir.listFiles(new FilenameFilter(){

			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(userUid);
			}
		});
		
		return fileList == null || fileList.length == 0 ? null : fileList[0].getPath();
	}

	//	public void updateEmail(String userUid, String email){
//		userRepository.updateEmail(userUid, email);
//	}
//
//	public void updateProperties(String userUid, Map<String, Object> properties){
//		userRepository.updateProperties(userUid, properties);
//	}
//
//	public void updateProperties(String userUid, JsonObject jProperties){
//		userRepository.updateProperties(userUid, XcJsonUtils.toMap(jProperties));
//	}
//
//	public void updateProperties(String userUid, String jProperties){
//		userRepository.updateProperties(userUid, XcJsonUtils.toMap(jProperties));
//	}
//
//
//	public void updateRoles(String userUid, Map<String, Boolean> roles){
//		userRepository.updateRoles(userUid, roles);
//	}
//
//	public void updateRoles(String userUid, JsonObject jRoles){
//		userRepository.updateRoles(userUid, XcJsonUtils.toBooleanMap(jRoles));
//	}
//
//	public void updateRoles(String userUid, String jRoles){
//		userRepository.updateRoles(userUid, XcJsonUtils.toBooleanMap(jRoles));
//	}
//
	
//	public List<CampusModel> getCampusList(String userUid, Boolean enabled){
//		return campusRepository.getUserCampusList(userUid);
//	}
//
//	public void putToCampus(String campusUid, String userUid){
//		campusRepository.putUserToCampus(campusUid, userUid);
//	}
//	
//	public void removeFromCampus(String campusUid, String userUid){
//		campusRepository.removeUserFromCampus(campusUid, userUid);
//	}

}
