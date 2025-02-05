package com.example.mybatis.mybatis;

import com.example.mybatis.dto.ReviewDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReviewMapper {
    void saveReview(ReviewDTO reviewDTO);
    List<ReviewDTO> getReviewsByProduct(int uid);
}
