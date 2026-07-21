package com.example.duelmatrix.exception;

/**
 * 指定されたアーキタイプ ID が存在しないときに投げる例外．
 *
 * <p>主に C（対戦記録登録）で，勝者/敗者のアーキタイプが存在しない場合に使う．
 * {@code GlobalExceptionHandler} が捕捉して 404 / ARCHETYPE_NOT_FOUND に整形する．</p>
 */
public class ArchetypeNotFoundException extends RuntimeException {

    public ArchetypeNotFoundException() {
        super("指定されたアーキタイプが存在しません．");
    }

    public ArchetypeNotFoundException(String message) {
        super(message);
    }
}
