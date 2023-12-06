package be.kuleuven.instances;

import java.util.*;

public class Mailbox {

    private static final int NUM_MESSAGES = 64;
    private Map<String, byte[]> messageMap;

    public Mailbox() {
        this.messageMap = new HashMap<>();
    }

    public Map<String, byte[]> getMessageMap() {
        return messageMap;
    }

    public void setMessageMap(Map<String, byte[]> messageMap) {
        this.messageMap = messageMap;
    }

    public void storeMessage(byte[] tag, byte[] message) {
        // Convert the tag byte array to a string for map storage
        String tagStr = new String(tag);
        messageMap.put(tagStr, message);
    }

    public byte[] getMessageByTag(byte[] tag) {
        String tagStr = new String(tag);
        byte[] message = messageMap.get(tagStr);
        // Remove the message from the map after retrieval
        if (message != null) {
            messageMap.remove(tagStr);
        }
        return message;
    }

    public boolean isFUll() {
        return messageMap.size() == NUM_MESSAGES;
    }

    public boolean isEmpty() {
        return messageMap.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("mailbox { messageMap: {");
        for (String s : messageMap.keySet()) {
            sb.append(s).append(": ").append(Arrays.toString(messageMap.get(s))).append(", ");
        }
        sb.append("}}");
        return sb.toString();
    }
}
