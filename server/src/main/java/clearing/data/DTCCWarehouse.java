package clearing.data;

import commonData.clearing.MarketParticipantPortfolio;
import commonData.clearing.SecurityCertificate;

import java.util.HashMap;


public class DTCCWarehouse {

    //<ticker,HashMap<userID,certificate>>
    public HashMap<String, HashMap<Integer, SecurityCertificate>> certificatesMap;

    public HashMap<Integer, MarketParticipantPortfolio> portfoliosMap;

    public DTCCWarehouse() {
        certificatesMap = new HashMap<String, HashMap<Integer, SecurityCertificate>>();
        portfoliosMap = new HashMap<Integer, MarketParticipantPortfolio>();
    }
}
