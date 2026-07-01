# TASK C: 対戦記録登録・履歴機能

担当 C の作業仕様書です．対戦記録の登録・履歴表示機能を，**Factory Pattern** と
**Command Pattern** を使って実装します．A の Entity・Repository と，B の
`ArchetypeService` が前提になります．

関連: [API_SPEC.md](API_SPEC.md)（3・4 節），[CLASS_DESIGN.md](CLASS_DESIGN.md)（7.1・7.2 節）

---

## 1. 担当範囲

- 対戦履歴取得 API（`GET /api/matches`）
- 対戦記録登録 API（`POST /api/matches`）
- 上記を支える Controller / Service / DTO
- Factory Pattern（`MatchRecordFactory`）
- Command Pattern（`Command<T>` / `CreateMatchCommand` / `CommandInvoker`）

---

## 2. 作成するファイル

| ファイル | 内容 |
| --- | --- |
| `controller/MatchController.java` | 対戦記録 API の入口 |
| `service/MatchService.java` | 業務ロジック（取得・登録・アーキタイプ存在確認） |
| `dto/MatchCreateRequest.java` | 登録リクエスト |
| `dto/MatchResponse.java` | 応答（アーキタイプ名を含む） |
| `pattern/factory/MatchRecordFactory.java` | DTO → MatchRecord 生成 |
| `pattern/command/CreateMatchCommand.java` | 対戦記録作成コマンド |

> `Command<T>` インターフェースと `CommandInvoker` は **A が基盤として提供**します（C は作りません）．
> C は自分の具体コマンド `CreateMatchCommand` を書くだけです．

---

## 3. 担当 API

| メソッド | パス | 内容 |
| --- | --- | --- |
| GET | `/api/matches` | 履歴取得（日時降順） |
| POST | `/api/matches` | 新規登録 |

詳細な Request / Response / エラーは [API_SPEC.md](API_SPEC.md) の 3・4 節を参照．

---

## 4. DTO の項目

### MatchCreateRequest（入力）

| フィールド | 型 | バリデーション |
| --- | --- | --- |
| winnerArchetypeId | Long | `@NotNull` |
| loserArchetypeId | Long | `@NotNull`（winner と異なること） |
| playOrder | String | FIRST/SECOND/UNKNOWN のいずれか（省略時 UNKNOWN，勝者の先攻後攻） |
| memo | String | `@Size(max = 500)` |
| playedAt | LocalDateTime | `@NotNull` |

> 勝った試合のみ登録する（勝者記録モデル）ため，勝敗（result）フィールドは無い．

### MatchResponse（出力）

| フィールド | 型 |
| --- | --- |
| id | Long |
| winnerArchetypeId | Long |
| winnerArchetypeName | String |
| loserArchetypeId | Long |
| loserArchetypeName | String |
| playOrder | String |
| memo | String |
| playedAt | LocalDateTime |
| createdAt | LocalDateTime |

---

## 5. 各クラスの責務

| クラス | 責務 |
| --- | --- |
| MatchController | リクエストを受け，Command を組み立てて実行し，結果を返すだけ． |
| MatchService | 履歴取得（日時降順），登録，アーキタイプ存在確認，Entity → DTO 変換（名前付与）． |
| MatchRecordFactory | `MatchCreateRequest` から `MatchRecord` を生成（playOrder の enum 変換を含む）． |
| CreateMatchCommand | 「対戦記録作成」1 操作を `execute()` として表す． |
| CommandInvoker | Command の `execute()` を呼ぶ（A が提供する共有基盤）． |

### Controller → Command → Service の流れ（登録）

```
MatchController.createMatch(request)
  → new CreateMatchCommand(matchService, request)
  → commandInvoker.invoke(command)
      → command.execute()
          → matchService.createMatch(request)
              → archetypeRepository.findById(winnerArchetypeId)   // 存在確認（A の Repository）
              → archetypeRepository.findById(loserArchetypeId)    // 無ければ ARCHETYPE_NOT_FOUND
              → matchRecordFactory.create(request)                // enum 変換して生成
              → matchRepository.save(...)
              → MatchResponse に詰めて返す（名前も付与）
```

**存在確認は A の Repository で行う**: アーキタイプの存在確認・名前引きは，B の `ArchetypeService`
ではなく **A の `ArchetypeRepository`**（`findById` / `existsById` / `findAllById`）を直接使います．
こうすることで C は A だけに依存し，B と並行開発できます．

**アーキタイプ名の付与**: `MatchResponse` の `winnerArchetypeName` / `loserArchetypeName` は
Entity に無い項目なので，Service で ID から名前を引いて詰めます．履歴取得（GET）では
件数が多くなり得るため，`archetypeRepository.findAllById(...)` で一括取得して Map 化してから
引くと N+1 を避けられます．

---

## 6. 必要なバリデーション

| ルール | 実現方法 | エラー |
| --- | --- | --- |
| winnerArchetypeId / loserArchetypeId 必須 | `@NotNull` | VALIDATION_ERROR (400) |
| playedAt 必須 | `@NotNull` | VALIDATION_ERROR (400) |
| winnerArchetypeId と loserArchetypeId は異なること | Service でチェック | VALIDATION_ERROR (400) |
| playOrder は FIRST/SECOND/UNKNOWN | `PlayOrder.valueOf` | VALIDATION_ERROR (400) |
| memo 500 文字以内 | `@Size(max = 500)` | VALIDATION_ERROR (400) |
| アーキタイプ ID が存在すること | A の `archetypeRepository.findById` / `existsById` | ARCHETYPE_NOT_FOUND (404) |

> enum 変換で未知の値が来たら `IllegalArgumentException` を捕まえて `VALIDATION_ERROR` に変換する，
> または独自バリデーションで事前チェックする．

---

## 7. 完成条件

- [ ] 対戦記録を登録できる（201 Created）．
- [ ] 対戦履歴を日時降順で取得できる（0 件なら空配列）．
- [ ] 存在しないアーキタイプ ID が指定された場合にエラー（404）にできる．
- [ ] Controller に複雑な処理を書かず，Service / Command に処理を置いている．
- [ ] Factory Pattern（MatchRecordFactory）を使った生成になっている．
- [ ] Command Pattern（CreateMatchCommand + CommandInvoker）を使った設計になっている．

---

## 8. 他担当との接続点

| 相手 | 接続内容 |
| --- | --- |
| A | `MatchRecord` Entity・`MatchRepository`・`ArchetypeRepository`・enum・コマンド基盤（`Command<T>` / `CommandInvoker`）を使う（A の完成が前提）． |
| D | D の画面が `GET /api/matches` を呼ぶが，これは [API_SPEC.md](API_SPEC.md) の**契約への依存**であり，C のコードへの依存ではない． |

**C が依存するのは A だけです．** アーキタイプの存在確認も A の `ArchetypeRepository` を使うため，
B のコード完成を待つ必要はなく，B・D と完全に並行して開発できます．
