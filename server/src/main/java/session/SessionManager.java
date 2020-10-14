package session;

import clearing.data.CredentialWareHouse;
import commonData.clearing.MarketParticipantPortfolio;
import common.ServerQueue;
import common.LimitOrderBookWareHouse;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;

public class SessionManager implements Runnable{
    private static int nextAvailableSessionID;
    private ArrayList<Session> sessionUniverse;
    private ServerSocket serverSocket;
    private ServerQueue systemServerQueue;
    private static AtomicInteger baseOrderID;
    private CredentialWareHouse credentialWareHouse;
    private LimitOrderBookWareHouse dataWareHouse;
    private HashMap<Integer, MarketParticipantPortfolio> portfoliosMap;

    public SessionManager(ServerSocket serverSocket, ServerQueue systemServerQueue, int baseOrderID,
                          CredentialWareHouse credentialWareHouse, LimitOrderBookWareHouse dataWareHouse,
                          HashMap<Integer, MarketParticipantPortfolio> portfoliosMap) {
        this.sessionUniverse = new ArrayList<Session>();
        this.serverSocket = serverSocket;
        this.systemServerQueue = systemServerQueue;
        this.baseOrderID = new AtomicInteger(baseOrderID);
        this.credentialWareHouse = credentialWareHouse;
        this.dataWareHouse = dataWareHouse;
        this.portfoliosMap = portfoliosMap;
        nextAvailableSessionID = 0;
    }

    public void Start() throws Exception{
        while (true) {
            Socket clientSocket = serverSocket.accept();
            Session session = new Session(clientSocket, nextAvailableSessionID, systemServerQueue,
                    credentialWareHouse,dataWareHouse, portfoliosMap);
            sessionUniverse.add(session);
            nextAvailableSessionID += 1;
            session.RunCurrentSession();
        }
    }

    public static int getAvailableOrderID() {
        return baseOrderID.incrementAndGet();
    }

    public void run() {
        try {
            Start();
        } catch (Exception e) {
        }
    }
}
