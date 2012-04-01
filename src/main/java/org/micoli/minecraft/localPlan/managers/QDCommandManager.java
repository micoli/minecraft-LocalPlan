package org.micoli.minecraft.localPlan.managers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.micoli.minecraft.localPlan.LocalPlan;
import org.micoli.minecraft.localPlan.entities.Parcel;
import org.micoli.minecraft.utils.ChatFormater;

public final class QDCommandManager implements CommandExecutor {
	private LocalPlan plugin;

	public QDCommandManager(LocalPlan plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		try {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (command.getName().equalsIgnoreCase(LocalPlan.getCommandString()))
					if (args.length > 0) {
						LocalPlan.log("[RealEstate] Command " + args[0]);
						if (args[0].equalsIgnoreCase("commentsOn")) {
							LocalPlan.setComments(player, true);
						} else if (args[0].equalsIgnoreCase("commentsOff")) {
							LocalPlan.setComments(player, false);
						} else if (args[0].equalsIgnoreCase("list")) {
							plugin.listParcels(player,args.length==1?player.getName():args[1],Parcel.parcelStatus.ANY);
						} else if (args[0].equalsIgnoreCase("listall")) {
							plugin.listParcels(player,"__all__",Parcel.parcelStatus.ANY);
						} else if (args[0].equalsIgnoreCase("listavailable")) {
							plugin.listParcels(player,"__all__",Parcel.parcelStatus.FREE);
						} else if (args[0].equalsIgnoreCase("listbuyable")) {
							plugin.listParcels(player,"__all__",Parcel.parcelStatus.OWNED_BUYABLE);
						} else if (args[0].equalsIgnoreCase("buyable")) {
							plugin.setBuyable(player,args[1],args[2]);
						} else if (args[0].equalsIgnoreCase("unbuyable")) {
							plugin.setUnbuyable(player,args[1]);
						} else if (args[0].equalsIgnoreCase("buy")) {
							plugin.buyParcel(player,args[1]);
						} else if (args[0].equalsIgnoreCase("tp")) {
							plugin.teleportToParcel(player,args[1]);
						} else if (args[0].equalsIgnoreCase("define")) {
							plugin.createParcel(player,args[1]);
						} else if (args[0].equalsIgnoreCase("allocate")) {
							plugin.allocateParcel(player,args[1],args[2]);
						} else if (args[0].equalsIgnoreCase("member")) {
							plugin.manageParcelMember(player,args);
						} else if (args[0].equalsIgnoreCase("show")) {
							plugin.showParcel(player,args[1]);
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