package nw.ExchangePlatform.wrapper;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class SessionManager implements Runnable{
    private static int nextAvailableSessionID;
    private ArrayList<Session> sessionUniverse;
    private ServerSocket serverSocket;
    private Queue systemQueue;
    private int baseOrderID;

    public SessionManager(ServerSocket serverSocket, Queue systemQueue, int baseOrderID) {
        this.sessionUniverse = new ArrayList<Session>();
        this.serverSocket = serverSocket;
        this.systemQueue = systemQueue;
        this.baseOrderID = baseOrderID;
        nextAvailableSessionID = 0;
    }

    public void Start() throws Exception{
        while (true) {
            Socket clientSocket = serverSocket.accept();
            Session session = new Session(clientSocket, nextAvailableSessionID, systemQueue, baseOrderID);
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
