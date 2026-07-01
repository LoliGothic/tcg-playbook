-- =====================================================================
-- Duel Matrix 初期データ（H2 Database 用 / 勝者記録モデル）
-- ---------------------------------------------------------------------
-- Spring Boot 起動時に schema.sql の後で実行される．
-- 設計の正本は docs/assets/data.sql（内容は同一）．
--
-- モデル: 勝った試合のみを「勝者・敗者」で 1 件登録する．
--   例）青白ウィリデが創世竜に勝った試合 → (winner=青白ウィリデ, loser=創世竜)
--
-- 実行順序メモ:
--   archetypes を先に INSERT してから matches を INSERT すること．
--   matches は archetypes の id を外部キーとして参照する．
-- =====================================================================

-- --- アーキタイプ（デッキの種類）: id は 1,2,3,4 の順で採番される想定 ---
INSERT INTO archetypes (name) VALUES ('青白ウィリデ');  -- id=1
INSERT INTO archetypes (name) VALUES ('創世竜');        -- id=2
INSERT INTO archetypes (name) VALUES ('黒緑アビス');    -- id=3
INSERT INTO archetypes (name) VALUES ('白緑ウィリデ');  -- id=4

-- --- 対戦記録（勝った試合のみ） ---

-- 青白ウィリデ(1) が 創世竜(2) に勝った試合 … 6 件
INSERT INTO matches (winner_archetype_id, loser_archetype_id, play_order, memo, played_at) VALUES
(1, 2, 'FIRST',   '先攻で押し切れた',   TIMESTAMP '2026-07-01 20:30:00'),
(1, 2, 'SECOND',  '受け切って勝ち',     TIMESTAMP '2026-07-02 21:00:00'),
(1, 2, 'FIRST',   NULL,                 TIMESTAMP '2026-07-03 19:15:00'),
(1, 2, 'UNKNOWN', NULL,                 TIMESTAMP '2026-07-04 20:00:00'),
(1, 2, 'FIRST',   NULL,                 TIMESTAMP '2026-07-05 20:45:00'),
(1, 2, 'SECOND',  NULL,                 TIMESTAMP '2026-07-06 18:30:00');

-- 創世竜(2) が 青白ウィリデ(1) に勝った試合 … 4 件
INSERT INTO matches (winner_archetype_id, loser_archetype_id, play_order, memo, played_at) VALUES
(2, 1, 'FIRST',   '早期展開で押し切り', TIMESTAMP '2026-07-07 20:00:00'),
(2, 1, 'FIRST',   NULL,                 TIMESTAMP '2026-07-08 21:10:00'),
(2, 1, 'SECOND',  NULL,                 TIMESTAMP '2026-07-09 19:40:00'),
(2, 1, 'UNKNOWN', NULL,                 TIMESTAMP '2026-07-10 22:05:00');
-- → 青白ウィリデ 対 創世竜 は 6 勝 4 敗（勝率 60% / 40%）

-- 黒緑アビス(3) が 創世竜(2) に勝った試合 … 3 件
INSERT INTO matches (winner_archetype_id, loser_archetype_id, play_order, memo, played_at) VALUES
(3, 2, 'FIRST',   'ハンデスが刺さった', TIMESTAMP '2026-07-11 20:00:00'),
(3, 2, 'SECOND',  NULL,                 TIMESTAMP '2026-07-12 20:30:00'),
(3, 2, 'UNKNOWN', NULL,                 TIMESTAMP '2026-07-13 21:00:00');

-- 創世竜(2) が 黒緑アビス(3) に勝った試合 … 10 件
INSERT INTO matches (winner_archetype_id, loser_archetype_id, play_order, memo, played_at) VALUES
(2, 3, 'FIRST',   NULL, TIMESTAMP '2026-07-14 19:00:00'),
(2, 3, 'FIRST',   NULL, TIMESTAMP '2026-07-15 19:20:00'),
(2, 3, 'SECOND',  NULL, TIMESTAMP '2026-07-16 19:40:00'),
(2, 3, 'FIRST',   NULL, TIMESTAMP '2026-07-17 20:00:00'),
(2, 3, 'UNKNOWN', NULL, TIMESTAMP '2026-07-18 20:20:00'),
(2, 3, 'FIRST',   NULL, TIMESTAMP '2026-07-19 20:40:00'),
(2, 3, 'SECOND',  NULL, TIMESTAMP '2026-07-20 21:00:00'),
(2, 3, 'FIRST',   NULL, TIMESTAMP '2026-07-21 21:20:00'),
(2, 3, 'UNKNOWN', NULL, TIMESTAMP '2026-07-22 21:40:00'),
(2, 3, 'FIRST',   NULL, TIMESTAMP '2026-07-23 22:00:00');
-- → 黒緑アビス 対 創世竜 は 3 勝 10 敗（勝率 約23.1% / 約76.9%）
