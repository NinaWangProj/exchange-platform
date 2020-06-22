package nw.ExchangePlatform.wrapper;

import nw.ExchangePlatform.data.DTOType;
import nw.ExchangePlatform.data.Transferable;

import java.io.OutputStream;
import java.util.ArrayList;

public class ClientWriter implements Runnable{
    private OutputStream outputStream;
    private Session observer;
    private Queue systemQueue;

    public ClientWriter(OutputStream outputStream, Queue systemQueue) {
        this.outputStream= outputStream;
        this.systemQueue = systemQueue;
    }

    public void Start() throws Exception {
        while(true) {
            ArrayList<String> messages = systemQueue.TakeMessage(observer.getSessionID());
            for(String message : messages) {
                byte[] messageDTOByteArray = NotifyAllObservers(message);
                TransmitMessagesToClient(messageDTOByteArray);
            }
        }
    }

    private byte[] NotifyAllObservers(String messages) {
        byte[] messageDTOByteArray = observer.ConstructByteArrayForWriter(messages);
        return messageDTOByteArray;
    }

    private void TransmitMessagesToClient(byte[] messageDTOByteArray) throws Exception{
        DTOType type = DTOType.MessageToClient;
        outputStream.write(type.getByteValue());
        outputStream.write((byte)messageDTOByteArray.length);
        outputStream.write(messageDTOByteArray);
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
