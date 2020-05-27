package nw.ExchangePlatform.client;

import nw.ExchangePlatform.data.*;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

public class ExchangeClient {

    private Socket clientSocket;

    public void ConnectWithServer(String serverIP, int serverPort) {
        try{
            clientSocket = new Socket(serverIP,serverPort);
        } catch (Exception exp){
            String meg = exp.getMessage();
        }
    }

    public void SubmitMarketOrder(Direction direction, String tickerSymbol, int size, OrderDuration orderDuration) {
        OrderDTO order = new OrderDTO(direction,OrderType.MARKETORDER,tickerSymbol,size,orderDuration);

        TransmitOrderDTO(order);
    }

    public void SubmitLimitOrder(Direction direction, String tickerSymbol, int size, double price, OrderDuration orderDuration) {
        OrderDTO limitOrder = new OrderDTO(direction,OrderType.LIMITORDER,tickerSymbol,size,price,orderDuration);
        byte[] limitOrderDTO = limitOrder.Serialize();
    }

    private void TransmitOrderDTO (OrderDTO orderDTO) {
        byte[] orderDTOByteArray = orderDTO.Serialize();
        try {
            OutputStream outputStream = clientSocket.getOutputStream();
            outputStream.write(orderDTOByteArray);
        } catch(IOException E) {

        }
    }

    public void GetMarketData() {

    }

    public void GetMyPortfolioInfo() {

    }

    public void CloseConnection() {

    }
}
