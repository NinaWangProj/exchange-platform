package nw.ExchangePlatform.wrapper;

import nw.ExchangePlatform.data.*;


import java.net.*;

public class Server{
    private ServerSocket serverSocket;
    private ServerConfig config;
    private CredentialWareHouse credentialWareHouse;

    public Server(ServerConfig config) {
        this.config = config;
        //need to persist the credential; will work on it later. for now, hardcode baseuserid to 0;
        credentialWareHouse = new CredentialWareHouse(0);
    }

    public void Start() throws Exception{
        serverSocket = new ServerSocket(config.getServerPortID());
        Queue systemQueue = new Queue(config.getNumOfOrderQueues(),config.getNumOfEngineResultQueues());
        MarketDataWareHouse marketDataWareHouse = new MarketDataWareHouse();

        //create session for each client and deserialize client requests
        SessionManager sessionManager = new SessionManager(serverSocket, systemQueue, config.getBaseOrderID(),
                credentialWareHouse, marketDataWareHouse);
        Thread sessions = new Thread(sessionManager);
        sessions.start();

        TradingEngineManager tradingEngineManager = new TradingEngineManager(systemQueue,marketDataWareHouse);
        tradingEngineManager.Start();

        DTCCWarehouse DTCC = new DTCCWarehouse();
        ClearingEngineManager clearingEngineManager = new ClearingEngineManager(systemQueue,DTCC);
        clearingEngineManager.Start();




    }
}




