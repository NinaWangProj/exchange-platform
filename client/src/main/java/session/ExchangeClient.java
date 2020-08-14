package session;


import commonData.clearing.MarketParticipantPortfolio;
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

    public void SetupClient(OrderStatusEventHandler orderStatusObserver) {
        //set up session to start reading from inputStream and process responses sent from server
        clientSession = new ClientSession(clientSocket, orderStatusObserver);
        Thread sessionThread = new Thread(clientSession);
        sessionThread.start();
    }

    public Long SubmitMarketOrder(Direction direction, String tickerSymbol, int size, OrderDuration orderDuration) throws Exception{
        Long requestID = clientRequestID.incrementAndGet();

        OrderDTO marketOrder = new OrderDTO(requestID, direction, OrderType.MARKETORDER,tickerSymbol,size,-1,orderDuration);
        TransmitOrderDTO(marketOrder);

        return requestID;
    }

    public Long SubmitLimitOrder(Direction direction, String tickerSymbol, int size, double price,
                                 OrderDuration orderDuration) {
        Long requestID = clientRequestID.incrementAndGet();
        OrderDTO limitOrder = new OrderDTO(requestID,direction,OrderType.LIMITORDER,tickerSymbol,size,price,orderDuration);
        TransmitOrderDTO(limitOrder);

        return requestID;
    }

    public MarketData SubmitMarketDataRequest(MarketDataType dataType, String tickerSymbol) throws Exception {
        MarketData marketData = marketDataWareHouse.getMarketData(tickerSymbol);

        if(marketData == null || !marketData.getContinuousLevel3DataFlag()) {
            Object sharedMonitor = new Object();
            Long requestID = clientRequestID.incrementAndGet();

            clientSession.AttachMonitor(requestID,sharedMonitor);

            switch (dataType) {
                case Level1:
                    MarketDataRequestDTO level1DTO = new MarketDataRequestDTO(requestID,tickerSymbol,
                            MarketDataType.Level1);
                    TransmitMarketDataRequest(level1DTO);
                case Level3:
                    MarketDataRequestDTO level3DTO = new MarketDataRequestDTO(requestID,tickerSymbol,
                            MarketDataType.Level3);
                    TransmitMarketDataRequest(level3DTO);
            }

            sharedMonitor.wait();
            marketData = marketDataWareHouse.getMarketData(tickerSymbol);
            clientSession.RemoveMonitor(requestID);
        }

        return marketData;
    }

    public String SubmitContinuousMarketDataRequest(String tickerSymbol) throws Exception {
        Long requestID = clientRequestID.incrementAndGet();
        MarketDataRequestDTO requestDTO = new MarketDataRequestDTO(requestID,tickerSymbol,MarketDataType.ContinuousLevel3);
        TransmitMarketDataRequest(requestDTO);

        return "Countinous level 3 market data request successfully submitted";
    }

    public MarketParticipantPortfolio SubmitPortfolioDataRequest() throws Exception {
        //synchronous method
        Object sharedMonitor = new Object();
        Long requestID = clientRequestID.incrementAndGet();
        clientSession.AttachMonitor(requestID,sharedMonitor);

        PortfolioRequestDTO reqeustDTO = new PortfolioRequestDTO(requestID);
        TransmitPortfolioDataRequestDTO(reqeustDTO);

        sharedMonitor.wait();
        PortfolioDTO portfolioDTO = clientSession.GetPortfolio(requestID);
        clientSession.RemovePortfolio(requestID);
        clientSession.RemoveMonitor(requestID);

        MarketParticipantPortfolio portfolio = new MarketParticipantPortfolio(portfolioDTO.getSecurities(),portfolioDTO.getCash());

        return portfolio;
    }

    private void TransmitMarketDataRequest(MarketDataRequestDTO dataRequestDTO) throws Exception {
        byte[] dataRequestDTOByteArray = dataRequestDTO.Serialize();

        OutputStream outputStream = clientSocket.getOutputStream();
        DTOType type = DTOType.MareketDataRequest;
        outputStream.write(type.getByteValue());
        outputStream.write((byte)dataRequestDTOByteArray.length);
        outputStream.write(dataRequestDTOByteArray);
    }

    private void TransmitOrderDTO (OrderDTO orderDTO) {
        byte[] orderDTOByteArray = orderDTO.Serialize();
        try {
            OutputStream outputStream = clientSocket.getOutputStream();
            //write header for the DTO: DTO type(1 byte); DTO size (1 byte);
            DTOType type = DTOType.Order;
            outputStream.write(type.getByteValue());
            outputStream.write((byte)orderDTOByteArray.length);
            outputStream.write(orderDTOByteArray);
        } catch(IOException E) {
        }
    }

    private void TransmitPortfolioDataRequestDTO(PortfolioRequestDTO portfolioRequestDTO) throws Exception {
        byte[] portfolioRequestDTOByteArray = portfolioRequestDTO.Serialize();

        OutputStream outputStream = clientSocket.getOutputStream();
        DTOType type = DTOType.PortfolioRequest;
        outputStream.write(type.getByteValue());
        outputStream.write((byte)portfolioRequestDTOByteArray.length);
        outputStream.write(portfolioRequestDTOByteArray);
    }

}
