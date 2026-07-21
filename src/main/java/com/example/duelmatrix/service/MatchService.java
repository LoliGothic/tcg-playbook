package com.example.duelmatrix.service;

// ドメインクラス
import com.example.duelmatrix.domain.Archetype;
import com.example.duelmatrix.domain.MatchRecord;

// リクエスト・レスポンス用DTO
import com.example.duelmatrix.dto.MatchCreateRequest;
import com.example.duelmatrix.dto.MatchResponse;

// 独自例外クラス
import com.example.duelmatrix.exception.ArchetypeNotFoundException;
import com.example.duelmatrix.exception.ValidationException;

// 対戦記録を生成するFactory
import com.example.duelmatrix.pattern.factory.MatchRecordFactory;

// データベース操作を行うRepository
import com.example.duelmatrix.repository.ArchetypeRepository;
import com.example.duelmatrix.repository.MatchRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

// 対戦記録に関する処理を担当するService
@Service

// 通常は読み取り専用のトランザクションとして実行する
@Transactional(readOnly = true)
public class MatchService {

    // 対戦記録を操作するRepository
    private final MatchRepository matchRepository;

    // アーキタイプを操作するRepository
    private final ArchetypeRepository archetypeRepository;

    // 対戦記録を生成するFactory
    private final MatchRecordFactory matchRecordFactory;

    // 必要なオブジェクトをDIによって受け取る
    public MatchService(
            MatchRepository matchRepository,
            ArchetypeRepository archetypeRepository,
            MatchRecordFactory matchRecordFactory) {

        this.matchRepository = matchRepository;
        this.archetypeRepository = archetypeRepository;
        this.matchRecordFactory = matchRecordFactory;
    }

    /**
     * 対戦履歴を新しい順で取得する。
     */
    public List<MatchResponse> getMatches() {

        // 対戦日時の新しい順で全対戦記録を取得する
        List<MatchRecord> matches =
                matchRepository.findAllByOrderByPlayedAtDesc();

        // 対戦記録がない場合は空のリストを返す
        if (matches.isEmpty()) {
            return List.of();
        }

        // 対戦で使用されているアーキタイプIDを重複なく集める
        Set<Long> archetypeIds = new LinkedHashSet<>();

        for (MatchRecord match : matches) {
            archetypeIds.add(
                    match.getWinnerArchetypeId()
            );
            archetypeIds.add(
                    match.getLoserArchetypeId()
            );
        }

        // アーキタイプを一括取得し、IDをキーとするMapに保存する
        Map<Long, Archetype> archetypeMap =
                new HashMap<>();

        archetypeRepository.findAllById(archetypeIds)
                .forEach(archetype ->
                        archetypeMap.put(
                                archetype.getId(),
                                archetype
                        )
                );

        // クライアントへ返すレスポンス一覧
        List<MatchResponse> responses =
                new ArrayList<>(matches.size());

        for (MatchRecord match : matches) {

            // 勝者のアーキタイプを取得する
            Archetype winner = archetypeMap.get(
                    match.getWinnerArchetypeId()
            );

            // 敗者のアーキタイプを取得する
            Archetype loser = archetypeMap.get(
                    match.getLoserArchetypeId()
            );

            // 勝者のアーキタイプが存在しない場合
            if (winner == null) {
                throw new ArchetypeNotFoundException(
                        "勝者のアーキタイプが存在しません。id="
                                + match.getWinnerArchetypeId()
                );
            }

            // 敗者のアーキタイプが存在しない場合
            if (loser == null) {
                throw new ArchetypeNotFoundException(
                        "敗者のアーキタイプが存在しません。id="
                                + match.getLoserArchetypeId()
                );
            }

            // EntityをレスポンスDTOに変換して追加する
            responses.add(
                    toResponse(match, winner, loser)
            );
        }

        return responses;
    }

    /**
     * 対戦記録を新規登録する。
     */
    @Transactional
    public MatchResponse createMatch(
            MatchCreateRequest request) {

        // リクエスト内容を検証する
        validateRequest(request);

        // 勝者のアーキタイプを取得する
        Archetype winner = archetypeRepository
                .findById(
                        request.getWinnerArchetypeId()
                )
                .orElseThrow(() ->
                        new ArchetypeNotFoundException(
                                "勝者のアーキタイプが存在しません。id="
                                        + request.getWinnerArchetypeId()
                        )
                );

        // 敗者のアーキタイプを取得する
        Archetype loser = archetypeRepository
                .findById(
                        request.getLoserArchetypeId()
                )
                .orElseThrow(() ->
                        new ArchetypeNotFoundException(
                                "敗者のアーキタイプが存在しません。id="
                                        + request.getLoserArchetypeId()
                        )
                );

        MatchRecord matchRecord;

        try {
            // Factoryを使用して対戦記録を生成する
            matchRecord =
                    matchRecordFactory.create(request);

        } catch (IllegalArgumentException exception) {

            // playOrderの値が不正な場合
            throw new ValidationException(
                    "playOrderはFIRST、SECOND、UNKNOWNの"
                            + "いずれかを指定してください。"
            );
        }

        // 対戦記録をデータベースへ保存する
        MatchRecord savedMatch =
                matchRepository.save(matchRecord);

        // 保存結果をレスポンスDTOへ変換して返す
        return toResponse(savedMatch, winner, loser);
    }

    /**
     * リクエストの業務ルールを検証する。
     */
    private void validateRequest(
            MatchCreateRequest request) {

        // リクエストが存在しない場合
        if (request == null) {
            throw new ValidationException(
                    "リクエストが指定されていません。"
            );
        }

        // 勝者と敗者が同じアーキタイプの場合
        if (Objects.equals(
                request.getWinnerArchetypeId(),
                request.getLoserArchetypeId())) {

            throw new ValidationException(
                    "winnerArchetypeIdとloserArchetypeIdには"
                            + "異なるIDを指定してください。"
            );
        }
    }

    /**
     * EntityをレスポンスDTOへ変換する。
     */
    private MatchResponse toResponse(
            MatchRecord match,
            Archetype winner,
            Archetype loser) {

        return new MatchResponse(
                match.getId(),
                match.getWinnerArchetypeId(),
                winner.getName(),
                match.getLoserArchetypeId(),
                loser.getName(),
                match.getPlayOrder().name(),
                match.getMemo(),
                match.getPlayedAt(),
                match.getCreatedAt()
        );
    }
}
