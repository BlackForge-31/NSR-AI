package com.nsr.ai.api;

import org.bukkit.entity.Player;

public interface NPCListener {
    void onNPCInteract(Player player, String npcName);
}
