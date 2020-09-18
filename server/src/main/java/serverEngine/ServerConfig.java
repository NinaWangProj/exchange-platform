package serverEngine;

import trading.limitOrderBook.OrderComparatorType;

import java.util.concurrent.atomic.AtomicLong;

public class ServerConfig {
    private final int numOfOrderQueues;
    private final int numOfEngineResultQueues;
    private final int serverPortID;
    private final int baseOrderID;
    private final OrderComparatorType comparatorType;
    private final AtomicLong previousTransactionID;

    public ServerConfig(int numOfOrderQueues, int numOfEngineResultQueues, int serverPortID,
                        int baseOrderID, OrderComparatorType comparatorType, AtomicLong previousTransactionID) {
        this.numOfOrderQueues = numOfOrderQueues;
        this.numOfEngineResultQueues = numOfEngineResultQueues;
        this.serverPortID = serverPortID;
        this.baseOrderID = baseOrderID;
        this.comparatorType = comparatorType;
        this.previousTransactionID = previousTransactionID;
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

    public AtomicLong getPreviousTransactionID() {
        return previousTransactionID;
    }
}
