package org.micoli.minecraft.localPlan;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dynmap.DynmapCommonAPI;
import org.micoli.minecraft.bukkit.QDBukkitPlugin;
import org.micoli.minecraft.bukkit.QDCommand;
import org.micoli.minecraft.bukkit.QDCommand.SenderType;
import org.micoli.minecraft.bukkit.QDCommandException;
import org.micoli.minecraft.bukkit.QDCommandUsageException;
import org.micoli.minecraft.localPlan.entities.Parcel;
import org.micoli.minecraft.localPlan.entities.Parcel.buyStatusTypes;
import org.micoli.minecraft.localPlan.entities.Parcel.ownerTypes;
import org.micoli.minecraft.localPlan.managers.InterestPointManager;
import org.micoli.minecraft.localPlan.managers.LocalPlanCommandManager;
import org.micoli.minecraft.localPlan.managers.ParcelExporter;
import org.micoli.minecraft.localPlan.managers.ParcelManager;
import org.micoli.minecraft.localPlan.managers.PreviewBlockManager;
import org.micoli.minecraft.utils.PluginEnvironment;
import org.micoli.minecraft.utils.Task;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

/**
 * The Class LocalPlan.
 */
public class LocalPlan extends QDBukkitPlugin implements ActionListener {

	/** The my executor. */
	private LocalPlanCommandManager executor;

	/** The instance. */
	private static LocalPlan instance;

	/** The worldguard plugin. */
	private WorldGuardPlugin worldGuardPlugin;

	/** The worldedit plugin. */
	private WorldEditPlugin worldEditPlugin;

	/** The dynmap plugin. */
	private DynmapCommonAPI dynmapPlugin;

	/** The marker default price. */
	private double markerDefaultPrice = 300;

	/** The marker maximum distance. */
	private double markerMaximumDistance = 1000;

	/** The markerset name for POI. */
	private String markersetName = "LocalPlanPOI";

	private String parcelExporterPath = "parcelImages";

	/** The interest point manager. */
	private InterestPointManager interestPointManager;
	
	/** The parcel manager. */
	private ParcelManager parcelManager;
	
	/** The preview block manager. */
	private PreviewBlockManager previewBlockManager;
	
	/** The parcel exporter. */
	private ParcelExporter parcelExporter;

	/**
	 * Gets the single instance of LocalPlan.
	 * 
	 * @return the instance
	 */
	public static LocalPlan getInstance() {
		return instance;
	}

	/**
	 * On enable.
	 *
	 * @see org.micoli.minecraft.bukkit.QDBukkitPlugin#onEnable()
	 */
	@Override
	public void onEnable() {
		instance = this;
		withDatabase = true;

		commandString = "lp";
		super.onEnable();
		logger.log("%s version enabled", this.pdfFile.getName(), this.pdfFile.getVersion());

		worldGuardPlugin = PluginEnvironment.getWorldGuard(instance, getServer());
		worldEditPlugin = PluginEnvironment.getWorldEdit(instance, getServer());

		setDynmapPlugin((DynmapCommonAPI) getServer().getPluginManager().getPlugin("dynmap"));

		configFile.set("PointOfInterest.defaultPrice", configFile.getDouble("PointOfInterest.defaultPrice", getMarkerDefaultPrice()));
		setMarkerDefaultPrice(configFile.getDouble("PointOfInterest.defaultPrice"));

		configFile.set("PointOfInterest.markerMaximumDistance", configFile.getDouble("PointOfInterest.markerMaximumDistance", getMarkerMaximumDistance()));
		setMarkerMaximumDistance(configFile.getDouble("PointOfInterest.markerMaximumDistance"));

		configFile.set("PointOfInterest.markersetName", configFile.getString("PointOfInterest.markersetName", getMarkersetName()));
		setMarkersetName(configFile.getString("PointOfInterest.markersetName"));

		configFile.set("ParcelExporter.imagePath", configFile.getString("ParcelExporter.imagePath", getParcelExporterPath()));
		setParcelExporterPath(configFile.getString("ParcelExporter.imagePath"));

		saveConfig();

		interestPointManager = new InterestPointManager(instance);
		parcelManager = new ParcelManager(instance);
		previewBlockManager = new PreviewBlockManager(instance);
		parcelExporter = new ParcelExporter(instance);
		
		if (interestPointManager.initialize()) {
			getParcelManager().initalizeRegions();
			Task runningTask = new Task(this, this) {
				public void run() {
					parcelExporter.getMaps();
				}
			};
			runningTask.startDelayed(25L);
			
		}

		executor = new LocalPlanCommandManager(this, new Class[] { getClass() });
	}

