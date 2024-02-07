package com.pokeskies.skiesskins.data;

import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Objects;

public class UserSkinData {
    @BsonProperty
    public String id = "";

    public UserSkinData() {}

    public UserSkinData(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSkinData skinData = (UserSkinData) o;
        return Objects.equals(id, skinData.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "UserSkinData{" +
                "id='" + id + '\'' +
                '}';
    }
}