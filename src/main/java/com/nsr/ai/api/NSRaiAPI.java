package com.nsr.ai.api;

import org.bukkit.entity.Player;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.nsr.ai.api.AIMessage;
import com.nsr.ai.api.AIResponse;
import com.nsr.ai.api.PetDataSnapshot;
import com.nsr.ai.api.PetListener;
import com.nsr.ai.api.NPCListener;
import com.nsr.ai.api.GUIBuilder;
import com.nsr.ai.api.GUIListener;
import com.nsr.ai.api.SecurityStatus;

/**
 * ⚠️ This API is read-only, cannot bypass NSR-AI security, cannot store or reveal API keys,
 * and cannot be used to create scripted/canned AI responses.
 */
public final class NSRaiAPI {

    public static final int API_VERSION = 2;

    // Internal core plugin reference (set via reflection by the core plugin)
    private static Object internalApiInstance; // Represents the internal com.nsr.ai.plugin.api.NSRaiAPI

    private NSRaiAPI() {
        // Private constructor to prevent instantiation
    }

    /**
     * Internal method used by the NSR-AI core plugin to set the internal API instance.
     * Addon developers should NOT call this method.
     * @param instance The internal API instance.
     */
    public static void setInternalApiInstance(Object instance) {
        NSRaiAPI.internalApiInstance = instance;
    }

    private static <T> T callInternalMethod(String methodName, Class<?>[] paramTypes, Object... args) {
        if (internalApiInstance == null) {
            throw new IllegalStateException("NSR-AI core plugin not initialized or API not ready.");
        }
        try {
            // Using reflection to call the internal API methods
            // This ensures the public API is a thin facade over the internal implementation
            java.lang.reflect.Method method = internalApiInstance.getClass().getMethod(methodName, paramTypes);
            return (T) method.invoke(internalApiInstance, args);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("NSR-AI core plugin does not support method: " + methodName + ". API mismatch?", e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            // Unwrap the real exception thrown by the internal method
            if (e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) e.getTargetException();
            } else {
                throw new IllegalStateException("Error calling internal NSR-AI API method: " + methodName, e.getTargetException());
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access internal NSR-AI API method: " + methodName, e);
        }
    }

    // --- Chat API ---
    public static CompletableFuture<Void> sendMessageToAI(Player player, AIMessage message) {
        return callInternalMethod("sendMessageToAI", new Class<?>[]{Player.class, AIMessage.class}, player, message);
    }

    public static CompletableFuture<AIResponse> getAIResponse(AIMessage message) {
        return callInternalMethod("getAIResponse", new Class<?>[]{AIMessage.class}, message);
    }

    // --- Pets API ---
    public static Optional<PetDataSnapshot> getPetData(UUID owner) {
        try {
            return Optional.ofNullable(callInternalMethod("getPetData", new Class<?>[]{UUID.class}, owner));
        } catch (IllegalStateException e) {
            // If the internal method throws an IllegalStateException (e.g., service not available),
            // we return Optional.empty() as per requirement.
            return Optional.empty();
        }
    }

    public static void registerPetListener(PetListener listener) {
        callInternalMethod("registerPetListener", new Class<?>[]{PetListener.class}, listener);
    }

    // --- NPC API ---
    public static void registerNPCListener(NPCListener listener) {
        callInternalMethod("registerNPCListener", new Class<?>[]{NPCListener.class}, listener);
    }

    public static void updateNPCSkin(String npcName, String texture, String signature) {
        callInternalMethod("updateNPCSkin", new Class<?>[]{String.class, String.class, String.class}, npcName, texture, signature);
    }

    // --- GUI API (Conditional) ---
    public static void openCustomGUI(Player player, GUIBuilder guiBuilder) {
        // Internal API throws UnsupportedOperationException if GUI service is null.
        // We re-throw it as IllegalStateException as per public API requirement.
        try {
            callInternalMethod("openCustomGUI", new Class<?>[]{Player.class, GUIBuilder.class}, player, guiBuilder);
        } catch (UnsupportedOperationException e) {
            throw new IllegalStateException("GUI system not supported by this NSR-AI version.", e);
        }
    }

    public static void registerGUIListener(GUIListener listener) {
        try {
            callInternalMethod("registerGUIListener", new Class<?>[]{GUIListener.class}, listener);
        } catch (UnsupportedOperationException e) {
            throw new IllegalStateException("GUI system not supported by this NSR-AI version.", e);
        }
    }

    // --- Memory API ---
    public static Optional<String> getSharedMemory(String key) {
        // Internal API logs warning and returns Optional.empty() for now.
        return callInternalMethod("getSharedMemory", new Class<?>[]{String.class}, key);
    }

    public static void updateSharedMemory(String key, String value) {
        // Internal API logs warning for now.
        callInternalMethod("updateSharedMemory", new Class<?>[]{String.class, String.class}, key, value);
    }

    // --- Security API (Conditional) ---
    public static SecurityStatus getSecurityStatus() {
        // Internal API throws UnsupportedOperationException if Security service is null.
        // We re-throw it as IllegalStateException as per public API requirement.
        try {
            return callInternalMethod("getSecurityStatus", new Class<?>[]{});
        } catch (UnsupportedOperationException e) {
            throw new IllegalStateException("Security system not supported by this NSR-AI version.", e);
        }
    }

    // --- Versioning API ---
    public static String getVersion() {
        return callInternalMethod("getVersion", new Class<?>[]{});
    }

    public static int getApiVersion() {
        return API_VERSION;
    }

    // --- Addon Management ---
    public static void registerAddon(Object addonPluginInstance) {
        // TODO: Implement addon registration logic in the internal API if needed.
        // For now, this is a placeholder to satisfy the public API requirement.
        // The internal API would likely have a method like 'addonManager.registerAddon(addonPluginInstance)'
        System.out.println("NSR-AI API: Addon " + addonPluginInstance.getClass().getName() + " registered.");
    }
}

