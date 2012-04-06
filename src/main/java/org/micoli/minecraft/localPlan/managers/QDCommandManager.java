package org.micoli.minecraft.localPlan.managers;

import java.lang.reflect.Method;
import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.micoli.minecraft.bukkit.QDBukkitPlugin;
import org.micoli.minecraft.bukkit.QDCommand;
import org.micoli.minecraft.localPlan.LocalPlan;
import org.micoli.minecraft.utils.ChatFormater;
import org.micoli.minecraft.utils.ServerLogger;

/**
 * The Class QDCommandManager.
 */
public final class QDCommandManager implements CommandExecutor {

	/** The plugin. */
	private LocalPlan plugin;
	private HashMap<String,Method> listAliases = new HashMap<String,Method>();

	/**
	 * Instantiates a new qD command manager.
	 * 
	 * @param plugin
	 *            the plugin
	 */
	@SuppressWarnings("rawtypes")
	public QDCommandManager(LocalPlan plugin,Class[] classes) {
		this.plugin = plugin;
		ServerLogger.log("----------------------------");
		for (Class classe : classes) {
			for (Method method : classe.getMethods()) {
				if (method.isAnnotationPresent(QDCommand.class)) {
					ServerLogger.log("Method : " + method.getName());
					QDCommand annotation = method.getAnnotation(QDCommand.class);
					listAliases.put(annotation.aliases().toLowerCase(),method);
					ServerLogger.log("Aliases-> : " + annotation.aliases());
					ServerLogger.log("Help-> : " + annotation.help());
					ServerLogger.log("Description-> : " + annotation.description());
				}
			}
		}
		plugin.getCommand(QDBukkitPlugin.getCommandString()).setExecutor(this);
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
		try {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (command.getName().equalsIgnoreCase(LocalPlan.getCommandString())){
					if (args.length > 0) {
						LocalPlan.log("Command " + args[0]);
						if(listAliases.containsKey(args[0].toLowerCase())){
							listAliases.get(args[0].toLowerCase()).invoke(plugin, sender, command,label,args);
						} else {
							player.sendMessage(ChatFormater.format("{ChatColor.RED} command unknown"));
						}
					} else {
						player.sendMessage(ChatFormater.format("{ChatColor.RED} Need more arguments"));
					}
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
