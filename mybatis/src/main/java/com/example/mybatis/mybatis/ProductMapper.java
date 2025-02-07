package com.example.mybatis.mybatis;

import com.example.mybatis.dto.CategoryDTO;
import com.example.mybatis.dto.ProductDTO;
import com.example.mybatis.dto.ProductImageDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductMapper {
    void saveProduct(ProductDTO productDTO);
    void saveProductImages(List<ProductImageDTO> images);
    void saveCategory(CategoryDTO categoryDTO);
}
