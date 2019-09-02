package com.zach_attack.rsd;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	
	String prefix = "§8[§e§lR§r§eeduced§6§lS§r§6neak§f§lD§r§fmg§8] §f";
	String cprefix = "§8[§e§lR§r§eeduced§6§lS§r§6neak§f§lD§r§fmg§8]§f";
	
	// Blood code courtesy of CraftGasM -- Thank You!
	
	ArrayList<String> nonoworlds = (ArrayList<String>) getConfig().getStringList("settings.disabled-worlds");
	Logger log = getLogger();
	
	boolean enabled = true;
	boolean round = true;
	double precent = 50.0D;
	boolean useperms = false;
	boolean particlessneak = false;
	boolean particlesnorm = false;
	
	static boolean worldguard = false;
	
	public void onEnable() {
		if(!Bukkit.getVersion().contains("1.14")) {
		      Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				  public void run() {
						getLogger().warning("> This version DOES NOT support anything below 1.14. Please use RSD v1.3 for legacy support.");
						getLogger().warning("> Download the legacy version: spigotmc.org/resources/reducesneakdamage.64357/download?version=258607");
			}},200L);
		}
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		  getConfig().options().copyDefaults(true);
		  getConfig().options().header("ReducedSneakDamage -- A 1.14.4 plugin by zach_attack\n\nNeed help? Join our support discord: https://discord.gg/6ugXPfX");
		  saveConfig();
		  
		  updateConfig();
		  
		  if(getServer().getPluginManager().isPluginEnabled("WorldGuard") && (getServer().getPluginManager().getPlugin("WorldGuard") != null)) {
			  if(getServer().getPluginManager().isPluginEnabled("WorldEdit") && (getServer().getPluginManager().getPlugin("WorldEdit") != null)) {
			  worldguard = true;
			  log.info("Found WorldGuard. Hooking into the WorldGuard API...");
		  }}
		  
		  if(Bukkit.getVersion().contains("1.14")) {
		  log.info("Need help with RSD? Join our support discord: https://discord.gg/6ugXPfX");
		  }
	}
	
	public void updateConfig() {
		  if(getConfig().getBoolean("settings.enable-plugin")) {
			  double iprecent = getConfig().getDouble("settings.dmg-precent");
			  	enabled = true;
			  	
			  	log.info("Reducing damage by: " + iprecent + "%");
			  	precent = iprecent;
			  	
			  	round = getConfig().getBoolean("settings.round");
			  	
			  	for(World worlds : Bukkit.getWorlds()) {
			  		if(nonoworlds.contains(worlds.getName())) {
		        		log.info("Disabled in world: " + worlds.getName());	
		        		}
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
	}
	
	public void reloadSound(CommandSender sender) {
		if(sender instanceof Player) {
			try {
				Player p = (Player)sender;
				p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0f, 2.0f);
				} catch(Exception e) {}
		}}
	
	public void bass(CommandSender sender) {
		if(sender instanceof Player) {
			try {
				Player p = (Player)sender;
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 2.0f, 1.3f);
			} catch(Exception e) {}
		}}
	
    public void pop(CommandSender sender) {
    	if(sender instanceof Player) {
    		try {
    			Player p = (Player)sender;
    			p.playSound(p.getLocation(), Sound.ENTITY_CHICKEN_EGG, 2.0f, 2.0f);
    		} catch(Exception e) {}
    }}
	
	  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	  {
		 if((cmd.getName().equalsIgnoreCase("reducesneakdmg") && args.length == 0)) {
			sender.sendMessage("§e§lR§r§eeduced§6§lS§r§6neak§f§lD§r§fmg");
			sender.sendMessage("§7§oDo §f/rsd help §7for a list of commands.");
			pop(sender);
		 } else if((cmd.getName().equalsIgnoreCase("reducesneakdmg") && args.length == 1) && args[0].equalsIgnoreCase("help")) {
                 sender.sendMessage(prefix + "To reload the plugin, do §7/rsd reload");	
                 pop(sender);
		 } else if((cmd.getName().equalsIgnoreCase("reducesneakdmg") && args.length == 1) && args[0].equalsIgnoreCase("reload")) {
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
		 } else if((cmd.getName().equalsIgnoreCase("reducesneakdmg") && args.length == 1) && args[0].equalsIgnoreCase("toggle")) {
			 if(!sender.hasPermission("reducesneakdmg.admin") && useperms) {
				 sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.no-permission")).replace("%prefix%", cprefix));
				 bass(sender);
				 return true;
			 } else if(!sender.isOp()) {
				 sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.no-permission")).replace("%prefix%", cprefix));
				 bass(sender);
				 return true; 
			 }
			 
			 if(enabled) {
				 getConfig().set("settings.enable-plugin", false);
				 saveConfig();
				 reloadConfig();
				 updateConfig();
				 sender.sendMessage(prefix + "Players now take §c§lnormal fall damage §fwhen sneaking.");
			 } else {
				 getConfig().set("settings.enable-plugin", true);
				 saveConfig();
				 reloadConfig();
				 updateConfig();
				 sender.sendMessage(prefix + "Players will take §a§lless fall damage §fif sneaking.");
			 }
		 } else if((cmd.getName().equalsIgnoreCase("reducesneakdmg") && args.length == 1) && args[0].equalsIgnoreCase("help")) {
			 sender.sendMessage(prefix + "§7§lCommands:");
			 sender.sendMessage(prefix + " §8§l > §f§l/rsd help  §7Shows this page...");
			 sender.sendMessage(prefix + " §8§l > §f§l/rsd toggle  §7Toggles the dmg discount.");
			 sender.sendMessage(prefix + " §8§l > §f§l/rsd reload  §7Reloads the configuration.");
		 }
		return false;
	  }
	  
	    @EventHandler(priority = EventPriority.LOWEST)
	    public void onDmg(EntityDamageEvent e) {
	    	if(e.getEntity() instanceof Player && !e.isCancelled()) {
		        Player p = (Player) e.getEntity();
		        
		        if(p.isInvulnerable() || p.getGameMode().equals(GameMode.CREATIVE) || p.getAllowFlight()) {
		        	return;
		        }
		        
		        if(worldguard) {
		        if(!WG.canTakeFallDMG(p)) {
		        	// WorldGuard Flags prohibited Fall DMG.
		        	return;
		        }}
		        
	    		if(enabled && !nonoworlds.contains(p.getLocation().getWorld().getName())) {
	        if(!useperms || p.hasPermission("reducesneakdmg.use")) {
	    		if (e.getCause() == DamageCause.FALL){
	    			if(p.isSneaking()) {
	    			
	    				if(round) {
	    			e.setDamage(Math.round(e.getDamage())*(getConfig().getDouble("settings.dmg-precent")/100));
	    				} else {
	    			e.setDamage((e.getDamage())*(precent/100));		
	    				}
	    			
	    			if(particlessneak) {
	    				p.getWorld().playEffect(p.getLocation().add(0.0D, 0.8D, 0.0D), Effect.STEP_SOUND, Material.RED_CONCRETE);
	    			}
	        } else {
	        	// Player is NOT sneaking.
    				if(particlesnorm) {
    					p.getWorld().playEffect(p.getLocation().add(0.0D, 0.8D, 0.0D), Effect.STEP_SOUND, Material.RED_CONCRETE);
    				}
	        	}
	    }}}}}
}