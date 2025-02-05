package com.example.mybatis.mybatis;


import com.example.mybatis.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    void saveUser(UserDTO userDTO);
    UserDTO findUser(String id);
}
