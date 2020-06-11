package nw.ExchangePlatform.wrapper;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.ListIterator;

public class Server implements Runnable{
    private ServerSocket serverSocket;
    private int serverPortID;
    private static int nextAvailableSessionID;
    private ArrayList<Session> sessionUniverse;

    public Server() {
        serverPortID = 58673;
        sessionUniverse = new ArrayList<>();
    }

    private void StartListening() throws IOException{
        serverSocket = new ServerSocket(serverPortID);
        //start listening for client; once heard client, hand shake with client to establish connection
        while(true) {
            Socket clientSocket = serverSocket.accept();
            Session session = new Session(clientSocket,nextAvailableSessionID);
            sessionUniverse.add(session);
            nextAvailableSessionID += 1;
        }
    }

    public void StartWorking() throws Exception {
        ListIterator<Session> sessions = sessionUniverse.listIterator();
        while(true) {
            if(sessions.hasNext()) {
                Session session = sessions.next();
                //create a new thread for this session
                Thread sessionThread = new Thread(session);
                sessionThread.start();
            }
        }
    }

    public void run() {
        try {
            StartListening();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}




