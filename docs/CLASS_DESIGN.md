# Duel Matrix クラス設計書

このドキュメントは，Duel Matrix の Java クラス設計をまとめたものです．
パッケージ構成・各クラスの責務・主要メソッド・クラス間の依存関係，そして
採用するデザインパターン（Factory / Command / Template Method）を説明します．

クラス図の Mermaid ソースは [assets/class_diagram.mmd](assets/class_diagram.mmd)，
パターン関係図は [assets/patterns_diagram.mmd](assets/patterns_diagram.mmd) にあります．

> 注意: このドキュメントは**設計**であり，Java 実装コードそのものはまだ作成しません．
> メソッドシグネチャは実装の指針として示しています．

---

## 1. パッケージ構成

```
com.example.duelmatrix
├── controller          … HTTP リクエストを受ける層（薄く保つ）
│   ├── ArchetypeController
│   ├── MatchController
│   ├── MatrixController
│   └── ViewController          … Thymeleaf 画面のルーティング
├── service             … 業務ロジックを持つ層
│   ├── ArchetypeService
│   ├── MatchService
│   └── MatrixService
├── repository          … DB アクセス層（Spring Data JPA）
│   ├── ArchetypeRepository
│   └── MatchRepository
├── domain              … ドメインモデル（Entity・enum）
│   ├── Archetype
│   ├── MatchRecord
│   └── PlayOrder               … enum（勝者の先攻後攻）
├── dto                 … 入出力データ（外部との受け渡し用）
│   ├── ArchetypeCreateRequest
│   ├── ArchetypeResponse
│   ├── MatchCreateRequest
│   ├── MatchResponse
│   ├── MatrixRowResponse
│   ├── MatrixResponse
│   └── ErrorResponse           … 共通エラー形式（A が提供）
├── exception           … 例外と共通エラーハンドリング（A が提供する共有基盤）
│   ├── ValidationException             … 400 用（Service 層の業務ルール違反）
│   ├── ArchetypeNotFoundException      … 404 用の例外
│   ├── ArchetypeAlreadyExistsException … 409 用の例外
│   └── GlobalExceptionHandler          … @RestControllerAdvice で共通形式に整形
└── pattern             … デザインパターンの実装
    ├── factory
    │   ├── ArchetypeFactory
    │   └── MatchRecordFactory
    ├── command
    │   ├── Command<T>          … インターフェース（A が提供する共有基盤）
    │   ├── CommandInvoker      … 実行役（A が提供する共有基盤）
    │   ├── CreateArchetypeCommand   … B が作成
    │   ├── CreateMatchCommand       … C が作成
    │   └── GenerateMatrixCommand    … D が作成
    └── template
        ├── AbstractMatrixBuilder   … 抽象クラス（テンプレートメソッド）
        └── StandardMatrixBuilder   … 具象クラス
```

### レイヤ間の依存方向

```
controller → service → repository
                     → pattern.factory
                     → pattern.template
controller → pattern.command → service
```

- 依存は上から下への一方向のみ．下位層が上位層を参照してはいけません（疎結合）．
- Controller は薄く保ち，判断・計算は Service / Command / Template に置きます．

### 担当間の依存を A に集約する方針（重要）

チームは非同期・並行開発のため，**B・C・D は互いに依存せず，A（基盤）だけに依存**します．
これを成立させるため，以下を守ります．

| 共有物 | 所有者 | 使う人 |
| --- | --- | --- |
| Entity（Archetype / MatchRecord）・enum | A | B・C・D |
| Repository（ArchetypeRepository / MatchRepository） | A | B・C・D |
| `Command<T>` インターフェース・`CommandInvoker`（コマンド基盤） | A | B・C・D |
| `ErrorResponse`・共通例外・`GlobalExceptionHandler`（エラー基盤） | A | B・C・D |

- **C の対戦記録登録**では，アーキタイプの存在確認・名前引きを **B の `ArchetypeService` ではなく，
  A の `ArchetypeRepository`（`existsById` / `findById` / `findAllById`）** で行います．
  これにより C は B のコードに依存しません．
