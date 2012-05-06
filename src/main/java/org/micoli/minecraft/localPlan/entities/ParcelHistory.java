package org.micoli.minecraft.localPlan.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.micoli.minecraft.localPlan.LocalPlan;
import org.micoli.minecraft.localPlan.entities.Parcel.buyStatusTypes;
import org.micoli.minecraft.localPlan.entities.Parcel.ownerTypes;

import com.avaje.ebean.annotation.EnumValue;
import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotNull;

/**
 * The Class Parcel.
 * 
 * @author o.michaud
 */

@Entity
@Table(name = "mra_prh_parcel_history")
public class ParcelHistory{
	/** The plugin. */
	transient static LocalPlan plugin;

	/**
	 * The Enum buyStatusTypes.
	 */
	public enum historyTypes {

		/** The ANY. */
		@EnumValue("ANY")
		ANY,

		@EnumValue("ALLOCATION")
		ALLOCATION,

		@EnumValue("SALE")
		SALE,

		@EnumValue("CREATION")
		CREATION,
		
		@EnumValue("SET_UNBUYABLE")
		SET_UNBUYABLE,
		
		@EnumValue("SET_BUYABLE")
		SET_BUYABLE, 
		
		@EnumValue("MODIFICATION")
		MODIFICATION
	}
	
	/** The id. */
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
	@Length(max = 100)
	protected String owner = "";

	/** The point of interest. */
	@Length(max = 100)
	protected String pointOfInterest;

	/** The dist to point of interest. */
	@Length(max = 100)
	protected double distToPointOfInterest;

	/** The price. */
	@Length(min = 1)
	protected double price = 1;

	/** The surface. */
	@Length(min = 1)
	protected int surface = 1;

	/** The buy status. */
	protected buyStatusTypes buyStatus = buyStatusTypes.BUYABLE;

	/** The owner type. */
	protected ownerTypes ownerType = ownerTypes.STATE;
	
	private Date date;

	private historyTypes historyType = historyTypes.CREATION;

	
	/** The comment. */
	@Length(max = 200)
	protected String comment;
	
	/**
	 * Instantiates a new parcel.
	 */
	public ParcelHistory() {
		plugin = LocalPlan.getInstance();
	}
	/**
	 * Instantiates a new parcel history.
	 */
	public ParcelHistory(Parcel parcel,historyTypes historyType,String comment,boolean autoSave) {
		plugin = LocalPlan.getInstance();
		this.setBuyStatus(parcel.getBuyStatus());
		this.setDate(new Date());
		this.setId(parcel.getId());
		this.setRegionId(parcel.getRegionId());
		this.setWorld(parcel.getWorld());
		this.setOwner(parcel.getOwner());
		this.setOwnerType(parcel.getOwnerType());
		this.setPrice(parcel.getPrice());
		this.setSurface(parcel.getSurface());
		this.setHistoryType(historyType);
		this.setComment(comment);
		if (autoSave){
			this.save();
		}
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the regionId
	 */
	public String getRegionId() {
		return regionId;
	}

	/**
	 * @param regionId the regionId to set
	 */
	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	/**
	 * @return the world
	 */
	public String getWorld() {
		return world;
	}

	/**
	 * @param world the world to set
	 */
	public void setWorld(String world) {
		this.world = world;
	}

	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @param owner the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @return the pointOfInterest
	 */
	public String getPointOfInterest() {
		return pointOfInterest;
	}

	/**
	 * @param pointOfInterest the pointOfInterest to set
	 */
	public void setPointOfInterest(String pointOfInterest) {
		this.pointOfInterest = pointOfInterest;
	}

	/**
	 * @return the distToPointOfInterest
	 */
	public double getDistToPointOfInterest() {
		return distToPointOfInterest;
	}

	/**
	 * @param distToPointOfInterest the distToPointOfInterest to set
	 */
	public void setDistToPointOfInterest(double distToPointOfInterest) {
		this.distToPointOfInterest = distToPointOfInterest;
	}

	/**
	 * @return the price
	 */
	public double getPrice() {
		return price;
	}

	/**
	 * @param price the price to set
	 */
	public void setPrice(double price) {
		this.price = price;
	}

	/**
	 * @return the surface
	 */
	public int getSurface() {
		return surface;
	}

	/**
	 * @param surface the surface to set
	 */
	public void setSurface(int surface) {
		this.surface = surface;
	}

	/**
	 * @return the buyStatus
	 */
	public buyStatusTypes getBuyStatus() {
		return buyStatus;
	}

	/**
	 * @param buyStatus the buyStatus to set
	 */
	public void setBuyStatus(buyStatusTypes buyStatus) {
		this.buyStatus = buyStatus;
	}

	/**
	 * @return the ownerType
	 */
	public ownerTypes getOwnerType() {
		return ownerType;
	}

	/**
	 * @param ownerType the ownerType to set
	 */
	public void setOwnerType(ownerTypes ownerType) {
		this.ownerType = ownerType;
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * @return the historyType
	 */
	public historyTypes getHistoryType() {
		return historyType;
	}

	/**
	 * @param historyType the historyType to set
	 */
	public void setHistoryType(historyTypes historyType) {
		this.historyType = historyType;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
	/**
	 * Save.
	 */
	public void save() {
		plugin.getStaticDatabase().save(this);
	}

}
