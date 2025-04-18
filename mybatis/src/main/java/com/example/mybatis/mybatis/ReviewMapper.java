package com.example.mybatis.mybatis;

import com.example.mybatis.dto.ReviewDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReviewMapper {
    void saveReview(ReviewDTO reviewDTO);
    List<ReviewDTO> getReviewsByProduct(@Param("productUid") int productUid,
                                        @Param("offset") int offset,
                                        @Param("pageSize") int pageSize);
    int getReviewCountByProduct(@Param("productUid") int productUid);
    ReviewDTO getReviewByUid(@Param("uid") int uid);
    void updateReview(ReviewDTO reviewDTO);
    void deleteReview(int uid);
}
