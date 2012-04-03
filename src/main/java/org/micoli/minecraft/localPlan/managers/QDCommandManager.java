package org.micoli.minecraft.localPlan.managers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.micoli.minecraft.localPlan.LocalPlan;
import org.micoli.minecraft.localPlan.entities.Parcel;
import org.micoli.minecraft.utils.ChatFormater;

/**
 * The Class QDCommandManager.
 */
public final class QDCommandManager implements CommandExecutor {

	/** The plugin. */
	private LocalPlan plugin;

	/**
	 * Instantiates a new qD command manager.
	 * 
	 * @param plugin
	 *            the plugin
	 */
	public QDCommandManager(LocalPlan plugin) {
		this.plugin = plugin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender
	 * , org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		final String cmd_commentsOn = "commentsOn";
		final String cmd_commentsOff = "commentsOff";
		final String cmd_list = "list";
		final String cmd_listall = "listall";
		final String cmd_listavailable = "listavailable";
		final String cmd_listbuyable = "listbuyable";
		final String cmd_buyable = "buyable";
		final String cmd_unbuyable = "unbuyable";
		final String cmd_buy = "buy";
		final String cmd_tp = "tp";
		final String cmd_define = "define";
		final String cmd_allocate = "allocate";
		final String cmd_member = "member";
		final String cmd_show = "show";
		final String cmd_hide = "hide";
		
		try {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (command.getName().equalsIgnoreCase(LocalPlan.getCommandString()))
					if (args.length > 0) {
						LocalPlan.log("Command " + args[1]);
						if (args[0].equalsIgnoreCase(cmd_commentsOn)) {
							LocalPlan.setComments(player, true);
						} else if (args[0].equalsIgnoreCase(cmd_commentsOff)) {
							LocalPlan.setComments(player, false);
						} else if (args[0].equalsIgnoreCase(cmd_list)) {
							plugin.listParcels(player, args.length == 1 ? player.getName() : args[1], Parcel.parcelStatus.ANY);
						} else if (args[0].equalsIgnoreCase(cmd_listall)) {
							plugin.listParcels(player, "__all__", Parcel.parcelStatus.ANY);
						} else if (args[0].equalsIgnoreCase(cmd_listavailable)) {
							plugin.listParcels(player, "__all__", Parcel.parcelStatus.FREE);
						} else if (args[0].equalsIgnoreCase(cmd_listbuyable)) {
							plugin.listParcels(player, "__all__", Parcel.parcelStatus.OWNED_BUYABLE);
						} else if (args[0].equalsIgnoreCase(cmd_buyable)) {
							plugin.setBuyable(player, args[1], args[2]);
						} else if (args[0].equalsIgnoreCase(cmd_unbuyable)) {
							plugin.setUnbuyable(player, args[1]);
						} else if (args[0].equalsIgnoreCase(cmd_buy)) {
							plugin.buyParcel(player, args[1]);
						} else if (args[0].equalsIgnoreCase(cmd_tp)) {
							plugin.teleportToParcel(player, args[1]);
						} else if (args[0].equalsIgnoreCase(cmd_define)) {
							plugin.createParcel(player, args[1]);
						} else if (args[0].equalsIgnoreCase(cmd_allocate)) {
							plugin.allocateParcel(player, args[1], args[2]);
						} else if (args[0].equalsIgnoreCase(cmd_member)) {
							plugin.manageParcelMember(player, args);
						} else if (args[0].equalsIgnoreCase(cmd_show)) {
							plugin.showParcel(player, args[1]);
						} else if (args[0].equalsIgnoreCase(cmd_hide)) {
							plugin.hideParcel(player, args[1]);
						} else {
							player.sendMessage(ChatFormater.format("{ChatColor.RED} command unknown"));
						}
					} else {
						player.sendMessage(ChatFormater.format("{ChatColor.RED} Need more arguments"));
					}
			} else {
				LocalPlan.log(ChatFormater.format("[RealEstate] requires you to be a Player"));
			}
			return false;
		} catch (Exception ex) {
			LocalPlan.log(ChatFormater.format("[RealEstate] Command failure: %s", ex.getMessage()));
			ex.printStackTrace();
		}

		return false;
	}
}