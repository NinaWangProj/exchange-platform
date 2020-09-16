package serverEngine;

import clearing.data.CredentialWareHouse;
import clearing.data.DTCCWarehouse;
import clearing.engine.ClearingEngineManager;
import common.ServerQueue;

import common.LimitOrderBookWareHouse;
import session.SessionManager;
import trading.workflow.TradingEngineManager;


import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServerEngine {
    private ServerSocket serverSocket;
    private ServerConfig config;
    private CredentialWareHouse credentialWareHouse;
    private ConcurrentHashMap<String,ReadWriteLock> OrderBooklocks;

    public ServerEngine(ServerConfig config) {
        this.config = config;
        //need to persist the credential; will work on it later. for now, hardcode baseuserid to 0;
        credentialWareHouse = new CredentialWareHouse(0);
        OrderBooklocks = new ConcurrentHashMap<String,ReadWriteLock>();
    }

    public void Start() throws Exception{
        serverSocket = new ServerSocket(config.getServerPortID());
        ServerQueue systemServerQueue = new ServerQueue(config.getNumOfOrderQueues(),config.getNumOfEngineResultQueues());
        LimitOrderBookWareHouse limitOrderBookWareHouse = new LimitOrderBookWareHouse(config.getComparatorType());
        DTCCWarehouse DTCC = new DTCCWarehouse();

        //create session for each client and deserialize client requests
        SessionManager sessionManager = new SessionManager(serverSocket, systemServerQueue, config.getBaseOrderID(),
                credentialWareHouse, limitOrderBookWareHouse, OrderBooklocks, DTCC.portfoliosMap);
        Thread sessions = new Thread(sessionManager);
        sessions.start();

        TradingEngineManager tradingEngineManager = new TradingEngineManager(systemServerQueue,
                limitOrderBookWareHouse);
        tradingEngineManager.Start();

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




