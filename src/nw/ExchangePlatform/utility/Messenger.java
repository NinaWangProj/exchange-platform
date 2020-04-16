package nw.ExchangePlatform.utility;

import java.util.ArrayList;

public abstract class Messenger {
    protected ArrayList<String> messages;

    public abstract void AddMessages(String userID,int orderID,String tickerSymbol,int size,
                                     double tradePrice, String reason);
}
