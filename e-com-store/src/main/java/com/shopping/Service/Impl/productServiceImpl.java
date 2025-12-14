package com.shopping.Service.Impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.shopping.Repository.productRepo;
import com.shopping.Service.productService;
import com.shopping.model.Product;

@Service
public class productServiceImpl implements productService {

	@Autowired
	private productRepo productRepo;

	@Override
	public Product saveProduct(Product product) {
		// TODO Auto-generated method stub
		return productRepo.save(product);
	}

	@Override
	public List<Product> getAllProducts() {
		// TODO Auto-generated method stub
		return productRepo.findAll();
	}

	@Override
	public Page<Product> getAllProductsPagination(Integer pageNo, Integer pageSize) {
		// TODO Auto-generated method stub
		Pageable pageable = PageRequest.of(pageNo, pageSize);

		return productRepo.findAll(pageable);
	}

	@Override
	public Boolean deleteProduct(Integer id) {
		// TODO Auto-generated method stub
		Product product = productRepo.findById(id).orElse(null);
		if (!ObjectUtils.isEmpty(product)) {
			productRepo.delete(product);
			return true;
		}
		return false;
	}

	@Override
	public Product getProductById(Integer id) {
		// TODO Auto-generated method stub
		Product product = productRepo.findById(id).orElse(null);
		return product;
	}

	@Override
	public Product updateProduct(Product product, MultipartFile file) {
		Product dbProduct = getProductById(product.getId());

		String imageName = file.isEmpty() ? dbProduct.getImage() : file.getOriginalFilename();

		dbProduct.setTitle(product.getTitle());
		dbProduct.setDescription(product.getDescription());
		dbProduct.setCategory(product.getCategory());
		dbProduct.setPrice(product.getPrice());
		dbProduct.setStock(product.getStock());
		dbProduct.setImage(imageName);
		dbProduct.setIsActive(product.getIsActive());

		dbProduct.setDiscount(product.getDiscount());

		Double discount = product.getPrice() * (product.getDiscount() / 100.0);
		Double discountPrice = product.getPrice() - discount;

		dbProduct.setDiscountPrice(discountPrice);

		Product updateProduct = productRepo.save(dbProduct);
		if (!ObjectUtils.isEmpty(updateProduct)) {
			if (!file.isEmpty()) {
				try {
					File savefile = new ClassPathResource("static/img/").getFile();

					Path path = Paths.get(savefile.getAbsolutePath() + File.separator + "p_i" + File.separator
							+ file.getOriginalFilename());
					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return product;
		}
		return updateProduct;
	}

	@Override
	public List<Product> getAllActiveProducts(String category) {
		List<Product> products = null;
		if (ObjectUtils.isEmpty(category)) {
			products = productRepo.findByIsActiveTrue();
		} else {
			products = productRepo.findByCategory(category);
		}
		return products;
	}

	@Override
	public List<Product> searchProduct(String ch) {
		return productRepo.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch);
	}

	@Override
	public Page<Product> searchProductPagination(Integer pageNo, Integer pageSize, String ch) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return productRepo.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch, pageable);
	}

	@Override
	public Page<Product> getAllActiveProductPagination(Integer pageNo, Integer pageSize, String category) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		Page<Product> pageProduct = null;
		if (ObjectUtils.isEmpty(category)) {
			pageProduct = productRepo.findByIsActiveTrue(pageable);
		} else {
			pageProduct = productRepo.findByCategory(category, pageable);
		}
		return pageProduct;
	}

	@Override
	public Page<Product> searchActiveProductPagination(Integer pageNo, Integer pageSize, String category, String ch) {
		Page<Product> pageProduct = null;
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		pageProduct = productRepo.findByisActiveTrueAndTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch, pageable);
		return pageProduct;
	}
}
