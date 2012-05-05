package org.micoli.minecraft.localPlan.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.micoli.minecraft.localPlan.LocalPlan;

import com.avaje.ebean.annotation.EnumValue;
import com.avaje.ebean.validation.Length;

/**
 * The Class Parcel.
 * 
 * @author o.michaud
 */

@Entity
@Table(name = "mra_prh_parcel_history")
public class ParcelHistory extends Parcel{
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
	/** The plugin. */
	static LocalPlan plugin;
	
	/** The id. */
	@Length(max = 200)
	protected String id;
	
	protected Date date;

	protected historyTypes historyType = historyTypes.CREATION;

	
	/** The comment. */
	@Length(max = 200)
	protected String comment;
	
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
	
}
