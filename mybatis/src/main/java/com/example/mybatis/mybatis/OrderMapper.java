package com.example.mybatis.mybatis;

import com.example.mybatis.dto.OrderDTO;
import com.example.mybatis.dto.OrderDetailDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderMapper {
    void createOrder(OrderDTO orderDTO);
    void createOrderDetail(OrderDetailDTO orderDetailDTO);
    List<OrderDTO> getOrderByUserUid(int userUid);
    void updateOrderStatus(OrderDTO orderDTO);
    OrderDTO getOrderByUid(int orderUid);
}
