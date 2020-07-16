package nw.ExchangePlatform.server.session;

import nw.ExchangePlatform.commonData.DTO.*;

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
            byte[] DTOByteArray = new byte[byteSizeOfDTO];
            inputStream.read(DTOByteArray, 0, byteSizeOfDTO);

            switch (dtoType) {
                case Order:
                    DTO = OrderDTO.Deserialize(DTOByteArray);
                case LoginRequest:
                    DTO = LoginDTO.Deserialize(DTOByteArray);
                case MareketDataRequest:
                    DTO = MarketDataDTO.Deserialize(DTOByteArray);
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
