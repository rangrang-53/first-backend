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

import java.util.*;

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

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("/qna/{productUid}")
    public ResponseEntity<Map<String, Object>> getQnaByProduct(
            @PathVariable("productUid") int productUid,
            @RequestParam(value = "page", defaultValue = "1") int page, // 페이지 번호
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, // 페이지 크기

            HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        String userAuth = (session != null) ? (String) session.getAttribute("auth") :null;
        Set<Integer> verifiedQnaIds = (session != null) ?
                (Set<Integer>) session.getAttribute("verifiedQnaIds") :
                new HashSet<>();

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
            if (qna.getPassword() == null || verifiedQnaIds.contains(qna.getUid())) {
                // 🔓 비밀번호 없는 QnA는 바로 추가
                filteredQnaList.add(qna);
            } else {
                QnaDTO protectedQna = new QnaDTO();
                protectedQna.setUid(qna.getUid());
                protectedQna.setCategory(qna.getCategory());
                protectedQna.setTitle("🔒 비공개 질문입니다.");
                protectedQna.setContent("이 질문의 상세 내용을 보려면 비밀번호를 입력하세요.");
                protectedQna.setUserDTO(qna.getUserDTO());
                protectedQna.setWriteDate(qna.getWriteDate());
                filteredQnaList.add(protectedQna);
            }
        }
        response.put("qnas", filteredQnaList);
        return ResponseEntity.ok(response);
    }

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @PostMapping("/qna/{qnaUid}/verify")
    public ResponseEntity<?> verifyQnaPassword(
            @PathVariable("qnaUid") int qnaUid,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpServletRequest) {

        String inputPassword = request.get("password");
        QnaDTO qna = qnaMapper.findQnaByUid(qnaUid);

        if (qna == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("QnA가 존재하지 않습니다.");
        }

        // 비밀번호가 없는 Q&A는 비밀번호 검증 없이 바로 공개
        if (qna.getPassword() == null || qna.getPassword().isEmpty()) {
            return ResponseEntity.ok(qna);
        }

        if (!Objects.equals(qna.getPassword(), inputPassword)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("비밀번호가 일치하지 않습니다.");
        }

        // 세션에서 기존의 인증된 질문 리스트를 저장하는 로직 추가
        HttpSession session = httpServletRequest.getSession(true);
        Set<Integer> verifiedQnaIds = (Set<Integer>) session.getAttribute("verifiedQnaIds");

        if (verifiedQnaIds == null) {
            verifiedQnaIds = new HashSet<>();
        }
        verifiedQnaIds.add(qnaUid);
        session.setAttribute("verifiedQnaIds", verifiedQnaIds);
        System.out.println("🔓 비밀번호 인증 완료! 원래 내용 반환: " + qna.getContent());

        return ResponseEntity.ok(qna);
    }

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @DeleteMapping("/qna/{uid}")
    public ResponseEntity<?> deleteQna(@PathVariable("uid") int uid, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Integer userUid = (Integer) session.getAttribute("userUid");
        if (userUid == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // QnA 존재 여부 확인
        QnaDTO qna = qnaMapper.findQnaByUid(uid);
        if (qna == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("삭제할 QnA가 존재하지 않습니다.");
        }

        // 관리자이거나 작성자인 경우 삭제 가능
        String userAuth = (String) session.getAttribute("auth");
        if (!"role_admin".equals(userAuth) && !Objects.equals(qna.getUserDTO().getUid(), userUid)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 권한이 없습니다.");
        }

        // 실제 삭제 처리 (DB에서 삭제 플래그 업데이트)
        qnaMapper.deleteQna(uid);
        return ResponseEntity.ok().body("QnA가 삭제되었습니다.");
    }

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @PatchMapping("/qna/{productUid}/{uid}")
    public ResponseEntity<?> updateQna(
            @PathVariable("uid") int uid,
            @PathVariable("productUid") int productUid,
            @RequestBody QnaDTO updatedQnaDTO,
            HttpServletRequest request) {

        // 세션에서 로그인 사용자 정보 확인
        HttpSession session = request.getSession(false);
        if (session == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Integer userUid = (Integer) session.getAttribute("userUid");
        if (userUid == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 로그인되지 않은 사용자
        }

        // QnA 존재 여부 확인
        QnaDTO existingQna  = qnaMapper.findQnaByUid(uid);
        if (existingQna  == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("QnA가 존재하지 않습니다.");
        }

        // 권한 확인: QnA 작성자이거나 관리자일 때 수정 가능
        String userAuth = (String) session.getAttribute("auth");
        if (!"role_admin".equals(userAuth) && !Objects.equals(existingQna .getUserDTO().getUid(), userUid)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정 권한이 없습니다.");
        }

        if (updatedQnaDTO.getTitle() == null || updatedQnaDTO.getContent() == null || updatedQnaDTO.getTitle().trim().isEmpty() ||
        updatedQnaDTO.getContent().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("수정할 데이터가 부족합니다.");
        }

        if (updatedQnaDTO.getCategory() == null || updatedQnaDTO.getCategory().trim().isEmpty()){
            updatedQnaDTO.setCategory(existingQna .getCategory());
        }

        updatedQnaDTO.setUid(uid);

        try {
            qnaMapper.updateQna(updatedQnaDTO);
            return ResponseEntity.ok("QnA가 성공적으로 수정되었습니다.");  // DB에서 QnA 수정
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
        }


    }



}

