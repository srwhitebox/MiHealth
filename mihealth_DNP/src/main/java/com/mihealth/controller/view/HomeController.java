package com.mihealth.controller.view;

import java.security.Principal;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ximpl.lib.util.XcStringUtils;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController extends BaseViewController{
	@RequestMapping(value = {"/"}, method = RequestMethod.GET)
	public String home(HttpServletRequest request, HttpServletResponse response, Authentication authentication, Locale locale, Model model, Principal principal, @RequestParam(required=false) String language) {
		// If it defines language, it'll shows the message as localized. If not, it'll use the default language from the current locale or user settings.
		if (XcStringUtils.isValid(language)){
			locale = Locale.forLanguageTag(language);
		}
		
        super.initLocale(request, response, authentication, locale);
        
        if (principal == null)
        	return "login.html";
        
       	return "index.html";
	}
}
