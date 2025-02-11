package com.example.mybatis.controller;

import com.example.mybatis.dto.QnaDTO;
import com.example.mybatis.dto.ReviewDTO;
import com.example.mybatis.dto.UserDTO;
import com.example.mybatis.mybatis.QnaMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class QnaController {

    private QnaMapper qnaMapper;

    @Autowired
    public QnaController(QnaMapper qnaMapper) {
        this.qnaMapper = qnaMapper;
    }

    @PostMapping("/qna/{productUid}")
    public ResponseEntity<?> writeQna(@RequestBody QnaDTO qnaDTO,
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


        if (qnaDTO.getTitle() == null || qnaDTO.getContent() == null
                || qnaDTO.getCategory() == null || qnaDTO.getProductDTO() == null || qnaDTO.getProductDTO().getUid() == 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

// ✅ 비밀번호가 선택 사항이므로, null 이거나 빈 값이면 저장하지 않음
        if (qnaDTO.getPassword() != null) {
            System.out.println("🔒 QnA 비밀번호가 설정되었습니다." + qnaDTO.getPassword());
        } else {
            System.out.println("⚠️ 비밀번호가 설정되지 않았습니다. 공개 질문으로 처리됩니다.");
            qnaDTO.setPassword(null); // null 로 설정하여 저장
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setUid(userUid);
        qnaDTO.setUserDTO(userDTO);

        System.out.println("작성된 리뷰의 userDTO uid: " + qnaDTO.getUserDTO().getUid());

        System.out.println("Saving Review: " + qnaDTO.getUserDTO().getUid());
        System.out.println("Product ID: " + qnaDTO.getProductDTO().getUid());


        qnaMapper.saveQna(qnaDTO);

        return new ResponseEntity<>(HttpStatus.OK);

    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/qna/{productUid}")
    public ResponseEntity<Map<String, Object>> getQnaByProduct(
            @PathVariable("productUid") int productUid,
            @RequestParam(value = "page", defaultValue = "1") int page, // 페이지 번호
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, // 페이지 크기

            HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        String userAuth = (session != null) ? (String) session.getAttribute("auth") :null;

        page = Math.max(1,page);
        int offset = (page - 1) * pageSize;
        List<QnaDTO> qnaList = qnaMapper.getqnaByProduct(productUid, offset, pageSize);

        if (qnaList == null || qnaList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        int totalQnas = qnaMapper.getQnaCountByProduct(productUid);


        Map<String, Object> response = new HashMap<>();
        response.put("uid", productUid);
        response.put("total", totalQnas);
        response.put("currentPage", page);
        response.put("totalPages", (int) Math.ceil((double) totalQnas / pageSize));

        // ✅ 관리자(role_admin)는 비밀번호 없이 접근 가능
        if ("role_admin".equals(userAuth)) {
            response.put("qnas", qnaList);
            return ResponseEntity.ok(response);
        }

        List<QnaDTO> filteredQnaList =new ArrayList<>();


        for (QnaDTO qna : qnaList) {
            if (qna.getPassword() == null) {
                // 🔓 비밀번호 없는 QnA는 바로 추가
                filteredQnaList.add(qna);
            } else {
                QnaDTO protectedQna = new QnaDTO();
                protectedQna.setUid(qna.getUid());
                protectedQna.setTitle("🔒 비공개 질문입니다.");
                protectedQna.setContent("이 질문의 상세 내용을 보려면 비밀번호를 입력하세요.");
                filteredQnaList.add(protectedQna);
            }
        }
        response.put("qnas", filteredQnaList);
        return ResponseEntity.ok(response);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/qna/{qnaUid}/verify")
    public ResponseEntity<?> verifyQnaPassword(
            @PathVariable("qnaUid") int qnaUid,
            @RequestBody Map<String, String> request) {

        String inputPassword = request.get("password");
        QnaDTO qna = qnaMapper.findQnaByUid(qnaUid);

        if (qna == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("QnA가 존재하지 않습니다.");
        }

        // 비밀번호가 없는 Q&A는 비밀번호 검증 없이 바로 공개
        if (qna.getPassword() == null) {
            return ResponseEntity.ok(qna);
        }

        // 비밀번호 비교 (문자열 비교; 해시를 사용한다면 해시값 비교)
        if (qna.getPassword().equals(inputPassword)) {
            return ResponseEntity.ok(qna);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("비밀번호가 일치하지 않습니다.");
        }
    }

}

