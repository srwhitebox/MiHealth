package com.mihealth.controller.resource;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.mihealth.controller.ThemeUtils;

@Controller
@RequestMapping(value = "css/")
public class CssController {
	private static final String cssFolderName = "css";
	
	@RequestMapping(value = "{cssName}", method = RequestMethod.GET)
    public String common(Model model, HttpServletResponse response, @PathVariable("cssName") String cssName) {
        return ThemeUtils.getResource(null, cssFolderName, cssName);
    }	
}
