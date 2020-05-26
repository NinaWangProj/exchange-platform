package nw.ExchangePlatform.client;

import nw.ExchangePlatform.data.Direction;
import nw.ExchangePlatform.data.MarketParticipantOrder;
import nw.ExchangePlatform.data.OrderDuration;
import nw.ExchangePlatform.data.OrderType;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

public class APIsForClient {

    private Socket clientSocket;

    public void ConnectWithServer(String serverIP, int serverPort) {
        try{
            clientSocket = new Socket(serverIP,serverPort);
        } catch (Exception exp){
            String meg = exp.getMessage();
        }
    }

    public void SubmitOrder(int userID, String name, int orderID, Date time, Direction direction, String tickerSymbol, int size,
                            double price, OrderType orderType, OrderDuration orderDuration) {
        byte[] order = new byte[46];

        byte[] userIDByte = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putInt(userID).array();
        //fix name with 8 character max;
        byte[] nameByte = name.getBytes();
        byte[] orderIDByte = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putInt(orderID).array();
        //convert time to millisecond first and then to byte
        byte[] timeByte = ByteBuffer.allocate(8).order(ByteOrder.nativeOrder()).putInt(orderID).array();
        if(direction.equals(Direction.BUY)) {
            order[24] = (byte) 1;
        } else {
            order[24] = (byte) 2;
        }
        byte[] tickerSymbolByte = tickerSymbol.getBytes();
        byte[] sizeByte = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putInt(size).array();
        byte[] priceByte = ByteBuffer.allocate(8).order(ByteOrder.nativeOrder()).putDouble(price).array();
        if(orderType.equals(OrderType.MARKETORDER)) {
            order[44] = (byte) 1;
        } else {
            order[44] = (byte) 2;
        }
        if(orderDuration.equals(OrderDuration.DAY)) {
            order[45] = (byte) 1;
        } else {
            order[45] = (byte) 2;
        }
        System.arraycopy(userIDByte,0,order,0,4);
        System.arraycopy(nameByte,0,order,4,8);
        System.arraycopy(orderIDByte,0,order,12,4);
        System.arraycopy(timeByte,0,order,16,8);
        System.arraycopy(tickerSymbolByte,0,order,25,8);
        System.arraycopy(sizeByte,0,order,32,4);
        System.arraycopy(priceByte,0,order,36,8);


        try {
            clientSocket.getOutputStream().write(order);
        } catch (Exception e) {
            String meg = e.getMessage();
        }
    }

    public void GetMarketData() {

    }

    public void GetMyPortfolioInfo() {

    }

    public void CloseConnection() {

    }
}
