package com.mihealth.controller.resource;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Joiner;
import com.google.common.io.Files;

import com.mihealth.db.model.UserModel;
import com.mihealth.db.service.CampusService;
import com.mihealth.db.service.UserService;
import com.mihealth.security.UserPrincipal;
import com.ximpl.lib.barcode.QrCode;
import com.ximpl.lib.io.XcResource;

@Controller
public class ImageController {
	@Autowired
	private ServletContext servletContext;
	
	@Autowired
	UserService userService;

	@Autowired
	CampusService campusService;
	
	@RequestMapping(value = "/profile", method = RequestMethod.GET)
    public void profile(HttpServletResponse response, Authentication authentication) {
		if (authentication != null){
			final UserModel user = UserPrincipal.getUser(authentication);
			if (user == null)
				return;
			final String profilePath = userService.getProfilePath(user);
			if (profilePath != null){
				XcResource.copyFileToResponse(servletContext, response, profilePath);
				return;
			}
		}
		XcResource.copyResourceToResponse(servletContext, response, "/WEB-INF/image/profile/no_profile.png");
    }

	@RequestMapping(value = "/profile/{userUid}", method = RequestMethod.GET)
    public void profile(HttpServletResponse response, Authentication authentication, @PathVariable String userUid) {
		if (authentication != null && !userUid.equals("unknown")){
			final UserModel user = userService.getUser(userUid);
			final String profilePath = userService.getProfilePath(user);
			if (profilePath != null){
				XcResource.copyFileToResponse(servletContext, response, profilePath);
				return;
			}
		}
		XcResource.copyResourceToResponse(servletContext, response, "/WEB-INF/image/profile/no_profile.png");
    }

	@RequestMapping(value = "/profile/campus/{campusId}/{itemType}", method = RequestMethod.GET)
    public void profile(HttpServletResponse response, Authentication authentication, @PathVariable String campusId, @PathVariable String itemType) {
		if (authentication != null){
			final String profilePath = campusService.getProfilePath(campusId, itemType);
			if (profilePath != null){
				XcResource.copyFileToResponse(servletContext, response, profilePath);
				return;
			}
		}
		XcResource.copyResourceToResponse(servletContext, response, "/WEB-INF/image/profile/no_profile.png");
    }

	@RequestMapping(value = "/favicon", method = RequestMethod.GET)
    public void favicon(HttpServletResponse response) {
		XcResource.copyResourceToResponse(servletContext, response, "/WEB-INF/image/favicon.png");
    }
	
	@RequestMapping(value = "/barcode/qrcode/{code}", method = RequestMethod.GET)
    public void qrCode(HttpServletResponse response, @PathVariable String code, @RequestParam(required=false) Integer size, @RequestParam(required=false) Integer margin) {
		XcResource.copyToResponse(response, QrCode.toImage(code, size, margin));
	}
	
}
