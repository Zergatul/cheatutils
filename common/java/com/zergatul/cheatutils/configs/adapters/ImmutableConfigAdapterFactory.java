package com.zergatul.cheatutils.configs.adapters;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.zergatul.cheatutils.configs.ImmutableConfig;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ImmutableConfigAdapterFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (ImmutableConfig.class.isAssignableFrom(type.getRawType())) {
            TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
            return new ImmutableConfigAdapter<>(gson, delegate, type.getRawType());
        }
        return null;
    }

    private static class ImmutableConfigAdapter<T> extends TypeAdapter<T> {

        private final TypeAdapter<T> delegate;
        private final Class<? super T> clazz;
        private final Method defaultsMethod;
        private final Constructor<?> constructor;
        private final Component[] components;
        private final Map<String, Component> componentsByName;

        public ImmutableConfigAdapter(Gson gson, TypeAdapter<T> delegate, Class<? super T> clazz) {
            this.delegate = delegate;
            this.clazz = clazz;

            if (!clazz.isRecord()) {
                throw new IllegalArgumentException("Immutable config must be a record: " + clazz.getName());
            }

            RecordComponent[] recordComponents = clazz.getRecordComponents();
            try {
                defaultsMethod = clazz.getMethod("defaults");
                if (!Modifier.isStatic(defaultsMethod.getModifiers()) || defaultsMethod.getReturnType() != clazz) {
                    throw new IllegalArgumentException("Expected public static " + clazz.getSimpleName() + " defaults()");
                }
                constructor = clazz.getDeclaredConstructor(Arrays.stream(recordComponents).map(RecordComponent::getType).toArray(Class<?>[]::new));
            } catch (ReflectiveOperationException e) {
                throw new IllegalArgumentException("Cannot initialize immutable config adapter for " + clazz.getName(), e);
            }

            components = new Component[recordComponents.length];
            componentsByName = new HashMap<>();
            for (int i = 0; i < recordComponents.length; i++) {
                RecordComponent recordComponent = recordComponents[i];
                Component component = new Component(i, recordComponent.getAccessor(), gson.getAdapter(TypeToken.get(recordComponent.getGenericType())));
                components[i] = component;
                componentsByName.put(recordComponent.getName(), component);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public T read(JsonReader in) throws IOException {
            try {
                T config = (T) defaultsMethod.invoke(null);

                // return default value if null
                if (in.peek() == JsonToken.NULL) {
                    in.nextNull();
                    return config;
                }

                Object[] arguments = new Object[components.length];
                for (Component component : components) {
                    arguments[component.index] = component.accessor.invoke(config);
                }

                in.beginObject();
                while (in.hasNext()) {
                    Component component = componentsByName.get(in.nextName());
                    if (component == null) {
                        in.skipValue();
                    } else if (in.peek() == JsonToken.NULL) {
                        in.nextNull();
                    } else {
                        Object value = component.adapter.read(in);
                        if (value != null) {
                            arguments[component.index] = value;
                        }
                    }
                }
                in.endObject();

                return (T) constructor.newInstance(arguments);
            } catch (InvocationTargetException e) {
                throw new JsonParseException("Cannot deserialize immutable config " + clazz.getName(), e.getCause());
            } catch (ReflectiveOperationException | NumberFormatException e) {
                throw new JsonParseException("Cannot deserialize immutable config " + clazz.getName(), e);
            }
        }

        @Override
        public void write(JsonWriter out, T value) throws IOException {
            delegate.write(out, value);
        }

        private record Component(int index, Method accessor, TypeAdapter<?> adapter) {}
    }
}