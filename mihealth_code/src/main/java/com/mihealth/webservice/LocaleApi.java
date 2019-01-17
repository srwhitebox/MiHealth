package com.mihealth.webservice;

import java.security.Principal;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mihealth.db.model.LocaleModel;
import com.mihealth.db.model.UserModel;
import com.mihealth.db.model.AltLocaleModel;
import com.mihealth.db.service.LocaleService;
import com.mihealth.db.service.UserService;
import com.mihealth.security.UserPrincipal;
import com.ximpl.lib.constant.GeneralConst;
import com.ximpl.lib.util.XcStringUtils;

@Controller
@RequestMapping(value = "/api")
public class LocaleApi {
	
	@Autowired
	private LocaleService localeService;
	
	@Autowired
	private UserService userService;
	
	@RequestMapping(value = {"locale/add", "locale/save"}, method = RequestMethod.POST, produces = "text/plain;charset=UTF-8", headers = "Accept=application/json")
	@ResponseBody public String add(HttpServletRequest request, @RequestBody LocaleModel locale, Principal principal) {
		
		localeService.save(locale);
		
		return ApiResponse.getSucceedResponse(locale);
	}

	@RequestMapping(value = "locale", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String locale(Authentication authentication, Locale locale, @RequestParam(value = "language", required = false) String localeTag) {
		if (authentication != null && XcStringUtils.isNullOrEmpty(localeTag)){
			Object principal = authentication.getPrincipal();
			if (principal instanceof UserModel){
				UserModel user = (UserModel)principal;
				localeTag = user.getSettingAsString(GeneralConst.KEY_LANGUAGE);
			}
		}
		
		if (XcStringUtils.isNullOrEmpty(localeTag)){
			localeTag = locale.toLanguageTag();;
		}
		
		LocaleModel localeModel = localeService.get(localeTag);
		
		if (localeModel == null)
			return ApiResponse.getFailedResponse("The '"+localeTag + "' is not valid language tag.");

		return ApiResponse.getSucceedResponse(localeModel);
	}
	
	@RequestMapping(value = "/locale/change", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String updateUserSetting(Authentication authentication,@RequestParam(value = "language", required = false) String localeTag) {
		if (authentication == null)
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_UNKNOWN_COMMAND, "No logged user.");
		
		UserModel user = UserPrincipal.getUser(authentication);
		user.setSetting(GeneralConst.KEY_LANGUAGE, localeTag);
		userService.updateSettings(user, GeneralConst.KEY_LANGUAGE, localeTag);
		
		return ApiResponse.getSucceedResponse();
	}
	
		

	@RequestMapping(value = {"/locales", "locale/list"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String locales() {
		final List<LocaleModel> locales = localeService.getList(null);
		
		return ApiResponse.getSucceedResponse(locales);
	}	

	@RequestMapping(value = "locale/delete", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8", headers = "Accept=application/json")
	@ResponseBody public String delete(HttpServletRequest request, @RequestBody LocaleModel locale, Principal principal) {
		localeService.delete(locale);
		
		return ApiResponse.getSucceedResponse("Locale has been removed");
	}
	
	
	@RequestMapping(value = {"altLocale/add", "altLocale/save"}, method = RequestMethod.POST, produces = "text/plain;charset=UTF-8", headers = "Accept=application/json")
	@ResponseBody public String add(HttpServletRequest request, @RequestBody AltLocaleModel locale, Principal principal) {
		
		localeService.save(locale);
		
		return ApiResponse.getSucceedResponse(locale);
	}

	@RequestMapping(value = {"/altLocales", "altLocale/list"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String altLocales() {
		final List<AltLocaleModel> locales = localeService.getAltLocales();
		
		return ApiResponse.getSucceedResponse(locales);
	}	


	@RequestMapping(value = "altLocale/delete", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8", headers = "Accept=application/json")
	@ResponseBody public String delete(HttpServletRequest request, @RequestBody AltLocaleModel locale, Principal principal) {
		localeService.delete(locale);
		
		return ApiResponse.getSucceedResponse("Locale has been removed");
	}

}
