package com.zach_attack.rsd;

import com.earth2me.essentials.Essentials;
import me.clip.actionannouncer.ActionAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class Main extends JavaPlugin implements Listener {

    static boolean worldguard = false;
    static HashSet<UUID> hasseentip = new HashSet<>();

    // Blood code courtesy of CraftGasM -- Thank You!
    final DecimalFormat df = new DecimalFormat("#.#");
    private final String cprefix = "§8[§e§lR§r§eeduced§6§lS§r§6neak§f§lD§r§fmg§8]§f";
    private final List<String> nonoworlds = getConfig().getStringList("settings.disabled-worlds");
    private final Logger log = getLogger();
    private final String version = Bukkit.getBukkitVersion().replace("-SNAPSHOT", "");
    private final boolean supported = version.contains("1.12") || version.contains("1.13") || version.contains("1.14") || version.contains("1.15") || version.contains("1.16") || version.contains("1.17") || version.contains("1.18");
    boolean round = true;
    private boolean enabled = true;
    private double precent = 50.0D;
    private boolean useperms = false;
    private boolean particlessneak = false;
    private boolean particlesnorm = false;
    private boolean ess = false;
    private boolean hasaa = false;
    private boolean useaa = false;
    private boolean notify = false;
    private boolean sounds = true;
    private BukkitTask csht = null;
    private boolean aabail = false;

    private boolean isGodMode(Player p) {
        if (ess) {
            Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
            return ess.getUser(p).isGodModeEnabled();
        }

        return false;
    }

    private boolean isVanished(Player player) {
        if (ess) {
            Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
            if (ess.getUser(player).isVanished()) {
                return true;
            }
        }

        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean())
                return true;
        }

        return player.hasPotionEffect(PotionEffectType.INVISIBILITY);
    }

    private void stopTimers() {
        if (csht != null) {
            csht.cancel();
        }
    }

    public void onEnable() {
        if (!supported) {
            Bukkit.getScheduler().runTask(this, () -> getLogger().warning("> This plugin may not work for this version of Minecraft. (Supports 1.17 through 1.12)"));
        }

        hasseentip.clear();

        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        getConfig().options().copyDefaults(true);
        getConfig().options().header("ReducedSneakDamage -- A plugin by zach_attack.\n\nNeed help? Join our support discord: https://discord.gg/6ugXPfX");
        saveConfig();

        updateConfig();

        if (getServer().getPluginManager().isPluginEnabled("WorldGuard") && (getServer().getPluginManager().getPlugin("WorldGuard") != null)) {
            if (getServer().getPluginManager().isPluginEnabled("WorldEdit") && (getServer().getPluginManager().getPlugin("WorldEdit") != null)) {
                worldguard = true;
                log.info("Found WorldGuard. Hooking into the WorldGuard API...");
            }
        }

        if (this.getServer().getPluginManager().isPluginEnabled("Essentials") &&
                this.getServer().getPluginManager().getPlugin("Essentials") != null) {
            ess = true;
            getLogger().info("Found Essentials. Hooking into Essentials for vanish/god-mode support...");
        }

        if (getServer().getPluginManager().isPluginEnabled("ActionAnnouncer") && (getServer().getPluginManager().getPlugin("ActionAnnouncer") != null)) {
            getLogger().info("Hooked into ActionAnnouncer.");
            hasaa = true;
        }

        csht = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> hasseentip.clear(), 1728000, 1728000); // 1 day

        if (supported) {
            log.info("Need help with RSD? Join our support discord: https://discord.gg/6ugXPfX");
        }
    }

    public void updateConfig() {
        if (getConfig().getBoolean("settings.enable-plugin")) {
            double iprecent = getConfig().getDouble("settings.dmg-precent");
            enabled = true;

            log.info("Reducing damage by: " + iprecent + "%");
            precent = iprecent;

            round = getConfig().getBoolean("settings.round");
            useaa = getConfig().getBoolean("settings.notify.use-action-bar");
            notify = getConfig().getBoolean("settings.notify.enable");

            if (nonoworlds.size() != 0) {
                log.info("Disabled in world(s): " + nonoworlds);
            }

            nonoworlds.clear();
            nonoworlds.addAll(getConfig().getStringList("settings.disabled-worlds"));

            particlessneak = getConfig().getBoolean("settings.particles.when-falling-sneak");
            particlesnorm = getConfig().getBoolean("settings.particles.when-falling-normal");

        } else {
            log.info("Plugin disabled via the configuration...");
            enabled = false;
        }

        useperms = getConfig().getBoolean("settings.use-permissions");

        if (!supported) {
            particlessneak = false;
            particlesnorm = false;
            // Disabled if an error is thrown -> sounds = false;
        }
    }

    public void onDisable() {
        hasseentip.clear();
        stopTimers();
    }

    public void reloadSound(CommandSender sender) {
        if (!sounds) {
            return;
        }
        if (sender instanceof Player) {
            try {
                Player p = (Player) sender;
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0F, 2.0F);
            } catch (Exception e) {
                sounds = false;
            }
        }
    }

    public void bass(CommandSender sender) {
        if (!sounds) {
            return;
        }
        if (sender instanceof Player) {
            try {
                Player p = (Player) sender;
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 2.0F, 1.3F);
            } catch (Exception e) {
                sounds = false;
            }
        }
    }

    public void pop(CommandSender sender) {
        if (!sounds) {
            return;
        }
        if (sender instanceof Player) {
            try {
                Player p = (Player) sender;
                p.playSound(p.getLocation(), Sound.ENTITY_CHICKEN_EGG, 2.0F, 2.0F);
            } catch (Exception e) {
                sounds = false;
            }
        }
    }

    private void sendAA(Player p, String m, int time) {
        final String mcd = ChatColor.translateAlternateColorCodes('&', m);
        if (aabail) {
            p.sendMessage(mcd.replace("%prefix%", cprefix));
            return;
        }
        try {
            if (!hasaa) {
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(mcd));
                return;
            }
            try {
                ActionAPI.sendTimedPlayerAnnouncement(this, p, mcd, time);
            } catch (Exception err) {
                hasaa = false;
                sendAA(p, mcd, time);
            }
        } catch (Exception err) {
            aabail = true;
            sendAA(p, mcd, time);
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("reducesneakdmg")) {
            if (args.length == 0) {
                sender.sendMessage(" ");
                sender.sendMessage("§e§lR§r§eeduced§6§lS§r§6neak§f§lD§r§fmg");
                sender.sendMessage("§7Do §f/rsd help §7for a list of commands.");
                sender.sendMessage(" ");
                pop(sender);
                return true;
            }
            String prefix = "§8[§e§lR§r§eeduced§6§lS§r§6neak§f§lD§r§fmg§8] §f";
            if (args[0].equalsIgnoreCase("help")) {
                pop(sender);

                if (!sender.hasPermission("reducesneakdmg.admin") && useperms && !sender.isOp()) {
                    sender.sendMessage(prefix + "A plugin by zach_attack");
                    return true;
                }
                sender.sendMessage(prefix + "To reload the plugin, do §7/rsd reload");
                return true;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("reducesneakdmg.admin") && useperms && !sender.isOp()) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.no-permission")).replace("%prefix%", cprefix));
                    bass(sender);
                    return true;
                }
                try {
                    reloadConfig();
                    updateConfig();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.reload")).replace("%prefix%", cprefix));
                    reloadSound(sender);
                } catch (Exception cfgerr) {
                    log.info("Error when reloading configuration. -----------------");
                    cfgerr.printStackTrace();
                    sender.sendMessage(prefix + "§4§lError. §fSomething went wrong here, check your console.");
                    bass(sender);
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("toggle")) {
                if (!sender.hasPermission("reducesneakdmg.admin") && useperms && !sender.isOp()) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.no-permission")).replace("%prefix%", cprefix));
                    bass(sender);
                    return true;
                }

                if (enabled) {
                    getConfig().set("settings.enable-plugin", false);
                    sender.sendMessage(prefix + "§fPlayers now take §c§lnormal fall damage §fwhen sneaking.");
                } else {
                    getConfig().set("settings.enable-plugin", true);
                    sender.sendMessage(prefix + "§fPlayers will take §a§lless fall damage §fif sneaking.");
                }

                saveConfig();
                reloadConfig();
                updateConfig();
                return true;
            }
            sender.sendMessage(prefix + "§c§lError.§f The sub command §7§l" + args[0] + "§f couldn't be found.");
            bass(sender);
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDmg(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && !e.isCancelled()) {
            Player p = (Player) e.getEntity();

            if (p.isInvulnerable() || p.getGameMode().equals(GameMode.CREATIVE) || p.getAllowFlight() || isGodMode(p)) {
                return;
            }

            try {
                if (worldguard) {
                    if (!WG.canTakeFallDMG(p)) {
                        // WorldGuard Flags prohibited Fall DMG.
                        return;
                    }
                }
            } catch (Exception err) {
                log.warning("Unable to test for WorldGuard flags. This feature will be disabled.");
                worldguard = false;
            }

            if (enabled && !nonoworlds.contains(p.getLocation().getWorld().getName())) {
                if (!useperms || p.hasPermission("reducesneakdmg.use")) {
                    if (e.getCause() == DamageCause.FALL) {
                        if (p.isSneaking()) {

                            final double olddam = e.getDamage();
                            double newdam;
                            if (round) {
                                newdam = Math.round(olddam) * (getConfig().getDouble("settings.dmg-precent") / 100);
                            } else {
                                newdam = ((olddam) * (precent / 100));
                            }
                            e.setDamage(newdam);
                            if (notify) {
                                if (useaa) {
                                    sendAA(p, getConfig().getString("messages.fell").replace("%dmg%", df.format(olddam - newdam)), 2);
                                } else {
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.fell").replace("%dmg%", df.format(olddam - newdam))));
                                }
                            }

                            if (!supported) {
                                return;
                            }
                            if (particlessneak && !isVanished(p)) {
                                p.getWorld().playEffect(p.getLocation().add(0.0D, 0.8D, 0.0D), Effect.STEP_SOUND, Material.RED_CONCRETE);
                            }
                        } else {
                            // Player is NOT sneaking.
                            if (notify && !hasseentip.contains(p.getUniqueId())) {
                                if (useaa) {
                                    sendAA(p, getConfig().getString("messages.fell-tip"), 5);
                                } else {
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.fell-tip")));
                                }
                                hasseentip.add(p.getUniqueId());
                            }
                            if (particlesnorm && !isVanished(p)) {
                                p.getWorld().playEffect(p.getLocation().add(0.0D, 0.8D, 0.0D), Effect.STEP_SOUND, Material.RED_CONCRETE);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            final Player player = e.getPlayer();
            // This is a dev-join message sent to me only. It's to help me understand which servers support my work <3
            if (player.getUniqueId().toString().equals("6191ff85-e092-4e9a-94bd-63df409c2079")) {
                player.sendMessage(ChatColor.GRAY + "This server is running " + ChatColor.WHITE + "RSD " + ChatColor.GOLD + "v" + getDescription().getVersion() + ChatColor.GRAY + " for " + version);
            }
            // I kindly ask you leave the above portion in ANY modification of this plugin. Thank You!
        });
    }
}