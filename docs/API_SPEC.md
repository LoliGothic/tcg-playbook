# Duel Matrix API 仕様書

このドキュメントは，Duel Matrix が提供する REST API の詳細仕様です．
担当者はこの仕様に従って実装し，**フィールド名・型・エラー形式を勝手に変えない**でください．
変更が必要な場合は，先にこのドキュメントを更新し，代表者に共有してください．

---

## 共通事項

| 項目 | 内容 |
| --- | --- |
| ベース URL | `http://localhost:8080` |
| API パス | `/api/**` |
| リクエスト形式 | JSON（`Content-Type: application/json`） |
| レスポンス形式 | JSON（`Content-Type: application/json`） |
| 認証 | なし |
| 日時形式 | ISO-8601（例: `2026-07-01T20:30:00`） |
| 文字コード | UTF-8 |

### 共通エラーレスポンス形式

すべてのエラーは以下の形式で返します（`ErrorResponse` DTO）．

```json
{
  "error": "ERROR_CODE",
  "message": "人間向けの説明文．",
  "path": "/api/xxx",
  "timestamp": "2026-07-01T11:00:02"
}
```

| フィールド | 型 | 説明 |
| --- | --- | --- |
| error | String | エラーコード（機械判定用の定数） |
| message | String | 画面表示用の説明文 |
| path | String | エラーが発生したリクエストパス |
| timestamp | String(ISO-8601) | エラー発生時刻 |

### エラーコード一覧

| コード | HTTP | 意味 |
| --- | --- | --- |
| `VALIDATION_ERROR` | 400 | 必須項目未入力・文字数超過・enum 値不正など |
| `ARCHETYPE_ALREADY_EXISTS` | 409 | 同名アーキタイプが既に存在する |
| `ARCHETYPE_NOT_FOUND` | 404 | 指定されたアーキタイプ ID が存在しない |
| `INTERNAL_ERROR` | 500 | 想定外のサーバエラー |

> 実装ヒント: エラー整形は `@RestControllerAdvice` を使った例外ハンドラに集約すると，
> 各 Controller が例外整形の責務を持たずに済み，凝集度が上がります．

---

## 1. GET /api/archetypes

| 項目 | 内容 |
| --- | --- |
| Endpoint | `GET /api/archetypes` |
| Purpose | 登録済みアーキタイプ一覧を取得する． |
| Used by | `/archetypes` 画面，`/matches` 画面（プルダウン用） |
| 担当者 | B |

### Request

なし（クエリパラメータ・ボディともに不要）．

### Response JSON（200 OK）

```json
[
  {
    "id": 1,
    "name": "青白ウィリデ",
    "createdAt": "2026-07-01T10:00:00"
  },
  {
    "id": 2,
    "name": "創世竜",
    "createdAt": "2026-07-01T10:05:00"
  }
]
```

| フィールド | 型 | 説明 |
| --- | --- | --- |
| id | Long | アーキタイプ ID |
| name | String | アーキタイプ名 |
| createdAt | String(ISO-8601) | 登録日時 |

### HTTP status code

| コード | 条件 |
| --- | --- |
| 200 OK | 取得成功（データ 0 件でも空配列 `[]` を返す） |

### Validation rule

なし（読み取り専用）．

### 実装時の注意点

- 0 件のときは `null` ではなく空配列 `[]` を返すこと．
- 並び順は `id` 昇順を基本とする（プルダウン表示の安定のため）．

---

## 2. POST /api/archetypes

| 項目 | 内容 |
| --- | --- |
| Endpoint | `POST /api/archetypes` |
| Purpose | 新しいアーキタイプを登録する． |
| Used by | `/archetypes` 画面 |
| 担当者 | B |

### Request JSON

```json
{
  "name": "黒緑アビス"
}
```

| フィールド | 型 | 必須 | 説明 |
| --- | --- | --- | --- |
| name | String | ○ | アーキタイプ名 |

### Response JSON（201 Created）

```json
{
  "id": 3,
  "name": "黒緑アビス",
  "createdAt": "2026-07-01T11:00:00"
}
```

### HTTP status code

| コード | 条件 |
| --- | --- |
| 201 Created | 登録成功 |
| 400 Bad Request | バリデーションエラー（`VALIDATION_ERROR`） |
| 409 Conflict | 同名アーキタイプが既に存在（`ARCHETYPE_ALREADY_EXISTS`） |

### Validation rule

| ルール | エラーコード |
| --- | --- |
| name は必須（`null` 不可） | `VALIDATION_ERROR` |
| name は空文字・空白のみ不可 | `VALIDATION_ERROR` |
| name は 100 文字以内 | `VALIDATION_ERROR` |
| 大文字小文字を無視して重複禁止 | `ARCHETYPE_ALREADY_EXISTS` |

