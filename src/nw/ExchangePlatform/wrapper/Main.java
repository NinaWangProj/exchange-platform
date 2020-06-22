package nw.ExchangePlatform.wrapper;

import nw.ExchangePlatform.data.MarketParticipantOrder;

import java.io.*;
import java.net.*;

public class Main {

    public static void main(String[] args) throws Exception {
        ServerConfig config = new ServerConfig(5,5,
                58673,0);
        Server server = new Server(config);
        server.Start();
    }
}
