package be.kuleuven.Instances;

import javax.crypto.*;
import java.util.*;

public class BulletinEntry {
    private int boxNumber;
    private byte[] tag;
    private SecretKey secretKey;

    public BulletinEntry() {
    }

    public BulletinEntry(int boxNumber, byte[] tag, SecretKey secretKey) {
        this.boxNumber = boxNumber;
        this.tag = tag;
        this.secretKey = secretKey;
    }

    public int getBoxNumber() {
        return boxNumber;
    }

    public byte[] getTag() {
        return tag;
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }

    public void setBoxNumber(int boxNumber) {
        this.boxNumber = boxNumber;
    }

    public void setTag(byte[] tag) {
        this.tag = tag;
    }

    public void setSecretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    // String representation of the BulletinEntry
    public String toString() {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded()) + ";" + boxNumber + ";" + new String(tag);
    }

}
