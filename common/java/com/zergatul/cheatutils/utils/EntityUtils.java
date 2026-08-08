package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.mixins.common.accessors.EntityTypeAccessor;
import com.zergatul.cheatutils.wrappers.ClassRemapper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

public class EntityUtils {

    private static final Logger logger = LogManager.getLogger(EntityUtils.class);

    private static List<EntityInfo> classes;
    private static Map<String, EntityInfo> classMap;

    public static List<EntityInfo> getEntityClasses() {
        if (classes == null) {
            loadEntityClasses();
        }
        return classes;
    }

    public static EntityInfo getEntityClass(String name) {
        if (classMap == null) {
            loadEntityClasses();
        }
        return classMap.get(name);
    }

    private static synchronized void loadEntityClasses() {
        if (classes != null) {
            return;
        }

        List<EntityInfo> finalClasses = new ArrayList<>();
        HashSet<EntityInfo> set = new HashSet<>();

        EntityInfo playerInfo = new EntityInfo(Player.class, "minecraft:player");
        finalClasses.add(playerInfo);
        set.add(playerInfo);

        EntityInfo localPlayerInfo = new EntityInfo(LocalPlayer.class);
        finalClasses.add(localPlayerInfo);
        set.add(localPlayerInfo);

        EntityInfo remotePlayerInfo = new EntityInfo(RemotePlayer.class);
        finalClasses.add(remotePlayerInfo);
        set.add(remotePlayerInfo);

        Registries.ENTITY_TYPES.getValues().stream().map(entityType -> {
            if (entityType == EntityType.PLAYER) {
                return null;
            }

            String key = Registries.ENTITY_TYPES.getKey(entityType).toString();
            EntityType.EntityFactory<?> factory = ((EntityTypeAccessor) entityType).getFactory_CU();
            Class<?> entityClass;
            try {
                try {
                    // Passing nulls intentionally triggers the factory's constructor or helper method.
                    // Its stack trace lets us discover the concrete class without creating a fake Level.
                    @SuppressWarnings("ConstantConditions")
                    Entity entity = factory.create(null, null);
                    entityClass = entity.getClass();
                } catch (Throwable throwable) {
                    Optional<Class<?>> optional = findEntityClassFromException(throwable);
                    if (optional.isPresent()) {
                        entityClass = optional.get();
                    } else {
                        logger.warn("Cannot figure out entity class name from stacktrace for {}.", key);
                        logger.warn("Exception", throwable);
                        return null;
                    }
                }

                EntityInfo info = new EntityInfo(entityClass, key);
                set.add(info);
                return info;
            } catch (Throwable throwable) {
                logger.warn("Create entity by EntityType {} failed.", key);
                logger.warn("Exception", throwable);
                return null;
            }
        }).filter(Objects::nonNull).forEach(finalClasses::add);

        Set<Class<?>> interfaces = new HashSet<>();

        finalClasses.forEach(info -> {
            forEachInterface(info.clazz, interfaces::add);

            Class<?> clazz = info.clazz.getSuperclass();
            while (Entity.class.isAssignableFrom(clazz)) {
                try {
                    set.add(new EntityInfo(clazz));
                } catch (Exception ex) {
                    logger.warn("Cannot create EntityInfo for base class {}.", clazz.getName(), ex);
                    continue;
                }
                clazz = clazz.getSuperclass();
            }
        });

        classes = new ArrayList<>(set);
        for (Class<?> iface : interfaces) {
            try {
                classes.add(new EntityInfo(iface));
            } catch (Exception ex) {
                logger.warn("Cannot create EntityInfo for interface {}.", iface.getName(), ex);
            }
        }

        classes = classes.stream().sorted((info1, info2) -> info1.simpleName.compareToIgnoreCase(info2.simpleName)).toList();

        classMap = new HashMap<>(classes.size());
        for (EntityInfo info : classes) {
            classMap.put(info.clazz.getName(), info);
        }
    }

    private static Optional<Class<?>> findEntityClassFromException(Throwable throwable) {
        StackTraceElement[] elements = throwable.getStackTrace();

        int index = -1;
        for (int i = 0; i < elements.length; i++) {
            if (elements[i].getClassName().equals(EntityUtils.class.getName())) {
                index = i;
                break;
            }
        }
        if (index <= 0) {
            throw new IllegalStateException("Cannot process stack trace.");
        }

        while (index > 0) {
            StackTraceElement element = elements[--index];

            Class<?> clazz;
            try {
                clazz = ClassUtils.forName(element.getClassName());
            } catch (ClassNotFoundException ex) {
                logger.warn("Cannot get Class object for {}.", element.getClassName(), ex);
                continue;
            }

            Class<?> returnType;
            if (element.getMethodName().equals("<init>")) {
                returnType = clazz;
            } else {
                List<Method> methods = Arrays.stream(clazz.getDeclaredMethods())
                        .filter(method -> method.getName().equals(element.getMethodName()))
                        .toList();
                if (methods.isEmpty()) {
                    logger.warn("Cannot find method {} for class {}.", element.getMethodName(), element.getClassName());
                    continue;
                }
                if (methods.size() > 1) {
                    logger.warn("More than one {} method exists for class {}.", element.getMethodName(), element.getClassName());
                    continue;
                }
                returnType = methods.get(0).getReturnType();
            }

            if (Entity.class.isAssignableFrom(returnType)) {
                return Optional.of(returnType);
            }
        }

        return Optional.empty();
    }

    private static void forEachInterface(Class<?> clazz, Consumer<Class<?>> consumer) {
        while (clazz != Entity.class) {
            Arrays.stream(clazz.getInterfaces()).forEach(consumer);
            clazz = clazz.getSuperclass();
        }
    }

    public static class EntityInfo {

        public Class<?> clazz;
        public boolean isInterface;
        public String simpleName;
        public List<Class<?>> baseClasses;
        public List<Class<?>> interfaces;
        public String id;

        public EntityInfo(Class<?> clazz) {
            this(clazz, null);
        }

        public EntityInfo(Class<?> clazz, String id) {
            if (clazz.isInterface()) {
                this.clazz = clazz;
                simpleName = getSimpleName(clazz);
                isInterface = true;
            } else {
                if (!Entity.class.isAssignableFrom(clazz)) {
                    throw new IllegalStateException("Not supported.");
                }

                this.clazz = clazz;
                simpleName = getSimpleName(clazz);
                this.id = id;

                baseClasses = new ArrayList<>();
                while (clazz != Entity.class) {
                    clazz = clazz.getSuperclass();
                    baseClasses.add(clazz);
                }

                interfaces = new ArrayList<>();
                forEachInterface(this.clazz, interfaces::add);
            }
        }

        @Override
        public int hashCode() {
            return clazz.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof EntityInfo info) {
                return info.clazz == clazz;
            } else {
                return false;
            }
        }

        private String getSimpleName(Class<?> clazz) {
            String rawName = ClassRemapper.fromObf(clazz.getName());
            int index = rawName.lastIndexOf('.');
            if (index < 0) {
                return rawName;
            } else {
                return rawName.substring(index + 1);
            }
        }
    }
}