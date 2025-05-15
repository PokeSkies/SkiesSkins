package com.pokeskies.skiesskins.data.shop;

import com.google.gson.annotations.SerializedName;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.ArrayList;
import java.util.List;

/*
 * This is the user saved data for a set of RANDOM Skin entries in a shop. This saves the list of skins that were
 * randomly generated ($skins) as well as the time ($resetTime) that it was generated at.
 */
public class RandomEntryShopData {
    @BsonProperty(value = "reset_time")
    @SerializedName("reset_time")
    public Long resetTime;

    @BsonProperty
    public List<SkinData> skins;

    public RandomEntryShopData() {
        this.resetTime = System.currentTimeMillis();
        this.skins = new ArrayList<>();
    }

    public RandomEntryShopData(Long resetTime, List<SkinData> skins) {
        this.resetTime = resetTime;
        this.skins = skins;
    }

    /*
     * This is the skin data saved for a user's RANDOM entry skin list.
     * This tracks the skin ID and the number of purchases
     */
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
