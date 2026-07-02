package com.example.duelmatrix.dto;

import java.time.LocalDateTime;

/**
 * エラー応答の共通形式（API_SPEC の共通エラーレスポンスに対応）．
 *
 * <p>すべての API はエラー時にこの形で返す．整形は {@code GlobalExceptionHandler} に集約するため，
 * 各 Controller / Service は例外を投げるだけでよい．</p>
 *
 * <pre>
 * { "error": "...", "message": "...", "path": "...", "timestamp": "..." }
 * </pre>
 */
public class ErrorResponse {

    private final String error;
    private final String message;
    private final String path;
    private final LocalDateTime timestamp;

    public ErrorResponse(String error, String message, String path) {
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    /** エラーコード（機械判定用の定数．例: VALIDATION_ERROR）． */
    public String getError() {
        return error;
    }

    /** 画面表示用の説明文． */
    public String getMessage() {
        return message;
    }

    /** エラーが発生したリクエストパス． */
    public String getPath() {
        return path;
    }

    /** エラー発生時刻． */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
