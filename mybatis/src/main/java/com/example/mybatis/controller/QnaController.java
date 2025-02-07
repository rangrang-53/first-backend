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

import java.util.List;

@RestController
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

    @GetMapping("/qna/{productUid}/{uid}")
    public ResponseEntity<?> getQnaByProduct(
            @PathVariable("productUid") int productUid,
            @PathVariable("uid") int uid,
            @RequestParam(value = "password", required = false) String password,
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        String userAuth = (session != null) ? (String) session.getAttribute("auth") :null;

        List<QnaDTO> qnaList = qnaMapper.getqnaByProduct(productUid, uid);

        if (qnaList == null || qnaList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        QnaDTO qna = qnaList.get(0);

        // ✅ 관리자(role_admin)는 비밀번호 없이 접근 가능
        if ("role_admin".equals(userAuth)) {
            System.out.println("✅ 관리자 접근: 비밀번호 검사 생략");
            return ResponseEntity.ok(qna);
        }

        // ✅ 디버깅용 출력
        System.out.println("🔍 DB에서 가져온 QnA 비밀번호: " + qna.getPassword());
        System.out.println("🔍 사용자가 입력한 비밀번호 (쿼리 파라미터): " + password);

        // ✅ 비밀번호가 설정된 QnA인지 확인
        if (qna.getPassword() != null) {
            try {
                Integer inputPassword = Integer.parseInt(password); // 🔥 String → Integer 변환
                System.out.println("🔍 변환된 비밀번호(Integer): " + inputPassword);

                if (!inputPassword.equals(qna.getPassword())) {
                    System.out.println("❌ 비밀번호가 일치하지 않습니다.");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("비밀번호가 일치하지 않습니다.");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ 올바르지 않은 비밀번호 형식입니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("비밀번호는 숫자로 입력해야 합니다.");
            }
        }



        // ✅ 비밀번호가 맞거나 비밀번호가 없는 경우, QnA 데이터 반환
        return ResponseEntity.ok(qna);
    }
}

