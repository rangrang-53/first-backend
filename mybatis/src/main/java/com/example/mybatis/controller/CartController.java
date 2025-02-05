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
                                 HttpServletRequest request){
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userUid") != null){

            int userUid = (int) session.getAttribute("userUid");
            UserDTO userDTO = new UserDTO();
            userDTO.setUid(userUid);


            cartDTO.setUserDTO(userDTO);

            cartMapper.addToCart(cartDTO);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            UserDTO userDTO = new UserDTO();
            userDTO.setUid(-1);
            cartDTO.setUserDTO(userDTO);
            cartMapper.addToCart(cartDTO);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        if(session == null || session.getAttribute("userUid") == null){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } else {
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }
}
