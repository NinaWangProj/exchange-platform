package session;

import commonData.DTO.Transferable;
import common.ServerQueue;

import java.io.OutputStream;

public class ClientResponseProcessor implements Runnable{
    private OutputStream outputStream;
    private ServerQueue systemServerQueue;
    private int sessionID;

    public ClientResponseProcessor(OutputStream outputStream, ServerQueue systemServerQueue, int sessionID) {
        this.outputStream= outputStream;
        this.systemServerQueue = systemServerQueue;
        this.sessionID = sessionID;
    }

    public void Start() throws Exception {
        while(true) {
            Transferable DTO = systemServerQueue.TakeResponseDTO(sessionID);
            TransmitDTOToClient(DTO);
        }
    }

    private void TransmitDTOToClient(Transferable DTO) throws Exception{
        byte[] DTOByteArray = DTO.Serialize();
        outputStream.write(DTO.getDtoType().getByteValue());
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
