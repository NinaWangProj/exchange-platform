package nw.ExchangePlatform.client;

import nw.ExchangePlatform.commonData.marketData.MarketData;

import java.util.concurrent.LinkedBlockingQueue;

public class ClientQueue {
    private LinkedBlockingQueue<String> serverMessages;

    public ClientQueue() {
        serverMessages = new LinkedBlockingQueue<>();
    }

    public LinkedBlockingQueue<String> getServerMessages() {
        return serverMessages;
    }
}
