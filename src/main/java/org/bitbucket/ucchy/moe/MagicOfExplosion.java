/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2016
 */
package org.bitbucket.ucchy.moe;

import java.io.File;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * 爆裂魔法プラグイン
 * @author ucchy
 */
public class MagicOfExplosion extends JavaPlugin implements Listener {

    private MOEConfig config;
    private HashMap<String, SpellProcessPhases> phases;

    /**
     * コンストラクタ
     */
    public MagicOfExplosion() {
        phases = new HashMap<String, SpellProcessPhases>();
    }

    /**
     * プラグインが有効化されたときに呼び出されるメソッドです。
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {

        // コンフィグのロード
        reloadMOEConfig();

        // リスナーの登録
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    /**
     * コンフィグファイルをリロードする
     */
    private void reloadMOEConfig() {

        // 必要に応じて、config.ymlを新規作成
        File configFile = new File(getDataFolder(), "config.yml");
        if ( !configFile.exists() ) {
            Utility.copyFileFromJar(getFile(), configFile, "config_ja.yml", false);
        }

        // コンフィグファイルの読み込み
        reloadConfig(); // もう要らないけど、念のためやっとく。

        config = new MOEConfig(configFile);
    }

    /**
     * プレイヤーがチャット発言したときに呼び出されるメソッドです。
     * @param event
     */
    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {

        final Player player = event.getPlayer();

        // 詠唱履歴が無いなら、新規作成する
        if ( !phases.containsKey(player.getName()) ) {
            phases.put(player.getName(), new SpellProcessPhases());
        }

        // 詠唱履歴をチェックする。trueが返されたら、詠唱完了。
        if ( phases.get(player.getName()).check(config.getSpells(), event.getMessage()) ) {
            if ( player.getFoodLevel() < 20 ) {
                new BukkitRunnable() {
                    public void run() {
                        player.sendMessage("スタミナが足りなくて魔法が発動しなかった...");
                    }
                }.runTaskLater(this, 2);
            } else {
                new BukkitRunnable() {
                    public void run() {
                        launchMagic(player);
                        player.setFoodLevel(0);
                        player.setExhaustion(0);
                    }
                }.runTaskLater(this, 2);
            }
        }
    }

    private void launchMagic(Player player) {

        // 魔法を発射する
        Vector velocity = player.getLocation().getDirection();
        velocity.normalize().multiply(config.getMagicSpeed());

        Snowball ball = player.launchProjectile(Snowball.class, velocity);
        ball.setMetadata("MagicOfExplosion", new FixedMetadataValue(this, true));

        // エフェクト生成
        Location location = ball.getLocation();
        Firework fw = (Firework)player.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        fwm.setPower(10);
        fw.setFireworkMeta(fwm);
        ball.setPassenger(fw);

        // 効果音を鳴らす
        location.getWorld().playSound(location, SoundEnum.NOTE_BASS_DRUM.getBukkit(), 1, 0.7F);
        location.getWorld().playSound(location, SoundEnum.CHEST_CLOSE.getBukkit(), 1, 1.4F);
    }

    /**
     * 飛来物が着弾したときに呼び出されるメソッドです。
     * @param event
     */
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {

        Projectile proj = event.getEntity();

        // 雪玉じゃないなら無視する
        if ( !(proj instanceof Snowball) ) {
            return;
        }

        // メタデータを含んでいないなら無視する。
        if ( !proj.hasMetadata("MagicOfExplosion") ) {
            return;
        }

        // 載せているエフェクト用の花火を除去する
        Entity passenger = proj.getPassenger();
        if ( passenger != null ) {
            proj.eject();
            passenger.remove();
        }

        // 爆発を起こす
        Location loc = proj.getLocation();
        proj.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(),
                (float)config.getExplosionPower(), false, config.isDestructTerrain());

        // 演出用の花火を起こす
        for ( Color color : new Color[]{Color.RED, Color.ORANGE, Color.MAROON} ) {
            Firework fw = (Firework)loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
            FireworkMeta fwm = fw.getFireworkMeta();
            FireworkEffect effect = FireworkEffect.builder()
                    .flicker(true)
                    .withColor(color)
                    .withFade(Color.BLACK)
                    .with(Type.BALL_LARGE)
                    .trail(true)
                    .build();
            fwm.addEffect(effect);
            fw.setFireworkMeta(fwm);
            fw.detonate();
        }
    }

    /**
     * このプラグインのインスタンスを取得します。
     * @return このプラグインのインスタンス
     */
    public static MagicOfExplosion getInstance() {
        return (MagicOfExplosion)Bukkit.getPluginManager().getPlugin("MagicOfExplosion");
    }
}
