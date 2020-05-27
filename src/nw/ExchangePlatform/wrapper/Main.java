package nw.ExchangePlatform.wrapper;

import nw.ExchangePlatform.data.MarketParticipantOrder;

import java.io.*;
import java.net.*;

public class Main {

    public static void main(String[] args) throws IOException {

        Server server = new Server();
        server.StartListening();
        server.StartWorking();
    }
}
