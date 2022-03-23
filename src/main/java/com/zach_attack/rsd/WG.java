package com.zach_attack.rsd;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.entity.Player;

public class WG {
    static boolean canTakeFallDMG(Player p) {
        if (Main.worldguard) {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(p.getLocation()));
            LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(p);
            return set.testState(lp, Flags.FALL_DAMAGE) && !set.testState(lp, Flags.INVINCIBILITY);
        }
        return false;
    }
}
