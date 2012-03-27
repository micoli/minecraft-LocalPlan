package org.micoli.minecraft.realEstate;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.micoli.minecraft.realEstate.entities.QDObjectRealEstate;
import org.micoli.minecraft.realEstate.listeners.QDBlockListener;
import org.micoli.minecraft.realEstate.listeners.QDPlayerListener;
import org.micoli.minecraft.realEstate.managers.QDCommandManager;
import org.micoli.minecraft.utils.ChatFormater;
import org.micoli.minecraft.utils.ServerLogger;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.BlockVector;
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
import com.sk89q.worldguard.protection.databases.RegionDBUtil;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class RealEstate extends JavaPlugin implements ActionListener {
	private static Logger logger = Logger.getLogger("Minecraft");
	private QDCommandManager myExecutor;
	private static RealEstate instance;
	public static Map<String, QDObjectRealEstate> aRealEstate;
	private static String commandString = "realEstate";
	private static boolean comments = true;
	private static String lastMsg = "";

	public static Permission vaultPermission = null;
	public static Economy vaultEconomy = null;
	public static Chat vaultChat = null;
	private WorldGuardPlugin wg;
	private WorldEditPlugin we;

	/**
	 * @return the instance
	 */
	public static RealEstate getInstance() {
		return instance;
	}

	public static String getCommandString() {
		return commandString;
	}

	public static void setComments(Player player, boolean active) {
		comments = active;
		player.sendMessage(ChatFormater.format("{ChatColor.RED} %s", (active ? "comments activated" : "comments desactived")));
	}

	public static boolean getComments() {
		return comments;
	}

	public static void log(String str) {
		logger.info(str);
	}

	public static void sendComments(Player player, String text, boolean global) {
		if (getComments()) {
			if (!RealEstate.lastMsg.equalsIgnoreCase(text)) {
				RealEstate.lastMsg = text + "";
				if (global) {
					getInstance().getServer().broadcastMessage(text);
				} else {
					player.sendMessage(text);
				}
			}
		}
	}

	public void onDisable() {
		PluginDescriptionFile pdfFile = getDescription();
		log(ChatFormater.format("%s version disabled", pdfFile.getName(), pdfFile.getVersion()));
	}

	@Override
	public void onEnable() {
		aRealEstate = new HashMap<String, QDObjectRealEstate>();
		instance = this;
		myExecutor = new QDCommandManager(this);
		PluginManager pm = getServer().getPluginManager();
		PluginDescriptionFile pdfFile = getDescription();

		ServerLogger.setPrefix(pdfFile.getName());
		pm.registerEvents(new QDPlayerListener(this), this);
		pm.registerEvents(new QDBlockListener(this), this);
		getCommand(getCommandString()).setExecutor(myExecutor);
		setupPermissions();
		setupChat();
		setupEconomy();
		wg = getWorldGuard();
		we = getWorldEdit();

		log(ChatFormater.format("%s version enabled", pdfFile.getName(), pdfFile.getVersion()));
		listRegions();
	}

	private WorldGuardPlugin getWorldGuard() {
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

		// WorldGuard may not be loaded
		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
			return null; // Maybe you want throw an exception instead
		}

		return (WorldGuardPlugin) plugin;
	}

	private WorldEditPlugin getWorldEdit() {
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldEdit");

		// WorldGuard may not be loaded
		if (plugin == null || !(plugin instanceof WorldEdit)) {
			return null; // Maybe you want throw an exception instead
		}

		return (WorldEditPlugin) plugin;
	}

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null) {
			vaultPermission = permissionProvider.getProvider();
		}
		return (vaultPermission != null);
	}

	private boolean setupChat() {
		RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
		if (chatProvider != null) {
			vaultChat = chatProvider.getProvider();
		}

		return (vaultChat != null);
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			vaultEconomy = economyProvider.getProvider();
		}

		return (vaultEconomy != null);
	}

	public void actionPerformed(ActionEvent event) {
	}

	public void blockBreak(BlockBreakEvent event) {
	}

	public void playerMove(Player player) {
	}

	public void listRegions() {
		final int maxdepth = 10;
		for (World w : getServer().getWorlds()) {
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
				if (depth > maxdepth)
					continue;
				ServerLogger.log("%s %s", pr.getId(), pr.getOwners().toPlayersString());
			}
		}

	}

	public void getParcelAround(Player player) {
		World w = player.getWorld();
		Vector pt = toVector(player.getLocation()); // This also takes a
													// location

		ApplicableRegionSet set = wg.getRegionManager(w).getApplicableRegions(pt);

		ServerLogger.log("list %s %s", player.getName(), set.toString());
		for (ProtectedRegion reg : set) {
			ServerLogger.log("%s %s", player.getName(), reg.getId());
		}
		ServerLogger.log("--list %s", player.getName());

	}

	public void createParcel(Player player, String id) {
		try {
			World w = player.getWorld();

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

			RegionManager mgr = wg.getGlobalRegionManager().get(sel.getWorld());
			if (mgr.hasRegion(id)) {
				throw new CommandException("That region is already defined. Use redefine instead.");
			}

			ProtectedRegion region;

			// Detect the type of region from WorldEdit
			if (sel instanceof Polygonal2DSelection) {
				Polygonal2DSelection polySel = (Polygonal2DSelection) sel;
				int minY = polySel.getNativeMinimumPoint().getBlockY();
				int maxY = polySel.getNativeMaximumPoint().getBlockY();
				region = new ProtectedPolygonalRegion(id, polySel.getNativePoints(), minY, maxY);
			} else if (sel instanceof CuboidSelection) {
				BlockVector min = sel.getNativeMinimumPoint().toBlockVector();
				BlockVector max = sel.getNativeMaximumPoint().toBlockVector();
				region = new ProtectedCuboidRegion(id, min, max);
			} else {
				throw new CommandException("The type of region selected in WorldEdit is unsupported in WorldGuard!");
			}

			// Get the list of region owners
			DefaultDomain own = new DefaultDomain();
			own.addPlayer(player.getName());
			region.setOwners(own);

			mgr.addRegion(region);

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
}