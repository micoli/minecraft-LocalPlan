package org.micoli.minecraft.localPlan;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;

import java.awt.event.ActionEvent;
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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;
import org.micoli.minecraft.bukkit.QDBukkitPlugin;
import org.micoli.minecraft.localPlan.entities.Parcel;
import org.micoli.minecraft.localPlan.entities.Parcel.parcelStatus;
import org.micoli.minecraft.localPlan.managers.QDCommandManager;
import org.micoli.minecraft.utils.ChatFormater;
import org.micoli.minecraft.utils.ServerLogger;

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
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

// TODO: Auto-generated Javadoc
/**
 * The Class LocalPlan.
 */
public class LocalPlan extends QDBukkitPlugin implements ActionListener {

	/** The my executor. */
	private QDCommandManager myExecutor;

	/** The instance. */
	private static LocalPlan instance;

	/** The internal array of parcels. */
	public static Map<String, Parcel> aParcel;

	/** The wg. */
	private WorldGuardPlugin wg;

	/** The we. */
	private WorldEditPlugin we;

	/**
	 * Gets the single instance of LocalPlan.
	 * 
	 * @return the instance
	 */
	public static LocalPlan getInstance() {
		return instance;
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

		ServerLogger.log("plugins %s", plugin.toString());

		if (plugin == null || !(plugin instanceof WorldEdit)) {
			Plugin[] plugs = getServer().getPluginManager().getPlugins();
			int t = 0;
			for (t = 0; t < plugs.length; t++) {
				ServerLogger.log("plugins : [%s]", plugs[t].getName());
				if (plugs[t].getName().trim().equalsIgnoreCase("WorldEdit")) {
					ServerLogger.log("plugins found [%s]", plugs[t].getName());
					plugin = plugs[t];
					break;
				}
			}
		}
		ServerLogger.log("plugin found [%s]", plugin.getClass());

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
		myExecutor = new QDCommandManager(this);
		commandString = "re";
		getCommand(getCommandString()).setExecutor(myExecutor);

		wg = getWorldGuard();
		we = getWorldEdit();
		loadConfiguration();
		initializeDatabase();

		ServerLogger.log("Parcels list");
		Iterator<Parcel> iter = database.getDatabase().find(Parcel.class).findList().iterator();
		while (iter.hasNext()) {
			Parcel re = iter.next();
			ServerLogger.log("[%s] %s", re.getWorld(), re.getId());
		}
		ServerLogger.log("Parcels list end");
		initalizeRegions();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.micoli.minecraft.bukkit.QDBukkitPlugin#actionPerformed(java.awt.event
	 * .ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {
	}

	/**
	 * Block break.
	 * 
	 * @param event
	 *            the event
	 */
	public void blockBreak(BlockBreakEvent event) {
	}

	/**
	 * Player move.
	 * 
	 * @param player
	 *            the player
	 */
	public void playerMove(Player player) {
	}

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
		Iterator<Parcel> iter = database.getDatabase().find(Parcel.class).where().like("playerOwner", ownerArg).like("status", statusArg).orderBy("id desc").findList().iterator();

		if (iter.hasNext()) {
			sendComments(player, ChatFormater.format("List of owned parcels"));
			while (iter.hasNext()) {
				Parcel re = iter.next();
				sendComments(player, ChatFormater.format("%5s:%15s:%8s:%8s", re.getWorld(), re.getId(), re.getPlayerOwner(), re.getStatus()));
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
		for (World w : getServer().getWorlds()) {
			Map<?, Parcel> allregions = database.getDatabase().find(Parcel.class).where().like("world", w.getName()).orderBy("id asc").findMap();
			ServerLogger.log("Map %s (%d)", w.getName(), allregions.size());

			RegionManager rm = wg.getRegionManager(w);
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
					ServerLogger.log(" %s[%s]=>%s %d ccc ", w.getName(), pr.getId(), pr.getOwners().toPlayersString(), pr.volume() / 256);

					if (!allregions.containsKey(pr.getId())) {
						Parcel parcel = new Parcel();
						parcel.setWorld(w.getName());
						parcel.setId(pr.getId());
						parcel.setPlayerOwner("__state__");
						parcel.setStatus(Parcel.parcelStatus.FREE);

						database.getDatabase().save(parcel);
						ServerLogger.log("Automatically adding %s[%s]=>%s", w.getName(), pr.getId(), pr.getOwners().toPlayersString());
					} else {
						Parcel parcel = allregions.get(pr.getId());
						if (parcel.getSurface() != pr.volume() / 256) {
							parcel.setSurface(pr.volume() / 256);
							database.getDatabase().save(parcel);
							ServerLogger.log("updating surface of  %s[%s]=>%d", w.getName(), pr.getId(), parcel.getSurface());
						}
					}
				}
			}
		}

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

		ApplicableRegionSet set = wg.getRegionManager(w).getApplicableRegions(pt);

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
	public void createParcel(Player player, String id) {
		try {

			if (!ProtectedRegion.isValidId(id)) {
				throw new CommandException("Invalid region ID specified!");
			}

			if (id.equalsIgnoreCase("__global__")) {
				throw new CommandException("A region cannot be named __global__");
			}

			// Attempt to get the player's selection from WorldEdit
			Selection sel = we.getSelection(player);

			if (sel == null) {
				throw new CommandException("Select a region with WorldEdit first.");
			}

			World w = sel.getWorld();
			RegionManager mgr = wg.getGlobalRegionManager().get(w);
			if (mgr.hasRegion(id)) {
				throw new CommandException("That region is already defined. Use redefine instead.");
			}

			ProtectedRegion region;

			// Detect the type of region from WorldEdit
			if (sel instanceof Polygonal2DSelection) {
				Polygonal2DSelection polySel = (Polygonal2DSelection) sel;
				region = new ProtectedPolygonalRegion(id, polySel.getNativePoints(), 0, w.getMaxHeight());
			} else if (sel instanceof CuboidSelection) {
				BlockVector min = sel.getNativeMinimumPoint().toBlockVector();
				BlockVector max = sel.getNativeMaximumPoint().toBlockVector();
				min.setY(0);
				max.setY(w.getMaxHeight());
				region = new ProtectedCuboidRegion(id, min, max);
			} else {
				throw new CommandException("The type of region selected in WorldEdit is unsupported in WorldGuard!");
			}

			// Get the list of region owners
			DefaultDomain own = new DefaultDomain();
			own.addPlayer(player.getName());
			region.setOwners(own);
			setRegionFlag(player, region, "BUILD", "allow");

			mgr.addRegion(region);

			Parcel parcel = new Parcel();
			parcel.setId(region.getId());
			parcel.setWorld(w.getName());
			parcel.setPlayerOwner("__state__");
			parcel.setStatus(Parcel.parcelStatus.FREE);
			parcel.setSurface(region.volume() / 256);

			database.getDatabase().save(parcel);

			try {
				mgr.save();
				sendComments(player, ChatColor.YELLOW + "Region saved as " + id + ".", false);
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
		region.setFlag(flag, flag.parseInput(wg, sender, value));
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
	public void teleportToParcel(Player player, String id) {
		World w = player.getWorld();
		RegionManager rm = wg.getRegionManager(w);
		ProtectedRegion region = rm.getRegion(id);
		if (region != null) {
			final BlockVector min = region.getMinimumPoint();
			final BlockVector max = region.getMaximumPoint();
			Location dstLocation = new Location(w, (double) (min.getBlockX() + max.getBlockX()) / 2, w.getMaxHeight() - 1, (double) (min.getBlockZ() + max.getBlockZ()) / 2);
			while (w.getBlockAt(dstLocation).getType().getId() == 0) {
				dstLocation = dstLocation.subtract(0, 1, 0);
			}
			dstLocation = dstLocation.add(0, 1, 0);
			player.teleport(dstLocation);
		}
	}

	public void setMaterialOnTop(World world, BlockVector2D point, Material material) {
		Location dstLocation = new Location(world, point.getX(), 256, point.getZ());
		while (world.getBlockAt(dstLocation).getType().getId() == 0) {
			dstLocation = dstLocation.subtract(0, 1, 0);
		}
		dstLocation = dstLocation.add(0, 1, 0);
		world.getBlockAt(dstLocation).setType(material);
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
	public void allocateParcel(Player player, String parcelName, String newOwner) {
		Parcel parcel = database.getDatabase().find(Parcel.class).where().eq("id", parcelName).findUnique();
		if (parcel == null) {
			sendComments(player, "Parcel not found");
			return;
		}

		RegionManager mgr = wg.getGlobalRegionManager().get(getServer().getWorld(parcel.getWorld()));
		ProtectedRegion region = mgr.getRegion(parcel.getId());

		DefaultDomain own = new DefaultDomain();
		own.addPlayer(newOwner);
		region.setOwners(own);

		parcel.setPlayerOwner(newOwner);
		parcel.setStatus(Parcel.parcelStatus.OWNED);
		database.getDatabase().save(parcel);

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
		Parcel parcel = database.getDatabase().find(Parcel.class).where().eq("id", parcelName).eq("playerOwner", player.getName()).findUnique();
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
		database.getDatabase().save(parcel);
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
		Parcel parcel = database.getDatabase().find(Parcel.class).where().eq("id", parcelName).eq("playerOwner", player.getName()).findUnique();
		if (parcel == null) {
			sendComments(player, "Parcel not found or doesn't belong to you");
			return;
		}
		parcel.setStatus(Parcel.parcelStatus.OWNED);
		database.getDatabase().save(parcel);
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
		Parcel parcel = database.getDatabase().find(Parcel.class).where().eq("id", parcelName).findUnique();
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
		allocateParcel(player, parcelName, player.getDisplayName());
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
		Parcel parcel = database.getDatabase().find(Parcel.class).where().eq("id", parcelName).findUnique();
		if (parcel == null) {
			sendComments(player, "Parcel not found");
			return;
		}

		if (!(parcel.getPlayerOwner().equalsIgnoreCase(player.getName()) || vaultPermission.playerHas(player, "localPlan.members.allow"))) {
			sendComments(player, "You don't have right on that Parcel");
			return;
		}
		sendComments(player, "You have right on that Parcel, but nothinh is coded ^^");

	}

	public void showParcel(Player player, String parcelName) {
		Parcel parcel = database.getDatabase().find(Parcel.class).where().eq("id", parcelName).findUnique();
		if (parcel == null) {
			sendComments(player, "Parcel not found");
			return;
		}
		if (!(parcel.getPlayerOwner().equalsIgnoreCase(player.getName()) || vaultPermission.playerHas(player, "localPlan.members.allow"))){
			sendComments(player, "You don't have right on that Parcel");
			return;
		}
		RegionManager mgr = wg.getGlobalRegionManager().get(getServer().getWorld(parcel.getWorld()));
		ProtectedRegion region = mgr.getRegion(parcel.getId());
		
		Iterator<BlockVector2D> pointIterator = region.getPoints().iterator();
		while(pointIterator.hasNext()){
			BlockVector2D point = pointIterator.next();
			setMaterialOnTop(getServer().getWorld(parcel.getWorld()),point,Material.FENCE);
		}
	}
}