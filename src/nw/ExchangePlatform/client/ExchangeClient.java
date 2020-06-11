package nw.ExchangePlatform.client;

import nw.ExchangePlatform.data.*;

import java.io.*;
import java.net.*;

public class ExchangeClient {

    private Socket clientSocket;
    private String serverIP;
    private int serverPort;

    public ExchangeClient() {
        serverIP = "192.168.0.20";
        serverPort = 58673;
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

    public void SubmitMarketOrder(Direction direction, String tickerSymbol, int size, OrderDuration orderDuration) {
        OrderDTO marketOrder = new OrderDTO(direction,OrderType.MARKETORDER,tickerSymbol,size,-1,orderDuration);

        TransmitOrderDTO(marketOrder);
    }

    public void SubmitLimitOrder(Direction direction, String tickerSymbol, int size, double price, OrderDuration orderDuration) {
        OrderDTO limitOrder = new OrderDTO(direction,OrderType.LIMITORDER,tickerSymbol,size,price,orderDuration);
        TransmitOrderDTO(limitOrder);
    }

    private void TransmitOrderDTO (OrderDTO orderDTO) {
        byte[] orderDTOByteArray = orderDTO.Serialize();
        try {
            OutputStream outputStream = clientSocket.getOutputStream();
            //write header for the DTO: DTO type(1 byte); DTO size (1 byte);
            DTOType type = DTOType.ORDER;
            outputStream.write(type.getByteValue());
            outputStream.write((byte)orderDTOByteArray.length);
            outputStream.write(orderDTOByteArray);
        } catch(IOException E) {
        }
    }

    public void SubmitSessionConfig() {

    }

}
