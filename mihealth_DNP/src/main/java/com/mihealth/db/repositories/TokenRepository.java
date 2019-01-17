package com.mihealth.db.repositories;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mihealth.db.model.TokenModel;

public interface TokenRepository extends JpaRepository<TokenModel, Date>{
	@Query(value = "SELECT tokenUid, userUid, tokenType, registeredAt FROM tb_token WHERE tokenUid = UNHEX(?1)", nativeQuery = true)
	TokenModel findOne(String tokenUid);

	@Query(value = "SELECT tokenUid, userUid, tokenType, registeredAt FROM tb_token WHERE userUid = UNHEX(?1) AND tokenType = (?2)", nativeQuery = true)
	TokenModel findByUserUidAndTokenType(String userUid, String tokenType);	

	@Query(value = "DELETE FROM tb_token WHERE tokenUid = UNHEX(?1)", nativeQuery = true)
	void deleteByTokenUid(String tokenUid);	

	@Query(value = "DELETE FROM tb_token WHERE userUid = UNHEX(?1)", nativeQuery = true)
	void deleteByUserUid(String userUid);

}
