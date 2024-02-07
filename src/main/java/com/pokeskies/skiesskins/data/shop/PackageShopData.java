package com.pokeskies.skiesskins.data.shop;

import org.bson.codecs.pojo.annotations.BsonProperty;

public class PackageShopData {
    @BsonProperty
    public int purchases;

    public PackageShopData() {
        this.purchases = 0;
    }

    public PackageShopData(int purchases) {
        this.purchases = purchases;
    }

    @Override
    public String toString() {
        return "PackageShopData{" +
                "purchases=" + purchases +
                '}';
    }
}
