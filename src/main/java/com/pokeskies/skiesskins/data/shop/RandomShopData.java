package com.pokeskies.skiesskins.data.shop;

import com.google.gson.annotations.SerializedName;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RandomShopData {
    @BsonProperty(value = "reset_time")
    @SerializedName("reset_time")
    public Long resetTime;

    @BsonProperty
    public List<SkinData> skins;

    public RandomShopData() {
        this.resetTime = System.currentTimeMillis();
        this.skins = new ArrayList<>();
    }

    public RandomShopData(Long resetTime, List<SkinData> skins) {
        this.resetTime = resetTime;
        this.skins = skins;
    }

    public static class SkinData {
        @BsonProperty
        public String id;
        @BsonProperty
        public int purchases;

        public SkinData() {
            this.id = null;
            this.purchases = 0;
        }

        public SkinData(String id, int purchases) {
            this.id = id;
            this.purchases = purchases;
        }

        @Override
        public String toString() {
            return "SkinData{" +
                    "id='" + id + '\'' +
                    ", purchases=" + purchases +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "RandomShopData{" +
                "resetTime=" + resetTime +
                ", skins=" + skins +
                '}';
    }
}