package nw.ExchangePlatform.server.session;

import javafx.util.Pair;
import nw.ExchangePlatform.clearing.data.CredentialWareHouse;
import nw.ExchangePlatform.commonData.DTO.*;
import nw.ExchangePlatform.commonData.ServerQueue;
import nw.ExchangePlatform.commonData.MarketDataType;
import nw.ExchangePlatform.trading.limitOrderBook.BookOperation;
import nw.ExchangePlatform.trading.limitOrderBook.LimitOrderBookWareHouse;
import nw.ExchangePlatform.trading.limitOrderBook.sortedOrderList;
import nw.ExchangePlatform.trading.data.MarketParticipantOrder;
import nw.ExchangePlatform.utility.BinSelector;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;

public class Session implements Runnable {
    private final int sessionID;
    private final Socket clientSocket;
    private int clientUserID;
    private String clientUserName;
    private ServerQueue serverQueue;
    private CredentialWareHouse credentialWareHouse;
    private LimitOrderBookWareHouse limitOrderBookWareHouse;
    private ConcurrentHashMap<String,ReadWriteLock> locks;
    public static AtomicInteger currentAvailableOrderID;

    static {
        currentAvailableOrderID = null;
    }
    public Session(Socket clientSocket, int sessionID, ServerQueue serverQueue, int baseOrderID,
                   CredentialWareHouse credentialWareHouse, LimitOrderBookWareHouse limitOrderBookWareHouse,ConcurrentHashMap<String,ReadWriteLock> locks) {
        this.clientSocket = clientSocket;
        this.sessionID = sessionID;
        this.serverQueue = serverQueue;
        this.credentialWareHouse = credentialWareHouse;
        this.limitOrderBookWareHouse = limitOrderBookWareHouse;
        this.locks = locks;
        if(currentAvailableOrderID == null) {
            currentAvailableOrderID = new AtomicInteger(baseOrderID);
        }
    }

    private void RunCurrentSession() throws Exception{
        serverQueue.RegisterSessionWithQueue(sessionID);

        ClientRequestProcessor reader = new ClientRequestProcessor(clientSocket.getInputStream());
        reader.Attach(this);
        Thread readerThread = new Thread(reader);
        readerThread.start();

        MessageProcessor messageProcessor = new MessageProcessor();
        Thread messageThread = new Thread(messageProcessor);
        messageThread.start();

        ClientResponseProcessor writer = new ClientResponseProcessor(clientSocket.getOutputStream(), serverQueue,sessionID);
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
            serverQueue.PutOrder(order);
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
            MarketDataRequestDTO marketDataRequestDTO = (MarketDataRequestDTO)DTO;
            String tickerSymbol = marketDataRequestDTO.getTickerSymbol();
            MarketDataType type = marketDataRequestDTO.getDataType();
            boolean found = limitOrderBookWareHouse.ValidateRequest(tickerSymbol);
            if(found) {
                Pair<sortedOrderList, sortedOrderList> marketData;

                ReadWriteLock lock = locks.get(tickerSymbol);
                lock.readLock().lock();
                try{
                    marketData = limitOrderBookWareHouse.GetLimitOrderBook(tickerSymbol,type,this);
                } finally {
                    lock.readLock().unlock();
                }

                if(marketData.getKey().size() > 0 || marketData.getValue().size() > 0) {
                    //Create market data dto and put into queue
                    MarketDataDTO marketDataDTO = new MarketDataDTO(tickerSymbol, marketData);
                    serverQueue.PutResponseDTO(sessionID,marketDataDTO);
                }
            } else {
                //send message back to client, no market data for the request ticker symbol is found
            }
        }
    }

    public void On_ReceivingLevel3DataChanges(String tickerSymbol, List<Pair<BookOperation, Object[]>> bookchanges) throws Exception {
        //Create change DTO and put into queue
        BookChangesDTO DTO = new BookChangesDTO(tickerSymbol,bookchanges);
        serverQueue.PutResponseDTO(sessionID, DTO);
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
            ArrayList<String> messages = serverQueue.TakeMessage(sessionID);
            for(String message : messages) {
                MessageDTO messageDTO = new MessageDTO(message);
                serverQueue.PutResponseDTO(sessionID,messageDTO);
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


