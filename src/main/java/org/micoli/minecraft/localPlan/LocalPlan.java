package org.micoli.minecraft.localPlan;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.micoli.minecraft.bukkit.QDBukkitPlugin;
import org.micoli.minecraft.bukkit.QDCommand;
import org.micoli.minecraft.localPlan.entities.Parcel;
import org.micoli.minecraft.localPlan.entities.Parcel.parcelStatus;
import org.micoli.minecraft.localPlan.managers.QDCommandManager;
import org.micoli.minecraft.utils.BlockUtils;
import org.micoli.minecraft.utils.ChatFormater;
import org.micoli.minecraft.utils.ServerLogger;

import com.avaje.ebean.Expression;
import com.avaje.ebean.Query;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.UnsupportedIntersectionException;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * The Class LocalPlan.
 */
public class LocalPlan extends QDBukkitPlugin implements ActionListener {

	/** The my executor. */
	protected QDCommandManager executor;

	/** The instance. */
	private static LocalPlan instance;

	/** The internal array of parcels. */
	public static Map<String, Parcel> aParcel;

	/** The worldguard plugin. */
	private WorldGuardPlugin worldGuardPlugin;

	/** The worldedit plugin. */
	private WorldEditPlugin worldEditPlugin;

	/** The preview blocks. */
	private HashMap<String, List<Block>> previewBlocks = new HashMap<String, List<Block>>();

	/**
	 * Gets the single instance of LocalPlan.
	 * 
	 * @return the instance
	 */
	public static LocalPlan getInstance() {
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.micoli.minecraft.bukkit.QDBukkitPlugin#onEnable()
	 */
	@Override
	public void onEnable() {
		super.onEnable();
		log(ChatFormater.format("%s version enabled", this.pdfFile.getName(), this.pdfFile.getVersion()));

		aParcel = new HashMap<String, Parcel>();
		instance = this;
		commandString = "lp";
		worldGuardPlugin = getWorldGuard();
		worldEditPlugin = getWorldEdit();

		loadConfiguration();
		initializeDatabase();

		// ServerLogger.log("Parcels list");
		// Iterator<Parcel> iter =
		// getStaticDatabase().find(Parcel.class).findList().iterator();
		// while (iter.hasNext()) {
		// Parcel re = iter.next();
		// ServerLogger.log("[%s]",re.getId());
		// }
		// ServerLogger.log("Parcels list end");
		initalizeRegions();

		executor = new QDCommandManager(this, new Class[] { getClass() });
	}

	/**
	 * Gets the world guard.
	 * 
	 * @return the world guard
	 */
	private WorldGuardPlugin getWorldGuard() {
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

		// WorldGuard may not be loaded
		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
			ServerLogger.log("WorldGuard not found");
			return null; // Maybe you want throw an exception instead
		}

		return (WorldGuardPlugin) plugin;
	}

