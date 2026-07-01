# Duel Matrix レポート骨子（PDF 用）

このドキュメントは，提出用 PDF レポートの骨子です．各章に「何を書くか」と
「書くときのポイント」をまとめてあるので，そのまま肉付けして本文にできます．
図は `docs/assets/*.mmd` を画像化して貼り付けてください．

---

## 章立て

### 1. プロジェクト概要

- Duel Matrix が何をするアプリか（対戦記録＋勝率マトリクス）を 3〜4 行で説明．
- スクリーンショットまたはマトリクス表示例を 1 枚入れると分かりやすい．

### 2. テーマ選定理由

- なぜ TCG の対戦記録アプリを題材に選んだか（身近で，勝率という明確な計算対象がある）．
- 「対面ごとの勝率」という中心機能が，OOP とデザインパターンを見せやすいことに触れる．

### 3. Duel Matrix と TCG Playbook の関係

- Duel Matrix = 授業課題用 Java プロトタイプ．
- TCG Playbook = 将来の本番ネイティブアプリ．
- 今回は TCG Playbook の一部機能を切り出して実装したことを説明．

### 4. 今回のスコープ

- 含める機能／含めない機能を表で示す（[PROJECT_SPEC.md](PROJECT_SPEC.md) 4 節を流用）．
- 将来拡張（申請承認・CSV・PostgreSQL・Go・iOS・複数 TCG）は「構想のみ」と明記．

### 5. システムの目的

- 対戦記録を蓄積し，アーキタイプ相性を数値で把握できるようにすること．

### 6. アーキテクチャ

- Controller → Service → Repository のレイヤ構成を図で示す（[PROJECT_SPEC.md](PROJECT_SPEC.md) 5 節）．
- 各層の責務を簡潔に説明．

### 7. DB 設計

- archetypes / matches の 2 テーブル最小構成の説明（[DB_SCHEMA.md](DB_SCHEMA.md)）．
- 主キー・外部キー・インデックスの理由を表で示す．

### 8. ER 図

- `assets/er_diagram.mmd` を画像化して貼る．

### 9. クラス図

- `assets/class_diagram.mmd` を画像化して貼る．
- パッケージ構成（controller / service / repository / domain / dto / pattern）を併記．

### 10. 使用したデザインパターン

- Factory / Command / Template Method の 3 つを採用したことを明示．
- `assets/patterns_diagram.mmd` を貼る．

### 11. 各パターンの使用箇所

| パターン | 使用箇所 |
| --- | --- |
| Factory | ArchetypeFactory / MatchRecordFactory（DTO → Entity 生成） |
| Command | CreateArchetypeCommand / CreateMatchCommand / GenerateMatrixCommand + CommandInvoker |
| Template Method | AbstractMatrixBuilder（build）→ StandardMatrixBuilder |

### 12. 各パターンを使った理由

- Factory: 生成ロジックを Service から分離し，変更に強くするため．
- Command: 操作を抽象化し，Controller を薄く保ち，ポリモーフィズムを示すため．
- Template Method: マトリクス生成手順を親クラスで固定し，具体処理を子で差し替えられるようにするため．
- 「形式的に入れた」ではなく「実際の設計課題を解いた」ことを強調する．

### 13. OOP 要件との対応

- カプセル化・継承・ポリモーフィズムがコードのどこで確認できるかを表で示す
  （[PROJECT_SPEC.md](PROJECT_SPEC.md) 6 節，[CLASS_DESIGN.md](CLASS_DESIGN.md) 8 節）．

### 14. 疎結合・高凝集の工夫

- レイヤの一方向依存，DTO による Entity 分離，Service の機能分割を説明．

### 15. Git を使ったチーム開発の方法

- Issue → branch → PR / MR → develop → main の流れ（[DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md)）．
- コミットプレフィックス規約と，履歴で貢献が分かることに触れる．

### 16. 各メンバーの担当

- A / B / C / D の分担を表で示し，Git 履歴（PR / コミット）と対応づける．

### 17. 実装上の工夫

- enum による表記ゆれ防止，バリデーションの一元化，N+1 回避，ゼロ除算回避など．

### 18. 自己評価・振り返り

- うまくいった点・難しかった点・次にやるなら改善したい点を率直に書く．

### 19. 今後の展望

- TCG Playbook への発展（PostgreSQL / Go / iOS / 複数 TCG / 調整ノート）．

---

## レポートで特に押さえるべきポイント

以下は採点観点に直結するので，必ず本文で明示的に説明してください．

- **なぜ API サーバという形でもソフトウェアシステムとして成立するのか**
  → HTTP 入力から永続化・応答までを 1 つのアプリで完結させており，画面（Thymeleaf）も
     同居しているため，GUI アプリと同様に設計対象になる．
- **なぜ最小構成に絞ったのか**
  → 評価対象は機能量ではなく設計の質．中心機能に絞ることで OOP とパターンの意図を明確にできる．
- **なぜ Factory / Command / Template Method を選んだのか**
  → 生成の分離・操作の抽象化とポリモーフィズム・手順共通化という 3 つの異なる設計課題に
     それぞれ自然に対応するため．
- **なぜ H2 + Docker Compose + Spring Boot にしたのか**
  → H2 は設定が軽く授業向き，Docker Compose で環境を統一でき，Spring Boot は
     Controller/Service/Repository の分離を実践しやすいため．
