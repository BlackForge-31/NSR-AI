# NSR-AI

This repository provides the API dependency layer for the NSR-AI Minecraft plugin. It allows developers to create addons that can interact with and extend the functionality of NSR-AI without including the proprietary core logic.

## Maven Dependency

To use this API in your project, add the following to your `pom.xml`:

```xml
<dependency>
    <groupId>com.nsr.ai</groupId>
    <artifactId>nsr-ai</artifactId>
    <version>1.2.0</version> <!-- Depend on the specific version tag you need -->
    <scope>provided</scope>
</dependency>
```

## Example: Listening to AIChatEvent

Developers can listen to custom events fired by NSR-AI to interact with its features. Here's an example of how to listen for the `AIChatEvent`:

```java
import com.nsr.ai.api.events.AIChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class MyAddon extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Register this class as an event listener
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("MyAddon has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MyAddon has been disabled!");
    }

    @EventHandler
    public void onAIChat(AIChatEvent event) {
        // Get the player who initiated the chat with the AI
        // Player player = event.getPlayer();

        // Get the original message sent by the player to the AI
        // String message = event.getMessage();

        // Get the AI's current response. This can be modified.
        String currentResponse = event.getResponse();

        // Example: Modify the AI's response by adding a custom prefix and emoji
        event.setResponse("✨ [Addon] " + currentResponse + " 😊");

        // You can also cancel the event if you want to prevent the AI's response from being sent
        // event.setCancelled(true);
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

---

**Security Information:**

For security reasons, we want to assure you that we do not control your server; your full power and control over the plugin remain unchanged. Security measures are implemented for addons or dependent plugins that might contain malware, suspicious code, or violate our terms. Such addons can be detached through the security system.
