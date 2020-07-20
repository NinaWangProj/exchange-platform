package nw.ExchangePlatform.client;

import nw.ExchangePlatform.commonData.DTO.MarketDataDTO;
import nw.ExchangePlatform.commonData.DTO.MessageDTO;
import nw.ExchangePlatform.commonData.DTO.Transferable;
import nw.ExchangePlatform.client.marketData.MarketDataWareHouse;

import java.net.Socket;

public class ClientSession implements Runnable {
    private Socket clientSocket;
    private MarketDataWareHouse marketDataWareHouse;
    private ClientQueue clientQueue;

    public ClientSession(Socket clientSocket, MarketDataWareHouse marketDataWareHouse, ClientQueue clientQueue) {
        this.clientSocket = clientSocket;
        this.marketDataWareHouse = marketDataWareHouse;
        this.clientQueue = clientQueue;
    }

    public void Start() throws Exception{
        ServerMessageProcessor reader = new ServerMessageProcessor(clientSocket.getInputStream());
        reader.Attach(this);
        Thread readerThread = new Thread(reader);
        readerThread.start();
    }

    public void On_ReceivingDTO(Transferable DTO) throws Exception{
        if(Class.forName("MarketDataDTO").isInstance(DTO)) {
            MarketDataDTO marketDataDTO = (MarketDataDTO)DTO;
            String tickerSymbol = marketDataDTO.getTickerSymbol();
            marketDataWareHouse.setMarketData(tickerSymbol,marketDataDTO.getBids(),marketDataDTO.getAsks());
        } else if (Class.forName("MessageDTO").isInstance(DTO)) {
            MessageDTO messageDTO = (MessageDTO)DTO;
            String message = messageDTO.getMessage();
            clientQueue.getServerMessages().add(message);
        }
    }

    public void run() {
        try {
            Start();
        } catch (Exception e) {
        }
    }
}
