package com.zergatul.cheatutils.controllers;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.zergatul.cheatutils.ModMain;
import com.zergatul.cheatutils.interfaces.CameraMixinInterface;
import com.zergatul.cheatutils.interfaces.ClientboundPlayerInfoPacketMixinInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.*;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class FreeCamController {

    public static final FreeCamController instance = new FreeCamController();

    private static final int fakePlayerEntityId = Integer.MIN_VALUE;
    private static final UUID fakePlayerProfileUUID = new UUID(0x1234567812345678L, 0x1234567812345678L);
    private static final String fakePlayerName = "FakePlayerName";
    private static final GameProfile fakeProfile = new GameProfile(fakePlayerProfileUUID, fakePlayerName);
    private static final UUID shadowCopyPlayerProfileUUID = new UUID(0x1234567812345678L, 0x1234567812345679L);
    private static final int shadowCopyPlayerEntityId = Integer.MIN_VALUE + 1;

    public GameProfile profileOverride;

    private boolean active = false;
    private Minecraft mc = Minecraft.getInstance();
    private LocalPlayer player;
    private FakePlayer fake;
    private ShadowCopyPlayer shadow;
    private GameType oldGameType;
    private Input oldInput;
    private Abilities oldAbilities;
    private Set<Packet<?>> dontSkip = new HashSet<>();
    private final Logger logger = LogManager.getLogger(FreeCamController.class);

    private FreeCamController() {
        NetworkPacketsController.instance.addServerPacketHandler(this::onServerPacket);
        NetworkPacketsController.instance.addClientPacketHandler(this::onClientPacket);

        //NetworkPacketsController.instance.addServerPacketHandler(this::onServerPacketTemp);
        //NetworkPacketsController.instance.addClientPacketHandler(this::onClientPacketTemp);
    }

    private void onClientPacketTemp(NetworkPacketsController.ClientPacketArgs clientPacketArgs) {
        ModMain.LOGGER.info("Client: " + clientPacketArgs.packet.getClass().getName());
    }

    private void onServerPacketTemp(NetworkPacketsController.ServerPacketArgs serverPacketArgs) {
        if (serverPacketArgs.packet instanceof ClientboundRotateHeadPacket) {
            var packet = (ClientboundRotateHeadPacket) serverPacketArgs.packet;
            if (!(packet.getEntity(mc.level) instanceof Player)) {
                return;
            }
        }
        if (serverPacketArgs.packet instanceof ClientboundSetEntityMotionPacket) {
            var packet = (ClientboundSetEntityMotionPacket) serverPacketArgs.packet;
            if (!(mc.level.getEntity(packet.getId()) instanceof Player)) {
                return;
            }
        }
        if (serverPacketArgs.packet instanceof ClientboundMoveEntityPacket) {
            var packet = (ClientboundMoveEntityPacket) serverPacketArgs.packet;
            if (!(packet.getEntity(mc.level) instanceof Player)) {
                return;
            }
        }
        if (serverPacketArgs.packet instanceof ClientboundTeleportEntityPacket) {
            var packet = (ClientboundTeleportEntityPacket) serverPacketArgs.packet;
            if (!(mc.level.getEntity(packet.getId()) instanceof Player)) {
                return;
            }
        }
        if (serverPacketArgs.packet instanceof ClientboundUpdateAttributesPacket) {
            var packet = (ClientboundUpdateAttributesPacket) serverPacketArgs.packet;
            if (!(mc.level.getEntity(packet.getEntityId()) instanceof Player)) {
                return;
            }
        }
        if (serverPacketArgs.packet instanceof ClientboundEntityEventPacket) {
            var packet = (ClientboundEntityEventPacket) serverPacketArgs.packet;
            if (!(packet.getEntity(mc.level) instanceof Player)) {
                return;
            }
        }
        if (serverPacketArgs.packet instanceof ClientboundSetEntityDataPacket) {
            var packet = (ClientboundSetEntityDataPacket) serverPacketArgs.packet;
            if (!(mc.level.getEntity(packet.getId()) instanceof Player)) {
                return;
            }
        }
        if (serverPacketArgs.packet instanceof ClientboundAddEntityPacket) {
            var packet = (ClientboundAddEntityPacket) serverPacketArgs.packet;
            if (mc.level.getEntity(packet.getId()) instanceof Player) {
                ModMain.LOGGER.info("Player added");
            }
            return;
        }
        if (serverPacketArgs.packet instanceof ClientboundRemoveEntitiesPacket) {
            var packet = (ClientboundRemoveEntitiesPacket) serverPacketArgs.packet;
            for (int id : packet.getEntityIds()) {
                var entity = mc.level.getEntity(id);
                if (entity instanceof Player) {
                    ModMain.LOGGER.info("Player removed");
                }
            }
            return;
        }

        if (serverPacketArgs.packet instanceof ClientboundLevelChunkWithLightPacket) {
            return;
        }


        //
        //
        //
        //
        //

        ModMain.LOGGER.info("Server: " + serverPacketArgs.packet.getClass().getName());

        if (serverPacketArgs.packet instanceof ClientboundPlayerPositionPacket) {
            var packet = (ClientboundPlayerPositionPacket) serverPacketArgs.packet;
            logger.info("Pos: id={} coo={};{};{}", packet.getId(), (int)packet.getX(), (int)packet.getY(), (int)packet.getZ());
        }
    }

    public void toggle() {
        synchronized (this) {
            active = !active;
            if (active) {
                enable();
            } else {
                disable();
            }
        }
    }

    public boolean isActive() {
        return active;
    }

    public LocalPlayer getPlayer() {
        return player;
    }

    public RemotePlayer getShadow() {
        return shadow;
    }

    public UUID getShadowCopyUUID() {
        return shadowCopyPlayerProfileUUID;
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (active) {
            toggle();
        }
    }

    @SubscribeEvent
    public void onClickInput(InputEvent.ClickInputEvent event) {
        // disable crash if you try to attack itself
        if (active) {
            if (event.isAttack() && event.getHand() == InteractionHand.MAIN_HAND && event.getKeyMapping() == mc.options.keyAttack) {
                if (mc.hitResult.getType() == HitResult.Type.ENTITY) {
                    Entity entity = ((EntityHitResult) mc.hitResult).getEntity();
                    if (entity == player) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    private void enable() {

        player = mc.player;
        oldInput = player.input;
        player.input = new FakeInput();
        saveAbilities();

        profileOverride = fakeProfile;
        try {
            fake = new FakePlayer(mc);
        } finally {
            profileOverride = null;
        }

        mc.level.addPlayer(fakePlayerEntityId, fake);
        /*ClientChunkCache chunkProvider = mc.level.getChunkSource();
        ChunkAccess chunk = chunkProvider.getChunk(Mth.floor(fake.getX() / 16.0D), Mth.floor(fake.getZ() / 16.0D), ChunkStatus.FULL, false);
        if (chunk != null) {
            chunk.addEntity(fake);
        }*/

        fake.setPos(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        fake.absMoveTo(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYRot(), mc.player.getXRot());
        fake.setDeltaMovement(0, 0, 0);

        mc.player = fake;
        mc.cameraEntity = fake;
        ((CameraMixinInterface) mc.gameRenderer.getMainCamera()).setEntity(fake);

        oldGameType = mc.gameMode.getPlayerMode();
        mc.gameMode.setLocalMode(GameType.SPECTATOR);

        shadow = new ShadowCopyPlayer(player, createShadowGameProfile());
        mc.level.addPlayer(shadowCopyPlayerEntityId, shadow);

        var packet = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER);
        var list = new ArrayList<ClientboundPlayerInfoPacket.PlayerUpdate>();
        list.add(new ClientboundPlayerInfoPacket.PlayerUpdate(fakeProfile, 0, GameType.SPECTATOR, null, null));
        list.add(new ClientboundPlayerInfoPacket.PlayerUpdate(shadow.getGameProfile(), 1, GameType.DEFAULT_MODE, null, null));
        ((ClientboundPlayerInfoPacketMixinInterface) packet).setEntries(list);
        player.connection.handlePlayerInfo(packet);

        //setPlayerAbilities(new Abilities());

        /*mc.player.getAbilities().mayfly = true;
        mc.player.getAbilities().flying = true;
        mc.gameMode.setLocalMode(GameType.SPECTATOR);
        mc.player.setGameMode(GameType.SPECTATOR);
        networkPlayerInfoSetGameMode(GameType.SPECTATOR);*/
    }

    private void disable() {

        player.input = oldInput;
        restoreAbilities();

        mc.player = player;
        mc.cameraEntity = player;
        ((CameraMixinInterface) mc.gameRenderer.getMainCamera()).setEntity(player);

        if (mc.gameMode != null) {
            mc.gameMode.setLocalMode(oldGameType);
        }

        removeClientPlayer(fake);
        removeClientPlayer(shadow);

        fake = null;
        shadow = null;
    }

    private GameProfile createShadowGameProfile() {
        GameProfile shadowProfile = new GameProfile(shadowCopyPlayerProfileUUID, player.getGameProfile().getName());
        GameProfile mainProfile = player.getGameProfile();
        for (Map.Entry<String, Property> entry: mainProfile.getProperties().entries()) {
            shadowProfile.getProperties().put(entry.getKey(), entry.getValue());
        }
        return shadowProfile;
    }

    private void saveAbilities() {
        var abilities = player.getAbilities();
        oldAbilities = new Abilities();
        oldAbilities.mayfly = abilities.mayfly;
        oldAbilities.flying = abilities.flying;
        oldAbilities.instabuild = abilities.instabuild;
        oldAbilities.invulnerable = abilities.invulnerable;
        oldAbilities.mayBuild = abilities.mayfly;
        oldAbilities.setWalkingSpeed(abilities.getWalkingSpeed());
        oldAbilities.setFlyingSpeed(abilities.getFlyingSpeed());
    }

    private void restoreAbilities() {
        var abilities = player.getAbilities();
        abilities.mayfly = oldAbilities.mayfly;
        abilities.flying = oldAbilities.flying;
        abilities.instabuild = oldAbilities.instabuild;
        abilities.invulnerable = oldAbilities.invulnerable;
        abilities.mayBuild = oldAbilities.mayfly;
        abilities.setWalkingSpeed(oldAbilities.getWalkingSpeed());
        abilities.setFlyingSpeed(oldAbilities.getFlyingSpeed());
    }

    private void removeClientPlayer(AbstractClientPlayer clientPlayer) {
        clientPlayer.remove(Entity.RemovalReason.DISCARDED);

        var packet = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER);
        var list = new ArrayList<ClientboundPlayerInfoPacket.PlayerUpdate>();
        list.add(new ClientboundPlayerInfoPacket.PlayerUpdate(clientPlayer.getGameProfile(), 0, GameType.DEFAULT_MODE, null, null));
        ((ClientboundPlayerInfoPacketMixinInterface) packet).setEntries(list);
        player.connection.handlePlayerInfo(packet);
    }

    private void onServerPacket(NetworkPacketsController.ServerPacketArgs args) {

        if (active) {
            if (args.packet instanceof ClientboundPlayerPositionPacket) {
                /*SPlayerPositionLookPacket packet = (SPlayerPositionLookPacket)args.packet;
                handlePlayerPositionLook(packet);
                NetworkPacketsController.instance.sendPacket(new CConfirmTeleportPacket(packet.getId()));
                CPlayerPacket.PositionRotationPacket posRotPacket = new CPlayerPacket.PositionRotationPacket(packet.getX(), packet.getY(), packet.getZ(), packet.getYRot(), packet.getXRot(), false);
                synchronized (dontSkip) {
                    dontSkip.add(posRotPacket);
                }
                NetworkPacketsController.instance.sendPacket(posRotPacket);
                args.skip = true;*/
                return;
            }
            /*if (args.packet instanceof SChangeGameStatePacket) {
                args.skip = true;
                return;
            }*/
            /*if (args.packet instanceof ClientboundPlayerAbilitiesPacket) {
                ClientboundPlayerAbilitiesPacket packet = (ClientboundPlayerAbilitiesPacket) args.packet;
                fake.getAbilities().flying = packet.isFlying();
                fake.getAbilities().instabuild = packet.canInstabuild();
                fake.getAbilities().invulnerable = packet.isInvulnerable();
                fake.getAbilities().mayfly = packet.canFly();
                fake.getAbilities().setFlyingSpeed(packet.getFlyingSpeed());
                fake.getAbilities().setWalkingSpeed(packet.getWalkingSpeed());
                args.skip = true;
                return;
            }*/
            /*if (args.packet instanceof SEntityVelocityPacket) {
                SEntityVelocityPacket packet = (ClientBoundEntity)args.packet;
                if (mc.player.getId() == packet.getId()) {
                    if (fake != null) {
                        fake.lerpMotion(packet.getXa() / 8000d, packet.getYa() / 8000d, packet.getZa() / 8000d);
                        args.skip = true;
                    }
                }
                return;
            }*/

            /*if (args.packet instanceof ClientboundMoveEntityPacket) {
                ClientboundMoveEntityPacket packet = (ClientboundMoveEntityPacket) args.packet;
                Entity entity = packet.getEntity(mc.level);
                if (entity instanceof LocalPlayer) {
                    ((SEntityPacketMixinInterface)packet).setEntityId(fake.getId());
                }
                return;
            }*/
            if (args.packet instanceof ClientboundEntityEventPacket) {
                ClientboundEntityEventPacket packet = (ClientboundEntityEventPacket) args.packet;
                if (packet.getEntity(this.mc.level) == player && packet.getEventId() != 35 && packet.getEventId() != 21) {
                    if (active) {
                        toggle();
                    }
                }
                return;
            }
        }
    }

    private void onClientPacket(NetworkPacketsController.ClientPacketArgs args) {

        if (active) {
            if (args.packet instanceof ServerboundMovePlayerPacket.StatusOnly) {
                args.packet = new ServerboundMovePlayerPacket.StatusOnly(
                        player.isOnGround());
                return;
            }
            if (args.packet instanceof ServerboundMovePlayerPacket.Rot) {
                args.packet = new ServerboundMovePlayerPacket.Rot(
                        player.getYRot(),
                        player.getXRot(),
                        player.isOnGround());
                return;
            }
            if (args.packet instanceof ServerboundMovePlayerPacket.Pos) {
                args.packet = new ServerboundMovePlayerPacket.Pos(
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        player.isOnGround());
                return;
            }
            if (args.packet instanceof ServerboundMovePlayerPacket.PosRot) {
                args.packet = new ServerboundMovePlayerPacket.PosRot(
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        player.getYRot(),
                        player.getXRot(),
                        player.isOnGround());
                return;
            }
            if (args.packet instanceof ServerboundMovePlayerPacket) {
                logger.error("Subclass case is missing {}", args.packet.getClass().getName());
                return;
            }
        }
    }

    /*private void handlePlayerPositionLook(SPlayerPositionLookPacket packet) {

        Vector3d vec3d = fake.getDeltaMovement();

        boolean hasX = packet.getRelativeArguments().contains(SPlayerPositionLookPacket.Flags.X);
        boolean hasY = packet.getRelativeArguments().contains(SPlayerPositionLookPacket.Flags.Y);
        boolean hasZ = packet.getRelativeArguments().contains(SPlayerPositionLookPacket.Flags.Z);

        double dx;
        double newX;
        if (hasX) {
            dx = vec3d.x();
            newX = fake.getX() + packet.getX();
            fake.xOld += packet.getX();
        } else {
            dx = 0.0D;
            newX = packet.getX();
            fake.xOld = newX;
        }

        double dy;
        double newY;
        if (hasY) {
            dy = vec3d.y();
            newY = fake.getY() + packet.getY();
            fake.yOld += packet.getY();
        } else {
            dy = 0.0D;
            newY = packet.getY();
            fake.yOld = newY;
        }

        double dz;
        double newZ;
        if (hasZ) {
            dz = vec3d.z();
            newZ = fake.getZ() + packet.getZ();
            fake.zOld += packet.getZ();
        } else {
            dz = 0.0D;
            newZ = packet.getZ();
            fake.zOld = newZ;
        }

        fake.setPosRaw(newX, newY, newZ);
        fake.xo = newX;
        fake.yo = newY;
        fake.zo = newZ;
        fake.setDeltaMovement(dx, dy, dz);
        float yRot = packet.getYRot();
        float xRot = packet.getXRot();
        if (packet.getRelativeArguments().contains(SPlayerPositionLookPacket.Flags.X_ROT)) {
            xRot += fake.xRot;
        }

        if (packet.getRelativeArguments().contains(SPlayerPositionLookPacket.Flags.Y_ROT)) {
            yRot += fake.yRot;
        }

        fake.absMoveTo(newX, newY, newZ, yRot, xRot);
        fake.setPosAndOldPos(newX, newY, newZ);
    }

    private void setPlayerAbilities(Abilities abilities) {
        ((PlayerEntityMixinInterface)mc.player).setAbilities(abilities);
    }

    private void networkPlayerInfoSetGameMode(GameType type) {
        PlayerInfo playerInfo = mc.player.connection.getPlayerInfo(mc.player.getGameProfile().getId());
        ((NetworkPlayerInfoMixinInterface)playerInfo).forceSetGameMode(type);
    }*/

    public static class FakePlayer extends LocalPlayer {

        public FakePlayer(Minecraft mc) {
            super(mc, mc.player.clientLevel, mc.player.connection, mc.player.getStats(), mc.player.getRecipeBook(), false, false);

            input = new KeyboardInput(mc.options);

            getAbilities().flying = true;
            getAbilities().instabuild = true;
            getAbilities().invulnerable = true;
            getAbilities().mayfly = true;

            uuid = Mth.createInsecureUUID();
        }

        @Override
        public boolean isSpectator() {
            return true;
        }
    }

    public static class ShadowCopyPlayer extends RemotePlayer {

        public ShadowCopyPlayer(LocalPlayer player, GameProfile gameProfile) {
            super(player.clientLevel, gameProfile, null);

            this.setPos(player.getX(), player.getY(), player.getZ());
            this.setRot(player.getYRot(), player.getXRot());
            this.setYHeadRot(player.getYHeadRot());

            copyItems(player.getInventory().armor, this.getInventory().armor);
            copyItems(player.getInventory().items, this.getInventory().items);
            copyItems(player.getInventory().offhand, this.getInventory().offhand);

            this.getInventory().selected = player.getInventory().selected;

            this.getEntityData().assignValues(player.getEntityData().getAll());
        }

        private static void copyItems(NonNullList<ItemStack> source, NonNullList<ItemStack> destination) {
            for (int i = 0; i < source.size(); i++) {
                destination.set(i, source.get(i));
            }
        }
    }

    private static class FakeInput extends Input {
        @Override
        public void tick(boolean p_108576_, float p_234116_) {
            if (p_108576_) {
                this.leftImpulse *= 0.3F;
                this.forwardImpulse *= 0.3F;
            }
        }
    }

}
