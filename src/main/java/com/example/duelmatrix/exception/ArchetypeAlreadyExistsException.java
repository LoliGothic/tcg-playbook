package com.example.duelmatrix.exception;

/**
 * 同名のアーキタイプが既に存在するときに投げる例外．
 *
 * <p>主に B（アーキタイプ登録）で，大文字小文字を無視した重複を検出した場合に使う．
 * {@code GlobalExceptionHandler} が捕捉して 409 / ARCHETYPE_ALREADY_EXISTS に整形する．</p>
 */
public class ArchetypeAlreadyExistsException extends RuntimeException {

    public ArchetypeAlreadyExistsException() {
        super("同名のアーキタイプが既に存在します．");
    }

    public ArchetypeAlreadyExistsException(String message) {
        super(message);
    }
}
