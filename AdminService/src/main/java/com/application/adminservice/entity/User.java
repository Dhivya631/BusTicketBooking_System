package com.application.adminservice.entity;

import lombok.Data;


@Data
public class User {
    private Long id;
    private String name;
    private String username;
    private String email;
    private String password;
    private Long phoneno;
    private String role="CUSTOMER";
    public User(){}
    public User(Long id,String name,String username,String email,String password,Long phoneno){
        this.id=id;
        this.name=name;
        this.username=username;
        this.email=email;
        this.phoneno=phoneno;
        this.password=password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getPhoneno() {
        return phoneno;
    }

    public void setPhoneno(Long phoneno) {
        this.phoneno = phoneno;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}