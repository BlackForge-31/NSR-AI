package com.nsr.ai.api;

import java.util.UUID;

public class PetDataSnapshot {
    private final UUID owner;
    private final String data; // Placeholder for actual pet data

    public PetDataSnapshot(UUID owner, String data) {
        this.owner = owner;
        this.data = data;
    }

    public UUID getOwner() { return owner; }
    public String getData() { return data; }
}