- **D のマトリクス計算**は，`ArchetypeRepository` / `MatchRepository`（A）から直接データを取得します．
  B・C の Service や API コードには依存しません．
- **D の画面**が呼ぶ `GET /api/archetypes`・`GET /api/matches` は，コード依存ではなく
  [API_SPEC.md](API_SPEC.md) で固定した**契約への依存**です．契約は先に確定しているため，
  D は B・C のコード完成を待たずに画面を並行実装できます．
- `Command<T>` / `CommandInvoker` を A 所有にすることで，B・C・D は「自分の Command クラス」だけを
  各自で書けばよく，共有基盤の取り合いが起きません．

---

## 2. domain（ドメインモデル）

### 2.1 Archetype（Entity）

| 項目 | 内容 |
| --- | --- |
| 役割 | アーキタイプ（デッキの種類）を表す Entity． |
| 責務 | id・name・createdAt を保持する． |
| フィールド | `private Long id`，`private String name`，`private LocalDateTime createdAt` |
| 関係 | ArchetypeRepository が永続化を担当．ArchetypeFactory が生成を担当． |
| 配置理由 | ビジネスの中心概念なので domain に置く． |

**カプセル化のポイント**: フィールドは `private`．外部からは getter で読み取り，
生成は Factory 経由で行い，直接 setter で無秩序に書き換えさせない．

### 2.2 MatchRecord（Entity）

| 項目 | 内容 |
| --- | --- |
| 役割 | 1 回の対戦記録（勝った試合）を表す Entity． |
| 責務 | 勝者・敗者のアーキタイプ ID，勝者の先攻後攻，メモ，日時を保持する． |
| フィールド | `winnerArchetypeId`，`loserArchetypeId`，`PlayOrder playOrder`，`memo`，`playedAt`，`createdAt`（すべて private） |
| 関係 | MatchRepository が永続化，MatchRecordFactory が生成を担当． |
| 配置理由 | 対戦という中心概念なので domain に置く． |

> クラス名を `Match` ではなく `MatchRecord` にしているのは，`match` が予約的な語感で
> 紛らわしいこと，そして「対戦の記録」という意味を明確にするためです．
> DB テーブル名は `matches` のままです（`@Table(name = "matches")` で対応）．
>
> **勝者記録モデル**: 本アプリは「勝った試合」だけを 1 件登録します．勝者・敗者を
> `winnerArchetypeId` / `loserArchetypeId` で保持し，勝敗（result）フィールドは持ちません．
> 1 試合を勝者視点で 1 レコードにすることで，両者視点での二重登録を防ぎます．

### 2.3 enum 設計

```java
public enum PlayOrder {
    FIRST,
    SECOND,
    UNKNOWN
}
```

| enum | 用途 |
| --- | --- |
| PlayOrder | 勝者が先攻・後攻・不明のいずれだったかを表す． |

> 勝敗を表す `MatchResult` enum は，勝者記録モデルへの変更に伴い廃止しました
> （勝った試合しか登録しないため result が不要になったため）．

**enum を使う理由**

| 効果 | 説明 |
| --- | --- |
| 文字列の表記ゆれ防止 | `"first"` `"FIRST"` `"先攻"` などのばらつきを防ぎ，値を固定する． |
| バリデーション簡易化 | 受け取った文字列を `valueOf` で変換するだけで，不正値を検出できる． |
| コードの読みやすさ向上 | `if (playOrder == PlayOrder.FIRST)` のように意図が明確になる． |

DB には文字列（VARCHAR）で保存し，JPA では `@Enumerated(EnumType.STRING)` を使います．
`ORDINAL` ではなく `STRING` を使うのは，enum の並び順を変えてもデータが壊れないようにするためです．

---

## 3. dto（データ転送オブジェクト）

DTO は API の入出力専用のオブジェクトで，Entity を直接外部に晒さないために使います．

