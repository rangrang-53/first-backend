package com.example.mybatis.mybatis;

import com.example.mybatis.dto.QnaDTO;
import com.example.mybatis.dto.ReviewDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QnaMapper {
    void saveQna(QnaDTO qnaDTO);
    List<QnaDTO> getqnaByProduct(@Param("productUid") int productUid, @Param("uid") int uid);
}
