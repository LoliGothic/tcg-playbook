package com.example.duelmatrix.dto;

import java.time.LocalDateTime;

/**
 * アーキタイプ API のレスポンス DTO．
 */
public class ArchetypeResponse {

    private final Long id;
    private final String name;
    private final LocalDateTime createdAt;

    public ArchetypeResponse(Long id, String name, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
