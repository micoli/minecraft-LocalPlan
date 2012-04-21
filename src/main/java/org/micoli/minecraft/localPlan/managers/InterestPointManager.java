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

import com.sk89q.worldedit.BlockVector2D;

public class InterestPointManager {
	
	LocalPlan instance;

	/** The interest points. */
	private HashMap<String, HashMap<String,InterestPoint>> interestPoints;

	/**
	 * @return the interestPoints
	 */
	public HashMap<String, HashMap<String, InterestPoint>> getInterestPoints() {
		return interestPoints;
	}
	/**
	 * @param interestPoints the interestPoints to set
	 */
	public void setInterestPoints(HashMap<String, HashMap<String, InterestPoint>> interestPoints) {
		this.interestPoints = interestPoints;
	}
	public InterestPointManager(LocalPlan instance) {
		this.instance = instance;
	}
	/**
	 * Initialize interests point.
	 */
	public boolean initialize(){
		Pattern pattern = Pattern.compile("-(\\d+(\\.\\d+)?)$");

		interestPoints = new HashMap<String, HashMap<String,InterestPoint>>();

		MarkerSet localPlanMarkerSet = instance.getDynmapPlugin().getMarkerAPI().getMarkerSet(instance.getMarkersetName());
		
		if (localPlanMarkerSet == null){
			instance.logger.log("Adding Point Of Interest markerset :" + instance.getMarkersetName());
			localPlanMarkerSet = instance.getDynmapPlugin().getMarkerAPI().createMarkerSet(instance.getMarkersetName(), instance.getMarkersetName(), null, true);
		}

		Iterator<Marker> localPlanMarkerSetIterator = localPlanMarkerSet.getMarkers().iterator();
		while (localPlanMarkerSetIterator.hasNext()) {
			Marker marker = localPlanMarkerSetIterator.next();
			Matcher matcher = pattern.matcher(marker.getLabel());
			if (matcher.find()) {
				if (!interestPoints.containsKey(marker.getWorld())) {
					interestPoints.put(marker.getWorld(), new HashMap<String,InterestPoint>());
				}
				interestPoints.get(marker.getWorld()).put(marker.getLabel(),new InterestPoint(marker.getWorld(), marker.getMarkerSet(), marker.getLabel(), matcher.group().substring(1),new BlockVector2D( marker.getX(),marker.getZ())));
				instance.logger.log("Markers : %s ", marker.getLabel());
			}
		}
		return true;
	}
	
	public void addPOI(Player player, String poiName, String icon, String priceString) throws Exception {
		if (getInterestPoints().get(player.getWorld().getName())==null){
			getInterestPoints().put(player.getWorld().getName(),new HashMap<String,InterestPoint>());
		}
		Marker existingMarker = null;
		try {
			existingMarker = instance.getDynmapPlugin().getMarkerAPI().getMarkerSet(instance.getMarkersetName()).findMarker(poiName);
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
		MarkerIcon markerIcon = instance.getDynmapPlugin().getMarkerAPI().getMarkerIcon(icon);
		if(markerIcon == null){
			throw new QDCommandException("Icon does not exists");
		}
		Marker marker = instance.getDynmapPlugin().getMarkerAPI().getMarkerSet(instance.getMarkersetName()).createMarker(poiName+"-"+String.format("%.2f",price), poiName+"-"+String.format("%.2f",price), player.getWorld().getName(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), markerIcon, true);
		this.getInterestPoints().get(marker.getWorld()).put(marker.getLabel(),new InterestPoint(marker.getWorld(), marker.getMarkerSet(), poiName, String.format("%.2f",price),new BlockVector2D( marker.getX(),marker.getZ())));
		
		instance.sendComments(player, ChatFormater.format("POI %s is now is created %f", poiName, price));
	}

}
