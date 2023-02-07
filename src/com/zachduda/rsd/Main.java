package com.zachduda.rsd;

import com.earth2me.essentials.Essentials;
import com.zachduda.puuids.api.PUUIDS;
import me.clip.actionannouncer.ActionAPI;
import me.dave.chatcolorhandler.ChatColorHandler;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;
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
import java.util.*;
import java.util.logging.Logger;

public class Main extends JavaPlugin implements Listener {

    static boolean worldguard = false;
    static HashSet<UUID> hasseentip = new HashSet<>();

    // Blood code courtesy of CraftGasM -- Thank You!
    final DecimalFormat df = new DecimalFormat("#.#");
    private final String pl_color = "&#fffd91";
    @SuppressWarnings("FieldCanBeLocal")
    private final String sec_color = "&#e8e8cf";
    private final String prefix = "&8["+pl_color+"&lRSD&r&8] " + sec_color;
    private final List<String> nonoworlds = getConfig().getStringList("settings.disabled-worlds");
    private final Logger log = getLogger();
    private final String version = Bukkit.getBukkitVersion().replace("-SNAPSHOT", "");
    private final boolean supported = version.contains("1.16") || version.contains("1.17") || version.contains("1.18") || version.contains("1.19");
    boolean round = true;
    private boolean enabled = true;
    private double percent = 50.0D;
    private boolean useperms = false;
    private boolean particlessneak = false;
    private boolean particlesnorm = false;
    private boolean ess = false;
    private boolean hasaa = false;
    private boolean useaa = false;
    private boolean hasPUUIDs = false;
    private boolean notify = false;
    private boolean sounds = true;
    private BukkitTask csht = null;
    private boolean aabail = false;

    private final String cm_err = "&cCheck your RSD config.yml, this message is missing or misformatted.";

    private boolean isGodMode(Player p) {
        if (ess) {
            Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
            return Objects.requireNonNull(ess).getUser(p).isGodModeEnabled();
        }

        return false;
    }