	/*
	 * 
	 * @see org.micoli.minecraft.bukkit.QDBukkitPlugin#getDatabaseORMClasses()
	 */
	/* (non-Javadoc)
	 * @see org.micoli.minecraft.bukkit.QDBukkitPlugin#getDatabaseORMClasses()
	 */
	protected java.util.List<Class<?>> getDatabaseORMClasses() {
		List<Class<?>> list = new ArrayList<Class<?>>();
		list.add(Parcel.class);
		return list;
	};

	/**
	 * Gets the marker default price.
	 *
	 * @return the markerDefaultPrice
	 */
	public double getMarkerDefaultPrice() {
		return markerDefaultPrice;
	}

	/**
	 * Sets the marker default price.
	 *
	 * @param markerDefaultPrice the markerDefaultPrice to set
	 */
	public void setMarkerDefaultPrice(double markerDefaultPrice) {
		this.markerDefaultPrice = markerDefaultPrice;
	}

	/**
	 * Gets the marker maximum distance.
	 *
	 * @return the markerMaximumDistance
	 */
	public double getMarkerMaximumDistance() {
		return markerMaximumDistance;
	}

	/**
	 * Sets the marker maximum distance.
	 *
	 * @param markerMaximumDistance the markerMaximumDistance to set
	 */
	public void setMarkerMaximumDistance(double markerMaximumDistance) {
		this.markerMaximumDistance = markerMaximumDistance;
	}

	/**
	 * Gets the markerset name.
	 *
	 * @return the markersetName
	 */
	public String getMarkersetName() {
		return markersetName;
	}

	/**
	 * Sets the markerset name.
	 *
	 * @param markersetName the markersetName to set
	 */
	public void setMarkersetName(String markersetName) {
		this.markersetName = markersetName;
	}

	/**
	 * Gets the parcel.
	 * 
	 * @param worldId
	 *            the world id
	 * @param parcelName
	 *            the parcel name
	 * @return the parcel
	 */
	public Parcel getParcel(String worldId, String parcelName) {
		return Parcel.getParcel(worldId, parcelName);
	}

	/**
	 * Gets the dynmap plugin.
	 *
	 * @return the dynmapPlugin
	 */
	public DynmapCommonAPI getDynmapPlugin() {
		return dynmapPlugin;
	}

	/**
	 * Sets the dynmap plugin.
	 *
	 * @param dynmapPlugin the dynmapPlugin to set
	 */
	public void setDynmapPlugin(DynmapCommonAPI dynmapPlugin) {
		this.dynmapPlugin = dynmapPlugin;
	}

	/**
	 * Gets the world guard plugin.
	 *
	 * @return the worldGuardPlugin
	 */
	public WorldGuardPlugin getWorldGuardPlugin() {
		return worldGuardPlugin;
	}

	/**
	 * Sets the world guard plugin.
	 *
	 * @param worldGuardPlugin the worldGuardPlugin to set
	 */
	public void setWorldGuardPlugin(WorldGuardPlugin worldGuardPlugin) {
		this.worldGuardPlugin = worldGuardPlugin;
	}

	/**
	 * Gets the world edit plugin.
	 *
	 * @return the worldEditPlugin
	 */
	public WorldEditPlugin getWorldEditPlugin() {
		return worldEditPlugin;
	}