### Error JSON（409 の例）

```json
{
  "error": "ARCHETYPE_ALREADY_EXISTS",
  "message": "同名のアーキタイプが既に存在します．",
  "path": "/api/archetypes",
  "timestamp": "2026-07-01T11:00:02"
}
```

### 実装時の注意点

- 重複チェックは `ArchetypeRepository.existsByNameIgnoreCase(name)` を使う．
- 入力チェックは DTO に `@NotBlank` `@Size(max = 100)` を付け，Bean Validation に任せる．
- 成功時は `ResponseEntity.status(201)` で `Location` ヘッダを付けると丁寧（任意）．

---

## 3. GET /api/matches

| 項目 | 内容 |
| --- | --- |
| Endpoint | `GET /api/matches` |
| Purpose | 登録済みの対戦履歴を取得する． |
| Used by | `/matches` 画面 |
| 担当者 | C |

### Request

なし．

### Response JSON（200 OK）

```json
[
  {
    "id": 10,
    "winnerArchetypeId": 1,
    "winnerArchetypeName": "青白ウィリデ",
    "loserArchetypeId": 2,
    "loserArchetypeName": "創世竜",
    "playOrder": "FIRST",
    "memo": "先攻で押し切れた",
    "playedAt": "2026-07-01T20:30:00",
    "createdAt": "2026-07-01T20:31:00"
  }
]
```

| フィールド | 型 | 説明 |
| --- | --- | --- |
| id | Long | 対戦記録 ID |
| winnerArchetypeId | Long | 勝者のアーキタイプ ID |
| winnerArchetypeName | String | 勝者のアーキタイプ名（表示用に付与） |
| loserArchetypeId | Long | 敗者のアーキタイプ ID |
| loserArchetypeName | String | 敗者のアーキタイプ名（表示用に付与） |
| playOrder | String | 勝者の先攻後攻 `FIRST` / `SECOND` / `UNKNOWN` |
| memo | String | メモ（null 可） |
| playedAt | String(ISO-8601) | 対戦日時 |
| createdAt | String(ISO-8601) | 登録日時 |

> 本アプリは「勝った試合」だけを登録する（勝者記録モデル）．敗北・引き分けは登録しない．

### HTTP status code

| コード | 条件 |
| --- | --- |
| 200 OK | 取得成功（0 件でも空配列 `[]`） |

### Validation rule

なし（読み取り専用）．

### 実装時の注意点

- 並び順は対戦日時の新しい順（`findAllByOrderByPlayedAtDesc()`）とする．
- `winnerArchetypeName` / `loserArchetypeName` は Entity には無い項目なので，
  Service で ID から名前を引いて `MatchResponse` に詰める．
  （N+1 が気になる場合は，アーキタイプ一覧を一括取得して Map 化してから引く）

---

## 4. POST /api/matches

| 項目 | 内容 |
| --- | --- |
| Endpoint | `POST /api/matches` |
| Purpose | 新しい対戦記録（勝った試合）を登録する． |
| Used by | `/matches` 画面 |
| 担当者 | C |

### Request JSON

```json
{
  "winnerArchetypeId": 1,
  "loserArchetypeId": 2,
  "playOrder": "FIRST",
  "memo": "先攻で押し切れた",
  "playedAt": "2026-07-01T20:30:00"
}
```

| フィールド | 型 | 必須 | 説明 |
| --- | --- | --- | --- |
| winnerArchetypeId | Long | ○ | 勝者のアーキタイプ ID |
| loserArchetypeId | Long | ○ | 敗者のアーキタイプ ID |
| playOrder | String | △ | 勝者の先攻後攻 `FIRST` / `SECOND` / `UNKNOWN`（省略時 `UNKNOWN`） |
| memo | String | ✕ | メモ（500 文字以内） |
| playedAt | String(ISO-8601) | ○ | 対戦日時 |

> 勝った試合のみを登録するため，勝敗（result）フィールドは持たない．

### Response JSON（201 Created）

```json
{
  "id": 10,
  "winnerArchetypeId": 1,
  "winnerArchetypeName": "青白ウィリデ",
  "loserArchetypeId": 2,
  "loserArchetypeName": "創世竜",
  "playOrder": "FIRST",
  "memo": "先攻で押し切れた",
  "playedAt": "2026-07-01T20:30:00",
  "createdAt": "2026-07-01T20:31:00"
}
```

### HTTP status code

| コード | 条件 |
| --- | --- |
| 201 Created | 登録成功 |
| 400 Bad Request | バリデーションエラー（`VALIDATION_ERROR`） |
| 404 Not Found | 指定アーキタイプが存在しない（`ARCHETYPE_NOT_FOUND`） |

