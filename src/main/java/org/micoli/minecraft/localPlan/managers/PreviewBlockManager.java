package org.micoli.minecraft.localPlan.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

// TODO: Auto-generated Javadoc
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
 	 * @throws QDCommandException the qD command exception
 	 */
 	public void showParcel(Player player, String parcelName) throws QDCommandException {
		Parcel parcel = Parcel.getParcel(player.getWorld().getName(), parcelName);
		if (parcel == null) {
			throw new QDCommandException("Parcel not found " + player.getWorld().getName() + "::" + parcelName);
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
	 * @throws QDCommandException the qD command exception
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
		hideOneParcel(parcel.getWorld(),parcel.getRegionId());

		previewBlocks.remove(parcel.getWorld() + "::" + parcel.getRegionId());
		plugin.sendComments(player, "Parcel hidden");
	}
	
	/**
	 * Hide one parcel.
	 *
	 * @param worldId the world id
	 * @param regionId the region id
	 */
	public void hideOneParcel(String worldId, String regionId){
		World world = plugin.getServer().getWorld(worldId);
		List<Block> listBlock = previewBlocks.get(worldId + "::" + regionId);
		for (Iterator<Block> pointIterator = listBlock.iterator(); pointIterator.hasNext();) {
			Block block = pointIterator.next();
			world.getBlockAt(block.getLocation()).setTypeId(0);
		}
		previewBlocks.remove(worldId + "::" + regionId);
	}
	
	/**
	 * Hide all parcels shown.
	 */
	public void hideAllParcelsShown(){
		plugin.logger.log("Auto hidding parcels shown");
		Pattern pattern = Pattern.compile("(.*)::(.*)");
		for(String key : previewBlocks.keySet()){
			Matcher matcher = pattern.matcher(key);
			if (matcher.matches()) {
				plugin.logger.log("auto hide shown parcel "+ matcher.group(1)+"::"+matcher.group(2));
				hideOneParcel(matcher.group(1),matcher.group(2));
			}
		}
	}

}
