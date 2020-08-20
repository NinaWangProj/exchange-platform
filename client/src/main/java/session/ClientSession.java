package session;

import commonData.DTO.*;
import commonData.limitOrderBook.ChangeOperation;
import marketData.MarketDataWareHouse;
import commonData.DataType.OrderStatusType;

import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ClientSession implements Runnable {
    private Socket clientSocket;
    private MarketDataWareHouse marketDataWareHouse;
    private OrderStatusEventHandler orderStatusObserver;
    private ConcurrentHashMap<Long,Object> requestIDMonitorMap;
    private ConcurrentHashMap<Long,PortfolioDTO> requestIDPortfolioMap;

    public ClientSession(Socket clientSocket, OrderStatusEventHandler orderStatusObserver) {
        this.clientSocket = clientSocket;
        this.orderStatusObserver = orderStatusObserver;
        requestIDMonitorMap = new ConcurrentHashMap<Long,Object>();
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
                monitor.notifyAll();
                break;
            case BookChanges:
                BookChangeDTO bookChangeDTO = (BookChangeDTO) DTO;
                String bookChangeTickerSymbol = bookChangeDTO.getTickerSymbol();
                List<ChangeOperation> bookChanges = bookChangeDTO.getBookChanges();
                marketDataWareHouse.applyBookChanges(bookChangeTickerSymbol, bookChanges);
                break;
            case Message:
                MessageDTO messageDTO = (MessageDTO) DTO;
                String message = messageDTO.getMessage();
                OrderStatusType msgType = messageDTO.getMsgType();
                long requestID = messageDTO.getClientRequestID();
                orderStatusObserver.On_ReceiveOrderStatusChange(requestID, msgType, message);
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

    public PortfolioDTO GetPortfolio(long requestID) {
        return requestIDPortfolioMap.get(requestID);
    }

    public void run() {
        try {
            Start();
        } catch (Exception e) {
        }
    }
}