	/**
	 * Sets the world edit plugin.
	 *
	 * @param worldEditPlugin the worldEditPlugin to set
	 */
	public void setWorldEditPlugin(WorldEditPlugin worldEditPlugin) {
		this.worldEditPlugin = worldEditPlugin;
	}

	/**
	 * Gets the interest point manager.
	 *
	 * @return the interestPointManager
	 */
	public InterestPointManager getInterestPointManager() {
		return interestPointManager;
	}

	/**
	 * Sets the interest point manager.
	 *
	 * @param interestPointManager the interestPointManager to set
	 */
	public void setInterestPointManager(InterestPointManager interestPointManager) {
		this.interestPointManager = interestPointManager;
	}

	/**
	 * Gets the parcel manager.
	 *
	 * @return the parcelManager
	 */
	public ParcelManager getParcelManager() {
		return parcelManager;
	}

	/**
	 * Sets the parcel manager.
	 *
	 * @param parcelManager the parcelManager to set
	 */
	public void setParcelManager(ParcelManager parcelManager) {
		this.parcelManager = parcelManager;
	}

	/**
	 * Gets the preview block manager.
	 *
	 * @return the previewBlockManager
	 */
	public PreviewBlockManager getPreviewBlockManager() {
		return previewBlockManager;
	}

	/**
	 * Sets the preview block manager.
	 *
	 * @param previewBlockManager the previewBlockManager to set
	 */
	public void setPreviewBlockManager(PreviewBlockManager previewBlockManager) {
		this.previewBlockManager = previewBlockManager;
	}

	/**
	 * @return the parcelExporterPath
	 */
	public String getParcelExporterPath() {
		return parcelExporterPath;
	}

	/**
	 * @param parcelExporterPath the parcelExporterPath to set
	 */
	public void setParcelExporterPath(String parcelExporterPath) {
		this.parcelExporterPath = parcelExporterPath;
	}

	/**
	 * Cmd_comments on.
	 * 
	 * @param sender
	 *            the sender
	 * @param command
	 *            the command
	 * @param label
	 *            the label
	 * @param args
	 *            the args
	 */
	@QDCommand(aliases = "commentsOn", permissions = {}, usage = "", description = "enable plugin comments")
	public void cmd_commentsOn(CommandSender sender, Command command, String label, String[] args) {
		setComments((Player) sender, true);
	}

	/**
	 * Cmd_comments off.
	 * 
	 * @param sender
	 *            the sender
	 * @param command
	 *            the command
	 * @param label
	 *            the label
	 * @param args
	 *            the args
	 */
	@QDCommand(aliases = "commentsOff", permissions = {}, usage = "", description = "disabled plugin comments")
	public void cmd_commentsOff(CommandSender sender, Command command, String label, String[] args) {
		setComments((Player) sender, false);
	}

	/**
	 * Cmd_list.
	 * 
	 * @param sender
	 *            the sender
	 * @param command
	 *            the command
	 * @param label
	 *            the label
	 * @param args
	 *            the args
	 * @throws Exception
	 *             the exception
	 */
	@QDCommand(aliases = "list", permissions = { "localplan.list" }, usage = "[<player>]", description = "list all parcel belonging to a given player, if no player given then use the current player",senderType=SenderType.BOTH)
	public void cmd_list(CommandSender sender, Command command, String label, String[] args) throws Exception {
		if (args.length == 1) {
			getParcelManager().listParcels((Player) sender, ((Player) sender).getName(), buyStatusTypes.ANY, ownerTypes.ANY);
		} else {
			if (args.length == 2) {
				getParcelManager().listParcels((Player) sender, args[1], buyStatusTypes.ANY, ownerTypes.ANY);
			} else {
				throw new QDCommandException("Too many arguments");
			}
		}
	}

