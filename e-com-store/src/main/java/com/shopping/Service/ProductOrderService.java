package com.shopping.Service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.shopping.model.OrderRequest;
import com.shopping.model.ProductOrder;

public interface ProductOrderService {

	public void saveOrder(Integer userId, OrderRequest orderRequest) throws Exception;
	
	public List<ProductOrder> getOrdersByUser(Integer userId);
	
	public ProductOrder updateOrderStatus(Integer id, String status);
	
	public List<ProductOrder> getAllOrders();
	
	public ProductOrder getOrdersByOrderId(String orderId);
	
	public Page<ProductOrder> getAllOrdersPagination(Integer pageNo, Integer pageSize);
}