	/**
	 * Gets the world edit.
	 * 
	 * @return the world edit
	 */
	private WorldEditPlugin getWorldEdit() {
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldEdit");

		if (plugin == null || !(plugin instanceof WorldEdit)) {
			Plugin[] plugs = getServer().getPluginManager().getPlugins();
			for (int t = 0; t < plugs.length; t++) {
				if (plugs[t].getName().trim().equalsIgnoreCase("WorldEdit")) {
					plugin = plugs[t];
					break;
				}
			}
		}

		// WorldEdit may not be loaded
		if (plugin == null || !(plugin instanceof WorldEditPlugin)) {
			ServerLogger.log("WorldEdit not found");
			return null; // Maybe you want throw an exception instead
		}

		return (WorldEditPlugin) plugin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.micoli.minecraft.bukkit.QDBukkitPlugin#getDatabaseORMClasses()
	 */
	protected java.util.List<Class<?>> getDatabaseORMClasses() {
		List<Class<?>> list = new ArrayList<Class<?>>();
		list.add(Parcel.class);
		return list;
	};

	/**
	 * List parcels.
	 * 
	 * @param player
	 *            the player
	 * @param owner
	 *            the owner
	 * @param status
	 *            the status
	 */
	public void listParcels(Player player, String owner, parcelStatus status) {
		String ownerArg = owner;
		String statusArg = Parcel.parcelStatus.ANY.toString();

		if (owner.equalsIgnoreCase("__all__")) {
			owner = "ALL";
			ownerArg = "%";
		}

		if (status.equals(Parcel.parcelStatus.ANY)) {
			statusArg = "%";
		} else {
			statusArg = status.toString();
		}

		// todo revoir l'ordre des resultats pour les alignements
		Iterator<Parcel> parcelIterator = getStaticDatabase().find(Parcel.class).where().like("playerOwner", ownerArg).like("status", statusArg).orderBy("id desc").findList().iterator();

		if (parcelIterator.hasNext()) {
			sendComments(player, ChatFormater.format("List of owned parcels"));
			while (parcelIterator.hasNext()) {
				Parcel re = parcelIterator.next();
				sendComments(player, ChatFormater.format("%5s:%15s:%8s:%8s", re.getWorld(), re.getRegionId(), re.getPlayerOwner(), re.getStatus()));
			}
			sendComments(player, ChatFormater.format("-----------"));
		} else {
			sendComments(player, ChatFormater.format("No parcels", owner));
		}
	}

	/**
	 * Initalize regions.
	 */
	public void initalizeRegions() {
		final int maxdepth = 10;
		ServerLogger.log("InitalizeRegions");
		for (World w : getServer().getWorlds()) {
			String worldName = w.getName();
			List<String> listRegions = new ArrayList<String>();
			Map<?, Parcel> listParcels = getStaticDatabase().find(Parcel.class).where().like("world", worldName).findMap();
			ServerLogger.log("Map %s (%d)", worldName, listParcels.size());

			RegionManager rm = worldGuardPlugin.getRegionManager(w);
			if (rm == null)
				continue;

			Map<String, ProtectedRegion> regions = rm.getRegions();
			for (ProtectedRegion pr : regions.values()) {
				int depth = 1;
				ProtectedRegion p = pr;
				while (p.getParent() != null) {
					depth++;
					p = p.getParent();
				}
				if (depth > maxdepth) {
					continue;
				}
				if (pr.getId() != "__global__") {
					ServerLogger.log("W:%s,P:%s,O:%s,S:%d", worldName, pr.getId(), pr.getOwners().toPlayersString(), pr.volume() / 256);
					listRegions.add(pr.getId());
					if (!listParcels.containsKey(worldName + "::" + pr.getId())) {
						Parcel parcel = new Parcel();
						parcel.setId(worldName + "::" + pr.getId());
						parcel.setWorld(worldName);
						parcel.setRegionId(pr.getId());
						parcel.setPlayerOwner("__state__");
						parcel.setStatus(Parcel.parcelStatus.FREE);
						parcel.save();

						ServerLogger.log("Automatically adding %s::%s(%s)", worldName, pr.getId(), pr.getOwners().toPlayersString());
					} else {
						Parcel parcel = listParcels.get(worldName + "::" + pr.getId());
						if (parcel.getSurface() != pr.volume() / 256) {
							parcel.setSurface(pr.volume() / 256);
							parcel.save();

							ServerLogger.log("Updating surface of  %s::%s(%d)", worldName, pr.getId(), parcel.getSurface());
						}
					}
				}
			}
			ServerLogger.log("List of deleted parcels");
			Query<?> query = getStaticDatabase().find(Parcel.class);
			Expression exp = query.getExpressionFactory().in("regionId", listRegions);
			@SuppressWarnings("unchecked")
			Iterator<Parcel> listDeletedParcels = (Iterator<Parcel>) query.where().like("world", worldName).not(exp).findList().iterator();
			while (listDeletedParcels.hasNext()) {
				Parcel parcel = listDeletedParcels.next();
				ServerLogger.log("Automatically removing W:%s,P:%s", worldName, parcel.getRegionId());
				parcel.delete();
			}
		}
		ServerLogger.log("EndInitalizeRegions");

	}

	/**
	 * Gets the parcel around.
	 * 
	 * @param player
	 *            the player
	 * @return the parcel around
	 */
	public void getParcelAround(Player player) {
		World w = player.getWorld();
		Vector pt = toVector(player.getLocation());

		ApplicableRegionSet set = worldGuardPlugin.getRegionManager(w).getApplicableRegions(pt);

		ServerLogger.log("list %s %s", player.getName(), set.toString());
		for (ProtectedRegion reg : set) {
			ServerLogger.log("%s %s", player.getName(), reg.getId());
		}
		ServerLogger.log("--list %s", player.getName());
	}

	/**
	 * Creates the parcel.
	 * 
	 * @param player
	 *            the player
	 * @param id
	 *            the id
	 */
	public void createParcel(Player player, String parcelName) {
		try {

			if (!ProtectedRegion.isValidId(parcelName)) {
				throw new CommandException("Invalid region ID specified!");
			}

			if (parcelName.equalsIgnoreCase("__global__")) {
				throw new CommandException("A region cannot be named __global__");
			}

			// Attempt to get the player's selection from WorldEdit
			Selection sel = worldEditPlugin.getSelection(player);

			if (sel == null) {
				throw new CommandException("Select a region with WorldEdit first.");
			}

			World w = sel.getWorld();
			RegionManager mgr = worldGuardPlugin.getGlobalRegionManager().get(w);
			if (mgr.hasRegion(parcelName)) {
				throw new CommandException("That region is already defined. Use redefine instead.");
			}

			ProtectedRegion region;

			// Detect the type of region from WorldEdit
			if (sel instanceof Polygonal2DSelection) {
				Polygonal2DSelection polySel = (Polygonal2DSelection) sel;
				region = new ProtectedPolygonalRegion(parcelName, polySel.getNativePoints(), 0, w.getMaxHeight());
			} else if (sel instanceof CuboidSelection) {
				BlockVector min = sel.getNativeMinimumPoint().toBlockVector();
				BlockVector max = sel.getNativeMaximumPoint().toBlockVector();
				min.setY(0);
				max.setY(w.getMaxHeight());
				region = new ProtectedCuboidRegion(parcelName, min, max);
			} else {
				throw new CommandException("The type of region selected in WorldEdit is unsupported in WorldGuard!");
			}
			List<ProtectedRegion> allregionslist = new ArrayList<ProtectedRegion>(mgr.getRegions().values());
			List<ProtectedRegion> overlaps;

			try {
				overlaps = region.getIntersectingRegions(allregionslist);
				if (!(overlaps == null || overlaps.isEmpty())) {
					throw new CommandException("That region is overlapping an existing one.");
				}
			} catch (UnsupportedIntersectionException e) {
				e.printStackTrace();
			}
			// Get the list of region owners
			DefaultDomain own = new DefaultDomain();
			own.addPlayer(player.getName());
			region.setOwners(own);
			setRegionFlag(player, region, "BUILD", "allow");

			mgr.addRegion(region);

			Parcel parcel = new Parcel();
			parcel.setId(w.getName() + "::" + region.getId());
			parcel.setWorld(w.getName());
			parcel.setRegionId(region.getId());
			parcel.setPlayerOwner("__state__");
			parcel.setStatus(Parcel.parcelStatus.FREE);
			parcel.setSurface(region.volume() / 256);

			parcel.save();

			try {
				mgr.save();
				sendComments(player, ChatColor.YELLOW + "Region saved as " + parcelName + ".", false);
			} catch (ProtectionDatabaseException e) {
				throw new CommandException("Failed to write regions: " + e.getMessage());
			}
		} catch (CommandException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Sets the flag.
	 * 
	 * @param <V>
	 *            the value type
	 * @param region
	 *            the region
	 * @param flag
	 *            the flag
	 * @param sender
	 *            the sender
	 * @param value
	 *            the value
	 * @throws InvalidFlagFormat
	 *             the invalid flag format
	 * @author sk89q
	 */
	public <V> void setFlag(ProtectedRegion region, Flag<V> flag, CommandSender sender, String value) throws InvalidFlagFormat {
		region.setFlag(flag, flag.parseInput(worldGuardPlugin, sender, value));
	}

	/**
	 * Sets the region flag.
	 * 
	 * @param player
	 *            the player
	 * @param region
	 *            the region
	 * @param flagName
	 *            the flag name
	 * @param flagValue
	 *            the flag value
	 * @author sk89q
	 */
	private void setRegionFlag(Player player, ProtectedRegion region, String flagName, String flagValue) {
		Flag<?> foundFlag = null;
		// Now time to find the flag!
		for (Flag<?> flag : DefaultFlag.getFlags()) {
			// Try to detect the flag
			if (flag.getName().replace("-", "").equalsIgnoreCase(flagName)) {
				foundFlag = flag;
				break;
			}
		}

		if (foundFlag != null) {
			try {
				setFlag(region, foundFlag, player, flagValue);
			} catch (InvalidFlagFormat e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Teleport the player to parcel.
	 * 
	 * @param player
	 *            the player
	 * @param id
	 *            the id
	 */
	public void teleportToParcel(Player player, String parcelName) {
		World w = player.getWorld();
		RegionManager rm = worldGuardPlugin.getRegionManager(w);
		ProtectedRegion region = rm.getRegion(parcelName);
		if (region != null) {
			final BlockVector min = region.getMinimumPoint();
			final BlockVector max = region.getMaximumPoint();
			Location dstLocation = BlockUtils.getTopPositionAtPos(new Location(w, (double) (min.getBlockX() + max.getBlockX()) / 2, (double) 0, (double) (min.getBlockZ() + max.getBlockZ()) / 2));
			player.teleport(dstLocation);
		}
	}

	/**
	 * Allocate parcel.
	 * 
	 * @param player
	 *            the player
	 * @param parcelName
	 *            the parcel name
	 * @param newOwner
	 *            the new owner
	 */
	public void allocateParcel(Player player, String WorldId, String parcelName, String newOwner) {
		Parcel parcel = Parcel.getParcel(WorldId, parcelName);
		if (parcel == null) {
			sendComments(player, "Parcel not found");
			return;
		}

		RegionManager mgr = worldGuardPlugin.getGlobalRegionManager().get(getServer().getWorld(parcel.getWorld()));
		ProtectedRegion region = mgr.getRegion(parcel.getRegionId());

		DefaultDomain own = new DefaultDomain();
		own.addPlayer(newOwner);
		region.setOwners(own);

		parcel.setPlayerOwner(newOwner);
		parcel.setStatus(Parcel.parcelStatus.OWNED);
		parcel.save();

		sendComments(player, ChatFormater.format("Allocation of %s to %s done", parcelName, newOwner));
	}

	/**
	 * Sets the buyable.
	 * 
	 * @param player
	 *            the player
	 * @param parcelName
	 *            the parcel name
	 * @param priceString
	 *            the price string
	 */
	public void setBuyable(Player player, String parcelName, String priceString) {
		Parcel parcel = Parcel.getParcel(player.getWorld().toString(), parcelName, player);
		if (parcel == null) {
			sendComments(player, "Parcel not found or doesn't belong to you");
			return;
		}
		Scanner scanner = new Scanner(priceString);
		if (!scanner.hasNextDouble()) {
			sendComments(player, "Price not found or not the good format 99.9");
			return;
		}
		double price = scanner.nextDouble();
		parcel.setPrice(price);
		parcel.setStatus(Parcel.parcelStatus.OWNED_BUYABLE);
		parcel.save();
		sendComments(player, ChatFormater.format("Parcel %s is now buyable at the following price %f", parcelName, price));
	}

	/**
	 * Sets the unbuyable.
	 * 
	 * @param player
	 *            the player
	 * @param parcelName
	 *            the parcel name
	 */
	public void setUnbuyable(Player player, String parcelName) {
		Parcel parcel = Parcel.getParcel(player.getWorld().toString(), parcelName, player);
		if (parcel == null) {
			sendComments(player, "Parcel not found or doesn't belong to you");
			return;
		}
		parcel.setStatus(Parcel.parcelStatus.OWNED);
		parcel.save();
		sendComments(player, ChatFormater.format("Parcel %s is now unbuyable ", parcelName));
	}

	/**
	 * Buy parcel.
	 * 
	 * @param player
	 *            the player
	 * @param parcelName
	 *            the parcel name
	 */
	public void buyParcel(Player player, String parcelName) {
		Parcel parcel = Parcel.getParcel(player.getWorld().toString(), parcelName);
		if (parcel == null) {
			sendComments(player, "Parcel not found");
			return;
		}

		if (parcel.getStatus().equals(Parcel.parcelStatus.OWNED_BUYABLE)) {
			sendComments(player, "Parcel is not buyable");
			return;
		}

		if (parcel.getPrice() > vaultEconomy.getBalance(player.getName())) {
			sendComments(player, ChatFormater.format("Not enough money to buy that parcel %f<%f", vaultEconomy.getBalance(player.getName()), parcel.getPrice()));
			return;
		}
		vaultEconomy.depositPlayer(parcel.getPlayerOwner(), parcel.getPrice());
		vaultEconomy.withdrawPlayer(player.getName(), parcel.getPrice());
		allocateParcel(player, player.getWorld().toString(), parcelName, player.getDisplayName());
		sendComments(player, ChatFormater.format("Parcel %s bought ", parcelName));

	}

	/**
	 * Manage parcel member.
	 * 
	 * @param player
	 *            the player
	 * @param args
	 *            the args
	 */
	public void manageParcelMember(Player player, String[] args) {
		String parcelName = args[1];
		Parcel parcel = Parcel.getParcel(player.getWorld().toString(), parcelName);
		if (parcel == null) {
			sendComments(player, "Parcel not found");
			return;
		}

		if (!(parcel.getPlayerOwner().equalsIgnoreCase(player.getName()) || vaultPermission.playerHas(player, "localPlan.members.allow"))) {
			sendComments(player, "You don't have right on that Parcel");
			return;
		}
		sendComments(player, "You have right on that Parcel, but nothing is coded ^^");
	}

	/**
	 * Show parcel.
	 * 
	 * @param player
	 *            the player
	 * @param parcelName
	 *            the parcel name
	 */
	public void showParcel(Player player, String parcelName) {
		Parcel parcel = Parcel.getParcel(player.getWorld().toString(), parcelName);
		if (parcel == null) {
			sendComments(player, "Parcel not found " + player.getWorld().toString() + "::" + parcelName);
			return;
		}
		if (!(parcel.getPlayerOwner().equalsIgnoreCase(player.getName()) || vaultPermission.playerHas(player, "localPlan.members.allow"))) {
			sendComments(player, "You don't have right on that Parcel");
			return;
		}
		if (previewBlocks.containsKey(player.getWorld().getName() + "::" + parcel.getRegionId())) {
			sendComments(player, "Parcel preview already shown");
			return;
		}
		List<Block> listBlock = new ArrayList<Block>();
		previewBlocks.put(player.getWorld().getName() + "::" + parcel.getRegionId(), listBlock);
		RegionManager mgr = worldGuardPlugin.getGlobalRegionManager().get(getServer().getWorld(parcel.getWorld()));
		ProtectedRegion region = mgr.getRegion(parcel.getRegionId());

		int nb = 0;
		World world = getServer().getWorld(parcel.getWorld());
		List<BlockVector2D> points = region.getPoints();
		if (points != null && points.size() > 0) {
			BlockVector2D firstPoint = points.get(0);
			BlockVector2D lastPoint = points.get(points.size() - 1);
			if (region.getTypeName().equalsIgnoreCase("cuboid")) {
				lastPoint = points.get(2);
				points.set(2, points.get(3));
				points.set(3, lastPoint);
			}
			for (int i = 0; i < points.size(); i++) {
				if (nb == 0) {
					firstPoint = points.get(i);
					lastPoint = firstPoint;
				} else {
					BlockVector2D point = points.get(i);// pointIterator.next();
					BlockUtils.drawLineOnTop(new Location(world, lastPoint.getX(), 0, lastPoint.getZ()), new Location(world, point.getX(), 0, point.getZ()), Material.FENCE, listBlock);
					// sendComments(player, ChatFormater.format("Point %f,%f",
					// point.getX(), point.getZ()));
					lastPoint = point;
				}
				nb++;
			}
			// sendComments(player, ChatFormater.format("Point %f,%f",
			// firstPoint.getX(), firstPoint.getZ()));
			BlockUtils.drawLineOnTop(new Location(world, lastPoint.getX(), 0, lastPoint.getZ()), new Location(world, firstPoint.getX(), 0, firstPoint.getZ()), Material.FENCE, listBlock);
			sendComments(player, "Parcel shown");
		}
	}

	/**
	 * Hide parcel.
	 * 
	 * @param player
	 *            the player
	 * @param parcelName
	 *            the parcel name
	 */
	public void hideParcel(Player player, String parcelName) {
		Parcel parcel = Parcel.getParcel(player.getWorld().getName(), parcelName);
		if (parcel == null) {
			sendComments(player, "Parcel not found");
			return;
		}
		if (!(parcel.getPlayerOwner().equalsIgnoreCase(player.getName()) || vaultPermission.playerHas(player, "localPlan.members.allow"))) {
			sendComments(player, "You don't have right on that Parcel");
			return;
		}
		if (!previewBlocks.containsKey(player.getWorld().getName() + "::" + parcel.getRegionId())) {
			sendComments(player, "Parcel preview already shown");
			return;
		}
		World world = getServer().getWorld(parcel.getWorld());
		List<Block> listBlock = previewBlocks.get(player.getWorld().getName() + "::" + parcel.getRegionId());
		for (Iterator<Block> pointIterator = listBlock.iterator(); pointIterator.hasNext();) {
			Block block = pointIterator.next();
			world.getBlockAt(block.getLocation()).setTypeId(0);
		}

		previewBlocks.remove(player.getWorld().getName() + "::" + parcel.getRegionId());
		sendComments(player, "Parcel hided");
	}

	/**
	 * Gets the parcel.
	 * 
	 * @param region
	 *            the region
	 * @return the parcel
	 */
	public Parcel getParcel(String worldId, String parcelName) {
		return Parcel.getParcel(worldId, parcelName);
	}

	@QDCommand(aliases = "commentsOn")
	public void cmd_commentsOn(CommandSender sender, Command command, String label, String[] args) {
		setComments((Player) sender, true);
	}

	@QDCommand(aliases = "commentsOff")
	public void cmd_commentsOff(CommandSender sender, Command command, String label, String[] args) {
		setComments((Player) sender, false);
	}

	@QDCommand(aliases = "list")
	public void cmd_list(CommandSender sender, Command command, String label, String[] args) {
		this.listParcels((Player) sender, args.length == 1 ? ((Player) sender).getName() : args[1], Parcel.parcelStatus.ANY);
	}

	@QDCommand(aliases = "listall")
	public void cmd_listall(CommandSender sender, Command command, String label, String[] args) {
		this.listParcels((Player) sender, "__all__", Parcel.parcelStatus.ANY);
	}

	@QDCommand(aliases = "listavailable")
	public void cmd_listavailable(CommandSender sender, Command command, String label, String[] args) {
		this.listParcels((Player) sender, "__all__", Parcel.parcelStatus.FREE);
	}

	@QDCommand(aliases = "listbuyable")
	public void cmd_listbuyable(CommandSender sender, Command command, String label, String[] args) {
		this.listParcels((Player) sender, "__all__", Parcel.parcelStatus.OWNED_BUYABLE);
	}

	@QDCommand(aliases = "buyable")
	public void cmd_buyable(CommandSender sender, Command command, String label, String[] args) {
		this.setBuyable((Player) sender, args[1], args[2]);
	}

	@QDCommand(aliases = "unbuyable")
	public void cmd_unbuyable(CommandSender sender, Command command, String label, String[] args) {
		this.setUnbuyable((Player) sender, args[1]);
	}

	@QDCommand(aliases = "buy")
	public void cmd_buy(CommandSender sender, Command command, String label, String[] args) {
		this.buyParcel((Player) sender, args[1]);
	}

	@QDCommand(aliases = "tp")
	public void cmd_tp(CommandSender sender, Command command, String label, String[] args) {
		this.teleportToParcel((Player) sender, args[1]);
	}

	@QDCommand(aliases = "create")
	public void cmd_define(CommandSender sender, Command command, String label, String[] args) {
		this.createParcel((Player) sender, args[1]);
	}

	@QDCommand(aliases = "alllocate")
	public void cmd_allocate(CommandSender sender, Command command, String label, String[] args) {
		this.allocateParcel((Player) sender, ((Player) sender).getWorld().getName(), args[1], args[2]);
	}

	@QDCommand(aliases = "member")
	public void cmd_member(CommandSender sender, Command command, String label, String[] args) {
		this.manageParcelMember((Player) sender, args);
	}

	@QDCommand(aliases = "show")
	public void cmd_show(CommandSender sender, Command command, String label, String[] args) {
		this.showParcel((Player) sender, args[1]);
	}

	@QDCommand(aliases = "hide")
	public void cmd_hide(CommandSender sender, Command command, String label, String[] args) {
		this.hideParcel((Player) sender, args[1]);
	}

	@QDCommand(aliases = "scan")
	public void cmd_scan(CommandSender sender, Command command, String label, String[] args) {
		this.initalizeRegions();
	}
}