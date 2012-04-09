package org.micoli.minecraft.localPlan.entities;

import java.util.Scanner;

import org.dynmap.markers.MarkerSet;

import com.sk89q.worldedit.BlockVector2D;

public class InterestPoint {
	public BlockVector2D blockVector2D;
	public String world;
	public String label;
	public double price;

	public InterestPoint(String world, MarkerSet markerSet, String label, String priceStr, BlockVector2D blockVector2D) {
		Scanner scanner = new Scanner(priceStr);
		
		this.world = world;
		this.label = label;
		this.blockVector2D = blockVector2D;
		if (scanner.hasNextDouble()) {
			this.price = scanner.nextDouble();
		}
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
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
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
}
