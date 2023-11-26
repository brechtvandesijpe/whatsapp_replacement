package be.kuleuven.model;

import java.util.ArrayList;

public class Chat extends ArrayList<ChatMessage> {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ChatMessage message : this) {
            sb.append(message + "\n");
        }
        return sb.toString();
    }
}
