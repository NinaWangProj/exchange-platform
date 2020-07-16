package nw.ExchangePlatform.commonData.limitOrderBook;

import nw.ExchangePlatform.trading.data.MarketParticipantOrder;

public class AskPriceTimeComparator implements OrderComparator {
    public int compare(MarketParticipantOrder order1, MarketParticipantOrder order2) {
        if(order1.getPrice() > order2.getPrice()) {
            return -1;
        } else if (order1.getPrice() < order2.getPrice()) {
            return 1;
        } else {
            if(order1.getTime().before(order2.getTime())) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}
