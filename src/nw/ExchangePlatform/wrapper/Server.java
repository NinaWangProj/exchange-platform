package nw.ExchangePlatform.wrapper;

import nw.ExchangePlatform.data.*;

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
        int nextByte= inputStream.read();

        while (nextByte != -1) {
            DTOType dtoType = DTOType.valueOf(nextByte);

            switch (dtoType) {
                case ORDER:
                    int byteSizeOfDTO = inputStream.read();
                    byte[] orderDTOByteArray = new byte[byteSizeOfDTO];
                    inputStream.read(orderDTOByteArray,0,byteSizeOfDTO);
                    OrderDTO orderDTO = OrderDTO.Deserialize(orderDTOByteArray);

                    //convert OrderDTO to MarketParticipantOrder

                case CONFIG:
                    //need to implement later

            }
        }
    }
}
