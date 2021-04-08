package me.deftware.cursemods.curse;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class Serializer {

    public static Gson getGson() {
        return new GsonBuilder()
                .addSerializationExclusionStrategy(onlySerializedName())
                .addDeserializationExclusionStrategy(onlySerializedName())
                .create();
    }

    public static ExclusionStrategy onlySerializedName() {
        return new ExclusionStrategy() {

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }

            @Override
            public boolean shouldSkipField(FieldAttributes field) {
                return field.getAnnotation(SerializedName.class) == null;
            }

        };
    }


}
