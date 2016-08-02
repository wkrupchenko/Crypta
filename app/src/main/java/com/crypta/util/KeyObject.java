package com.crypta.util;

import java.io.Serializable;

import javax.crypto.SecretKey;

/**
 * Created by D064343 on 31.07.2016.
 */

public class KeyObject implements Serializable {

    private static final long serialVersionUID = 1L;

    private SecretKey key;

    private int fileNameHash;

    public KeyObject(SecretKey key, int fileNameHash) {
        this.key = key;
        this.fileNameHash = fileNameHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeyObject keyObject = (KeyObject) o;

        return getFileNameHash() == keyObject.getFileNameHash();

    }

    @Override
    public int hashCode() {
        return getFileNameHash();
    }

    public SecretKey getKey() {
        return key;
    }

    public void setKey(SecretKey key) {
        this.key = key;
    }

    public int getFileNameHash() {
        return fileNameHash;
    }

    public void setFileNameHash(int fileNameHash) {
        this.fileNameHash = fileNameHash;
    }
}
