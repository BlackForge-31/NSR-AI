package com.nsr.ai.api;

import java.util.UUID;

public class AIMessage {
    private final String content;
    private final UUID senderId;

    public AIMessage(String content, UUID senderId) {
        this.content = content;
        this.senderId = senderId;
    }

    public String getContent() { return content; }
    public UUID getSenderId() { return senderId; }
}
