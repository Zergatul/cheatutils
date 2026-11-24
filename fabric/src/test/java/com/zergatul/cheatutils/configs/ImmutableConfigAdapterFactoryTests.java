package com.zergatul.cheatutils.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.zergatul.cheatutils.configs.adapters.ImmutableConfigAdapterFactory;
import com.zergatul.cheatutils.configs.modules.AirPlaceConfig;
import com.zergatul.cheatutils.configs.modules.EntityTitleConfig;
import com.zergatul.cheatutils.configs.utility.FontConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ImmutableConfigAdapterFactoryTests {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(new ImmutableConfigAdapterFactory())
            .create();

    @Test
    public void emptyObjectUsesDefaults() {
        assertEquals(AirPlaceConfig.defaults(), gson.fromJson("{}", AirPlaceConfig.class));
    }

    @Test
    public void partialObjectPreservesMissingProperties() {
        assertEquals(new AirPlaceConfig(1, 8), gson.fromJson("{\"maxRange\":8}", AirPlaceConfig.class));
        assertEquals(new AirPlaceConfig(2, 5), gson.fromJson("{\"minRange\":2}", AirPlaceConfig.class));
    }

    @Test
    public void constructorNormalizesAfterAllPropertiesAreRead() {
        AirPlaceConfig expected = new AirPlaceConfig(4, 2);
        assertEquals(expected, gson.fromJson("{\"minRange\":4,\"maxRange\":2}", AirPlaceConfig.class));
        assertEquals(expected, gson.fromJson("{\"maxRange\":2,\"minRange\":4}", AirPlaceConfig.class));
        assertEquals(new AirPlaceConfig(0.5, 10), gson.fromJson("{\"minRange\":-10,\"maxRange\":100}", AirPlaceConfig.class));
    }

    @Test
    public void nullRecordUsesDefaultsAndConsumesInput() {
        assertEquals(AirPlaceConfig.defaults(), gson.fromJson("null", AirPlaceConfig.class));
        List<AirPlaceConfig> configs = gson.fromJson("[null,{\"maxRange\":8},null]", new TypeToken<List<AirPlaceConfig>>() {});
        assertEquals(List.of(AirPlaceConfig.defaults(), new AirPlaceConfig(1, 8), AirPlaceConfig.defaults()), configs);
    }

    @Test
    public void nullPropertiesRetainDefaultsForPrimitiveAndReferenceTypes() {
        assertEquals(AirPlaceConfig.defaults(), gson.fromJson("{\"minRange\":null,\"maxRange\":null}", AirPlaceConfig.class));
        assertEquals(OptionsConfig.defaults(), gson.fromJson("{\"label\":null,\"enabled\":null}", OptionsConfig.class));
        assertEquals(ParentConfig.defaults(), gson.fromJson("{\"range\":null}", ParentConfig.class));
    }

    @Test
    public void falseZeroAndEmptyStringAreNotTreatedAsMissing() {
        assertEquals(new OptionsConfig("", false, 0), gson.fromJson("{\"label\":\"\",\"enabled\":false,\"count\":0}", OptionsConfig.class));
    }

    @Test
    public void unknownPropertiesSkipEntireValues() {
        assertEquals(new AirPlaceConfig(1, 8), gson.fromJson("""
                {"unknown":{"nested":[1,null,{"x":true}]},"maxRange":8,"other":null}
                """, AirPlaceConfig.class));
    }

    @Test
    public void nestedPartialConfigUsesItsOwnDefaults() {
        ParentConfig config = gson.fromJson("{\"range\":{\"maxRange\":8}}", ParentConfig.class);
        assertEquals(new ParentConfig("parent", new AirPlaceConfig(1, 8)), config);
    }

    @Test
    public void entityTitleConfigUsesFontDefaultsAndNormalizesNestedValues() {
        EntityTitleConfig config = gson.fromJson("""
                {"titleFont":{"size":24},"enchantmentFont":{"size":200},"itemScale":100}
                """, EntityTitleConfig.class);
        assertEquals(EntityTitleConfig.defaults().hpPrefix(), config.hpPrefix());
        assertEquals(FontConfig.defaults().withSize(24), config.titleFont());
        assertEquals(FontConfig.defaults().withSize(100), config.enchantmentFont());
        assertEquals(50, config.itemScale());
        assertEquals(config, gson.fromJson(gson.toJson(config), EntityTitleConfig.class));
        assertEquals(EntityTitleConfig.defaults(), gson.fromJson("{\"titleFont\":null}", EntityTitleConfig.class));
    }

    @Test
    public void genericComponentTypesPreserveElementTypes() {
        CollectionsConfig config = gson.fromJson("""
                {"ranges":[{}, {"maxRange":8}], "counts":{"one":1}}
                """, CollectionsConfig.class);
        assertEquals(List.of(AirPlaceConfig.defaults(), new AirPlaceConfig(1, 8)), config.ranges());
        assertEquals(Integer.valueOf(1), config.counts().get("one"));
    }

    @Test
    public void serializationMatchesTheOrdinaryRecordAdapterAndRoundTrips() {
        CollectionsConfig config = new CollectionsConfig(List.of(new AirPlaceConfig(2, 8)), Map.of("one", 1));
        assertEquals(new Gson().toJsonTree(config), gson.toJsonTree(config));
        assertEquals(config, gson.fromJson(gson.toJson(config), CollectionsConfig.class));
        assertEquals("null", gson.toJson(null, AirPlaceConfig.class));
    }

    @Test
    public void repeatedReadsDoNotShareConstructorArgumentsOrDefaultCollections() {
        gson.fromJson("{\"minRange\":4,\"maxRange\":8}", AirPlaceConfig.class);
        assertEquals(AirPlaceConfig.defaults(), gson.fromJson("{}", AirPlaceConfig.class));
        CollectionsConfig first = gson.fromJson("{}", CollectionsConfig.class);
        first.ranges().add(new AirPlaceConfig(2, 8));
        assertTrue(gson.fromJson("{}", CollectionsConfig.class).ranges().isEmpty());
    }

    @Test
    public void nonConfigTypesKeepTheirNormalAdapters() {
        ImmutableConfigAdapterFactory factory = new ImmutableConfigAdapterFactory();
        assertNull(factory.create(gson, TypeToken.get(Object.class)));
        assertNull(factory.create(gson, TypeToken.get(String.class)));
        assertEquals(Map.of("value", "test"), gson.fromJson("{\"value\":\"test\"}", Object.class));
    }

    @Test
    public void invalidJsonValuesFail() {
        assertThrows(JsonParseException.class, () -> gson.fromJson("[]", AirPlaceConfig.class));
        assertThrows(JsonParseException.class, () -> gson.fromJson("\"text\"", AirPlaceConfig.class));
        assertThrows(JsonParseException.class, () -> gson.fromJson("{\"minRange\":\"invalid\"}", AirPlaceConfig.class));
    }

    @Test
    public void constructorFailurePreservesItsCause() {
        JsonParseException exception = assertThrows(JsonParseException.class, () -> gson.fromJson("{\"count\":-1}", PositiveConfig.class));
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertEquals("count must be positive", exception.getCause().getMessage());
    }

    @Test
    public void componentAdaptersUseTheSameGsonConfiguration() {
        Gson custom = new GsonBuilder()
                .registerTypeAdapterFactory(new ImmutableConfigAdapterFactory())
                .registerTypeAdapter(Value.class, new TypeAdapter<Value>() {
                    @Override
                    public Value read(JsonReader in) throws IOException {
                        return new Value(in.nextInt() + 10);
                    }

                    @Override
                    public void write(JsonWriter out, Value value) throws IOException {
                        out.value(value.number() - 10);
                    }
                })
                .create();
        ValueConfig config = custom.fromJson("{\"value\":2}", ValueConfig.class);
        assertEquals(new ValueConfig(new Value(12)), config);
        assertEquals("{\"value\":2}", custom.toJson(config));
    }

    public record OptionsConfig(String label, boolean enabled, int count) implements ImmutableConfig {
        public static OptionsConfig defaults() {
            return new OptionsConfig("default", true, 5);
        }
    }

    public record ParentConfig(String name, AirPlaceConfig range) implements ImmutableConfig {
        public static ParentConfig defaults() {
            return new ParentConfig("parent", new AirPlaceConfig(2, 6));
        }
    }

    public record CollectionsConfig(List<AirPlaceConfig> ranges, Map<String, Integer> counts) implements ImmutableConfig {
        public static CollectionsConfig defaults() {
            return new CollectionsConfig(new ArrayList<>(), Map.of());
        }
    }

    public record PositiveConfig(int count) implements ImmutableConfig {
        public PositiveConfig {
            if (count <= 0) {
                throw new IllegalArgumentException("count must be positive");
            }
        }

        public static PositiveConfig defaults() {
            return new PositiveConfig(1);
        }
    }

    public record Value(int number) {}

    public record ValueConfig(Value value) implements ImmutableConfig {
        public static ValueConfig defaults() {
            return new ValueConfig(new Value(10));
        }
    }
}
