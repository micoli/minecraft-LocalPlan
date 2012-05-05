package org.micoli.minecraft.localPlan.managers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import org.micoli.minecraft.bukkit.QDCommandException;
import org.micoli.minecraft.localPlan.LocalPlan;
import org.micoli.minecraft.localPlan.entities.InterestPoint;
import org.micoli.minecraft.utils.ChatFormater;
import org.micoli.minecraft.utils.PluginEnvironment;

import com.sk89q.worldedit.BlockVector2D;

/**
 * The Class InterestPointManager.
 */
public class InterestPointManager {
	
	/** The plugin. */
	private LocalPlan plugin;

	/** The interest points. */
	private HashMap<String, HashMap<String,InterestPoint>> interestPoints;

	/**
	 * Gets the interest points.
	 *
	 * @return the interestPoints
	 */
	public HashMap<String, HashMap<String, InterestPoint>> getInterestPoints() {
		return interestPoints;
	}
	
	/**
	 * Sets the interest points.
	 *
	 * @param interestPoints the interestPoints to set
	 */
	public void setInterestPoints(HashMap<String, HashMap<String, InterestPoint>> interestPoints) {
		this.interestPoints = interestPoints;
	}
	
	/**
	 * Instantiates a new interest point manager.
	 *
	 * @param instance the instance
	 */
	public InterestPointManager(LocalPlan instance) {
		this.plugin = instance;
	}
	
	/**
	 * Initialize interests point.
	 *
	 * @return true, if successful
	 */
	public boolean initialize(){
		Pattern pattern = Pattern.compile("-(\\d+(\\.\\d+)?)$");

		interestPoints = new HashMap<String, HashMap<String,InterestPoint>>();

		MarkerSet localPlanMarkerSet = PluginEnvironment.getDynmapCommonAPIPlugin(plugin).getMarkerAPI().getMarkerSet(plugin.getMarkersetName());
		
		if (localPlanMarkerSet == null){
			plugin.logger.log("Adding Point Of Interest markerset :" + plugin.getMarkersetName());
			localPlanMarkerSet = PluginEnvironment.getDynmapCommonAPIPlugin(plugin).getMarkerAPI().createMarkerSet(plugin.getMarkersetName(), plugin.getMarkersetName(), null, true);
		}

		Iterator<Marker> localPlanMarkerSetIterator = localPlanMarkerSet.getMarkers().iterator();
		while (localPlanMarkerSetIterator.hasNext()) {
			Marker marker = localPlanMarkerSetIterator.next();
			Matcher matcher = pattern.matcher(marker.getLabel());
			if (matcher.find()) {
				if (!interestPoints.containsKey(marker.getWorld())) {
					interestPoints.put(marker.getWorld(), new HashMap<String,InterestPoint>());
				}
				interestPoints.get(marker.getWorld()).put(marker.getLabel(),new InterestPoint(marker.getWorld(), marker.getLabel(), matcher.group().substring(1),new BlockVector2D( marker.getX(),marker.getZ())));
				plugin.logger.log("Markers : %s ", marker.getLabel());
			}
		}
		return true;
	}
	
	/**
	 * Adds the poi.
	 *
	 * @param player the player
	 * @param poiName the poi name
	 * @param icon the icon
	 * @param priceString the price string
	 * @throws Exception the exception
	 */
	public void addPOI(Player player, String poiName, String icon, String priceString) throws QDCommandException {
		if (getInterestPoints().get(player.getWorld().getName())==null){
			getInterestPoints().put(player.getWorld().getName(),new HashMap<String,InterestPoint>());
		}
		Marker existingMarker = null;
		try {
			existingMarker = PluginEnvironment.getDynmapCommonAPIPlugin(plugin).getMarkerAPI().getMarkerSet(plugin.getMarkersetName()).findMarker(poiName);
		} catch (Exception e) {
		}
		if (existingMarker!=null || getInterestPoints().get(player.getWorld().getName()).containsKey(poiName)){
			throw new QDCommandException("POI already exists");
		}
		Scanner scanner = new Scanner(priceString);
		if (!scanner.hasNextDouble()) {
			throw new QDCommandException("Price not found or not the good format 99.9");
		}
		double price = scanner.nextDouble();
		MarkerIcon markerIcon = PluginEnvironment.getDynmapCommonAPIPlugin(plugin).getMarkerAPI().getMarkerIcon(icon);
		if(markerIcon == null){
			throw new QDCommandException("Icon does not exists");
		}
		Marker marker = PluginEnvironment.getDynmapCommonAPIPlugin(plugin).getMarkerAPI().getMarkerSet(plugin.getMarkersetName()).createMarker(poiName+"-"+String.format("%.2f",price), poiName+"-"+String.format("%.2f",price), player.getWorld().getName(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), markerIcon, true);
		this.getInterestPoints().get(marker.getWorld()).put(marker.getLabel(),new InterestPoint(marker.getWorld(), poiName, String.format("%.2f",price),new BlockVector2D( marker.getX(),marker.getZ())));
		
		plugin.sendComments(player, ChatFormater.format("POI %s is now is created %f", poiName, price));
	}

}
