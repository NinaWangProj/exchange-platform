package nw.ExchangePlatform.data;

import java.util.HashMap;


public class DTCCWarehouse {

    //<userID, MarketParticipantPortfolio>
    HashMap<String, MarketParticipantPortfolio> portfolio = new HashMap<>();

    public DTCCWarehouse(HashMap<String, MarketParticipantPortfolio> portfolio) {
        this.portfolio = portfolio;
    }


}
