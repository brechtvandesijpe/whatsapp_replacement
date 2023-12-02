package be.kuleuven.connection;

import javax.crypto.SecretKey;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Base64;

public class ConnectionInfo {
    private int boxNumber;
    private byte[] tag;
    private SecretKey key;

    public ConnectionInfo(int boxNummer, byte[] tag, SecretKey key) {
        this.boxNumber = boxNummer;
        this.tag = tag;
        this.key = key;
    }

    public JSONObject toJSONObject() {
        JSONObject output = new JSONObject();
        output.put("boxNumber", boxNumber);
        String base64EncodedTag = Base64.getEncoder().encodeToString(tag);
        output.put("tag", base64EncodedTag);
        output.put("key", convertSecretKeyToString(key));
        return output;
    }

    public ConnectionInfo(JSONObject data) {
        boxNumber = data.getInt("boxNumber");
        String retrievedBase64 = data.getString("tag");
        tag = Base64.getDecoder().decode(retrievedBase64);
        key = convertStringToSecretKey(data.getString("key"));
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

    // Convert SecretKey to string
    private static String convertSecretKeyToString(SecretKey secretKey) {
        byte[] encodedKey = secretKey.getEncoded();
        return Base64.getEncoder().encodeToString(encodedKey);
    }

    // Convert string back to SecretKey
    private static SecretKey convertStringToSecretKey(String encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new javax.crypto.spec.SecretKeySpec(decodedKey, "AES");
    }
}
