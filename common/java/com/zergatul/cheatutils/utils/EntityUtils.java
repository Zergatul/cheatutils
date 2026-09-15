package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.common.RegistryExtensions;
import com.zergatul.cheatutils.mixins.common.accessors.EntityTypeAccessor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
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

        EntityInfo info1 = new EntityInfo(Player.class, "minecraft:player");
        finalClasses.add(info1);
        set.add(info1);

        EntityInfo info2 = new EntityInfo(LocalPlayer.class);
        finalClasses.add(info2);
        set.add(info2);

        EntityInfo info3 = new EntityInfo(RemotePlayer.class);
        finalClasses.add(info3);
        set.add(info3);

        RegistryExtensions.getValues(BuiltInRegistries.ENTITY_TYPE).stream().map(et -> {
            if (et == EntityTypes.PLAYER) {
                return null;
            }

            String key = BuiltInRegistries.ENTITY_TYPE.getKey(et).toString();
            EntityType.EntityFactory<?> factory = ((EntityTypeAccessor) et).getFactory_CU();
            Class<?> entityClass;
            try {
                try {
                    // we know it is not OK to pass null as parameters, but we do this explicitly to trigger exception
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

                EntityInfo info = new EntityInfo(entityClass, BuiltInRegistries.ENTITY_TYPE.getKey(et).toString());
                set.add(info);
                return info;
            } catch (Throwable throwable) {
                logger.warn("Create entity by EntityType {} failed.", key);
                logger.warn("Exception", throwable);
                return null;
            }
        }).filter(Objects::nonNull).forEach(finalClasses::add);

        Set<Class<?>> interfaces = new HashSet<>();

        finalClasses.forEach(ei -> {
            forEachInterface(ei.clazz, interfaces::add);

            Class<?> clazz = ei.clazz.getSuperclass();
            while (Entity.class.isAssignableFrom(clazz)) {
                try {
                    EntityInfo baseInfo = new EntityInfo(clazz);
                    set.add(baseInfo);
                } catch (Exception ex) {
                    logger.warn("Cannot create EntityInfo for base class {}.", clazz.getName(), ex);
                    continue;
                }
                clazz = clazz.getSuperclass();
            }
        });

        classes = new ArrayList<>();
        classes.addAll(set);
        for (Class<?> iface : interfaces) {
            try {
                classes.add(new EntityInfo(iface));
            } catch (Exception ex) {
                logger.warn("Cannot create EntityInfo for interface {}.", iface.getName(), ex);
            }
        }

        classes = classes.stream().sorted((i1, i2) -> i1.simpleName.compareToIgnoreCase(i2.simpleName)).toList();

        classMap = new HashMap<>(classes.size());
        for (EntityInfo info: classes) {
            classMap.put(info.clazz.getName(), info);
        }
    }

    private static Optional<Class<?>> findEntityClassFromException(Throwable throwable) {
        StackTraceElement[] elements = throwable.getStackTrace();

        // find index of first com.zergatul.cheatutils.EntityUtils class in stack trace
        // we will check elements only before this index
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
            index--;
            StackTraceElement element = elements[index];

            Class<?> clazz;
            try {
                clazz = ClassUtils.forName(element.getClassName());
            } catch (ClassNotFoundException ex) {
                logger.warn("Cannot get Class object for {}.", element.getClassName());
                logger.warn("Exception", ex);
                continue;
            }

            Class<?> returnType;
            // for constructor, we simply use current class
            if (element.getMethodName().equals("<init>")) {
                returnType = clazz;
            } else {
                // for methods, we have to check return value
                List<Method> methods = Arrays.stream(clazz.getDeclaredMethods())
                        .filter(m -> m.getName().equals(element.getMethodName()))
                        .toList();
                if (methods.isEmpty()) {
                    logger.warn("Cannot find method {} for class {}.", element.getMethodName(), element.getClassName());
                    continue;
                }
                if (methods.size() > 1) {
                    logger.warn("More than one {} method exists for class {}.", element.getMethodName(), element.getClassName());
                    continue;
                }
                returnType = methods.getFirst().getReturnType();
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
                forEachInterface(this.clazz, iface -> interfaces.add(iface));
            }
        }

        @Override
        public int hashCode() {
            return clazz.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof EntityInfo ei) {
                return ei.clazz == clazz;
            } else {
                return false;
            }
        }

        private String getSimpleName(Class<?> clazz) {
            String rawName = clazz.getName();
            int index = rawName.lastIndexOf('.');
            if (index < 0) {
                return rawName;
            } else {
                return rawName.substring(index + 1);
            }
        }
    }
}