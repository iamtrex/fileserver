package com.rweqx.files;

import com.rweqx.authentication.AccessType;

/**
 * Represents a shared file.
 */
public class SharedFile {
    private final String userKey;
    private final String ownerKey;
    private final String path;
    private final AccessType accessType;
    private final String expiresAtMillis;

    public SharedFile(String userKey, String ownerKey, String path, AccessType accessType, String expiresAtMillis) {
        this.userKey = userKey;
        this.ownerKey = ownerKey;
        this.path = path;
        this.accessType = accessType;
        this.expiresAtMillis = expiresAtMillis;
    }

    public String getUserKey() {
        return userKey;
    }

    public String getOwnerKey() {
        return ownerKey;
    }

    public String getPath() {
        return path;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public String getExpiresAtMillis() {
        return expiresAtMillis;
    }

}

