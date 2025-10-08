# NSR-AI Developer Guide

This guide provides essential information for developers creating addons for the NSR-AI Minecraft plugin. It covers how to interact with the NSR-AI API, check for feature availability, and ensure compatibility.

## 1. API Versioning and Compatibility

The NSR-AI API uses a strict versioning system to ensure compatibility between the core plugin and your addons.

*   **`NSRaiAPI.API_VERSION`**: This `public static final int` constant in the `NSRaiAPI` class indicates the current major API version. Addons should check this value to ensure compatibility.

### Checking API Compatibility

It is crucial to check the API version at your addon's `onEnable()` stage.

```java
import com.nsr.ai.api.NSRaiAPI;
import com.nsr.ai.plugin.NSRAIPlugin; // Import the core plugin for context
import org.bukkit.plugin.java.JavaPlugin;

public class MyAddon extends JavaPlugin {

    // Define the API version your addon is built against.
    // This should match the API_VERSION constant in the NSR-AI API you are using.
    private static final int REQUIRED_API_VERSION = 2; 

    @Override
    public void onEnable() {
        // Always check the API version to ensure your addon is compatible with the running NSR-AI core.
        if (NSRaiAPI.getApiVersion() < REQUIRED_API_VERSION) {
            getLogger().severe("NSR-AI API version is too old! " 
                + "Required: " + REQUIRED_API_VERSION 
                + ", Found: " + NSRaiAPI.getApiVersion());
            getServer().getPluginManager().disablePlugin(this); // Disable your addon if incompatible
            return;
        }
        getLogger().info("NSR-AI API version " + NSRaiAPI.getApiVersion() + " detected. Addon enabled.");
        // Proceed with your addon's specific initialization logic here.
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
            // Attempt to get the security status. This might throw IllegalStateException if the service is not available.
            SecurityStatus status = NSRaiAPI.getSecurityStatus();
            event.getPlayer().sendMessage("NSR-AI Security Status: " + status.name());
        } catch (IllegalStateException e) {
            // Handle the case where the security system is not supported or enabled.
            event.getPlayer().sendMessage("NSR-AI Security System is not supported by this core version.");
            getLogger().warning("Could not get security status: " + e.getMessage());
        }
    }

    public void tryOpenGUI(Player player) {
        try {
            // Attempt to open a custom GUI. This might throw IllegalStateException if the GUI service is not available.
            // Assuming you have a GUIBuilder instance:
            // NSRaiAPI.openCustomGUI(player, myGUIBuilder);
            player.sendMessage("Opened custom GUI!");
        } catch (IllegalStateException e) {
            // Handle the case where the GUI system is not supported or enabled.
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
        // Attempt to retrieve pet data.
        Optional<PetDataSnapshot> petData = NSRaiAPI.getPetData(ownerId);
        if (petData.isPresent()) {
            // If pet data is available, process it.
            player.sendMessage("Owner " + ownerId + " has pet data: " + petData.get().getData());
        } else {
            // Handle the case where no pet data is found or the pet system is not supported.
            player.sendMessage("No pet data found for owner " + ownerId + ", or pet system not supported.");
        }
    }
}
```

## 3. Asynchronous Operations

All AI-related operations (e.g., `sendMessageToAI`, `getAIResponse`) are asynchronous and return `CompletableFuture`. This prevents your addon from blocking the main server thread, ensuring a smooth player experience.

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
        // Call the AI asynchronously and handle the response when it's ready.
        NSRaiAPI.getAIResponse(userMessage)
                .thenAccept(aiResponse -> {
                    // This block executes on the main thread after the AI responds.
                    if (aiResponse.isSuccess()) {
                        player.sendMessage("AI says: " + aiResponse.getResponse());
                    } else {
                        player.sendMessage("AI failed to respond: " 
                        + aiResponse.getResponse());
                    }
                })
                .exceptionally(ex -> {
                    // Handle any exceptions that occurred during the asynchronous operation.
                    player.sendMessage("An error occurred while getting AI response: " 
                        + ex.getMessage());
                    ex.printStackTrace(); // Log the full stack trace for debugging
                    return null; // Return null to complete the CompletableFuture exceptionally
                });
    }
}
```

## 4. Addon Structure

Your addon's main class must implement the `AIAddon` interface. This interface defines the contract for how your addon interacts with the NSR-AI core plugin, allowing it to manage your addon's lifecycle and retrieve essential information.

Here's a detailed breakdown of the `AIAddon` interface methods and how to implement them:

```java
package com.example.myaddon; // Your addon's package

