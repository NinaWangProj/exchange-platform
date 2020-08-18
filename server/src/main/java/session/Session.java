package session;

import commonData.limitOrderBook.BookOperation;
import commonData.marketData.MarketDataItem;
import javafx.util.Pair;
import clearing.data.CredentialWareHouse;
import commonData.clearing.MarketParticipantPortfolio;
import commonData.DTO.*;
import commonData.DataType.OrderStatusType;
import server.common.ServerQueue;
import commonData.DataType.MarketDataType;
import server.common.LimitOrderBookWareHouse;
import server.common.sortedOrderList;
import commonData.Order.MarketParticipantOrder;
import server.common.OrderStatus;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
    private HashMap<Integer, MarketParticipantPortfolio> portfolioHashMap;
    private MarketParticipantPortfolio clientPortfolio;
    private ConcurrentHashMap<String,ReadWriteLock> locks;
    private HashMap<Integer,Long> orderIDRequestIDMAP;

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

        OrderStatusProcessor orderStatusProcessor = new OrderStatusProcessor();
        Thread messageThread = new Thread(orderStatusProcessor);
        messageThread.start();

        ClientResponseProcessor writer = new ClientResponseProcessor(clientSocket.getOutputStream(), serverQueue,sessionID);
        Thread writerThread = new Thread(writer);
        writerThread.start();
    }

    public void On_ReceivingDTO(Transferable DTO) throws Exception {
        if(Class.forName("OrderDTO").isInstance(DTO)) {
            OrderDTO orderDTO = (OrderDTO)DTO;
            int orderID = currentAvailableOrderID.getAndIncrement();
            orderIDRequestIDMAP.put(orderID, orderDTO.getClientRequestID());
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

                //set up portfolio for client when they open an account
                MarketParticipantPortfolio portfolio = new MarketParticipantPortfolio();
                portfolioHashMap.put(clientUserID,portfolio);
                clientPortfolio = portfolio;
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

        if(Class.forName("DepositDTO").isInstance(DTO)) {
            DepositDTO depositDTO = (DepositDTO)DTO;
            double cashAmt = depositDTO.getCashAmount();
            clientPortfolio.DepositCash(cashAmt);
            String message = cashAmt + " dollars have been successfully deposited into your account";
            MessageDTO messageDTO = new MessageDTO(depositDTO.getClientRequestID(), OrderStatusType.Deposit, message);
            serverQueue.PutResponseDTO(sessionID,messageDTO);
        }

        if(Class.forName("PortfolioRequestDTO").isInstance(DTO)) {
            PortfolioRequestDTO portfolioRequestDTO = (PortfolioRequestDTO)DTO;
            PortfolioDTO portfolioDTO = new PortfolioDTO(portfolioRequestDTO.getClientRequestID(),clientPortfolio.getSecurities(),clientPortfolio.getCashAmt());
            serverQueue.PutResponseDTO(sessionID,portfolioDTO);
        }

        if(Class.forName("MareketDataRequestDTO").isInstance(DTO)) {
            MarketDataRequestDTO marketDataRequestDTO = (MarketDataRequestDTO)DTO;
            String tickerSymbol = marketDataRequestDTO.getTickerSymbol();
            MarketDataType type = marketDataRequestDTO.getDataType();
            boolean found = limitOrderBookWareHouse.ValidateRequest(tickerSymbol);
            if(found) {
                Pair<sortedOrderList, sortedOrderList> limitOrderBook;

                ReadWriteLock lock = locks.get(tickerSymbol);
                lock.readLock().lock();
                try{
                    limitOrderBook = limitOrderBookWareHouse.GetLimitOrderBook(tickerSymbol,type,this);
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
        }
    }

    private Pair<ArrayList<MarketDataItem>,ArrayList<MarketDataItem>> FormMarketDataFromOrderBook(
        Pair<sortedOrderList,sortedOrderList> limitOrderBook) {
        ArrayList<MarketDataItem> bids = new ArrayList<MarketDataItem>();
        ArrayList<MarketDataItem> asks = new ArrayList<MarketDataItem>();

        for(MarketParticipantOrder order : limitOrderBook.getKey().sortedList) {
            MarketDataItem item = new MarketDataItem(order.getTickerSymbol(),order.getSize(),order.getPrice());
            bids.add(item);
        }

        for(MarketParticipantOrder order : limitOrderBook.getValue().sortedList) {
            MarketDataItem item = new MarketDataItem(order.getTickerSymbol(),order.getSize(),order.getPrice());
            asks.add(item);
        }

        return new Pair<>(bids,asks);
    }

    public void On_ReceivingLevel3DataChanges(String tickerSymbol, List<Pair<BookOperation, Object[]>> bookchanges) throws Exception {
        //Create change DTO and put into queue
        BookChangeDTO DTO = new BookChangeDTO(tickerSymbol,bookchanges);
        serverQueue.PutResponseDTO(sessionID, DTO);
    }

    public MessageDTO CreateMessageDTOForWriter(Long requestID, OrderStatusType msgType, String messages) {
        MessageDTO messageDTO = new MessageDTO(requestID, msgType, messages);
        return messageDTO;
    }

    public void run() {
        try {
            RunCurrentSession();
        } catch (Exception e) {

        }
    }

    private class OrderStatusProcessor implements Runnable {
        private void Process() throws Exception{
            OrderStatus orderStatus = serverQueue.TakeOrderStatus(sessionID);
            Long requestID = orderIDRequestIDMAP.get(orderStatus.getOrderID());
            for(String statusMessage : orderStatus.getStatusMessages()) {
                MessageDTO messageDTO = new MessageDTO(requestID,orderStatus.getMsgType(),statusMessage);
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


