package com.example.mybatis.controller;

import com.example.mybatis.dto.CartDTO;
import com.example.mybatis.dto.ProductDTO;
import com.example.mybatis.dto.UserDTO;
import com.example.mybatis.mybatis.CartMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.sql.In;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CartController {

    private CartMapper cartMapper;

    @Autowired
    public CartController(CartMapper cartMapper) {
        this.cartMapper = cartMapper;
    }

    @PostMapping("/cart")
    public ResponseEntity<?> addCart(@RequestBody CartDTO cartDTO, HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Integer userUid = (session.getAttribute("userUid") != null) ? (Integer) session.getAttribute("userUid") : null;

        if (userUid != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setUid(userUid);
            cartDTO.setUserDTO(userDTO);
            cartDTO.setSessionId(null); // 로그인한 경우 sessionId 필요 없음
        } else {
            cartDTO.setUserDTO(null);
            cartDTO.setSessionId(session.getId()); // 비로그인 사용자는 sessionId로 저장
        }

        cartMapper.addToCart(cartDTO);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/cart")
    public ResponseEntity<List<CartDTO>> viewCart(HttpServletRequest request,
                                                  @RequestHeader(value = "Cookie", required = false) String cookie) {
        HttpSession session = request.getSession(false);
        String sessionId = null;
        Integer userUid = null;

        if(session != null){
            userUid = (Integer) session.getAttribute("userUid");
        }

        if(cookie != null && userUid == null){
            String[] cookies = cookie.split(";");
            for (String c : cookies){
                if (c.trim().startsWith("JSESSIONID=")){
                    sessionId = c.trim().substring("JSESSIONID=".length());
                    break;
                }
            }
        }

        if(userUid != null) {
            List<CartDTO> cartList = cartMapper.getCartProducts(userUid, null);
            if (cartList == null || cartList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(cartList);

        } else if (sessionId != null){
            List<CartDTO> cartList = cartMapper.getCartProductsBySession(sessionId);
            if(cartList == null || cartList.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(cartList);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @DeleteMapping("/cart")
    public ResponseEntity<?> removeCartItem(@RequestBody CartDTO cartDTO,
                                            HttpServletRequest request,
                                            @RequestHeader(value = "Cookie", required = false) String cookie) {
        HttpSession session = request.getSession(false);
        String sessionId = null;
        Integer userUid = null;

        if (session != null) {
            userUid = (Integer) session.getAttribute("userUid");
            sessionId = session.getId();
        }

        if (cookie != null && userUid == null){
            String[] cookies = cookie.split(";");
            for (String c : cookies){
                if (c.trim().startsWith("JSESSIONID=")){
                    sessionId = c.trim().substring("JSESSIONID=".length());
                    break;
                }
            }
        }

        if (sessionId == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("세션이 존재하지 않습니다.");
        }

        if (userUid != null){
            UserDTO userDTO = new UserDTO();
            userDTO.setUid(userUid);
            cartDTO.setUserDTO(userDTO);
            cartDTO.setSessionId(null);
        } else {
            cartDTO.setUserDTO(null);
            cartDTO.setSessionId(sessionId);
        }

        try {
            cartMapper.removeFromCart(cartDTO);  // 카트에서 아이템 삭제
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("카트에서 아이템 삭제 실패: " + e.getMessage());
        }
    }


}
