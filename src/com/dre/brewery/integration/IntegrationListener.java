package com.dre.brewery.integration;

import com.dre.brewery.Brewery;
import com.dre.brewery.api.events.barrel.BarrelAccessEvent;
import com.dre.brewery.api.events.barrel.BarrelDestroyEvent;
import com.dre.brewery.api.events.barrel.BarrelRemoveEvent;
import com.dre.brewery.filedata.BConfig;
import com.dre.brewery.integration.barrel.LWCBarrel;
import com.dre.brewery.utility.LegacyUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public class IntegrationListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBarrelAccessLowest(BarrelAccessEvent event) {
		if (BConfig.useWG) {
			Plugin plugin = Brewery.getInstance().getServer().getPluginManager().getPlugin("WorldGuard");
			if (plugin != null) {
				try {
					if (!BConfig.wg.checkAccess(event.getPlayer(), event.getSpigot(), plugin)) {
						event.setCancelled(true);
						Brewery.getInstance().msg(event.getPlayer(), Brewery.getInstance().languageReader.get("Error_NoBarrelAccess"));
					}
				} catch (Throwable e) {
					event.setCancelled(true);
					Brewery.getInstance().errorLog("Failed to Check WorldGuard for Barrel Open Permissions!");
					Brewery.getInstance().errorLog("Brewery was tested with version 5.8, 6.1 to 7.0 of WorldGuard!");
					Brewery.getInstance().errorLog("Disable the WorldGuard support in the config and do /brew reload");
					Brewery.getInstance().getLogger().log(Level.SEVERE, e.getMessage(), e);
					Player player = event.getPlayer();
					if (player.hasPermission("brewery.admin") || player.hasPermission("brewery.mod")) {
						Brewery.getInstance().msg(player, "&cWorldGuard check Error, Brewery was tested with up to v7.0 of Worldguard");
						Brewery.getInstance().msg(player, "&cSet &7useWorldGuard: false &cin the config and /brew reload");
					} else {
						Brewery.getInstance().msg(player, "&cError opening Barrel, please report to an Admin!");
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBarrelAccess(BarrelAccessEvent event) {
		if (BConfig.useLWC) {
			Plugin plugin = Brewery.getInstance().getServer().getPluginManager().getPlugin("LWC");
			if (plugin != null) {

				// If the Clicked Block was the Sign, LWC already knows and we dont need to do anything here
				if (!LegacyUtil.isSign(event.getClickedBlock().getType())) {
					Block sign = event.getBarrel().getBody().getSignOfSpigot();
					// If the Barrel does not have a Sign, it cannot be locked
					if (!sign.equals(event.getClickedBlock())) {
						Player player = event.getPlayer();
						try {
							if (!LWCBarrel.checkAccess(player, sign, plugin)) {
								Brewery.getInstance().msg(event.getPlayer(), Brewery.getInstance().languageReader.get("Error_NoBarrelAccess"));
								event.setCancelled(true);
								return;
							}
						} catch (Throwable e) {
							event.setCancelled(true);
							Brewery.getInstance().errorLog("Failed to Check LWC for Barrel Open Permissions!");
							Brewery.getInstance().errorLog("Brewery was tested with version 4.5.0 of LWC!");
							Brewery.getInstance().errorLog("Disable the LWC support in the config and do /brew reload");
							Brewery.getInstance().getLogger().log(Level.SEVERE, e.getMessage(), e);
							if (player.hasPermission("brewery.admin") || player.hasPermission("brewery.mod")) {
								Brewery.getInstance().msg(player, "&cLWC check Error, Brewery was tested with up to v4.5.0 of LWC");
								Brewery.getInstance().msg(player, "&cSet &7useLWC: false &cin the config and /brew reload");
							} else {
								Brewery.getInstance().msg(player, "&cError opening Barrel, please report to an Admin!");
							}
							return;
						}
					}
				}
			}
		}

		if (BConfig.virtualChestPerms) {
			Player player = event.getPlayer();
			BlockState originalBlockState = event.getClickedBlock().getState();

			event.getClickedBlock().setType(Material.CHEST, false);
			PlayerInteractEvent simulatedEvent = new PlayerInteractEvent(
				player,
				Action.RIGHT_CLICK_BLOCK,
				player.getInventory().getItemInMainHand(),
				event.getClickedBlock(),
				event.getClickedBlockFace(),
				EquipmentSlot.HAND);

			try {
				Brewery.getInstance().getServer().getPluginManager().callEvent(simulatedEvent);
			} catch (Throwable e) {
				Brewery.getInstance().errorLog("Failed to simulate a Chest for Barrel Open Permissions!");
				Brewery.getInstance().errorLog("Disable useVirtualChestPerms in the config and do /brew reload");
				Brewery.getInstance().getLogger().log(Level.SEVERE, e.getMessage(), e);
				if (player.hasPermission("brewery.admin") || player.hasPermission("brewery.mod")) {
					Brewery.getInstance().msg(player, "&cVirtual Chest Error");
					Brewery.getInstance().msg(player, "&cSet &7useVirtualChestPerms: false &cin the config and /brew reload");
				} else {
					Brewery.getInstance().msg(player, "&cError opening Barrel, please report to an Admin!");
				}
			} finally {
				event.getClickedBlock().setType(Material.AIR, false);
				originalBlockState.update(true);
			}

			if (simulatedEvent.useInteractedBlock() == Event.Result.DENY) {
				event.setCancelled(true);
				Brewery.getInstance().msg(event.getPlayer(), Brewery.getInstance().languageReader.get("Error_NoBarrelAccess"));
				//return;
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onBarrelDestroy(BarrelDestroyEvent event) {
		if (!BConfig.useLWC) return;

		if (event.hasPlayer()) {
			Player player = event.getPlayerOptional();
			assert player != null;
			try {
				if (LWCBarrel.denyDestroy(player, event.getBarrel())) {
					event.setCancelled(true);
				}
			} catch (Throwable e) {
				event.setCancelled(true);
				Brewery.getInstance().errorLog("Failed to Check LWC for Barrel Break Permissions!");
				Brewery.getInstance().errorLog("Brewery was tested with version 4.5.0 of LWC!");
				Brewery.getInstance().errorLog("Disable the LWC support in the config and do /brew reload");
				Brewery.getInstance().getLogger().log(Level.SEVERE, e.getMessage(), e);
				if (player.hasPermission("brewery.admin") || player.hasPermission("brewery.mod")) {
					Brewery.getInstance().msg(player, "&cLWC check Error, Brewery was tested with up to v4.5.0 of LWC");
					Brewery.getInstance().msg(player, "&cSet &7useLWC: false &cin the config and /brew reload");
				} else {
					Brewery.getInstance().msg(player, "&cError breaking Barrel, please report to an Admin!");
				}
			}
		} else {
			try {
				if (event.getReason() == BarrelDestroyEvent.Reason.EXPLODED) {
					if (LWCBarrel.denyExplosion(event.getBarrel())) {
						event.setCancelled(true);
					}
				} else {
					if (LWCBarrel.denyDestroyOther(event.getBarrel())) {
						event.setCancelled(true);
					}
				}
			} catch (Throwable e) {
				event.setCancelled(true);
				Brewery.getInstance().errorLog("Failed to Check LWC on Barrel Destruction!");
				Brewery.getInstance().errorLog("Brewery was tested with version 4.5.0 of LWC!");
				Brewery.getInstance().errorLog("Disable the LWC support in the config and do /brew reload");
				Brewery.getInstance().getLogger().log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}

	@EventHandler
	public void onBarrelRemove(BarrelRemoveEvent event) {
		if (!BConfig.useLWC) return;

		try {
			LWCBarrel.remove(event.getBarrel());
		} catch (Throwable e) {
			Brewery.getInstance().errorLog("Failed to Remove LWC Lock from Barrel!");
			Brewery.getInstance().errorLog("Brewery was tested with version 4.5.0 of LWC!");
			Brewery.getInstance().getLogger().log(Level.SEVERE, e.getMessage(), e);
		}
	}
}
