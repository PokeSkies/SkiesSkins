package com.pokeskies.skiesskins.data;

import com.google.gson.annotations.SerializedName;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.*;

public class UserData {
    @BsonProperty
    public UUID uuid;

    @BsonProperty
    public List<SkinData> inventory;

    @BsonProperty(value = "shop_data")
    @SerializedName("shop_data")
    public ShopData shopData;

    @BsonProperty(value = "purchase_history")
    @SerializedName("purchase_history")
    public List<String> purchaseHistory;

    public UserData() {}

    public UserData(UUID uuid) {
        this.uuid = uuid;
        this.inventory = new ArrayList<>();
        this.shopData = new ShopData();
        this.purchaseHistory = new ArrayList<>();
    }

    public UserData(UUID uuid, List<SkinData> inventory, ShopData shopData, List<String> purchaseHistory) {
        this.uuid = uuid;
        this.inventory = inventory;
        this.shopData = shopData;
        this.purchaseHistory = purchaseHistory;
    }

    public static class SkinData {
        @BsonProperty
        public String id = "";

        public SkinData() {}

        public SkinData(String id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SkinData skinData = (SkinData) o;
            return Objects.equals(id, skinData.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return "SkinData{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }

    public static class ShopData {
        @BsonProperty(value = "reset_time")
        @SerializedName("reset_time")
        public String resetTime = "";

        @BsonProperty
        public List<String> skins = Collections.emptyList();

        public ShopData() {}

        public ShopData(String resetTime, List<String> skins) {
            this.resetTime = resetTime;
            this.skins = skins;
        }

        @Override
        public String toString() {
            return "ShopData{" +
                    "resetTime='" + resetTime + '\'' +
                    ", skins=" + skins +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "UserData{" +
                "uuid=" + uuid +
                ", inventory=" + inventory +
                ", shopData=" + shopData +
                ", purchaseHistory=" + purchaseHistory +
                '}';
    }
}
