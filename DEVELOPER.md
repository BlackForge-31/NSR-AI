# NSR-AI Developer Guide

This guide provides essential information for developers creating addons for the NSR-AI Minecraft plugin. It covers how to interact with the NSR-AI API, check for feature availability, and ensure compatibility.

## 1. API Versioning and Compatibility

The NSR-AI API uses a strict versioning system to ensure compatibility between the core plugin and your addons.

*   **`NSRaiAPI.API_VERSION`**: This `public static final int` constant in the `NSRaiAPI` class indicates the current major API version. Addons should check this value to ensure compatibility.

### Checking API Compatibility

It is crucial to check the API version at your addon's `onEnable()` stage.

```java
import com.nsr.ai.api.NSRaiAPI;
import org.bukkit.plugin.java.JavaPlugin;

public class MyAddon extends JavaPlugin {

    private static final int REQUIRED_API_VERSION = 2; // The API version your addon is built against

    @Override
    public void onEnable() {
        if (NSRaiAPI.getApiVersion() < REQUIRED_API_VERSION) {
            getLogger().severe("NSR-AI API version is too old! " 
                + "Required: " + REQUIRED_API_VERSION 
                + ", Found: " + NSRaiAPI.getApiVersion());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("NSR-AI API version " + NSRaiAPI.getApiVersion() + " detected. Addon enabled.");
        // Proceed with your addon's logic
    }
}
```

## 2. Safely Detecting Missing Features

The NSR-AI core plugin may not have all features (like GUI or Security) enabled or implemented in every version. The public API is designed to gracefully handle these situations.

*   **Conditional Features:** Methods for features that might not be present (e.g., `openCustomGUI`, `getSecurityStatus`) will throw an `IllegalStateException` if the underlying service is not available in the core plugin.

### Example: Handling Conditional Features

Always wrap calls to conditional features in `try-catch` blocks to prevent your addon from crashing.

```java
import com.nsr.ai.api.NSRaiAPI;
import com.nsr.ai.api.SecurityStatus;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MySecurityAddon extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            SecurityStatus status = NSRaiAPI.getSecurityStatus();
            event.getPlayer().sendMessage("NSR-AI Security Status: " + status.name());
        } catch (IllegalStateException e) {
            event.getPlayer().sendMessage("NSR-AI Security System is not supported by this core version.");
            getLogger().warning("Could not get security status: " + e.getMessage());
        }
    }

    public void tryOpenGUI(Player player) {
        try {
            // Assuming you have a GUIBuilder instance
            // NSRaiAPI.openCustomGUI(player, myGUIBuilder);
            player.sendMessage("Opened custom GUI!");
        } catch (IllegalStateException e) {
            player.sendMessage("NSR-AI GUI System is not supported by this core version.");
            getLogger().warning("Could not open GUI: " + e.getMessage());
        }
    }
}
```

*   **Optional Returns:** Methods that return data (e.g., `getPetData`, `getSharedMemory`) will return `Optional.empty()` if the feature is not supported or no data is available. Always check if the `Optional` is present.

```java
import com.nsr.ai.api.NSRaiAPI;
import com.nsr.ai.api.PetDataSnapshot;
import org.bukkit.entity.Player;
import java.util.Optional;
import java.util.UUID;

public class MyPetHelper {

    public void displayPetData(Player player, UUID ownerId) {
        Optional<PetDataSnapshot> petData = NSRaiAPI.getPetData(ownerId);
        if (petData.isPresent()) {
            player.sendMessage("Owner " + ownerId + " has pet data: " + petData.get().getData());
        } else {
            player.sendMessage("No pet data found for owner " + ownerId + ", or pet system not supported.");
        }
    }
}
```

## 3. Asynchronous Operations

All AI-related operations (e.g., `sendMessageToAI`, `getAIResponse`) are asynchronous and return `CompletableFuture`. This prevents your addon from blocking the main server thread.

### Example: Handling AI Responses

```java
import com.nsr.ai.api.NSRaiAPI;
import com.nsr.ai.api.AIMessage;
import com.nsr.ai.api.AIResponse;
import org.bukkit.entity.Player;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MyAIInteraction {

    public void askAI(Player player, String question) {
        AIMessage userMessage = new AIMessage(question, player.getUniqueId());
        NSRaiAPI.getAIResponse(userMessage)
                .thenAccept(aiResponse -> {
                    if (aiResponse.isSuccess()) {
                        player.sendMessage("AI says: " + aiResponse.getResponse());
                    } else {
                        player.sendMessage("AI failed to respond: " 
                        + aiResponse.getResponse());
                    }
                })
                .exceptionally(ex -> {
                    player.sendMessage("An error occurred while getting AI response: " 
                        + ex.getMessage());
                    ex.printStackTrace();
                    return null;
                });
    }
}
```

## 4. Addon Registration

To ensure your addon is properly loaded and recognized by the NSR-AI core plugin, you **must** include an `addon.yml` file in the root of your addon's JAR archive. This file provides essential metadata for your addon.

### 4.1 The `addon.yml` File (Mandatory)

Every NSR-AI addon **must** include an `addon.yml` file in the root of its JAR archive. This file provides critical metadata that the NSR-AI plugin uses to load and manage your addon.

#### Required Fields:

*   **`main`**: This field specifies the fully qualified class name of your addon's main class. This class must implement the `com.nsr.ai.plugin.addons.Addon` interface.
    *   **Example:** `main: com.yourcompany.yourapi.YourAddonMainClass`

*   **`name`**: This field defines the unique name of your addon. This name is used for logging, commands, and security validation. Choose a descriptive and unique name.
    *   **Example:** `name: MyAwesomeAddon`

