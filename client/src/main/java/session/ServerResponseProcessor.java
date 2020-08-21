package session;

import commonData.DTO.*;

import java.io.InputStream;

public class ServerResponseProcessor implements Runnable{
    private InputStream inputStream;
    private ClientSession observer;

    public ServerResponseProcessor(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public Transferable ReadMessageFromServer() throws Exception {
        int nextByte = inputStream.read();
        Transferable DTO = null;

        if(nextByte != -1) {
            DTOType dtoType = DTOType.valueOf(nextByte);
            int byteSizeOfDTO = inputStream.read();
            byte[] DTOByteArray = new byte[byteSizeOfDTO];
            inputStream.read(DTOByteArray, 0, byteSizeOfDTO);

            switch (dtoType) {
                case Message:
                    DTO = MessageDTO.Deserialize(DTOByteArray);
                    break;
                case MarketData:
                    DTO = MarketDataDTO.Deserialize(DTOByteArray);
                    break;
                case BookChanges:
                    DTO = BookChangeDTO.Deserialize(DTOByteArray);
                    break;
                case Portfolio:
                    DTO = PortfolioDTO.Deserialize(DTOByteArray);
                    break;
            }
        }
        return DTO;
    }

    private void Start() throws Exception {
        boolean readerFlag = true;

        while (readerFlag) {
            Transferable DTO = ReadMessageFromServer();

            if(DTO != null) {
                NotifyAllObservers(DTO);
            } else {
                readerFlag = false;
            }
        }
    }

    private void NotifyAllObservers(Transferable DTO) throws Exception{
        observer.On_ReceivingDTO(DTO);
    }

    public void Attach(ClientSession session) {
        observer = session;
    }

    public void run() {
        try {
            Start();
        } catch (Exception e) {
            String test = null;
        }
    }
}
