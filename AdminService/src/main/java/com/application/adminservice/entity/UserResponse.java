package com.application.adminservice.entity;

import lombok.Data;

import java.util.List;

@Data
public class UserResponse {
    private List<User> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public List<User> getContent() {
        return content;
    }

    public void setContent(List<User> content) {
        this.content = content;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
