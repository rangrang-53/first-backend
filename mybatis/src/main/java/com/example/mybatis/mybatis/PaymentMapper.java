package com.example.mybatis.mybatis;

import com.example.mybatis.dto.PaymentDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentMapper {
    void savePayment(PaymentDTO paymentDTO);
    PaymentDTO getPaymentByOrderUid(int orderUid);
    void updatePaymentStatus(PaymentDTO paymentDTO);
}
