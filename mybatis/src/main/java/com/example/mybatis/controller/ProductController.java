package com.example.mybatis.controller;

import com.example.mybatis.dto.ProductDTO;
import com.example.mybatis.dto.ProductImageDTO;
import com.example.mybatis.mybatis.ProductMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductController {

    private ProductMapper productMapper;

    @Autowired
    public ProductController(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @PostMapping("/product")
    public ResponseEntity<?> writeProduct(@RequestBody ProductDTO productDTO, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("세션이 존재하지 않습니다.");
        }

        Integer userUid = (Integer) session.getAttribute("userUid");
        String userAuth = (String) session.getAttribute("auth");

        if (userUid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인되지 않았습니다.");
        }

        if (!"role_admin".equals(userAuth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("권한이 없습니다.");
        }

        // ✅ 카테고리가 존재하지 않으면 새로 추가
        if (productDTO.getCategoryDTO() != null && productDTO.getCategoryDTO().getUid() == 0) {
            try {
                productMapper.saveCategory(productDTO.getCategoryDTO());

                // ✅ 새로 추가된 카테고리의 UID가 설정되었는지 확인
                if (productDTO.getCategoryDTO().getUid() == 0) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("카테고리 저장 실패");
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("카테고리 저장 중 오류 발생: " + e.getMessage());
            }
        }

        // ✅ 먼저 상품을 저장하고, 생성된 UID를 productDTO에 설정
        try {
            productMapper.saveProduct(productDTO);
            System.out.println("Saved productDTO: " + productDTO);
            System.out.println("Generated productUID: " + productDTO.getUid());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상품 저장 중 오류 발생: " + e.getMessage());
        }

        // ✅ 상품 저장 후 생성된 UID가 있는지 확인
        if (productDTO.getUid() == 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상품 저장 실패: UID 생성 안됨");
        }

        // ✅ 이미지 등록 (상품 UID 필요하므로 상품 저장 후 진행)
        List<ProductImageDTO> images = productDTO.getImages();
        if (images != null && !images.isEmpty()) {

            System.out.println("Product DTO: " + productDTO);
            System.out.println("Product UID: " + productDTO.getUid());
            System.out.println("Images: " + images);

        }
        return ResponseEntity.status(HttpStatus.CREATED).body("상품 등록 성공");
    }
}