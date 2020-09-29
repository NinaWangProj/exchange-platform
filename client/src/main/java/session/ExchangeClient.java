package session;


import commonData.DataType.MessageType;
import commonData.clearing.MarketParticipantPortfolio;
import javafx.util.Pair;
import marketData.MarketData;
import marketData.MarketDataWareHouse;
import commonData.DTO.*;
import commonData.DataType.MarketDataType;
import commonData.Order.Direction;
import commonData.Order.OrderDuration;
import commonData.Order.OrderType;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicLong;

public class ExchangeClient {

    private Socket clientSocket;
    private String serverIP;
    private int serverPort;
    private MarketDataWareHouse marketDataWareHouse;
    private ClientSession clientSession;
    private AtomicLong clientRequestID;
    private Boolean loggedIn;
    private Boolean hasAccount;

    public ExchangeClient() throws Exception{
        serverIP = InetAddress.getLocalHost().getHostAddress();
        serverPort = 58673;
        marketDataWareHouse = new MarketDataWareHouse();
        clientRequestID = new AtomicLong(0);
        hasAccount = false;
        loggedIn = false;
    }

    public boolean ConnectWithServer() {
        boolean connected;
        try{
            clientSocket = new Socket(serverIP,serverPort);
            connected = true;
        } catch (Exception exp){
            String meg = exp.getMessage();
            connected = false;
        }
        return connected;
    }

    public void SetupClient(OrderStatusEventHandler orderStatusObserver) throws Exception {
        //set up session to start reading from inputStream and process responses sent from server
        clientSession = new ClientSession(clientSocket, orderStatusObserver,marketDataWareHouse);
        clientSession.Start();
    }

    public Long SubmitMarketOrder(Direction direction, String tickerSymbol, int size, OrderDuration orderDuration) throws Exception{
        if(!loggedIn) {
            throw new Exception("Please login.");
        }
        Long requestID = clientRequestID.incrementAndGet();

        OrderDTO marketOrder = new OrderDTO(requestID, direction, OrderType.MARKETORDER,tickerSymbol,size,-1,orderDuration);
        TransmitRequestDTOToServer(marketOrder);

        return requestID;
    }

    public Long SubmitLimitOrder(Direction direction, String tickerSymbol, int size, double price,
                                 OrderDuration orderDuration) throws Exception {
        if(!loggedIn) {
            throw new Exception("Please login.");
        }
        Long requestID = clientRequestID.incrementAndGet();
        OrderDTO limitOrder = new OrderDTO(requestID,direction,OrderType.LIMITORDER,tickerSymbol,size,price,orderDuration);
        TransmitRequestDTOToServer(limitOrder);

        return requestID;
    }

    public MarketData SubmitMarketDataRequest(MarketDataType dataType, String tickerSymbol) throws Exception {
        if(!loggedIn) {
            throw new Exception("Please login.");
        }
        MarketData marketData = marketDataWareHouse.getMarketData(tickerSymbol);

        if(marketData == null || !marketData.getContinuousLevel3DataFlag()) {
            Object sharedMonitor = new Object();
            Long requestID = clientRequestID.incrementAndGet();

            clientSession.AttachMonitor(requestID,sharedMonitor);
            MarketDataRequestDTO marketDataRequestDTO = null;

            switch (dataType) {
                case Level1:
                    marketDataRequestDTO = new MarketDataRequestDTO(requestID,tickerSymbol,
                            MarketDataType.Level1);

                    break;
                case Level3:
                    marketDataRequestDTO = new MarketDataRequestDTO(requestID,tickerSymbol,
                            MarketDataType.Level3);
                    break;
            }
            TransmitRequestDTOToServer(marketDataRequestDTO);

            synchronized (sharedMonitor) {
                sharedMonitor.wait();
            }

            marketData = marketDataWareHouse.getMarketData(tickerSymbol);
            clientSession.RemoveMonitor(requestID);
        }

        return marketData;
    }

