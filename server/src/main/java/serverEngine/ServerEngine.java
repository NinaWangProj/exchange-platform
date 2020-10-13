package serverEngine;

import clearing.data.CredentialWareHouse;
import clearing.data.DTCCWarehouse;
import clearing.engine.ClearingEngineManager;
import common.ServerQueue;

import common.LimitOrderBookWareHouse;
import session.SessionManager;
import trading.workflow.TradingEngineManager;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.Paths;

public class ServerEngine {
    private ServerSocket serverSocket;
    private ServerConfig config;

    public ServerEngine(ServerConfig config) {
        this.config = config;
    }

    public ServerEngine(String configFilePath) throws FileNotFoundException {
        InputStream configInputStream = new FileInputStream(configFilePath);
        this.config = ServerConfig.BuildFromJSON(configInputStream);
    }

    public void Start() throws Exception{
        serverSocket = new ServerSocket(config.getServerPortID());
        ServerQueue systemServerQueue = new ServerQueue(config.getNumOfOrderQueues(),config.getNumOfEngineResultQueues());
        LimitOrderBookWareHouse limitOrderBookWareHouse = new LimitOrderBookWareHouse(config.getComparatorType());
        DTCCWarehouse DTCC = new DTCCWarehouse();
        CredentialWareHouse credentialWareHouse = new CredentialWareHouse(0);

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

    public void StartFromSnapShot() throws Exception {
        serverSocket = new ServerSocket(config.getServerPortID());

        ServerQueue systemServerQueue = new ServerQueue(config.getNumOfOrderQueues(),config.getNumOfEngineResultQueues());

        //Restore server snapshot from file
        String snapshotFolderPath = config.getSnapShotFolderPath();

        //Load LimitOrderBookWareHouse from file
        String bidBooksFilePath = Paths.get(snapshotFolderPath,"BidOrderBooks.csv").toString();
        String askBooksFilePath = Paths.get(snapshotFolderPath,"AskOrderBooks.csv").toString();
        InputStream bidsInputStream = new FileInputStream(bidBooksFilePath);
        InputStream asksInputStream = new FileInputStream(askBooksFilePath);
        LimitOrderBookWareHouse limitOrderBookWareHouse = LimitOrderBookWareHouse.BuildFromCSV(bidsInputStream,
                asksInputStream,config.getComparatorType());

        //Load DTCCWareHouse from file
        String dtccFilePath = Paths.get(snapshotFolderPath,"DTCCWareHouse.json").toString();
        InputStream dtccInputStream = new FileInputStream(dtccFilePath);
        DTCCWarehouse DTCC = DTCCWarehouse.ReadFromJSON(dtccInputStream);

        //Load Credentials from file
        String credentialsFilePath = Paths.get(snapshotFolderPath,"CredentialWareHouse.json").toString();
        InputStream credentialInputStream = new FileInputStream(credentialsFilePath);
        CredentialWareHouse credentialWareHouse =  CredentialWareHouse.BuildFromJSON(credentialInputStream);

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
        //Graceful exit
        String SnapShotOutputPath = config.getSnapShotFolderPath();

        //Create a flag to signal server to stop taking requests from client
        //finish current workflow


        //save dtcc warehouse to file
        //save credential to file
        //save limitOrderBook to file
        //write out config file

    }
}




