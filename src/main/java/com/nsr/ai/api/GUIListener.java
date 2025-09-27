package com.nsr.ai.api;

import org.bukkit.entity.Player;

public interface GUIListener {
    void onGUIEvent(Player player, String eventType);
}
