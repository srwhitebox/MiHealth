package com.mihealth.webservice;

import java.security.Principal;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mihealth.db.model.PropertyModel;
import com.mihealth.db.service.PropertyService;
import com.ximpl.lib.constant.DbConst;
import com.ximpl.lib.util.XcStringUtils;

@Controller
@RequestMapping(value = "/api")
public class PropertyApi {
	
	@Autowired
	private PropertyService propertyService;
	
	@RequestMapping(value = {"property/add/{category}/{code}", "property/save/{category}/{code}", "property/update/{category}/{code}"}, method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String add(HttpServletRequest request, @PathVariable("category") String category, @PathVariable("code") String code, 
			@RequestParam String language, @RequestParam String value, @RequestParam(required = false) String comment, @RequestParam(required = false) Integer displayOrder, @RequestParam(required = false) Boolean enabled, Principal principal) {
		
		Locale locale = Locale.forLanguageTag(language);
		if (locale == null){
			return ApiResponse.getFailedResponse("The language is not valid language tag.");
		}
		
		if (XcStringUtils.isNullOrEmpty(category))
			return ApiResponse.getFailedResponse("The category is not valid language tag.");

		PropertyModel propertyModel = propertyService.get(category, code);
		
		if (propertyModel == null){
			if (enabled == null)
				enabled = true;
			propertyModel = new PropertyModel(category, code, language, value, comment, displayOrder, enabled);
			propertyService.save(propertyModel);
		}else{
			propertyModel.setProperty(language, value);
			propertyModel.setComment(comment);
			if (displayOrder != null && propertyModel.getDisplayOrder() != displayOrder)
				propertyModel.setDisplayOrder(displayOrder);
			if (enabled != null && propertyModel.getEnabled() != enabled)
				propertyModel.setEnabled(enabled);
			propertyService.save(propertyModel);
		}
		
		return ApiResponse.getSucceedResponse(propertyModel);
	}

	/**
	 * Get message content
	 * If the cateogry is not defined, the default category shall be "app".
	 * @param category
	 * @param code
	 * @return
	 */
	@RequestMapping(value = "property/{category}/{code}", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String get(@PathVariable String category, @PathVariable String code) {
		final PropertyModel property = propertyService.get(category, code);		
		
		return property == null ? ApiResponse.getFailedResponse("The '"+ code + "' is not found in '" + category + "'.") : ApiResponse.getSucceedResponse(property);
	}

	/**
	 * List up all the message in category.
	 * If the category is not defined, it'll return the messages in "app" category.
	 * @param category
	 * @return
	 */
	@RequestMapping(value = "properties/{category}", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String propertiesByCategory(@PathVariable String category, @RequestParam(value = DbConst.FIELD_ENABLED, required = false) Boolean enabled) {
		if (category.toLowerCase().equals("all"))
			return properties(null, null, enabled, null, null);
		
		final List<PropertyModel> list = propertyService.getList(category, enabled);
		
		return ApiResponse.getSucceedResponse(list);
	}
	
	@RequestMapping(value = "properties", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String properties(
			@RequestParam(required=false) String orderBy, 
			@RequestParam(required=false) String filter,
			@RequestParam(required=false) Boolean enabled,
			@RequestParam(required=false) Integer count, @RequestParam(required=false) Integer page 
			) {
		final List<PropertyModel> list = propertyService.getList(filter, orderBy, enabled, count, page);
		
		return ApiResponse.getSucceedResponse(list);
	}
	

	/**
	 * Remove message
	 * If the cateogry is not defined, the default category shall be "app".
	 * @param request
	 * @param category
	 * @param code
	 * @param principal
	 * @return
	 */
	@RequestMapping(value = "property/delete/{category}/{code}", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String delete(HttpServletRequest request, @PathVariable String category, @PathVariable  String code, Principal principal) {
		if (XcStringUtils.isNullOrEmpty(category))
			return ApiResponse.getFailedResponse("The message resource category is not found.");
		
		final PropertyModel property = propertyService.get(category, code);
		if (property == null)
			return ApiResponse.getFailedResponse("The message resource is not found.'");
		
		propertyService.delete(property);
		
		return ApiResponse.getSucceedResponse(property);
	}

}
