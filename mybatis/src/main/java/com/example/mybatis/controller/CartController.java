package com.example.mybatis.controller;

import com.example.mybatis.dto.CartDTO;
import com.example.mybatis.dto.ProductDTO;
import com.example.mybatis.dto.UserDTO;
import com.example.mybatis.mybatis.CartMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CartController {

    private CartMapper cartMapper;

    @Autowired
    public CartController(CartMapper cartMapper) {
        this.cartMapper = cartMapper;
    }

    @PostMapping("/cart")
    public ResponseEntity<?> add(@RequestBody CartDTO cartDTO,
                                 HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userUid") != null) {
            // 로그인한 경우
            int userUid = (int) session.getAttribute("userUid");
            UserDTO userDTO = new UserDTO();
            userDTO.setUid(userUid);
            cartDTO.setUserDTO(userDTO);
        } else {
            // 로그인하지 않은 경우
            cartDTO.setUserDTO(null);  // user_uid를 NULL로 설정
        }
        cartMapper.addToCart(cartDTO);
        return new ResponseEntity<>(HttpStatus.OK);

    }
}
