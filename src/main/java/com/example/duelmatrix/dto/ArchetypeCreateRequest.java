package com.example.duelmatrix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * アーキタイプ登録 API のリクエスト DTO．
 */
public class ArchetypeCreateRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    public ArchetypeCreateRequest() {
    }

    public ArchetypeCreateRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
