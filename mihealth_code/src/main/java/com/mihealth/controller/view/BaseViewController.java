package com.mihealth.controller.view;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.LocaleResolver;

import com.mihealth.db.model.UserModel;
import com.mihealth.db.service.UserService;
import com.mihealth.security.UserPrincipal;
import com.ximpl.lib.constant.GeneralConst;
import com.ximpl.lib.util.XcStringUtils;

/**
 * Handles requests for the application home page.
 */
public class BaseViewController {
	@Autowired
	protected LocaleResolver localeResolver;
	
	@Autowired
	protected UserService userService;
	
	protected UserModel userModel;
	
	protected void initLocale(HttpServletRequest request, HttpServletResponse response, Authentication authentication, Locale locale){
		String localeTag = null;
		UserModel user = UserPrincipal.getUser(authentication);
		if (user != null)
			localeTag = user.getSettingAsString(GeneralConst.KEY_LANGUAGE);
		
		// If not logged user or no locale settings for user
		if (XcStringUtils.isNullOrEmpty(localeTag)){
			localeTag = locale.toLanguageTag();;
			if (user != null){
				user.setSetting(GeneralConst.KEY_LANGUAGE, localeTag);
				userService.save(user);
			}
		}else
			locale = Locale.forLanguageTag(localeTag);

		this.localeResolver.setLocale(request, response, locale);
	}
}