| DTO | 向き | 用途 |
| --- | --- | --- |
| ArchetypeCreateRequest | 入力 | アーキタイプ登録リクエスト（name） |
| ArchetypeResponse | 出力 | アーキタイプ 1 件の応答 |
| MatchCreateRequest | 入力 | 対戦記録登録リクエスト |
| MatchResponse | 出力 | 対戦記録 1 件の応答（アーキタイプ名を含む） |
| MatrixRowResponse | 出力 | マトリクスの 1 行（対面ごとの勝率） |
| MatrixResponse | 出力 | マトリクス全体（rows の配列） |
| ErrorResponse | 出力 | エラー応答の共通形式 |

**なぜ Entity を直接返さないのか**

- API の項目（例: `winnerArchetypeName`）は Entity に無い表示用の情報を含むため．
- Entity をそのまま返すと，DB 構造の変更が API を壊しやすくなる（結合が強くなる）．
- 入力用 DTO にバリデーションアノテーションを付けることで，検証を 1 か所に集約できる．

---

## 3.5 exception（共通エラーハンドリング・A が提供）

全 API のエラーを共通形式で返すための土台です．**A が基盤として提供**し，B・C・D は
「例外を投げるだけ」で API_SPEC のエラー JSON が返るようにします（各 Controller が
エラー整形の責務を持たない＝高凝集，担当間の結合も A に集約）．

| クラス | 役割 |
| --- | --- |
| `dto.ErrorResponse` | エラー応答の共通形式（error / message / path / timestamp） |
| `ValidationException` | Service 層で検出した入力値・業務ルール違反（→ 400 / VALIDATION_ERROR） |
| `ArchetypeNotFoundException` | アーキタイプ ID が存在しない（→ 404 / ARCHETYPE_NOT_FOUND） |
| `ArchetypeAlreadyExistsException` | 同名アーキタイプが既に存在（→ 409 / ARCHETYPE_ALREADY_EXISTS） |
| `GlobalExceptionHandler` | `@RestControllerAdvice`．例外を捕捉して `ErrorResponse` に整形する |

`GlobalExceptionHandler` が対応するエラーコード（[API_SPEC.md](API_SPEC.md) 準拠）:

| 例外 / 状況 | HTTP | error コード |
| --- | --- | --- |
| `MethodArgumentNotValidException`（`@Valid` 検証失敗）・不正 JSON | 400 | `VALIDATION_ERROR` |
| `ValidationException`（Service 層の業務ルール違反） | 400 | `VALIDATION_ERROR` |
| `ArchetypeNotFoundException` | 404 | `ARCHETYPE_NOT_FOUND` |
| `ArchetypeAlreadyExistsException` | 409 | `ARCHETYPE_ALREADY_EXISTS` |
| その他の想定外例外 | 500 | `INTERNAL_ERROR` |

**担当ごとの使い方**

- B: 重複検出時に `throw new ArchetypeAlreadyExistsException()`
- C: 存在しない ID のとき `throw new ArchetypeNotFoundException()`
- C: 勝者 ID＝敗者 ID や不正な playOrder など Service で検出した違反は `throw new ValidationException("...")` → 400
- 全員: DTO に `@NotBlank` `@Size` などを付けて `@Valid` するだけで，検証失敗は自動的に 400 になる

---

## 4. controller（HTTP を受ける層）

Controller は「受け取って，Service / Command に渡して，結果を返すだけ」に保ちます．

### 4.1 REST Controller の想定メソッド

```java
// ArchetypeController
@GetMapping("/api/archetypes")
List<ArchetypeResponse> getAllArchetypes();

@PostMapping("/api/archetypes")
ResponseEntity<ArchetypeResponse> createArchetype(@RequestBody @Valid ArchetypeCreateRequest request);

// MatchController
@GetMapping("/api/matches")
List<MatchResponse> getAllMatches();

@PostMapping("/api/matches")
ResponseEntity<MatchResponse> createMatch(@RequestBody @Valid MatchCreateRequest request);

// MatrixController
@GetMapping("/api/matrix")
MatrixResponse getMatrix();
```

