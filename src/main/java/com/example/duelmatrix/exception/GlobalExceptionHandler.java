package com.example.duelmatrix.exception;

import com.example.duelmatrix.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全 API のエラーを共通形式（{@link ErrorResponse}）に整形するグローバル例外ハンドラ．
 *
 * <p>各 Controller / Service は例外を投げるだけでよく，エラー整形の責務を持たない（高凝集）．
 * B・C・D はここで用意した例外を投げるだけで，API_SPEC のエラー JSON が返る．</p>
 *
 * <p>対応するエラーコード（API_SPEC）:
 * <ul>
 *   <li>VALIDATION_ERROR (400) … 入力検証エラー・不正な JSON</li>
 *   <li>ARCHETYPE_NOT_FOUND (404) … アーキタイプ ID が存在しない</li>
 *   <li>ARCHETYPE_ALREADY_EXISTS (409) … 同名アーキタイプが既に存在</li>
 *   <li>INTERNAL_ERROR (500) … 想定外のサーバエラー</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 指定アーキタイプが存在しない → 404 */
    @ExceptionHandler(ArchetypeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ArchetypeNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "ARCHETYPE_NOT_FOUND", ex.getMessage(), req);
    }

    /** 同名アーキタイプが既に存在 → 409 */
    @ExceptionHandler(ArchetypeAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ArchetypeAlreadyExistsException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "ARCHETYPE_ALREADY_EXISTS", ex.getMessage(), req);
    }

    /** @Valid の検証失敗（@RequestBody） → 400．フィールドごとのメッセージをまとめる． */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        if (message.isEmpty()) {
            message = "入力値が不正です．";
        }
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, req);
    }

    /** リクエスト本文が壊れている・enum に無い値など読み取り不能 → 400 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "リクエスト本文の形式が不正です．", req);
    }

    /** 上記以外の想定外エラー → 500 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "予期しないエラーが発生しました．", req);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message, HttpServletRequest req) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message, req.getRequestURI()));
    }
}
