package com.zach_attack.rsd;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener { //     Boo...
	
	String prefix = "§8[§e§lR§r§eeduced§6§lS§r§6neak§f§lD§r§fmg§8] §f";
	String cprefix = "§8[§e§lR§r§eeduced§6§lS§r§6neak§f§lD§r§fmg§8]§f";
	ArrayList<String> nonoworlds = (ArrayList<String>) getConfig().getStringList("settings.disabled-worlds");
	
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
		  saveConfig();
		  
		  if(getConfig().getBoolean("settings.enable-plugin")) {
			  getLogger().info("All Ready! Now reducing fall damage by " + getConfig().getDouble("settings.dmg-precent") + "%");
		        for(World worlds : Bukkit.getWorlds()) {
		        	if(nonoworlds.contains(worlds.getName())) {
				    getLogger().info("Disabled in world: " + worlds.getName());	
		        	}
			        }
		  } else {
			  getLogger().info("Plugin disabled via the configuration...");
		  }
	}
	
	public void reloadSound(CommandSender sender) {
		if(sender instanceof Player) {
			try {
        Player p = (Player)sender;
        if (!Bukkit.getBukkitVersion().contains("1.8") && !Bukkit.getBukkitVersion().contains("1.7") && !Bukkit.getBukkitVersion().contains("1.6") && !Bukkit.getBukkitVersion().contains("1.5")) {
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0f, 2.0f);
        } else {
            p.playSound(p.getLocation(), Sound.valueOf((String)"LEVEL_UP"), 2.0f, 2.0f);
        }
			}catch(Exception e) {
				getLogger().info("Error, couldn't play sound for reload. We don't support 1.4 or below.");
			}
	}}
	
	public void bass(CommandSender sender) {
		if(sender instanceof Player) {
        Player p = (Player)sender;
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 2.0f, 1.3f);
	}}
	
    public void pop(Player p) {
         p.playSound(p.getLocation(), Sound.ENTITY_CHICKEN_EGG, 2.0f, 2.0f);
    }
	
	  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	  {
		 if((cmd.getName().equalsIgnoreCase("reducesneakdmg") && args.length == 0)) {
			sender.sendMessage("§e§lR§r§ereduced§6§lS§r§6neak§f§lD§r§fmg");
			sender.sendMessage("§7§oDo §f/rsd help §7for a list of commands.");
			
		 } else if((cmd.getName().equalsIgnoreCase("reducesneakdmg") && args.length == 1) && args[0].equalsIgnoreCase("help")) {
                 sender.sendMessage(prefix + "To reload the plugin, do §7/rsd reload");			 
		 } else if((cmd.getName().equalsIgnoreCase("reducesneakdmg") && args.length == 1) && args[0].equalsIgnoreCase("reload")) {
             try {
             reloadConfig();
             sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.reload")).replace("%prefix%", cprefix));
             reloadSound(sender);
             } catch (Exception cfgerr) {
            	 getLogger().info("Error when reloading configuration. -----------------");
            	 cfgerr.printStackTrace();
            	 sender.sendMessage(prefix + "§4§lError. §fSomething went wrong here, check your console.");
            	 bass(sender);
             }
		 } else if((cmd.getName().equalsIgnoreCase("reducesneakdmg") && args.length == 1) && args[0].equalsIgnoreCase("toggle")) {
			 if(!sender.hasPermission("reducesneakdmg.admin") && getConfig().getBoolean("settings.use-permissions")) {
				 sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.no-permission")).replace("%prefix%", cprefix));
				 bass(sender);
				 return true;
			 } else if(!sender.isOp()) {
				 sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.no-permission")).replace("%prefix%", cprefix));
				 bass(sender);
				 return true; 
			 }
			 
			 if(getConfig().getBoolean("settings.enable-plugin")) {
				 getConfig().set("settings.enable-plugin", false);
				 saveConfig();
				 reloadConfig();
				 sender.sendMessage(prefix + "Players §c§lwill§f now take normal damage when sneaking.");
			 } else {
				 getConfig().set("settings.enable-plugin", true);
				 saveConfig();
				 reloadConfig();
				 sender.sendMessage(prefix + "Players §a§lwill§f take less fall damage if sneaking.");
			 }
		 } else if((cmd.getName().equalsIgnoreCase("reducesneakdmg") && args.length == 1) && args[0].equalsIgnoreCase("help")) {
			 sender.sendMessage(prefix + "§7§lCommands:");
			 sender.sendMessage(prefix + " §8§l > §f§l/rsd help  §7Shows this page...");
			 sender.sendMessage(prefix + " §8§l > §f§l/rsd toggle  §7Toggles the dmg discount.");
			 sender.sendMessage(prefix + " §8§l > §f§l/rsd reload  §7Reloads the configuration.");
		 }
		return false;
	  }
	  
	    @EventHandler
	    public void onDmg(EntityDamageEvent e) {
	    	if(e.getEntity() instanceof Player) {
		        Player p = (Player) e.getEntity();
	    		if(getConfig().getBoolean("settings.enable-plugin") && !nonoworlds.contains(p.getLocation().getWorld().getName().toString())) {
	        if(!getConfig().getBoolean("settings.use-permissions") || p.hasPermission("reducesneakdmg.use")) {
	    		if (e.getCause() == DamageCause.FALL){
	    			if(p.isSneaking()) {
	    			
	    				if(getConfig().getBoolean("settings.round")) {
	    			e.setDamage(Math.round(e.getDamage())*(getConfig().getDouble("settings.dmg-precent")/100));
	    				} else {
	    			e.setDamage((e.getDamage())*(getConfig().getDouble("settings.dmg-precent")/100));		
	    				}
	    			
	    			if(getConfig().getBoolean("settings.particles.when-falling-sneak")) {
	    				// The following BLOOD code was borrowed by CraftGasM, Thank You <3
	    			p.getWorld().playEffect(p.getLocation().add(0.0D, 0.8D, 0.0D), Effect.STEP_SOUND, Material.RED_CONCRETE);
	    			}
	        } else { //if not sneaking
    			if(getConfig().getBoolean("settings.particles.when-falling-normal")) {
    				// The following BLOOD code was borrowed by CraftGasM, Thank You <3
    			p.getWorld().playEffect(p.getLocation().add(0.0D, 0.8D, 0.0D), Effect.STEP_SOUND, Material.RED_CONCRETE);
    			}
	        }
	    			}}
	    }}}
}
