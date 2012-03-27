package org.micoli.minecraft.realEstate.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.Listener;
import org.micoli.minecraft.realEstate.RealEstate;

public class QDBlockListener implements Listener {
	RealEstate activeBall;

	public QDBlockListener(RealEstate activeBall) {
		this.activeBall = activeBall;
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		activeBall.blockBreak(event);
	}
}
