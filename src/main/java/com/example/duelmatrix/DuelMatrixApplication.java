package com.example.duelmatrix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Duel Matrix アプリケーションの起動クラス．
 * ここから Spring Boot が起動し，同一パッケージ配下のコンポーネントを自動検出する．
 */
@SpringBootApplication
public class DuelMatrixApplication {

    public static void main(String[] args) {
        SpringApplication.run(DuelMatrixApplication.class, args);
    }
}
