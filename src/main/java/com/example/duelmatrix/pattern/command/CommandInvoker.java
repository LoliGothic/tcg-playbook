package com.example.duelmatrix.pattern.command;

import org.springframework.stereotype.Component;

/**
 * {@link Command} を実行する役（Invoker）．
 *
 * <p>Controller は具体的な処理を持たず，Command を組み立ててこの Invoker に渡すだけにする．
 * 実行を 1 か所に集約することで，将来ここにログ・監査・実行時間計測などの横断的な処理を
 * まとめて差し込める（今回の最小構成では素通しで実行するだけ）．</p>
 */
@Component
public class CommandInvoker {

    /**
     * 渡されたコマンドを実行し，その結果を返す．
     *
     * @param command 実行するコマンド
     * @param <T>     コマンドの結果型
     * @return コマンドの実行結果
     */
    public <T> T invoke(Command<T> command) {
        return command.execute();
    }
}
