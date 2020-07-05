package nw.ExchangePlatform.wrapper;

import javafx.util.Pair;
import nw.ExchangePlatform.data.*;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class Session implements Runnable {
    private final int sessionID;
    private final Socket clientSocket;
    private int clientUserID;
    private String clientUserName;
    private Queue systemQueue;
    private CredentialWareHouse credentialWareHouse;
    private MarketDataWareHouse dataWareHouse;
    public static AtomicInteger currentAvailableOrderID;

    static {
        currentAvailableOrderID = null;
    }
    public Session(Socket clientSocket,int sessionID, Queue systemQueue, int baseOrderID,
                   CredentialWareHouse credentialWareHouse, MarketDataWareHouse dataWareHouse) {
        this.clientSocket = clientSocket;
        this.sessionID = sessionID;
        this.systemQueue = systemQueue;
        this.credentialWareHouse = credentialWareHouse;
        this.dataWareHouse = dataWareHouse;
        if(currentAvailableOrderID == null) {
            currentAvailableOrderID = new AtomicInteger(baseOrderID);
        }
    }

    private void RunCurrentSession() throws Exception{
        systemQueue.RegisterSessionWithQueue(sessionID);

        ClientRequestProcessor reader = new ClientRequestProcessor(clientSocket.getInputStream());
        reader.Attach(this);
        Thread readerThread = new Thread(reader);
        readerThread.start();

        MessageProcessor messageProcessor = new MessageProcessor();
        Thread messageThread = new Thread(messageProcessor);
        messageThread.start();

        ClientResponseProcessor writer = new ClientResponseProcessor(clientSocket.getOutputStream(), systemQueue,sessionID);
        Thread writerThread = new Thread(writer);
        writerThread.start();
    }

    public void On_ReceivingDTO(Transferable DTO) throws Exception {
        if(Class.forName("OrderDTO").isInstance(DTO)) {
            OrderDTO orderDTO = (OrderDTO)DTO;
            int orderID = currentAvailableOrderID.getAndIncrement();
            MarketParticipantOrder order = new MarketParticipantOrder(sessionID,clientUserID,clientUserName, orderID,
                    new Date(),orderDTO.getDirection(),orderDTO.getTickerSymbol(),orderDTO.getSize(),orderDTO.getPrice(),
                    orderDTO.getOrderType(),orderDTO.getOrderDuration());
            systemQueue.PutOrder(order);
        }

        if(Class.forName("OpenAcctDTO").isInstance(DTO)) {
            LoginDTO loginDTO = (LoginDTO)DTO;
            String userName = loginDTO.getUserName();
            String password = loginDTO.getPassword();
            boolean pass = credentialWareHouse.CreateAccount(userName,password);
            if(pass) {
                clientUserName = userName;
                clientUserID = credentialWareHouse.GetUserID(userName);
            } else {
                //send message back to client
            }
        }

        if(Class.forName("LoginDTO").isInstance(DTO)) {
            LoginDTO loginDTO = (LoginDTO)DTO;
            String userName = loginDTO.getUserName();
            String password = loginDTO.getPassword();
            boolean pass = credentialWareHouse.ValidateLogin(userName,password);
            if(pass) {
                clientUserName = userName;
                clientUserID = credentialWareHouse.GetUserID(userName);
            } else {
                //session needs to send client a message "wrong credential, please try again"
                //implement later
            }
        }

        if(Class.forName("MareketDataRequestDTO").isInstance(DTO)) {
            MareketDataRequestDTO marketDataRequestDTO = (MareketDataRequestDTO)DTO;
            String tickerSymbol = marketDataRequestDTO.getTickerSymbol();
            MarketDataType type = marketDataRequestDTO.getDataType();
            boolean found = dataWareHouse.ValidateRequest(tickerSymbol);
            if(found) {
                Pair<sortedOrderList,sortedOrderList> marketData =
                        dataWareHouse.GetMarketData(tickerSymbol,type);
                //Create market data dto and put into queue

            } else {
                //send message back to client, no market data for the request ticker symbol is found
            }
        }
    }

    public MessageDTO CreateMessageDTOForWriter(String messages) {
        MessageDTO messageDTO = new MessageDTO(messages);
        return messageDTO;
    }

    public void run() {
        try {
            RunCurrentSession();
        } catch (Exception e) {

        }
    }

    private class MessageProcessor implements Runnable {
        private void Process() throws Exception{
            ArrayList<String> messages = systemQueue.TakeMessage(sessionID);
            for(String message : messages) {
                MessageDTO messageDTO = new MessageDTO(message);
                systemQueue.PutResponseDTO(sessionID,messageDTO);
            }
        }

        public void run() {
            try {
                Process();
            } catch (Exception e) {
            }
        }

    }
}


