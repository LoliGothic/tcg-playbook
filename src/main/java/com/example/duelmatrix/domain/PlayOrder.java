package com.example.duelmatrix.domain;

/**
 * 対戦時の先攻・後攻・不明を表す enum．
 * DB には文字列（VARCHAR）で保存する．未入力時は {@link #UNKNOWN} を既定とする．
 */
public enum PlayOrder {
    FIRST,
    SECOND,
    UNKNOWN
}
