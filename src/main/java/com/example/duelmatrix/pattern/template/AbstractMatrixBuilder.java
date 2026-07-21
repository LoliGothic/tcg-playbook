package com.example.duelmatrix.pattern.template;

import java.util.ArrayList;
import java.util.List;

import com.example.duelmatrix.domain.Archetype;
import com.example.duelmatrix.domain.MatchRecord;
import com.example.duelmatrix.dto.MatrixResponse;
import com.example.duelmatrix.dto.MatrixRowResponse;

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