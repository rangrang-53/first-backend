package com.example.mybatis.controller;

import com.example.mybatis.dto.ReviewDTO;
import com.example.mybatis.dto.UserDTO;
import com.example.mybatis.mybatis.ReviewMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ReviewController {
    private ReviewMapper reviewMapper;


    @Autowired
    public ReviewController(ReviewMapper reviewMapper) {
        this.reviewMapper = reviewMapper;
    }

    @PostMapping("/{productUid}")
    public ResponseEntity<?> writeReview(@RequestBody ReviewDTO reviewDTO,
                                         HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Integer userUid = (Integer) session.getAttribute("userUid");
        if (userUid == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 세션에 userUid가 없으면 401 응답
        }

        if (reviewDTO.getTitle() == null || reviewDTO.getContent() == null
        || reviewDTO.getProductDTO() == null || reviewDTO.getProductDTO().getUid() == 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setUid((int) session.getAttribute("userUid"));
        reviewDTO.setUserDTO(userDTO);
        reviewMapper.saveReview(reviewDTO);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @GetMapping("/{productUid}/{uid}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByProduct(@PathVariable("productUid") int productUid, @PathVariable("uid") int uid) {
        List<ReviewDTO> review = reviewMapper.getReviewsByProduct(productUid,uid);
        if(review == null || review.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return new ResponseEntity<>(review, HttpStatus.OK);
    }

    @PatchMapping("/{productUid}/{uid}")
    public ResponseEntity<?> updateReview(@RequestBody ReviewDTO reviewDTO, HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        // 세션 확인
        if (session == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 세션이 없으면 401
        }

        Integer userUid = (Integer) session.getAttribute("userUid");
        if (userUid == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 로그인 안된 상태면 401
        }

        // 해당 리뷰를 조회 (리뷰가 존재하지 않으면 404 반환)
        List<ReviewDTO> existingReviews = reviewMapper.getReviewsByProduct(reviewDTO.getProductDTO().getUid(), reviewDTO.getUid());
        if (existingReviews == null || existingReviews.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 리뷰가 없으면 404
        }

        ReviewDTO existingReview = existingReviews.get(0);  // 첫 번째 리뷰 선택

        // 리뷰 작성자 확인 (로그인된 사용자와 리뷰 작성자 비교)
        if (existingReview.getUserDTO().getUid() != userUid) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 리뷰 작성자와 로그인된 사용자가 다르면 401
        }

        // 제목과 내용은 필수 항목
        if (reviewDTO.getTitle() == null || reviewDTO.getContent() == null || reviewDTO.getTitle().isEmpty() || reviewDTO.getContent().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 제목과 내용이 없으면 400
        }

        // 리뷰 수정
        existingReview.setTitle(reviewDTO.getTitle());
        existingReview.setContent(reviewDTO.getContent());
        existingReview.setRating(reviewDTO.getRating());

        // 이미지도 수정 가능하면 처리
        if (reviewDTO.getImage() != 0) {
            existingReview.setImage(reviewDTO.getImage());
        }

        // 로그인된 사용자의 정보로 업데이트
        UserDTO userDTO = new UserDTO();
        userDTO.setUid(userUid); // 세션에서 가져온 userUid로 설정
        existingReview.setUserDTO(userDTO);

        reviewMapper.updateReview(existingReview);

        return new ResponseEntity<>(HttpStatus.OK); // 수정 성공 시 200 반환
    }


}