package nw.ExchangePlatform.data;

import java.util.HashMap;


public class DTCCWarehouse {

    //<ticker,HashMap<userID,certificate>>
    public HashMap<String, HashMap<String,SecurityCertificate>> certificatesMap = new HashMap<>();

    public HashMap<String, MarketParticipantPortfolio> portfoliosMap = new HashMap<>();

    public DTCCWarehouse() {

    }


}
