package nw.ExchangePlatform.data;

public class SecurityCertificate {

    //fields
    public String shareHolderName;
    public String tickerSymbol;
    public int quantity;
    public String typeOfStock;
    public int certificateNum;

    //constructor
    public SecurityCertificate(String shareHolderName, String tickerSymbol, int quantity, String typeOfStock, int certificateNum)
    {
        this.shareHolderName = shareHolderName;
        this.tickerSymbol = tickerSymbol;
        this.quantity = quantity;
        this.typeOfStock = typeOfStock;
        this.certificateNum = certificateNum;
    }
}
