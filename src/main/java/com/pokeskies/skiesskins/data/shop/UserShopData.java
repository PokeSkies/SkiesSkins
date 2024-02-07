package com.pokeskies.skiesskins.data.shop;

import com.google.gson.annotations.SerializedName;
import com.pokeskies.skiesskins.config.shop.SkinOptions;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.HashMap;

public class UserShopData {
    @BsonProperty(value = "random")
    @SerializedName("random")
    public HashMap<String, RandomShopData> randomData;

    @BsonProperty(value = "static")
    @SerializedName("static")
    public HashMap<String, StaticShopData> staticData;

    @BsonProperty(value = "packages")
    @SerializedName("packages")
    public HashMap<String, PackageShopData> packagesData;

    public UserShopData() {
        this.randomData = new HashMap<>();
        this.staticData = new HashMap<>();
        this.packagesData = new HashMap<>();
    }

    public UserShopData(
            HashMap<String, RandomShopData> randomData,
            HashMap<String, StaticShopData> staticData,
            HashMap<String, PackageShopData> packagesData
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