package org.micoli.minecraft.localPlan.managers;

import org.micoli.minecraft.bukkit.QDBukkitPlugin;
import org.micoli.minecraft.bukkit.QDCommandManager;

/**
 * The Class LocalPlanCommandManager.
 */
public class LocalPlanCommandManager extends QDCommandManager {

	/**
	 * Instantiates a new local plan command manager.
	 *
	 * @param plugin the plugin
	 * @param classes the classes
	 */
	@SuppressWarnings("rawtypes")
	public LocalPlanCommandManager(QDBukkitPlugin plugin, Class[] classes) {
		super(plugin, classes);
	}

}
