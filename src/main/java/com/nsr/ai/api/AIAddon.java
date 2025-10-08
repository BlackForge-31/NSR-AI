package com.nsr.ai.api;

import org.bukkit.entity.Player;
import java.util.Map;

/**
 * This interface must be implemented by all NSR-AI addons.
 * Addons should register themselves with {@link NSRAI#registerAddon(AIAddon)} during their plugin's onEnable.
 */
public interface AIAddon {

    void onEnable(com.nsr.ai.plugin.NSRAIPlugin plugin);

    void onDisable();

    String getName();

    String getVersion();

    String getAuthor();

    String onCommand(Player player, String[] args);

    Map<String, String> getCommands();

    Map<String, String> getFeatures();
}
