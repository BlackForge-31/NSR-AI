package com.nsr.ai.api;

import org.bukkit.entity.Player;
import java.util.Map;

/**
 * This interface must be implemented by all NSR-AI addons.
 * Addons should register themselves with {@link NSRAI#registerAddon(AIAddon)} during their plugin's onEnable.
 */
public interface AIAddon {

    /**
     * Called when a command is executed that is not handled by the main plugin.
     * @param player The player who executed the command.
     * @param args The arguments of the command.
     * @return A message to be sent to the player, or null if the command is not handled by this addon.
     */
    String onCommand(Player player, String[] args);

    /**
     * Gets a map of commands provided by the addon.
     * The key is the command name, and the value is a description of the command.
     * @return A map of commands.
     */
    Map<String, String> getCommands();

    /**
     * Gets a map of features provided by the addon.
     * The key is the feature name, and the value is a description of the feature.
     * @return A map of features.
     */
    Map<String, String> getFeatures();
}
