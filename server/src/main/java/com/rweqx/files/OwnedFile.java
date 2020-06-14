package com.rweqx.files;

public class OwnedFile {
    private String owner;
    private String path;

    public OwnedFile(String owner, String path) {
        this.owner = owner;
        this.path = path;
    }

    public String getOwner() {
        return owner;
    }

    public String getPath() {
        return path;
    }
}
