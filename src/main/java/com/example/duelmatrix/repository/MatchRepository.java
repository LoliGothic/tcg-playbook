package com.example.duelmatrix.repository;

import com.example.duelmatrix.domain.MatchRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 対戦記録の永続化を担う Repository．
 *
 * <p>基本 CRUD に加え，履歴を対戦日時の新しい順で取得するメソッドを提供する．
 * C（履歴表示）と D（マトリクス集計）が使う．</p>
 */
public interface MatchRepository extends JpaRepository<MatchRecord, Long> {

    /** 対戦日時の降順（新しい順）で全件取得する． */
    List<MatchRecord> findAllByOrderByPlayedAtDesc();
}
