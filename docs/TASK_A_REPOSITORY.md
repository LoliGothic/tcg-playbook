# TASK A: DB・Entity・Repository・Docker 環境構築

担当 A の作業仕様書です．**このプロジェクトの土台**を作る役割で，B・C・D 全員が
A の成果物に依存します．そのため最優先で着手し，早めに `develop` にマージしてください．

関連: [DB_SCHEMA.md](DB_SCHEMA.md)，[CLASS_DESIGN.md](CLASS_DESIGN.md)，[ENVIRONMENT.md](ENVIRONMENT.md)

---

## 1. 担当範囲

- データベース設計（archetypes / matches）
- Entity（`Archetype` / `MatchRecord`）と enum（`PlayOrder`）
- Repository インターフェース（`ArchetypeRepository` / `MatchRepository`）
- **コマンド基盤**（`Command<T>` インターフェース・`CommandInvoker`）… B・C・D が共有する土台
- SQL（`schema.sql` / `data.sql`）
- Docker 環境（`Dockerfile` / `docker-compose.yml` / `.dockerignore`）
- `application.properties` / `application-docker.properties` の基本設定
- 環境構築手順（[ENVIRONMENT.md](ENVIRONMENT.md)）

> ★ B・C・D を互いに独立させ，A だけに依存させるための重要ポイント:
> `Command<T>` と `CommandInvoker` は「誰か 1 人が作って共有」ではなく，**A が基盤として先に用意**します．
> これにより B・C・D は各自の具体コマンド（`CreateArchetypeCommand` など）を書くだけでよく，
> 共有ファイルの取り合いや作成待ちが発生しません．

---

## 2. 作成するファイル

| ファイル | 内容 |
| --- | --- |
| `domain/Archetype.java` | アーキタイプ Entity |
| `domain/MatchRecord.java` | 対戦記録 Entity |
| `domain/PlayOrder.java` | 先攻後攻 enum |
| `repository/ArchetypeRepository.java` | アーキタイプ Repository |
| `repository/MatchRepository.java` | 対戦記録 Repository |
| `pattern/command/Command.java` | コマンド共通インターフェース `Command<T>`（B・C・D 共有） |
| `pattern/command/CommandInvoker.java` | コマンド実行役（B・C・D 共有） |
| `src/main/resources/schema.sql` | テーブル定義（`docs/assets/schema.sql` を基に） |
| `src/main/resources/data.sql` | 初期データ（`docs/assets/data.sql` を基に） |
| `Dockerfile` | ビルド・実行イメージ（作成済み） |
| `docker-compose.yml` | 起動定義（作成済み） |
| `.dockerignore` | ビルド除外（作成済み） |
| `application.properties` / `application-docker.properties` | DB・H2 コンソール設定 |

> `docs/assets/schema.sql` と `docs/assets/data.sql` は設計フェーズで用意済みです．
> 実装時は `src/main/resources/` にコピーし，Spring Boot が起動時に読み込む設定にします．

---

## 3. 作成する Entity（設計指針）

### Archetype

| フィールド | 型 | 備考 |
| --- | --- | --- |
| id | Long | `@Id` `@GeneratedValue`，自動採番 |
| name | String | NOT NULL・UNIQUE，最大 100 文字 |
| createdAt | LocalDateTime | 生成時に設定 |

- フィールドはすべて `private`（カプセル化）．
- `@Entity` `@Table(name = "archetypes")` を付ける．
- getter は用意し，無秩序な setter は避ける（生成は Factory に任せる方針）．

### MatchRecord

| フィールド | 型 | 備考 |
| --- | --- | --- |
| id | Long | 自動採番 |
| winnerArchetypeId | Long | NOT NULL |
| loserArchetypeId | Long | NOT NULL |
| playOrder | PlayOrder | `@Enumerated(EnumType.STRING)`，既定 UNKNOWN（勝者の先攻後攻） |
| memo | String | NULL 可，最大 500 文字 |
| playedAt | LocalDateTime | NOT NULL |
| createdAt | LocalDateTime | 生成時に設定 |

- 勝者記録モデル．勝った試合のみ登録するため勝敗（result）は持たない．
- `@Table(name = "matches")` を付ける（クラス名と DB テーブル名を対応させる）．
- enum は必ず `EnumType.STRING` で保存する（並び順変更に強くするため）．

