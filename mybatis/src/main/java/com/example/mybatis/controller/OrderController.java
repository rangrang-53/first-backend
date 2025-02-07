package com.example.mybatis.controller;

import com.example.mybatis.dto.OrderDTO;
import com.example.mybatis.dto.OrderDetailDTO;
import com.example.mybatis.mybatis.OrderMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController {
    private OrderMapper orderMapper;

    @Autowired
    public OrderController(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }


    @PostMapping("/order")
    public ResponseEntity<?> createOrder(@RequestBody OrderDTO orderDTO,
                                         HttpServletRequest request){
        HttpSession session = request.getSession(false);
        if (session == null){
            System.out.println("세션 존재 X");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Integer userUid = (Integer) session.getAttribute("userUid");
        if(userUid == null) {
            System.out.println("userUid가 존재하지 않습니다.");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        System.out.println("로그인 한 사용자 uid"+userUid);

        // 로그인 유효성부터 다시!
        try {
            orderMapper.createOrder(orderDTO);
            for (OrderDetailDTO orderDetail : orderDTO.getOrderDetails()) {
                orderDetail.setOrderDTO(orderDTO);
                orderMapper.createOrderDetail(orderDetail); // 주문 상세 항목 생성
            }
            return ResponseEntity.status(HttpStatus.CREATED).body("주문이 생성되었습니다.");
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("주문이 생성되지 않았습니다." +  e.getMessage());
        }
    }

    @GetMapping("/order/list/{userUid}")
    public ResponseEntity<List<OrderDTO>> getUserOrders(@PathVariable int userUid) {
        List<OrderDTO> orders = orderMapper.getOrderByUserUid(userUid);
        if (orders == null || orders.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/order/update/{orderUid}")
    public ResponseEntity<?> updateOrderStatus(@PathVariable int orderUid, @RequestBody OrderDTO orderDTO) {
        try {
            orderDTO.setUid(orderUid);
            orderMapper.updateOrderStatus(orderDTO);
            return ResponseEntity.ok("주문 상태가 성공적으로 업데이트되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("주문 상태 업데이트 실패: " + e.getMessage());
        }
    }
}
