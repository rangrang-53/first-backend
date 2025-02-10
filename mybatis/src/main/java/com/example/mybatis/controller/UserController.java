package com.example.mybatis.controller;


import com.example.mybatis.dto.UserDTO;
import com.example.mybatis.mybatis.UserMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class UserController {
    private UserMapper userMapper;

    @Autowired
    public UserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @PostMapping("/signup")
    public void signup(@RequestBody UserDTO userDTO){
        userMapper.saveUser(userDTO);
    }

    @PostMapping("/login")
    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    public ResponseEntity<?> login(@RequestBody UserDTO userDTO,
                                   HttpServletRequest request,
                                   HttpServletResponse response){
        try {
            UserDTO user = userMapper.findUser(userDTO.getId());

            if(user == null || !userDTO.getPassword().equals(user.getPassword())){
                Cookie cookie = new Cookie("JSESSIONID", null);
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);


                return new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED); // 좀 더 구체적인 메시지 제공
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("userUid", user.getUid());
            session.setAttribute("auth", user.getAuth());

            Cookie cookie = new Cookie("JSESSIONID", session.getId());
            cookie.setHttpOnly(true);
            cookie.setSecure(false);  // HTTPS에서만 쿠키 전송 (필요 시 활성화)
            cookie.setMaxAge(30 * 60); // 세션 타임아웃 (30분)
            cookie.setPath("/");

            response.addCookie(cookie);
            response.setHeader("Set-Cookie", "JSESSIONID=" + session.getId() + "; Path=/; HttpOnly; Secure; SameSite=None; Max-Age=" + (30 * 60));

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(); // 예외 로그 출력
            return new ResponseEntity<>("An error occurred", HttpStatus.INTERNAL_SERVER_ERROR); // 500 상태 반환
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response){
        HttpSession session = request.getSession(false);
        if(session != null){
            session.invalidate();

            Cookie cookie = new Cookie("JSESSIONID", null);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
