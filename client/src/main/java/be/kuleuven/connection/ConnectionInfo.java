package be.kuleuven.connection;

import javax.crypto.SecretKey;

public class ConnectionInfo {
    private int boxNumber;
    private byte[] tag;
    private SecretKey key;

    public ConnectionInfo(int boxNummer, byte[] tag, SecretKey key) {
        this.boxNumber = boxNummer;
        this.tag = tag;
        this.key = key;
    }

    public int getBoxNumber() {
        return boxNumber;
    }

    public byte[] getTag() {
        return tag;
    }

    public SecretKey getSecretKey() {
        return key;
    }

    public void setBoxNumber(int boxNummer) {
        this.boxNumber = boxNummer;
    }

    public void setTag(byte[] tag) {
        this.tag = tag;
    }

    public void setSecretKey(SecretKey key) {
        this.key = key;
    }
}
