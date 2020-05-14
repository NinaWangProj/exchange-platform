package nw.ExchangePlatform.data;

import java.util.HashMap;


public class DTCCWarehouse {

    //<ticker,HashMap<userID,certificate>>
    public HashMap<String, HashMap<Integer, SecurityCertificate>> certificatesMap = new HashMap<>();

    public HashMap<Integer, MarketParticipantPortfolio> portfoliosMap = new HashMap<>();

    public DTCCWarehouse() {

    }


}
