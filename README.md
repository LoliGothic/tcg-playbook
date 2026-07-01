# Duel Matrix

デュエル・マスターズの対戦記録を保存し，アーキタイプ（デッキの種類）同士の
**勝率マトリクス**を表示する Java Spring Boot アプリケーションです．
大学のオブジェクト指向設計の最終課題として，最小構成のプロトタイプを作ります．

---

## Duel Matrix と TCG Playbook の関係

| 名前 | 位置づけ |
| --- | --- |
| **Duel Matrix** | 今回の授業課題で作る Java / Spring Boot 版のプロトタイプ．デュエマ専用・最小構成． |
| **TCG Playbook** | 将来的に開発予定の本番ネイティブアプリ．TCG 全般の対戦記録・調整ノート・勝率分析を扱う想定． |

今回の課題では，将来の **TCG Playbook** の一部機能（対戦記録と勝率分析）だけを切り出し，
**Duel Matrix** という名前で Java の授業課題向けに実装します．

---

## 使用技術

| 項目 | 採用技術 |
| --- | --- |
| 言語 | Java 17 |
| フレームワーク | Spring Boot 3.x |
| ビルドツール | Maven |
| データベース | H2 Database（インメモリ／組み込み） |
| 永続化 | Spring Data JPA |
| 画面 | Thymeleaf（最小限の表示画面） |
| 認証 | なし |
| 環境構築 | Docker / Docker Compose |
| ベースパッケージ | `com.example.duelmatrix` |

---

## 主な機能（最小スコープ）

1. アーキタイプ登録
2. アーキタイプ一覧表示
3. 対戦記録登録
4. 対戦履歴表示
5. 勝率マトリクス表示

---

## 起動方法（Docker）

```bash
docker compose up --build
```

### 確認 URL

| URL | 内容 |
| --- | --- |
| http://localhost:8080 | トップ（画面へのリンク） |
| http://localhost:8080/archetypes | アーキタイプ画面 |
| http://localhost:8080/matches | 対戦記録画面 |
| http://localhost:8080/matrix | 勝率マトリクス画面 |
| http://localhost:8080/api/archetypes | アーキタイプ API |
| http://localhost:8080/h2-console | H2 コンソール（DB の中身確認） |

停止は `docker compose down`．詳しくは [docs/ENVIRONMENT.md](docs/ENVIRONMENT.md) を参照してください．

---

## API 一覧

| メソッド | パス | 内容 | 担当 |
| --- | --- | --- | --- |
| GET | `/api/archetypes` | アーキタイプ一覧取得 | B |
| POST | `/api/archetypes` | アーキタイプ登録 | B |
| GET | `/api/matches` | 対戦履歴取得 | C |
| POST | `/api/matches` | 対戦記録登録 | C |
| GET | `/api/matrix` | 勝率マトリクス取得 | D |

詳細は [docs/API_SPEC.md](docs/API_SPEC.md) を参照してください．

---

## 画面 URL

| パス | 画面 | 担当 |
| --- | --- | --- |
| `/archetypes` | アーキタイプ一覧・登録 | D |
| `/matches` | 対戦記録登録・履歴 | D |
| `/matrix` | 勝率マトリクス表示 | D |

---

## Git 運用ルール（概要）

1. 作業は必ず **Issue** を作成してから始める．
2. Issue 番号を使ってブランチを切る（例: `feature/12-match-api`）．
3. 作業完了後，**Pull Request / Merge Request** を作成する．
4. 担当者は PR / MR の URL を代表者に送る．
5. 代表者が確認して `develop` にマージする．
6. `main` には直接 push しない．

コミットメッセージは `feat: add match creation api` のようにプレフィックスを付けます．
詳細は [docs/DEVELOPMENT_GUIDE.md](docs/DEVELOPMENT_GUIDE.md) を参照してください．

---

## 担当分担

| 担当 | 範囲 | 主なタスク文書 |
| --- | --- | --- |
| A | DB・Entity・Repository・Docker 環境構築 | [TASK_A_REPOSITORY.md](docs/TASK_A_REPOSITORY.md) |
| B | アーキタイプ登録・一覧機能 | [TASK_B_ARCHETYPE.md](docs/TASK_B_ARCHETYPE.md) |
| C | 対戦記録登録・履歴機能 | [TASK_C_MATCH.md](docs/TASK_C_MATCH.md) |
| D | 勝率マトリクス計算・画面表示 | [TASK_D_MATRIX_UI.md](docs/TASK_D_MATRIX_UI.md) |

---

## ドキュメント一覧

| ファイル | 内容 |
| --- | --- |
| [docs/PROJECT_SPEC.md](docs/PROJECT_SPEC.md) | プロジェクト全体仕様・用語・利用の流れ・OOP 要件対応 |
| [docs/API_SPEC.md](docs/API_SPEC.md) | REST API の詳細仕様 |
| [docs/DB_SCHEMA.md](docs/DB_SCHEMA.md) | DB 設計・ER 図・テーブル説明 |
| [docs/CLASS_DESIGN.md](docs/CLASS_DESIGN.md) | Java クラス設計・デザインパターン |
| [docs/DEVELOPMENT_GUIDE.md](docs/DEVELOPMENT_GUIDE.md) | Git 運用・開発順序・注意点 |
| [docs/ENVIRONMENT.md](docs/ENVIRONMENT.md) | Docker 環境構築手順 |
| [docs/REPORT_OUTLINE.md](docs/REPORT_OUTLINE.md) | レポート（PDF）骨子 |
| [docs/SLIDE_OUTLINE.md](docs/SLIDE_OUTLINE.md) | 発表スライド骨子 |
| docs/TASK_*.md | 各担当者向けの実装仕様書 |
| docs/assets/*.mmd, *.sql | Mermaid 図・SQL |