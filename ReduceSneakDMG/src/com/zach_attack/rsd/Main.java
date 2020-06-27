package com.zach_attack.rsd;

import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
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

import com.earth2me.essentials.Essentials;

public class Main extends JavaPlugin implements Listener {

    private String prefix = "§8[§e§lR§r§eeduced§6§lS§r§6neak§f§lD§r§fmg§8] §f";
    private String cprefix = "§8[§e§lR§r§eeduced§6§lS§r§6neak§f§lD§r§fmg§8]§f";

    // Blood code courtesy of CraftGasM -- Thank You!

    private List<String> nonoworlds = getConfig().getStringList("settings.disabled-worlds");
    private Logger log = getLogger();

    private boolean enabled = true;
    boolean round = true;
    private double precent = 50.0D;
    private boolean useperms = false;
    private boolean particlessneak = false;
    private boolean particlesnorm = false;

    private boolean ess = false;
    static boolean worldguard = false;
    
    private boolean sounds = true;

    private String version = Bukkit.getBukkitVersion().replace("-SNAPSHOT", "");
    private boolean supported = (version.contains("1.12") || version.contains("1.13") || version.contains("1.14") || version.contains("1.15") || version.contains("1.16")) ?true :false;

    private boolean isGodMode(Player p) {
        if (ess) {
            Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
            if(ess.getUser(p).isGodModeEnabled()) {
            	return true;
            }
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

        for (MetadataValue meta: player.getMetadata("vanished")) {
            if (meta.asBoolean())
                return true;
        }

        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            return true;
        }

        return false;
    }

    public void onEnable() {
        if (!supported) {
        	Bukkit.getScheduler().runTask(this, () -> {
        		getLogger().warning("> This version may not work for this version of Minecraft. (Supports 1.16 through 1.12)");
            });
        }
        
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

        if (!supported) {
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

            if(nonoworlds.size() != 0) {
            	log.info("Disabled in world(s): " + nonoworlds.toString());
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
        
        if(!supported) {
        	particlessneak = false;
        	particlesnorm = false;
        	// Disabled if an error is thrown -> sounds = false;
        }
    }

    public void reloadSound(CommandSender sender) {
    	if(!sounds) {
    		return;
    	}
        if (sender instanceof Player) {
            try {
                Player p = (Player) sender;
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0F, 2.0F);
            } catch (Exception e) {sounds = false;}
        }
    }

    public void bass(CommandSender sender) {
    	if(!sounds) {
    		return;
    	}
        if (sender instanceof Player) {
            try {
                Player p = (Player) sender;
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 2.0F, 1.3F);
            } catch (Exception e) {sounds = false;}
        }
    }

    public void pop(CommandSender sender) {
    	if(!sounds) {
    		return;
    	}
        if (sender instanceof Player) {
            try {
                Player p = (Player) sender;
                p.playSound(p.getLocation(), Sound.ENTITY_CHICKEN_EGG, 2.0F, 2.0F);
            } catch (Exception e) {sounds = false;}
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("reducesneakdmg")) {
            if (args.length == 0) {
                sender.sendMessage(" ");
                sender.sendMessage("§e§lR§r§eeduced§6§lS§r§6neak§f§lD§r§fmg");
                sender.sendMessage("§7§oDo §f/rsd help §7for a list of commands.");
                sender.sendMessage(" ");
                pop(sender);
                return true;
            }
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("help")) {
                    sender.sendMessage(prefix + "To reload the plugin, do §7/rsd reload");
                    pop(sender);
                    return true;
                }

                if (args[0].equalsIgnoreCase("reload")) {
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
                sender.sendMessage(prefix + "§c§lError.§f The sub command §7§l" + args[1] + "§f couldn't be found.");
                bass(sender);
            }
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
            } catch(Exception err) {
            	log.warning("Unable to test for WorldGuard flags. This feature will be disabled.");
            	worldguard = false;
            }

            if (enabled && !nonoworlds.contains(p.getLocation().getWorld().getName())) {
                if (!useperms || p.hasPermission("reducesneakdmg.use")) {
                    if (e.getCause() == DamageCause.FALL) {
                        if (p.isSneaking()) {

                            if (round) {
                                e.setDamage(Math.round(e.getDamage()) * (getConfig().getDouble("settings.dmg-precent") / 100));
                            } else {
                                e.setDamage((e.getDamage()) * (precent / 100));
                            }

                            if (!supported) {
                                return;
                            }
                            if (particlessneak && !isVanished(p)) {
                                p.getWorld().playEffect(p.getLocation().add(0.0D, 0.8D, 0.0D), Effect.STEP_SOUND, Material.RED_CONCRETE);
                            }
                        } else {
                            // Player is NOT sneaking.
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