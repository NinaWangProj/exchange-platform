package server.common.utility;

public class BinSelector {

    public static int ChooseAlphabeticalBin(String tickerSymbol,int numOfBins) {
        //take an ticker symbol; select the bin index based on the initial letter of ticker symbol
        char tickerSymbolInitialChar = Character.toUpperCase(tickerSymbol.charAt(0));

        int orderQueueIndex = (tickerSymbolInitialChar - 'A') % numOfBins;
        return orderQueueIndex;
    }
}
