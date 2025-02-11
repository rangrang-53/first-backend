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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class ReviewController {
    private ReviewMapper reviewMapper;


    @Autowired
    public ReviewController(ReviewMapper reviewMapper) {
        this.reviewMapper = reviewMapper;
    }

    @PostMapping("/review/{productUid}")
    public ResponseEntity<?> writeReview(@RequestBody ReviewDTO reviewDTO,
                                         HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            System.out.println("❌ 세션이 존재하지 않습니다.");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Integer userUid = (Integer) session.getAttribute("userUid");
        if (userUid == null) {
            System.out.println("❌ userUid가 세션에 존재하지 않습니다.");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 세션에 userUid가 없으면 401 응답
        }

        System.out.println("✅ 로그인한 사용자 userUid: " + userUid);


        if (reviewDTO.getTitle() == null || reviewDTO.getContent() == null
        || reviewDTO.getRating() == 0 || reviewDTO.getProductDTO() == null || reviewDTO.getProductDTO().getUid() == 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setUid(userUid);
        reviewDTO.setUserDTO(userDTO);

        System.out.println("작성된 리뷰의 userDTO uid: " + reviewDTO.getUserDTO().getUid());

        System.out.println("Saving Review: " + reviewDTO.getUserDTO().getUid());
        System.out.println("Product ID: " + reviewDTO.getProductDTO().getUid());


        reviewMapper.saveReview(reviewDTO);

        return new ResponseEntity<>(reviewDTO, HttpStatus.OK);

    }

    @GetMapping("/review/{productUid}")
    public ResponseEntity<Map<String, Object>> getReviewsByProduct(
            @PathVariable("productUid") int productUid,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        page = Math.max(1,page);
        int offset = (page - 1) * pageSize;  // offset 계산
        List<ReviewDTO> reviews = reviewMapper.getReviewsByProduct(productUid, offset, pageSize);
        if (reviews == null || reviews.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // 전체 리뷰 수 조회
        int totalReviews = reviewMapper.getReviewCountByProduct(productUid);

        // 페이징 정보 추가하여 응답
        Map<String, Object> response = new HashMap<>();
        response.put("reviews", reviews);
        response.put("total", totalReviews);
        response.put("currentPage", page);
        response.put("totalPages", (int) Math.ceil((double) totalReviews / pageSize));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/review/{productUid}")
    public ResponseEntity<?> editReview(@PathVariable("productUid") int productUid,
                                          @RequestBody ReviewDTO reviewDTO, HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        // 세션 확인
        if (session == null) {
            System.out.println("세션이 존재하지 않음");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 세션이 없으면 401
        }

        Integer userUid = (Integer) session.getAttribute("userUid");
        if (userUid == null) {
            System.out.println("세션에 userUid 없음");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 로그인 안된 상태면 401
        }

        System.out.println("✅ 로그인한 사용자 userUid: " + userUid);

        int page = 1;  // 기본값
        int pageSize = 10;  // 기본값
        int offset = (page - 1) * pageSize;

        List<ReviewDTO> existingReviews = reviewMapper.getReviewsByProduct(productUid, offset, pageSize);
        if (existingReviews == null || existingReviews.isEmpty()) {
            System.out.println("해당 리뷰가 존재하지 않음 (productUid: \" + reviewDTO.getProductDTO().getUid() + \", reviewUid: \" + reviewDTO.getUid() + \")");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 리뷰가 없으면 404
        }

        ReviewDTO existingReview = existingReviews.get(0);  // 첫 번째 리뷰 선택

        // 리뷰 작성자 확인 (로그인된 사용자와 리뷰 작성자 비교)
        if (existingReview.getUserDTO().getUid() != userUid) {
            System.out.println("❌ 리뷰 작성자가 로그인한 사용자와 다름! (로그인한 userUid: " + userUid + " / 리뷰 작성자 UID: " + existingReview.getUserDTO().getUid() + ")");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 리뷰 작성자와 로그인된 사용자가 다르면 401
        }

        // 제목과 내용은 필수 항목
        if (reviewDTO.getTitle() == null || reviewDTO.getContent() == null || reviewDTO.getTitle().isEmpty() || reviewDTO.getContent().isEmpty()) {
            System.out.println("❌ 제목 또는 내용이 비어 있음!");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 제목과 내용이 없으면 400
        }

        // 리뷰 수정
        existingReview.setTitle(reviewDTO.getTitle());
        existingReview.setContent(reviewDTO.getContent());
        existingReview.setRating(reviewDTO.getRating());

        // 이미지도 수정 가능하면 처리

        if (reviewDTO.getRating() != 0) {
            existingReview.setRating(reviewDTO.getRating());
        }
        if (reviewDTO.getImage() != null) {
            existingReview.setImage(reviewDTO.getImage());
        }

        // 로그인된 사용자의 정보로 업데이트
        UserDTO userDTO = new UserDTO();
        userDTO.setUid(userUid); // 세션에서 가져온 userUid로 설정
        existingReview.setUserDTO(userDTO);

        System.out.println("Existing Review: " + existingReview); // ReviewDTO 전체 객체를 출력해서 확인
        System.out.println("Existing Review UserDTO: " + existingReview.getUserDTO());
        System.out.println("Existing Review UserDTO UID: " + existingReview.getUserDTO().getUid());


        reviewMapper.updateReview(existingReview);
        System.out.println("✅ 리뷰 수정 완료!");


        return new ResponseEntity<>(HttpStatus.OK); // 수정 성공 시 200 반환
    }

    @DeleteMapping("/review/{productUid}")
    public ResponseEntity<?> removeReview(@PathVariable("productUid") int productUid,
                                          HttpServletRequest request){
        HttpSession session = request.getSession(false);

        if(session == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Integer userUid = (Integer) session.getAttribute("userUid");
        if (userUid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 페이징을 위한 기본값 설정
        int page = 1;  // 기본값
        int pageSize = 10;  // 기본값
        int offset = (page - 1) * pageSize;

        // 해당 리뷰를 가져옴 (productUid와 uid를 함께 사용하여 리뷰를 확인)
        List<ReviewDTO> existingReviews = reviewMapper.getReviewsByProduct(productUid, offset, pageSize);
        if (existingReviews == null || existingReviews.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();  // 리뷰가 없으면 404
        }

        // List에서 첫 번째 리뷰 가져오기
        ReviewDTO review = existingReviews.get(0);

        // 리뷰 작성자 확인 (로그인된 사용자와 리뷰 작성자 비교)
        if (review.getUserDTO().getUid() != userUid) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();  // 리뷰 작성자와 다르면 403
        }

        // 리뷰 삭제
        reviewMapper.deleteReview(productUid);
        return ResponseEntity.status(HttpStatus.OK).build();  // 성공 시 200 반환
    }




}