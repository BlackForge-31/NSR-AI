# NSR-AI API

This is the official API for the NSR-AI Minecraft plugin. It allows developers to create addons that can interact with and extend the functionality of NSR-AI.

## Disclaimer

This API only provides a way to interact with the NSR-AI plugin. The core AI features, such as NPCs, pets, AI, memory, and offline mode, are only available in the closed-source NSR-AI plugin.

## Maven Dependency

To use this API, you need to add the following dependency to your `pom.xml` file:

```xml
<repositories>
    <repository>
        <id>nsr-ai-api-repo</id>
        <url>https://raw.github.com/BlackForge-31/NSR-AI/main/repo</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.nsr.ai</groupId>
    <artifactId>nsr-ai</artifactId>
    <version>1.2</version>
    <scope>provided</scope>
</dependency>
```

## Example Usage

Here is an example of how to listen to the `AIChatEvent`:

```java
import com.nsr.ai.api.events.AIChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MyListener implements Listener {

    @EventHandler
    public void onAIChat(AIChatEvent event) {
        // Your code here
    }
}
```
