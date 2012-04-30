package org.micoli.minecraft.localPlan.managers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

import javax.imageio.ImageIO;

import org.bukkit.World;
import org.dynmap.DynmapCore;
import org.dynmap.DynmapWorld;
import org.dynmap.MapManager;
import org.dynmap.MapTile;
import org.dynmap.bukkit.DynmapPlugin;
import org.dynmap.hdmap.HDMapTile;
import org.dynmap.hdmap.IsoHDPerspective;
import org.dynmap.utils.Matrix3D;
import org.dynmap.utils.Vector3D;
import org.micoli.minecraft.localPlan.LocalPlan;
import org.micoli.minecraft.utils.Images;
import org.micoli.minecraft.utils.Json;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ParcelExporter {
	LocalPlan plugin;

	public ParcelExporter(LocalPlan instance) {
		this.plugin = instance;
	}

	public DynmapCore getDynmapCore(DynmapPlugin dm) {
		Class<? extends DynmapPlugin> presumedClass = dm.getClass();
		Field f;
		try {
			f = presumedClass.getDeclaredField("core");
			f.setAccessible(true);
			return (DynmapCore) f.get(dm);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("static-access")
	public void getMaps() {
		DynmapPlugin dm = ((DynmapPlugin) plugin.pm.getPlugin("dynmap"));

		DynmapCore dynmapCore = getDynmapCore(dm);
		if (dynmapCore == null) {
			plugin.logger.log("Could not get acces to DynmapCore from DynmapPlugin");
			return;
		}

		IsoHDPerspective isoHDPerspective = (IsoHDPerspective) MapManager.mapman.hdmapman.perspectives.get("iso_SE_60_vlowres");
		if (isoHDPerspective == null) {
			plugin.logger.log("Could not get acces to isoHDPerspective from DynmapCore");
			return;
		}
		int w = isoHDPerspective.tileWidth;
		int h = isoHDPerspective.tileHeight;

		Matrix3D transform = new Matrix3D(0.0, 0.0, -1.0, -1.0, 0.0, 0.0, 0.0, 1.0, 0.0);
		transform.rotateXY(180 - isoHDPerspective.azimuth);
		transform.rotateYZ(90.0 - isoHDPerspective.inclination);
		transform.shearZ(0, Math.tan(Math.toRadians(90.0 - isoHDPerspective.inclination)));
		transform.scale(isoHDPerspective.scale, isoHDPerspective.scale, Math.sin(Math.toRadians(isoHDPerspective.inclination)));
		plugin.logger.log("Matrix %s", Json.exportObjectToJson(transform));

		plugin.logger.log("perspective %s", isoHDPerspective.toString());
		for (World world : plugin.getServer().getWorlds()) {
			String worldName = world.getName();
			DynmapWorld dynmapWorld = dynmapCore.getMapManager().getWorld(worldName);

			RegionManager rm = plugin.getWorldGuardPlugin().getRegionManager(world);
			if (rm == null) {
				continue;
			}

			Map<String, ProtectedRegion> regions = rm.getRegions();
			for (ProtectedRegion pr : regions.values()) {
				if (!pr.getId().equalsIgnoreCase("__global__") && pr.getId().equalsIgnoreCase("parcel1")) {
					MapTile[] mapTiles = isoHDPerspective.getTiles(dynmapWorld, pr.getMinimumPoint().getBlockX(), 65, pr.getMinimumPoint().getBlockZ(), pr.getMaximumPoint().getBlockX(), 65, pr.getMaximumPoint().getBlockZ());
					plugin.logger.log(" %s => %d tiles %s", pr.getId(), mapTiles.length, dynmapCore.getTilesFolder().getAbsolutePath());
					int i, minx = 0, miny = 0, maxx = 0, maxy = 0;
					for (i = 0; i < mapTiles.length; i++) {
						MapTile maptile = mapTiles[i];
						if (i == 0) {
							minx = maxx = maptile.tileOrdinalX();
							miny = maxy = maptile.tileOrdinalY();
						} else {
							minx = Math.min(minx, maptile.tileOrdinalX());
							miny = Math.min(miny, maptile.tileOrdinalY());
							maxx = Math.max(maxx, maptile.tileOrdinalX());
							maxy = Math.max(maxy, maptile.tileOrdinalY());
						}
					}
					int sizex = Math.abs(maxx - minx + 1);
					int sizey = Math.abs(maxy - miny + 1);
					BufferedImage ExportParcel = new BufferedImage(sizex * isoHDPerspective.tileHeight, sizey * isoHDPerspective.tileWidth, BufferedImage.TYPE_INT_ARGB);

					Vector3D block = new Vector3D();
					Vector3D point_01 = new Vector3D();
					block.x = 149;//pr.getMinimumPoint().getBlockX();
					block.y = 65;
					block.z = 182;//pr.getMinimumPoint().getBlockZ();
					transform.transform(block, point_01);
					
					//block.x = pr.getMaximumPoint().getBlockX();
					//block.y = 65;
					//block.z = pr.getMaximumPoint().getBlockZ();
					//transform.transform(block, cornermax);
					plugin.logger.log("min %s", Json.exportObjectToJson(point_01));

					for (i = 0; i < mapTiles.length; i++) {
						HDMapTile maptile = (HDMapTile)mapTiles[i];
						String mapTileFilename = MapManager.mapman.getTileFile(maptile).getAbsoluteFile().toString().replaceFirst("/hdmap", "/t");
						File inFile = new File(mapTileFilename);
						BufferedImage tileImage;
						try {
							tileImage = ImageIO.read(inFile);
							int mx = maptile.tileOrdinalX()*w;
							int my = maptile.tileOrdinalY()*h;
							
							int posx = (int) (point_01.x);
							int posy = (int) (point_01.y);
							int nposx,nposy;
							nposx=posx%w;
							nposy=posy%h;
							
							if(nposx<0){
								nposx=w+nposx;
							}
							if(nposy<0){
								nposy=h+nposy;
							}
							//flip posy
							nposy=h-nposy;
							plugin.logger.log("%s %s %d %d", mapTileFilename, maptile.getClass().toString(),maptile.tx,maptile.ty);
							plugin.logger.log("tile[%3d/%3d] (%2d,%2d) min(%5d,%5d) -> max(%5d,%5d) -> pos(%5d,%5d) -> npos(%5d,%5d)", maptile.tx,maptile.ty,w,h,mx, my,mx+w,my+h,posx,posy,nposx,nposy);
							
							if (mx< posx && posx<(mx+w) && my<posy && posy<(my+h)) {
								plugin.logger.log("color %s %d %d", mapTileFilename, nposx,nposy);
								tileImage.setRGB(nposx,nposy, 0xFFFF0000);
								savePNGBufferedImage(tileImage, String.format("%s/%s.png", plugin.getDataFolder(), "eeee"));
							}
							Images.copySrcIntoDstAt(tileImage, ExportParcel, (maptile.tileOrdinalX() - minx) * isoHDPerspective.tileWidth, (sizey - (maptile.tileOrdinalY() - miny) - 1) * isoHDPerspective.tileHeight);// isoHDPerspective.tileHeight,isoHDPerspective.tileWidth);
						} catch (IOException e) {
							e.printStackTrace();
						}
						savePNGBufferedImage(ExportParcel, String.format("%s/%s_%s.png", plugin.getDataFolder(), worldName, pr.getId()));
					}

					plugin.logger.log(" %s(%d) => %d %d / %d %d / %d %d", pr.getId(), mapTiles.length, minx, miny, maxx, maxy, sizex, sizey);
					plugin.logger.log("-----------------");
				}
			}
		}
	}

	void savePNGBufferedImage(BufferedImage bufferedImage, String fileName) {
		File pngWriter = new File(fileName);
		try {
			ImageIO.write(bufferedImage, "png", pngWriter);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
