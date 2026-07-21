package com.example.duelmatrix.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * アーキタイプ（デッキの種類）を表す Entity．
 *
 * <p>カプセル化のため，フィールドはすべて {@code private} とし，外部からは getter で読み取る．
 * 生成は {@code ArchetypeFactory}（B 担当）経由で行う想定で，無秩序な setter は用意しない．</p>
 */
@Entity
@Table(name = "archetypes")
public class Archetype {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** JPA が利用するためのデフォルトコンストラクタ（直接使用しない）． */
    protected Archetype() {
    }

    /**
     * 新規アーキタイプを生成する．
     *
     * @param name      アーキタイプ名
     * @param createdAt 登録日時
     */
    public Archetype(String name, LocalDateTime createdAt) {
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
