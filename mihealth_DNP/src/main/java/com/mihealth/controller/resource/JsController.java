package com.mihealth.controller.resource;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Joiner;
import com.mihealth.controller.ThemeUtils;
import com.mihealth.db.model.UserModel;
import com.mihealth.security.UserPrincipal;

@Controller
@RequestMapping(value = "/js")
public class JsController {
	private static final String jsFolderName = "js";
	
	@RequestMapping(value = "{jsName}", method = RequestMethod.GET)
    public String common(Authentication authentication, Model model, @PathVariable String jsName) {
		model.addAttribute("user", UserPrincipal.getUser(authentication));
		return ThemeUtils.getResource(null, jsFolderName, jsName);
    }
	
	@RequestMapping(value = "{folderName}/{jsName}", method = RequestMethod.GET)
    public String subModule(Authentication authentication, Model model, @PathVariable String folderName, @PathVariable String jsName) {
		model.addAttribute("user", UserPrincipal.getUser(authentication));
		return ThemeUtils.getResource(null, jsFolderName, folderName, jsName);
    }
	
	@RequestMapping(value = "{folderName}/{moduleName}/{jsName}", method = RequestMethod.GET)
    public String subModule(Authentication authentication, Model model, @PathVariable String folderName, @PathVariable String moduleName, @PathVariable String jsName) {
		model.addAttribute("user", UserPrincipal.getUser(authentication));
		return ThemeUtils.getResource(null, jsFolderName, Joiner.on("/").join(folderName, moduleName), jsName);
    }

	@RequestMapping(value = "{folderName}/account.js", method = RequestMethod.GET)
    public String account(Authentication authentication, HttpServletRequest request, Model model, @PathVariable String folderName, @RequestParam String url, @RequestParam String template) {
		model.addAttribute("user", UserPrincipal.getUser(authentication));
		model.addAttribute("accountsUrl", request.getContextPath()+url);
		model.addAttribute("template", request.getContextPath()+template);
		return ThemeUtils.getResource(null, jsFolderName, folderName, "account.js");
    }
}
