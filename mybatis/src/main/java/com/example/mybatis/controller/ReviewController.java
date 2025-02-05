package com.example.mybatis.controller;

import com.example.mybatis.dto.ReviewDTO;
import com.example.mybatis.dto.UserDTO;
import com.example.mybatis.mybatis.ReviewMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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

    @PostMapping("/review")
    public ResponseEntity<?> writeReview(@RequestBody ReviewDTO reviewDTO,
                                         HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (reviewDTO.getTitle() == null || reviewDTO.getContent() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setUid((int) session.getAttribute("userUid"));
        reviewDTO.setUserDTO(userDTO);
        reviewMapper.saveReview(reviewDTO);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @GetMapping("/review/{productDTO.uid}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByProduct(@PathVariable int productUid) {
        List<ReviewDTO> reviews = reviewMapper.getReviewsByProduct(productUid);
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }
}