    public String SubmitContinuousMarketDataRequest(String tickerSymbol) throws Exception {
        if(!loggedIn) {
            throw new Exception("Please login.");
        }
        Long requestID = clientRequestID.incrementAndGet();
        MarketDataRequestDTO requestDTO = new MarketDataRequestDTO(requestID,tickerSymbol,MarketDataType.ContinuousLevel3);
        TransmitRequestDTOToServer(requestDTO);

        return "Countinous level 3 market data request successfully submitted";
    }

    public MarketParticipantPortfolio SubmitPortfolioDataRequest() throws Exception {
        //synchronous method
        if(!loggedIn) {
            throw new Exception("Please login.");
        }
        Object sharedMonitor = new Object();
        Long requestID = clientRequestID.incrementAndGet();
        clientSession.AttachMonitor(requestID,sharedMonitor);

        PortfolioRequestDTO requestDTO = new PortfolioRequestDTO(requestID);
        TransmitRequestDTOToServer(requestDTO);

        synchronized (sharedMonitor) {
            sharedMonitor.wait();
        }

        MarketParticipantPortfolio portfolio = clientSession.GetPortfolio(requestID);
        clientSession.RemovePortfolio(requestID);
        clientSession.RemoveMonitor(requestID);

        return portfolio;
    }

    public Boolean SubmitLoginRequest(String userName, String password) throws Exception {
        //synchronous method
        if(!hasAccount) {
            throw new Exception("sorry, you currently do not have an account. Please create an account " +
                    "first, and try loggin in again.");
        }
        Object sharedMonitor = new Object();
        Long requestID = clientRequestID.incrementAndGet();
        clientSession.AttachMonitor(requestID,sharedMonitor);

        LoginDTO loginDTO = new LoginDTO(requestID,userName,password);
        TransmitRequestDTOToServer(loginDTO);

        synchronized (sharedMonitor) {
            sharedMonitor.wait();
        }

        Pair<MessageType, String> message = clientSession.GetMessage(requestID);
        MessageType msgType = message.getKey();
        String msg = message.getValue();

        switch (msgType) {
            case SuccessMessage:
                loggedIn = true;
                clientSession.RemoveMessage(requestID);
                clientSession.RemoveMonitor(requestID);
                break;

            case ErrorMessage:
                clientSession.RemoveMessage(requestID);
                clientSession.RemoveMonitor(requestID);
                throw new Exception(msg);
        }

        return loggedIn;
    }

    public Boolean SubmitOpenAcctRequest(String userName, String password) throws Exception {
        //synchronous method
        Object sharedMonitor = new Object();
        Long requestID = clientRequestID.incrementAndGet();
        clientSession.AttachMonitor(requestID,sharedMonitor);

        OpenAcctDTO openAcctDTO = new OpenAcctDTO(requestID,userName,password);
        TransmitRequestDTOToServer(openAcctDTO);

        synchronized (sharedMonitor) {
            sharedMonitor.wait();
        }

        Pair<MessageType, String> message = clientSession.GetMessage(requestID);
        MessageType msgType = message.getKey();
        String msg = message.getValue();

        switch (msgType) {
            case SuccessMessage:
                hasAccount = true;
                clientSession.RemoveMessage(requestID);
                clientSession.RemoveMonitor(requestID);
                break;

            case ErrorMessage:
                clientSession.RemoveMessage(requestID);
                clientSession.RemoveMonitor(requestID);
                throw new Exception(msg);
        }

        return hasAccount;
    }

    private void TransmitRequestDTOToServer(Transferable DTO) throws Exception {
        byte[] orderDTOByteArray = DTO.Serialize();

        try {
            OutputStream outputStream = clientSocket.getOutputStream();
            //write header for the DTO: DTO type(1 byte); DTO size (1 byte);
            DTOType type = DTO.getDtoType();
            outputStream.write(type.getByteValue());
            outputStream.write((byte)orderDTOByteArray.length);
            outputStream.write(orderDTOByteArray);
        } catch(IOException E) {
        }

    }
}
