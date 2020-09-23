package session;

import commonData.DTO.*;
import commonData.Order.Direction;
import commonData.clearing.MarketParticipantPortfolio;
import commonData.limitOrderBook.ChangeOperation;
import marketData.MarketDataWareHouse;
import commonData.DataType.OrderStatusType;

import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ClientSession {
    private Socket clientSocket;
    private MarketDataWareHouse marketDataWareHouse;
    private OrderStatusEventHandler orderStatusObserver;
    private ConcurrentHashMap<Long,Object> requestIDMonitorMap;
    private ConcurrentHashMap<Long,MarketParticipantPortfolio> requestIDPortfolioMap;

    public ClientSession(Socket clientSocket, OrderStatusEventHandler orderStatusObserver,MarketDataWareHouse marketDataWareHouse) {
        this.clientSocket = clientSocket;
        this.orderStatusObserver = orderStatusObserver;
        this.marketDataWareHouse = marketDataWareHouse;
        requestIDMonitorMap = new ConcurrentHashMap<Long,Object>();
        requestIDPortfolioMap = new ConcurrentHashMap<>();
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
                marketDataWareHouse.setMarketData(marketDataTickerSymbol, marketDataDTO.getBids(), marketDataDTO.getAsks());

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
            case Message:
                MessageDTO messageDTO = (MessageDTO) DTO;
                String message = messageDTO.getMessage();
                OrderStatusType msgType = messageDTO.getMsgType();
                long requestID = messageDTO.getClientRequestID();
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
        }
    }

    public void AttachMonitor(Long clientRequestID, Object monitor) {
        requestIDMonitorMap.put(clientRequestID,monitor);
    }

    public void RemoveMonitor(long requestID) {
        requestIDMonitorMap.remove(requestID);
    }

    public void RemovePortfolio(long requestID) {
        requestIDPortfolioMap.remove(requestID);
    }

    public MarketParticipantPortfolio GetPortfolio(long requestID) {
        return requestIDPortfolioMap.get(requestID);
    }
}
