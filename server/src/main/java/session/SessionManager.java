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
import java.util.concurrent.locks.ReadWriteLock;

public class SessionManager implements Runnable{
    private static int nextAvailableSessionID;
    private ArrayList<Session> sessionUniverse;
    private ServerSocket serverSocket;
    private ServerQueue systemServerQueue;
    private int baseOrderID;
    private CredentialWareHouse credentialWareHouse;
    private LimitOrderBookWareHouse dataWareHouse;
    private ConcurrentHashMap<String,ReadWriteLock> locks;
    private HashMap<Integer, MarketParticipantPortfolio> portfoliosMap;

    public SessionManager(ServerSocket serverSocket, ServerQueue systemServerQueue, int baseOrderID,
                          CredentialWareHouse credentialWareHouse, LimitOrderBookWareHouse dataWareHouse,
                          ConcurrentHashMap<String,ReadWriteLock> locks, HashMap<Integer, MarketParticipantPortfolio> portfoliosMap) {
        this.sessionUniverse = new ArrayList<Session>();
        this.serverSocket = serverSocket;
        this.systemServerQueue = systemServerQueue;
        this.baseOrderID = baseOrderID;
        this.credentialWareHouse = credentialWareHouse;
        this.dataWareHouse = dataWareHouse;
        this.locks = locks;
        this.portfoliosMap = portfoliosMap;
        nextAvailableSessionID = 0;
    }

    public void Start() throws Exception{
        while (true) {
            Socket clientSocket = serverSocket.accept();
            Session session = new Session(clientSocket, nextAvailableSessionID, systemServerQueue, baseOrderID,
                    credentialWareHouse,dataWareHouse,locks, portfoliosMap);
            sessionUniverse.add(session);
            nextAvailableSessionID += 1;
            session.RunCurrentSession();
        }
    }

    public void run() {
        try {
            Start();
        } catch (Exception e) {
        }
    }
}
