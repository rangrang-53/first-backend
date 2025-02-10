package com.example.mybatis.controller;

import com.example.mybatis.dto.OrderDTO;
import com.example.mybatis.dto.OrderDetailDTO;
import com.example.mybatis.dto.UserDTO;
import com.example.mybatis.mybatis.OrderMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
public class OrderController {
    private OrderMapper orderMapper;

    @Autowired
    public OrderController(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }


    @PostMapping("/order")
    public ResponseEntity<?> createOrder(@RequestBody OrderDTO orderDTO, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Integer userUid = (session != null) ? (Integer) session.getAttribute("userUid") : null;

        if (userUid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        // 🔹 userDTO 객체가 없는 경우 생성
        if (orderDTO.getUserDTO() == null) {
            orderDTO.setUserDTO(new UserDTO());
        }
        orderDTO.getUserDTO().setUid(userUid);  // 🔹 userUid 설정

        try {
            // 🔹 주문 생성 (자동 생성된 order_uid가 orderDTO.uid에 저장됨)
            orderMapper.createOrder(orderDTO);

            for (OrderDetailDTO orderDetail : orderDTO.getOrderDetails()) {
                orderDetail.setOrderDTO(orderDTO);  // 🔹 orderDTO의 uid 값을 설정
                orderMapper.createOrderDetail(orderDetail);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("주문이 생성되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("주문 생성 실패: " + e.getMessage());
        }
    }




    @GetMapping("/order/list/{userUid}")
    public ResponseEntity<List<OrderDTO>> getUserOrders(@PathVariable int userUid) {
        List<OrderDTO> orders = orderMapper.getOrderByUserUid(userUid);
        return ResponseEntity.ok(orders != null ? orders : Collections.emptyList());
    }


    @PutMapping("/order/update/{orderUid}")
    public ResponseEntity<?> updateOrderStatus(@PathVariable int orderUid, @RequestBody OrderDTO orderDTO) {
        try {
            // 🔹 주문 ID로 해당 주문이 존재하는지 확인
            OrderDTO existingOrder = orderMapper.getOrderByUid(orderUid);
            if (existingOrder == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 주문이 존재하지 않습니다.");
            }

            // 주문 상태 업데이트
            orderDTO.setUid(orderUid);
            orderMapper.updateOrderStatus(orderDTO);
            return ResponseEntity.ok("주문 상태가 성공적으로 업데이트되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("주문 상태 업데이트 실패: " + e.getMessage());
        }
    }
}

