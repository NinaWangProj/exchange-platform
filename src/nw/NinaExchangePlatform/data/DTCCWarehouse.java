package nw.NinaExchangePlatform.data;

import java.util.HashMap;


public class DTCCWarehouse {

    //<userID, MarketParticipantPortfolio>
    HashMap<String, MarketParticipantPortfolio> portfolio = new HashMap<String, MarketParticipantPortfolio>();

    public DTCCWarehouse(HashMap<String, MarketParticipantPortfolio> portfolio) {
        this.portfolio = portfolio;
    }


}
