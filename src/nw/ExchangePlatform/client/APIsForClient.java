package nw.ExchangePlatform.client;

import nw.ExchangePlatform.data.Direction;
import nw.ExchangePlatform.data.MarketParticipantOrder;
import nw.ExchangePlatform.data.OrderDuration;
import nw.ExchangePlatform.data.OrderType;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
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
        //byte[] order = new byte[10] {(byte)userID, (byte)orderID, , (byte)direction, (byte)tickerSymbol,
                //(byte)size, (byte)price, (byte)orderType, (byte)orderDuration};
        try {
            //clientSocket.getOutputStream().write
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
