package marketData;

import commonData.Order.Direction;
import commonData.limitOrderBook.BookOperation;
import commonData.limitOrderBook.ChangeOperation;
import commonData.marketData.MarketDataItem;

import java.util.ArrayList;
import java.util.List;

public class MarketData {
    private String tickerSymbol;
    private ArrayList<MarketDataItem> bids;
    private ArrayList<MarketDataItem> asks;
    private boolean continuousLevel3DataFlag;

    public MarketData(ArrayList<MarketDataItem> bids, ArrayList<MarketDataItem> asks) {
        this.bids = bids;
        this.asks = asks;
        if (bids.size() != 0) {
            this.tickerSymbol = bids.get(0).getTickerSymbol();
        } else if (asks.size() != 0) {
            this.tickerSymbol = asks.get(0).getTickerSymbol();
        }
        this.continuousLevel3DataFlag = false;
    }

    public MarketData(String tickerSymbol) {
        this.tickerSymbol = tickerSymbol;
        bids = new ArrayList<>();
        asks = new ArrayList<>();
        this.continuousLevel3DataFlag = false;
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public ArrayList<MarketDataItem> getBids() {
        return bids;
    }

    public ArrayList<MarketDataItem> getAsks() {
        return asks;
    }

    public void setBids(ArrayList<MarketDataItem> bids) {
        this.bids = bids;
    }

    public void setAsks(ArrayList<MarketDataItem> asks) {
        this.asks = asks;
    }

    public boolean getContinuousLevel3DataFlag() {
        return continuousLevel3DataFlag;
    }

    public void setContinuousLevel3DataFlag(boolean continuousLevel3DataFlag) {
        this.continuousLevel3DataFlag = continuousLevel3DataFlag;
    }

    public void applyBookOperations(Direction direction, List<ChangeOperation> changeOperations) {
        ArrayList<MarketDataItem> book = null;
        switch (direction) {
            case BUY:
                book = bids;
                break;
            case SELL:
                book = asks;
                break;
        }

        for(ChangeOperation change : changeOperations) {
            switch (change.Operation) {
                case INSERT:
                    book.add(change.IndexOfChange,change.AddedRow);
                    break;
                case REMOVE:
                    book.remove(change.IndexOfChange);
                    break;
                case MODIFY:
                    book.set(change.IndexOfChange,change.AddedRow);
            }
        }
    }
}
