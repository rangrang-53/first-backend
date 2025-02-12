package com.example.mybatis.mybatis;


import com.example.mybatis.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    void saveUser(UserDTO userDTO);
    UserDTO findUser(String id);
    UserDTO findUserByIdAndPassword(@Param("id") String id, @Param("password") String password);
}
