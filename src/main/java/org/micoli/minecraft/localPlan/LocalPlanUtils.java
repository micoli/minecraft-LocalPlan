package org.micoli.minecraft.localPlan;

import java.util.List;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * The Class LocalPlanUtils.
 */
public class LocalPlanUtils {
	
	/**
	 * Block vector2 d distance.
	 *
	 * @param b1 the point 1
	 * @param b2 the point 2
	 * @return the distance
	 */
	public static double blockVector2DDistance(BlockVector2D b1,BlockVector2D b2){
		return Math.round( Math.sqrt((b1.getX()-b2.getX())*(b1.getX()-b2.getX())+(b1.getX()-b2.getX())*(b1.getX()-b2.getX())));
	}
	
	/**
	 * Gets the region surface.
	 *
	 * @param region the region
	 * @return the region surface
	 */
	public static int getRegionSurface(ProtectedRegion region){
		return (int) Math.round(region.volume()/Math.abs(region.getMaximumPoint().getY()-region.getMinimumPoint().getY()+1));
	}
	
	/**
	 * Gets the barycentre.
	 *
	 * @param region the region
	 * @return the barycentre
	 */
	public static BlockVector2D getBarycentre(ProtectedRegion region){
		double x = 0;
		double z = 0;
		List<BlockVector2D> points = region.getPoints();
		if (points != null && points.size() > 0) {
			for (int i = 0; i < points.size(); i++) {
				x+= points.get(i).getX();
				z+=points.get(i).getZ();
			}
			x=x/points.size();
			z=z/points.size();
		}
		return new BlockVector2D(x,z);
	}
}
