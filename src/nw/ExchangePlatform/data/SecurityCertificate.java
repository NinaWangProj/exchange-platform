package nw.ExchangePlatform.data;

public class SecurityCertificate {

    //fields
    String shareHolderName;
    String tickerSymbol;
    int quantity;
    double parValue;
    String typeOfStock;
    int certificateNum;

    //constructor
    public SecurityCertificate(String shareHolderName, String tickerSymbol, int quantity, double parValue, String typeOfStock, int certificateNum)
    {
        this.shareHolderName = shareHolderName;
        this.tickerSymbol = tickerSymbol;
        this.quantity = quantity;
        this.parValue = parValue;
        this.typeOfStock = typeOfStock;
        this.certificateNum = certificateNum;
    }
}
