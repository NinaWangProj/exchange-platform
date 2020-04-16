package nw.ExchangePlatform.utility;

import java.util.ArrayList;

public class UnfilledOrderMessenger extends Messenger {

    public void AddMessages(String userName,int orderID,String tickerSymbol,int size,
                            double tradePrice, String unfilledReason) {
        String message = "Sorry " + userName + " .Unfortunately your order with orderID: " + orderID
                + " is not filled for the reason that " + unfilledReason;
        messages.add(message);
    }
}
