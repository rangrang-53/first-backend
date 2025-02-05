package com.example.mybatis.controller;


import com.example.mybatis.dto.UserDTO;
import com.example.mybatis.mybatis.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
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
    public ResponseEntity<?> login(@RequestBody UserDTO userDTO,
                                   HttpServletRequest request){
        UserDTO user = userMapper.findUser(userDTO.getId());

        if(user == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if(user != null && userDTO.getPassword().equals(user.getPassword())){
            if(user != null && userDTO.getPassword().equals(user.getPassword())){
                HttpSession session = request.getSession();
                session.setAttribute("userUid",user.getUid());
                session.setAttribute("auth",user.getAuth());}

            return new ResponseEntity<>(HttpStatus.OK);
        }else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
