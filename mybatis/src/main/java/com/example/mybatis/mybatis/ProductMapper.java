package com.example.mybatis.mybatis;

import com.example.mybatis.dto.ProductDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper {
    void saveProduct(ProductDTO productDTO);
}
