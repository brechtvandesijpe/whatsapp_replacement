package be.kuleuven.MessageHandling;

import be.kuleuven.*;

public class MessageHandler {
    private Client client;

    public MessageHandler(Client client) {
        this.client = client;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public String toString() {
        return "MessageHandler{" +
                "client=" + client +
                '}';
    }
}
