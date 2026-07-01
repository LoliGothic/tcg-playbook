# TASK D: 勝率マトリクス計算・画面表示

担当 D の作業仕様書です．勝率マトリクスの計算を **Command Pattern** と
**Template Method Pattern** で実装し，さらに 3 画面（Thymeleaf）を用意します．
A の Entity・Repository と，B・C の API が前提になります．

関連: [API_SPEC.md](API_SPEC.md)（5 節），[CLASS_DESIGN.md](CLASS_DESIGN.md)（7.2・7.3 節）

---

## 1. 担当範囲

- 勝率マトリクス取得 API（`GET /api/matrix`）
- 画面ルーティング（`/archetypes` `/matches` `/matrix`）
- Command Pattern（`GenerateMatrixCommand`）
- Template Method Pattern（`AbstractMatrixBuilder` / `StandardMatrixBuilder`）
- Thymeleaf テンプレート 3 枚

---

## 2. 作成するファイル

| ファイル | 内容 |
| --- | --- |
| `controller/MatrixController.java` | マトリクス API の入口 |
| `controller/ViewController.java` | 画面ルーティング（3 画面） |
| `service/MatrixService.java` | マトリクス計算の窓口（Builder を呼ぶ） |
| `dto/MatrixResponse.java` | マトリクス全体（rows） |
| `dto/MatrixRowResponse.java` | マトリクス 1 行（対面ごと） |
| `pattern/command/GenerateMatrixCommand.java` | マトリクス生成コマンド |
| `pattern/template/AbstractMatrixBuilder.java` | テンプレートメソッド（手順の骨格） |
| `pattern/template/StandardMatrixBuilder.java` | 具体的な集計処理 |
| `templates/archetypes.html` | アーキタイプ画面 |
| `templates/matches.html` | 対戦記録画面 |
| `templates/matrix.html` | マトリクス画面 |

> `Command<T>` インターフェースと `CommandInvoker` は **A が基盤として提供**します（D は作りません）．
> D は自分の具体コマンド `GenerateMatrixCommand` を書くだけです．
> マトリクス計算に必要なデータ（アーキタイプ・対戦記録）は，**A の Repository から直接取得**します．
> B・C の Service や API コードには依存しません．

---

## 3. 担当 API

| メソッド | パス | 内容 |
| --- | --- | --- |
| GET | `/api/matrix` | 勝率マトリクス取得 |

画面ルーティング:

```java
@GetMapping("/archetypes") String archetypesPage();  // templates/archetypes.html
@GetMapping("/matches")    String matchesPage();      // templates/matches.html
@GetMapping("/matrix")     String matrixPage();       // templates/matrix.html
```

---

## 4. DTO の項目

### MatrixRowResponse（1 行 = 1 対面）

| フィールド | 型 | 説明 |
| --- | --- | --- |
| myArchetypeId | Long | 行のアーキタイプ（この行の視点） |
| myArchetypeName | String | 行のアーキタイプ名 |
| opponentArchetypeId | Long | 相手（列）のアーキタイプ |
| opponentArchetypeName | String | 相手（列）のアーキタイプ名 |
| wins | int | 行が相手に勝った件数 |
| losses | int | 行が相手に負けた件数（＝相手が勝った件数） |
| total | int | wins + losses |
| winRate | double | wins / total × 100 |

### MatrixResponse

| フィールド | 型 |
| --- | --- |
| rows | List\<MatrixRowResponse\> |

---

## 5. 各クラスの責務

| クラス | 責務 |
| --- | --- |
| MatrixController | `GET /api/matrix` を受け，Command を実行して結果を返すだけ． |
| ViewController | 3 画面のテンプレート名を返すだけ（ロジックなし）． |
| MatrixService | `StandardMatrixBuilder.build()` を呼び，結果を返す窓口． |
| GenerateMatrixCommand | 「マトリクス生成」1 操作を `execute()` として表す． |
| AbstractMatrixBuilder | マトリクス生成の**手順の骨格**（テンプレートメソッド `build()`）． |
| StandardMatrixBuilder | 取得・行生成・集計の具体処理を実装（Repository を使う）． |

### Template Method の骨格（[CLASS_DESIGN.md](CLASS_DESIGN.md) 7.3 と対応）

`AbstractMatrixBuilder.build()` が固定する手順:

