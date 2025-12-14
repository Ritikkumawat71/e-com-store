package com.shopping.Service.Impl;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.shopping.Repository.CartRepo;
import com.shopping.Repository.ProductOrderRepo;
import com.shopping.Service.ProductOrderService;
import com.shopping.model.Cart;
import com.shopping.model.OrderAddress;
import com.shopping.model.OrderRequest;
import com.shopping.model.ProductOrder;
import com.shopping.utils.CommonUtil;
import com.shopping.utils.OrderStatus;

@Service
public class ProductOrderServiceImpl implements ProductOrderService {

	@Autowired
	private ProductOrderRepo productOrderRepo;
	
	@Autowired
	private CartRepo cartRepo;
	
	@Autowired
	private CommonUtil commonUtil;
	
	@Override
	public void saveOrder(Integer userId, OrderRequest orderRequest) throws Exception {
		// TODO Auto-generated method stub
		List<Cart> carts = cartRepo.findByUserId(userId);
		
		for(Cart cart : carts) {
			ProductOrder order = new ProductOrder();
			order.setOrderId(UUID.randomUUID().toString());
			order.setOrderDate(LocalDate.now());
			order.setProduct(cart.getProduct());
			order.setPrice(cart.getProduct().getDiscountPrice());
			order.setQuantity(cart.getQuantity());
			order.setUser(cart.getUser());
			order.setStatus(OrderStatus.IN_PROGRESS.getName());
			order.setPaymentType(orderRequest.getPaymentType());
			
			OrderAddress address = new OrderAddress();
			
			address.setFirstName(orderRequest.getFirstName());
			address.setLastName(orderRequest.getLastName());
			address.setEmail(orderRequest.getEmail());
			address.setMobileNumber(orderRequest.getMobileNumber());
			address.setAddress(orderRequest.getAddress());
			address.setCity(orderRequest.getCity());
			address.setState(orderRequest.getState());
			address.setPincode(orderRequest.getPincode());
			
			order.setOrderAddress(address);
			
			ProductOrder saveOrder = productOrderRepo.save(order);
			commonUtil.sendMailForProductOrder(saveOrder, "success");
		}
	}

	@Override
	public List<ProductOrder> getOrdersByUser(Integer userId) {
		// TODO Auto-generated method stub
		List<ProductOrder> orders = productOrderRepo.findByUserId(userId);
		return orders;
	}

	@Override
	public ProductOrder updateOrderStatus(Integer id, String status) {
		// TODO Auto-generated method stub
		
		Optional<ProductOrder> findById = productOrderRepo.findById(id);
		
		if (findById.isPresent()) {
			ProductOrder productOrder = findById.get();
			productOrder.setStatus(status);
			ProductOrder updateOrder = productOrderRepo.save(productOrder);
			return updateOrder;
		}
		
		return null;
	}

	@Override
	public List<ProductOrder> getAllOrders() {
		// TODO Auto-generated method stub
		return productOrderRepo.findAll();
	}
	
	@Override
	public Page<ProductOrder> getAllOrdersPagination(Integer pageNo, Integer pageSize) {
		
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		
		return productOrderRepo.findAll(pageable);
	}

	@Override
	public ProductOrder getOrdersByOrderId(String orderId) {
		// TODO Auto-generated method stub
		return productOrderRepo.findByOrderId(orderId);
	}

}
