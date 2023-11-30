package be.kuleuven.model;

import be.kuleuven.connection.BulletinEntry;

import javax.crypto.SecretKey;

public class Contact {
    private String contactName;
    private BulletinEntry bulletinEntry_AB;
    private BulletinEntry bulletinEntry_BA;

    // Contains information needed to communicate with the contact
    // and the name you use for your contact in your client
    public Contact(String contactName, int boxNumber_AB, int boxNumber_BA, byte[] tag_AB, byte[] tag_BA,
                       SecretKey secretKey_AB, SecretKey secretKey_BA) {
        this.contactName = contactName;
        this.bulletinEntry_AB = new BulletinEntry(boxNumber_AB, tag_AB, secretKey_AB);
        this.bulletinEntry_BA = new BulletinEntry(boxNumber_BA, tag_BA, secretKey_BA);
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public BulletinEntry getBulletinEntry_AB() {
        return bulletinEntry_AB;
    }

    public BulletinEntry getBulletinEntry_BA() {
        return bulletinEntry_BA;
    }
}
