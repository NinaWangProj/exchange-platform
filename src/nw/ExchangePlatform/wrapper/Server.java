package nw.ExchangePlatform.wrapper;

import nw.ExchangePlatform.data.DTCCWarehouse;
import nw.ExchangePlatform.data.MarketParticipantOrder;
import nw.ExchangePlatform.data.OrderDTO;
import nw.ExchangePlatform.data.TradingOutput;
import nw.ExchangePlatform.trading.TradingEngine;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class Server{
    private ServerSocket serverSocket;
    private ServerConfig config;

    public Server(ServerConfig config) {
        this.config = config;
    }

    public void Start() throws Exception{
        serverSocket = new ServerSocket(config.getServerPortID());
        Queue systemQueue = new Queue(config.getNumOfOrderQueues(),config.getNumOfEngineResultQueues(),
                config.getNumOfClearingEngineResultQueues());

        //create session for each client and deserialize client requests
        SessionManager sessionManager = new SessionManager(serverSocket, systemQueue, config.getBaseOrderID());
        Thread sessions = new Thread(sessionManager);
        sessions.start();

        TradingEngineManager tradingEngineManager = new TradingEngineManager(systemQueue);
        tradingEngineManager.Start();

        DTCCWarehouse DTCC = new DTCCWarehouse();
        ClearingEngineManager clearingEngineManager = new ClearingEngineManager(systemQueue,DTCC);
        clearingEngineManager.Start();




    }
}




