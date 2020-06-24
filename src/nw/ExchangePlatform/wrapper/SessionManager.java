package nw.ExchangePlatform.wrapper;

import nw.ExchangePlatform.data.CredentialWareHouse;
import nw.ExchangePlatform.data.MareketDataRequestDTO;
import nw.ExchangePlatform.data.MarketDataWareHouse;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class SessionManager implements Runnable{
    private static int nextAvailableSessionID;
    private ArrayList<Session> sessionUniverse;
    private ServerSocket serverSocket;
    private Queue systemQueue;
    private int baseOrderID;
    private CredentialWareHouse credentialWareHouse;
    private MarketDataWareHouse dataWareHouse;

    public SessionManager(ServerSocket serverSocket, Queue systemQueue, int baseOrderID,
                          CredentialWareHouse credentialWareHouse, MarketDataWareHouse dataWareHouse) {
        this.sessionUniverse = new ArrayList<Session>();
        this.serverSocket = serverSocket;
        this.systemQueue = systemQueue;
        this.baseOrderID = baseOrderID;
        this.credentialWareHouse = credentialWareHouse;
        this.dataWareHouse = dataWareHouse;
        nextAvailableSessionID = 0;
    }

    public void Start() throws Exception{
        while (true) {
            Socket clientSocket = serverSocket.accept();
            Session session = new Session(clientSocket, nextAvailableSessionID, systemQueue, baseOrderID,
                    credentialWareHouse,dataWareHouse);
            sessionUniverse.add(session);
            nextAvailableSessionID += 1;
            Thread sessionThread = new Thread(session);
            sessionThread.start();
        }
    }

    public void run() {
        try {
            Start();
        } catch (Exception e) {
        }
    }
}
