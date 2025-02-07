package com.example.mybatis.mybatis;


import com.example.mybatis.dto.CartDTO;
import com.example.mybatis.dto.ProductDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CartMapper {
    void addToCart(CartDTO cartDTO);
    List<CartDTO> getCartProducts(@Param("userUid") Integer userUid, @Param("sessionId") String sessionId);
    List<CartDTO> getCartProductsBySession(@Param("sessionId") String sessionId);
    void removeFromCart(CartDTO cartDTO);
    void checkout(CartDTO cartDTO);
}