package com.example.duelmatrix.pattern.command;

/**
 * Command パターンの共通インターフェース．
 *
 * <p>アプリの主要操作（アーキタイプ作成・対戦記録作成・マトリクス生成）を，
 * この 1 つの {@code execute()} で扱えるようにする．具体的なコマンドは各担当が実装する
 * （B: CreateArchetypeCommand，C: CreateMatchCommand，D: GenerateMatrixCommand）．</p>
 *
 * <p>戻り値の型を型パラメータ {@code T} にすることで，操作ごとに異なる結果型を
 * 型安全に返せる．{@link CommandInvoker} を通じてポリモーフィックに実行される．</p>
 *
 * @param <T> 操作の実行結果の型
 */
public interface Command<T> {

    /** 操作を実行し，その結果を返す． */
    T execute();
}
