package com.shopping.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.shopping.Repository.UserRepo;
import com.shopping.model.UserDtls;

@Service
public class UserDetailsServiceImpl implements UserDetailsService{

	@Autowired
	private UserRepo userRepo;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserDtls userDtls = userRepo.findByEmail(username);
		
		if (userDtls==null) {
			throw new UsernameNotFoundException("user not found");
		}
		return new CustomUser(userDtls);
	}

}
