package com.nsr.ai.api;

/**
 * This interface must be implemented by all NSR-AI addons.
 * Addons should register themselves with {@link NSRAI#registerAddon(AIAddon)} during their plugin's onEnable.
 */
public interface AIAddon {

    /**
     * Called when the addon is enabled by the NSR-AI plugin.
     * This is typically where an addon would register its event listeners or perform other setup tasks.
     */
    void onEnable();

    /**
     * Called when the addon is disabled by the NSR-AI plugin.
     * This is typically where an addon would unregister its event listeners or perform other cleanup tasks.
     */
    void onDisable();

}