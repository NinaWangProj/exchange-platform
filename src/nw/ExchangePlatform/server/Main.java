package nw.ExchangePlatform.server;

import nw.ExchangePlatform.commonData.limitOrderBook.BidPriceTimeComparator;
import nw.ExchangePlatform.commonData.limitOrderBook.OrderComparator;
import nw.ExchangePlatform.commonData.limitOrderBook.OrderComparatorType;

public class Main {

    public static void main(String[] args) throws Exception {
        ServerConfig config = new ServerConfig(5,5,
                58673,0, OrderComparatorType.PriceTimePriority);
        ServerEngine server = new ServerEngine(config);
        server.Start();
    }
}
