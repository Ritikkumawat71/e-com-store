package com.shopping.Controller;

import java.security.Principal;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.shopping.Service.CartService;
import com.shopping.Service.CategoryService;
import com.shopping.Service.ProductOrderService;
import com.shopping.Service.UserService;
import com.shopping.model.Cart;
import com.shopping.model.Category;
import com.shopping.model.OrderRequest;
import com.shopping.model.Product;
import com.shopping.model.ProductOrder;
import com.shopping.model.UserDtls;
import com.shopping.utils.CommonUtil;
import com.shopping.utils.OrderStatus;
import com.shopping.utils.SHCCustomExceptionLogger;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	Logger logger = LogManager.getLogger(HomeController.class);
	
	@Autowired
	private UserService userService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private CartService cartService;

	@Autowired
	private ProductOrderService productOrderService;
	
	@Autowired
	private CommonUtil commonUtil;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@GetMapping("/")
	public String home() {
		return "user/home";
	}

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
		}catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
	}

	@GetMapping("/addCart")
	public String addToCart(@RequestParam Integer pid, @RequestParam Integer uid, HttpSession httpSession) {
		try {
			Cart saveCart = cartService.saveCart(pid, uid);
			if (ObjectUtils.isEmpty(saveCart)) {
				httpSession.setAttribute("errorMsg", "Product add to cart failed");
			} else {
				httpSession.setAttribute("successMsg", "Product added to Cart !!");
			}
		}catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return "redirect:/viewProduct/" + pid;
	}

	@GetMapping("/cart")
	public String loadCartPage(Principal p, Model m) {
		try {
			UserDtls user = getLoggedInUserDetails(p);
			List<Cart> carts = cartService.getCartsByUser(user.getId());
			m.addAttribute("carts", carts);
			if (carts.size() > 0) {
				Double totalOrderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
				m.addAttribute("totalOrderPrice", totalOrderPrice);
			}
		}catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return "/user/cart";
	}

	@GetMapping("/cartQuantityUpdate")
	public String updateCartQuantity(@RequestParam String sy, @RequestParam Integer cid) {
		cartService.updateQuantity(sy, cid);
		return "redirect:/user/cart";
	}

	private UserDtls getLoggedInUserDetails(Principal p) {
		String email = p.getName();
		UserDtls userDtls = userService.getUserByEmail(email);
		return userDtls;
	}

	@GetMapping("/orders")
	public String orderPage(Principal p, Model m) {
		try {
			UserDtls user = getLoggedInUserDetails(p);
			List<Cart> carts = cartService.getCartsByUser(user.getId());
			m.addAttribute("carts", carts);

			if (carts.size() > 0) {
				Double OrderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
				Double totalOrderPrice = carts.get(carts.size() - 1).getTotalOrderPrice() + 60 + 100;
				m.addAttribute("OrderPrice", OrderPrice);
				m.addAttribute("totalOrderPrice", totalOrderPrice);
			}
		} catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return "/user/order";
	}

	@PostMapping("/save-order")
	public String saveOrder(@ModelAttribute OrderRequest request, Principal p) throws Exception {
		try {
			UserDtls user = getLoggedInUserDetails(p);
			productOrderService.saveOrder(user.getId(), request);
		}catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return "redirect:/user/success";
	}
	
	@GetMapping("/success")
	public String loadSuccess() {
		return "/user/success";
	}
	
	@GetMapping("/user-orders")
	public String myOrder(Model m, Principal p) {
		try {
			UserDtls loginUser = getLoggedInUserDetails(p);
			List<ProductOrder> orders = productOrderService.getOrdersByUser(loginUser.getId());
			m.addAttribute("orders", orders);
		}catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return "/user/my_orders";
	}
	
	@GetMapping("/update-status")
	public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession httpSession) {
		try {
			OrderStatus[] values = OrderStatus.values();
			String status = null;
			for(OrderStatus orderStatus:values) {
				if (orderStatus.getId().equals(st)) {
					status = orderStatus.getName();
				}
			}
			
			ProductOrder updateOrder = productOrderService.updateOrderStatus(id, status);
			commonUtil.sendMailForProductOrder(updateOrder, status);
			
			if (ObjectUtils.isEmpty(updateOrder)) {
				httpSession.setAttribute("successMsg", "Status Updated");
			}else {
				httpSession.setAttribute("errorMsg", "Something wrong on server");
			}
		}catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return "redirect:/user/user-orders";
	}
	
	@GetMapping("/profile")
	public String profile() {
		return "/user/profile";
	}
	
	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute UserDtls user, @RequestParam MultipartFile img, HttpSession httpSession) {
		try {
			UserDtls updateUserProfile = userService.updateUserProfile(user, img);
			if (ObjectUtils.isEmpty(updateUserProfile)) {
				httpSession.setAttribute("errorMsg", "Profile not updated");
			}else {
				httpSession.setAttribute("successMsg", "profile Updated");
			}
		}catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return "redirect:/user/profile";
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal p, HttpSession httpSession) {
		try {
			UserDtls loggedInUserDetails = getLoggedInUserDetails(p);
			boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDetails.getPassword());
			if (matches) {
				String encodePassword = passwordEncoder.encode(newPassword);
				loggedInUserDetails.setPassword(encodePassword);
				UserDtls updateUser = userService.updateUser(loggedInUserDetails);
				if (ObjectUtils.isEmpty(updateUser)) {
					httpSession.setAttribute("errorMsg", "Password not updated. Error in server");
				}else {
					httpSession.setAttribute("successMsg", "Password updated successfully !!");
				}
			}else {
				httpSession.setAttribute("errorMsg", "Current Password Incorrect.");
			}
		}catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return "redirect:/user/profile";
	}
}
