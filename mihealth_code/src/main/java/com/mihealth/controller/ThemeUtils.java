package com.mihealth.controller;

import java.util.Arrays;

import com.google.common.base.Joiner;
import com.mihealth.db.model.UserModel;
import com.ximpl.lib.constant.GeneralConst;

public class ThemeUtils {
	public static String getResource(UserModel user, String pathParams){
		return getResource(user, null, pathParams);
	}

	public static String getResource(UserModel user, String... pathParams){
		return Joiner.on('/').skipNulls().join(Arrays.asList(pathParams));
	}	
}
