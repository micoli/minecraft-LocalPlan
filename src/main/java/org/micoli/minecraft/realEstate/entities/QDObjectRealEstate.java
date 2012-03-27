package org.micoli.minecraft.realEstate.entities;

import java.text.SimpleDateFormat;

import org.bukkit.entity.Player;
import org.micoli.minecraft.realEstate.RealEstate;

/**
 * @author o.michaud
 *
 */
public class QDObjectRealEstate {
	static RealEstate plugin;
	boolean debug = false;
	private SimpleDateFormat hourFmt = new SimpleDateFormat("HH:mm:ss");

	/**
	 * @param player
	 */
	public QDObjectRealEstate(Player player) {
		plugin = RealEstate.getInstance();
	}

}