### Validation rule

| ルール | エラーコード |
| --- | --- |
| winnerArchetypeId は必須 | `VALIDATION_ERROR` |
| loserArchetypeId は必須 | `VALIDATION_ERROR` |
| playedAt は必須 | `VALIDATION_ERROR` |
| winnerArchetypeId と loserArchetypeId は異なること（自分自身との対戦は不可） | `VALIDATION_ERROR` |
| playOrder は `FIRST` / `SECOND` / `UNKNOWN` のみ | `VALIDATION_ERROR` |
| memo は 500 文字以内 | `VALIDATION_ERROR` |
| 指定されたアーキタイプ ID が存在すること | `ARCHETYPE_NOT_FOUND` |

### Error JSON（404 の例）

```json
{
  "error": "ARCHETYPE_NOT_FOUND",
  "message": "指定されたアーキタイプが存在しません．",
  "path": "/api/matches",
  "timestamp": "2026-07-01T20:31:02"
}
```

### 実装時の注意点

- `playOrder` の enum 変換は `PlayOrder.valueOf(...)` を使い，未知の値なら `VALIDATION_ERROR` にする．
- アーキタイプ存在チェックは A の `ArchetypeRepository`（`existsById` / `findById`）で行う．
  B の `ArchetypeService` には依存させない（担当 C は A だけに依存し，B と並行開発できる）．
- `winnerArchetypeId` と `loserArchetypeId` が同じ場合はバリデーションエラーにする
  （勝者と敗者が同一の試合はありえないため）．

---

## 5. GET /api/matrix

| 項目 | 内容 |
| --- | --- |
| Endpoint | `GET /api/matrix` |
| Purpose | 登録された対戦記録から，アーキタイプ同士の勝率マトリクスを計算して返す． |
| Used by | `/matrix` 画面 |
| 担当者 | D |

### Request

なし．

### Response JSON（200 OK）

```json
{
  "rows": [
    {
      "myArchetypeId": 1,
      "myArchetypeName": "青白ウィリデ",
      "opponentArchetypeId": 2,
      "opponentArchetypeName": "創世竜",
      "wins": 6,
      "losses": 4,
      "total": 10,
      "winRate": 60.0
    },
    {
      "myArchetypeId": 2,
      "myArchetypeName": "創世竜",
      "opponentArchetypeId": 1,
      "opponentArchetypeName": "青白ウィリデ",
      "wins": 4,
      "losses": 6,
      "total": 10,
      "winRate": 40.0
    }
  ]
}
```

| フィールド | 型 | 説明 |
| --- | --- | --- |
| rows | Array | マトリクスの各行（対面ごと） |
| rows[].myArchetypeId | Long | 行のアーキタイプ ID（この行の視点） |
| rows[].myArchetypeName | String | 行のアーキタイプ名 |
| rows[].opponentArchetypeId | Long | 相手（列）のアーキタイプ ID |
| rows[].opponentArchetypeName | String | 相手（列）のアーキタイプ名 |
| rows[].wins | int | 行のアーキタイプが相手に勝った件数 |
| rows[].losses | int | 行のアーキタイプが相手に負けた件数（＝相手が勝った件数） |
| rows[].total | int | 対面の総対戦数（wins + losses） |
| rows[].winRate | double | 勝率（％，小数第 1 位程度） |

### 勝率計算ルール

対面「自分 A vs 相手 B」について，勝った試合の記録から集計します．

```
wins    = A が B に勝った件数（winner = A, loser = B の件数）
losses  = A が B に負けた件数（winner = B, loser = A の件数）
total   = wins + losses
winRate = (total == 0) ? 除外 : wins / total * 100
```

- 引き分けは記録されないため計算対象外（`total` にも含めない）．
- **A対B と B対A は両方返す**（視点が違うため別の行になる）．この 2 行の勝率は合計 100%．
  例）A が B に 3 勝，B が A に 10 勝 → A対B は 3/13 ≈ 23.1%，B対A は 10/13 ≈ 76.9%．
- **同じアーキタイプ同士（A対A）の組み合わせは返さない**（対角は除外）．
- `total == 0` の対面（対戦記録が 1 件もない対面）は行に含めない（ゼロ除算回避）．

### HTTP status code

| コード | 条件 |
| --- | --- |
| 200 OK | 計算成功（記録が無ければ `{"rows": []}`） |

### Validation rule

なし（読み取り専用）．

### 実装時の注意点

- `total == 0` のゼロ除算を避ける（0 件対面は行に含めない，または winRate を 0.0）．
- winRate の丸めは表示の見やすさのため小数第 1 位程度に整える．
- 計算手順は Template Method（`AbstractMatrixBuilder`）に置き，行生成を子クラスに委ねる．
