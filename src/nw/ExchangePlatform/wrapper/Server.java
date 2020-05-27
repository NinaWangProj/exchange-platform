package nw.ExchangePlatform.wrapper;

import nw.ExchangePlatform.data.Direction;
import nw.ExchangePlatform.data.MarketParticipantOrder;
import nw.ExchangePlatform.data.OrderDuration;
import nw.ExchangePlatform.data.OrderType;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Date;

public class Server {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private int serverPortID;

    public void StartListening() throws IOException{
        serverSocket = new ServerSocket(serverPortID);
        //start listening for client; once heard client, hand shake with client to establish connection
        clientSocket = serverSocket.accept();
    }

    public Server() {
        serverPortID = 58673;
    }

    public void StartWorking() throws IOException{
        InputStream inputStream = clientSocket.getInputStream();
        byte[] order = new byte[45];
        while(inputStream.read(order) != -1) {
            MarketParticipantOrder marketParticipantOrder = Deserialize(order);
        }
    }

    private MarketParticipantOrder Deserialize(byte[] order) {

        byte[] userIDByte = new byte[4];
        byte[] nameByte = new byte[8];
        byte[] orderIDByte = new byte[4];
        byte[] timeByte = new byte[8];
        byte[] directionByte = new byte[8];
        byte[] tickerSymbolByte = new byte[8];
        byte[] sizeByte = new byte[4];
        byte[] priceByte = new byte[8];
        byte[] orderTypeByte = new byte[1];
        byte[] orderDurationByte = new byte[1];

        System.arraycopy(order,0,userIDByte,0,4);
        System.arraycopy(order,4,nameByte,0,8);
        System.arraycopy(order,12,orderIDByte,0,4);
        System.arraycopy(order,16,timeByte,0,8);
        System.arraycopy(order,24,directionByte,0,8);
        System.arraycopy(order,25,tickerSymbolByte,0,8);
        System.arraycopy(order,32,sizeByte,0,4);
        System.arraycopy(order,36,priceByte,0,8);
        System.arraycopy(order,44,orderTypeByte,0,1);
        System.arraycopy(order,45,orderDurationByte,0,1);

        int userID = ByteBuffer.wrap(userIDByte).getInt();
        String name = new String(nameByte);
        int orderID = ByteBuffer.wrap(userIDByte).getInt();
        Date time = new Date(ByteBuffer.wrap(userIDByte).getInt());
        int directionInt = ByteBuffer.wrap(directionByte).getInt();
        Direction direction = Direction.SELL;
        if(directionInt == 1)
            direction = Direction.BUY;
        String tickerSymbol = new String(tickerSymbolByte);
        int size = ByteBuffer.wrap(sizeByte).getInt();
        double price = ByteBuffer.wrap(sizeByte).getDouble();
        OrderType orderType = OrderType.LIMITORDER;
        if(ByteBuffer.wrap(orderTypeByte).getInt() ==1)
            orderType = OrderType.MARKETORDER;
        OrderDuration orderDuration = OrderDuration.DAY;
        if(ByteBuffer.wrap(orderDurationByte).getInt() == 2)
            orderDuration = OrderDuration.GTC;

        MarketParticipantOrder marketParticipantOrder = new MarketParticipantOrder(userID,name,orderID,time,direction,
                tickerSymbol,size,price,orderType,orderDuration);

        return marketParticipantOrder;
    }


}
