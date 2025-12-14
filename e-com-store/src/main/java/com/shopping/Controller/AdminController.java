package com.shopping.Controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.shopping.Service.CartService;
import com.shopping.Service.CategoryService;
import com.shopping.Service.ProductOrderService;
import com.shopping.Service.UserService;
import com.shopping.Service.productService;
import com.shopping.model.Category;
import com.shopping.model.Product;
import com.shopping.model.ProductOrder;
import com.shopping.model.UserDtls;
import com.shopping.utils.CommonUtil;
import com.shopping.utils.OrderStatus;
import com.shopping.utils.SHCCustomExceptionLogger;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/")
public class AdminController {
	Logger logger = LogManager.getLogger(AdminController.class);

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private productService productService;

	@Autowired
	private UserService userService;

	@Autowired
	private CartService cartService;

	@Autowired
	private ProductOrderService productOrderService;

	@Autowired
	private CommonUtil commonUtil;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		if (p != null) {
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
			Integer countCart = cartService.getCountCart(userDtls.getId());
			m.addAttribute("countCart", countCart);
		}

		List<Category> allActiveCategory = categoryService.getAllActiveCategory();
		m.addAttribute("categorys", allActiveCategory);
	}

	@GetMapping("/")
	public String index() {
		return "admin/index";
	}

	@GetMapping("/product")
	public String addProduct(Model m) {
		List<Category> category = categoryService.getAllCategory();
		m.addAttribute("category", category);
		return "admin/add_product";
	}

	@GetMapping("/editCategory/{id}")
	public String loadEditCategory(@PathVariable int id, Model m) {
		m.addAttribute("category", categoryService.getCategoryById(id));
		return "admin/Edit_category";
	}

	@GetMapping("/category")
	public String category(Model m, @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "4") Integer pageSize) {
        try {
        	Page<Category> page = categoryService.getAllCategoryPagination(pageNo, pageSize);

    		List<Category> category = page.getContent();
    		m.addAttribute("category", category);
    		m.addAttribute("pageNo", page.getNumber());
    		m.addAttribute("pageSize", pageSize);
    		m.addAttribute("totalElements", page.getTotalElements());
    		m.addAttribute("totalPages", page.getTotalPages());
    		m.addAttribute("isFirst", page.isFirst());
    		m.addAttribute("isLast", page.isLast());
        }catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return "admin/category";
	}

	@PostMapping("/saveCategory")
	public String saveCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
			HttpSession httpSession) throws IOException {
        try {
        	String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
        	category.setImageName(imageName);
        	Boolean existCategory = categoryService.existCategory(category.getName());
        	if (existCategory) {
        		httpSession.setAttribute("errorMsg", "Category name already exist");
        	} else {
        		Category saveCategory = categoryService.saveCategory(category);
        		if (ObjectUtils.isEmpty(saveCategory)) {
        			httpSession.setAttribute("errorMsg", "Not Saved !! Internal server error");
        		} else {
        			File saveFile = new ClassPathResource("static/img/").getFile();
        			Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_image" + File.separator
        					+ file.getOriginalFilename());
        			
        			System.out.println(path);
        			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        			
        			httpSession.setAttribute("successMsg", "Saved Successfully !!");
        		}
        	}
        }catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return "redirect:/admin/category";
	}

	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id, HttpSession httpSession) {
		try {
			boolean deleteCategory = categoryService.deleteCategory(id);
			if (deleteCategory) {
				httpSession.setAttribute("successMsg", "Category delete Success !!");
			} else {
				httpSession.setAttribute("errorMsg", "Something wrong on error !!");
			}
		} catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return "redirect:/admin/category";
	}

	@PostMapping("/updateCategory")
	public String updateCategory(@ModelAttribute Category category, @RequestParam MultipartFile file,
			HttpSession httpSession){
        try {
        	Category Oldcategory = categoryService.getCategoryById(category.getId());
        	String imageName = file.isEmpty() ? Oldcategory.getImageName() : file.getOriginalFilename();
        	
        	if (!ObjectUtils.isEmpty(category)) {
        		Oldcategory.setName(category.getName());
        		Oldcategory.setIsActive(category.getIsActive());
        		Oldcategory.setImageName(imageName);
        	}
        	
        	Category updateCategory = categoryService.saveCategory(Oldcategory);
        	if (!ObjectUtils.isEmpty(updateCategory)) {
        		
        		if (!file.isEmpty()) {
        			File savefile = new ClassPathResource("static/img").getFile();
        			
        			Path path = Paths.get(savefile.getAbsolutePath() + File.separator + "category_image" + File.separator
        					+ file.getOriginalFilename());
        			
        			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        		}
        		
        		httpSession.setAttribute("successMsg", "Category update Successfully !!");
        	} else {
        		httpSession.setAttribute("errorMsg", "Something wrong on error !!");
        	}
        }catch (Exception e) {
        	SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}

		return "redirect:/admin/editCategory/" + category.getId();
	}

	@PostMapping("/saveProduct")
	public String saveProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile file,
			HttpSession httpSession) throws IOException {
		try {

		} catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		
		
		String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
		product.setImage(imageName);
		product.setDiscount(0);
		product.setDiscountPrice(product.getDiscountPrice());

		Product saveProduct = productService.saveProduct(product);

		if (!ObjectUtils.isEmpty(saveProduct)) {
			if (!file.isEmpty()) {
				File savefile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(savefile.getAbsolutePath() + File.separator + "p_i" + File.separator
						+ file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
			httpSession.setAttribute("successMsg", "Product saved Success");
		} else {
			httpSession.setAttribute("errorMsg", "Something wrong on server");
		}

		return "redirect:/admin/product";
	}

	@GetMapping("/products")
	public String viewProduct(Model m, @RequestParam(defaultValue = "") String ch,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "4") Integer pageSize) {

		try {

		} catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		
		Page<Product> page = null;

		if (ch != null && ch.length() > 0) {
			page = productService.searchProductPagination(pageNo, pageSize, ch);
		} else {
			page = productService.getAllProductsPagination(pageNo, pageSize);
		}

		m.addAttribute("products", page.getContent());
		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "admin/products";
	}

	@GetMapping("/deleteProduct/{id}")
	public String deleteProduct(@PathVariable int id, HttpSession httpSession) {
		try {

		} catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		Boolean deleteProduct = productService.deleteProduct(id);
		if (deleteProduct) {
			httpSession.setAttribute("successMsg", "Product delete Success !!");
		} else {
			httpSession.setAttribute("errorMsg", "Something wrong on server !!");
		}
		return "redirect:/admin/products";
	}

	@GetMapping("/editProduct/{id}")
	public String editProduct(@PathVariable int id, Model m) {
		m.addAttribute("product", productService.getProductById(id));
		m.addAttribute("category", categoryService.getAllCategory());
		return "admin/Edit_product";
	}

	@PostMapping("/updateProduct")
	public String updateProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile file,
			HttpSession httpSession, Model m) {
		try {
			if (product.getDiscount() < 0 || product.getDiscount() > 100) {
				httpSession.setAttribute("errorMsg", "Invalid Discount !!");
			} else {
				
				Product updateProduct = productService.updateProduct(product, file);
				if (!ObjectUtils.isEmpty(updateProduct)) {
					httpSession.setAttribute("successMsg", "product update success");
				} else {
					httpSession.setAttribute("errorMsg", "Something wrong on server");
				}
			}
		} catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return "redirect:/admin/editProduct/" + product.getId();
	}

	@GetMapping("/users")
	public String getAllUsers(Model m, @RequestParam Integer type) {
		try {
			List<UserDtls> users = null;
			if (type == 1) {
				users = userService.getAllUsers("ROLE_USER");
			} else {
				users = userService.getAllUsers("ROLE_ADMIN");
			}
			m.addAttribute("userType", type);
			m.addAttribute("users", users);
		} catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return "/admin/users";
	}

	@GetMapping("/updateStatus")
	public String updateUserAccountStatus(@RequestParam Boolean status, @RequestParam Integer id, @RequestParam Integer type, 
			HttpSession httpSession) {
		try {
			Boolean f = userService.updateAccountStatus(id, status);
			if (f) {
				httpSession.setAttribute("successMsg", "Account Status updated");
			} else {
				httpSession.setAttribute("errorMsg", "Something wrong on server");
			}
		} catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return "redirect:/admin/users?type="+type;
	}

	@GetMapping("/orders")
	public String getAllOrders(Model m, @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			                            @RequestParam(name = "pageSize", defaultValue = "4") Integer pageSize) {
		try {
			Page<ProductOrder> page = productOrderService.getAllOrdersPagination(pageNo, pageSize);
			m.addAttribute("orders", page.getContent());
			m.addAttribute("srch", false);
			
			m.addAttribute("products", page.getContent());
			m.addAttribute("pageNo", page.getNumber());
			m.addAttribute("pageSize", pageSize);
			m.addAttribute("totalElements", page.getTotalElements());
			m.addAttribute("totalPages", page.getTotalPages());
			m.addAttribute("isFirst", page.isFirst());
			m.addAttribute("isLast", page.isLast());
		} catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}		
		return "/admin/orders";
	}

	@PostMapping("/update-order-status")
	public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession httpSession) {
		try {
			OrderStatus[] values = OrderStatus.values();
			String status = null;
			for (OrderStatus orderStatus : values) {
				if (orderStatus.getId().equals(st)) {
					status = orderStatus.getName();
				}
			}
			ProductOrder updateOrder = productOrderService.updateOrderStatus(id, status);
			commonUtil.sendMailForProductOrder(updateOrder, status);
			if (ObjectUtils.isEmpty(updateOrder)) {
				httpSession.setAttribute("successMsg", "Status Updated");
			} else {
				httpSession.setAttribute("errorMsg", "Something wrong on server");
			}
		} catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return "redirect:/admin/orders";
	}

	@GetMapping("/search-order")
	public String searchProduct(@RequestParam String orderId, Model m, HttpSession httpSession, 
			                    @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
                                @RequestParam(name = "pageSize", defaultValue = "4") Integer pageSize) {
		try {
			if (orderId != null && orderId.length() > 0) {
				
				ProductOrder order = productOrderService.getOrdersByOrderId(orderId);
				if (ObjectUtils.isEmpty(order)) {
					httpSession.setAttribute("errorMsg", "Incorrect Order Id.");
					m.addAttribute("orderDtls", null);
				} else {
					m.addAttribute("orderDtls", order);
				}
				
				m.addAttribute("srch", true);
			} else {
				Page<ProductOrder> page = productOrderService.getAllOrdersPagination(pageNo, pageSize);
				m.addAttribute("orders", page.getContent());
				m.addAttribute("srch", false);
				
				m.addAttribute("pageNo", page.getNumber());
				m.addAttribute("pageSize", pageSize);
				m.addAttribute("totalElements", page.getTotalElements());
				m.addAttribute("totalPages", page.getTotalPages());
				m.addAttribute("isFirst", page.isFirst());
				m.addAttribute("isLast", page.isLast());
			}
		} catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return "/admin/orders";
	}
	
	@GetMapping("/add-admin")
	public String adminAdd() {
		return "/admin/add_admin";
	}
	
	@PostMapping("/save-admin")
	public String saveUser(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession httpSession) throws IOException {
		try {
			String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
			user.setProfileImage(imageName);
			UserDtls saveAdmin = userService.saveAdmin(user);
			
			if (!ObjectUtils.isEmpty(saveAdmin)) {
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
		return "redirect:/admin/add-admin";
	}
	
	@GetMapping("/profile")
	public String profile() {
		return "/admin/profile";
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
		} catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return "redirect:/admin/profile";
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal p, HttpSession httpSession) {
		try {
			UserDtls loggedInUserDetails = commonUtil.getLoggedInUserDetails(p);
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
		} catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return "redirect:/admin/profile";
	}
}
