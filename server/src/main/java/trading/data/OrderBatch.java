package trading.data;

import commonData.Order.MarketParticipantOrder;

import java.util.ArrayList;

public class OrderBatch {
    public final String tickerSymbol;
    public ArrayList<MarketParticipantOrder> batch;

    public OrderBatch(String tickerSymbol, ArrayList<MarketParticipantOrder> batch) {
        this.tickerSymbol = tickerSymbol;
        this.batch = batch;
    }
}
