# Duel Matrix 環境構築ガイド（Docker / Docker Compose）

このドキュメントは，Docker と Docker Compose を使って Duel Matrix を起動する手順です．
開発環境の差（OS・Java のバージョン違いなど）をなくし，全員が同じ状態で動かすために使います．

---

## 1. Docker を使う理由

| 理由 | 説明 |
| --- | --- |
| 環境の統一 | 各自の PC に Java 17 や Maven を個別にインストールしなくても，同じ環境で動く． |
| 手順の単純化 | `docker compose up --build` の 1 コマンドで起動できる． |
| 再現性 | 「自分の PC では動くのに他の人は動かない」という問題を減らせる． |
| 提出のしやすさ | 採点者も同じコマンドで動作確認できる． |

このプロジェクトは H2 Database を Spring Boot アプリ内で動かすため，
**DB 用のコンテナは不要**です．Docker Compose では `app` サービスのみを定義します．

---

## 2. 必要なツール

| ツール | 用途 |
| --- | --- |
| Docker Desktop（または Docker Engine） | コンテナの実行 |
| Docker Compose | 複数コンテナ定義の起動（Docker Desktop に同梱） |

インストール確認:

```bash
docker --version
docker compose version
```

---

## 3. 初回起動手順

リポジトリのルート（`Dockerfile` があるディレクトリ）で実行します．

```bash
docker compose up --build
```

- `--build` を付けると，イメージを作り直してから起動します．
- 初回はビルドに数分かかることがあります（Maven の依存ダウンロードのため）．

起動後，以下の URL を確認します．

| URL | 内容 |
| --- | --- |
| http://localhost:8080 | トップページ |
| http://localhost:8080/api/archetypes | アーキタイプ API（JSON が返る） |
| http://localhost:8080/h2-console | H2 コンソール |

---

## 4. 停止手順

別のターミナルから，またはログを `Ctrl + C` で止めたあとに:

```bash
docker compose down
```

コンテナを停止・削除します．H2 がインメモリの場合，保存したデータは消えます
（デモ用途では毎回 `data.sql` で初期データが入るため問題ありません）．

---

## 5. 再ビルド手順

ソースや依存を変更してキャッシュを完全に作り直したいとき:

```bash
docker compose build --no-cache
docker compose up
```

`--no-cache` を付けると，キャッシュを使わずゼロからビルドします．
「変更が反映されない」ときの切り分けに使います．

---

## 6. API 動作確認方法

`curl` を使った確認例です．

```bash
# 一覧取得
curl http://localhost:8080/api/archetypes

# 登録
curl -X POST http://localhost:8080/api/archetypes \
  -H "Content-Type: application/json" \
  -d '{"name":"黒緑アビス"}'

# 対戦記録登録
curl -X POST http://localhost:8080/api/matches \
  -H "Content-Type: application/json" \
  -d '{"winnerArchetypeId":1,"loserArchetypeId":2,"playOrder":"FIRST","memo":"テスト","playedAt":"2026-07-01T20:30:00"}'

# マトリクス取得
curl http://localhost:8080/api/matrix
```

ブラウザで画面 `/archetypes` `/matches` `/matrix` を開いても確認できます．

---

## 7. H2 Console の確認方法

1. ブラウザで http://localhost:8080/h2-console を開く．
2. JDBC URL・ユーザー名は，実装時の `application.properties` の設定に合わせる．
   （例: `spring.h2.console.enabled=true`，既定パスは `/h2-console`）
3. 接続後，`SELECT * FROM archetypes;` や `SELECT * FROM matches;` で中身を確認できる．

> H2 Console を有効にするには `spring.h2.console.enabled=true` を設定します．
> 有効化やパスの設定は A 担当が `application.properties` 側で行います．

---

## 8. application-docker.properties について（実装時）

Docker で動かすとき用の設定を分けるため，実装フェーズでは
`src/main/resources/application-docker.properties` を用意する方針とします
（`docker-compose.yml` で `SPRING_PROFILES_ACTIVE=docker` を指定しているため）．

- 通常開発用: `application.properties`
- Docker 用: `application-docker.properties`（プロファイル `docker` で有効）

> 今回の設計フェーズではこのファイルは作成しません（実装フェーズで A 担当が用意）．
> 記述内容の例: H2 の接続設定，`spring.h2.console.enabled=true`，`schema.sql` / `data.sql` の読み込み設定など．

---

## 9. よくあるエラーと対処

| 症状 | 原因の候補 | 対処 |
| --- | --- | --- |
| `port is already allocated` | 8080 番が他で使用中 | 使用中のプロセスを止めるか，`docker-compose.yml` のポートを `8081:8080` などに変更． |
| ビルドが遅い・失敗する | 依存ダウンロード失敗，ネットワーク | 再実行．改善しなければ `--no-cache` で再ビルド． |
| 変更が反映されない | 古いイメージのキャッシュ | `docker compose build --no-cache` → `docker compose up`． |
| `/h2-console` が開けない | console 未有効化 | `application.properties` の `spring.h2.console.enabled=true` を確認（A 担当）． |
| API が 404 | パス誤り，コントローラ未実装 | パスが `/api/...` か，担当機能が実装済みかを確認． |
| コンテナがすぐ終了する | 起動時例外（DB 設定・SQL エラー等） | `docker compose logs app` でスタックトレースを確認． |
