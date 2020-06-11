package nw.ExchangePlatform.wrapper;

import nw.ExchangePlatform.data.DTOType;
import nw.ExchangePlatform.data.OrderDTO;

import javax.print.attribute.standard.ReferenceUriSchemesSupported;
import java.io.InputStream;
import java.net.Socket;

public class Session implements Runnable {
    private final int sessionID;
    private final Socket clientSocket;
    private int clientUserID;
    private String clientUserName;

    public Session(Socket clientSocket,int sessionID) {
        this.clientSocket = clientSocket;
        this.sessionID = sessionID;
    }
    public int getSessionID() {
        return sessionID;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public int getClientUserID() {
        return clientUserID;
    }

    public void setClientUserID(int clientUserID) {
        this.clientUserID = clientUserID;
    }

    public String getClientUserName() {
        return clientUserName;
    }

    public void setClientUserName(String clientUserName) {
        this.clientUserName = clientUserName;
    }

    private void RunCurrentSession() throws Exception{
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
                    //Execute the order

                case CONFIG:
                    //need to implement later

            }
            nextByte = inputStream.read();
        }
    }

    public void run() {
        try {
            RunCurrentSession();
        } catch (Exception e) {

        }
    }
}


