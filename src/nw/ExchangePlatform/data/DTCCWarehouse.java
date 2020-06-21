package nw.ExchangePlatform.data;

import java.util.HashMap;


public class DTCCWarehouse {

    //<ticker,HashMap<userID,certificate>>
    public HashMap<String, HashMap<Integer, SecurityCertificate>> certificatesMap;

    public HashMap<Integer, MarketParticipantPortfolio> portfoliosMap;

    public DTCCWarehouse() {
        certificatesMap = new HashMap<>();
        portfoliosMap = new HashMap<>();
    }
}
