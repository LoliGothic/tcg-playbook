package com.example.duelmatrix.repository;

import com.example.duelmatrix.domain.Archetype;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * アーキタイプの永続化を担う Repository．
 *
 * <p>{@link JpaRepository} を継承することで，基本的な CRUD（save / findAll / findById /
 * existsById / findAllById など）が自動的に使える．メソッド名からクエリが導出される．</p>
 *
 * <p>B・C・D 共通の土台．とくに C（対戦記録）はアーキタイプの存在確認・名前引きに，
 * D（マトリクス）は一覧取得に，この Repository を直接使う．</p>
 */
public interface ArchetypeRepository extends JpaRepository<Archetype, Long> {

    /** 同名（大文字小文字を無視）のアーキタイプが既に存在するかを判定する． */
    boolean existsByNameIgnoreCase(String name);

    /** 同名（大文字小文字を無視）のアーキタイプを取得する． */
    Optional<Archetype> findByNameIgnoreCase(String name);
}
