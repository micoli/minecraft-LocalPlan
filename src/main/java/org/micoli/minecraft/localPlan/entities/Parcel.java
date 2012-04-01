package org.micoli.minecraft.localPlan.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.micoli.minecraft.localPlan.LocalPlan;

import com.avaje.ebean.annotation.EnumValue;
import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotNull;

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
	 * The Enum parcelStatus.
	 */
	public enum parcelStatus {
		
		/** The ANY. */
		@EnumValue("ANY")
		ANY,
		
		/** The FREE. */
		@EnumValue("FREE")
		FREE,
		
		/** The OWNED. */
		@EnumValue("OWNED")
		OWNED,
		
		/** The OWNE d_ buyable. */
		@EnumValue("OWNED_BUYABLE")
		OWNED_BUYABLE,
		
		/** The SYSTEM. */
		@EnumValue("SYSTEM")
		SYSTEM
	}
	
	/** The id. */
	@Id
	private String id;

	/** The world. */
	@NotNull
	@Length(max = 100)
	private String world;

	/** The player owner. */
	@NotNull
	@Length(max = 100)
	private String playerOwner;
	
	/** The price. */
	@NotNull
	@Length(min = 1)
	private double price=1;
	
	/** The surface. */
	@NotNull
	@Length(min = 1)
	private int surface=1;
	
	/** The status. */
	private parcelStatus status = parcelStatus.SYSTEM; 
	
	/**
	 * Instantiates a new parcel.
	 */
	public Parcel() {
		plugin = LocalPlan.getInstance();
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
	 * @param id the new id
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
	 * @param world the new world
	 */
	public void setWorld(String world) {
		this.world = world;
	}

	/**
	 * Gets the player owner.
	 *
	 * @return the player owner
	 */
	public String getPlayerOwner() {
		return playerOwner;
	}

	/**
	 * Sets the player owner.
	 *
	 * @param playerOwner the new player owner
	 */
	public void setPlayerOwner(String playerOwner) {
		this.playerOwner = playerOwner;
	}

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public parcelStatus getStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 *
	 * @param status the new status
	 */
	public void setStatus(parcelStatus status) {
		this.status = status;
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
	 * @param price the new price
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
	 * @param surface the new surface
	 */
	public void setSurface(int surface) {
		this.surface = surface;
	}

}
