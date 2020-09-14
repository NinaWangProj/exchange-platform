package session;

import common.utility.MessageGenerator;
import commonData.DataType.MessageType;
import commonData.marketData.MarketDataItem;
import javafx.util.Pair;
import clearing.data.CredentialWareHouse;
import commonData.clearing.MarketParticipantPortfolio;
import commonData.DTO.*;
import commonData.DataType.OrderStatusType;
import common.ServerQueue;
import commonData.DataType.MarketDataType;
import common.LimitOrderBookWareHouse;
import common.SortedOrderList;
import commonData.Order.MarketParticipantOrder;
import common.OrderStatus;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;

public class Session {
    private final int sessionID;
    private final Socket clientSocket;
    private int clientUserID;
    private String clientUserName;
    private ServerQueue serverQueue;
    private CredentialWareHouse credentialWareHouse;
    private LimitOrderBookWareHouse limitOrderBookWareHouse;
    private HashMap<Integer, MarketParticipantPortfolio> portfolioHashMap;
    //private MarketParticipantPortfolio clientPortfolio;
    private ConcurrentHashMap<String,ReadWriteLock> locks;
    private HashMap<Integer,Long> orderIDRequestIDMap;

    public static AtomicInteger currentAvailableOrderID;

    static {
        currentAvailableOrderID = null;
    }

    public Session(Socket clientSocket, int sessionID, ServerQueue serverQueue, int baseOrderID,
                   CredentialWareHouse credentialWareHouse, LimitOrderBookWareHouse limitOrderBookWareHouse,
                   ConcurrentHashMap<String,ReadWriteLock> locks, HashMap<Integer, MarketParticipantPortfolio> portfolioHashMap) {
        this.clientSocket = clientSocket;
        this.sessionID = sessionID;
        this.serverQueue = serverQueue;
        this.credentialWareHouse = credentialWareHouse;
        this.limitOrderBookWareHouse = limitOrderBookWareHouse;
        this.locks = locks;
        this.portfolioHashMap = portfolioHashMap;
        this.orderIDRequestIDMap = new HashMap<Integer,Long>();
        if(currentAvailableOrderID == null) {
            currentAvailableOrderID = new AtomicInteger(baseOrderID);
        }
    }

    public void RunCurrentSession() throws Exception{
        serverQueue.RegisterSessionWithQueue(sessionID);

        ClientRequestProcessor reader = new ClientRequestProcessor(clientSocket.getInputStream());
        reader.Attach(this);
        Thread readerThread = new Thread(reader);
        readerThread.start();

        OrderStatusProcessor orderStatusProcessor = new OrderStatusProcessor();
        Thread messageThread = new Thread(orderStatusProcessor);
        messageThread.start();

        ClientResponseProcessor writer = new ClientResponseProcessor(clientSocket.getOutputStream(), serverQueue,sessionID);
        Thread writerThread = new Thread(writer);
        writerThread.start();
    }