### 4.2 ViewController（画面ルーティング）

```java
@GetMapping("/archetypes")
String archetypesPage();   // templates/archetypes.html を返す

@GetMapping("/matches")
String matchesPage();      // templates/matches.html を返す

@GetMapping("/matrix")
String matrixPage();       // templates/matrix.html を返す
```

| クラス | 役割 | 依存先 |
| --- | --- | --- |
| ArchetypeController | アーキタイプ API の入口 | ArchetypeService（または Command 経由） |
| MatchController | 対戦記録 API の入口 | MatchService（または Command 経由） |
| MatrixController | マトリクス API の入口 | MatrixService（または Command 経由） |
| ViewController | 画面 HTML を返す | なし（テンプレート名を返すだけ） |

**初心者が迷いやすいポイント**

- Controller にビジネスロジックや計算を書かない（Service / Command に置く）．
- 画面用 Controller（ViewController）と API 用 Controller を分ける．
- `@Controller`（画面）と `@RestController`（JSON）を混同しない．

---

## 5. service（業務ロジック層）

### 5.1 想定メソッド

```java
// ArchetypeService
List<ArchetypeResponse> getAllArchetypes();
ArchetypeResponse createArchetype(ArchetypeCreateRequest request);
Archetype findById(Long id);   // 存在しなければ ARCHETYPE_NOT_FOUND 例外

// MatchService
List<MatchResponse> getAllMatches();
MatchResponse createMatch(MatchCreateRequest request);

// MatrixService
MatrixResponse calculateMatrix();
```

| クラス | 責務 | 依存先 |
| --- | --- | --- |
| ArchetypeService | アーキタイプの取得・登録・重複チェック・ID 存在確認 | ArchetypeRepository, ArchetypeFactory |
| MatchService | 対戦記録の取得・登録・アーキタイプ存在確認 | MatchRepository, MatchRecordFactory, ArchetypeRepository |
| MatrixService | 勝率マトリクスの計算 | StandardMatrixBuilder（Template Method） |

**高凝集のポイント**: Service を機能ごとに分けることで，各クラスが 1 つの関心事だけを持ちます．
アーキタイプの変更が Match の実装に波及しにくくなります．

**初心者が迷いやすいポイント**

- MatchService は「アーキタイプが存在するか」を A の `ArchetypeRepository`（`existsById` / `findById`）で
  直接確認する（B の `ArchetypeService` には依存させず，担当間の結合を A だけに絞る）．
- `MatchResponse` に載せるアーキタイプ名は，`ArchetypeRepository.findAllById(...)` などで一括取得して
  Map 化してから引く（B の API を呼ばずに済ませ，かつ N+1 を避ける）．
- Response DTO への詰め替え（Entity → DTO）は Service で行う．

---

## 6. repository（DB アクセス層）

```java
public interface ArchetypeRepository extends JpaRepository<Archetype, Long> {
    boolean existsByNameIgnoreCase(String name);
    Optional<Archetype> findByNameIgnoreCase(String name);
}

public interface MatchRepository extends JpaRepository<MatchRecord, Long> {
    List<MatchRecord> findAllByOrderByPlayedAtDesc();
}
```

| クラス | 役割 |
| --- | --- |
| ArchetypeRepository | アーキタイプの CRUD と，名前による存在確認・検索． |
| MatchRepository | 対戦記録の CRUD と，日時降順の履歴取得． |

- `JpaRepository` を継承するだけで，基本的な CRUD（save / findAll / findById 等）が使えます．
- メソッド名からクエリが自動生成される（`existsByNameIgnoreCase` 等）ので，SQL を書かずに済みます．
- **配置理由**: DB アクセスの詳細を 1 層に閉じ込め，Service がクエリの詳細を知らずに済むようにします．

---

## 7. デザインパターン

課題要件を満たすため，以下の 3 種類を**今回の設計対象クラスとして採用**します．
「将来拡張として説明するだけ」ではなく，実際にコードで確認できる形にします．

