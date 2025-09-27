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
            getLogger().severe("NSR-AI API version is too old! Required: " + REQUIRED_API_VERSION + ", Found: " + NSRaiAPI.getApiVersion());
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
                        player.sendMessage("AI failed to respond: " + aiResponse.getResponse());
                    }m
                })
                .exceptionally(ex -> {
                    player.sendMessage("An error occurred while getting AI response: " + ex.getMessage());
                    ex.printStackTrace();
                    return null;
                });
    }
}
```

## 4. Addon Registration

Register your addon with the NSR-AI core using `NSRaiAPI.registerAddon()`. This allows the core plugin to manage your addon, especially for security purposes.

```java
import com.nsr.ai.api.NSRaiAPI;
import org.bukkit.plugin.java.JavaPlugin;

public class MyAddon extends JavaPlugin {

    @Override
    public void onEnable() {
        NSRaiAPI.registerAddon(this); // Pass your plugin instance
        getLogger().info("MyAddon registered with NSR-AI core.");
    }
}
```

## 5. Further Assistance

For any further questions or issues, please refer to the main `README.md` or contact the NSR-AI development team.
