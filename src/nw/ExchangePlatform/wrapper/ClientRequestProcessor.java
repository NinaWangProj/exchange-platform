package nw.ExchangePlatform.wrapper;

import nw.ExchangePlatform.data.DTOType;
import nw.ExchangePlatform.data.LoginDTO;
import nw.ExchangePlatform.data.OrderDTO;
import nw.ExchangePlatform.data.Transferable;

import java.io.InputStream;

public class ClientRequestProcessor implements Runnable{
    private InputStream inputStream;
    private Session observer;

    public ClientRequestProcessor(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public Transferable ReadRequestFromClient() throws Exception {
        int nextByte = inputStream.read();
        Transferable DTO = null;

        if(nextByte != -1) {
            DTOType dtoType = DTOType.valueOf(nextByte);
            int byteSizeOfDTO = inputStream.read();

            switch (dtoType) {
                case ORDER:
                    byte[] orderDTOByteArray = new byte[byteSizeOfDTO];
                    inputStream.read(orderDTOByteArray, 0, byteSizeOfDTO);
                    OrderDTO orderDTO = OrderDTO.Deserialize(orderDTOByteArray);
                    DTO = orderDTO;
                case LoginRequest:
                    byte[] LoginDTOByteArray = new byte[byteSizeOfDTO];
                    inputStream.read(LoginDTOByteArray, 0, byteSizeOfDTO);
                    DTO = LoginDTO.Deserialize(LoginDTOByteArray);
            }
        }

        return DTO;
    }

    private void Start() throws Exception {
        boolean readerFlag = true;

        while (readerFlag) {
            Transferable DTO = ReadRequestFromClient();
            NotifyAllObservers(DTO);

            if(DTO.equals(null))
                readerFlag = false;
        }
    }

    private void NotifyAllObservers(Transferable DTO) throws Exception{
        observer.On_ReceivingDTO(DTO);
    }

    public void Attach(Session session) {
        observer = session;
    }

    public void run() {
        try {
            Start();
        } catch (Exception e) {
        }
    }
}
