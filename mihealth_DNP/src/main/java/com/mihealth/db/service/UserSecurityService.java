package com.mihealth.db.service;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;

import com.mihealth.db.model.AccountModel;
import com.mihealth.db.repositories.AccountRepository;

public class UserSecurityService implements UserDetailsService{
	
	private AccountRepository accountRepository;

	public void setAccountRepository(AccountRepository accountRepository){
		this.accountRepository = accountRepository;
	}
	
	@Override
	public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
		AccountModel user = accountRepository.findById(loginId);
		String password = null;
		if (user == null){
			if (loginId.equalsIgnoreCase("admin")){
				password = BCrypt.hashpw("admin", BCrypt.gensalt());
			}else{
				throw new UsernameNotFoundException(loginId + " is not available.");
			}
		}else{
			password = user.getPassword();
		}
		
		Collection<SimpleGrantedAuthority> roles = new ArrayList<SimpleGrantedAuthority>(); 
		roles.add(new SimpleGrantedAuthority("ROLE_USER"));
		
		UserDetails userDetails = new User(loginId, password, roles);

		return userDetails;
	}

}
