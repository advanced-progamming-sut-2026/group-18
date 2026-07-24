package com.compileordie.pvz2.models.missions.shop;

public class ShopItem {
    public String itemId;
    public String displayName;
    public int price;
    public CurrencyType currencyType;
    public int quantity;
    public int maxOwnable;

    public ShopItem(String itemId,
                    String displayName,
                    int price,
                    CurrencyType currencyType,
                    int quantity,
                    int maxOwnable) {
        this.itemId = itemId;
        this.displayName = displayName;
        this.price = price;
        this.currencyType = currencyType;
        this.quantity = quantity;
        this.maxOwnable = maxOwnable;
    }
}
