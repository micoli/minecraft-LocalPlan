package org.micoli.minecraft.realEstate.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.micoli.minecraft.realEstate.RealEstate;

import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotNull;

/**
 * @author o.michaud
 *
 */
@Entity
@Table(name = "mra_mre_real_estate")
public class QDObjectRealEstate {
	static RealEstate plugin;
	
	@Id
	private String id;

	@NotNull
	@Length(max = 100)
	private String world;

	@NotNull
	@Length(max = 100)
	private String playerOwner;
	
	/**
	 * @param player
	 */
	public QDObjectRealEstate() {
		plugin = RealEstate.getInstance();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getWorld() {
		return world;
	}

	public void setWorld(String world) {
		this.world = world;
	}

	public String getPlayerOwner() {
		return playerOwner;
	}

	public void setPlayerOwner(String playerOwner) {
		this.playerOwner = playerOwner;
	}

}
