package com.mihealth.app;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.mihealth.db.model.CampusModel;
import com.mihealth.db.model.UserModel;
import com.mihealth.db.service.CampusService;
import com.mihealth.security.UserPrincipal;
import com.ximpl.lib.util.XcJsonUtils;

@ControllerAdvice
public final class GlobalController {
	@Value("#{projectProperties['build.version']}")
	private String buildVersion;

	@Value("#{projectProperties['build.time']}")
	private String buildTime;
	
	@Autowired
	CampusService campusService;
	
	@ModelAttribute("buildVersion")
    public String buildVersion() {
	   return buildVersion;
    }

	@ModelAttribute("buildTime")
    public String buildTime() {
	   return buildTime;
    }

	@ModelAttribute("user")
    public Object user(Principal principal, Authentication authentication) {
		
        return authentication == null ? guest() : UserPrincipal.getUser(authentication);
    }
	
	@ModelAttribute("visionGradeUnit")
    public Object visionGradeUnit(Principal principal, Authentication authentication) {
		final String defaultUnit = "decimal";
		if (authentication == null)
			return defaultUnit;
		CampusModel campus = campusService.getCampusByCampusUid(UserPrincipal.getUser(authentication).getCurCampus());
		return campus == null ? "decimal" : XcJsonUtils.getString(campus.getSettings(), "visionGradeUnit");
    }

	private UserModel guest(){
		final UserModel user = new UserModel();
		user.setName("Guest");
		return user;
	}
}
