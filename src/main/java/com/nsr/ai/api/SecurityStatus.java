package com.nsr.ai.api;

/**
 * Represents the current security status of the NSR-AI plugin.
 */
public enum SecurityStatus {
    /** The security system is active and operating normally. */
    ACTIVE,
    /** The security system has detected a potential issue or is operating with reduced functionality. */
    WARNING,
    /** The security system has detected a critical threat or has failed. */
    CRITICAL
}
