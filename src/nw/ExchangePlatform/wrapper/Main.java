package nw.ExchangePlatform.wrapper;

import nw.ExchangePlatform.data.MarketParticipantOrder;

import java.io.*;
import java.net.*;

public class Main {

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        server.Start();
    }
}
