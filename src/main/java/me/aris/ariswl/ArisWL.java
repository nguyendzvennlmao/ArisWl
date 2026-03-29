package me.aris.ariswl;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArisWL extends JavaPlugin implements Listener {

    private String unknownMessage;
    private String reloadMessage;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigValues();
        getServer().getPluginManager().registerEvents(this, this);
    }

    private void loadConfigValues() {
        reloadConfig();
        unknownMessage = getConfig().getString("messages.unknown_command", "Unknown command.");
        reloadMessage = getConfig().getString("messages.reload", "&aReloaded!");
    }

    private String color(String text) {
        if (text == null) return "";
        Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String hexCode = text.substring(matcher.start(), matcher.end());
            text = text.replace(hexCode, ChatColor.of(hexCode.replace("&", "")) + "");
            matcher = pattern.matcher(text);
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("ariswl") && sender.hasPermission("ariswl.admin")) {
            loadConfigValues();
            sender.sendMessage(color(reloadMessage));
            return true;
        }
        return false;
    }

    private Set<String> getWL(Player p) {
        Set<String> wl = new HashSet<>();
        ConfigurationSection sec = getConfig().getConfigurationSection("groups");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                String g = sec.getString(key + ".lp_group");
                if (g == null || g.equalsIgnoreCase("none") || p.hasPermission("group." + g.toLowerCase())) {
                    List<String> cmds = sec.getStringList(key + ".commands");
                    for (String c : cmds) wl.add(c.toLowerCase());
                }
            }
        }
        return wl;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCmd(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("ariswl.bypass")) return;

        String raw = e.getMessage().substring(1).toLowerCase();
        
        if (raw.startsWith("pl") || raw.startsWith("plugins") || raw.contains(":") || 
            raw.startsWith("ver") || raw.startsWith("about") || raw.startsWith("?") || 
            raw.startsWith("icanhasbukkit")) {
            e.setCancelled(true);
            p.sendMessage(color(unknownMessage));
            return;
        }

        String cmd = raw.split(" ")[0];
        if (!getWL(p).contains(cmd)) {
            e.setCancelled(true);
            p.sendMessage(color(unknownMessage));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTab(PlayerCommandSendEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("ariswl.bypass")) return;

        Set<String> wl = getWL(p);
        e.getCommands().removeIf(c -> !wl.contains(c.toLowerCase()) || c.contains(":"));
    }
  }
