package com.example.duelmatrix.dto;

// 必須入力を指定するためのアノテーション
import jakarta.validation.constraints.NotNull;

// 文字数の上限を指定するためのアノテーション
import jakarta.validation.constraints.Size;

// 日時を扱うクラス
import java.time.LocalDateTime;

/**
 * 対戦記録を登録するときに受け取るDTO
 */
public class MatchCreateRequest {

    // 勝者のアーキタイプID
    @NotNull
    private Long winnerArchetypeId;

    // 敗者のアーキタイプID
    @NotNull
    private Long loserArchetypeId;

    // 勝者が先攻・後攻のどちらだったか
    private String playOrder;

    // 対戦に関するメモ
    @Size(max = 500)
    private String memo;

    // 対戦を行った日時
    @NotNull
    private LocalDateTime playedAt;

    // JSONからオブジェクトを生成するためのコンストラクタ
    public MatchCreateRequest() {
    }

    // 勝者のアーキタイプIDを取得する
    public Long getWinnerArchetypeId() {
        return winnerArchetypeId;
    }

    // 勝者のアーキタイプIDを設定する
    public void setWinnerArchetypeId(
            Long winnerArchetypeId) {

        this.winnerArchetypeId = winnerArchetypeId;
    }

    // 敗者のアーキタイプIDを取得する
    public Long getLoserArchetypeId() {
        return loserArchetypeId;
    }

    // 敗者のアーキタイプIDを設定する
    public void setLoserArchetypeId(
            Long loserArchetypeId) {

        this.loserArchetypeId = loserArchetypeId;
    }

    // 先攻・後攻の情報を取得する
    public String getPlayOrder() {
        return playOrder;
    }

    // 先攻・後攻の情報を設定する
    public void setPlayOrder(String playOrder) {
        this.playOrder = playOrder;
    }

    // メモを取得する
    public String getMemo() {
        return memo;
    }

    // メモを設定する
    public void setMemo(String memo) {
        this.memo = memo;
    }

    // 対戦日時を取得する
    public LocalDateTime getPlayedAt() {
        return playedAt;
    }

    // 対戦日時を設定する
    public void setPlayedAt(
            LocalDateTime playedAt) {

        this.playedAt = playedAt;
    }
}