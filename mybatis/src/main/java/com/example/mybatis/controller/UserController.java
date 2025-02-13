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
            // id와 password를 findUserByIdAndPassword로 전달
            UserDTO user = userMapper.findUserByIdAndPassword(userDTO.getId(), userDTO.getPassword());

            if(user == null){
                // 인증 실패 처리
                return new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED);
            }

            // 인증 성공 처리
            HttpSession session = request.getSession(true);
            session.setAttribute("userUid", user.getUid());
            session.setAttribute("userId", user.getId());
            session.setAttribute("auth", user.getAuth());

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("An error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
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

    @GetMapping("/check-login")
    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    public ResponseEntity<?> checkLoginStatus(HttpServletRequest request) {
        System.out.println("Request URL: " + request.getRequestURL()); // 로그로 요청 URL 확인
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userUid") != null) {
            return new ResponseEntity<>(new LoginStatus(true), HttpStatus.OK);
        }
        return new ResponseEntity<>(new LoginStatus(false), HttpStatus.OK);
    }


    @GetMapping("/user-info")
    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    public ResponseEntity<?> getUserInfo(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userUid") == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Integer userUid = (Integer) session.getAttribute("userUid");
        UserDTO user = userMapper.findUserByUid(userUid);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    public static class LoginStatus {
        private boolean loggedIn;

        public LoginStatus(boolean loggedIn) {
            this.loggedIn = loggedIn;
        }

        public boolean isLoggedIn() {
            return loggedIn;
        }

        public void setLoggedIn(boolean loggedIn) {
            this.loggedIn = loggedIn;
        }
    }


}
