# TASK B: アーキタイプ登録・一覧機能

担当 B の作業仕様書です．アーキタイプの登録・一覧機能を，**Factory Pattern** と
**Command Pattern** を使って実装します．A の Entity・Repository が前提になります．

関連: [API_SPEC.md](API_SPEC.md)（1・2 節），[CLASS_DESIGN.md](CLASS_DESIGN.md)（7.1・7.2 節）

---

## 1. 担当範囲

- アーキタイプ一覧取得 API（`GET /api/archetypes`）
- アーキタイプ登録 API（`POST /api/archetypes`）
- 上記を支える Controller / Service / DTO
- Factory Pattern（`ArchetypeFactory`）
- Command Pattern（`Command<T>` / `CreateArchetypeCommand` / `CommandInvoker`）

---

## 2. 作成するファイル

| ファイル | 内容 |
| --- | --- |
| `controller/ArchetypeController.java` | アーキタイプ API の入口 |
| `service/ArchetypeService.java` | 業務ロジック（取得・登録・重複チェック・ID 存在確認） |
| `dto/ArchetypeCreateRequest.java` | 登録リクエスト（name） |
| `dto/ArchetypeResponse.java` | 応答（id, name, createdAt） |
| `pattern/factory/ArchetypeFactory.java` | DTO → Archetype 生成 |
| `pattern/command/CreateArchetypeCommand.java` | アーキタイプ作成コマンド |

> `Command<T>` インターフェースと `CommandInvoker` は **A が基盤として提供**します（B は作りません）．
> B は自分の具体コマンド `CreateArchetypeCommand` を書き，`commandInvoker.invoke(...)` で実行します．
> これにより B は A だけに依存し，C・D とは完全に並行して開発できます．

---

## 3. 担当 API

| メソッド | パス | 内容 |
| --- | --- | --- |
| GET | `/api/archetypes` | 一覧取得 |
| POST | `/api/archetypes` | 新規登録 |

詳細な Request / Response / エラーは [API_SPEC.md](API_SPEC.md) の 1・2 節を参照．

---

## 4. DTO の項目

### ArchetypeCreateRequest（入力）

| フィールド | 型 | バリデーション |
| --- | --- | --- |
| name | String | `@NotBlank`，`@Size(max = 100)` |

### ArchetypeResponse（出力）

| フィールド | 型 |
| --- | --- |
| id | Long |
| name | String |
| createdAt | LocalDateTime |

---

## 5. 各クラスの責務

| クラス | 責務 |
| --- | --- |
| ArchetypeController | リクエストを受け，Command を組み立てて CommandInvoker に渡し，結果を返すだけ． |
| ArchetypeService | 一覧取得，登録，重複チェック（`existsByNameIgnoreCase`），ID 存在確認（findById）． |
| ArchetypeFactory | `ArchetypeCreateRequest` から `Archetype` を生成する（new を集約）． |
| CreateArchetypeCommand | 「アーキタイプ作成」1 操作を `execute()` として表す． |
| CommandInvoker | 渡された Command の `execute()` を呼ぶ（将来ログ等を差し込める）． |

### Controller → Command → Service の流れ（登録）

```
ArchetypeController.createArchetype(request)
  → new CreateArchetypeCommand(archetypeService, request)
  → commandInvoker.invoke(command)
      → command.execute()
          → archetypeService.createArchetype(request)
              → 重複チェック（existsByNameIgnoreCase）
              → archetypeFactory.create(request) で Archetype 生成
              → repository.save(...)
              → ArchetypeResponse に詰めて返す
```

---

## 6. 必要なバリデーション

| ルール | 実現方法 | エラー |
| --- | --- | --- |
| name 必須 | `@NotBlank` | VALIDATION_ERROR (400) |
| name 空文字不可 | `@NotBlank` | VALIDATION_ERROR (400) |
| name 100 文字以内 | `@Size(max = 100)` | VALIDATION_ERROR (400) |
| 大文字小文字無視で重複禁止 | Service で `existsByNameIgnoreCase` | ARCHETYPE_ALREADY_EXISTS (409) |

---

## 7. 完成条件

- [ ] アーキタイプ一覧を取得できる（0 件なら空配列）．
- [ ] 新規アーキタイプを追加できる（201 Created）．
- [ ] 同名アーキタイプ（大文字小文字無視）の重複を防げる（409）．
- [ ] Factory Pattern（ArchetypeFactory）を使った生成になっている．
- [ ] Command Pattern（CreateArchetypeCommand + CommandInvoker）を使った設計になっている．
- [ ] Controller が薄い（ロジックを持たない）．

---

## 8. 他担当との接続点

| 相手 | 接続内容 |
| --- | --- |
| A | `Archetype` Entity・`ArchetypeRepository`・コマンド基盤（`Command<T>` / `CommandInvoker`）を使う（A の完成が前提）． |
| D | D の画面が `GET /api/archetypes` を呼ぶが，これは [API_SPEC.md](API_SPEC.md) の**契約への依存**であり，B のコードへの依存ではない．B はレスポンス形式を仕様通りに保つだけでよい． |

**B が依存するのは A だけです．** C・D のコード完成を待つ必要はなく，完全に並行して開発できます．
