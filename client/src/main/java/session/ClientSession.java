package session;

import commonData.DTO.*;
import commonData.DataType.MessageType;
import commonData.Order.Direction;
import commonData.clearing.MarketParticipantPortfolio;
import commonData.limitOrderBook.ChangeOperation;
import commonData.marketData.MarketDataItem;
import javafx.util.Pair;
import marketData.MarketDataWareHouse;
import commonData.DataType.OrderStatusType;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ClientSession {
    private Socket clientSocket;
    private MarketDataWareHouse marketDataWareHouse;
    private OrderStatusEventHandler orderStatusObserver;
    private ConcurrentHashMap<Long,Object> requestIDMonitorMap;
    private ConcurrentHashMap<Long, Pair<MessageType, String>> requestIDMessageMap;
    private ConcurrentHashMap<Long,MarketParticipantPortfolio> requestIDPortfolioMap;

    public ClientSession(Socket clientSocket, OrderStatusEventHandler orderStatusObserver,MarketDataWareHouse marketDataWareHouse) {
        this.clientSocket = clientSocket;
        this.orderStatusObserver = orderStatusObserver;
        this.marketDataWareHouse = marketDataWareHouse;
        requestIDMonitorMap = new ConcurrentHashMap<>();
        requestIDPortfolioMap = new ConcurrentHashMap<>();
        requestIDMessageMap = new ConcurrentHashMap<>();
    }

    public void Start() throws Exception{
        ServerResponseProcessor reader = new ServerResponseProcessor(clientSocket.getInputStream());
        reader.Attach(this);
        Thread readerThread = new Thread(reader);
        readerThread.start();
    }

    public void On_ReceivingDTO(Transferable DTO) throws Exception{
        DTOType dtoType = DTO.getDtoType();

        switch (dtoType) {
            case MarketData:
                MarketDataDTO marketDataDTO = (MarketDataDTO) DTO;
                String marketDataTickerSymbol = marketDataDTO.getTickerSymbol();

                Boolean pass = ValidateMarketData(marketDataDTO);

                if(pass) {
                    marketDataWareHouse.setMarketData(marketDataTickerSymbol,
                            marketDataDTO.getBids(), marketDataDTO.getAsks());
                }

                Object monitor = requestIDMonitorMap.get(marketDataDTO.getClientRequestID());
                if (monitor != null) {
                    synchronized (monitor) {
                        monitor.notifyAll();
                    }
                }
                break;

            case BookChanges:
                BookChangeDTO bookChangeDTO = (BookChangeDTO) DTO;
                String tickerSymbol = bookChangeDTO.getTickerSymbol();
                List<ChangeOperation> bookChanges = bookChangeDTO.getBookChanges();
                Direction direction = bookChangeDTO.getDirection();
                marketDataWareHouse.applyBookChanges(tickerSymbol, direction, bookChanges);
                break;

            case OrderStatus:
                OrderStatusDTO orderStatusDTO = (OrderStatusDTO) DTO;
                String message = orderStatusDTO.getMessage();
                OrderStatusType msgType = orderStatusDTO.getStatusType();
                long requestID = orderStatusDTO.getClientRequestID();
                orderStatusObserver.On_ReceiveOrderStatusChange(requestID, msgType, message);
                break;

            case Portfolio:
                PortfolioDTO portfolioDTO = (PortfolioDTO) DTO;
                MarketParticipantPortfolio portfolio = new MarketParticipantPortfolio(portfolioDTO.getSecurities(),
                        portfolioDTO.getCash());
                requestIDPortfolioMap.put(portfolioDTO.getClientRequestID(),portfolio);
                Object monitorForPortfolioRequest = requestIDMonitorMap.get(portfolioDTO.getClientRequestID());

                synchronized (monitorForPortfolioRequest) {
                    if (monitorForPortfolioRequest != null)
                        monitorForPortfolioRequest.notifyAll();
                }
                break;

            case Message:
                MessageDTO messageDTO = (MessageDTO) DTO;
                requestIDMessageMap.put(messageDTO.getClientRequestID(), new Pair<MessageType,String>
                        (messageDTO.getMsgType(),messageDTO.getMessage()));
                Object messageMonitor = requestIDMonitorMap.get(messageDTO.getClientRequestID());

                synchronized (messageMonitor) {
                    if (messageMonitor != null)
                        messageMonitor.notifyAll();
                }
                break;
        }
    }

    public void AttachMonitor(Long clientRequestID, Object monitor) {
        requestIDMonitorMap.put(clientRequestID,monitor);
    }

    public void RemoveMonitor(long requestID) {
        requestIDMonitorMap.remove(requestID);
    }

    public void RemoveMessage(long requestID) {
        requestIDMessageMap.remove(requestID);
    }

    public void RemovePortfolio(long requestID) {
        requestIDPortfolioMap.remove(requestID);
    }

    public MarketParticipantPortfolio GetPortfolio(long requestID) {
        return requestIDPortfolioMap.get(requestID);
    }

    public Pair<MessageType, String> GetMessage(long requestID) {
        return requestIDMessageMap.get(requestID);
    }

    private boolean ValidateMarketData(MarketDataDTO marketDataDTO) {
        ArrayList<MarketDataItem> bids = marketDataDTO.getBids();
        ArrayList<MarketDataItem> asks = marketDataDTO.getAsks();
        Boolean pass = true;

        if((bids.size()==1 && bids.size() == -1) || (asks.size()==1 && asks.size()==-1)) {
            pass = false;
        }

        return pass;
    }
}
