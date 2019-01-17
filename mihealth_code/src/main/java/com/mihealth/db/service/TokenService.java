package com.mihealth.db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mihealth.db.model.TokenModel;
import com.mihealth.db.repositories.TokenRepository;
import com.mihealth.db.utils.EncodeUtils;
import com.ximpl.lib.type.TOKEN_TYPE;

@Service
public class TokenService {
	@Autowired
	private TokenRepository tokenRepository;
	
	public void save(TokenModel token) {
		
		tokenRepository.saveAndFlush(token);
	}

	public TokenModel get(String tokenUid) {
		
		return tokenRepository.findOne(EncodeUtils.decrypt(tokenUid));
	}
	
	public TokenModel get(String userUid, TOKEN_TYPE tokenType){
		return tokenRepository.findByUserUidAndTokenType(EncodeUtils.decrypt(userUid), tokenType.name());
	}

	public void delete(TokenModel token) {
		tokenRepository.delete(token);
	}
	
	public void deleteByTokenUid(String tokenUid){
		tokenRepository.deleteByTokenUid(EncodeUtils.decrypt(tokenUid));
	}

	public void deleteByUserUid(String userUid){
		tokenRepository.deleteByUserUid(EncodeUtils.decrypt(userUid));
	}	
}
