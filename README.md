# NSR-AI Open-Source API

![Version](https://img.shields.io/badge/Version-1.2.1--Beta-blue.svg)

This is the official open-source API for the NSR-AI Minecraft Plugin. It allows developers to interact with core NSR-AI functionalities in a safe and controlled manner.

## Installation (Maven)

Add the following to your `pom.xml`:

```xml

<dependencies>
    <dependency>
        <groupId>com.nsr-ai</groupId>
        <artifactId>nsr-ai-api</artifactId>
        <version>1.2.1-Beta</version> <!-- Use the current API version -->
        <scope>provided</scope>
    </dependency>
</dependencies>
```

## Features Available in API Version 2

*   **Chat System:** Send messages to AI, get AI responses (asynchronous).
*   **Pet System:** Get pet data, register pet listeners.
*   **NPC System:** Register NPC listeners, update NPC skins.
*   **Memory System:** Provides methods to access and update shared memory. (Note: This feature is currently a placeholder and will log warnings upon use).
*   **Versioning:** Get plugin version and API version.
*   **GUI System:** (Conditional) Offers functionality to open custom GUIs and register GUI listeners. Calling these methods will throw an an `IllegalStateException` if the GUI system is not enabled in the core plugin.
*   **Security System:** (Conditional) Provides methods to retrieve the current security status. Calling these methods will throw an `IllegalStateException` if the Security system is not enabled in the core plugin.

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
                    getLogger().info("Pet event for owner " 
                        + petData.getOwner() + ": " + petData.getData());
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
            NSRaiAPI.getPetData(event.getPlayer().getUniqueId())
                    .ifPresent(petData -> {
                        getLogger().info(event.getPlayer().getName() 
                            + "'s pet data: " + petData.getData());
                    });
        } catch (IllegalStateException e) {
            getLogger().warning("Could not get pet data: " + e.getMessage());
        }
    }
}
```

## What's Not Included

This API layer *does not* include any of the proprietary or closed-source features of the main NSR-AI plugin, such as:

*   NPC AI System (Spawning, Pathfinding, Interactions) – Upcoming Feature (Coming Soon)
*   Pet AI System (Taming, Behaviors, Communication) – Implemented, under testing for API compatibility
*   Advanced Memory System for AI Entities – Implemented, currently in testing phase
*   Offline AI integrations (e.g., Ollama, LLaMA, local models)
*   Direct API key expansions for external services (e.g., Gemini, OpenAI, Claude)
*   Scripted or canned response systems

These features remain part of the closed-source NSR-AI plugin.

License Restrictions

*   As per the included LICENSE.txt (MIT with Commons Clause), the following restrictions apply:
*   You may NOT create or redistribute offline AI integrations (e.g., Ollama, LLaMA, local models).
*   You may NOT add or expand API key providers (e.g., Gemini, OpenAI, Claude).
*   Addons may use the API for general purposes only (e.g., custom commands, player stats, conversation improvements).
*   However, you must not attempt to expand its scope with additional API providers, offline modes, or external services.
*   You may NOT create scripted or canned-response systems.
*   You may NOT fork, re-implement, or otherwise bypass NSR-AI’s core monetization model.
*   Only the official NSR-AI backend may be used for secure API key handling.
*   Addons must respect user privacy — they must not steal or transmit sensitive data.

## Releases

Developers should depend on specific version tags (e.g., `1.2.0`) for stability. The `main` branch may contain unreleased changes.

## Documentation

*   **Developer Guide:** For detailed information on API usage, versioning, and feature detection, please refer to [DEVELOPER.md](DEVELOPER.md).
*   **Security Policy:** For information on addon compliance, prohibited actions, and security updates, please refer to [SECURITY.md](SECURITY.md).

##full repo tree of BlackForge-31/NSR-AI:

```directory tree
NSR-AI/
├── .gitignore
├── DEVELOPER.md
├── LICENSE.txt
├── README.md
├── SECURITY.md
├── _config.yml
├── pom.xml
└── src/
    └── main/
        └── java/
            └── com/
                └── nsr/
                    └── ai/
                        └── api/
                            ├── events/
                            │   └── (event classes here)
                            ├── AIAddon.java
                            ├── AIMessage.java
                            ├── AIResponse.java
                            ├── GUIBuilder.java
                            ├── GUIListener.java
                            ├── NPCListener.java
                            ├── NSRAI.java
                            ├── NSRaiAPI.java
                            ├── PetDataSnapshot.java
                            ├── PetListener.java
                            └── SecurityStatus.java
```

## For Addon Developers

If you are developing an addon for NSR-AI, please adhere to the following critical guidelines:

*   **Installation Path:** Addon JAR files must be placed in `/plugins/NSR-AI/addons/` to be loaded correctly. You must instruct your users to do this.
*   **Command Prefixes:** All addon commands must start with `/ai` followed by the addon's specific subcommand (e.g., `/ai joke`).

For detailed rules on command structure, advanced command usage, and the addon lifecycle, please refer to the **[Developer Guide](DEVELOPER.md)**.
