package org.micoli.minecraft.localPlan.entities;

import java.util.Iterator;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.bukkit.entity.Player;
import org.micoli.minecraft.localPlan.LocalPlan;
import org.micoli.minecraft.localPlan.LocalPlanUtils;
import org.micoli.minecraft.utils.ServerLogger;

import com.avaje.ebean.annotation.EnumValue;
import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotNull;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * The Class Parcel.
 * 
 * @author o.michaud
 */

@Entity
@Table(name = "mra_prc_parcel")
public class Parcel {

	/** The plugin. */
	static LocalPlan plugin;

	public enum buyStatusTypes {
		@EnumValue("ANY")
		ANY,

		@EnumValue("BUYABLE")
		BUYABLE,

		@EnumValue("UNBUYABLE")
		UNBUYABLE,
	}

	public enum ownerTypes {
		@EnumValue("ANY")
		ANY,

		@EnumValue("STATE")
		STATE,

		@EnumValue("FACTION")
		FACTION,

		@EnumValue("PLAYER")
		PLAYER,
	}

	/** The id. */
	@Id
	@Length(max = 200)
	private String id;

	/** The regionId. */
	@NotNull
	@Length(max = 100)
	private String regionId;

	/** The world. */
	@NotNull
	@Length(max = 100)
	private String world;

	/** The player owner. */
	@NotNull
	@Length(max = 100)
	private String owner = "";

	@NotNull
	@Length(max = 100)
	private String pointOfInterest;

	@NotNull
	@Length(max = 100)
	private double distToPointOfInterest;

	/** The price. */
	@NotNull
	@Length(min = 1)
	private double price = 1;

	/** The surface. */
	@NotNull
	@Length(min = 1)
	private int surface = 1;

	private buyStatusTypes buyStatus = buyStatusTypes.BUYABLE;

	private ownerTypes ownerType = ownerTypes.STATE;

	/**
	 * Instantiates a new parcel.
	 */
	public Parcel() {
		plugin = LocalPlan.getInstance();
	}

	public Parcel(String worldName, ProtectedRegion region){
		plugin = LocalPlan.getInstance();
		String regionId = region.getId();


		this.setId(worldName + "::" + regionId);
		this.setWorld(worldName);
		this.setRegionId(regionId);
		this.setOwner("");
		this.setBuyStatus(Parcel.buyStatusTypes.BUYABLE);
		this.setPriceAndSurface(world,region);
	}
	
	public void setPriceAndSurface(String worldName,ProtectedRegion region){
		double maxDistance = 1024*1024;
		double dist = 0;
		ServerLogger.log("volume %d",region.volume());
		int surf = LocalPlanUtils.getRegionSurface(region);
		double price = plugin.markerDefaultPrice*surf;

		setPointOfInterest("");
		setDistToPointOfInterest(0);
		
		BlockVector2D barycentre = LocalPlanUtils.getBarycentre(region);
		Iterator<InterestPoint> interestPointIterator = plugin.getInterestPoints().get(worldName).iterator();
		while (interestPointIterator.hasNext()) {
			InterestPoint interestPoint = interestPointIterator.next();
			dist = LocalPlanUtils.blockVector2DDistance(barycentre,interestPoint.blockVector2D);
			if (dist<maxDistance){
				maxDistance=dist;
				setPointOfInterest(interestPoint.getLabel()); 
				setDistToPointOfInterest(dist);
				if (dist>plugin.markerMaximumDistance){
					price = surf * interestPoint.price;
				}else{
					price = surf * (interestPoint.price-(interestPoint.price-plugin.markerDefaultPrice) / plugin.markerMaximumDistance*dist);
				}
			}
		}
		ServerLogger.log("===>%s %d,%f",region.getId(),surf,price);
		this.setPrice(Math.round(price));
		this.setSurface(surf);
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the id.
	 * 
	 * @param id
	 *            the new id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Gets the world.
	 * 
	 * @return the world
	 */
	public String getWorld() {
		return world;
	}

	/**
	 * Sets the world.
	 * 
	 * @param world
	 *            the new world
	 */
	public void setWorld(String world) {
		this.world = world;
	}

	/**
	 * Gets the price.
	 * 
	 * @return the price
	 */
	public double getPrice() {
		return price;
	}

	/**
	 * Sets the price.
	 * 
	 * @param price
	 *            the new price
	 */
	public void setPrice(double price) {
		this.price = price;
	}

	/**
	 * Gets the surface.
	 * 
	 * @return the surface
	 */
	public int getSurface() {
		return surface;
	}

	/**
	 * Sets the surface.
	 * 
	 * @param surface
	 *            the new surface
	 */
	public void setSurface(int surface) {
		this.surface = surface;
	}

	public String getRegionId() {
		return regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @param owner
	 *            the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @return the buyStatus
	 */
	public buyStatusTypes getBuyStatus() {
		return buyStatus;
	}

	/**
	 * @param buyStatus
	 *            the buyStatus to set
	 */
	public void setBuyStatus(buyStatusTypes buyStatus) {
		this.buyStatus = buyStatus;
	}

	public String getPointOfInterest() {
		return pointOfInterest;
	}

	public void setPointOfInterest(String pointOfInterest) {
		this.pointOfInterest = pointOfInterest;
	}

	/**
	 * @return the ownerType
	 */
	public ownerTypes getOwnerType() {
		return ownerType;
	}

	/**
	 * @param ownerType
	 *            the ownerType to set
	 */
	public void setOwnerType(ownerTypes ownerType) {
		this.ownerType = ownerType;
	}

	public double getDistToPointOfInterest() {
		return distToPointOfInterest;
	}

	public void setDistToPointOfInterest(double distToPointOfInterest) {
		this.distToPointOfInterest = distToPointOfInterest;
	}

	public void save() {
		LocalPlan.getStaticDatabase().save(this);
	}

	public static Parcel getParcel(String world, String parcelName) {
		return LocalPlan.getStaticDatabase().find(Parcel.class).where().eq("id", world + "::" + parcelName).findUnique();
	}

	public static Parcel getParcel(String world, String parcelName, Player player) {
		return LocalPlan.getStaticDatabase().find(Parcel.class).where().eq("world", world).eq("regionId", parcelName).eq("owner", player.getName()).findUnique();
	}

	public void delete() {
		LocalPlan.getStaticDatabase().delete(this);
	}

}
