# =====================================================================
# Duel Matrix Dockerfile（multi-stage build）
# ---------------------------------------------------------------------
# ステージ 1 (build):
#   Maven + JDK 17 のイメージでソースをビルドし，実行可能 jar を作る．
# ステージ 2 (runtime):
#   JRE のみの軽量イメージに，ビルド成果物の jar だけをコピーして実行する．
#
# multi-stage build を使う理由:
#   ビルドに必要な Maven や JDK を最終イメージに含めないため，
#   実行用イメージを小さく保てる．
# =====================================================================

# ---- ステージ 1: ビルド ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 依存解決を効かせるため pom.xml を先にコピーする
COPY pom.xml .
COPY src ./src

# テストはビルド時にはスキップ（CI や手元で別途実行する想定）
RUN mvn clean package -DskipTests

# ---- ステージ 2: 実行 ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# ビルドステージで生成された jar だけをコピーする
COPY --from=build /app/target/*.jar app.jar

# Spring Boot の既定ポート
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