import com.nsr.ai.api.AIAddon;
import com.nsr.ai.plugin.NSRAIPlugin; // Import the core plugin for context
import org.bukkit.entity.Player;
import org.bukkit.ChatColor; // For sending colored messages

import java.util.Collections;
import java.util.Map;
import java.util.HashMap; // For returning multiple commands/features

public class MySimpleAddon implements AIAddon {

    // A reference to the main NSR-AI plugin instance.
    // This is crucial for interacting with core functionalities like logging,
    // accessing managers (e.g., PetManager, KnowledgeManager), and sending messages.
    private NSRAIPlugin nsrAiPlugin;

    /**
     * Called when your addon is enabled by the NSR-AI core plugin.
     * This is where you should perform your addon's initialization logic,
     * such as registering event listeners, loading configurations, or setting up internal data structures.
     *
     * @param plugin The main instance of the NSR-AI core plugin. Use this to access core functionalities.
     */
    @Override
    public void onEnable(NSRAIPlugin plugin) {
        this.nsrAiPlugin = plugin;
        nsrAiPlugin.getLogger().info(getName() + " v" + getVersion() + " by " + getAuthor() + " enabled!");
        // Example: Register a Bukkit event listener if your addon needs to listen for events
        // nsrAiPlugin.getServer().getPluginManager().registerEvents(new MyAddonListener(nsrAiPlugin), nsrAiPlugin);
    }

    /**
     * Called when your addon is disabled by the NSR-AI core plugin.
     * This is where you should perform cleanup tasks, such as unregistering listeners,
     * saving data, or closing connections, to prevent memory leaks or unexpected behavior.
     */
    @Override
    public void onDisable() {
        nsrAiPlugin.getLogger().info(getName() + " v" + getVersion() + " by " + getAuthor() + " disabled!");
        // Example: Unregister listeners if you registered any in onEnable
        // HandlerList.unregisterAll(myAddonListenerInstance);
    }

    /**
     * Returns the official name of your addon.
     * This name is used for display purposes in the `/ai addon list` command and in logs.
     * It should match the 'name' field in your addon.yml.
     *
     * @return The name of the addon.
     */
    @Override
    public String getName() {
        // It's good practice to get this from your addon's plugin.yml/addon.yml if possible,
        // but for simplicity, we return a hardcoded string here.
        return "MySimpleAddon";
    }

    /**
     * Returns the current version of your addon.
     * This version is displayed in the `/ai addon list` command and helps users and developers
     * track which version of your addon is running. It should match the 'version' field in your addon.yml.
     *
     * @return The version string of the addon.
     */
    @Override
    public String getVersion() {
        return "1.0";
    }

    /**
     * Returns the author(s) of your addon.
     * This information is displayed in the `/ai addon list` command.
     * It should match the 'author' field in your addon.yml.
     *
     * @return The author(s) of the addon.
     */
    @Override
    public String getAuthor() {
        return "Gemini";
    }

    /**
     * This method is called when a player executes a command that starts with `/ai`
     * and is not handled by the core plugin's default commands.
     * Your addon can choose to handle specific subcommands here.
     *
     * @param player The player who executed the command.
     * @param args   The arguments of the command (e.g., for `/ai myaddon hello`, args would be ["myaddon", "hello"]).
     * @return A message to be sent back to the player if the command was handled by this addon,
     *         or `null` if the command was not handled by this addon (allowing other addons or default behavior to take over).
     */
    @Override
    public String onCommand(Player player, String[] args) {
        // Example: Handle a subcommand like "/ai myaddon hello"
        if (args.length > 0 && args[0].equalsIgnoreCase("myaddon")) {
            if (args.length > 1 && args[1].equalsIgnoreCase("hello")) {
                player.sendMessage(ChatColor.GREEN + "Hello from MySimpleAddon!");
                return "Handled by MySimpleAddon"; // Indicate that the command was handled
            }
        }
        return null; // Command not handled by this addon
    }

    /**
     * Returns a map of commands provided by your addon.
     * This map is used by the core plugin to generate help messages or for internal command routing.
     * The key should be the command name (e.g., "myaddon hello"), and the value should be a brief description.
     *
     * @return A map where keys are command names and values are their descriptions.
     */
    @Override
    public Map<String, String> getCommands() {
        Map<String, String> commands = new HashMap<>();
        commands.put("myaddon hello", "Says hello from the addon.");
        return commands;
    }

