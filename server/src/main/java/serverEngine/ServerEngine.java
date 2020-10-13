package serverEngine;

import clearing.data.CredentialWareHouse;
import clearing.data.DTCCWarehouse;
import clearing.engine.ClearingEngineManager;
import common.ServerQueue;

import common.LimitOrderBookWareHouse;
import session.SessionManager;
import trading.workflow.TradingEngineManager;


import java.io.IOException;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
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
        DTCCWarehouse DTCC = new DTCCWarehouse();

        //create session for each client and deserialize client requests
        SessionManager sessionManager = new SessionManager(serverSocket, systemServerQueue, config.getBaseOrderID(),
                credentialWareHouse, limitOrderBookWareHouse, DTCC.portfoliosMap);
        Thread sessions = new Thread(sessionManager);
        sessions.start();

        TradingEngineManager tradingEngineManager = new TradingEngineManager(systemServerQueue,
                limitOrderBookWareHouse,config.getPreviousTransactionID());
        tradingEngineManager.Start();

        ClearingEngineManager clearingEngineManager = new ClearingEngineManager(systemServerQueue,DTCC);
        clearingEngineManager.Start();
    }

    public void StartFromSnapShot(String SnapShotFolderPath) throws Exception {
        serverSocket = new ServerSocket(config.getServerPortID());
        ServerQueue systemServerQueue = new ServerQueue(config.getNumOfOrderQueues(),config.getNumOfEngineResultQueues());

        //Load LimitOrderBookWareHouse from file
        LimitOrderBookWareHouse limitOrderBookWareHouse = new LimitOrderBookWareHouse(config.getComparatorType());

        //Load DTCCWareHouse from file
        DTCCWarehouse DTCC = new DTCCWarehouse();

        //Load Credentials from file
        //credentialWareHouse;

        //create session for each client and deserialize client requests
        SessionManager sessionManager = new SessionManager(serverSocket, systemServerQueue, config.getBaseOrderID(),
                credentialWareHouse, limitOrderBookWareHouse, DTCC.portfoliosMap);
        Thread sessions = new Thread(sessionManager);
        sessions.start();

        TradingEngineManager tradingEngineManager = new TradingEngineManager(systemServerQueue,
                limitOrderBookWareHouse,config.getPreviousTransactionID());
        tradingEngineManager.Start();

        ClearingEngineManager clearingEngineManager = new ClearingEngineManager(systemServerQueue,DTCC);
        clearingEngineManager.Start();
    }

    public void SaveStateAndStopServer() {

    }
}