| パターン | 目的 | 主なクラス |
| --- | --- | --- |
| Factory | DTO からドメインオブジェクトを生成する責務を分離する | ArchetypeFactory, MatchRecordFactory |
| Command | アプリの主要操作を「コマンド」として抽象化し，ポリモーフィズムで扱う | Command<T> ほか |
| Template Method | マトリクス生成の共通手順を親クラスにまとめ，具体処理を子クラスに委ねる | AbstractMatrixBuilder, StandardMatrixBuilder |

> Strategy パターンは今回は必須にせず，Factory / Command / Template Method を優先します．
> この 3 つで「生成の分離」「操作の抽象化＋ポリモーフィズム」「継承による手順共通化」を
> それぞれ示せるため，課題の評価観点に対して過不足がありません．

---

### 7.1 Factory Pattern

**使用クラス**: `pattern.factory.ArchetypeFactory`，`pattern.factory.MatchRecordFactory`

**用途**: DTO（リクエスト）からドメインオブジェクト（Entity）を生成する．

```
ArchetypeCreateRequest → ArchetypeFactory → Archetype
MatchCreateRequest     → MatchRecordFactory → MatchRecord
```

**設計上のポイント**

| 観点 | 説明 |
| --- | --- |
| どこで使うか | Service の登録処理（createArchetype / createMatch）の中で使う． |
| なぜ Service で直接 new しないのか | 生成手順（enum 変換・初期値設定・日時付与など）が増えても，Service が肥大化しないようにするため． |
| どの責務を分離しているか | 「DTO を解釈してドメインを組み立てる責務」を Factory に切り出す． |
| 何が良くなるか | 生成ロジックが 1 か所に集まり，テストしやすく，変更に強くなる（変更の影響が Factory 内に閉じる）． |

例（設計イメージ，実装は各担当）:

```java
public Archetype create(ArchetypeCreateRequest request) {
    // name の正規化や createdAt の付与などをここで一元的に行う
    return new Archetype(request.getName(), LocalDateTime.now());
}
```

---

### 7.2 Command Pattern

**使用クラス**: `Command<T>`，`CreateArchetypeCommand`，`CreateMatchCommand`，
`GenerateMatrixCommand`，`CommandInvoker`

**用途**: 主要なアプリ操作（アーキタイプ作成・対戦記録作成・マトリクス生成）を
「コマンド」として分離し，同じ入口（`execute()`）で扱う．

```java
public interface Command<T> {
    T execute();
}
```

```java
public class CreateMatchCommand implements Command<MatchResponse> {
    private final MatchService matchService;
    private final MatchCreateRequest request;
    // コンストラクタで依存と入力を受け取る
    @Override
    public MatchResponse execute() {
        return matchService.createMatch(request);
    }
}
```

```java
public class CommandInvoker {
    public <T> T invoke(Command<T> command) {
        // 将来ここにログ・監査・計測・トランザクション境界などを差し込める
        return command.execute();
    }
}
```

**設計上のポイント**

| 観点 | 説明 |
| --- | --- |
| Controller を薄く保つ | Controller は Command を組み立てて Invoker に渡すだけ．複雑な処理を持たない． |
| 1 操作 1 責務 | 各 Command クラスが 1 つの操作だけを表す（高凝集）． |
| ポリモーフィズム | 異なる操作を同じ `Command<T>` 型・同じ `execute()` で扱える（ジェネリクスで戻り値型も安全）． |
| 拡張しやすさ | 将来 CommandInvoker にログ・監査・非同期化・リトライを一括で追加できる． |

**ポリモーフィズムがどこで見えるか**: `CommandInvoker.invoke(Command<T>)` は，
渡された具象コマンドの型を知らずに `execute()` を呼びます．実際に動くのは
`CreateMatchCommand` なのか `GenerateMatrixCommand` なのか，実行時に決まります．

---

### 7.3 Template Method Pattern

**使用クラス**: `pattern.template.AbstractMatrixBuilder`（抽象），
`pattern.template.StandardMatrixBuilder`（具象）

