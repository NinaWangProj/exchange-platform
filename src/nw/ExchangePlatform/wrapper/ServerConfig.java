package nw.ExchangePlatform.wrapper;

public class ServerConfig {
    private final int numOfOrderQueues;
    private final int numOfEngineResultQueues;
    private final int serverPortID;
    private final int baseOrderID;

    public ServerConfig(int numOfOrderQueues,int numOfEngineResultQueues,
                        int serverPortID,int baseOrderID) {
        this.numOfOrderQueues = numOfOrderQueues;
        this.numOfEngineResultQueues = numOfEngineResultQueues;
        this.serverPortID = serverPortID;
        this.baseOrderID = baseOrderID;
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
}
