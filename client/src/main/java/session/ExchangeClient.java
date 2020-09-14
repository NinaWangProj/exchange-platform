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

    public ExchangeClient() {
        serverIP = "192.168.0.20";
        serverPort = 58673;
        marketDataWareHouse = new MarketDataWareHouse();
        clientRequestID = new AtomicLong(0);
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
        Long requestID = clientRequestID.incrementAndGet();

        OrderDTO marketOrder = new OrderDTO(requestID, direction, OrderType.MARKETORDER,tickerSymbol,size,-1,orderDuration);
        TransmitRequestDTOToServer(marketOrder);

        return requestID;
    }

    public Long SubmitLimitOrder(Direction direction, String tickerSymbol, int size, double price,
                                 OrderDuration orderDuration) throws Exception {
        Long requestID = clientRequestID.incrementAndGet();
        OrderDTO limitOrder = new OrderDTO(requestID,direction,OrderType.LIMITORDER,tickerSymbol,size,price,orderDuration);
        TransmitRequestDTOToServer(limitOrder);

        return requestID;
    }

    public MarketData SubmitMarketDataRequest(MarketDataType dataType, String tickerSymbol) throws Exception {
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
        Long requestID = clientRequestID.incrementAndGet();
        MarketDataRequestDTO requestDTO = new MarketDataRequestDTO(requestID,tickerSymbol,MarketDataType.ContinuousLevel3);
        TransmitRequestDTOToServer(requestDTO);

        return "Countinous level 3 market data request successfully submitted";
    }

    public MarketParticipantPortfolio SubmitPortfolioDataRequest() throws Exception {
        //synchronous method
        Object sharedMonitor = new Object();
        Long requestID = clientRequestID.incrementAndGet();
        clientSession.AttachMonitor(requestID,sharedMonitor);

        PortfolioRequestDTO reqeustDTO = new PortfolioRequestDTO(requestID);
        TransmitRequestDTOToServer(reqeustDTO);

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
        Boolean loginSuccessful = false;
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
                loginSuccessful = true;
                clientSession.RemoveMessage(requestID);
                clientSession.RemoveMonitor(requestID);
                break;

            case ErrorMessage:
                clientSession.RemoveMessage(requestID);
                clientSession.RemoveMonitor(requestID);
                throw new Exception(msg);
        }

        return loginSuccessful;
    }

    public Boolean SubmitOpenAcctRequest(String userName, String password) throws Exception {
        //synchronous method
        Boolean successful = false;
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
                successful = true;
                clientSession.RemoveMessage(requestID);
                clientSession.RemoveMonitor(requestID);
                break;

            case ErrorMessage:
                clientSession.RemoveMessage(requestID);
                clientSession.RemoveMonitor(requestID);
                throw new Exception(msg);
        }

        return successful;
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

    public Socket getClientSocket() {
        return clientSocket;
    }

    public AtomicLong getClientRequestID() {
        return clientRequestID;
    }
}
