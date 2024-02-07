package com.pokeskies.skiesskins.data.shop;

import org.bson.codecs.pojo.annotations.BsonProperty;

public class StaticShopData {
    @BsonProperty
    public int purchases;

    public StaticShopData() {
        this.purchases = 0;
    }

    public StaticShopData(int purchases) {
        this.purchases = purchases;
    }

    @Override
    public String toString() {
        return "StaticShopData{" +
                "purchases=" + purchases +
                '}';
    }
}
