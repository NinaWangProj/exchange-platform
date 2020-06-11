package nw.ExchangePlatform.wrapper;

import nw.ExchangePlatform.data.MarketParticipantOrder;

import java.io.*;
import java.net.*;

public class Main {

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        //create a separate thread for listening to client connection requests
        Thread thread = new Thread(server);
        //Once server client connection has been established, server will start reading from inputstream to execute requests
        server.StartWorking();
    }
}
