package be.kuleuven.model;

import javax.crypto.SecretKey;

public class Contact {
    private String contactName;
    private int boxNumber_AB;
    private int boxNumber_BA;
    private byte[] tag_AB;
    private byte[] tag_BA;
    private SecretKey secretKey_AB;
    private SecretKey secretKey_BA;

    // Contains information needed to communicate with the contact
    // and the name you use for your contact in your client
    public Contact(String contactName, int boxNumber_AB, int boxNumber_BA, byte[] tag_AB, byte[] tag_BA,
                       SecretKey secretKey_AB, SecretKey secretKey_BA) {
        this.contactName = contactName;
        this.boxNumber_AB = boxNumber_AB;
        this.boxNumber_BA = boxNumber_BA;
        this.tag_AB = tag_AB;
        this.tag_BA = tag_BA;
        this.secretKey_AB = secretKey_AB;
        this.secretKey_BA = secretKey_BA;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }
}
