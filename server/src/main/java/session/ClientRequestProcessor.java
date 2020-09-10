package session;

import commonData.DTO.*;
import javafx.util.Pair;

import java.io.InputStream;

public class ClientRequestProcessor implements Runnable{
    private InputStream inputStream;
    private Session observer;

    public ClientRequestProcessor(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public Pair<Transferable, Boolean> ReadRequestFromClient() throws Exception {
        int nextByte = inputStream.read();
        Transferable DTO = null;
        Boolean endOfStream = false;

        if(nextByte != -1) {
            DTOType dtoType = DTOType.valueOf(nextByte);
            int byteSizeOfDTO = inputStream.read();
            byte[] DTOByteArray = new byte[byteSizeOfDTO];
            inputStream.read(DTOByteArray, 0, byteSizeOfDTO);

            switch (dtoType) {
                case Order:
                    DTO = OrderDTO.Deserialize(DTOByteArray);
                    break;
                case OpenAcctRequest:
                    DTO = OpenAcctDTO.Deserialize(DTOByteArray);
                    break;
                case LoginRequest:
                    DTO = LoginDTO.Deserialize(DTOByteArray);
                    break;
                case MarketDataRequest:
                    DTO = MarketDataRequestDTO.Deserialize(DTOByteArray);
                    break;
                case PortfolioRequest:
                    DTO = PortfolioRequestDTO.Deserialize(DTOByteArray);
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
            Pair<Transferable, Boolean> readerOutput = ReadRequestFromClient();
            Transferable DTO = readerOutput.getKey();
            Boolean endOfStream = readerOutput.getValue();

            if(DTO != null) {
                NotifyAllObservers(DTO);
            }

            if(endOfStream)
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
