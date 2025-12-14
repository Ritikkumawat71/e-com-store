package com.shopping.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopping.model.UserDtls;

public interface UserRepo extends JpaRepository<UserDtls, Integer> {

	public UserDtls findByEmail(String email);

	public List<UserDtls> findByRole(String role);
	
	public UserDtls findByResetToken(String token);
}
