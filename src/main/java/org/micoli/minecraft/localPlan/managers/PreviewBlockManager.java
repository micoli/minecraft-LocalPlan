package org.micoli.minecraft.localPlan.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.micoli.minecraft.bukkit.QDCommandException;
import org.micoli.minecraft.localPlan.LocalPlan;
import org.micoli.minecraft.localPlan.entities.Parcel;
import org.micoli.minecraft.utils.BlockUtils;
import org.micoli.minecraft.utils.PluginEnvironment;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * The Class PreviewBlockManager.
 */
public class PreviewBlockManager {
	
	/** The plugin. */
	private LocalPlan plugin;

	/** The preview blocks. */
	private HashMap<String, List<Block>> previewBlocks = new HashMap<String, List<Block>>();

	/**
	 * Instantiates a new preview block manager.
	 *
	 * @param instance the instance
	 */
	public PreviewBlockManager(LocalPlan instance) {
		this.plugin = instance;
	}

	 /* Show parcel.
	 *
	 * @param player the player
	 * @param parcelName the parcel name
	 * @throws Exception the exception
	 */
	/**
 	 * Show parcel.
 	 *
 	 * @param player the player
 	 * @param parcelName the parcel name
 	 * @throws Exception the exception
 	 */
 	public void showParcel(Player player, String parcelName) throws QDCommandException {
		Parcel parcel = Parcel.getParcel(player.getWorld().toString(), parcelName);
		if (parcel == null) {
			throw new QDCommandException("Parcel not found " + player.getWorld().toString() + "::" + parcelName);
		}
		if (!(parcel.getOwner().equalsIgnoreCase(player.getName()) || PluginEnvironment.getVaultPermission(plugin).playerHas(player, "localPlan.members.allow"))) {
			throw new QDCommandException("You don't have right on that Parcel");
		}
		if (previewBlocks.containsKey(player.getWorld().getName() + "::" + parcel.getRegionId())) {
			throw new QDCommandException("Parcel preview already shown");
		}
		List<Block> listBlock = new ArrayList<Block>();
		previewBlocks.put(player.getWorld().getName() + "::" + parcel.getRegionId(), listBlock);
		RegionManager mgr = PluginEnvironment.getWorldGuardPlugin(plugin).getGlobalRegionManager().get(plugin.getServer().getWorld(parcel.getWorld()));
		ProtectedRegion region = mgr.getRegion(parcel.getRegionId());

		int nb = 0;
		World world = plugin.getServer().getWorld(parcel.getWorld());
		List<BlockVector2D> points = region.getPoints();
		if (points != null && points.size() > 0) {
			BlockVector2D firstPoint = points.get(0);
			BlockVector2D lastPoint = points.get(points.size() - 1);
			if (region.getTypeName().equalsIgnoreCase("cuboid")) {
				lastPoint = points.get(2);
				points.set(2, points.get(3));
				points.set(3, lastPoint);
			}
			for (int i = 0; i < points.size(); i++) {
				if (nb == 0) {
					firstPoint = points.get(i);
					lastPoint = firstPoint;
				} else {
					BlockVector2D point = points.get(i);// pointIterator.next();
					BlockUtils.drawLineOnTop(plugin,new Location(world, lastPoint.getX(), 0, lastPoint.getZ()), new Location(world, point.getX(), 0, point.getZ()), Material.FENCE, listBlock);
					// sendComments(player, ChatFormater.format("Point %f,%f",
					// point.getX(), point.getZ()));
					lastPoint = point;
				}
				nb++;
			}
			// sendComments(player, ChatFormater.format("Point %f,%f",
			// firstPoint.getX(), firstPoint.getZ()));
			BlockUtils.drawLineOnTop(plugin,new Location(world, lastPoint.getX(), 0, lastPoint.getZ()), new Location(world, firstPoint.getX(), 0, firstPoint.getZ()), Material.FENCE, listBlock);
			plugin.sendComments(player, "Parcel shown");
		}
	}

	/**
	 * Hide parcel.
	 *
	 * @param player the player
	 * @param parcelName the parcel name
	 * @throws Exception the exception
	 */
	public void hideParcel(Player player, String parcelName) throws QDCommandException {
		Parcel parcel = Parcel.getParcel(player.getWorld().getName(), parcelName);
		if (parcel == null) {
			throw new QDCommandException("Parcel not found");
		}
		if (!(parcel.getOwner().equalsIgnoreCase(player.getName()) || PluginEnvironment.getVaultPermission(plugin).playerHas(player, "localPlan.members.allow"))) {
			throw new QDCommandException("You don't have right on that Parcel");
		}
		if (!previewBlocks.containsKey(player.getWorld().getName() + "::" + parcel.getRegionId())) {
			throw new QDCommandException("Parcel preview already shown");
		}
		World world = plugin.getServer().getWorld(parcel.getWorld());
		List<Block> listBlock = previewBlocks.get(player.getWorld().getName() + "::" + parcel.getRegionId());
		for (Iterator<Block> pointIterator = listBlock.iterator(); pointIterator.hasNext();) {
			Block block = pointIterator.next();
			world.getBlockAt(block.getLocation()).setTypeId(0);
		}

		previewBlocks.remove(player.getWorld().getName() + "::" + parcel.getRegionId());
		plugin.sendComments(player, "Parcel hidden");
	}

}
