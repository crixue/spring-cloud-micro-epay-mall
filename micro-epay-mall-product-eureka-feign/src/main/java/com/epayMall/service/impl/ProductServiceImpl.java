package com.epayMall.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epayMall.common.Const;
import com.epayMall.common.ResponseCodeEnum;
import com.epayMall.common.ServerResponse;
import com.epayMall.dao.ProductMapper;
import com.epayMall.pojo.Category;
import com.epayMall.pojo.Product;
import com.epayMall.service.IDataClientZuulForCategory;
import com.epayMall.service.IProductSercive;
import com.epayMall.util.Constants;
import com.epayMall.util.DateTimeUtil;
import com.epayMall.vo.ProductDetailVo;
import com.epayMall.vo.ProductListVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service("iProductSercive")
public class ProductServiceImpl implements IProductSercive {
	private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private IDataClientZuulForCategory dataClientZuulForCategory;
    
    @Override
    public ServerResponse saveOrUpdateProduct(Product product){
        if(product != null)
        {
            if(StringUtils.isNotBlank(product.getSubImages())){
                String[] subImageArray = product.getSubImages().split(",");
                if(subImageArray.length > 0){
                    product.setMainImage(subImageArray[0]);
                }
            }

            if(product.getId() != null){
                int rowCount = productMapper.updateByPrimaryKey(product);
                if(rowCount > 0){
                    return ServerResponse.createBySucessResReturnMsg("更新产品成功");
                }
                return ServerResponse.createBySucessResReturnMsg("更新产品失败");
            }else{
                int rowCount = productMapper.insert(product);
                if(rowCount > 0){
                    return ServerResponse.createBySucessResReturnMsg("新增产品成功");
                }
                return ServerResponse.createBySucessResReturnMsg("新增产品失败");
            }
        }
        return ServerResponse.createByErrorResReturnMsg("新增或更新产品参数不正确");
    }

    @Override
    public ServerResponse<String> setSaleStatus(Integer productId,Integer status){
        if(productId == null || status == null){
            return ServerResponse.createByOtherErrorResReturnMsg(ResponseCodeEnum.ILLEGAL_ARGUMENT.getStatus(),ResponseCodeEnum.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if(rowCount > 0){
            return ServerResponse.createBySucessResReturnMsg("修改产品销售状态成功");
        }
        return ServerResponse.createByErrorResReturnMsg("修改产品销售状态失败");
    }

	@Override
	public ServerResponse<ProductDetailVo> getProductDetail(Integer productId) {
        if(productId == null){
            return ServerResponse.createByOtherErrorResReturnMsg(ResponseCodeEnum.ILLEGAL_ARGUMENT.getStatus(),ResponseCodeEnum.ILLEGAL_ARGUMENT.getDesc());
        }
        
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
			return ServerResponse.createByErrorResReturnMsg("不存在该商品");
		}
        
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        
		return ServerResponse.createBySucessResReturnData(productDetailVo);
	}
	
	private ProductDetailVo assembleProductDetailVo(Product product){
		ProductDetailVo productDetailVo = new ProductDetailVo();
		BeanUtils.copyProperties(product, productDetailVo);
		
		String imageHost = Constants.getProperty("ftp.server.http.prefix", "http://img.xuerongjing.com/");
		ServerResponse<Category> response = dataClientZuulForCategory.getCategoryByCategoryId(product.getCategoryId());
		if(!response.isSuccess()) {
			logger.info("[assembleProductDetailVo]-dataClientZuulForCategory-getCategoryByCategoryId 调用异常");
		}
		Category category = response.getData();
		Integer parentCategoryId = category.getParentId() == null? 0 : category.getParentId();
		String createTime = DateTimeUtil.dateToStr(product.getCreateTime());
		String updateTime = DateTimeUtil.dateToStr(product.getUpdateTime());
		
		productDetailVo.setImageHost(imageHost);
		productDetailVo.setParentCategoryId(parentCategoryId);
		productDetailVo.setCreateTime(createTime);
		productDetailVo.setUpdateTime(updateTime);
		return productDetailVo;
		
	}
	
	@Override
	public ServerResponse<ProductDetailVo> getProductDetailForMcht(Integer productId) {
        if(productId == null){
            return ServerResponse.createByOtherErrorResReturnMsg(ResponseCodeEnum.ILLEGAL_ARGUMENT.getStatus(),ResponseCodeEnum.ILLEGAL_ARGUMENT.getDesc());
        }
        
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
			return ServerResponse.createByErrorResReturnMsg("不存在该商品");
		}
        if (product.getStatus() != 1) {
        	return ServerResponse.createByErrorResReturnMsg("该商品已下架或删除");
		}
        
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        
		return ServerResponse.createBySucessResReturnData(productDetailVo);
	}

