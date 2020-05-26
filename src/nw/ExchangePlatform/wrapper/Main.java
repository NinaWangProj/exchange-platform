package nw.ExchangePlatform.wrapper;

import nw.ExchangePlatform.data.MarketParticipantOrder;

import java.io.*;
import java.net.*;

public class Main {

    public static void main(String[] args) throws IOException {

        int portID = 58673;
        Server server = new Server(58673);
        server.ConnectWithClient();
        server.StartWorking();
    }
}
