package com.example.duelmatrix.controller;

import com.example.duelmatrix.dto.MatchCreateRequest;
import com.example.duelmatrix.dto.MatchResponse;
import com.example.duelmatrix.pattern.command.CommandInvoker;
import com.example.duelmatrix.pattern.command.CreateMatchCommand;
import com.example.duelmatrix.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 対戦記録APIの入口となるController。
 */
@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;
    private final CommandInvoker commandInvoker;

    public MatchController(
            MatchService matchService,
            CommandInvoker commandInvoker) {

        this.matchService = matchService;
        this.commandInvoker = commandInvoker;
    }

    /**
     * GET /api/matches
     * 登録されている対戦記録を日時降順で取得する。
     */
    @GetMapping
    public ResponseEntity<List<MatchResponse>> getMatches() {
        List<MatchResponse> responses =
                matchService.getMatches();

        return ResponseEntity.ok(responses);
    }

    /**
     * POST /api/matches
     * 新しい対戦記録を登録する。
     */
    @PostMapping
    public ResponseEntity<MatchResponse> createMatch(
            @Valid @RequestBody MatchCreateRequest request) {

        CreateMatchCommand command =
                new CreateMatchCommand(
                        matchService,
                        request
                );

        MatchResponse response =
                commandInvoker.invoke(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}