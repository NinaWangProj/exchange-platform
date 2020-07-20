package nw.ExchangePlatform.trading.limitOrderBook;

import nw.ExchangePlatform.trading.data.MarketParticipantOrder;

public interface OrderComparator{
    public int compare(MarketParticipantOrder order1, MarketParticipantOrder order2);
}
