package com.pokeskies.skiesskins.data.shop;

import org.bson.codecs.pojo.annotations.BsonProperty;

/*
 * This is the user saved data for a single STATIC Skin entry in a shop.
 * This purely tracks the number of purchases of this package.
 */
public class StaticEntryShopData {
    @BsonProperty
    public int purchases;

    public StaticEntryShopData() {
        this.purchases = 0;
    }

    public StaticEntryShopData(int purchases) {
        this.purchases = purchases;
    }

    @Override
    public String toString() {
        return "StaticShopData{" +
                "purchases=" + purchases +
                '}';
    }
}
