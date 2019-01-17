package com.mihealth.controller.view;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ximpl.lib.util.XcStringUtils;

/**
 * Handles requests for the application home page.
 */
@Controller
public class FragmentController extends BaseViewController{
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = {"/views/{viewName}"}, method = RequestMethod.GET)
	public String view(HttpServletRequest request, HttpServletResponse response, Authentication authentication, Locale locale, Model model, @PathVariable String viewName) {
        super.initLocale(request, response, authentication, locale);

		return "template/"+viewName+".html";
	}
	
	@RequestMapping(value = {"/views/{folder}/{viewName}"}, method = RequestMethod.GET)
	public String view(HttpServletRequest request, HttpServletResponse response, Authentication authentication, Locale locale, Model model, @PathVariable String folder, @PathVariable String viewName) {
        super.initLocale(request, response, authentication, locale);

		return "template/"+folder+"/"+viewName+".html";
	}

	@RequestMapping(value = {"/views/system/resource"}, method = RequestMethod.GET)
	public String resourceView(HttpServletRequest request, HttpServletResponse response, Authentication authentication, Locale locale, Model model, @RequestParam String category) {
        super.initLocale(request, response, authentication, locale);
        model.addAttribute("category",  category);
		return "template/system/resource.html";
	}

	@RequestMapping(value = {"/fragment/{fragmentName}"}, method = RequestMethod.GET)
	public String fragment(HttpServletRequest request, HttpServletResponse response, Authentication authentication, Locale locale, Model model, @PathVariable String fragmentName) {
        super.initLocale(request, response, authentication, locale);

		return "fragment/"+fragmentName+".html :: fragment";
	}
	
	@RequestMapping(value = {"/fragment/{fragmentName}/{blockName}"}, method = RequestMethod.GET)
	public String fragment(HttpServletRequest request, HttpServletResponse response, Authentication authentication, Locale locale, Model model, @PathVariable String fragmentName, @PathVariable String blockName) {
        super.initLocale(request, response, authentication, locale);

		return "fragment/"+fragmentName+".html :: "+ blockName;
	}
	
	@RequestMapping(value = {"/dialog/{dialogName}"}, method = RequestMethod.GET)
	public String dialog(HttpServletRequest request, HttpServletResponse response, Authentication authentication, Locale locale, Model model, @PathVariable String dialogName, @RequestParam(required=false) String category) {
        super.initLocale(request, response, authentication, locale);
        
        if (XcStringUtils.isValid(category))
        	model.addAttribute("category",  category);
		
        return "template/dialog/"+dialogName+".html :: dialog";
	}
	


}
