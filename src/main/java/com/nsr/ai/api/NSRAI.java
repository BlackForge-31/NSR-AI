package com.nsr.ai.api;

import org.bukkit.entity.Player;

/**
 * This is a utility class with static methods for addons to interact with the NSR-AI plugin.
 * The methods in this class are empty stubs and only contain comments.
 * The actual implementation is in the closed-source NSR-AI plugin.
 */
public class NSRAI {

    /**
     * Sends a message to the AI on behalf of a player. This method is a stub and contains no logic.
     * The actual implementation is handled by the core NSR-AI plugin.
     *
     * @param player  The player sending the message.
     * @param message The message to send.
     */
    public static void sendAIMessage(Player player, String message) {
        // This method is a stub. The actual implementation is in the closed-source NSR-AI plugin.
        // Addons should call this method to allow the core plugin to process AI interactions.
    }

    /**
     * Registers an addon with NSR-AI. This method is a stub and contains no logic.
     * The actual implementation is handled by the core NSR-AI plugin.
     *
     * @param addon The addon to register. It must implement the {@link AIAddon} interface.
     */
    public static void registerAddon(AIAddon addon) {
        // This method is a stub. The actual implementation is in the closed-source NSR-AI plugin.
        // Addons should call this method during their onEnable to register themselves with NSR-AI.
    }
}