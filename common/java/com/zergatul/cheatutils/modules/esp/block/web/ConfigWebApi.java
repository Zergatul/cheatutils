package com.zergatul.cheatutils.modules.esp.block.web;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.modules.BlockEspEntry;
import com.zergatul.cheatutils.modules.esp.block.BlockEsp;
import com.zergatul.cheatutils.modules.esp.block.BlockGroupKey;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
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
    public synchronized String get() {
        Object[] result;
        ImmutableList<BlockEspEntry> entries = BlockEsp.INSTANCE.getSettings().get().entries();
        result = entries.stream().toArray();
        return gson.toJson(result);
    }

    @Override
    public synchronized String post(String body) throws ApiException {
        String id = gson.fromJson(body, String.class);
        Identifier loc = Identifier.parse(id);
        if (loc.equals(BuiltInRegistries.BLOCK.getDefaultKey())) {
            throw new ApiException("Cannot find block by id.", HttpResponseCodes.BAD_REQUEST);
        }

        Block block = BuiltInRegistries.BLOCK.getValue(loc);
        if (BuiltInRegistries.BLOCK.getKey(block).equals(BuiltInRegistries.BLOCK.getDefaultKey())) {
            throw new ApiException("Cannot find block by id.", HttpResponseCodes.BAD_REQUEST);
        }

        String[] errorHolder = new String[1];
        BlockEspEntry[] newEntryHolder = new BlockEspEntry[1];

        BlockEsp.INSTANCE.getSettings().update(config -> {
            if (config.entries().stream().anyMatch(e -> e.blocks().contains(block))) {
                errorHolder[0] = "Selected block is already part of other BlockEspEntry.";
                return config;
            }

            BlockEspEntry newEntry = BlockEspEntry.defaults().withBlocks(ImmutableList.from(block));
            newEntryHolder[0] = newEntry;
            return config.withEntries(config.entries().add(newEntry));
        });

        if (errorHolder[0] != null) {
            throw new ApiException(errorHolder[0], HttpResponseCodes.BAD_REQUEST);
        }

        return gson.toJson(newEntryHolder[0]);
    }

    @Override
    public synchronized String put(String id, String body) throws Throwable {
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

        String[] errorHolder = new String[1];
        BlockEspEntry[] newEntryHolder = new BlockEspEntry[1];

        BlockEsp.INSTANCE.getSettings().update(config -> {
            BlockEspEntry oldEntry = key.findIn(config.entries());
            if (oldEntry == null) {
                errorHolder[0] = "Block group not found. Please refresh the page.";
                return config;
            }

            for (BlockEspEntry entry : config.entries()) {
                if (entry == oldEntry) {
                    continue;
                }
                if (entry.blocks().intersects(request.blocks())) {
                    errorHolder[0] = "New block group intersects with another group. Please refresh the page.";
                    return config;
                }
            }

            BlockEspEntry newEntry = oldEntry.withOptions(request);
            newEntryHolder[0] = newEntry;

            BlockGroupKey newKey = new BlockGroupKey(newEntry.blocks());
            if (!key.equals(newKey)) {
                MultiScriptSlot slot = (MultiScriptSlot) ScriptWorkspace.INSTANCE.get(ScriptType.BLOCK_ESP);
                slot.move(key.toString(), newKey.toString());
                BlockEsp.INSTANCE.moveScript(oldEntry.blocks(), newEntry.blocks(), new ScriptRef(ScriptType.BLOCK_ESP, newKey.toString()));
            }

            return config.withEntries(config.entries().replaceFirst(e -> e == oldEntry, _ -> newEntry));
        });

        if (errorHolder[0] != null) {
            throw new ApiException(errorHolder[0], HttpResponseCodes.BAD_REQUEST);
        }

        return gson.toJson(newEntryHolder[0]);
    }

    @Override
    public synchronized String delete(String id) throws ApiException {
        BlockGroupKey key = BlockGroupKey.parse(id);

        String[] errorHolder = new String[1];

        BlockEsp.INSTANCE.getSettings().update(config -> {
            BlockEspEntry entry = key.findIn(config.entries());

            if (entry == null) {
                errorHolder[0] = "Block group not found. Please refresh the page.";
                return config;
            }

            MultiScriptSlot slot = (MultiScriptSlot) ScriptWorkspace.INSTANCE.get(ScriptType.BLOCK_ESP);
            slot.remove(key.toString());

            return config.withEntries(config.entries().remove(entry));
        });

        if (errorHolder[0] != null) {
            throw new ApiException(errorHolder[0], HttpResponseCodes.BAD_REQUEST);
        }

        return "{ \"ok\": true }";
    }
}