	/**
	 * Cmd_listall.
	 * 
	 * @param sender
	 *            the sender
	 * @param command
	 *            the command
	 * @param label
	 *            the label
	 * @param args
	 *            the args
	 */
	@QDCommand(aliases = "listall", permissions = { "localplan.listall" }, usage = "", description = "list all parcels")
	public void cmd_listall(CommandSender sender, Command command, String label, String[] args) {
		getParcelManager().listParcels((Player) sender, "__all__", buyStatusTypes.ANY, ownerTypes.ANY);
	}

	/**
	 * Cmd_listavailable.
	 * 
	 * @param sender
	 *            the sender
	 * @param command
	 *            the command
	 * @param label
	 *            the label
	 * @param args
	 *            the args
	 */
	@QDCommand(aliases = "listavailable", permissions = { "localplan.listavailable" }, usage = "", description = "list all parcels with no owner")
	public void cmd_listavailable(CommandSender sender, Command command, String label, String[] args) {
		getParcelManager().listParcels((Player) sender, "", buyStatusTypes.ANY, ownerTypes.ANY);
	}

	/**
	 * Cmd_listbuyable.
	 * 
	 * @param sender
	 *            the sender
	 * @param command
	 *            the command
	 * @param label
	 *            the label
	 * @param args
	 *            the args
	 */
	@QDCommand(aliases = "listbuyable", permissions = { "localplan.listbuyable" }, usage = "", description = "list all buyable parcels")
	public void cmd_listbuyable(CommandSender sender, Command command, String label, String[] args) {
		getParcelManager().listParcels((Player) sender, "__all__", buyStatusTypes.BUYABLE, ownerTypes.ANY);
	}

	/**
	 * Cmd_buyable.
	 * 
	 * @param sender
	 *            the sender
	 * @param command
	 *            the command
	 * @param label
	 *            the label
	 * @param args
	 *            the args
	 * @throws Exception
	 *             the exception
	 */
	@QDCommand(aliases = "buyable", permissions = { "localplan.setbuyable" }, usage = "<parcelName> <price>", description = "put a parcel on the market, set it as buyable to the given price")
	public void cmd_buyable(CommandSender sender, Command command, String label, String[] args) throws Exception {
		getParcelManager().setBuyable((Player) sender, args[1], args[2]);
	}

	/**
	 * Cmd_unbuyable.
	 * 
	 * @param sender
	 *            the sender
	 * @param command
	 *            the command
	 * @param label
	 *            the label
	 * @param args
	 *            the args
	 * @throws Exception
	 *             the exception
	 */
	@QDCommand(aliases = "unbuyable", permissions = { "localplan.setunbuyable" }, usage = "<parcelName>", description = "set a parcel unbuyable, disallow to buy it")
	public void cmd_unbuyable(CommandSender sender, Command command, String label, String[] args) throws Exception {
		getParcelManager().setUnbuyable((Player) sender, args[1]);
	}

	/**
	 * Cmd_buy.
	 * 
	 * @param sender
	 *            the sender
	 * @param command
	 *            the command
	 * @param label
	 *            the label
	 * @param args
	 *            the args
	 * @throws Exception
	 *             the exception
	 */
	@QDCommand(aliases = "buy", permissions = { "localplan.buy" }, usage = "<parcelName>", description = "buy a parcel if it is buyable, use economy")
	public void cmd_buy(CommandSender sender, Command command, String label, String[] args) throws Exception {
		getParcelManager().buyParcel((Player) sender, args[1]);
	}

	/**
	 * Cmd_tp.
	 * 
	 * @param sender
	 *            the sender
	 * @param command
	 *            the command
	 * @param label
	 *            the label
	 * @param args
	 *            the args
	 */
	@QDCommand(aliases = "tp", permissions = { "localplan.teleport" }, usage = "<parcelName>", description = "Teleport the player to the center of the parcel")
	public void cmd_tp(CommandSender sender, Command command, String label, String[] args) {
		getParcelManager().teleportToParcel((Player) sender, args[1]);
	}

