package org.micoli.minecraft.localPlan.entities;

import java.util.Iterator;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.bukkit.entity.Player;
import org.micoli.minecraft.localPlan.LocalPlan;
import org.micoli.minecraft.localPlan.LocalPlanUtils;

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

	/**
	 * The Enum buyStatusTypes.
	 */
	public enum buyStatusTypes {

		/** The ANY. */
		@EnumValue("ANY")
		ANY,

		/** The BUYABLE. */
		@EnumValue("BUYABLE")
		BUYABLE,

		/** The UNBUYABLE. */
		@EnumValue("UNBUYABLE")
		UNBUYABLE,
	}

	/**
	 * The Enum ownerTypes.
	 */
	public enum ownerTypes {

		/** The ANY. */
		@EnumValue("ANY")
		ANY,

		/** The STATE. */
		@EnumValue("STATE")
		STATE,

		/** The FACTION. */
		@EnumValue("FACTION")
		FACTION,

		/** The PLAYER. */
		@EnumValue("PLAYER")
		PLAYER, 
		
		/** The SYSTEM. */
		@EnumValue("SYSTEM")
		SYSTEM,
	}

	/** The id. */
	@Id
	@Length(max = 200)
	protected String id;

	/** The regionId. */
	@NotNull
	@Length(max = 100)
	protected String regionId;

	/** The world. */
	@NotNull
	@Length(max = 100)
	protected String world;

	/** The player owner. */
	@NotNull
	@Length(max = 100)
	protected String owner = "";

	/** The point of interest. */
	@NotNull
	@Length(max = 100)
	protected String pointOfInterest;

	/** The dist to point of interest. */
	@NotNull
	@Length(max = 100)
	protected double distToPointOfInterest;

	/** The price. */
	@NotNull
	@Length(min = 1)
	protected double price = 1;

	/** The surface. */
	@NotNull
	@Length(min = 1)
	protected int surface = 1;

	/** The buy status. */
	protected buyStatusTypes buyStatus = buyStatusTypes.BUYABLE;

	/** The owner type. */
	protected ownerTypes ownerType = ownerTypes.STATE;

	/**
	 * Instantiates a new parcel.
	 */
	public Parcel() {
		plugin = LocalPlan.getInstance();
	}

	/**
	 * Instantiates a new parcel.
	 * 
	 * @param worldName
	 *            the world name
	 * @param region
	 *            the region
	 */
	public Parcel(String worldName, ProtectedRegion region) {
		plugin = LocalPlan.getInstance();
		String regionIdPrm = region.getId();

		this.setId(worldName + "::" + regionIdPrm);
		this.setWorld(worldName);
		this.setRegionId(regionIdPrm);
		this.setOwner("");
		this.setBuyStatus(Parcel.buyStatusTypes.BUYABLE);
		this.setPriceAndSurface(world, region);
	}

	/**
	 * Sets the price and surface.
	 * 
	 * @param worldName
	 *            the world name
	 * @param region
	 *            the region
	 */
	public void setPriceAndSurface(String worldName, ProtectedRegion region) {
		double maxDistance = 1024 * 1024;
		double dist = 0;
		plugin.logger.log("volume %d", region.volume());
		int surf = LocalPlanUtils.getRegionSurface(region);
		double calcPrice = plugin.getMarkerDefaultPrice() * surf;

		setPointOfInterest("");
		setDistToPointOfInterest(0);

		BlockVector2D barycentre = LocalPlanUtils.getBarycentre(region);
		Iterator<String> interestPointIterator = null;
		try {
			interestPointIterator = plugin.getInterestPointManager().getInterestPoints().get(worldName).keySet().iterator();
		} catch (Exception e) {
		}
		if (interestPointIterator == null) {
			calcPrice=0;
		} else {
			while (interestPointIterator.hasNext()) {
				InterestPoint interestPoint = plugin.getInterestPointManager().getInterestPoints().get(worldName).get(interestPointIterator.next());
				dist = LocalPlanUtils.blockVector2DDistance(barycentre, interestPoint.getBlockVector2D());
				if (dist < maxDistance) {
					maxDistance = dist;
					setPointOfInterest(interestPoint.getLabel());
					setDistToPointOfInterest(dist);
					if (dist > plugin.getMarkerMaximumDistance()) {
						calcPrice = surf * interestPoint.getPrice();
					} else {
						calcPrice = surf * (interestPoint.getPrice() - (interestPoint.getPrice() - plugin.getMarkerDefaultPrice()) / plugin.getMarkerMaximumDistance() * dist);
					}
				}
			}
		}
		plugin.logger.log("===>%s %d,%f", region.getId(), surf, calcPrice);
		this.setPrice(Math.round(calcPrice));
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

	/**
	 * Gets the region id.
	 * 
	 * @return the region id
	 */
	public String getRegionId() {
		return regionId;
	}

	/**
	 * Sets the region id.
	 * 
	 * @param regionId
	 *            the new region id
	 */
	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	/**
	 * Gets the owner.
	 * 
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * Sets the owner.
	 * 
	 * @param owner
	 *            the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * Gets the buy status.
	 * 
	 * @return the buyStatus
	 */
	public buyStatusTypes getBuyStatus() {
		return buyStatus;
	}

	/**
	 * Sets the buy status.
	 * 
	 * @param buyStatus
	 *            the buyStatus to set
	 */
	public void setBuyStatus(buyStatusTypes buyStatus) {
		this.buyStatus = buyStatus;
	}

	/**
	 * Gets the point of interest.
	 * 
	 * @return the point of interest
	 */
	public String getPointOfInterest() {
		return pointOfInterest;
	}

	/**
	 * Sets the point of interest.
	 * 
	 * @param pointOfInterest
	 *            the new point of interest
	 */
	public void setPointOfInterest(String pointOfInterest) {
		this.pointOfInterest = pointOfInterest;
	}

	/**
	 * Gets the owner type.
	 * 
	 * @return the ownerType
	 */
	public ownerTypes getOwnerType() {
		return ownerType;
	}

	/**
	 * Sets the owner type.
	 * 
	 * @param ownerType
	 *            the ownerType to set
	 */
	public void setOwnerType(ownerTypes ownerType) {
		this.ownerType = ownerType;
	}

	/**
	 * Gets the dist to point of interest.
	 * 
	 * @return the dist to point of interest
	 */
	public double getDistToPointOfInterest() {
		return distToPointOfInterest;
	}

	/**
	 * Sets the dist to point of interest.
	 * 
	 * @param distToPointOfInterest
	 *            the new dist to point of interest
	 */
	public void setDistToPointOfInterest(double distToPointOfInterest) {
		this.distToPointOfInterest = distToPointOfInterest;
	}

	/**
	 * Save.
	 */
	public void save() {
		plugin.getStaticDatabase().save(this);
	}

	/**
	 * Gets the parcel.
	 * 
	 * @param world
	 *            the world
	 * @param parcelName
	 *            the parcel name
	 * @return the parcel
	 */
	public static Parcel getParcel(String world, String parcelName) {
		//plugin.logger.log("%s",world);
		return plugin.getStaticDatabase().find(Parcel.class).where().eq("id", world + "::" + parcelName).findUnique();
	}

	/**
	 * Gets the parcel.
	 * 
	 * @param world
	 *            the world
	 * @param parcelName
	 *            the parcel name
	 * @param player
	 *            the player
	 * @return the parcel
	 */
	public static Parcel getParcel(String world, String parcelName, Player player) {
		return plugin.getStaticDatabase().find(Parcel.class).where().eq("world", world).eq("regionId", parcelName).eq("owner", player.getName()).findUnique();
	}

	/**
	 * Delete.
	 */
	public void delete() {
		plugin.getStaticDatabase().delete(this);
	}

}
