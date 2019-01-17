package com.mihealth.app;

import java.text.MessageFormat;
import java.util.Locale;

import org.springframework.context.support.AbstractMessageSource;

import com.mihealth.db.model.ResourceModel;
import com.mihealth.db.model.AltLocaleModel;
import com.mihealth.db.repositories.ResourceRepository;
import com.mihealth.db.repositories.AltLocaleRepository;
import com.ximpl.lib.constant.GeneralConst;
import com.ximpl.lib.util.XcStringUtils;

public class ResourceMessageSource extends AbstractMessageSource{
	private ResourceRepository resourceRepository;
	private AltLocaleRepository altLocaleRepository;
	
	private final String defaultLanguage = Locale.US.toLanguageTag();
	
	@Override
	protected MessageFormat resolveCode(String code, Locale locale) {
		final String tag = getLanguageCode(locale);
		
		String message = resourceRepository.getResource(tag, GeneralConst.CATEGORY_APP, code);
		if (XcStringUtils.isNullOrEmpty(message))
			message = ResourceModel.getDefaultMessage(code);
				
		return new MessageFormat(message, locale);
	}
	
	public void setResourceRepository(ResourceRepository resourceRepository){
		this.resourceRepository = resourceRepository;
	}
	
	public void setAltLocaleRepository(AltLocaleRepository altLocaleRepository){
		this.altLocaleRepository = altLocaleRepository;
	}
	
	private String getLanguageCode(Locale locale){
		final AltLocaleModel localeModel = altLocaleRepository.findOneByLocaleTag(locale.toLanguageTag());
		return localeModel == null ? defaultLanguage : localeModel.getAltLocaleTag();
	}
}
