package nw.ExchangePlatform.wrapper;

import java.io.OutputStream;

public class ClientWriter implements Runnable{
    private OutputStream outputStream;

    public ClientWriter(OutputStream outputStream) {
        this.outputStream= outputStream;
    }

    public void WriteMessagesToClient() {

    }

    public void run() {
        try {
            WriteMessagesToClient();
        } catch (Exception e) {

        }
    }
}
