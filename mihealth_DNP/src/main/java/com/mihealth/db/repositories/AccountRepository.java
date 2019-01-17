package com.mihealth.db.repositories;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.mihealth.db.model.AccountModel;

@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<AccountModel, String>{

	AccountModel findById(String loginId);

	int countById(String id);

	List<AccountModel> findByUserUidAndEnabled(String userUid, Boolean enabled);

	List<AccountModel> findByUserUid(String decryptBytes);
	
	@Modifying
	@Query(value = "UPDATE tb_account SET id = ?2 WHERE id = ?1", nativeQuery = true)
	void updateId(String id, String newId);

	@Modifying
	@Query(value = "UPDATE tb_account SET password = ?2 WHERE id = ?1", nativeQuery = true)
	void updatePassword(String id, String encryptedPassword);

	@Modifying
	@Query(value = "UPDATE tb_account SET enabled = ?2 WHERE userUid = UNHEX(?1)", nativeQuery = true)
	void enableByUserUid(String userUid, boolean enabled);
	
	@Modifying
	@Query(value = "UPDATE tb_account SET activatedAt = ?2 WHERE id = ?1", nativeQuery = true)
	void activateById(String id, Date date);
	
	@Modifying
	@Query(value = "DELETE FROM tb_account WHERE userUid = UNHEX(?1)", nativeQuery = true)
	void deleteByUserUid(String userUid);
	
	@Modifying
	@Query(value = "DELETE FROM tb_account WHERE id = ?1", nativeQuery = true)
	void deleteById(String id);
	

}
