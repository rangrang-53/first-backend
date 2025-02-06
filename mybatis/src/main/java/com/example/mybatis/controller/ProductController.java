package com.example.mybatis.controller;

import com.example.mybatis.dto.ProductDTO;
import com.example.mybatis.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {

    @Autowired
    public ProductController() {
    }

    @PostMapping("/product/{uid}")
    public ResponseEntity<?> writeProduct(@RequestBody ProductDTO productDTO,
                                          HttpServletRequest request){
        HttpSession session = request.getSession(false);
        if(session == null){
            System.out.println("세션이 존재하지 않습니다");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Integer userUid = (Integer) session.getAttribute("userUid");
        String userAuth = (String) session.getAttribute("auth");

        if(userUid == null){
            System.out.println("로그인 되지 않았습니다");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if(!"role_admin".equals(userAuth)){
            System.out.println("권한이 없습니다");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        productDTO.setUid(userUid);



        return new ResponseEntity<>("상품 등록 성공",HttpStatus.OK)
    }

}