	/* 
	 * 使用mysql的pagehelper插件进行分页查询
	 */
	@Override
	public ServerResponse<PageInfo> getProductList(Integer pageNum, Integer pageSize) {
		//startPage设置其中几个分页属性
		PageHelper.startPage(pageNum, pageSize);
		//紧跟着的第一个select方法会被分页,此时我们可以从此list参数里面获得分页的参数
		List<Product> products = productMapper.selectList();
		
		List<ProductListVo> productListVos = new ArrayList<>();
		for (Product product : products) {
			ProductListVo productListVo = assembleProductListVo(product);
			productListVos.add(productListVo);
		}
		
		//pageHelper结尾
		
		PageInfo pageInfo = new PageInfo<>(products);
		pageInfo.setList(productListVos);
		return ServerResponse.createBySucessResReturnData(pageInfo);
	}
	
	private ProductListVo assembleProductListVo(Product product){
		ProductListVo productListVo = new ProductListVo();
		BeanUtils.copyProperties(product, productListVo);
		return productListVo;
		
	}

	@Override
	public ServerResponse searchProducts(String productName, Integer productId, Integer pageNum, Integer pageSize) {
		if (StringUtils.isNoneBlank(productName)) {
			productName = new StringBuilder().append("%").append(productName).append("%").toString();
		}
		
		PageHelper.startPage(pageNum, pageSize);
		List<Product> products = productMapper.selectProducByNameAndId(productName, productId);
		
		List<ProductListVo> productListVos = new ArrayList<>();
		for (Product product : products) {
			ProductListVo  productListVo = assembleProductListVo(product);
			productListVos.add(productListVo);
		}
		
		PageInfo pageInfo = new PageInfo<>(products);
		pageInfo.setList(productListVos);
		return ServerResponse.createBySucessResReturnData(pageInfo);
	}

	@SuppressWarnings("unused")
	@Override
	public <T> ServerResponse<T> searchProductForMcht(Integer categoryId, String keyword,
			Integer pageNum, Integer pageSize, String orderBy){
		ServerResponse response;
		if (categoryId == null && StringUtils.isBlank(keyword)) {
			response = ServerResponse.createByOtherErrorResReturnMsg(ResponseCodeEnum.ILLEGAL_ARGUMENT.getStatus(), ResponseCodeEnum.ILLEGAL_ARGUMENT.getDesc());
			return response;
		}
		if (StringUtils.isNoneBlank(keyword)) {
			keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
		}
		
		Product product = productMapper.selectByCategoryIdAndName(categoryId, keyword);
		if (product == null || product.getStatus() != 1) {
			return ServerResponse.createByErrorResReturnMsg("不存在该商品");
		}
		if (product == null && StringUtils.isBlank(keyword)) {
			PageHelper.startPage(pageNum, pageSize);
			List<Product> products = new ArrayList<>();
			PageInfo pageInfo = new PageInfo<>(products);
			response = ServerResponse.createBySucessResReturnData(pageInfo);
		}
		
		//product不为空，则获取包括其子类目的所有类目
		ServerResponse<List<Integer>> categoryIdsRes = dataClientZuulForCategory.getRecursionCategoryId(categoryId);
		List<Integer> categoryIds = categoryIdsRes.getData();
		
		PageHelper.startPage(pageNum, pageSize);
		//进行排序
		if (StringUtils.isNotBlank(orderBy)) {
			if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)) {
				String[] strs = orderBy.split("_");
				orderBy = new StringBuilder().append(strs[0]).append(" ").append(strs[1]).toString();
				PageHelper.orderBy(orderBy);
			}
		}

		List<Product> products = productMapper.selectByNameAndCategoryIds(keyword, categoryIds);
		List<ProductListVo> productListVos = new ArrayList<>();
		for (Product product2 : products) {
			ProductListVo productListVo = assembleProductListVo(product2);
			productListVos.add(productListVo);
		}
		PageInfo pageInfo = new PageInfo<>(productListVos);
		

		response  = ServerResponse.createBySucessResReturnData(pageInfo);
		return response;
		
	}
	
}
