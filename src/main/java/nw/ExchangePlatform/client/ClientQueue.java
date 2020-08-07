package nw.ExchangePlatform.client;

import java.util.concurrent.LinkedBlockingQueue;

public class ClientQueue {
    private LinkedBlockingQueue<String> serverMessages;

    public ClientQueue() {
        serverMessages = new LinkedBlockingQueue<String>();
    }

    public LinkedBlockingQueue<String> getServerMessages() {
        return serverMessages;
    }
}
