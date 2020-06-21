package nw.ExchangePlatform.wrapper;

import nw.ExchangePlatform.data.MarketParticipantOrder;
import nw.ExchangePlatform.data.OrderDTO;
import nw.ExchangePlatform.data.Transferable;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class Session implements Runnable {
    private final int sessionID;
    private final Socket clientSocket;
    private int clientUserID;
    private String clientUserName;
    private Queue systemQueue;
    public static AtomicInteger currentAvailableOrderID;

    static {
        currentAvailableOrderID = null;
    }
    public Session(Socket clientSocket,int sessionID, Queue systemQueue, int baseOrderID) {
        this.clientSocket = clientSocket;
        this.sessionID = sessionID;
        this.systemQueue = systemQueue;
        if(currentAvailableOrderID == null) {
            currentAvailableOrderID = new AtomicInteger(baseOrderID);
        }
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
        ClientReader reader = new ClientReader(clientSocket.getInputStream());
        boolean readerFlag = true;

        while (readerFlag) {
            Transferable DTO = reader.ReadRequestFromClient();
            if(Class.forName("OrderDTO").isInstance(DTO)) {
                OrderDTO orderDTO = (OrderDTO)DTO;
                int orderID = currentAvailableOrderID.getAndIncrement();
                MarketParticipantOrder order = new MarketParticipantOrder(sessionID,clientUserID,clientUserName, orderID,
                 new Date(),orderDTO.getDirection(),orderDTO.getTickerSymbol(),orderDTO.getSize(),orderDTO.getPrice(),
                        orderDTO.getOrderType(),orderDTO.getOrderDuration());
                systemQueue.PutOrder(order);
            }

            if(DTO.equals(null))
                readerFlag = false;
            }
    }

    public void run() {
        try {
            RunCurrentSession();
        } catch (Exception e) {

        }
    }
}


