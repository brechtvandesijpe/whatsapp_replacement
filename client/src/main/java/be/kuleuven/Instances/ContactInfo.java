package be.kuleuven.Instances;

import javax.crypto.*;
import java.util.*;

public class ContactInfo {
    private String contactName;
    private int boxNumber_AB;
    private int boxNumber_BA;
    private byte[] tag_AB;
    private byte[] tag_BA;
    private SecretKey secretKey_AB;
    private SecretKey secretKey_BA;

    public ContactInfo(String contactName, int boxNumber_AB, int boxNumber_BA, byte[] tag_AB, byte[] tag_BA, SecretKey secretKey_AB, SecretKey secretKey_BA){
        this.contactName = contactName;
        this.boxNumber_AB = boxNumber_AB;
        this.boxNumber_BA = boxNumber_BA;
        this.tag_AB = tag_AB;
        this.tag_BA = tag_BA;
        this.secretKey_AB = secretKey_AB;
        this.secretKey_BA = secretKey_BA;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public int getBoxNumber_AB() {
        return boxNumber_AB;
    }

    public void setBoxNumber_AB(int boxNumber_AB) {
        this.boxNumber_AB = boxNumber_AB;
    }

    public int getBoxNumber_BA() {
        return boxNumber_BA;
    }

    public void setBoxNumber_BA(int boxNumber_BA) {
        this.boxNumber_BA = boxNumber_BA;
    }

    public byte[] getTag_AB() {
        return tag_AB;
    }

    public void setTag_AB(byte[] tag_AB) {
        this.tag_AB = tag_AB;
    }

    public byte[] getTag_BA() {
        return tag_BA;
    }

    public void setTag_BA(byte[] tag_BA) {
        this.tag_BA = tag_BA;
    }

    public SecretKey getSecretKey_AB() {
        return secretKey_AB;
    }

    public void setSecretKey_AB(SecretKey secretKey_AB) {
        this.secretKey_AB = secretKey_AB;
    }

    public SecretKey getSecretKey_BA() {
        return secretKey_BA;
    }

    public void setSecretKey_BA(SecretKey secretKey_BA) {
        this.secretKey_BA = secretKey_BA;
    }

    @Override
    public String toString() {
        return "ContactInfo{" +
                "contactName='" + contactName + '\'' +
                ", boxNumber_AB=" + boxNumber_AB +
                ", boxNumber_BA=" + boxNumber_BA +
                ", tag_AB=" + Arrays.toString(tag_AB) +
                ", tag_BA=" + Arrays.toString(tag_BA) +
                ", secretKey_AB=" + secretKey_AB +
                ", secretKey_BA=" + secretKey_BA +
                '}';
    }
}