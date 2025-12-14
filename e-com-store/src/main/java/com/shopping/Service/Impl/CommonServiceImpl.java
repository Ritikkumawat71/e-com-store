package com.shopping.Service.Impl;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.shopping.Service.CommonService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Service
public class CommonServiceImpl implements CommonService{

	@Override
	public void removeSessionMessage() {
		// TODO Auto-generated method stub
	    HttpServletRequest request = ((ServletRequestAttributes)(RequestContextHolder.getRequestAttributes())).getRequest();
	    HttpSession session = request.getSession();
	    session.removeAttribute("successMsg");
	    session.removeAttribute("errorMsg");
	}

}
