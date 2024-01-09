package com.zergatul.cheatutils.modules.esp;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.zergatul.cheatutils.collections.LinesIterable;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ProjectilePathConfig;
import com.zergatul.cheatutils.modules.utilities.RenderUtilities;
import com.zergatul.cheatutils.render.DebugLinesLineRenderer;
import com.zergatul.cheatutils.render.LineRenderer;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.List;

public class ProjectilePath {

    public static final ProjectilePath instance = new ProjectilePath();

    private final Minecraft mc = Minecraft.getInstance();
    private final List<ThrowableItemEntry> entries = Lists.newArrayList(
            new EnderPearlEntry(),
            new SnowballEntry(),
            new EggEntry(),
            new PotionEntry(),
            new ExpBottleEntry(),
            new BowItemEntry(),
            new CrossbowItemEntry(),
            new TridentItemEntry());
    private final Map<Integer, LinkedList<TraceRecord>> traces = new HashMap<>();

    // TODO: highlight block?

    private ProjectilePath() {
        Events.AfterRenderWorld.add(this::render);
    }

    private void render(RenderWorldLastEvent event) {
        if (mc.level == null) {
            return;
        }

        if (!ConfigStore.instance.getConfig().esp) {
            return;
        }

        ProjectilePathConfig config = getConfig();
        if (config.showTraces) {
            long time = System.nanoTime();
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity instanceof Projectile) {
                    if (!traces.containsKey(entity.getId())) {
                        traces.put(entity.getId(), new LinkedList<>());
                    }

                    LinkedList<TraceRecord> list = traces.get(entity.getId());
                    Vec3 position = entity.getPosition(event.getTickDelta());
                    if (list.size() == 0) {
                        list.addFirst(new TraceRecord(position, time));
                    } else {
                        TraceRecord first = list.getFirst();
                        if (first.position.distanceToSqr(position) > 0.01) {
                            list.addFirst(new TraceRecord(position, time));
                        } else {
                            first.position = position;
                            first.time = time;
                        }
                    }
                }
            }

