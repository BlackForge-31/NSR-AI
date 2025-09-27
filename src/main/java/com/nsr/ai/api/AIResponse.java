package com.nsr.ai.api;

public class AIResponse {
    private final String response;
    private final boolean success;

    public AIResponse(String response, boolean success) {
        this.response = response;
        this.success = success;
    }

    public String getResponse() { return response; }
    public boolean isSuccess() { return success; }
}
