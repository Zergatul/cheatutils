package com.zergatul.cheatutils.modules.esp.block.web;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.concurrent.ClientThread;
import com.zergatul.cheatutils.configs.SettingUpdateResult;
import com.zergatul.cheatutils.configs.modules.BlockEspEntry;
import com.zergatul.cheatutils.modules.esp.block.BlockEsp;
import com.zergatul.cheatutils.modules.esp.block.BlockGroupKey;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;
import com.zergatul.cheatutils.scripting.workspace.slots.MultiScriptSlot;
import com.zergatul.cheatutils.web.ApiException;
import com.zergatul.cheatutils.web.HttpResponseCodes;
import com.zergatul.cheatutils.web.WebApiBase;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public class ConfigWebApi extends WebApiBase {

    @Override
    public String getRoute() {
        return "blocks";
    }

    @Override
    public String get() {
        Object[] result;
        ImmutableList<BlockEspEntry> entries = BlockEsp.INSTANCE.getSettings().get().entries();
        result = entries.stream().toArray();
        return gson.toJson(result);
    }

    @Override
    public String post(String body) throws Exception {
        String id = gson.fromJson(body, String.class);
        Identifier loc = Identifier.parse(id);
        if (loc.equals(BuiltInRegistries.BLOCK.getDefaultKey())) {
            throw new ApiException("Cannot find block by id.", HttpResponseCodes.BAD_REQUEST);
        }

        Block block = BuiltInRegistries.BLOCK.getValue(loc);
        if (BuiltInRegistries.BLOCK.getKey(block).equals(BuiltInRegistries.BLOCK.getDefaultKey())) {
            throw new ApiException("Cannot find block by id.", HttpResponseCodes.BAD_REQUEST);
        }

        return BlockEsp.INSTANCE.getSettings().updateWithResult(config -> {
            if (config.entries().stream().anyMatch(e -> e.blocks().contains(block))) {
                throw new ApiException("Selected block is already part of other BlockEspEntry.", HttpResponseCodes.BAD_REQUEST);
            }

            BlockEspEntry newEntry = BlockEspEntry.defaults().withBlocks(ImmutableList.from(block));
            return new SettingUpdateResult<>(
                    config.withEntries(config.entries().add(newEntry)),
                    gson.toJson(newEntry));
        });
    }

    @Override
    public String put(String id, String body) throws Exception {
        BlockGroupKey key = BlockGroupKey.parse(id);

        // verify blocks inside BlockGroupKey are good by any normalization rules inside BlockEspEntry
        if (!key.isValid()) {
            throw new ApiException("Group key is not valid.", HttpResponseCodes.BAD_REQUEST);
        }

        BlockEspEntry.UpdateOptions request = gson.fromJson(body, BlockEspEntry.UpdateOptions.class);
        if (request.blocks().isEmpty()) {
            throw new ApiException("Blocks cannot be empty array.", HttpResponseCodes.BAD_REQUEST);
        }

        if (!new BlockGroupKey(request.blocks()).isValid()) {
            throw new ApiException("New group key is not valid.", HttpResponseCodes.BAD_REQUEST);
        }

        // Blocks identify the group script, so a change of blocks also renames the script.
        // Config and script change together on the client thread, which owns script state.
        return ClientThread.call(() -> {
            PutResult result = BlockEsp.INSTANCE.getSettings().updateWithResult(config -> {
                BlockEspEntry oldEntry = key.findIn(config.entries());
                if (oldEntry == null) {
                    throw new ApiException("Block group not found. Please refresh the page.", HttpResponseCodes.BAD_REQUEST);
                }

                for (BlockEspEntry entry : config.entries()) {
                    if (entry == oldEntry) {
                        continue;
                    }
                    if (entry.blocks().intersects(request.blocks())) {
                        throw new ApiException("New block group intersects with another group. Please refresh the page.", HttpResponseCodes.BAD_REQUEST);
                    }
                }

                BlockEspEntry newEntry = oldEntry.withOptions(request);
                return new SettingUpdateResult<>(
                        config.withEntries(config.entries().replaceFirst(e -> e == oldEntry, _ -> newEntry)),
                        new PutResult(new BlockGroupKey(newEntry.blocks()), gson.toJson(newEntry)));
            });

            if (!key.equals(result.newKey())) {
                getScriptSlot().move(key.toString(), result.newKey().toString());
            }

            return result.json();
        });
    }

    @Override
    public String delete(String id) throws Exception {
        BlockGroupKey key = BlockGroupKey.parse(id);

        return ClientThread.call(() -> {
            BlockEsp.INSTANCE.getSettings().update(config -> {
                BlockEspEntry entry = key.findIn(config.entries());
                if (entry == null) {
                    throw new ApiException("Block group not found. Please refresh the page.", HttpResponseCodes.BAD_REQUEST);
                }

                return config.withEntries(config.entries().remove(entry));
            });

            getScriptSlot().remove(key.toString());
            return "{ \"ok\": true }";
        });
    }

    private static MultiScriptSlot getScriptSlot() {
        return (MultiScriptSlot) ScriptWorkspace.INSTANCE.get(ScriptType.BLOCK_ESP);
    }

    private record PutResult(BlockGroupKey newKey, String json) {}
}