    private boolean isNotVanished(Player player) {
        if (ess) {
            Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
            if (Objects.requireNonNull(ess).getUser(player).isVanished()) {
                return false;
            }
        }

        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean())
                return false;
        }

        return !player.hasPotionEffect(PotionEffectType.INVISIBILITY);
    }

    private void stopTimers() {
        if (csht != null) {
            csht.cancel();
        }
    }

    protected String color(String msg) {
        return ChatColorHandler.translateAlternateColorCodes(msg).replaceAll("%prefix%", prefix);
    }

    private void msg(CommandSender p, String msg) {
        p.sendMessage(color(msg));
    }

    public void onEnable() {
        if (!supported) {
            Bukkit.getScheduler().runTask(this, () -> getLogger().warning("> This plugin may not work for this version of Minecraft. (Supports 1.19 through 1.16)"));
        }

        hasseentip.clear();

        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        getConfig().options().copyDefaults(true);

        List<String> confighead = new ArrayList<>();
        confighead.add("ReducedSneakDamage -- A plugin by zach_attack.");
        confighead.add("Need help? Join our support discord: https://discord.gg/6ugXPfX");

        try {
            getConfig().options().setHeader(confighead);
        } catch (NoSuchMethodError e) {
            // Using less than Java 18 will use this method instead.
            try {
                //noinspection deprecation
                getConfig().options().header(confighead.get(0) + "\n" + confighead.get(1));
            } catch (Exception giveup) { /* just skip this */ }
        }

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

        if (getServer().getPluginManager().isPluginEnabled("PUUIDs") && (getServer().getPluginManager().getPlugin("PUUIDs") != null)) {
            getLogger().info("Hooked into PUUIDs for saving damage saved.");
            try {
                if (PUUIDS.connect(this, PUUIDS.APIVersion.V4)) {
                    hasPUUIDs = true;
                }
            } catch(Exception e) {
                getLogger().warning("Unable to connect to PUUIDs: ");
                e.printStackTrace();
            }
        }

        csht = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> hasseentip.clear(), 1728000, 1728000); // 1 day

        if (supported) {
            log.info("Need help with RSD? Join our support discord: https://discord.gg/6ugXPfX");
        }
    }

    public void updateConfig() {
        if (!getConfig().getBoolean("settings.enable-plugin")) {
            log.info("Plugin disabled via the configuration...");
            enabled = false;
            return;
        }
        double iprecent = getConfig().getDouble("settings.dmg-percent", 50.0D);
        enabled = true;

        log.info("Reducing damage by: " + iprecent + "%");
        percent = iprecent;

        round = getConfig().getBoolean("settings.round", true);
        useaa = getConfig().getBoolean("settings.notify.use-action-bar", true);
        notify = getConfig().getBoolean("settings.notify.enable", true);

        if (nonoworlds.size() != 0) {
            log.info("Disabled in world(s): " + nonoworlds);
        }

        nonoworlds.clear();
        nonoworlds.addAll(getConfig().getStringList("settings.disabled-worlds"));

        particlessneak = getConfig().getBoolean("settings.particles.when-falling-sneak", true);
        particlesnorm = getConfig().getBoolean("settings.particles.when-falling-normal", true);

        useperms = getConfig().getBoolean("settings.use-permissions", false);

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
        final String mcd = color(m);
        if (aabail) {
            msg(p, mcd);
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

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("reducesneakdmg")) {
            if (args.length == 0) {
                msg(sender, " ");
                msg(sender, pl_color + "&lReduce Sneak Damage");
                msg(sender, sec_color + "Do &r&f/rsd help &#e8e8cffor a list of commands.");
                msg(sender," ");
                pop(sender);
                return true;
            }
            if (args[0].equalsIgnoreCase("help")) {
                pop(sender);

                if (!sender.hasPermission("reducesneakdmg.admin") && useperms && !sender.isOp()) {
                    msg(sender,prefix + "A plugin by zach_attack");
                    return true;
                }
                msg(sender,prefix + "To reload the plugin, do &f/rsd reload");
                return true;
            }

            if (args[0].equalsIgnoreCase("stats")) {
                if(!hasPUUIDs) {
                    msg(sender,prefix + "You must have &f&lPUUIDs " + sec_color + "to save and view stats.");
                    bass(sender);
                    return true;
                }

                if(!(sender instanceof Player p)) {
                    msg(sender,prefix + "&cOnly players can use the stats sub-command");
                    return true;
                }

                if (!sender.hasPermission("reducesneakdmg.stats") && useperms && !sender.isOp()) {
                    msg(sender, getConfig().getString("messages.no-permission", cm_err));
                    bass(sender);
                    return true;
                }
                pop(sender);
                msg(sender, getConfig().getString("messages.stats", cm_err).replaceAll("%dmg%", df.format(PUUIDS.getDouble(this, p.getUniqueId().toString(), "DMG-Saved"))));
                return true;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("reducesneakdmg.admin") && useperms && !sender.isOp()) {
                    msg(sender, getConfig().getString("messages.no-permission", cm_err));
                    bass(sender);
                    return true;
                }
                try {
                    reloadConfig();
                    updateConfig();
                    msg(sender, color(getConfig().getString("messages.reload", cm_err)));
                    // needs extra color call even tho its in msg(). idk why.
                    reloadSound(sender);
                } catch (Exception cfgerr) {
                    log.info("Error when reloading configuration. -----------------");
                    cfgerr.printStackTrace();
                    msg(sender,prefix + "&cError. &rSomething went wrong here, check your console.");
                    bass(sender);
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("toggle")) {
                if (!sender.hasPermission("reducesneakdmg.admin") && useperms && !sender.isOp()) {
                    msg(sender, getConfig().getString("messages.no-permission", cm_err));
                    bass(sender);
                    return true;
                }

                if (enabled) {
                    getConfig().set("settings.enable-plugin", false);
                    msg(sender,prefix + "&fPlayers now take &c&lnormal &rfall damage when sneaking.");
                } else {
                    getConfig().set("settings.enable-plugin", true);
                    msg(sender,prefix + "&fPlayers will take &a&lless &rfall damage when sneaking.");
                }

                saveConfig();
                reloadConfig();
                updateConfig();
                return true;
            }
            msg(sender,prefix + "&cError.&r The sub command &r&7" + args[0] + "&r couldn't be found.");
            bass(sender);
        }
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDmg(EntityDamageEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            if (e.getEntity() instanceof Player p && !e.isCancelled()) {

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

                if (enabled && !nonoworlds.contains(Objects.requireNonNull(p.getLocation().getWorld()).getName())) {
                    if (!useperms || p.hasPermission("reducesneakdmg.use")) {
                        if (e.getCause() == DamageCause.FALL) {
                            if (p.isSneaking()) {

                                final double olddam = e.getDamage();
                                double newdam;
                                if (round) {
                                    newdam = Math.round(olddam) * (getConfig().getDouble("settings.dmg-percent", 50.0D) / 100);
                                } else {
                                    newdam = ((olddam) * (percent / 100));
                                }
                                e.setDamage(newdam);
                                final double diff = olddam - newdam;

                                if (notify) {
                                    if (useaa) {
                                        sendAA(p, getConfig().getString("messages.fell", cm_err).replaceAll("%dmg%", df.format(diff)), 2);
                                    } else {
                                        msg(p, getConfig().getString("messages.fell", cm_err).replaceAll("%dmg%", df.format(diff)));
                                    }
                                }

                                if (hasPUUIDs) {
                                    final String sid = p.getUniqueId().toString();
                                    PUUIDS.set(this, sid, "DMG-Saved", diff+PUUIDS.getInt(this, sid, "DMG-Saved"));
                                }

                                if (supported && particlessneak && isNotVanished(p)) {
                                    p.getWorld().playEffect(p.getLocation().add(0.0D, 0.8D, 0.0D), Effect.STEP_SOUND, Material.RED_CONCRETE);
                                }
                            } else {
                                // Player is NOT sneaking.
                                if (notify && !hasseentip.contains(p.getUniqueId())) {
                                    if (useaa) {
                                        sendAA(p, getConfig().getString("messages.fell-tip", cm_err), 5);
                                    } else {
                                        msg(p, getConfig().getString("messages.fell-tip", cm_err));
                                    }
                                    hasseentip.add(p.getUniqueId());
                                }
                                if (supported && particlesnorm && isNotVanished(p)) {
                                    p.getWorld().playEffect(p.getLocation().add(0.0D, 0.8D, 0.0D), Effect.STEP_SOUND, Material.RED_CONCRETE);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            final Player p = e.getPlayer();
            final String sid = p.getUniqueId().toString();
            // This is a dev-join message sent to me only. It's to help me understand which servers support my work <3
            if (sid.equals("6191ff85-e092-4e9a-94bd-63df409c2079")) {
                msg(p, "&7This server is running &fRSD &6v" + getDescription().getVersion() + "&7 for " + version);
            }
            // I kindly ask you leave the above portion in ANY modification of this plugin. Thank You!


            if(hasPUUIDs) {
                if(!PUUIDS.contains(this, sid, "DMG-Saved")) {
                    PUUIDS.set(this, sid, "DMG-Saved", 0);
                }
            }
        });
    }
}