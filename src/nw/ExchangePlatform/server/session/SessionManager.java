package nw.ExchangePlatform.server.session;

import nw.ExchangePlatform.clearing.data.CredentialWareHouse;
import nw.ExchangePlatform.commonData.ServerQueue;
import nw.ExchangePlatform.trading.limitOrderBook.LimitOrderBookWareHouse;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;

public class SessionManager implements Runnable{
    private static int nextAvailableSessionID;
    private ArrayList<Session> sessionUniverse;
    private ServerSocket serverSocket;
    private ServerQueue systemServerQueue;
    private int baseOrderID;
    private CredentialWareHouse credentialWareHouse;
    private LimitOrderBookWareHouse dataWareHouse;
    private ReadWriteLock[] locks;

    public SessionManager(ServerSocket serverSocket, ServerQueue systemServerQueue, int baseOrderID,
                          CredentialWareHouse credentialWareHouse, LimitOrderBookWareHouse dataWareHouse, ReadWriteLock[] locks) {
        this.sessionUniverse = new ArrayList<Session>();
        this.serverSocket = serverSocket;
        this.systemServerQueue = systemServerQueue;
        this.baseOrderID = baseOrderID;
        this.credentialWareHouse = credentialWareHouse;
        this.dataWareHouse = dataWareHouse;
        this.locks = locks;
        nextAvailableSessionID = 0;
    }

    public void Start() throws Exception{
        while (true) {
            Socket clientSocket = serverSocket.accept();
            Session session = new Session(clientSocket, nextAvailableSessionID, systemServerQueue, baseOrderID,
                    credentialWareHouse,dataWareHouse,locks);
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
