package com.pokeskies.skiesskins.data.shop;

import org.bson.codecs.pojo.annotations.BsonProperty;

/*
 * This is the user saved data for a single PACKAGE in a shop.
 * This purely tracks the number of purchases of this package.
 */
public class PackageEntryShopData {
    @BsonProperty
    public int purchases;

    public PackageEntryShopData() {
        this.purchases = 0;
    }

    public PackageEntryShopData(int purchases) {
        this.purchases = purchases;
    }

    @Override
    public String toString() {
        return "PackageShopData{" +
                "purchases=" + purchases +
                '}';
    }
}
