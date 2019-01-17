package com.mihealth.webservice;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mihealth.db.model.AccountModel;
import com.mihealth.db.model.CampusModel;
import com.mihealth.db.model.TokenModel;
import com.mihealth.db.model.UserDetailedModel;
import com.mihealth.db.model.UserModel;
import com.mihealth.db.service.TokenService;
import com.mihealth.db.service.UserService;
import com.mihealth.db.type.COMMAND;
import com.mihealth.db.utils.EncodeUtils;
import com.mihealth.security.UserPrincipal;
import com.ximpl.lib.constant.DbConst;
import com.ximpl.lib.constant.GeneralConst;
import com.ximpl.lib.constant.PropertyConst;
import com.ximpl.lib.type.ID_TYPE;
import com.ximpl.lib.type.TOKEN_TYPE;
import com.ximpl.lib.type.UserRole;
import com.ximpl.lib.util.XcJsonUtils;
import com.ximpl.lib.util.XcStringUtils;
import com.ximpl.lib.util.XcUuidUtils;

@Controller
@RequestMapping(value = "/api")
public class UserApi {
	@Resource(name = "authenticationManager")
	private AuthenticationManager authenticationManager;
	
	@Autowired
	UserService userService;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	TokenService tokenService;
	
	@RequestMapping(value = {"account/update", "account/save"}, method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String updateAccount(HttpServletRequest request) {
		JsonElement jElement = XcJsonUtils.toJsonElement(request);
		String mode = "";
		
		if(jElement instanceof JsonObject){
			final JsonObject jObject = jElement.getAsJsonObject();
			mode = jObject.get("mode").getAsString();
		}
		
		AccountModel account = (AccountModel)XcJsonUtils.toObject(jElement, AccountModel.class);
		
		if (mode.equals("new")){
			account.init();
			account.encryptPassword(passwordEncoder);
		}else if (mode.equals("password") || mode.equals("edit")){
			account.encryptPassword(passwordEncoder);
		}else if(mode.equals("update")){
			
		}

		if (account == null){
			return ApiResponse.getFailedResponse();
		}
		
		if (XcStringUtils.isNullOrEmpty(account.getUserUid())){
			return ApiResponse.getFailedResponse();
		}
		
		if (XcStringUtils.isNullOrEmpty(account.getUid())){
			account.init();
		}
		
		userService.save(account);;

		return ApiResponse.getSucceedResponse(account);
	}

	/**
	 * Determine whether the ID is exist
	 * @param id
	 * @return
	 */
	@RequestMapping(value = {"account/exists"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String exists(@RequestParam String id) {
		boolean exist = userService.exists(id);
		return ApiResponse.getSucceedResponse(exist);
	}
	
	@RequestMapping(value = {"account/password/{id}"}, method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String updatePassword(HttpServletRequest request, @PathVariable String id, @RequestParam String password) {
		userService.updatePassword(id, password);

		return ApiResponse.getSucceedResponse();
	}

	@RequestMapping(value = {"account/delete/{id}", "account/remove/{id}"}, method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String deleteAccount(HttpServletRequest request, @PathVariable String id) {
		userService.deleteById(id);

		return ApiResponse.getSucceedResponse();
	}

	/**
	 * Get account information for give ID
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/account/{id}", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String getAccount(@PathVariable String id){
		AccountModel account = userService.getAccount(id);
		if (account == null){
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_USERS, "No account is found.");
		}else
			return ApiResponse.getSucceedResponse(account);
	}
	
	/**
	 * Command for an given account.
	 * Command : delete, enable, disable, activate
	 * @param authentication
	 * @param model
	 * @param command
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/account/{command}/{id}", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String accountCommand(Authentication authentication, Model model, @PathVariable("command") String command, @PathVariable("id") String id, @RequestParam(value = "value", required = false) String value) {
		AccountModel account = userService.getAccount(id);
		if (account == null){
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_UNKNOWN_USER, "The account is not found.");
		}else{
			if (authentication == null)
				return ApiResponse.getMessage(ApiResponse.CODE_FAILED_UNKNOWN_COMMAND, "No logged user.");

			UserModel user = UserPrincipal.getUser(authentication); 
			
			switch(COMMAND.get(command)){
			case delete:
			case remove:
				if (user.hasPermission(account.getUserUid())){
					userService.delete(account);
					return ApiResponse.getSucceedResponse("The account has been removed.");
				}
			case enable:
				if (user.hasPermission(account.getUserUid()) && !account.getEnabled()){
					userService.enable(account, true);
				}
				return ApiResponse.getSucceedResponse("The account has been enabled.");
			case disable:
				if (user.hasPermission(account.getUserUid()) && account.getEnabled()){
					userService.enable(account, false);
				}
				return ApiResponse.getSucceedResponse("The account has been disabled.");
			case activate:
				if (user.hasPermission(account.getUserUid()) && account.getActivatedAt() == null){
					userService.activate(account);
					return ApiResponse.getSucceedResponse("The account has been activated.");
				}else
					return ApiResponse.getSucceedResponse("Activated account.");
			case id:
				if (XcStringUtils.isNullOrEmpty(value))
					return ApiResponse.getSucceedResponse("The password is not defined.");
				userService.updateId(account, value);
				return ApiResponse.getSucceedResponse("Password has been changed.");
			case password:
				if (XcStringUtils.isNullOrEmpty(value))
					return ApiResponse.getSucceedResponse("The password is not defined.");
				userService.updatePassword(account, value);
				return ApiResponse.getSucceedResponse("Password has been changed.");
			default:
				return ApiResponse.getMessage(ApiResponse.CODE_FAILED_UNKNOWN_COMMAND, "Unknown command.");
			}
		}
	}
	
	

	/**
	 * Get all the account list
	 * @return
	 */
	@RequestMapping(value = "/accounts", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String getAllAccounts(){
		List<AccountModel> accounts = userService.getAllAccounts();
		if (accounts == null){
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_USERS, "No account is found.");
		}else
			return ApiResponse.getSucceedResponse(accounts);
	}

	/**
	 * Get all the account of user with given user UID
	 * @param userUid
	 * @param enabled
	 * @return
	 */
	@RequestMapping(value = "/accounts/{userUid}", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String getAccounts(@PathVariable String userUid, @RequestParam(value = DbConst.FIELD_ENABLED, required = false) Boolean enabled){
		List<AccountModel> accounts = userService.getAccountList(userUid, enabled);
		if (accounts == null){
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_USERS, "No account is found.");
		}else
			return ApiResponse.getSucceedResponse(accounts);
	}
	

	@RequestMapping(value = {"user/register"}, method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String register(HttpServletRequest request, Authentication authentication) {
		final JsonElement jElement = XcJsonUtils.toJsonElement(request);
		if (jElement == null)
			return ApiResponse.getFailedResponse();
		if (jElement instanceof JsonObject){
			final JsonObject jObject = jElement.getAsJsonObject();
			final JsonElement jAccount = jObject.get("account");
			final JsonElement jCampus = jObject.get("campus");
			final AccountModel account = (AccountModel)XcJsonUtils.toObject(jAccount, AccountModel.class);
			final UserModel user = (UserModel)XcJsonUtils.toObject(jElement, UserModel.class);
			final CampusModel campus = (CampusModel)XcJsonUtils.toObject(jCampus, CampusModel.class);
			user.init();
			user.addCampus(campus.getDecryptedUid());
			account.init();
			account.encryptPassword(passwordEncoder);
			account.setUserUid(user.getUid());
			
			userService.save(account);
			userService.save(user);
			
			return ApiResponse.getSucceedResponse();
		}
		return ApiResponse.getFailedResponse();
	}
	
	@RequestMapping(value = {"user/save"}, method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String saveUser(HttpServletRequest request, Authentication authentication) {
		JsonObject jData = XcJsonUtils.toJsonElement(request).getAsJsonObject();
		
		final UserModel user = (UserModel)XcJsonUtils.toObject(jData, UserModel.class);
		if (user == null)
			return ApiResponse.getFailedResponse();
		
		if (XcStringUtils.isNullOrEmpty(user.getUid())){
			user.init();
			JsonObject jCampus = jData.get(DbConst.FIELD_CAMPUS).getAsJsonObject();
			final String campusUuid = EncodeUtils.decrypt(jCampus.get(DbConst.FIELD_UID).getAsString());
			user.addCampus(campusUuid.toUpperCase());
		}

		userService.save(user);
		
//		UserModel loggedUser = UserPrincipal.getUser(authentication);
//		if (loggedUser.getUid().equals(user.getUid()))
//			authorize(user);
		
		return ApiResponse.getSucceedResponse(user);
	}

	@RequestMapping(value = {"user/update"}, method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String updateUser(String content, Principal principal) {
		JsonElement jContent = XcJsonUtils.toJsonElement(content);
		
		UserModel user = (UserModel) XcJsonUtils.toObject(jContent, UserModel.class);
		if (user.getUid() == null){
			user.init();
		}
		List<AccountModel> accountList = userService.getAccountList(user.getUid());
		userService.save(user);
		if (accountList == null || accountList.isEmpty()){
			AccountModel account = (AccountModel) XcJsonUtils.toObject(jContent.getAsJsonObject().get("account"), AccountModel.class);
			account.setUserUid(user.getUserUid());
			account.encryptPassword(passwordEncoder);
			account.init();
			account.setActivatedAt(new Date());
			userService.save(account);
		}
		return ApiResponse.getSucceedResponse(user);
	}

	/**
	 * Get user info with given user UID
	 * @param userUid
	 * @return
	 */
	@RequestMapping(value = "/user/{userUid}", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String getUser(@PathVariable String userUid) {
		UserModel user = userService.getUser(userUid);
		if (user != null)
			return ApiResponse.getSucceedResponse(user);
		else
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_PARAMS, "User UID not found.");
	}

	@RequestMapping(value = "/user/settings", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String updateUserSetting(Authentication authentication, @RequestParam String key, @RequestParam String value) {
		if (authentication == null)
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_UNKNOWN_COMMAND, "No logged user.");
		
		UserModel user = UserPrincipal.getUser(authentication);
		user.setSetting(key, value);
		userService.updateSettings(user, key, value);
		
		return ApiResponse.getSucceedResponse();
	}
	
	
	
	/**
	 * Get all the user list
	 * @param enabled
	 * @return
	 */
	@RequestMapping(value = "/users", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String getUsers(@RequestParam(value = DbConst.FIELD_ENABLED, required = false) Boolean enabled) {
		List<UserDetailedModel> list = userService.getUserList(enabled);
		if (list == null || list.isEmpty()){
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_USERS, "No user is found.");
		}else
			return ApiResponse.getSucceedResponse(list);
	}

	@RequestMapping(value = "/admins", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String getAdmins(Authentication authentication, @RequestParam(value = DbConst.FIELD_CAMPUS_UID, required = false) String campusUid, @RequestParam(value = DbConst.FIELD_ENABLED, required = false) Boolean enabled) {
		UserModel user = UserPrincipal.getUser(authentication);
		
		List<UserDetailedModel> list = userService.getUserList(campusUid, UserRole.ADMIN, enabled);
		if (list == null || list.isEmpty()){
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_USERS, "No user is found.");
		}else
			return ApiResponse.getSucceedResponse(list);
	}

	@RequestMapping(value = "/teachers", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String getTeachers(Authentication authentication, @RequestParam(value = DbConst.FIELD_ENABLED, required = false) Boolean enabled) {
		if (authentication == null)
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_UNKNOWN_COMMAND, "No logged user.");
		
		UserModel user = UserPrincipal.getUser(authentication);
		String campusUid = user.getCurCampus();
		List<UserDetailedModel> list = userService.getUserList(campusUid, UserRole.TEACHER, enabled);
		if (list == null || list.isEmpty()){
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_USERS, "No user is found.");
		}else
			return ApiResponse.getSucceedResponse(list);
	}


	@RequestMapping(value = "/nurses", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String getNurses(Authentication authentication,
			@RequestParam(value = DbConst.FIELD_TOKEN, required = false) String tokenUid,
			@RequestParam(value = DbConst.FIELD_ENABLED, required = false) Boolean enabled) {
		UserModel user = null;
		if (XcStringUtils.isValid(tokenUid)){
			TokenModel token = tokenService.get(tokenUid);
			user = userService.getUser(token.getUserUid());
		}else{
			user = UserPrincipal.getUser(authentication);
		}
		
		String campusUid  = user != null ? user.getCurCampus() : null;
		
		if (XcStringUtils.isNullOrEmpty(campusUid))
			return ApiResponse.getFailedResponse();
		
		List<UserDetailedModel> list = userService.getUserList(campusUid, UserRole.NURSE, enabled);
		if (list == null || list.isEmpty()){
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_USERS, "No nurse found.");
		}else
			return ApiResponse.getSucceedResponse(list);
	}

	@RequestMapping(value = "/nurse/{dept}", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String getNurse(Authentication authentication, @PathVariable String dept) {
		UserModel user = UserPrincipal.getUser(authentication);
		String campusUid = user.getCurCampus();
		UserModel nurse = userService.getNurseByDept(campusUid, UserRole.NURSE, dept);
		if (user == null){
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_USERS, "No nurse found.");
		}else
			return ApiResponse.getSucceedResponse(nurse);
	}

	/**
	 * Delete user with given user UID
	 * It'll delete all the accounts for the user.
	 * @param userUid
	 * @return
	 */
	@RequestMapping(value = "/user/{command}/{userUid}", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String userCommand(@PathVariable String command, @PathVariable String userUid) {
		UserModel user = userService.getUser(userUid);

		if (user == null)
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_PARAMS, "User UID not found.");
		
		switch(COMMAND.get(command)){
		case delete:
		case remove:
			userService.delete(user);
			break;
		case enable:
			userService.enable(user, true);
			break;
		case disable:
			userService.enable(user, false);
			break;
		default:
			break;
		}
		
		return ApiResponse.getSucceedResponse(user);
	}

	/**
	 * Get account list for current logged user
	 * @param authentication
	 * @param enabled
	 * @return
	 */
	@RequestMapping(value = "/user/accounts", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String userAccounts(Authentication authentication, @RequestParam(value = DbConst.FIELD_ENABLED, required = false) Boolean enabled) {
		if (authentication == null)
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_UNKNOWN_COMMAND, "No logged user.");
		
		UserModel user = UserPrincipal.getUser(authentication);
		
		List<AccountModel> list = userService.getAccountList(user.getUserUid(), enabled);
			
		if (list == null || list.isEmpty()){
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_USERS, "No account found.");
		}else
			return ApiResponse.getSucceedResponse(list);
		
	}

	@RequestMapping(value = {"/login", "/user/login"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String login(Locale locale, Model model, @RequestParam String id, @RequestParam String password, Principal principal) {
        AccountModel account = userService.getAccount(id);
        System.out.println("==================testuser===============");
		if (account == null){
			if (id.equalsIgnoreCase(PropertyConst.ADMIN) && password.equals(PropertyConst.ADMIN)){
				UserModel user = new UserModel();
				user.init();
				user.setName(PropertyConst.ADMINISTRATOR);
				user.addRole(UserRole.SUPERVISOR);
				userService.save(user);

				account = new AccountModel();
				account.init(user.getUserUid());
				account.setId(PropertyConst.ADMIN);
				account.setPassword(PropertyConst.ADMIN);
				account.setIdType(ID_TYPE.LOGIN_ID.name());
				account.encryptPassword(passwordEncoder);
				account.setActivatedAt(new Date());
				userService.save(account);
				
				TokenModel token = authorize(user);

				return ApiResponse.getTokenResponse(token);
			}else{
				return ApiResponse.getMessage(ApiResponse.CODE_FAILED_UNKNOWN_USER, "ID is not found.");
			}
		}else{
	        if (account.isAvailable()){
				if (passwordEncoder.matches(password, account.getPassword())){
					UserModel user = userService.getUser(account.getUserUid());
					TokenModel token = authorize(user);
					return ApiResponse.getTokenResponse(token);
				}else{
					return ApiResponse.getMessage(ApiResponse.CODE_FAILED_PASSWORD_NOT_MATCHES, "Password is not matched.");
				}
	        }else{
	        	return ApiResponse.getMessage(ApiResponse.CODE_FAILED_ACCOUNT_NOT_AVAILABLE, "Account is not enabled or activated.");
	        }
		}
	}

	@RequestMapping(value = {"/login", "/user/login"}, method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String login(Locale locale, Model model, @RequestParam String id, @RequestParam String password) {
		return login(locale, model, id, password, null);
	}
	
	@RequestMapping(value = {"/login/{token}", "/user/login/{token}"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String loginByToken(Locale locale, Model model, @PathVariable("token") String tokenUid) {
		TokenModel token = tokenService.get(tokenUid);
		if (token != null){
			if (token.isExpired(GeneralConst.DEFAULT_TOKEN_LIFE_CYCLE)){
				tokenService.delete(token);
				return ApiResponse.getMessage(ApiResponse.CODE_FAILED_EXPIRED_TOKEN, "Token is expired.");
			}else{
				UserModel user = userService.getUser(token.getUserUid());
				TokenModel newToken = authorize(user);
				
				return ApiResponse.getTokenResponse(newToken);
			}
		}else
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_TOKEN, "Token is not available.");		
	}
	
	@RequestMapping(value = {"/logout", "/user/logout"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String logout(Authentication authentication) {
		String token = (String)authentication.getCredentials();
		return logout(token);
	}

	@RequestMapping(value = "/logout/{token}", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String logout(@PathVariable("token") String tokenUid) {
		TokenModel token = tokenService.get(tokenUid);
		if (token != null){
			if (token.getTokenType() != TOKEN_TYPE.LOGGED_IN)
				return ApiResponse.getMessage(ApiResponse.CODE_FAILED_LOGOUT, "Token is not for logout.");
			
			tokenService.delete(token);
			
			if (token.isExpired(GeneralConst.DEFAULT_TOKEN_LIFE_CYCLE)){
				return ApiResponse.getMessage(ApiResponse.CODE_FAILED_EXPIRED_TOKEN, "Token is expired.");
			}else{
				SecurityContextHolder.getContext().setAuthentication(null);
				return ApiResponse.getMessage(ApiResponse.CODE_SUCCEED_LOGOUT, " User has been logged out.");
			}
		}else
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_TOKEN, "Token is not available.");
	}

	@RequestMapping(value="/profile/upload", method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody 
	public String upload(HttpServletRequest request, Authentication authentication, @RequestParam("file") MultipartFile file, @RequestParam(value="userUid", required=false) String userUid){
		UserModel user = null;
		if (XcStringUtils.isValid(userUid)){
			user = userService.getUser(userUid);
		}
		if (user == null  && authentication != null)
			user = UserPrincipal.getUser(authentication);
		
		if (user == null)
			return ApiResponse.getFailedResponse("User is not defined in.");
		
		String profilePath = userService.getProfilePath(user, file.getOriginalFilename());
		
		File targetFile = new File(profilePath);
		
		if (!targetFile.getParentFile().exists())
			targetFile.getParentFile().mkdirs();
		
		try {
			BufferedInputStream bis = new BufferedInputStream(file.getInputStream());
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(targetFile));
			byte[] buffer = new byte [1024*1024*8];
			int count = 0;
			while((count = bis.read(buffer))!=-1){
				bos.write(buffer, 0, count);
			}
			bos.flush();
			bos.close();
			bis.close();
			
			return ApiResponse.getSucceedResponse(user);
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		return null;
	}


	/**
	 * Authorize role of user
	 * @param user
	 * @return
	 */
	private TokenModel authorize(UserModel user){		
        TokenModel token = tokenService.get(user.getUid(), TOKEN_TYPE.LOGGED_IN);
        if ( token== null)
        	token = new TokenModel(user.getUid(), TOKEN_TYPE.LOGGED_IN);
        token.setRegisteredAt(new Date());
        tokenService.save(token);
        
		Authentication authentication = new UsernamePasswordAuthenticationToken(new UserPrincipal(user), token.getTokenUid(), user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return token;
	}	
}
