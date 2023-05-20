package com.github.kumo0621.hanahuda;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

public final class Hanahuda extends JavaPlugin implements org.bukkit.event.Listener {
    @Override
    public void onEnable() {
        // configファイルをロード
        config = getConfig();
        // config2ファイルをロードまたは生成
        config2 = loadConfig("config2.yml");
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        // configファイルを保存
        saveConfig();
        // config2ファイルを保存
        saveCustomConfig(config2, "config2.yml");
    }

    private FileConfiguration loadConfig(String fileName) {
        File configFile = new File(getDataFolder(), fileName);

        if (!configFile.exists()) {
            saveResource(fileName, false);
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }

    private void saveCustomConfig(FileConfiguration config, String fileName) {
        try {
            File configFile = new File(getDataFolder(), fileName);
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String setCount = "0";
    boolean kill = false;
    boolean start = false;
    FileConfiguration config;
    FileConfiguration config2;


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        String set = config.getString(setCount);
        if (start) {
            if (clickedBlock != null) {
                // ブロックの位置を取得
                Location location = clickedBlock.getLocation();
                // ブロックの種類を取得
                Material blockType = clickedBlock.getType();
                GameMode gameMode = event.getPlayer().getGameMode();
                if (gameMode == GameMode.ADVENTURE) {
                    if (Objects.equals(Objects.requireNonNull(set).toUpperCase(), blockType.toString())) {
                        Bukkit.broadcastMessage(event.getPlayer().getName() + "さんが正解しました。1ptです。");
                        addScore(event.getPlayer());
                        // ブロックを取得してブロックの種類をAIRに設定
                        Block block = location.getBlock();
                        block.setType(Material.AIR);
                        playLevelUpSoundToAllPlayers();
                        kill = false;
                    } else if (kill) {
                        Player player = event.getPlayer();
                        player.getInventory().clear();
                        player.setHealth(0);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (start) {
            // カスタムの死亡メッセージを設定
            event.setDeathMessage("お手つきのため " + event.getEntity().getName() + " は、死亡した。");
        }
    }

    public void onGivePlayer(String item) {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        for (Player player : players) {
            ItemStack itemStack = new ItemStack(Material.valueOf(item), 1);
            player.getInventory().clear();
            player.getInventory().addItem(itemStack);
            player.updateInventory();
        }
    }

    public static void addScore(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective("score");
        if (objective != null) {
            Score score = objective.getScore(player);
            int currentScore = score.getScore();
            int newScore = currentScore + 1;
            score.setScore(newScore);
        }
    }
    public void playLevelUpSoundToAllPlayers() {
        Sound sound = Sound.ENTITY_PLAYER_LEVELUP;
        float volume = 1.0f;
        float pitch = 2.0f;

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equals("karuta")) {
            if (sender instanceof Player) {
                if (args.length <= 0) {
                    sender.sendMessage("/karuta set or start or end");
                } else {
                    switch (args[0]) {
                        case "set":
                            setCount = String.valueOf(RandomCount.random());
                            onGivePlayer(Objects.requireNonNull(config2.getString(setCount)).toUpperCase());
                            kill = true;
                            sender.sendMessage("１枚引きました");
                            break;
                        case "start":
                            start = true;
                            sender.sendMessage("かるたを開始しました");
                            String title = "50人クラフトかるた";
                            String subtitle = "イラスト:おすぎ@o0sugi0o プラグイン:くも";
                            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                                    "title @a subtitle {\"text\":\"" + subtitle + "\"}");
                            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                                    "title @a title {\"text\":\"" + title + "\"}");
                            break;
                        case "end":
                            start = false;
                            sender.sendMessage("かるたを停止しました");
                            break;
                        default:
                            sender.sendMessage("/karuta set or start or end");
                    }
                }
            }
        }
        return super.onCommand(sender, command, label, args);
    }
}
