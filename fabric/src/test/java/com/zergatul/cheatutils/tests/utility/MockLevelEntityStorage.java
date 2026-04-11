package com.zergatul.cheatutils.tests.utility;

import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@NullMarked
public class MockLevelEntityStorage implements LevelEntityGetter<Entity> {

    private final List<Entity> entities = new ArrayList<>();

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    @Override
    public @Nullable Entity get(int id) {
        return entities.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
    }

    @Override
    public @Nullable Entity get(UUID id) {
        return entities.stream().filter(e -> e.getUUID().equals(id)).findFirst().orElse(null);
    }

    @Override
    public Iterable<Entity> getAll() {
        return entities;
    }

    @Override
    public <U extends Entity> void get(EntityTypeTest<Entity, U> type, AbortableIterationConsumer<U> consumer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void get(AABB bb, Consumer<Entity> output) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <U extends Entity> void get(EntityTypeTest<Entity, U> type, AABB bb, AbortableIterationConsumer<U> consumer) {
        throw new UnsupportedOperationException();
    }
}