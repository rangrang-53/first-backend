package com.example.mybatis.dto;

import java.time.LocalDateTime;

public class QnaDTO {
    private int uid;
    private String title;
    private String content;
    private String category;
    private Integer password;
    private LocalDateTime writeDate;
    private LocalDateTime modifyDate;
    private String deleteyn;
    private String answeryn;
    private String answerContent;
    private LocalDateTime answerDate;
    private UserDTO userDTO;
    private ProductDTO productDTO;

    public UserDTO getUserDTO() {
        return userDTO;
    }

    public void setUserDTO(UserDTO userDTO) {
        this.userDTO = userDTO;
    }

    public ProductDTO getProductDTO() {
        return productDTO;
    }

    public void setProductDTO(ProductDTO productDTO) {
        this.productDTO = productDTO;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getPassword() {
        return password;
    }

    public void setPassword(Integer password) {
        this.password = password;
    }

    public LocalDateTime getWriteDate() {
        return writeDate;
    }

    public void setWriteDate(LocalDateTime writeDate) {
        this.writeDate = writeDate;
    }

    public LocalDateTime getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(LocalDateTime modifyDate) {
        this.modifyDate = modifyDate;
    }

    public String getDeleteyn() {
        return deleteyn;
    }

    public void setDeleteyn(String deleteyn) {
        this.deleteyn = deleteyn;
    }

    public String getAnsweryn() {
        return answeryn;
    }

    public void setAnsweryn(String answeryn) {
        this.answeryn = answeryn;
    }

    public String getAnswerContent() {
        return answerContent;
    }

    public void setAnswerContent(String answerContent) {
        this.answerContent = answerContent;
    }

    public LocalDateTime getAnswerDate() {
        return answerDate;
    }

    public void setAnswerDate(LocalDateTime answerDate) {
        this.answerDate = answerDate;
    }
}
