# NSR-AI Open-Source API

This is the official open-source API for the NSR-AI Minecraft Plugin. It allows developers to interact with core NSR-AI functionalities in a safe and controlled manner.

## Installation (Maven)

Add the following to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>nsr-ai-repo</id>
        <url>https://repo.yourdomain.com/nsr-ai/</url> <!-- Replace with your actual repository URL -->
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.nsr.ai</groupId>
        <artifactId>nsr-ai-api</artifactId>
        <version>1.2</version> <!-- Use the current API version -->
        <scope>provided</scope>
    </dependency>
</dependencies>
```

## Features Available in API Version 2

*   **Chat System:** Send messages to AI, get AI responses (asynchronous).
*   **Pet System:** Get pet data, register pet listeners.
*   **NPC System:** Register NPC listeners, update NPC skins.
*   **Memory System:** Access and update shared memory (placeholder, currently logs warnings).
*   **Versioning:** Get plugin version and API version.
*   **GUI System:** (Conditional) Open custom GUIs, register GUI listeners. Throws `IllegalStateException` if not supported by the core plugin.
*   **Security System:** (Conditional) Get security status. Throws `IllegalStateException` if not supported by the core plugin.

## Example: Safe Event Listener

This example demonstrates how to register a pet listener and handle pet events safely.

```java
import com.nsr.ai.api.NSRaiAPI;
import com.nsr.ai.api.PetDataSnapshot;
import com.nsr.ai.api.PetListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MyAddonPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        
        // Register a pet listener
        try {
            NSRaiAPI.registerPetListener(new PetListener() {
                @Override
                public void onPetEvent(PetDataSnapshot petData) {
                    getLogger().info("Pet event for owner " + petData.getOwner() + ": " + petData.getData());
                }
            });
            getLogger().info("Pet listener registered successfully.");
        } catch (IllegalStateException e) {
            getLogger().warning("Could not register pet listener: " + e.getMessage());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Example: Get pet data for a joining player
        try {
            NSRaiAPI.getPetData(event.getPlayer().getUniqueId()).ifPresent(petData -> {
                getLogger().info(event.getPlayer().getName() + "'s pet data: " + petData.getData());
            });
        } catch (IllegalStateException e) {
            getLogger().warning("Could not get pet data: " + e.getMessage());
        }
    }
}
```

## What's Not Included

This API layer *does not* include any of the proprietary or closed-source features of the main NSR-AI plugin, such as:

*   NPC AI system (spawning, pathfinding, interactions)
*   Pet AI system (taming, behaviors, communication)
*   Advanced memory system for AI entities
*   Offline AI integrations (e.g., Ollama, LLaMA, local models)
*   Direct API key expansions for external services (e.g., Gemini, OpenAI, Claude)
*   Scripted or canned response systems

These features remain part of the closed-source NSR-AI plugin.

## **License Restrictions**

As per the included `LICENSE.txt` (MIT with Commons Clause), the following restrictions apply:

*   You may NOT create or redistribute offline AI integrations.
*   You may NOT add API key expansions.
*   You may NOT create scripted response systems.
*   You may NOT fork or re-implement the core functionality to bypass NSR-AI monetization.
*   Only the official NSR-AI backend can be used for secure API key handling.

## Releases

Developers should depend on specific version tags (e.g., `1.2.0`) for stability. The `main` branch may contain unreleased changes.

## Documentation

*   **Developer Guide:** For detailed information on API usage, versioning, and feature detection, please refer to [DEVELOPER.md](DEVELOPER.md).
*   **Security Policy:** For information on addon compliance, prohibited actions, and security updates, please refer to [SECURITY.md](SECURITY.md).