*   **`version`**: This field indicates the version of your addon. It's good practice to follow semantic versioning (e.g., `1.0.0`, `1.2.3-BETA`).
    *   **Example:** `version: 1.0.0`

#### Example `addon.yml`:

```yaml
main: com.yourcompany.yourapi.YourAddonMainClass
name: MyAwesomeAddon
version: 1.0.0
description: A brief description of what your addon does.
author: YourName
website: https://yourwebsite.com
```

### 4.2 Registering Your Addon Programmatically

After ensuring your `addon.yml` is correctly configured, register your addon with the NSR-AI core using `NSRaiAPI.registerAddon()`. Your main plugin class must implement the `AIAddon` interface.

```java
import com.nsr.ai.api.NSRaiAPI;
import com.nsr.ai.api.AIAddon;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.HashMap;
import org.bukkit.entity.Player;

public class MyAddon extends JavaPlugin implements AIAddon { // Implement AIAddon

    private static final int REQUIRED_API_VERSION = 2; // The API version your addon is built against

    @Override
    public void onEnable() {
        if (NSRaiAPI.getApiVersion() < REQUIRED_API_VERSION) {
            getLogger().severe("NSR-AI API version is too old! "
                + "Required: " + REQUIRED_API_VERSION
                + ", Found: " + NSRaiAPI.getApiVersion());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        NSRaiAPI.registerAddon(this); // Pass your AIAddon instance
        getLogger().info("MyAddon registered with NSR-AI core.");
        // ... other onEnable logic
    }

    @Override
    public void onDisable() {
        // ... onDisable logic
    }

    // AIAddon interface implementations
    // getName() and getVersion() are handled by JavaPlugin's getDescription()
    // onEnable() and onDisable() are handled by JavaPlugin's lifecycle

    @Override
    public String onCommand(Player player, String[] args) {
        // ... command handling logic
        return null;
    }

    @Override
    public Map<String, String> getCommands() {
        return new HashMap<>();
    }

    @Override
    public Map<String, String> getFeatures() {
        return new HashMap<>();
    }
}
``````

## 5. Addon Installation

For an addon to be recognized and loaded by NSR-AI, its JAR file **must** be placed in the following directory:

```
/plugins/NSR-AI/addons/
```

Addons placed in the main `/plugins/` directory or any other location will not be loaded by the addon manager. This ensures a clean separation between standard plugins and NSR-AI addons.

Standard Bukkit/Spigot plugins that do not interact with the NSR-AI API can be placed in the main `/plugins/` folder as usual.

As a developer, you **must** instruct your users to place your addon's JAR file in this specific directory. For an example of how to communicate this, see the installation instructions for plugins like [Advance-Player-Stats](https://modrinth.com/plugin/advance-player-stats).

## 6. Addon Command Guidelines

To prevent conflicts with the core plugin's commands and to ensure a consistent user experience, all addons must follow these command registration rules:

### Standard Addon Commands

All general addon commands must be prefixed with either `/aiaddon` or its shorter alias, `/aia`.

-   **Correct:** `/aiaddon myfeature`
-   **Correct:** `/aia stats`
-   **Incorrect:** `/myfeature`

### Advanced Commands (Conditional)

In specific cases, you may register a sub-command under the main `/ai` command (e.g., `/ai playerstats`). This is permitted **only if** your command logic meets the following criteria:

1.  **No Conflict:** It must not override or interfere with any existing or future core `/ai` sub-commands.
2.  **No Conversation Interference:** It must not disrupt a player's ongoing conversation with the AI. Your command must be distinct and not something a player would say in a normal chat. For example, an addon like `Advance-Player-Stats` could use `/ai stats` because it's a specific, non-conversational keyword.

Failure to follow these guidelines may result in your addon being blocked by the core plugin's security manager.

## 7. Further Assistance

For any further questions or issues, please refer to the main `README.md` or contact the NSR-AI development team (blackforge31@gmail.com).

## 8. Addon Submission and Review Process

To ensure the security, stability, and compliance of the NSR-AI ecosystem, especially for addons that interact with core functionalities, violets our rules or introduce
new commands, we have established a submission and review process. This process is designed to prevent your addon from being blocked or banned by the
core plugin's security manager. (Especially api,offline mode etc. Genral addons you can build without the permission)

### 8.1 Requesting Permission to Build

Before embarking on the development of a potentially high-risk or deeply integrated addon, we encourage developers to reach out to the NSR-AI
development team. This allows us to provide guidance, clarify API usage, and confirm the feasibility of your addon idea in advance.

   Contact:* Please email the NSR-AI development team at blackforge31@gmail.com with a brief description of your addon's intended functionality and how
it plans to interact with the NSR-AI API.

### 8.2 Post-Development Validation

Once your addon is developed, it must undergo a validation process to ensure it adheres to all guidelines, including the "Commons Clause" of the
license, and does not pose any security risks.

   Inform the Team:* After completing your addon, please inform the NSR-AI development team at blackforge31@gmail.com. We will then initiate the
validation process.
   Validation Outcome:* We will review your addon's functionality and inform you whether it is validated for use within the NSR-AI ecosystem.

### 8.3 Source Code Review for High-Risk Addons

For addons deemed "high-risk" (e.g., those interacting with security features, modifying core AI behavior, or handling sensitive player data), a source
 code review will be required as part of the validation process.

   Confidentiality:* We assure you that any source code provided for review will be stored privately and confidentially. It will not be shared with any
 third parties.
   Developer Rights:* Your intellectual property rights to your addon's source code remain entirely yours. The review is solely for security and
compliance verification.

Failure to comply with this submission and review process, especially for high-risk addons, may result in your addon being blocked by the core plugin's
 security manager.