1. アーキタイプ一覧を取得（`loadArchetypes()` … 抽象）
2. 対戦記録一覧を取得（`loadMatches()` … 抽象）
3. アーキタイプの組み合わせを作る（同一 ID は除外）
4. 勝ち・負けを数える（`buildRow()` … 抽象）
5. 勝率を計算する（`buildRow()` 内）
6. `MatrixResponse` として返す

`StandardMatrixBuilder` が `loadArchetypes` / `loadMatches` / `buildRow` を実装します．

---

## 6. 計算ルール

対面「自分 A vs 相手 B」を，勝った試合の記録から集計します．

```
wins    = A が B に勝った件数（winner = A, loser = B）
losses  = A が B に負けた件数（winner = B, loser = A）
total   = wins + losses
winRate = (total == 0) ? 除外 : wins / total * 100
```

- 引き分けは記録されないため計算対象外．
- A対B と B対A は**両方**返す（別の行）．この 2 行の勝率は合計 100%．
  例）A が B に 3 勝，B が A に 10 勝 → A対B = 3/13 ≈ 23.1%，B対A = 10/13 ≈ 76.9%．
- 同一アーキタイプ同士（対角）は返さない．
- `total == 0` の対面は行に含めない（ゼロ除算回避）．
- winRate は小数第 1 位程度に丸める．

---

## 7. 画面仕様

### /archetypes

- 目的: アーキタイプ一覧表示と新規追加．
- 表示項目: id, name, createdAt．
- 入力項目: name．
- 呼ぶ API: `GET /api/archetypes`（一覧），`POST /api/archetypes`（追加）．
- レイアウト: 上部に入力フォーム（name + 追加ボタン），下部に一覧テーブル．

### /matches

- 目的: 対戦記録（勝った試合）の登録と履歴表示．
- 入力項目: winnerArchetype, loserArchetype, playOrder, memo, playedAt．
- 表示項目: winnerArchetypeName, loserArchetypeName, playOrder, memo, playedAt．
- 呼ぶ API: `GET /api/matches`（履歴），`POST /api/matches`（登録），
  `GET /api/archetypes`（プルダウン用）．
- レイアウト: 上部に登録フォーム（アーキタイプはプルダウン），下部に履歴テーブル．

### /matrix

- 目的: 勝率マトリクスを表形式で表示．
- 呼ぶ API: `GET /api/matrix`．
- 表示例:

```
              青白ウィリデ   創世竜
青白ウィリデ       -          60%
創世竜            40%          -
```

- レイアウト: 行＝自分，列＝相手のクロス表．対角は「-」．
- `rows` を `(myArchetypeId, opponentArchetypeId)` で引けるようにして各セルを埋める．

---

## 8. 完成条件

- [ ] 勝率マトリクスを計算できる．
- [ ] A対B と B対A の勝率を両方返せる（対角は除外）．
- [ ] `total == 0` でゼロ除算しない．
- [ ] Command Pattern（GenerateMatrixCommand）を使っている．
- [ ] Template Method Pattern（AbstractMatrixBuilder / StandardMatrixBuilder）を使っている．
- [ ] 3 画面が表示でき，マトリクスが表として見られる．

---

## 9. 他担当との接続点

| 相手 | 接続内容 |
| --- | --- |
| A | `Archetype` / `MatchRecord` Entity・両 Repository・enum・コマンド基盤（`Command<T>` / `CommandInvoker`）を使う（A の完成が前提）． |
| B | D の画面が `GET /api/archetypes` を呼ぶが，これは [API_SPEC.md](API_SPEC.md) の**契約への依存**であり，B のコードへの依存ではない． |
| C | D の画面が `GET /api/matches` を呼ぶが，同じく**契約への依存**であり，C のコードへの依存ではない． |

**D が依存するのは A だけです．** マトリクス計算は A の Repository から直接データを取得するため，
B・C のコード完成を待つ必要はありません．画面が呼ぶ API は仕様（[API_SPEC.md](API_SPEC.md)）で
先に固定されているので，D は B・C と完全に並行して開発できます．

> 補足: 結合テスト（画面から実際に B・C の API を叩く確認）だけは全員の実装が揃ってから行いますが，
> D 自身の実装（マトリクス計算・画面）は A の完成後すぐ着手できます．