    /**
     * Returns a map of features provided by your addon.
     * This map can be used by the core plugin or other addons to understand the capabilities of your addon.
     * The key should be the feature name (e.g., "greeting-command"), and the value should be a brief description.
     *
     * @return A map where keys are feature names and values are their descriptions.
     */
    @Override
    public Map<String, String> getFeatures() {
        Map<String, String> features = new HashMap<>();
        features.put("simple-greeting", "Provides a basic greeting command for demonstration.");
        return features;
    }
}
```

## 5. Addon Configuration

Every NSR-AI addon **must** include an `addon.yml` file in its `src/main/resources` directory. This file serves as the manifest for your addon, providing essential metadata that the NSR-AI core plugin uses to load, identify, and manage it.

Here's an example `addon.yml` and a detailed explanation of each field:

```yaml
# addon.yml
# This file must be located in your addon's src/main/resources directory.

# The official name of your addon. This should be unique and descriptive.
# It is displayed in the /ai addon list command.
name: MySimpleAddon

# The current version of your addon. Follow semantic versioning (e.g., 1.0.0, 1.2-BETA).
# It is displayed in the /ai addon list command.
version: 1.0

# The author(s) of the addon. This is displayed in the /ai addon list command.
author: Gemini

# The fully qualified name of your addon's main class.
# This class must implement the com.nsr.ai.api.AIAddon interface.
# Example: com.yourcompany.youraddon.MainClass
main: com.example.myaddon.MySimpleAddon
```

**Explanation of `addon.yml` fields:**

*   **`name`**: (Required) A unique string identifying your addon. This name will be used in logs and in the `/ai addon list` command.
*   **`version`**: (Required) The current version of your addon. It's recommended to follow [Semantic Versioning](https://semver.org/).
*   **`author`**: (Required) The name(s) of the addon developer(s).
*   **`main`**: (Required) The full path to your addon's main class, including its package. This class must implement the `com.nsr.ai.api.AIAddon` interface. The NSR-AI core plugin will instantiate this class when loading your addon.

## 6. Addon Installation

For an addon to be recognized and loaded by NSR-AI, its JAR file **must** be placed in the following directory:

```
/plugins/NSR-AI/addons/
```

Addons placed in the main `/plugins/` directory or any other location will not be loaded by the addon manager. This ensures a clean separation between standard plugins and NSR-AI addons.

Standard Bukkit/Spigot plugins that do not interact with the NSR-AI API can be placed in the main `/plugins/` folder as usual.

As a developer, you **must** instruct your users to place your addon's JAR file in this specific directory. For an example of how to communicate this, see the installation instructions for plugins like [Advance-Player-Stats](https://modrinth.com/plugin/advance-player-stats).

## 7. Addon Command Guidelines

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

## 8. Further Assistance

For any further questions or issues, please refer to the main `README.md` or contact the NSR-AI development team (blackforge31@gmail.com).

## 9. Addon Submission and Review Process

To ensure the security, stability, and compliance of the NSR-AI ecosystem, especially for addons that interact with core functionalities, violets our rules or introduce
new commands, we have established a submission and review process. This process is designed to prevent your addon from being blocked or banned by the
core plugin's security manager. (Especially api,offline mode etc. Genral addons you can build without the permission)

### 9.1 Requesting Permission to Build

Before embarking on the development of a potentially high-risk or deeply integrated addon, we encourage developers to reach out to the NSR-AI
development team. This allows us to provide guidance, clarify API usage, and confirm the feasibility of your addon idea in advance.

   Contact:* Please email the NSR-AI development team at blackforge31@gmail.com with a brief description of your addon's intended functionality and how
it plans to interact with the NSR-AI API.

### 9.2 Post-Development Validation

Once your addon is developed, it must undergo a validation process to ensure it adheres to all guidelines, including the "Commons Clause" of the
license, and does not pose any security risks.

   Inform the Team:* After completing your addon, please inform the NSR-AI development team at blackforge31@gmail.com. We will then initiate the
validation process.
   Validation Outcome:* We will review your addon's functionality and inform you whether it is validated for use within the NSR-AI ecosystem.

### 9.3 Source Code Review for High-Risk Addons

For addons deemed "high-risk" (e.g., those interacting with security features, modifying core AI behavior, or handling sensitive player data), a source
 code review will be required as part of the validation process.

   Confidentiality:* We assure you that any source code provided for review will be stored privately and confidentially. It will not be shared with any
 third parties.
   Developer Rights:* Your intellectual property rights to your addon's source code remain entirely yours. The review is solely for security and
compliance verification.

Failure to comply with this submission and review process, especially for high-risk addons, may result in your addon being blocked by the core plugin's
 security manager.