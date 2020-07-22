package nw.ExchangePlatform.server;

import nw.ExchangePlatform.clearing.data.CredentialWareHouse;
import nw.ExchangePlatform.clearing.data.DTCCWarehouse;
import nw.ExchangePlatform.commonData.ServerQueue;
import nw.ExchangePlatform.trading.limitOrderBook.LimitOrderBookWareHouse;
import nw.ExchangePlatform.server.session.SessionManager;


import java.net.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServerEngine {
    private ServerSocket serverSocket;
    private ServerConfig config;
    private CredentialWareHouse credentialWareHouse;

    public ServerEngine(ServerConfig config) {
        this.config = config;
        //need to persist the credential; will work on it later. for now, hardcode baseuserid to 0;
        credentialWareHouse = new CredentialWareHouse(0);
    }

    public void Start() throws Exception{
        serverSocket = new ServerSocket(config.getServerPortID());
        ServerQueue systemServerQueue = new ServerQueue(config.getNumOfOrderQueues(),config.getNumOfEngineResultQueues());
        LimitOrderBookWareHouse limitOrderBookWareHouse = new LimitOrderBookWareHouse(config.getComparatorType());
        ReadWriteLock[] locks = GenerateSynchronizationLocks(config.getNumOfOrderQueues());

        //create session for each client and deserialize client requests
        SessionManager sessionManager = new SessionManager(serverSocket, systemServerQueue, config.getBaseOrderID(),
                credentialWareHouse, limitOrderBookWareHouse, locks);
        Thread sessions = new Thread(sessionManager);
        sessions.start();

        TradingEngineManager tradingEngineManager = new TradingEngineManager(systemServerQueue,
                limitOrderBookWareHouse,locks);
        tradingEngineManager.Start();

        DTCCWarehouse DTCC = new DTCCWarehouse();
        ClearingEngineManager clearingEngineManager = new ClearingEngineManager(systemServerQueue,DTCC);
        clearingEngineManager.Start();
    }

    private ReadWriteLock[] GenerateSynchronizationLocks(int numOfTradingEngineGroups) {
        ReadWriteLock[] locks = new ReadWriteLock[numOfTradingEngineGroups];
        for(int i = 0; i< numOfTradingEngineGroups; i++) {
            ReadWriteLock lock = new ReentrantReadWriteLock();
            locks[i] = lock;
        }
        return locks;
    }
}




