/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2016
 */
package org.bitbucket.ucchy.moe;

import java.util.HashMap;
import java.util.List;

/**
 * 詠唱をどこまでおこなったかを格納するクラス
 * @author ucchy
 */
public class SpellProcessPhases {

    /** 詠唱と詠唱の間隔の有効時間(1分間) */
    private static final int VALIDITY_PERIOD = 60 * 1000;

    /** 前回の詠唱の時刻 */
    private long previousTime;

    /** どのスペルを何行目まで詠唱したか */
    private HashMap<String, Integer> phases;

    /**
     * コンストラクタ
     */
    public SpellProcessPhases() {
        previousTime = System.currentTimeMillis();
        phases = new HashMap<String, Integer>();
    }

    /**
     * 発言がスペルの詠唱かどうかをチェックする
     * @param spells スペル
     * @param chat 発言内容
     * @return 魔法を発射可能になったかどうか
     */
    public boolean check(HashMap<String, List<String>> spells, String chat) {

        // 前回の詠唱から1分以上過ぎている場合は、すべてのフェーズをクリアする
        if ( System.currentTimeMillis() - previousTime > VALIDITY_PERIOD ) {
            phases.clear();
        }

        // 発言時刻を記録する
        previousTime = System.currentTimeMillis();

        // スペルの詠唱をチェックする
        for ( String key : spells.keySet() ) {

            if ( !phases.containsKey(key) ) {
                phases.put(key, 0);
            }

            int next = phases.get(key);

            // 全て詠唱仕切っているなら、このスペルのチェックはスキップする
            if ( next >= spells.get(key).size() ) continue;

            String spell = spells.get(key).get(next);
            chat = chat.replaceAll("[ 　!！、。]", ""); // 不要な文字をすべて消す

            // このスペルを詠唱したか。
            if ( chat.matches(spell) ) {
                next++;
                phases.put(key, next);
                if ( next >= spells.get(key).size() ) {
                    // 詠唱しきった。すべてのフェーズをクリアして、trueを返す
                    phases.clear();
                    return true;
                }
            } else {
                // 違う言葉をしゃべったようなので、このスペルの段階は0に戻す
                phases.put(key, 0);
            }
        }

        return false;
    }
}