	/**
	 * Cmd_create.
	 * 
	 * @param sender
	 *            the sender
	 * @param command
	 *            the command
	 * @param label
	 *            the label
	 * @param args
	 *            the args
	 * @throws Exception
	 *             the exception
	 */
	@QDCommand(aliases = "create", permissions = { "localplan.create" }, usage = "<parcelName>", description = "define a region and the parcel affected to it, no owner attributed to STATE")
	public void cmd_create(CommandSender sender, Command command, String label, String[] args) throws Exception {
		getParcelManager().createParcel((Player) sender, args[1]);
	}

	/**
	 * Cmd_allocate.
	 * 
	 * @param sender
	 *            the sender
	 * @param command
	 *            the command
	 * @param label
	 *            the label
	 * @param args
	 *            the args
	 * @throws QDCommandException
	 *             the qD command exception
	 */
	@QDCommand(aliases = "allocate", permissions = { "localplan.allocate" }, usage = "<parcelName> <newOwner>", description = "allocate a parcel to an owner without economy")
	public void cmd_allocate(CommandSender sender, Command command, String label, String[] args) throws QDCommandException {
		if (args.length != 3) {
			throw new QDCommandUsageException("need 3 arguments");
		}
		getParcelManager().allocateParcel((Player) sender, ((Player) sender).getWorld().getName(), args[1], args[2]);
	}

	/**
	 * Cmd_member.
	 * 
	 * @param sender
	 *            the sender
	 * @param command
	 *            the command
	 * @param label
	 *            the label
	 * @param args
	 *            the args
	 * @throws Exception
	 *             the exception
	 */
	@QDCommand(aliases = "member", permissions = { "localplan.member.set" }, usage = "<memberName>", description = "change the member of a parcel")
	public void cmd_member(CommandSender sender, Command command, String label, String[] args) throws Exception {
		getParcelManager().manageParcelMember((Player) sender, args);
	}

	/**
	 * Cmd_show.
	 * 
	 * @param sender
	 *            the sender
	 * @param command
	 *            the command
	 * @param label
	 *            the label
	 * @param args
	 *            the args
	 * @throws Exception
	 *             the exception
	 */
	@QDCommand(aliases = "show", permissions = { "localplan.show" }, usage = "<parcelName>", description = "show the border of a parcel in fence")
	public void cmd_show(CommandSender sender, Command command, String label, String[] args) throws Exception {
		getPreviewBlockManager().showParcel((Player) sender, args[1]);
	}

	/**
	 * Cmd_hide.
	 * 
	 * @param sender
	 *            the sender
	 * @param command
	 *            the command
	 * @param label
	 *            the label
	 * @param args
	 *            the args
	 * @throws Exception
	 *             the exception
	 */
	@QDCommand(aliases = "hide", permissions = { "localplan.hide" }, usage = "<parcelName>", description = "hide the border of a parcel")
	public void cmd_hide(CommandSender sender, Command command, String label, String[] args) throws Exception {
		getPreviewBlockManager().hideParcel((Player) sender, args[1]);
	}

	/**
	 * Cmd_scan.
	 * 
	 * @param sender
	 *            the sender
	 * @param command
	 *            the command
	 * @param label
	 *            the label
	 * @param args
	 *            the args
	 */
	@QDCommand(aliases = "scan", permissions = { "localplan.scan" }, usage = "", description = "rescan region and add parcels")
	public void cmd_scan(CommandSender sender, Command command, String label, String[] args) {
		if (getInterestPointManager().initialize()) {
			getParcelManager().initalizeRegions();
		}
	}

	/**
	 * Cmd_POI.
	 *
	 * @param sender the sender
	 * @param command the command
	 * @param label the label
	 * @param args the args
	 * @throws Exception the exception
	 */
	@QDCommand(aliases = "poi", permissions = { "localplan.poi" }, usage = "<POIname> <icon> <price>", description = "manage Point of Interest")
	public void cmd_POI(CommandSender sender, Command command, String label, String[] args) throws Exception {
		if (args.length != 4) {
			throw new QDCommandUsageException("need 3 arguments");
		}
		getInterestPointManager().addPOI((Player) sender, args[1], args[2], args[3]);
	}
}