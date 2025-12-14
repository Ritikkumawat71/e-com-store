package com.shopping.config;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.shopping.Repository.UserRepo;
import com.shopping.Service.UserService;
import com.shopping.model.UserDtls;
import com.shopping.utils.AppConstant;
import com.shopping.utils.SHCCustomExceptionLogger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthFailureHandlerImpl extends SimpleUrlAuthenticationFailureHandler{
	Logger logger = LogManager.getLogger(AuthFailureHandlerImpl.class);

	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private UserService userService;
	
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		try {
			String email = request.getParameter("username");
			UserDtls userDtls = userRepo.findByEmail(email);

			if (userDtls == null) {
				exception = new LockedException("Email & password id invalid");
			}
			
			if (userDtls.getIsEnable()) {
				if (userDtls.getAccountNonLocked()) {
					if (userDtls.getFailedAttempt() < AppConstant.ATTEMPT_TIME) {
						userService.increaseFailedAttempt(userDtls);
					} else {
						userService.userAccountLock(userDtls);
						exception = new LockedException("Your account is locked !! failed attempt 3");
					}
				} else {
					if (userService.unlockAccountTimeExpired(userDtls)) {
						exception = new LockedException("Your account is unlocked !! Please try to login");
					} else {
						exception = new LockedException("Your Account is Locked !! Please try again after sometimes");
					}
				}
			} else {
				exception = new LockedException("Your Account is Inactive");
			}

			super.setDefaultFailureUrl("/signin?error");
			super.onAuthenticationFailure(request, response, exception);
		} catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
	}

	
}
