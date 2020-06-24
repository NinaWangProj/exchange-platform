package nw.ExchangePlatform.wrapper;

import nw.ExchangePlatform.data.DTOType;
import nw.ExchangePlatform.data.MessageDTO;
import nw.ExchangePlatform.data.Transferable;

import java.io.OutputStream;


public class ClientResponseProcessor implements Runnable{
    private OutputStream outputStream;
    private Queue systemQueue;
    private int sessionID;

    public ClientResponseProcessor(OutputStream outputStream, Queue systemQueue, int sessionID) {
        this.outputStream= outputStream;
        this.systemQueue = systemQueue;
        this.sessionID = sessionID;
    }

    public void Start() throws Exception {
        while(true) {
            Transferable DTO = systemQueue.TakeResponseDTO(sessionID);
            DTOType type = null;

            if(Class.forName("MessageDTO").isInstance(DTO)) {
                type = DTOType.MESSAGE;
        }
            TransmitDTOToClient(type,DTO);
        }
    }

    private void TransmitDTOToClient(DTOType type, Transferable DTO) throws Exception{
        byte[] DTOByteArray = DTO.Serialize();
        outputStream.write(type.getByteValue());
        outputStream.write((byte)DTOByteArray.length);
        outputStream.write(DTOByteArray);
    }

    public void run() {
        try {
            Start();
        } catch (Exception e) {

        }
    }
}
