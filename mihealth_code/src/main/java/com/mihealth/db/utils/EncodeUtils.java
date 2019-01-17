package com.mihealth.db.utils;

import java.util.UUID;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import com.ximpl.lib.util.XcStringUtils;
import com.ximpl.lib.util.XcUuidUtils;

public class EncodeUtils {
	private static final String tokenPassword = "MiHEALTH@MiTAC.com";
	private static final String tokenSalt = "171891893b5cef2f"; //KeyGenerators.string().generateKey();
	private static final TextEncryptor encryptor = Encryptors.queryableText(tokenPassword, tokenSalt);	

	public static String encrypt(byte[] source){
		return encrypt(XcStringUtils.toHexString(source));
	}
	
	public static String encrypt(UUID uuid){
		return encrypt(XcUuidUtils.toHexString(uuid));
	}
	
	public static String encrypt(String source){
		return encryptor.encrypt(source);
	}
	
	public static String decrypt(String source){
		try {
			return XcStringUtils.isValid(source)? encryptor.decrypt(source) : null;
		}catch(Exception ex) {
			return null;
		}
	}

	public static byte[] decryptBytes(String source){
		final String hexString = encryptor.decrypt(source);
		return XcStringUtils.toBytes(hexString);
	}
}
