package com.shopping.Service.Impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.shopping.Controller.HomeController;
import com.shopping.Repository.CartRepo;
import com.shopping.Repository.UserRepo;
import com.shopping.Repository.productRepo;
import com.shopping.Service.CartService;
import com.shopping.model.Cart;
import com.shopping.model.Product;
import com.shopping.model.UserDtls;
import com.shopping.utils.SHCCustomExceptionLogger;

@Service
public class CartServiceImpl implements CartService {
	Logger logger = LogManager.getLogger(CartServiceImpl.class);

	@Autowired
	private CartRepo cartRepo;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private productRepo productRepo;

	@Override
	public Cart saveCart(Integer productId, Integer userId) {
		try {
			UserDtls userDtls = userRepo.findById(userId).get();
			Product product = productRepo.findById(productId).get();
			Cart cartStatus = cartRepo.findByProductIdAndUserId(productId, userId);

			Cart cart = null;
			if (ObjectUtils.isEmpty(cartStatus)) {
				cart = new Cart();
				cart.setProduct(product);
				cart.setUser(userDtls);
				cart.setQuantity(1);
				cart.setTotalPrice(1 * product.getDiscountPrice());
			} else {
				cart = cartStatus;
				cart.setQuantity(cart.getQuantity() + 1);
				cart.setTotalPrice(cart.getQuantity() * cart.getProduct().getDiscountPrice());
			}

			Cart saveCart = cartRepo.save(cart);

			return saveCart;
		} catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return null;
	}

	@Override
	public List<Cart> getCartsByUser(Integer userId) {
		try {

			List<Cart> carts = cartRepo.findByUserId(userId);
			Double totalOrderPrice = 0.0;
			List<Cart> updateCarts = new ArrayList<>();

			for (Cart c : carts) {
				Double totalPrice = (c.getProduct().getDiscountPrice() * c.getQuantity());
				c.setTotalPrice(totalPrice);

				totalOrderPrice = totalOrderPrice + totalPrice;
				c.setTotalOrderPrice(totalOrderPrice);
				updateCarts.add(c);
			}

			return updateCarts;
		} catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
		return null;
	}

	@Override
	public Integer getCountCart(Integer userId) {
		Integer countByUserId = cartRepo.countByUserId(userId);
		return countByUserId;
	}

	@Override
	public void updateQuantity(String sy, Integer cid) {
		try {
			Cart cart = cartRepo.findById(cid).get();
			int updateQuantity;

			if (sy.equalsIgnoreCase("de")) {
				updateQuantity = cart.getQuantity() - 1;

				if (updateQuantity <= 0) {
					cartRepo.delete(cart);
				} else {
					cart.setQuantity(updateQuantity);
					cartRepo.save(cart);
				}
			} else {
				updateQuantity = cart.getQuantity() + 1;
				cart.setQuantity(updateQuantity);
				cartRepo.save(cart);
			}
		} catch (Exception e) {
			SHCCustomExceptionLogger.customExceptionPrintTrace(logger, e);
		}
	}

}
