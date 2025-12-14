package com.shopping.Controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.shopping.Service.CartService;
import com.shopping.Service.CategoryService;
import com.shopping.Service.UserService;
import com.shopping.Service.productService;
import com.shopping.model.Category;
import com.shopping.model.Product;
import com.shopping.model.UserDtls;
import com.shopping.utils.CommonUtil;
import com.shopping.utils.SHCCustomExceptionLogger;

import io.micrometer.common.util.StringUtils;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;


@Controller
public class HomeController {
	Logger logger = LogManager.getLogger(HomeController.class);
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private productService productService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CommonUtil commonUtil;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private CartService cartService;

	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		try {
			if (p != null) {
				String email = p.getName();
				UserDtls userDtls = userService.getUserByEmail(email);
				m.addAttribute("user", userDtls);
				Integer countCart = cartService.getCountCart(userDtls.getId());
				m.addAttribute("countCart", countCart);
			}

			List<Category> allActiveCategory = categoryService.getAllActiveCategory();
			m.addAttribute("categorys", allActiveCategory);
		} catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
	}
	
	@GetMapping("/")
	public String index(Model m) {
		try {
			List<Category> allActiveCategory = categoryService.getAllActiveCategory().stream()
					.sorted((c1, c2)->c2.getId().compareTo(c1.getId()))
					.limit(6).toList();
			List<Product> allActiveProducts = productService.getAllActiveProducts("").stream()
					.sorted((p1, p2)->p2.getId().compareTo(p1.getId()))
					.limit(8).toList();
			m.addAttribute("category", allActiveCategory);
			m.addAttribute("products", allActiveProducts);
		} catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return "index";
	}
	
	@GetMapping("/signin")
	public String login() {
		return "login";
	}
	
	@GetMapping("/register")
	public String register() {
		return "register";
	}
	
	@GetMapping("/products")
	public String products(Model m, @RequestParam(value = "category", defaultValue = "") String category,
			                @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			                @RequestParam(name = "pageSize", defaultValue = "4") Integer pageSize,
			                @RequestParam(defaultValue = "") String ch) {
        try {
        	List<Category> categories = categoryService.getAllActiveCategory();
        	m.addAttribute("paramValue", category);
        	m.addAttribute("categories", categories);
        	
        	Page<Product> page = null;
        	if (StringUtils.isEmpty(ch)) {
        		page = productService.getAllActiveProductPagination(pageNo, pageSize, category);
        	}else {
        		page = productService.searchActiveProductPagination(pageNo, pageSize, category, ch);
        	}
        	
        	List<Product> products = page.getContent();
        	m.addAttribute("products", products);
        	m.addAttribute("productsSize", products.size());
        	m.addAttribute("pageNo", page.getNumber());
        	m.addAttribute("pageSize", pageSize);
        	m.addAttribute("totalElements", page.getTotalElements());
        	m.addAttribute("totalPages", page.getTotalPages());
        	m.addAttribute("isFirst", page.isFirst());
        	m.addAttribute("isLast", page.isLast());
		} catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return "product";
	}
	
	@GetMapping("/viewProduct/{id}")
	public String view_products(@PathVariable int id, Model m) {
		Product productById = productService.getProductById(id);
		m.addAttribute("product", productById);
		return "view_product";
	}
	
	@PostMapping("/saveUser")
	public String saveUser(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession httpSession) throws IOException {
        try {
        	String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
        	user.setProfileImage(imageName);
        	UserDtls saveUser = userService.saveUser(user);
        	if (!ObjectUtils.isEmpty(saveUser)) {
        		if (!file.isEmpty()) {
        			File savefile = new ClassPathResource("static/img").getFile();
        			
        			Path path = Paths.get(savefile.getAbsolutePath() + File.separator + "profile_img" + File.separator
        					+ file.getOriginalFilename());
        			
        			System.out.println(path);
        			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        		}
        		httpSession.setAttribute("successMsg", "Register Successfully !!");
        	}else {
        		httpSession.setAttribute("errorMsg", "Something wrong on error !!");
        	}
		} catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return "redirect:/register";
	}
	
	
	@GetMapping("/forgot-password")
	public String showForgotPassword() {
		return "forgot_password.html";
	}
	
	@PostMapping("/forgot-password")
	public String processForgotPassword(@RequestParam String email, HttpSession httpSession, HttpServletRequest request) throws UnsupportedEncodingException, MessagingException {
        try {
        	UserDtls userByEmail = userService.getUserByEmail(email);
        	if (ObjectUtils.isEmpty(userByEmail)) {
        		httpSession.setAttribute("errorMsg", "Inavlid Email !!");
        	}else {
        		String resetToken = UUID.randomUUID().toString();
        		userService.updateUserResetToken(email, resetToken);
        		String url = CommonUtil.generateUrl(request) + "/reset-password?token="+resetToken;
        		Boolean sendMail = commonUtil.sendMail(url, email);
        		if (sendMail) {
        			httpSession.setAttribute("successMsg", "Please check your email. Password reset link send !!");
        		}else {
        			httpSession.setAttribute("errorMsg", "Something wrong on server. Email not send !!");
        		}
        	}
		} catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return "redirect:/forgot-password";
	}
	
	@GetMapping("/reset-password")
	public String showResetPassword(@RequestParam String token, HttpSession httpSession, Model m) {
        try {
        	UserDtls userByToken = userService.getUserByToken(token);
        	if (userByToken == null) {
        		m.addAttribute("msg", "Your link is invalid or expired !!");
        		return "message";
        	}
        	m.addAttribute("token", token);
		} catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return "reset_password";
	}
	
	@PostMapping("/reset-password")
	public String resetPassword(@RequestParam String token,@RequestParam String password, HttpSession httpSession, Model m) {
        try {
        	UserDtls userByToken = userService.getUserByToken(token);
        	
        	if (userByToken == null) {
        		m.addAttribute("errorMsg", "Your link is invalid or expired !!");
        		return "message";
        	}else {
        		userByToken.setPassword(passwordEncoder.encode(password));
        		userByToken.setResetToken(null);
        		userService.updateUser(userByToken);
        		httpSession.setAttribute("successMsg", "Password change successfully !!");
        		m.addAttribute("msg", "Password change successfully !!");
        		return "message";
        	}
		} catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
			return "Somthing Went Wrong";
		}
	}
	
	@GetMapping("/search")
	public String searchProduct(@RequestParam String ch, Model m) {
        try {
        	List<Product> searchProduct = productService.searchProduct(ch);
        	List<Category> categories = categoryService.getAllActiveCategory();
        	m.addAttribute("products", searchProduct);
        	m.addAttribute("categories", categories);
		} catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return "/product";
	}
}
