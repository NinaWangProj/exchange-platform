package nw.ExchangePlatform.wrapper;

import nw.ExchangePlatform.data.DTOType;
import nw.ExchangePlatform.data.MarketParticipantOrder;
import nw.ExchangePlatform.data.OrderDTO;

import javax.print.attribute.standard.ReferenceUriSchemesSupported;
import java.io.InputStream;
import java.net.Socket;

public class Session implements Runnable {
    private final int sessionID;
    private final Socket clientSocket;
    private int clientUserID;
    private String clientUserName;
    private OrderQueue orderQueue;

    public Session(Socket clientSocket,int sessionID,OrderQueue orderQueue) {
        this.clientSocket = clientSocket;
        this.sessionID = sessionID;
        this.orderQueue = orderQueue;
    }
    public int getSessionID() {
        return sessionID;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public int getClientUserID() {
        return clientUserID;
    }

    public void setClientUserID(int clientUserID) {
        this.clientUserID = clientUserID;
    }

    public String getClientUserName() {
        return clientUserName;
    }

    public void setClientUserName(String clientUserName) {
        this.clientUserName = clientUserName;
    }

    private void RunCurrentSession() throws Exception{
        ClientReader reader = new ClientReader(clientSocket.getInputStream(),orderQueue);
        ClientWriter writer = new ClientWriter(clientSocket.getOutputStream());

        Thread readerThread = new Thread(reader);
        readerThread.start();

        Thread writerThread = new Thread(writer);
        writerThread.start();
    }

    public void run() {
        try {
            RunCurrentSession();
        } catch (Exception e) {

        }
    }
}


