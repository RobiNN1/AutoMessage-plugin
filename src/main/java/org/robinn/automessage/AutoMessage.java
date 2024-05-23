package org.robinn.automessage;

import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class AutoMessage extends JavaPlugin {
    private final List<String> messages = new ArrayList<>();
    private int currentMessageIndex;
    private int schedulerTask;
    static MiniMessage mm = MiniMessage.miniMessage();
    private String prefix;

    public void onEnable() {
        saveDefaultConfig();

        initializeMessages();
    }

    public void onDisable() {
        cancelScheduler();
    }

    private void initializeMessages() {
        messages.clear();
        currentMessageIndex = 0;

        messages.addAll(getConfig().getStringList("messages"));

        prefix = getConfig().getString("prefix");

        startScheduler();
    }

    private void startScheduler() {
        int interval = getConfig().getInt("interval");

        schedulerTask = new BukkitRunnable() {
            @Override
            public void run() {
                String message = messages.get(currentMessageIndex);
                getServer().getOnlinePlayers().forEach(p -> sendMsg(p, message));
                //getLogger().info(message);
                currentMessageIndex = (currentMessageIndex + 1) % messages.size(); // Cycle through messages
            }
        }.runTaskTimer(this, 0, interval * 20L).getTaskId();

        getLogger().info("AutoMessage scheduler started.");
    }

    private void cancelScheduler() {
        getServer().getScheduler().cancelTask(schedulerTask);
        getLogger().info("AutoMessage scheduler canceled.");
    }

    private void sendMsg(Player p, String message) {
        p.sendMessage(mm.deserialize(legacyToMiniMessage(prefix + message)));
    }

    public static String legacyToMiniMessage(String message) {
        return message.replaceAll("&0", "<black>")
                       .replaceAll("&1", "<dark_blue>")
                       .replaceAll("&2", "<dark_green>")
                       .replaceAll("&3", "<dark_aqua>")
                       .replaceAll("&4", "<dark_red>")
                       .replaceAll("&5", "<dark_purple>")
                       .replaceAll("&6", "<gold>")
                       .replaceAll("&7", "<gray>")
                       .replaceAll("&8", "<dark_gray>")
                       .replaceAll("&9", "<blue>")
                       .replaceAll("&a", "<green>")
                       .replaceAll("&b", "<aqua>")
                       .replaceAll("&c", "<red>")
                       .replaceAll("&d", "<light_purple>")
                       .replaceAll("&e", "<yellow>")
                       .replaceAll("&f", "<white>")
                       .replaceAll("&n", "<u>")
                       .replaceAll("&m", "<st>")
                       .replaceAll("&k", "<obf>")
                       .replaceAll("&o", "<i>")
                       .replaceAll("&l", "<b>")
                       .replaceAll("&r", "<reset>");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("automessage.reload")) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }

        reloadConfig();

        cancelScheduler();

        initializeMessages();

        sender.sendMessage("Configuration reloaded.");

        return true;
    }
}
