package org.micoli.minecraft.localPlan.managers;

import org.micoli.minecraft.bukkit.QDBukkitPlugin;
import org.micoli.minecraft.bukkit.QDCommandManager;

public class LocalPlanCommandManager extends QDCommandManager {

	@SuppressWarnings("rawtypes")
	public LocalPlanCommandManager(QDBukkitPlugin plugin, Class[] classes) {
		super(plugin, classes);
	}

}
