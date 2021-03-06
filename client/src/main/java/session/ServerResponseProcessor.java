package session;

import commonData.DTO.*;
import javafx.util.Pair;

import java.io.InputStream;

public class ServerResponseProcessor implements Runnable{
    private InputStream inputStream;
    private ClientSession observer;

    public ServerResponseProcessor(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public Pair<Transferable, Boolean> ReadMessageFromServer() throws Exception {
        int nextByte = inputStream.read();
        Transferable DTO = null;
        Boolean endOfStream = false;

        if(nextByte != -1) {
            DTOType dtoType = DTOType.valueOf(nextByte);
            int byteSizeOfDTO = inputStream.read();
            byte[] DTOByteArray = new byte[byteSizeOfDTO];
            inputStream.read(DTOByteArray, 0, byteSizeOfDTO);

            switch (dtoType) {
                case OrderStatus:
                    DTO = OrderStatusDTO.Deserialize(DTOByteArray);
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
                case Message:
                    DTO = MessageDTO.Deserialize(DTOByteArray);
                    break;
                }
        } else {
            endOfStream = true;
        }
        return new Pair<Transferable,Boolean>(DTO,endOfStream);
    }

    private void Start() throws Exception {
        boolean readerFlag = true;

        while (readerFlag) {
            Pair<Transferable, Boolean> readerOutput = ReadMessageFromServer();
            Transferable DTO = readerOutput.getKey();
            Boolean endOfStream = readerOutput.getValue();

            if(DTO != null)
                NotifyAllObservers(DTO);

            if(endOfStream)
                readerFlag = false;
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
