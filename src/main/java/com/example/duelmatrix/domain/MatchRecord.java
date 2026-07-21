package com.example.duelmatrix.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 1 回の対戦記録を表す Entity（勝者記録モデル）．
 *
 * <p>DB テーブル名は {@code matches}．本アプリでは「勝った試合」だけを登録する．
 * すなわち 1 試合につき 1 レコードを，勝者・敗者のアーキタイプとして保存する
 * （敗北・引き分けは登録しない）．これにより，同じ試合を両者の視点で二重登録することを防ぐ．</p>
 *
 * <p>アーキタイプは ID（Long）で保持する（最小構成のため関連エンティティにはしない）．
 * {@code playOrder} は「勝者が先攻／後攻だったか」を表し，enum を {@code STRING} で永続化する．</p>
 */
@Entity
@Table(name = "matches")
public class MatchRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "winner_archetype_id", nullable = false)
    private Long winnerArchetypeId;

    @Column(name = "loser_archetype_id", nullable = false)
    private Long loserArchetypeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "play_order", nullable = false, length = 10)
    private PlayOrder playOrder;

    @Column(length = 500)
    private String memo;

    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** JPA が利用するためのデフォルトコンストラクタ（直接使用しない）． */
    protected MatchRecord() {
    }

    /**
     * 新規対戦記録を生成する．
     *
     * @param winnerArchetypeId 勝者のアーキタイプ ID
     * @param loserArchetypeId  敗者のアーキタイプ ID
     * @param playOrder         勝者の先攻後攻
     * @param memo              メモ（null 可）
     * @param playedAt          対戦日時
     * @param createdAt         登録日時
     */
    public MatchRecord(Long winnerArchetypeId,
                       Long loserArchetypeId,
                       PlayOrder playOrder,
                       String memo,
                       LocalDateTime playedAt,
                       LocalDateTime createdAt) {
        this.winnerArchetypeId = winnerArchetypeId;
        this.loserArchetypeId = loserArchetypeId;
        this.playOrder = playOrder;
        this.memo = memo;
        this.playedAt = playedAt;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getWinnerArchetypeId() {
        return winnerArchetypeId;
    }

    public Long getLoserArchetypeId() {
        return loserArchetypeId;
    }

    public PlayOrder getPlayOrder() {
        return playOrder;
    }

    public String getMemo() {
        return memo;
    }

    public LocalDateTime getPlayedAt() {
        return playedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
