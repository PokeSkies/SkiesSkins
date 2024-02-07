package com.pokeskies.skiesskins.data;

import com.google.gson.annotations.SerializedName;
import com.pokeskies.skiesskins.data.shop.UserShopData;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class UserData {
    @BsonProperty
    public UUID uuid;

    @BsonProperty
    public List<UserSkinData> inventory;

    @BsonProperty(value = "shop_data")
    @SerializedName("shop_data")
    public HashMap<String, UserShopData> shopData;

    public UserData() {}

    public UserData(UUID uuid) {
        this.uuid = uuid;
        this.inventory = new ArrayList<>();
        this.shopData = new HashMap<>();
    }

    public UserData(UUID uuid, List<UserSkinData> inventory, HashMap<String, UserShopData> shopData) {
        this.uuid = uuid;
        this.inventory = inventory;
        this.shopData = shopData;
    }

    @Override
    public String toString() {
        return "UserData{" +
                "uuid=" + uuid +
                ", inventory=" + inventory +
                ", shopData=" + shopData +
                '}';
    }
}