            Iterator<Map.Entry<Integer, LinkedList<TraceRecord>>> iterator = traces.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, LinkedList<TraceRecord>> entry = iterator.next();
                LinkedList<TraceRecord> list = entry.getValue();
                while (list.size() > 0 && list.getLast().time + config.tracesDuration * 1000000000L < time) {
                    list.removeLast();
                }
                if (list.size() == 0) {
                    iterator.remove();
                }
            }

            // rendering

            LineRenderer renderer = new DebugLinesLineRenderer();
            renderer.begin(event, true);

            for (List<TraceRecord> list : traces.values()) {
                if (list.size() < 2) {
                    continue;
                }

                for (Pair<TraceRecord, TraceRecord> pair : new LinesIterable<>(list)) {
                    TraceRecord r1 = pair.getFirst();
                    TraceRecord r2 = pair.getSecond();
                    long remain1 = r1.time + config.tracesDuration * 1000000000L - time;
                    float alpha1 = Math.min(1f, remain1 / 1e9f / config.fadeDuration);
                    long remain2 = r2.time + config.tracesDuration * 1000000000L - time;
                    float alpha2 = Math.min(1f, remain2 / 1e9f / config.fadeDuration);
                    renderer.line(
                            r1.position.x, r1.position.y, r1.position.z,
                            0.5f, 1f, 0.5f, alpha1,
                            r2.position.x, r2.position.y, r2.position.z,
                            0.5f, 1f, 0.5f, alpha2);
                }
            }

            renderer.end();
        }

        ThrowableItemEntry entry = null;
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).shouldDraw()) {
                entry = entries.get(i);
                break;
            }
        }

        if (entry == null) {
            return;
        }

        if (mc.player == null) {
            return;
        }

        Vec3 playerPos = event.getPlayerPos();
        float partialTick = event.getTickDelta();

        double x = playerPos.x;
        double y = playerPos.y + mc.player.getEyeHeight();
        double z = playerPos.z;
        Vec3 from = new Vec3(x, y, z);
        float xRot = mc.player.getViewXRot(partialTick);
        float yRot = mc.player.getViewYRot(partialTick);

        double shiftX = -Mth.sin((yRot + 90) * ((float)Math.PI / 180F));
        double shiftY = -0.5;
        double shiftZ = Mth.cos((yRot + 90) * ((float)Math.PI / 180F));
        Vec3 shift = new Vec3(shiftX, shiftY, shiftZ);

        float speedX = -Mth.sin(yRot * ((float)Math.PI / 180F)) * Mth.cos(xRot * ((float)Math.PI / 180F));
        float speedY = -Mth.sin((xRot + entry.getXRotDelta()) * ((float)Math.PI / 180F));
        float speedZ = Mth.cos(yRot * ((float)Math.PI / 180F)) * Mth.cos(xRot * ((float)Math.PI / 180F));
        Vec3 movement = new Vec3(speedX, speedY, speedZ).normalize().scale(entry.getSpeed());

        Vec3 playerSpeed = mc.player.getDeltaMovement();
        if (mc.player.onGround()) {
            playerSpeed = playerSpeed.subtract(0, playerSpeed.y, 0);
        }
        movement = movement.add(playerSpeed);

        /*final double deviationBase = 0.0172275D;
        double dev = deviationBase * entry.getDeviation();
        List<Vec3> deviations = new ArrayList<>(8);
        deviations.add(movement.add(dev, dev, dev));
        deviations.add(movement.subtract(dev, dev, dev));
        deviations.add(movement.add(dev, dev, 0));
        deviations.add(movement.subtract(dev, dev, 0));
        deviations.add(movement.add(dev, 0, dev));
        deviations.add(movement.subtract(dev, 0, dev));
        deviations.add(movement.add(0, dev, dev));
        deviations.add(movement.subtract(0, dev, dev));*/

        /*Vec3 vec = mc.player.getDeltaMovement();
        movement = movement.add(vec.x, mc.player.isOnGround() ? 0.0D : vec.y, vec.z);*/

        LineRenderer renderer = RenderUtilities.instance.getLineRenderer();
        renderer.begin(event, true);

        List<Vec3> mainPath = calculatePath(entry, from, movement, shift);
        drawPath(mainPath, renderer);

        /*for (Vec3 devSpeed : deviations) {
            List<Vec3> devPath = calculatePath(entry, from, devSpeed, shift, view);
            drawPath(devPath, buffer, Color.LIGHT_GRAY.getRGB());
        }*/

        renderer.end();
    }

    private ProjectilePathConfig getConfig() {
        return ConfigStore.instance.getConfig().projectilePathConfig;
    }

    private List<Vec3> calculatePath(ThrowableItemEntry entry, Vec3 from, Vec3 speed, Vec3 shift) {
        List<Vec3> list = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            list.add(from.add(shift.scale(getShiftFactor(i))));
            from = from.add(speed);
            speed = speed.scale(entry.getResistance()).add(0, -entry.getGravity(), 0);
        }
        return list;
    }

    private void drawPath(List<Vec3> path, LineRenderer renderer) {
        for (int i = 0; i < path.size() - 1; i++) {
            Vec3 point1 = path.get(i);
            Vec3 point2 = path.get(i + 1);
            renderer.line(point1.x, point1.y, point1.z, point2.x, point2.y, point2.z, 1f, 1f, 1f, 1f);
        }
    }

    private double getShiftFactor(int step) {
        return 1d / (4 + 1d * step * step / 20);
    }

    private ItemStack getItemStackInHand() {
        LocalPlayer player = mc.player;
        if (player == null) {
            return null;
        }

        return player.getItemInHand(InteractionHand.MAIN_HAND);
    }

    private static abstract class ThrowableItemEntry {
        public abstract boolean shouldDraw();
        public abstract float getXRotDelta();
        public abstract float getSpeed();
        public abstract float getGravity();
        public float getResistance() {
            return 0.99F;
        }
        public abstract float getDeviation();
    }

    private abstract class SimpleThrowableItemEntry extends ThrowableItemEntry {

        private final Item item;
        private final float xRotDelta;
        private final float speed;
        private final float deviation;

        public SimpleThrowableItemEntry(Item item, float xRotDelta, float speed, float deviation) {
            this.item = item;
            this.xRotDelta = xRotDelta;
            this.speed = speed;
            this.deviation = deviation;
        }

        @Override
        public boolean shouldDraw() {
            ItemStack itemStack = getItemStackInHand();
            if (itemStack == null) {
                return false;
            }

            return itemStack.getItem() == item;
        }

        @Override
        public float getXRotDelta() {
            return xRotDelta;
        }

        @Override
        public float getSpeed() {
            return speed;
        }

        @Override
        public float getGravity() {
            return 0.03F;
        }

        @Override
        public float getDeviation() {
            return deviation;
        }
    }

    private class EnderPearlEntry extends SimpleThrowableItemEntry {

        public EnderPearlEntry() {
            super(Items.ENDER_PEARL, 0, 1.5f, 1);
        }

        @Override
        public boolean shouldDraw() {
            return getConfig().enderPearls && super.shouldDraw();
        }
    }

    private class SnowballEntry extends SimpleThrowableItemEntry {

        public SnowballEntry() {
            super(Items.SNOWBALL, 0, 1.5f, 1);
        }

        @Override
        public boolean shouldDraw() {
            return getConfig().snowballs && super.shouldDraw();
        }
    }

    private class EggEntry extends SimpleThrowableItemEntry {

        public EggEntry() {
            super(Items.EGG, 0, 1.5f, 1);
        }

        @Override
        public boolean shouldDraw() {
            return getConfig().eggs && super.shouldDraw();
        }
    }

    private class PotionEntry extends SimpleThrowableItemEntry {
        public PotionEntry() {
            super(Items.SPLASH_POTION, -20, 0.5f, 1);
        }

        @Override
        public boolean shouldDraw() {
            return getConfig().potions && super.shouldDraw();
        }

        @Override
        public float getGravity() {
            return 0.05F;
        }
    }

    private class ExpBottleEntry extends SimpleThrowableItemEntry {
        public ExpBottleEntry() {
            super(Items.EXPERIENCE_BOTTLE, -20, 0.7f, 1);
        }

        @Override
        public boolean shouldDraw() {
            return getConfig().expBottles && super.shouldDraw();
        }

        @Override
        public float getGravity() {
            return 0.07F;
        }
    }

    private class BowItemEntry extends ThrowableItemEntry {

        @Override
        public boolean shouldDraw() {
            if (!getConfig().bows) {
                return false;
            }

            LocalPlayer player = mc.player;
            if (player == null) {
                return false;
            }

            ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (itemStack.getItem() != Items.BOW) {
                return false;
            }

            return player.isUsingItem();
        }

        @Override
        public float getXRotDelta() {
            return 0;
        }

        @Override
        public float getSpeed() {
            LocalPlayer player = mc.player;
            if (player == null) {
                return 0;
            }

            int ticks = player.getTicksUsingItem();
            float power = BowItem.getPowerForTime(ticks);
            return power * 3;
        }

        @Override
        public float getGravity() {
            return 0.05F;
        }

        @Override
        public float getDeviation() {
            return 1;
        }
    }

    private class TridentItemEntry extends BowItemEntry {

        @Override
        public boolean shouldDraw() {
            if (!getConfig().tridents) {
                return false;
            }

            LocalPlayer player = mc.player;
            if (player == null) {
                return false;
            }

            ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
            return itemStack.getItem() == Items.TRIDENT;
        }

        @Override
        public float getXRotDelta() {
            return 0;
        }

        @Override
        public float getSpeed() {
            return 2.5f;
        }

        @Override
        public float getDeviation() {
            return 1;
        }
    }

    private class CrossbowItemEntry extends BowItemEntry {

        @Override
        public boolean shouldDraw() {
            if (!getConfig().crossbows) {
                return false;
            }

            ItemStack itemStack = getItemStackInHand();
            if (itemStack == null) {
                return false;
            }

            if (itemStack.getItem() != Items.CROSSBOW) {
                return false;
            }

            return CrossbowItem.isCharged(itemStack);
        }

        @Override
        public float getSpeed() {
            ItemStack itemStack = getItemStackInHand();
            if (itemStack == null) {
                return 0;
            }

            return CrossbowItem.containsChargedProjectile(itemStack, Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
        }
    }

    private static class TraceRecord {

        public Vec3 position;
        public long time;

        public TraceRecord(Vec3 position, long time) {
            this.position = position;
            this.time = time;
        }
    }
}