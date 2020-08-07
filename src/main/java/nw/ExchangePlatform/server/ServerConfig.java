package nw.ExchangePlatform.server;

import nw.ExchangePlatform.trading.limitOrderBook.OrderComparatorType;

public class ServerConfig {
    private final int numOfOrderQueues;
    private final int numOfEngineResultQueues;
    private final int serverPortID;
    private final int baseOrderID;
    private final OrderComparatorType comparatorType;

    public ServerConfig(int numOfOrderQueues,int numOfEngineResultQueues,
                        int serverPortID,int baseOrderID, OrderComparatorType comparatorType) {
        this.numOfOrderQueues = numOfOrderQueues;
        this.numOfEngineResultQueues = numOfEngineResultQueues;
        this.serverPortID = serverPortID;
        this.baseOrderID = baseOrderID;
        this.comparatorType = comparatorType;
    }

    public int getNumOfOrderQueues() {
        return numOfOrderQueues;
    }

    public int getNumOfEngineResultQueues() {
        return numOfEngineResultQueues;
    }

    public int getServerPortID() {
        return serverPortID;
    }

    public int getBaseOrderID() {
        return baseOrderID;
    }

    public OrderComparatorType getComparatorType() {
        return comparatorType;
    }
}
