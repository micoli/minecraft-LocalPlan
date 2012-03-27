package org.micoli.minecraft.realEstate.managers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.micoli.minecraft.realEstate.RealEstate;
import org.micoli.minecraft.utils.ChatFormater;

public final class QDCommandManager implements CommandExecutor {
	private RealEstate plugin;

	public QDCommandManager(RealEstate plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		try {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (command.getName().equalsIgnoreCase(RealEstate.getCommandString()))
					if (args.length > 0) {
						RealEstate.log("[RealEstate] Command " + args[0]);
						if (args[0].equalsIgnoreCase("commentsOn")) {
							RealEstate.setComments(player, true);
						} else if (args[0].equalsIgnoreCase("commentsOff")) {
							RealEstate.setComments(player, false);
						} else if (args[0].equalsIgnoreCase("list")) {
							plugin.listRegions();
						} else if (args[0].equalsIgnoreCase("create")) {
							plugin.createParcel(player,args[1]);
						} else {
							player.sendMessage(ChatFormater.format("{ChatColor.RED} command unknown"));
						}
					} else {
						player.sendMessage(ChatFormater.format("{ChatColor.RED} Need more arguments"));
					}
			} else {
				RealEstate.log(ChatFormater.format("[RealEstate] requires you to be a Player"));
			}
			return false;
		} catch (Exception ex) {
			RealEstate.log(ChatFormater.format("[RealEstate] Command failure: %s", ex.getMessage()));
		}

		return false;
	}
}