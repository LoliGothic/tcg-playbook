package com.example.duelmatrix.pattern.command;

import com.example.duelmatrix.dto.MatchCreateRequest;
import com.example.duelmatrix.dto.MatchResponse;
import com.example.duelmatrix.service.MatchService;

import java.util.Objects;

/**
 * 対戦記録を新規登録するCommand
 */
public class CreateMatchCommand implements Command<MatchResponse> {

    private final MatchService matchService;
    private final MatchCreateRequest request;

    public CreateMatchCommand(
            MatchService matchService,
            MatchCreateRequest request) {

        this.matchService = Objects.requireNonNull(
                matchService,
                "matchService must not be null"
        );

        this.request = Objects.requireNonNull(
                request,
                "request must not be null"
        );
    }

    /**
     * 対戦記録の登録処理を実行
     */
    @Override
    public MatchResponse execute() {
        return matchService.createMatch(request);
    }
}