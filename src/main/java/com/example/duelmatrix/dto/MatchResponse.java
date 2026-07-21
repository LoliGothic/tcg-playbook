package com.example.duelmatrix.dto;

// 日時を扱うためのクラス
import java.time.LocalDateTime;

/**
 * 対戦記録をクライアントへ返すためのレスポンスDTO。
 */
public class MatchResponse {

    // 対戦記録のID
    private final Long id;

    // 勝者のアーキタイプID
    private final Long winnerArchetypeId;

    // 勝者のアーキタイプ名
    private final String winnerArchetypeName;

    // 敗者のアーキタイプID
    private final Long loserArchetypeId;

    // 敗者のアーキタイプ名
    private final String loserArchetypeName;

    // 勝者が先攻か後攻かを表す値
    private final String playOrder;

    // 対戦に関するメモ
    private final String memo;

    // 対戦を行った日時
    private final LocalDateTime playedAt;

    // 対戦記録が登録された日時
    private final LocalDateTime createdAt;

    /**
     * 各項目を受け取ってレスポンスDTOを生成する。
     */
    public MatchResponse(
            Long id,
            Long winnerArchetypeId,
            String winnerArchetypeName,
            Long loserArchetypeId,
            String loserArchetypeName,
            String playOrder,
            String memo,
            LocalDateTime playedAt,
            LocalDateTime createdAt) {

        // 引数で受け取った値を各フィールドに設定する
        this.id = id;
        this.winnerArchetypeId = winnerArchetypeId;
        this.winnerArchetypeName = winnerArchetypeName;
        this.loserArchetypeId = loserArchetypeId;
        this.loserArchetypeName = loserArchetypeName;
        this.playOrder = playOrder;
        this.memo = memo;
        this.playedAt = playedAt;
        this.createdAt = createdAt;
    }

    // 対戦記録のIDを取得する
    public Long getId() {
        return id;
    }

    // 勝者のアーキタイプIDを取得する
    public Long getWinnerArchetypeId() {
        return winnerArchetypeId;
    }

    // 勝者のアーキタイプ名を取得する
    public String getWinnerArchetypeName() {
        return winnerArchetypeName;
    }

    // 敗者のアーキタイプIDを取得する
    public Long getLoserArchetypeId() {
        return loserArchetypeId;
    }

    // 敗者のアーキタイプ名を取得する
    public String getLoserArchetypeName() {
        return loserArchetypeName;
    }

    // 先攻・後攻の情報を取得する
    public String getPlayOrder() {
        return playOrder;
    }

    // 対戦に関するメモを取得する
    public String getMemo() {
        return memo;
    }

    // 対戦日時を取得する
    public LocalDateTime getPlayedAt() {
        return playedAt;
    }

    // 対戦記録の登録日時を取得する
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}