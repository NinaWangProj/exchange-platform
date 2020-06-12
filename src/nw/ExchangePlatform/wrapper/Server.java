package nw.ExchangePlatform.wrapper;

import nw.ExchangePlatform.data.DTCCWarehouse;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server{
    private ServerSocket serverSocket;
    private int serverPortID;
    private static int nextAvailableSessionID;
    private ArrayList<Session> sessionUniverse;

    public Server() {
        serverPortID = 58673;
        sessionUniverse = new ArrayList<>();
    }

    public void Start() throws IOException{
        serverSocket = new ServerSocket(serverPortID);
        OrderQueue orderQueue = new OrderQueue();


        WrapperEngine engine = new WrapperEngine(10,0,new DTCCWarehouse());

        while(true) {
            Socket clientSocket = serverSocket.accept();
            Session session = new Session(clientSocket,nextAvailableSessionID,orderQueue);
            sessionUniverse.add(session);
            nextAvailableSessionID += 1;
            Thread sessionThread = new Thread(session);
            sessionThread.start();


        }
    }
}




