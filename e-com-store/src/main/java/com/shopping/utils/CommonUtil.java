package com.shopping.utils;

import java.io.UnsupportedEncodingException;
import java.security.Principal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.shopping.Service.UserService;
import com.shopping.model.ProductOrder;
import com.shopping.model.UserDtls;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CommonUtil {
	Logger logger = LogManager.getLogger(CommonUtil.class);

	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private UserService userService;
	
	public Boolean sendMail(String url, String reciepentEmail) throws UnsupportedEncodingException, MessagingException {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true); 
			
			helper.setFrom(StatusResponse.SENDER_EMAIL, StatusResponse.EMAIL_TITLE); 
			helper.setTo(reciepentEmail);
			
			String content =
			        "<p>Hello,</p>"
			      + "<p>We received a request to reset your password for your account.</p>"
			      + "<p>Please click the link below to create a new password:</p>"
			      + "<p><a href=\"" + url + "\" style=\"color:#1a73e8; font-weight:bold;\">Reset My Password</a></p>"
			      + "<p>If you did not request a password reset, please ignore this email or contact our support team.</p>"
			      + "<p>This link will expire for security reasons.</p>"
			      + "<br>"
			      + "<p>Thank you,</p>"
			      + "<p><strong>E-Com-Store Support Team</strong></p>";
			
			helper.setSubject("Password Reset");
			helper.setText(content, true); 
			mailSender.send(message);
		}catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return true;
	}

	public static String generateUrl(HttpServletRequest request) {
		// TODO Auto-generated method stub
		String siteUrl = request.getRequestURL().toString();

		return siteUrl.replace(request.getServletPath(), "");
	}
	
	String msg = null;
	
	public Boolean sendMailForProductOrder(ProductOrder order, String status) throws Exception {
		
		msg = "<p>Hello, [[name]]</p> <p>Thank you, Your Order is <b>[[orderStatus]]</b> !!</p>"
			    +" <p><b>Product Details : </b></p>"
			    +" <p>Name : [[productName]]</p>"
			    +" <p>Category : [[category]]</p>"
			    +" <p>Quantity : [[quantity]]</p>"
			    +" <p>Price : [[price]]</p>"
			    +" <p>Payment : [[paymentType]]</p>";
		
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		
		helper.setFrom(StatusResponse.SENDER_EMAIL, StatusResponse.EMAIL_TITLE);
		helper.setTo(order.getOrderAddress().getEmail());
		
		msg = msg.replace("[[name]]", order.getOrderAddress().getFirstName());
		msg = msg.replace("[[orderStatus]]", status);
		msg = msg.replace("[[productName]]", order.getProduct().getTitle());
		msg = msg.replace("[[category]]", order.getProduct().getCategory());
		msg = msg.replace("[[quantity]]", order.getQuantity().toString());
		msg = msg.replace("[[price]]", order.getPrice().toString());
		msg = msg.replace("[[paymentType]]", order.getPaymentType());
		                 
		helper.setSubject("Product Order Status");
		helper.setText(msg, true);
		mailSender.send(message);		
		return true;
	}
	
	public UserDtls getLoggedInUserDetails(Principal p) {
		// TODO Auto-generated method stub
		String email = p.getName();
		UserDtls userDtls = userService.getUserByEmail(email);
		return userDtls;
	}
}
