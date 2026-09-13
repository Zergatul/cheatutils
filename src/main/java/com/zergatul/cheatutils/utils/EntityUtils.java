package com.zergatul.cheatutils.utils;

import com.zergatul.scripting.utility.Lists;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;

public class EntityUtils {

    private static final Logger logger = LogManager.getLogger(EntityUtils.class);

    private static final ThreadLocal<Boolean> isInProgress = new ThreadLocal<>();
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

    public static boolean isInProgress() {
        return Boolean.TRUE.equals(isInProgress.get());
    }

    private static synchronized void loadEntityClasses() {
        if (classes != null) {
            return;
        }

        isInProgress.set(true);
        try {
            loadEntityClassesInternal();
        } finally {
            isInProgress.set(false);
        }
    }

    private static void loadEntityClassesInternal() {
        List<EntityInfo> finalClasses = new ArrayList<>();
        Set<EntityInfo> set = new HashSet<>();

        EntityInfo info1 = new EntityInfo(EntityPlayer.class, "minecraft:player");
        finalClasses.add(info1);
        set.add(info1);

        EntityInfo info2 = new EntityInfo(EntityPlayerSP.class);
        finalClasses.add(info2);
        set.add(info2);

        EntityInfo info3 = new EntityInfo(EntityPlayerMP.class);
        finalClasses.add(info3);
        set.add(info3);

        ForgeRegistries.ENTITIES.getValuesCollection().stream().map(et -> {
            ResourceLocation location = ForgeRegistries.ENTITIES.getKey(et);
            if (location == null) {
                return null;
            }

            String key = location.toString();
            Class<?> entityClass;
            try {
                try {
                    // we know it is not OK to pass null as parameter, but we do this explicitly to trigger exception
                    @SuppressWarnings("ConstantConditions")
                    Entity entity = et.newInstance(null);
                    entityClass = entity.getClass();
                } catch (Throwable throwable) {
                    Throwable exception;
                    if (throwable instanceof InvocationTargetException) {
                        exception = ((InvocationTargetException) throwable).getTargetException();
                    } else {
                        exception = throwable;
                    }

                    Optional<Class<?>> optional = findEntityClassFromException(exception);
                    if (optional.isPresent()) {
                        entityClass = optional.get();
                    } else {
                        logger.warn("Cannot figure out entity class name from stacktrace for {}.", key);
                        logger.warn("Exception", exception);
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
        for (Class<?> _interface : interfaces) {
            try {
                classes.add(new EntityInfo(_interface));
            } catch (Exception ex) {
                logger.warn("Cannot create EntityInfo for interface {}.", _interface.getName(), ex);
            }
        }

        classes = Lists.from(classes.stream().sorted((i1, i2) -> i1.simpleName.compareToIgnoreCase(i2.simpleName)));

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

        List<Class<?>> ctorCandidates = new ArrayList<>();
        List<Class<?>> methodCandidates = new ArrayList<>();

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

            if (element.getMethodName().equals("<init>")) {
                // for constructor, we simply use current class
                if (canBeValidEntityClass(clazz)) {
                    ctorCandidates.add(clazz);
                }
            } else {
                // for methods, we have to check return value
                for (Method method : clazz.getDeclaredMethods()) {
                    if (!method.getName().equals(element.getMethodName())) {
                        continue;
                    }
                    if (canBeValidEntityClass(method.getReturnType())) {
                        methodCandidates.add(method.getReturnType());
                    }
                }
            }
        }

        if (!ctorCandidates.isEmpty()) {
            return Optional.of(ctorCandidates.get(0));
        }
        if (!methodCandidates.isEmpty()) {
            return Optional.of(methodCandidates.get(0));
        }
        return Optional.empty();
    }

    private static boolean canBeValidEntityClass(Class<?> clazz) {
        if (!Entity.class.isAssignableFrom(clazz)) {
            return false;
        }
        if (Modifier.isAbstract(clazz.getModifiers())) {
            return false;
        }
        return true;
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
            if (obj instanceof EntityInfo) {
                EntityInfo other = (EntityInfo) obj;
                return other.clazz == clazz;
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