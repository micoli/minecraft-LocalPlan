package org.micoli.minecraft.localPlan.entities;

import java.util.Scanner;

import org.dynmap.markers.MarkerSet;

import com.sk89q.worldedit.BlockVector2D;

/**
 * The Class InterestPoint.
 */
public class InterestPoint {
	
	/** The block vector2 d. */
	private BlockVector2D blockVector2D;
	
	/** The world. */
	private String world;
	
	/** The label. */
	private String label;
	
	/** The price. */
	private double price;

	/**
	 * Instantiates a new interest point.
	 *
	 * @param world the world
	 * @param markerSet the marker set
	 * @param label the label
	 * @param priceStr the price str
	 * @param blockVector2D the block vector2 d
	 */
	public InterestPoint(String world, MarkerSet markerSet, String label, String priceStr, BlockVector2D blockVector2D) {
		Scanner scanner = new Scanner(priceStr);
		
		this.world = world;
		this.label = label;
		this.setBlockVector2D(blockVector2D);
		if (scanner.hasNextDouble()) {
			this.price = scanner.nextDouble();
		}
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
	 * @param world the world to set
	 */
	public void setWorld(String world) {
		this.world = world;
	}

	/**
	 * Gets the label.
	 *
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label.
	 *
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
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
	 * @param price the price to set
	 */
	public void setPrice(double price) {
		this.price = price;
	}

	/**
	 * @return the blockVector2D
	 */
	public BlockVector2D getBlockVector2D() {
		return blockVector2D;
	}

	/**
	 * @param blockVector2D the blockVector2D to set
	 */
	final public void setBlockVector2D(BlockVector2D blockVector2D) {
		this.blockVector2D = blockVector2D;
	}
}
