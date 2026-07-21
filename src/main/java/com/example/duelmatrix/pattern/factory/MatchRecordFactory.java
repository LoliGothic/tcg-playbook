package com.example.duelmatrix.pattern.factory;

import com.example.duelmatrix.domain.MatchRecord;
import com.example.duelmatrix.domain.PlayOrder;
import com.example.duelmatrix.dto.MatchCreateRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MatchCreateRequestからMatchRecordを生成するFactory
 * Serviceが対戦記録登録処理とMatchRecord の作り方を管理する処理
 * の両方担うことを防ぐ
 */
@Component
public class MatchRecordFactory {

    public MatchRecord create(MatchCreateRequest request) {
        PlayOrder playOrder = convertPlayOrder(request.getPlayOrder());

        return new MatchRecord(
                request.getWinnerArchetypeId(),
                request.getLoserArchetypeId(),
                playOrder,
                request.getMemo(),
                request.getPlayedAt(),
                LocalDateTime.now()
        );
    }

    /**
     * 文字列をPlayOrderに変換
     *
     * @throws IllegalArgumentException 定義されていない値の場合
     */
    private PlayOrder convertPlayOrder(String value) {
        if (value == null || value.isBlank()) {
            return PlayOrder.UNKNOWN;
        }

        return PlayOrder.valueOf(value.trim());
    }
}