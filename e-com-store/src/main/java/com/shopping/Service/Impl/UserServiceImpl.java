package com.shopping.Service.Impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.shopping.Repository.UserRepo;
import com.shopping.Service.UserService;
import com.shopping.model.UserDtls;
import com.shopping.utils.AppConstant;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public UserDtls saveUser(UserDtls user) {
		// TODO Auto-generated method stub
		user.setRole("ROLE_USER");
		user.setIsEnable(true);
		user.setAccountNonLocked(true);
		user.setFailedAttempt(0);
		String encodePassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodePassword);
		UserDtls saveUser = userRepo.save(user);
		return saveUser;
	}

	@Override
	public UserDtls getUserByEmail(String email) {
		// TODO Auto-generated method stub
		return userRepo.findByEmail(email);
	}

	@Override
	public List<UserDtls> getAllUsers(String role) {
		// TODO Auto-generated method stub
		return userRepo.findByRole(role);

	}

	@Override
	public Boolean updateAccountStatus(Integer id, Boolean status) {
		// TODO Auto-generated method stub
		Optional<UserDtls> findByuser = userRepo.findById(id);

		if (findByuser.isPresent()) {
			UserDtls userDtls = findByuser.get();
			userDtls.setIsEnable(status);
			userRepo.save(userDtls);
			return true;
		}
		return false;
	}

	@Override
	public void increaseFailedAttempt(UserDtls user) {
		// TODO Auto-generated method stub
		int attempt = user.getFailedAttempt() + 1;
		user.setFailedAttempt(attempt);
		userRepo.save(user);

	}

	@Override
	public void userAccountLock(UserDtls user) {
		// TODO Auto-generated method stub
		user.setAccountNonLocked(false);
		user.setLockTime(new Date());
		userRepo.save(user);
	}

	@Override
	public Boolean unlockAccountTimeExpired(UserDtls user) {
		// TODO Auto-generated method stub
		long lockTime = user.getLockTime().getTime();
		long unlockTime = lockTime + AppConstant.UNLOCK_DURATION_TIME;
		long currentTime = System.currentTimeMillis();

		if (unlockTime < currentTime) {
			user.setAccountNonLocked(true);
			user.setFailedAttempt(0);
			user.setLockTime(null);
			userRepo.save(user);
			return true;
		}
		return false;
	}

	@Override
	public void resetAttempt(int userId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateUserResetToken(String email, String resetToken) {
		// TODO Auto-generated method stub
		UserDtls findByEmail = userRepo.findByEmail(email);
		findByEmail.setResetToken(resetToken);
		userRepo.save(findByEmail);
	}

	@Override
	public UserDtls getUserByToken(String token) {
		// TODO Auto-generated method stub
		return userRepo.findByResetToken(token);

	}

	@Override
	public UserDtls updateUser(UserDtls user) {
		// TODO Auto-generated method stub
		return userRepo.save(user);
	}

	@Override
	public UserDtls updateUserProfile(UserDtls user, MultipartFile img) {
		// TODO Auto-generated method stub
		UserDtls dbUser = userRepo.findById(user.getId()).get();

		if (!img.isEmpty()) {
			dbUser.setProfileImage(img.getOriginalFilename());
		}

		if (!ObjectUtils.isEmpty(dbUser)) {
			dbUser.setName(user.getName());
			dbUser.setMobileNumber(user.getMobileNumber());
			dbUser.setAddress(user.getAddress());
			dbUser.setCity(user.getCity());
			dbUser.setState(user.getState());
			dbUser.setPincode(user.getPincode());

			dbUser = userRepo.save(dbUser);
		}

		
		
		try {
			if (!img.isEmpty()) {
				File savefile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(savefile.getAbsolutePath() + File.separator + "profile_img" + File.separator
						+ img.getOriginalFilename());

				System.out.println(path);
				Files.copy(img.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return dbUser;
	}
	
	@Override
	public UserDtls saveAdmin(UserDtls user) {
		// TODO Auto-generated method stub
		user.setRole("ROLE_ADMIN");
		user.setIsEnable(true);
		user.setAccountNonLocked(true);
		user.setFailedAttempt(0);
		String encodePassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodePassword);
		UserDtls saveUser = userRepo.save(user);
		return saveUser;
	}

}
