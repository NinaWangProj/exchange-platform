package serverEngine;

import trading.limitOrderBook.OrderComparatorType;

import java.util.concurrent.atomic.AtomicLong;

public class Main {

    public static void main(String[] args) throws Exception {
        ServerConfig config = new ServerConfig(5,5,
                58673,0, OrderComparatorType.PriceTimePriority,
                (long) 0,"");
        ServerEngine server = new ServerEngine(config);
        server.Start();
    }
}
