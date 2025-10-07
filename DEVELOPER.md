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

NSR-AI automatically discovers and registers addons. To create an addon, you need to:

1.  **Implement the `AIAddon` interface:** Your main addon class must implement the `com.nsr.ai.api.AIAddon` interface.
2.  **Create an `addon.yml` file:** In your project's resources folder, create a file named `addon.yml`.

### `addon.yml` Example

Here is an example of a valid `addon.yml` file:

```yaml
# The main class of your addon, which implements AIAddon
main: com.example.myaddon.MyAddon
# The name of your addon
name: MyAddon
# The version of your addon
version: 1.0
```

## 5. Addon Installation

For an addon to be recognized and loaded by NSR-AI, its JAR file can be placed in two locations:

1.  **Dedicated Addons Folder:** `/plugins/NSR-AI/addons/`
2.  **Main Plugins Folder:** `/plugins/`

If an addon is placed in the main `/plugins/` folder, it **must** contain an `addon.yml` file and **must not** contain a `plugin.yml` file. This prevents conflicts with regular Bukkit/Spigot plugins.

As a developer, you **must** instruct your users on the correct installation method.

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
