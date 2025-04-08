package com.pokeskies.skiesskins.data.shop;

import com.google.gson.annotations.SerializedName;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.HashMap;

/*
 * A players data on a specific Shop, identified by the shop ID.
 *
 * $randomData contains the data for the random Pokemon Skin entries, which has a reset time that needs to be
 * checked against often.
 */
public class UserShopData {
    @BsonProperty(value = "random")
    @SerializedName("random")
    public HashMap<String, RandomEntryShopData> randomData;

    @BsonProperty(value = "static")
    @SerializedName("static")
    public HashMap<String, HashMap<String, StaticEntryShopData>> staticData;

    @BsonProperty(value = "packages")
    @SerializedName("packages")
    public HashMap<String, PackageEntryShopData> packagesData;

    public UserShopData() {
        this.randomData = new HashMap<>();
        this.staticData = new HashMap<>();
        this.packagesData = new HashMap<>();
    }

    public UserShopData(
            HashMap<String, RandomEntryShopData> randomData,
            HashMap<String, HashMap<String, StaticEntryShopData>> staticData,
            HashMap<String, PackageEntryShopData> packagesData
    ) {
        this.randomData = randomData;
        this.staticData = staticData;
        this.packagesData = packagesData;
    }

    @Override
    public String toString() {
        return "UserShopData{" +
                "randomData=" + randomData +
                ", staticData=" + staticData +
                ", packagesData=" + packagesData +
                '}';
    }
}
