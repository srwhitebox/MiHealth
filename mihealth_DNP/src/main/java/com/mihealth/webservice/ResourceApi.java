package com.mihealth.webservice;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mihealth.db.model.ResourceModel;
import com.mihealth.db.service.ResourceService;
import com.ximpl.lib.constant.DbConst;
import com.ximpl.lib.constant.GeneralConst;
import com.ximpl.lib.util.XcStringUtils;

@Controller
@RequestMapping(value = "/api")
public class ResourceApi {
	@Autowired
	private ResourceService resourceService;
	
	@RequestMapping(value = {"resource/save/{category}/{code}", "resource/add/{category}/{code}", "/update/{category}/{code}"}, method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String add(@PathVariable String category, @PathVariable String code, @RequestParam String language, @RequestParam String value, 
			@RequestParam(required = false) String comment) {
		
		Locale locale = Locale.forLanguageTag(language);
		if (locale == null){
			return ApiResponse.getFailedResponse("The language is not valid.");
		}
		
		if (XcStringUtils.isNullOrEmpty(category))
			category = GeneralConst.CATEGORY_APP;
		
		ResourceModel resource = resourceService.get(category, code);
		
		if (resource == null){	// No record
			resource = new ResourceModel(category, code);
		}
		
		resource.setProperty(language, value);

		if (comment != null){
			resource.setComment(comment);
		}
	
		resourceService.save(resource);
		
		return ApiResponse.getSucceedResponse(resource);
	}
	
	@RequestMapping(value = {"resource/{language}/{category}/{code}"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String get(@PathVariable String language, @PathVariable String category, @PathVariable String code){
		final String resource = resourceService.get(language, category, code);
		if (XcStringUtils.isNullOrEmpty(resource)){
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NOT_FOUND, "Resource is not defined for language or category and code.");
		}else{
			return ApiResponse.getSucceedResponse(resource);
		}
	}

	@RequestMapping(value = {"resources"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String getAll(@RequestParam(required=false) String orderBy, @RequestParam(required=false) String category, @RequestParam(required=false) String filter, @RequestParam(required=false) Integer count, @RequestParam(required=false) Integer page){
		List<ResourceModel> resources = resourceService.getAll(category, filter, orderBy);
		if (resources == null || resources.isEmpty()){
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_RECORD, "No resources found");
		}else{
			return ApiResponse.getSucceedResponse(resources);
		}
	}

	@RequestMapping(value = {"resources/{category}"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String getAllByCategory(@PathVariable String category){
		List<ResourceModel> resources = resourceService.getAll(category);
		if (resources == null || resources.isEmpty()){
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_RECORD, "No resources found");
		}else{
			return ApiResponse.getSucceedResponse(resources);
		}
	}
	
	@RequestMapping(value = {"resource/delete/{category}/{code}"}, method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String delete(@PathVariable String category, @PathVariable String code){
		resourceService.delete(category, code);
		return ApiResponse.getSucceedResponse("Resource has been removed.");
	}

}