**用途**: 勝率マトリクス生成の**共通手順**を親クラスにまとめ，
具体的な取得・行生成・集計処理を子クラスで実装する．

```java
public abstract class AbstractMatrixBuilder {

    // ★ テンプレートメソッド（手順を固定する。final で上書きを禁止）
    public final MatrixResponse build() {
        List<Archetype> archetypes = loadArchetypes();        // 手順1
        List<MatchRecord> matches = loadMatches();            // 手順2
        List<MatrixRowResponse> rows = new ArrayList<>();
        for (Archetype me : archetypes) {                     // 手順3: 組み合わせ
            for (Archetype opp : archetypes) {
                if (me.getId().equals(opp.getId())) continue; // 対角は除外
                MatrixRowResponse row = buildRow(me, opp, matches); // 手順4,5
                if (row != null) rows.add(row);
            }
        }
        return new MatrixResponse(rows);                      // 手順6
    }

    // ↓ 子クラスが差し替える処理（抽象メソッド）
    protected abstract List<Archetype> loadArchetypes();
    protected abstract List<MatchRecord> loadMatches();
    protected abstract MatrixRowResponse buildRow(
            Archetype me, Archetype opponent, List<MatchRecord> matches);
}
```

親クラス側で表現する手順（最低限）:

1. アーキタイプ一覧を取得する
2. 対戦記録一覧を取得する
3. アーキタイプの組み合わせを作る
4. 勝ち数・負け数を数える（wins = 行が相手に勝った件数，losses = 相手が行に勝った件数）
5. 勝率を計算する（winRate = wins / (wins + losses) × 100）
6. `MatrixResponse` として返す

`StandardMatrixBuilder` は，`loadArchetypes` / `loadMatches` を Repository から取得する形で実装し，
`buildRow` で対面ごとの wins / losses / total / winRate を計算します．
勝った試合しか記録されないため，行 A・相手 B の対面では
wins = `winner=A, loser=B` の件数，losses = `winner=B, loser=A` の件数となり，
total（= wins + losses）が 0 の対面は行に含めません（ゼロ除算回避）．

**設計上のポイント**

| 観点 | 説明 |
| --- | --- |
| どこがテンプレートメソッドか | `build()`．手順の骨格を固定し，`final` で上書きを禁止する． |
| どこを子クラスで差し替えるか | `loadArchetypes` / `loadMatches` / `buildRow` の 3 つの抽象メソッド． |
| 継承が見える場所 | `StandardMatrixBuilder extends AbstractMatrixBuilder`． |
| ポリモーフィズムが見える場所 | `build()` 内で呼ぶ抽象メソッドが，実行時に子クラスの実装に解決される． |
| 将来の拡張 | 例えば「先攻限定マトリクス」を作りたければ，`FirstTurnMatrixBuilder` を追加して `buildRow` だけ差し替えればよい（`build()` は再利用）． |

---

## 8. OOP 要件との対応（まとめ）

| 要件 | 実現方法 |
| --- | --- |
| カプセル化 | Entity のフィールドを `private` にし，getter・Factory・Service 経由で操作する． |
| 継承 | `AbstractMatrixBuilder` を `StandardMatrixBuilder` が継承する． |
| ポリモーフィズム | `Command<T>` を通じて異なる操作を `execute()` で扱う／Template Method の抽象メソッド解決． |
| 疎結合 | Controller → Service → Repository の一方向依存．DTO で外部と Entity を分離． |
| 高凝集 | Service を Archetype / Match / Matrix に分割し，各クラスの関心事を 1 つに絞る． |
| 変更に強い設計 | 生成（Factory）・計算（Template）を独立クラスに分け，変更の影響範囲を局所化する． |

---

## 9. クラス図

Mermaid ソース: [assets/class_diagram.mmd](assets/class_diagram.mmd)．
パターン関係図: [assets/patterns_diagram.mmd](assets/patterns_diagram.mmd)．
これらはレポート PDF・スライドにそのまま貼れる形で用意しています．
