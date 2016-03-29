/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2016
 */
package org.bitbucket.ucchy.moe;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * 爆裂魔法プラグインのコンフィグクラス
 * @author ucchy
 */
public class MOEConfig {

    private boolean destructTerrain;
    private double explosionPower;
    private double magicSpeed;
    private HashMap<String, List<String>> spells;

    /**
     * コンストラクタ
     */
    public MOEConfig(File file) {

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        // 設定の読み込み
        destructTerrain = config.getBoolean("destructTerrain", false);
        explosionPower = config.getDouble("explosionPower", 15.0);
        magicSpeed = config.getDouble("magicSpeed", 3.0);

        // spellの読み込み
        spells = new HashMap<String, List<String>>();

        if ( config.contains("spells") ) {

            ConfigurationSection sub = config.getConfigurationSection("spells");
            for ( String key : sub.getKeys(false) ) {
                if ( !sub.isList(key) ) continue;

                ArrayList<String> results = new ArrayList<String>();
                for ( String line : sub.getStringList(key) ) {
                    line = line.replaceAll("[ 　!！、。]", ""); // 不要な文字をすべて消す
                    results.add(line);
                }
                if ( results.size() > 0 ) {
                    spells.put(key, results);
                }
            }
        }
    }

    public boolean isDestructTerrain() {
        return destructTerrain;
    }

    public double getExplosionPower() {
        return explosionPower;
    }

    public double getMagicSpeed() {
        return magicSpeed;
    }

    public HashMap<String, List<String>> getSpells() {
        return spells;
    }
}