    public void On_ReceivingDTO(Transferable DTO) throws Exception {
        DTOType type = DTO.getDtoType();
        MessageType msgType = null;
        String statusMessage = "";
        MessageDTO msgDTO = null;
        switch (type) {
            case Order:
                OrderDTO orderDTO = (OrderDTO)DTO;
                int orderID = currentAvailableOrderID.getAndIncrement();
                orderIDRequestIDMap.put(orderID, orderDTO.getClientRequestID());
                MarketParticipantOrder order = new MarketParticipantOrder(sessionID,clientUserID,clientUserName, orderID,
                        new Date(),orderDTO.getDirection(),orderDTO.getTickerSymbol(),orderDTO.getSize(),orderDTO.getPrice(),
                        orderDTO.getOrderType(),orderDTO.getOrderDuration());
                serverQueue.PutOrder(order);
                break;

            case OpenAcctRequest:
                OpenAcctDTO openAcctDTO = (OpenAcctDTO)DTO;
                String userName = openAcctDTO.getUserName();
                String password = openAcctDTO.getPassword();
                boolean success = credentialWareHouse.CreateAccount(userName,password);
                if(success) {
                    clientUserName = userName;
                    clientUserID = credentialWareHouse.GetUserID(userName);

                    //set up portfolio for client when they open an account
                    MarketParticipantPortfolio portfolio = new MarketParticipantPortfolio();
                    portfolioHashMap.put(clientUserID,portfolio);
                    msgType = MessageType.SuccessMessage;
                } else {
                    msgType = MessageType.ErrorMessage;
                }
                statusMessage = MessageGenerator.GenerateStatusMessage(msgType, openAcctDTO.getDtoType());
                msgDTO = new MessageDTO(openAcctDTO.getClientRequestID(), msgType,statusMessage);
                serverQueue.PutResponseDTO(sessionID,msgDTO);
                break;

            case LoginRequest:
                LoginDTO loginDTO = (LoginDTO)DTO;
                String loginUserName = loginDTO.getUserName();
                String loginPassword = loginDTO.getPassword();
                boolean loginSuccessful = credentialWareHouse.ValidateLogin(loginUserName,loginPassword);
                if(loginSuccessful) {
                    clientUserName = loginUserName;
                    clientUserID = credentialWareHouse.GetUserID(loginUserName);
                    msgType = MessageType.SuccessMessage;
                } else {
                    msgType = MessageType.ErrorMessage;
                }
                statusMessage = MessageGenerator.GenerateStatusMessage(msgType, loginDTO.getDtoType());
                msgDTO = new MessageDTO(loginDTO.getClientRequestID(), msgType,statusMessage);
                serverQueue.PutResponseDTO(sessionID,msgDTO);
                break;

            case DepositRequest:
                DepositDTO depositDTO = (DepositDTO)DTO;
                double cashAmt = depositDTO.getCashAmount();
                portfolioHashMap.get(clientUserID).DepositCash(cashAmt);
                String message = cashAmt + " dollars have been successfully deposited into your account";
                OrderStatusDTO orderStatusDTO = new OrderStatusDTO(depositDTO.getClientRequestID(), OrderStatusType.Deposit, message);
                serverQueue.PutResponseDTO(sessionID, orderStatusDTO);
                break;

            case PortfolioRequest:
                PortfolioRequestDTO portfolioRequestDTO = (PortfolioRequestDTO)DTO;
                PortfolioDTO portfolioDTO = new PortfolioDTO(portfolioRequestDTO.getClientRequestID(),
                        portfolioHashMap.get(clientUserID).getSecurities(),portfolioHashMap.get(clientUserID).getCashAmt());
                serverQueue.PutResponseDTO(sessionID,portfolioDTO);
                break;

            case MarketDataRequest:
                MarketDataRequestDTO marketDataRequestDTO = (MarketDataRequestDTO)DTO;
                String tickerSymbol = marketDataRequestDTO.getTickerSymbol();
                MarketDataType marketDataType = marketDataRequestDTO.getDataType();
                boolean found = limitOrderBookWareHouse.ValidateRequest(tickerSymbol);
                if(found) {
                    Pair<SortedOrderList, SortedOrderList> limitOrderBook;

                    ReadWriteLock lock = locks.get(tickerSymbol);
                    lock.readLock().lock();
                    try{
                        limitOrderBook = limitOrderBookWareHouse.GetLimitOrderBook(tickerSymbol,marketDataType,this);
                    } finally {
                        lock.readLock().unlock();
                    }

                    if(limitOrderBook.getKey().size() > 0 || limitOrderBook.getValue().size() > 0) {
                        //Create market data dto and put into queue
                        Pair<ArrayList<MarketDataItem>,ArrayList<MarketDataItem>> marketData =
                                FormMarketDataFromOrderBook(limitOrderBook);
                        MarketDataDTO marketDataDTO = new MarketDataDTO(marketDataRequestDTO.getClientRequestID(),
                                tickerSymbol, marketData.getKey(),marketData.getValue());
                        serverQueue.PutResponseDTO(sessionID,marketDataDTO);
                    }
                } else {
                    //send message back to client, no market data for the request ticker symbol is found
                }
                break;
        }
    }

    private Pair<ArrayList<MarketDataItem>,ArrayList<MarketDataItem>> FormMarketDataFromOrderBook(
        Pair<SortedOrderList, SortedOrderList> limitOrderBook) {
        ArrayList<MarketDataItem> bids = new ArrayList<MarketDataItem>();
        ArrayList<MarketDataItem> asks = new ArrayList<MarketDataItem>();

        for(MarketParticipantOrder order : limitOrderBook.getKey().getSortedList()) {
            MarketDataItem item = new MarketDataItem(order.getTickerSymbol(),order.getSize(),order.getPrice());
            bids.add(item);
        }

        for(MarketParticipantOrder order : limitOrderBook.getValue().getSortedList()) {
            MarketDataItem item = new MarketDataItem(order.getTickerSymbol(),order.getSize(),order.getPrice());
            asks.add(item);
        }

        return new Pair<>(bids,asks);
    }

    public void On_ReceivingLevel3DataChanges(BookChangeDTO bookChangeDTO) throws Exception {
        //place book change dto to queue:
        serverQueue.PutResponseDTO(sessionID, bookChangeDTO);
    }

    public HashMap<Integer, MarketParticipantPortfolio> getPortfolioHashMap() {
        return portfolioHashMap;
    }

    public MarketParticipantPortfolio getClientPortfolio() {
        return portfolioHashMap.get(clientUserID);
    }

    public int getClientUserID() {
        return clientUserID;
    }

    public String getClientUserName() {
        return clientUserName;
    }

    private class OrderStatusProcessor implements Runnable {
        private void Process() throws Exception{
            OrderStatus orderStatus = serverQueue.TakeOrderStatus(sessionID);
            Long requestID = orderIDRequestIDMap.get(orderStatus.getOrderID());
            for(String statusMessage : orderStatus.getStatusMessages()) {
                OrderStatusDTO orderStatusDTO = new OrderStatusDTO(requestID,orderStatus.getMsgType(),statusMessage);
                serverQueue.PutResponseDTO(sessionID, orderStatusDTO);
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


