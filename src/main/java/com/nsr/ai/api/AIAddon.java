package com.nsr.ai.api;

/**
 * This interface must be implemented by all NSR-AI addons.
 */
public interface AIAddon {

    /**
     * Called when the addon is enabled.
     */
    void onEnable();

    /**
     * Called when the addon is disabled.
     */
    void onDisable();

}
