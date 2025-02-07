package com.example.mybatis.controller;

import com.example.mybatis.dto.PaymentDTO;
import com.example.mybatis.mybatis.PaymentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
public class PaymentController {

    private PaymentMapper paymentMapper;

    @Autowired
    public PaymentController(PaymentMapper paymentMapper) {
        this.paymentMapper = paymentMapper;
    }

    @PostMapping("/payment/process")
    public ResponseEntity<?> processPayment (@RequestBody PaymentDTO paymentDTO){
        try {
            paymentDTO.setPaidDate(LocalDateTime.now());
            paymentDTO.setPaymentStatus("paid");
            paymentMapper.savePayment(paymentDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body("결제가 완료되었습니다.");
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("결제 처리 중 오류 발생");
        }
    }

    @GetMapping("/payment/status/{orderUid}")
    public ResponseEntity<PaymentDTO> getPaymentStatus (@PathVariable int orderUid) {
        PaymentDTO paymentDTO = paymentMapper.getPaymentByOrderUid(orderUid);

        if (paymentDTO == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(paymentDTO);
    }

    @PutMapping("/payment/status")
    public ResponseEntity<?> updatePaymentStatus(@RequestBody PaymentDTO paymentDTO) {
        try {
            paymentMapper.updatePaymentStatus(paymentDTO);
            return ResponseEntity.ok("결제 상태가 업데이트되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("결제 상태 업데이트 중 오류가 발생했습니다.");
        }
    }
}
