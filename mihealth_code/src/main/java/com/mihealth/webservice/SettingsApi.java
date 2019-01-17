package com.mihealth.webservice;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonElement;
import com.mihealth.db.model.SettingsModel;
import com.mihealth.db.service.SettingsService;
import com.ximpl.lib.util.XcJsonUtils;
import com.ximpl.lib.util.XcStringUtils;

@Controller
@RequestMapping(value = "/api/settings")
public class SettingsApi {
	@Autowired
	SettingsService settingsService;
	
	/**
	 * Add settings for an item
	 * @param item
	 * @param params
	 * @return
	 */
	@RequestMapping(value = {"add/{item}"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String add(@PathVariable String item, @RequestParam Map<String, String> params) {
		SettingsModel settings = settingsService.getSetting(item);
		if (settings == null)
			settings = new SettingsModel(item);

		settings.addSetting(params);
		
		settingsService.save(settings);
		
		return ApiResponse.getSucceedResponse(settings);
	}

	@RequestMapping(value = {"save/{item}"}, method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String save(HttpServletRequest request, Authentication authentication, @PathVariable String item) {
		final JsonElement jSettings = XcJsonUtils.toJsonElement(request);
		if (jSettings == null){
			return ApiResponse.getFailedResponse();
		}
		
		final SettingsModel settings = new SettingsModel(item, jSettings.getAsJsonObject());
		settingsService.save(settings);
		
		return ApiResponse.getSucceedResponse(settings);
	}

	
	/**
	 * Get all setting items
	 * @return
	 */
	@RequestMapping(value = {""}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String getAll() {
		List<SettingsModel> settingsList = settingsService.getAll();
		if (settingsList == null || settingsList.isEmpty())
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_PARAMS, "No settings found");
		
		return ApiResponse.getSucceedResponse(settingsList);
	}

	@RequestMapping(value = {"{item}"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String getSettings(@PathVariable String item) {
		final SettingsModel settings = settingsService.getSetting(item);
		if (settings == null)
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_PARAMS, "Item settings is not defined");
		else
			return ApiResponse.getSucceedResponse(settings);
	}

	/**
	 * Get value for a key of item settings
	 * @param item
	 * @param key
	 * @return
	 */
	@RequestMapping(value = {"{item}/{key}"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String getValue(@PathVariable String item, @PathVariable String key) {
		final String value = settingsService.getValue(item, key);
		if (XcStringUtils.isNullOrEmpty(value))
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_PARAMS, "Item settings is not defined");
		else
			return ApiResponse.getSucceedResponse(value);
	}

	@RequestMapping(value = {"delete/{item}"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String deleteItem(@PathVariable String item) {
		settingsService.delete(item);
		return ApiResponse.getSucceedResponse("The settings item has been removed.");
	}

	@RequestMapping(value = {"delete/{item}/{key}"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String getKey(@PathVariable String item, @PathVariable String key) {
		settingsService.delete(item, key);
		return ApiResponse.getSucceedResponse("The key of settings item has been removed.");
	}
}
