package com.dre.brewery.integration;

import com.Acrobot.ChestShop.Events.ShopCreatedEvent;
import com.dre.brewery.Brew;
import com.dre.brewery.Brewery;
import com.dre.brewery.filedata.BConfig;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

public class ChestShopListener implements Listener {

	@EventHandler
	public void onShopCreated(ShopCreatedEvent event) {
		try {
			Container container = event.getContainer();
			if (container != null) {
				for (ItemStack item : container.getInventory().getContents()) {
					if (item != null && item.getType() == Material.POTION) {
						Brew brew = Brew.get(item);
						if (brew != null && !brew.isSealed()) {
							event.getPlayer().sendTitle("", Brewery.getInstance().color(Brewery.getInstance().languageReader.get("Player_ShopSealBrew")), 10, 70, 20);
							return;
						}
					}
				}
			}
		} catch (Throwable e) {
			HandlerList.unregisterAll(this);
			BConfig.hasChestShop = false;
			Brewery.getInstance().getLogger().log(Level.SEVERE, e.getMessage(), e);
			Brewery.getInstance().errorLog("Failed to notify Player using ChestShop. Disabling ChestShop support");
		}
	}
}
