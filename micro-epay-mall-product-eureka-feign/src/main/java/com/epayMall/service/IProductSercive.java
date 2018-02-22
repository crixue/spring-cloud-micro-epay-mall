package com.epayMall.service;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.epayMall.common.ServerResponse;
import com.epayMall.pojo.Product;
import com.epayMall.vo.ProductDetailVo;

public interface IProductSercive {

	ServerResponse saveOrUpdateProduct(Product product);

	ServerResponse<String> setSaleStatus(Integer productId, Integer status);
	
	ServerResponse<ProductDetailVo> getProductDetail(Integer productId);
	
	ServerResponse getProductList(Integer pageNum, Integer pageSize);
	
	ServerResponse searchProducts(String productName, Integer productId, Integer pageNum, Integer pageSize);

	ServerResponse<ProductDetailVo> getProductDetailForMcht(Integer productId);

	<T> ServerResponse<T> searchProductForMcht(Integer categoryId, String keyword, Integer pageNum, Integer pageSize,
			String orderBy);
	
}