### enum

```java
public enum PlayOrder { FIRST, SECOND, UNKNOWN }
```

---

## 4. Repository インターフェース

```java
public interface ArchetypeRepository extends JpaRepository<Archetype, Long> {
    boolean existsByNameIgnoreCase(String name);
    Optional<Archetype> findByNameIgnoreCase(String name);
}

public interface MatchRepository extends JpaRepository<MatchRecord, Long> {
    List<MatchRecord> findAllByOrderByPlayedAtDesc();
}
```

- `JpaRepository` を継承するだけで基本 CRUD が使えます．
- メソッド名からクエリが自動生成されるので，SQL は書きません．

---

## 4.5 コマンド基盤（Command Pattern の土台）

B・C・D が共通で使う，Command Pattern の**土台部分だけ**を A が用意します．
具体的なコマンド（`CreateArchetypeCommand` 等）は各担当が自分で書くので，A は作りません．

```java
// pattern/command/Command.java
public interface Command<T> {
    T execute();
}
```

```java
// pattern/command/CommandInvoker.java
@Component
public class CommandInvoker {
    public <T> T invoke(Command<T> command) {
        // 将来ここにログ・監査・計測などを一括で差し込める
        return command.execute();
    }
}
```

- この 2 ファイルは中身が単純なので，A の基盤作業に含めても負担は小さいです．
- ここを A が先に確定させることで，B・C・D は互いを待たずに並行開発できます．

## 5. SQL 関連ファイル

- `schema.sql`: テーブル定義（[assets/schema.sql](assets/schema.sql) をコピー）．
- `data.sql`: 初期データ（[assets/data.sql](assets/data.sql) をコピー）．
- Spring Boot がこれらを起動時に実行するよう `application.properties` を設定する．
  （`spring.sql.init.mode` の設定や，JPA の `ddl-auto` との組み合わせに注意）

---

## 6. Docker 関連ファイル

`Dockerfile`・`docker-compose.yml`・`.dockerignore` は作成済みです．
A 担当は，これらでアプリが実際に起動することを確認し，手順を [ENVIRONMENT.md](ENVIRONMENT.md)
に反映してください（起動・停止・再ビルド・H2 コンソール確認）．

---

## 7. A が先に作らないと他担当が困るもの

| 成果物 | これを待つ担当 |
| --- | --- |
| `Archetype` / `MatchRecord` Entity | B・C・D 全員 |
| `PlayOrder` enum | C・D |
| `ArchetypeRepository` | B・C・D（C は存在確認・名前引きに使う） |
| `MatchRepository` | C・D |
| `Command<T>` / `CommandInvoker`（コマンド基盤） | B・C・D |
| Docker で起動できる状態 | 全員（動作確認に必要） |

→ これらが揃うまで B・C・D は実装を始めにくいため，**A は最優先**．

---

## 8. 完成条件

- [ ] archetypes と matches のテーブル設計が完了している．
- [ ] Entity（Archetype / MatchRecord）と enum の設計・実装が明確になっている．
- [ ] Repository インターフェースが用意されている．
- [ ] コマンド基盤（`Command<T>` / `CommandInvoker`）が用意されている．
- [ ] 初期データ（data.sql）が用意され，起動時に投入される．
- [ ] `docker compose up --build` でアプリが起動できる．
- [ ] http://localhost:8080/h2-console で DB の中身が確認できる．
- [ ] 手順が [ENVIRONMENT.md](ENVIRONMENT.md) に書かれている．

---

## 9. 他担当者への引き渡し事項

`develop` にマージしたら，以下を代表者経由で共有してください．

- Entity のフィールド名・型（B・C・D が DTO や Factory を作る際の基準になる）．
- enum の値（C・D が playOrder を扱う際に必要）．
- Repository のメソッド名（B は `existsByNameIgnoreCase`，C は `findAllByOrderByPlayedAtDesc` と
  `existsById` / `findById` / `findAllById`，D は `findAll` 系を使う）．
- コマンド基盤の使い方（`commandInvoker.invoke(command)` で呼ぶ）．
- Docker の起動方法（全員が動作確認に使う）．

> この 5 点さえ共有されれば，**B・C・D は互いに連絡を取り合わなくても並行して実装を進められます**．
