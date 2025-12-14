package com.shopping.utils;

import java.io.UnsupportedEncodingException;
import java.security.Principal;

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

	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private UserService userService;
	
	public Boolean sendMail(String url, String reciepentEmail) throws UnsupportedEncodingException, MessagingException {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true); // Added 'true' for multipart (good practice)
			
			helper.setFrom("ritikkumawat71@gmail.com", "Shopping Cart"); 
			helper.setTo(reciepentEmail);
			
			String content = "<p>Hello, </p>" 
			                 + "<p>You have requested to reset your password.</p>"
			                 + "<p>Click the link below to change your password:</p>" 
			                 + "<p> <a href=\"" + url + "\">Change my password</a></p>"; // Corrected anchor tag
			                 
			helper.setSubject("Password Reset");
			helper.setText(content, true); 
			mailSender.send(message);
		}catch (Exception e) {
			e.printStackTrace();
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
		
		helper.setFrom("ritikkumawat71@gmail.com", "Shopping Cart");
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
