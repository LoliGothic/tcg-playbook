package com.example.duelmatrix.exception;

/**
 * Service 層などで検出した入力値・業務ルールのバリデーションエラーを表す例外．
 *
 * <p>DTO の {@code @Valid} では表現しにくい業務ルール（例: 勝者 ID と敗者 ID が同一，
 * enum に無い値など）を Service で検出したときに投げる．
 * {@code GlobalExceptionHandler} が捕捉して 400 / VALIDATION_ERROR に整形するため，
 * Service は HTTP ステータスや {@code ErrorResponse} を知らずに済む（責務分離）．</p>
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
