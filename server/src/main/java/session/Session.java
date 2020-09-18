package session;

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
                boolean pass = credentialWareHouse.CreateAccount(userName,password);
                if(pass) {
                    clientUserName = userName;
                    clientUserID = credentialWareHouse.GetUserID(userName);

                    //set up portfolio for client when they open an account
                    MarketParticipantPortfolio portfolio = new MarketParticipantPortfolio();
                    portfolioHashMap.put(clientUserID,portfolio);
                    //clientPortfolio = portfolio;
                } else {
                    //send message back to client
                }
                break;
            case LoginRequest:
                LoginDTO loginDTO = (LoginDTO)DTO;
                String loginUserName = loginDTO.getUserName();
                String loginPassword = loginDTO.getPassword();
                boolean loginPass = credentialWareHouse.ValidateLogin(loginUserName,loginPassword);
                if(loginPass) {
                    clientUserName = loginUserName;
                    clientUserID = credentialWareHouse.GetUserID(loginUserName);
                } else {
                    //session needs to send client a message "wrong credential, please try again"
                    //implement later
                }
                break;
            case DepositRequest:
                DepositDTO depositDTO = (DepositDTO)DTO;
                double cashAmt = depositDTO.getCashAmount();
                portfolioHashMap.get(clientUserID).DepositCash(cashAmt);
                String message = cashAmt + " dollars have been successfully deposited into your account";
                MessageDTO messageDTO = new MessageDTO(depositDTO.getClientRequestID(), OrderStatusType.Deposit, message);
                serverQueue.PutResponseDTO(sessionID,messageDTO);
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

                //Create market data dto and put into queue
                Pair<ArrayList<MarketDataItem>,ArrayList<MarketDataItem>> marketData =
                        limitOrderBookWareHouse.GetLimitOrderBookCopy(tickerSymbol,marketDataType,this);

                MarketDataDTO marketDataDTO = new MarketDataDTO(marketDataRequestDTO.getClientRequestID(),
                        tickerSymbol, marketData.getKey(),marketData.getValue());
                serverQueue.PutResponseDTO(sessionID,marketDataDTO);
                break;
        }
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
            for(int i = 0; i < orderStatus.getStatusMessages().size(); i ++) {
                MessageDTO messageDTO = new MessageDTO(requestID,orderStatus.getMsgType().get(i),
                        orderStatus.getStatusMessages().get(i));
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


