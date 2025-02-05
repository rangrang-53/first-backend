package com.example.mybatis.mybatis;


import com.example.mybatis.dto.CartDTO;
import com.example.mybatis.dto.ProductDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CartMapper {
    void addToCart(CartDTO cartDTO);
    void removeFromCart(CartDTO cartDTO);
    List<CartDTO> getCartProducts(CartDTO cartDTO);
    void checkout(CartDTO cartDTO);
}