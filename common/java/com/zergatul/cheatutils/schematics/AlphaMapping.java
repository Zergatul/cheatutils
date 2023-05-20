package com.zergatul.cheatutils.schematics;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

public class AlphaMapping {

    private static BlockState[] mapping;

    public static synchronized BlockState[] get() {
        if (mapping == null) {
            buildMapping();
        }

        return mapping;
    }

    private static void buildMapping() {
        mapping = new BlockState[65536];
        Arrays.fill(mapping, Blocks.AIR.defaultBlockState());

        int id1 = 0;

        fill(id1++, Blocks.AIR);

        fill(id1, Blocks.STONE);
        fill(id1, 0x01, Blocks.GRANITE);
        fill(id1, 0x02, Blocks.POLISHED_GRANITE);
        fill(id1, 0x03, Blocks.DIORITE);
        fill(id1, 0x04, Blocks.POLISHED_DIORITE);
        fill(id1, 0x05, Blocks.ANDESITE);
        fill(id1, 0x06, Blocks.POLISHED_ANDESITE);
        id1++;

        fill(id1++, Blocks.GRASS_BLOCK);

        fill(id1, Blocks.DIRT);
        fill(id1, 0x01, Blocks.COARSE_DIRT);
        fill(id1, 0x02, Blocks.PODZOL);
        id1++;

        fill(id1++, Blocks.COBBLESTONE);

        fill(id1, Blocks.OAK_PLANKS);
        fill(id1, 0x01, Blocks.SPRUCE_PLANKS);
        fill(id1, 0x02, Blocks.BIRCH_PLANKS);
        fill(id1, 0x03, Blocks.JUNGLE_PLANKS);
        fill(id1, 0x04, Blocks.ACACIA_PLANKS);
        fill(id1, 0x05, Blocks.DARK_OAK_PLANKS);
        id1++;

        fill(id1, Blocks.OAK_SAPLING);
        fill(id1, 0x01, Blocks.SPRUCE_SAPLING);
        fill(id1, 0x02, Blocks.BIRCH_SAPLING);
        fill(id1, 0x03, Blocks.JUNGLE_SAPLING);
        fill(id1, 0x04, Blocks.ACACIA_SAPLING);
        fill(id1, 0x05, Blocks.DARK_OAK_SAPLING);
        id1++;

        fill(id1++, Blocks.BEDROCK);

        fill(id1++, Blocks.WATER);

        fill(id1++, Blocks.WATER);

        fill(id1++, Blocks.LAVA);

        fill(id1++, Blocks.LAVA);

        fill(id1, Blocks.SAND);
        fill(id1, 0x01, Blocks.RED_SAND);
        id1++;

        fill(id1++, Blocks.GRAVEL);

        fill(id1++, Blocks.GOLD_ORE);

        fill(id1++, Blocks.IRON_ORE);

        fill(id1++, Blocks.COAL_ORE);

        fill(id1, Blocks.OAK_LOG);
        fill(id1, 0x01, Blocks.SPRUCE_LOG);
        fill(id1, 0x02, Blocks.BIRCH_LOG);
        fill(id1, 0x03, Blocks.JUNGLE_LOG);
        fill(id1, 0x04, Blocks.ACACIA_LOG);
        fill(id1, 0x05, Blocks.DARK_OAK_LOG);
        id1++;

        fill(id1, Blocks.OAK_LEAVES);
        fill(id1, 0x01, Blocks.SPRUCE_LEAVES);
        fill(id1, 0x02, Blocks.BIRCH_LEAVES);
        fill(id1, 0x03, Blocks.JUNGLE_LEAVES);
        fill(id1, 0x04, Blocks.ACACIA_LEAVES);
        fill(id1, 0x05, Blocks.DARK_OAK_LEAVES);
        id1++;

        fill(id1++, Blocks.SPONGE);

        fill(id1++, Blocks.GLASS);

        fill(id1++, Blocks.LAPIS_ORE);

        fill(id1++, Blocks.LAPIS_BLOCK);

        fill(id1++, Blocks.DISPENSER);

        fill(id1++, Blocks.SANDSTONE);

        fill(id1++, Blocks.NOTE_BLOCK);

        id1++; //fill(0x1A, Blocks.WHITE_BED);

        fill(id1++, Blocks.POWERED_RAIL);

        fill(id1++, Blocks.DETECTOR_RAIL);

        fill(id1++, Blocks.STICKY_PISTON);

        fill(id1++, Blocks.COBWEB);

        fill(id1++, Blocks.TALL_GRASS);

        fill(id1++, Blocks.DEAD_BUSH);

        fill(id1++, Blocks.PISTON);

        fill(id1++, Blocks.PISTON_HEAD);

        fill(id1, Blocks.WHITE_WOOL);
        fill(id1, 0x01, Blocks.ORANGE_WOOL);
        fill(id1, 0x02, Blocks.MAGENTA_WOOL);
        fill(id1, 0x03, Blocks.LIGHT_BLUE_WOOL);
        fill(id1, 0x04, Blocks.YELLOW_WOOL);
        fill(id1, 0x05, Blocks.LIME_WOOL);
        fill(id1, 0x06, Blocks.PINK_WOOL);
        fill(id1, 0x07, Blocks.GRAY_WOOL);
        fill(id1, 0x08, Blocks.LIGHT_GRAY_WOOL);
        fill(id1, 0x09, Blocks.CYAN_WOOL);
        fill(id1, 0x0A, Blocks.PURPLE_WOOL);
        fill(id1, 0x0B, Blocks.BLUE_WOOL);
        fill(id1, 0x0C, Blocks.BROWN_WOOL);
        fill(id1, 0x0D, Blocks.GREEN_WOOL);
        fill(id1, 0x0E, Blocks.RED_WOOL);
        fill(id1, 0x0F, Blocks.BLACK_WOOL);
        id1++;

        id1++; //fill(0x24, null);

        fill(id1++, Blocks.DANDELION);

        fill(id1++, Blocks.POPPY);

        fill(id1++, Blocks.BROWN_MUSHROOM);

        fill(id1++, Blocks.RED_MUSHROOM);

        fill(id1++, Blocks.GOLD_BLOCK);

        fill(id1++, Blocks.IRON_BLOCK);

        id1++; //fill(0x2B, Blocks.STONE_SLAB); // double

        fill(id1++, Blocks.STONE_SLAB);

        fill(id1++, Blocks.BRICKS);

        fill(id1++, Blocks.TNT);

        fill(id1++, Blocks.BOOKSHELF);

        fill(id1++, Blocks.MOSSY_COBBLESTONE);

        fill(id1++, Blocks.OBSIDIAN);

        fill(id1++, Blocks.TORCH);

        fill(id1++, Blocks.FIRE);

        fill(id1++, Blocks.SPAWNER);

        fill(id1++, Blocks.OAK_STAIRS);

        fill(id1++, Blocks.CHEST);

        fill(id1++, Blocks.REDSTONE_WIRE);

        fill(id1++, Blocks.REDSTONE_WIRE);

        fill(id1++, Blocks.DIAMOND_ORE);

        fill(id1++, Blocks.DIAMOND_BLOCK);

        fill(id1++, Blocks.CRAFTING_TABLE);

        fill(id1++, Blocks.WHEAT);

        fill(id1++, Blocks.FARMLAND);

        fill(id1++, Blocks.FURNACE);

        //fill(0x3F, Blocks.FURNACE); // lit
        id1++;

        fill(id1++, Blocks.OAK_SIGN);

        fill(id1++, Blocks.OAK_DOOR);
        fill(id1++, Blocks.LADDER);
        fill(id1++, Blocks.RAIL);
        fill(id1++, Blocks.STONE_STAIRS);
        fill(id1++, Blocks.OAK_WALL_SIGN);
        fill(id1++, Blocks.LEVER);
        fill(id1++, Blocks.STONE_PRESSURE_PLATE);
        fill(id1++, Blocks.IRON_DOOR);
        fill(id1++, Blocks.OAK_PRESSURE_PLATE);
        fill(id1++, Blocks.REDSTONE_ORE);
        fill(id1++, Blocks.REDSTONE_ORE); // lit
        fill(id1++, Blocks.REDSTONE_TORCH); // off
        fill(id1++, Blocks.REDSTONE_TORCH); // on
        fill(id1++, Blocks.STONE_BUTTON);
        fill(id1++, Blocks.SNOW);

        fill(id1++, Blocks.ICE);
        fill(id1++, Blocks.SNOW_BLOCK);
        fill(id1++, Blocks.CACTUS);
        fill(id1++, Blocks.CLAY);
        id1++; //fill(0x54, Blocks.REEDS);
        fill(id1++, Blocks.JUKEBOX);
        fill(id1++, Blocks.OAK_FENCE);
        fill(id1++, Blocks.PUMPKIN);
        fill(id1++, Blocks.NETHERRACK);
        fill(id1++, Blocks.SOUL_SAND);
        fill(id1++, Blocks.GLOWSTONE);
        fill(id1++, Blocks.NETHER_PORTAL);
        fill(id1++, Blocks.JACK_O_LANTERN);
        fill(id1++, Blocks.CAKE);
        fill(id1++, Blocks.REPEATER); // off
        fill(id1++, Blocks.REPEATER); // on

        fill(id1++, Blocks.WHITE_STAINED_GLASS);
        fill(id1++, Blocks.OAK_TRAPDOOR);
        id1++; //fill(0x62, Blocks.egg);
        fill(id1++, Blocks.STONE_BRICKS);
        fill(id1++, Blocks.BROWN_MUSHROOM_BLOCK);
        fill(id1++, Blocks.RED_MUSHROOM_BLOCK);
        fill(id1++, Blocks.IRON_BARS);
        fill(id1++, Blocks.GLASS_PANE);
        fill(id1++, Blocks.MELON);
        fill(id1++, Blocks.PUMPKIN_STEM);
        fill(id1++, Blocks.MELON_STEM);
        fill(id1++, Blocks.VINE);
        fill(id1++, Blocks.OAK_FENCE_GATE);
        fill(id1++, Blocks.BRICK_STAIRS);
        fill(id1++, Blocks.STONE_BRICK_STAIRS);
        fill(id1++, Blocks.MYCELIUM);

        fill(id1++, Blocks.LILY_PAD);
        fill(id1++, Blocks.NETHER_BRICKS);
        fill(id1++, Blocks.NETHER_BRICK_FENCE);
        fill(id1++, Blocks.NETHER_BRICK_STAIRS);
        fill(id1++, Blocks.NETHER_WART);
        fill(id1++, Blocks.ENCHANTING_TABLE);
        fill(id1++, Blocks.BREWING_STAND);
        fill(id1++, Blocks.CAULDRON);
        fill(id1++, Blocks.END_PORTAL);
        fill(id1++, Blocks.END_PORTAL_FRAME);
        fill(id1++, Blocks.END_STONE);
        fill(id1++, Blocks.DRAGON_EGG);
        fill(id1++, Blocks.REDSTONE_LAMP); // off
        fill(id1++, Blocks.REDSTONE_LAMP); // on
        fill(id1++, Blocks.OAK_SLAB); // double
        fill(id1++, Blocks.OAK_SLAB);

        fill(id1++, Blocks.COCOA);
        fill(id1++, Blocks.SANDSTONE_STAIRS);
        fill(id1++, Blocks.EMERALD_ORE);
        fill(id1++, Blocks.ENDER_CHEST);
        fill(id1++, Blocks.TRIPWIRE_HOOK);
        fill(id1++, Blocks.TRIPWIRE_HOOK);
        fill(id1++, Blocks.EMERALD_BLOCK);
        fill(id1++, Blocks.SPRUCE_STAIRS);
        fill(id1++, Blocks.BIRCH_STAIRS);
        fill(id1++, Blocks.JUNGLE_STAIRS);
        fill(id1++, Blocks.COMMAND_BLOCK);
        fill(id1++, Blocks.BEACON);
        fill(id1++, Blocks.COBBLESTONE_WALL);
        fill(id1++, Blocks.FLOWER_POT);
        fill(id1++, Blocks.CARROTS);
        fill(id1++, Blocks.POTATOES);

        fill(id1++, Blocks.OAK_BUTTON);
        fill(id1++, Blocks.SKELETON_SKULL);
        fill(id1++, Blocks.ANVIL);
        fill(id1++, Blocks.TRAPPED_CHEST);
        fill(id1++, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE);
        fill(id1++, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE);
        fill(id1++, Blocks.COMPARATOR); // off
        fill(id1++, Blocks.COMPARATOR); // on
        fill(id1++, Blocks.DAYLIGHT_DETECTOR);
        fill(id1++, Blocks.REDSTONE_BLOCK);
        fill(id1++, Blocks.NETHER_QUARTZ_ORE);
        fill(id1++, Blocks.HOPPER);
        fill(id1++, Blocks.QUARTZ_BLOCK);
        fill(id1++, Blocks.QUARTZ_STAIRS);
        fill(id1++, Blocks.ACTIVATOR_RAIL);
        fill(id1++, Blocks.DROPPER);

        id1++; //fill(0xA0, Blocks.CLAY);
        fill(id1++, Blocks.WHITE_STAINED_GLASS_PANE);
        id1++; //fill(0xA2, Blocks.LEAVES);
        id1++; //fill(0xA3, Blocks.log);
        fill(id1++, Blocks.ACACIA_STAIRS);
        fill(id1++, Blocks.DARK_OAK_STAIRS);
        fill(id1++, Blocks.SLIME_BLOCK);
        fill(id1++, Blocks.BARRIER);
        fill(id1++, Blocks.IRON_TRAPDOOR);
        fill(id1++, Blocks.PRISMARINE);
        fill(id1++, Blocks.SEA_LANTERN);
        fill(id1++, Blocks.HAY_BLOCK);
        fill(id1++, Blocks.WHITE_CARPET);
        id1++; //fill(0xAD, Blocks.clay);
        fill(id1++, Blocks.COAL_BLOCK);
        fill(id1++, Blocks.PACKED_ICE);

        id1++; //fill(0xB0, Blocks.double_plant);
        fill(id1++, Blocks.WHITE_BANNER);
        fill(id1++, Blocks.WHITE_WALL_BANNER);
        id1++; //fill(0xB3, Blocks.daylight_detector_inverted);
        fill(id1++, Blocks.RED_SANDSTONE);
        fill(id1++, Blocks.RED_SANDSTONE_STAIRS);
        fill(id1++, Blocks.RED_SANDSTONE_SLAB);
        fill(id1++, Blocks.STONE_SLAB);
        fill(id1++, Blocks.SPRUCE_FENCE_GATE);
        fill(id1++, Blocks.BIRCH_FENCE_GATE);
        fill(id1++, Blocks.JUNGLE_FENCE_GATE);
        fill(id1++, Blocks.DARK_OAK_FENCE_GATE);
        fill(id1++, Blocks.ACACIA_FENCE_GATE);
        fill(id1++, Blocks.SPRUCE_FENCE);
        fill(id1++, Blocks.BIRCH_FENCE);
        fill(id1++, Blocks.JUNGLE_FENCE);

        fill(id1++, Blocks.DARK_OAK_FENCE);
        fill(id1++, Blocks.ACACIA_FENCE);
        fill(id1++, Blocks.SPRUCE_DOOR);
        fill(id1++, Blocks.BIRCH_DOOR);
        fill(id1++, Blocks.JUNGLE_DOOR);
        fill(id1++, Blocks.ACACIA_DOOR);
        fill(id1++, Blocks.DARK_OAK_DOOR);
        fill(id1++, Blocks.END_ROD);
        fill(id1++, Blocks.CHORUS_PLANT);
        fill(id1++, Blocks.CHORUS_FLOWER);
        fill(id1++, Blocks.PURPUR_BLOCK);
        fill(id1++, Blocks.PURPUR_PILLAR);
        fill(id1++, Blocks.PURPUR_STAIRS);
        id1++; //fill(id1++, null, //Blocks.PURPUR_DOUBLE_SLAB,
        fill(id1++, Blocks.PURPUR_SLAB);
        fill(id1++, Blocks.END_STONE_BRICKS);
        fill(id1++, Blocks.BEETROOTS);
        fill(id1++, Blocks.DIRT_PATH);
        fill(id1++, Blocks.END_GATEWAY);
        fill(id1++, Blocks.REPEATING_COMMAND_BLOCK);
        fill(id1++, Blocks.CHAIN_COMMAND_BLOCK);
        fill(id1++, Blocks.FROSTED_ICE);
        fill(id1++, Blocks.MAGMA_BLOCK);
        fill(id1++, Blocks.NETHER_WART_BLOCK);
        id1++; //fill(id1++, null, //Blocks.RED_NETHER_BRICK,
        fill(id1++, Blocks.BONE_BLOCK);
        fill(id1++, Blocks.STRUCTURE_VOID);
        fill(id1++, Blocks.OBSERVER);
        fill(id1++, Blocks.WHITE_SHULKER_BOX);
        fill(id1++, Blocks.ORANGE_SHULKER_BOX);
        fill(id1++, Blocks.MAGENTA_SHULKER_BOX);
        fill(id1++, Blocks.LIGHT_BLUE_SHULKER_BOX);
        fill(id1++, Blocks.YELLOW_SHULKER_BOX);
        fill(id1++, Blocks.LIME_SHULKER_BOX);
        fill(id1++, Blocks.PINK_SHULKER_BOX);
        fill(id1++, Blocks.GRAY_SHULKER_BOX);
        id1++; //fill(id1++, null, //Blocks.SILVER_SHULKER_BOX,
        fill(id1++, Blocks.CYAN_SHULKER_BOX);
        fill(id1++, Blocks.PURPLE_SHULKER_BOX);
        fill(id1++, Blocks.BLUE_SHULKER_BOX);
        fill(id1++, Blocks.BROWN_SHULKER_BOX);
        fill(id1++, Blocks.GREEN_SHULKER_BOX);
        fill(id1++, Blocks.RED_SHULKER_BOX);
        fill(id1++, Blocks.BLACK_SHULKER_BOX);
        fill(id1++, Blocks.WHITE_GLAZED_TERRACOTTA);
        fill(id1++, Blocks.ORANGE_GLAZED_TERRACOTTA);
        fill(id1++, Blocks.MAGENTA_GLAZED_TERRACOTTA);
        fill(id1++, Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA);
        fill(id1++, Blocks.YELLOW_GLAZED_TERRACOTTA);
        fill(id1++, Blocks.LIME_GLAZED_TERRACOTTA);
        fill(id1++, Blocks.PINK_GLAZED_TERRACOTTA);
        fill(id1++, Blocks.GRAY_GLAZED_TERRACOTTA);
        fill(id1++, Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA);
        fill(id1++, Blocks.CYAN_GLAZED_TERRACOTTA);
        fill(id1++, Blocks.PURPLE_GLAZED_TERRACOTTA);
        fill(id1++, Blocks.BLUE_GLAZED_TERRACOTTA);
        fill(id1++, Blocks.BROWN_GLAZED_TERRACOTTA);
        fill(id1++, Blocks.GREEN_GLAZED_TERRACOTTA);
        fill(id1++, Blocks.RED_GLAZED_TERRACOTTA);
        fill(id1++, Blocks.BLACK_GLAZED_TERRACOTTA);
        //fill(id1++, null, //Blocks.CONCRETE,
        //fill(id1++, null, //Blocks.CONCRETE_POWDER,
    }

    private static void fill(int id1, Block block) {
        for (int id2 = 0; id2 < 256; id2++) {
            fill(id1, id2, block);
        }
    }

    private static void fill(int id1, int id2, Block block) {
        mapping[(id1 << 8) | id2] = block.defaultBlockState();
    }

    /*
    properties:
    	blockProperties: {

		"2:0" : {
			"snowy" : "false"
		},
		"3:0" : {
			"variant" : "dirt",
			"snowy" : "false"
		},
		"3:1" : {
			"variant" : "coarse_dirt",
			"snowy" : "false"
		},
		"3:2" : {
			"variant" : "podzol",
			"snowy" : "false"
		},
		"4:0" : {},
		"5:0" : {
			"variant" : "oak"
		},
		"5:1" : {
			"variant" : "spruce"
		},
		"5:2" : {
			"variant" : "birch"
		},
		"5:3" : {
			"variant" : "jungle"
		},
		"5:4" : {
			"variant" : "acacia"
		},
		"5:5" : {
			"variant" : "dark_oak"
		},
		"6:8" : {
			"type" : "oak",
			"stage" : "1"
		},
		"6:9" : {
			"type" : "spruce",
			"stage" : "1"
		},
		"6:10" : {
			"type" : "birch",
			"stage" : "1"
		},
		"6:11" : {
			"type" : "jungle",
			"stage" : "1"
		},
		"6:12" : {
			"type" : "acacia",
			"stage" : "1"
		},
		"6:13" : {
			"type" : "dark_oak",
			"stage" : "1"
		},
		"7:0" : {},
		"8:0" : {
			"level" : "0"
		},
		"8:1" : {
			"level" : "1"
		},
		"8:2" : {
			"level" : "2"
		},
		"8:3" : {
			"level" : "3"
		},
		"8:4" : {
			"level" : "4"
		},
		"8:5" : {
			"level" : "5"
		},
		"8:6" : {
			"level" : "6"
		},
		"8:7" : {
			"level" : "7"
		},
		"8:8" : {
			"level" : "8"
		},
		"8:9" : {
			"level" : "9"
		},
		"8:10" : {
			"level" : "10"
		},
		"8:11" : {
			"level" : "11"
		},
		"8:12" : {
			"level" : "12"
		},
		"8:13" : {
			"level" : "13"
		},
		"8:14" : {
			"level" : "14"
		},
		"8:15" : {
			"level" : "15"
		},
		"9:0" : {
			"level" : "0"
		},
		"9:1" : {
			"level" : "1"
		},
		"9:2" : {
			"level" : "2"
		},
		"9:3" : {
			"level" : "3"
		},
		"9:4" : {
			"level" : "4"
		},
		"9:5" : {
			"level" : "5"
		},
		"9:6" : {
			"level" : "6"
		},
		"9:7" : {
			"level" : "7"
		},
		"9:8" : {
			"level" : "8"
		},
		"9:9" : {
			"level" : "9"
		},
		"9:10" : {
			"level" : "10"
		},
		"9:11" : {
			"level" : "11"
		},
		"9:12" : {
			"level" : "12"
		},
		"9:13" : {
			"level" : "13"
		},
		"9:14" : {
			"level" : "14"
		},
		"9:15" : {
			"level" : "15"
		},
		"10:0" : {
			"level" : "0"
		},
		"10:1" : {
			"level" : "1"
		},
		"10:2" : {
			"level" : "2"
		},
		"10:3" : {
			"level" : "3"
		},
		"10:4" : {
			"level" : "4"
		},
		"10:5" : {
			"level" : "5"
		},
		"10:6" : {
			"level" : "6"
		},
		"10:7" : {
			"level" : "7"
		},
		"10:8" : {
			"level" : "8"
		},
		"10:9" : {
			"level" : "9"
		},
		"10:10" : {
			"level" : "10"
		},
		"10:11" : {
			"level" : "11"
		},
		"10:12" : {
			"level" : "12"
		},
		"10:13" : {
			"level" : "13"
		},
		"10:14" : {
			"level" : "14"
		},
		"10:15" : {
			"level" : "15"
		},
		"11:0" : {
			"level" : "0"
		},
		"11:1" : {
			"level" : "1"
		},
		"11:2" : {
			"level" : "2"
		},
		"11:3" : {
			"level" : "3"
		},
		"11:4" : {
			"level" : "4"
		},
		"11:5" : {
			"level" : "5"
		},
		"11:6" : {
			"level" : "6"
		},
		"11:7" : {
			"level" : "7"
		},
		"11:8" : {
			"level" : "8"
		},
		"11:9" : {
			"level" : "9"
		},
		"11:10" : {
			"level" : "10"
		},
		"11:11" : {
			"level" : "11"
		},
		"11:12" : {
			"level" : "12"
		},
		"11:13" : {
			"level" : "13"
		},
		"11:14" : {
			"level" : "14"
		},
		"11:15" : {
			"level" : "15"
		},
		"14:0" : {},
		"15:0" : {},
		"16:0" : {},
		"17:0" : {
			"variant" : "oak",
			"axis" : "y"
		},
		"17:1" : {
			"variant" : "spruce",
			"axis" : "y"
		},
		"17:2" : {
			"variant" : "birch",
			"axis" : "y"
		},
		"17:3" : {
			"variant" : "jungle",
			"axis" : "y"
		},
		"17:4" : {
			"variant" : "oak",
			"axis" : "x"
		},
		"17:5" : {
			"variant" : "spruce",
			"axis" : "x"
		},
		"17:6" : {
			"variant" : "birch",
			"axis" : "x"
		},
		"17:7" : {
			"variant" : "jungle",
			"axis" : "x"
		},
		"17:8" : {
			"variant" : "oak",
			"axis" : "z"
		},
		"17:9" : {
			"variant" : "spruce",
			"axis" : "z"
		},
		"17:10" : {
			"variant" : "birch",
			"axis" : "z"
		},
		"17:11" : {
			"variant" : "jungle",
			"axis" : "z"
		},
		"17:12" : {
			"variant" : "oak",
			"axis" : "none"
		},
		"17:13" : {
			"variant" : "spruce",
			"axis" : "none"
		},
		"17:14" : {
			"variant" : "birch",
			"axis" : "none"
		},
		"17:15" : {
			"variant" : "jungle",
			"axis" : "none"
		},
		"18:0" : {
			"variant" : "oak",
			"check_decay" : "false",
			"decayable" : "true"
		},
		"18:1" : {
			"variant" : "spruce",
			"check_decay" : "false",
			"decayable" : "true"
		},
		"18:2" : {
			"variant" : "birch",
			"check_decay" : "false",
			"decayable" : "true"
		},
		"18:3" : {
			"variant" : "jungle",
			"check_decay" : "false",
			"decayable" : "true"
		},
		"18:4" : {
			"variant" : "oak",
			"check_decay" : "false",
			"decayable" : "false"
		},
		"18:5" : {
			"variant" : "spruce",
			"check_decay" : "false",
			"decayable" : "false"
		},
		"18:6" : {
			"variant" : "birch",
			"check_decay" : "false",
			"decayable" : "false"
		},
		"18:7" : {
			"variant" : "jungle",
			"check_decay" : "false",
			"decayable" : "false"
		},
		"18:8" : {
			"variant" : "oak",
			"check_decay" : "true",
			"decayable" : "true"
		},
		"18:9" : {
			"variant" : "spruce",
			"check_decay" : "true",
			"decayable" : "true"
		},
		"18:10" : {
			"variant" : "birch",
			"check_decay" : "true",
			"decayable" : "true"
		},
		"18:11" : {
			"variant" : "jungle",
			"check_decay" : "true",
			"decayable" : "true"
		},
		"18:12" : {
			"variant" : "oak",
			"check_decay" : "true",
			"decayable" : "false"
		},
		"18:13" : {
			"variant" : "spruce",
			"check_decay" : "true",
			"decayable" : "false"
		},
		"18:14" : {
			"variant" : "birch",
			"check_decay" : "true",
			"decayable" : "false"
		},
		"18:15" : {
			"variant" : "jungle",
			"check_decay" : "true",
			"decayable" : "false"
		},
		"19:0" : {
			"wet" : "false"
		},
		"19:1" : {
			"wet" : "true"
		},
		"20:0" : {},
		"21:0" : {},
		"22:0" : {},
		"23:0" : {
			"facing" : "down",
			"triggered" : "false"
		},
		"23:1" : {
			"facing" : "up",
			"triggered" : "false"
		},
		"23:2" : {
			"facing" : "north",
			"triggered" : "false"
		},
		"23:3" : {
			"facing" : "south",
			"triggered" : "false"
		},
		"23:4" : {
			"facing" : "west",
			"triggered" : "false"
		},
		"23:5" : {
			"facing" : "east",
			"triggered" : "false"
		},
		"23:8" : {
			"facing" : "down",
			"triggered" : "true"
		},
		"23:9" : {
			"facing" : "up",
			"triggered" : "true"
		},
		"23:10" : {
			"facing" : "north",
			"triggered" : "true"
		},
		"23:11" : {
			"facing" : "south",
			"triggered" : "true"
		},
		"23:12" : {
			"facing" : "west",
			"triggered" : "true"
		},
		"23:13" : {
			"facing" : "east",
			"triggered" : "true"
		},
		"24:0" : {
			"type" : "sandstone"
		},
		"24:1" : {
			"type" : "chiseled_sandstone"
		},
		"24:2" : {
			"type" : "smooth_sandstone"
		},
		"25:0" : {},
		"26:0" : {
			"facing" : "south",
			"part" : "foot",
			"occupied" : "false"
		},
		"26:1" : {
			"facing" : "west",
			"part" : "foot",
			"occupied" : "false"
		},
		"26:2" : {
			"facing" : "north",
			"part" : "foot",
			"occupied" : "false"
		},
		"26:3" : {
			"facing" : "east",
			"part" : "foot",
			"occupied" : "false"
		},
		"26:8" : {
			"facing" : "south",
			"part" : "head",
			"occupied" : "false"
		},
		"26:9" : {
			"facing" : "west",
			"part" : "head",
			"occupied" : "false"
		},
		"26:10" : {
			"facing" : "north",
			"part" : "head",
			"occupied" : "false"
		},
		"26:11" : {
			"facing" : "east",
			"part" : "head",
			"occupied" : "false"
		},
		"26:12" : {
			"facing" : "south",
			"part" : "head",
			"occupied" : "true"
		},
		"26:13" : {
			"facing" : "west",
			"part" : "head",
			"occupied" : "true"
		},
		"26:14" : {
			"facing" : "north",
			"part" : "head",
			"occupied" : "true"
		},
		"26:15" : {
			"facing" : "east",
			"part" : "head",
			"occupied" : "true"
		},
		"27:0" : {
			"shape" : "north_south",
			"powered" : "false"
		},
		"27:1" : {
			"shape" : "east_west",
			"powered" : "false"
		},
		"27:2" : {
			"shape" : "ascending_east",
			"powered" : "false"
		},
		"27:3" : {
			"shape" : "ascending_west",
			"powered" : "false"
		},
		"27:4" : {
			"shape" : "ascending_north",
			"powered" : "false"
		},
		"27:5" : {
			"shape" : "ascending_south",
			"powered" : "false"
		},
		"27:8" : {
			"shape" : "north_south",
			"powered" : "true"
		},
		"27:9" : {
			"shape" : "east_west",
			"powered" : "true"
		},
		"27:10" : {
			"shape" : "ascending_east",
			"powered" : "true"
		},
		"27:11" : {
			"shape" : "ascending_west",
			"powered" : "true"
		},
		"27:12" : {
			"shape" : "ascending_north",
			"powered" : "true"
		},
		"27:13" : {
			"shape" : "ascending_south",
			"powered" : "true"
		},
		"28:0" : {
			"shape" : "north_south",
			"powered" : "false"
		},
		"28:1" : {
			"shape" : "east_west",
			"powered" : "false"
		},
		"28:2" : {
			"shape" : "ascending_east",
			"powered" : "false"
		},
		"28:3" : {
			"shape" : "ascending_west",
			"powered" : "false"
		},
		"28:4" : {
			"shape" : "ascending_north",
			"powered" : "false"
		},
		"28:5" : {
			"shape" : "ascending_south",
			"powered" : "false"
		},
		"28:8" : {
			"shape" : "north_south",
			"powered" : "true"
		},
		"28:9" : {
			"shape" : "east_west",
			"powered" : "true"
		},
		"28:10" : {
			"shape" : "ascending_east",
			"powered" : "true"
		},
		"28:11" : {
			"shape" : "ascending_west",
			"powered" : "true"
		},
		"28:12" : {
			"shape" : "ascending_north",
			"powered" : "true"
		},
		"28:13" : {
			"shape" : "ascending_south",
			"powered" : "true"
		},
		"29:0" : {
			"facing" : "down",
			"extended" : "false"
		},
		"29:1" : {
			"facing" : "up",
			"extended" : "false"
		},
		"29:2" : {
			"facing" : "north",
			"extended" : "false"
		},
		"29:3" : {
			"facing" : "south",
			"extended" : "false"
		},
		"29:4" : {
			"facing" : "west",
			"extended" : "false"
		},
		"29:5" : {
			"facing" : "east",
			"extended" : "false"
		},
		"29:8" : {
			"facing" : "down",
			"extended" : "true"
		},
		"29:9" : {
			"facing" : "up",
			"extended" : "true"
		},
		"29:10" : {
			"facing" : "north",
			"extended" : "true"
		},
		"29:11" : {
			"facing" : "south",
			"extended" : "true"
		},
		"29:12" : {
			"facing" : "west",
			"extended" : "true"
		},
		"29:13" : {
			"facing" : "east",
			"extended" : "true"
		},
		"30:0" : {},
		"31:0" : {
			"type" : "dead_bush"
		},
		"31:1" : {
			"type" : "tall_grass"
		},
		"31:2" : {
			"type" : "fern"
		},
		"32:0" : {},
		"33:0" : {
			"facing" : "down",
			"extended" : "false"
		},
		"33:1" : {
			"facing" : "up",
			"extended" : "false"
		},
		"33:2" : {
			"facing" : "north",
			"extended" : "false"
		},
		"33:3" : {
			"facing" : "south",
			"extended" : "false"
		},
		"33:4" : {
			"facing" : "west",
			"extended" : "false"
		},
		"33:5" : {
			"facing" : "east",
			"extended" : "false"
		},
		"33:8" : {
			"facing" : "down",
			"extended" : "true"
		},
		"33:9" : {
			"facing" : "up",
			"extended" : "true"
		},
		"33:10" : {
			"facing" : "north",
			"extended" : "true"
		},
		"33:11" : {
			"facing" : "south",
			"extended" : "true"
		},
		"33:12" : {
			"facing" : "west",
			"extended" : "true"
		},
		"33:13" : {
			"facing" : "east",
			"extended" : "true"
		},
		"34:0" : {
			"facing" : "down",
			"short" : "false",
			"type" : "normal"
		},
		"34:1" : {
			"facing" : "up",
			"short" : "false",
			"type" : "normal"
		},
		"34:2" : {
			"facing" : "north",
			"short" : "false",
			"type" : "normal"
		},
		"34:3" : {
			"facing" : "south",
			"short" : "false",
			"type" : "normal"
		},
		"34:4" : {
			"facing" : "west",
			"short" : "false",
			"type" : "normal"
		},
		"34:5" : {
			"facing" : "east",
			"short" : "false",
			"type" : "normal"
		},
		"34:8" : {
			"facing" : "down",
			"short" : "false",
			"type" : "sticky"
		},
		"34:9" : {
			"facing" : "up",
			"short" : "false",
			"type" : "sticky"
		},
		"34:10" : {
			"facing" : "north",
			"short" : "false",
			"type" : "sticky"
		},
		"34:11" : {
			"facing" : "south",
			"short" : "false",
			"type" : "sticky"
		},
		"34:12" : {
			"facing" : "west",
			"short" : "false",
			"type" : "sticky"
		},
		"34:13" : {
			"facing" : "east",
			"short" : "false",
			"type" : "sticky"
		},
		"35:0" : {
			"color" : "white"
		},
		"35:1" : {
			"color" : "orange"
		},
		"35:2" : {
			"color" : "magenta"
		},
		"35:3" : {
			"color" : "light_blue"
		},
		"35:4" : {
			"color" : "yellow"
		},
		"35:5" : {
			"color" : "lime"
		},
		"35:6" : {
			"color" : "pink"
		},
		"35:7" : {
			"color" : "gray"
		},
		"35:8" : {
			"color" : "silver"
		},
		"35:9" : {
			"color" : "cyan"
		},
		"35:10" : {
			"color" : "purple"
		},
		"35:11" : {
			"color" : "blue"
		},
		"35:12" : {
			"color" : "brown"
		},
		"35:13" : {
			"color" : "green"
		},
		"35:14" : {
			"color" : "red"
		},
		"35:15" : {
			"color" : "black"
		},
		"36:0" : {
			"facing" : "down",
			"type" : "normal"
		},
		"36:1" : {
			"facing" : "up",
			"type" : "normal"
		},
		"36:2" : {
			"facing" : "north",
			"type" : "normal"
		},
		"36:3" : {
			"facing" : "south",
			"type" : "normal"
		},
		"36:4" : {
			"facing" : "west",
			"type" : "normal"
		},
		"36:5" : {
			"facing" : "east",
			"type" : "normal"
		},
		"36:8" : {
			"facing" : "down",
			"type" : "sticky"
		},
		"36:9" : {
			"facing" : "up",
			"type" : "sticky"
		},
		"36:10" : {
			"facing" : "north",
			"type" : "sticky"
		},
		"36:11" : {
			"facing" : "south",
			"type" : "sticky"
		},
		"36:12" : {
			"facing" : "west",
			"type" : "sticky"
		},
		"36:13" : {
			"facing" : "east",
			"type" : "sticky"
		},
		"37:0" : {
			"type" : "dandelion"
		},
		"38:0" : {
			"type" : "poppy"
		},
		"38:1" : {
			"type" : "blue_orchid"
		},
		"38:2" : {
			"type" : "allium"
		},
		"38:3" : {
			"type" : "houstonia"
		},
		"38:4" : {
			"type" : "red_tulip"
		},
		"38:5" : {
			"type" : "orange_tulip"
		},
		"38:6" : {
			"type" : "white_tulip"
		},
		"38:7" : {
			"type" : "pink_tulip"
		},
		"38:8" : {
			"type" : "oxeye_daisy"
		},
		"39:0" : {},
		"40:0" : {},
		"41:0" : {},
		"42:0" : {},
		"43:0" : {
			"variant" : "stone",
			"seamless" : "false"
		},
		"43:1" : {
			"variant" : "sandstone",
			"seamless" : "false"
		},
		"43:2" : {
			"variant" : "wood_old",
			"seamless" : "false"
		},
		"43:3" : {
			"variant" : "cobblestone",
			"seamless" : "false"
		},
		"43:4" : {
			"variant" : "brick",
			"seamless" : "false"
		},
		"43:5" : {
			"variant" : "stone_brick",
			"seamless" : "false"
		},
		"43:6" : {
			"variant" : "nether_brick",
			"seamless" : "false"
		},
		"43:7" : {
			"variant" : "quartz",
			"seamless" : "false"
		},
		"43:8" : {
			"variant" : "stone",
			"seamless" : "true"
		},
		"43:9" : {
			"variant" : "sandstone",
			"seamless" : "true"
		},
		"43:10" : {
			"variant" : "wood_old",
			"seamless" : "true"
		},
		"43:11" : {
			"variant" : "cobblestone",
			"seamless" : "true"
		},
		"43:12" : {
			"variant" : "brick",
			"seamless" : "true"
		},
		"43:13" : {
			"variant" : "stone_brick",
			"seamless" : "true"
		},
		"43:14" : {
			"variant" : "nether_brick",
			"seamless" : "true"
		},
		"43:15" : {
			"variant" : "quartz",
			"seamless" : "true"
		},
		"44:0" : {
			"variant" : "stone",
			"half" : "bottom"
		},
		"44:1" : {
			"variant" : "sandstone",
			"half" : "bottom"
		},
		"44:2" : {
			"variant" : "wood_old",
			"half" : "bottom"
		},
		"44:3" : {
			"variant" : "cobblestone",
			"half" : "bottom"
		},
		"44:4" : {
			"variant" : "brick",
			"half" : "bottom"
		},
		"44:5" : {
			"variant" : "stone_brick",
			"half" : "bottom"
		},
		"44:6" : {
			"variant" : "nether_brick",
			"half" : "bottom"
		},
		"44:7" : {
			"variant" : "quartz",
			"half" : "bottom"
		},
		"44:8" : {
			"variant" : "stone",
			"half" : "top"
		},
		"44:9" : {
			"variant" : "sandstone",
			"half" : "top"
		},
		"44:10" : {
			"variant" : "wood_old",
			"half" : "top"
		},
		"44:11" : {
			"variant" : "cobblestone",
			"half" : "top"
		},
		"44:12" : {
			"variant" : "brick",
			"half" : "top"
		},
		"44:13" : {
			"variant" : "stone_brick",
			"half" : "top"
		},
		"44:14" : {
			"variant" : "nether_brick",
			"half" : "top"
		},
		"44:15" : {
			"variant" : "quartz",
			"half" : "top"
		},
		"45:0" : {},
		"46:0" : {
			"explode" : "false"
		},
		"46:1" : {
			"explode" : "true"
		},
		"47:0" : {},
		"48:0" : {},
		"49:0" : {},
		"50:1" : {
			"facing" : "east"
		},
		"50:2" : {
			"facing" : "west"
		},
		"50:3" : {
			"facing" : "south"
		},
		"50:4" : {
			"facing" : "north"
		},
		"50:5" : {
			"facing" : "up"
		},
		"51:0" : {
			"north" : "false",
			"west" : "false",
			"age" : "0",
			"up" : "false",
			"east" : "false",
			"south" : "false"
		},
		"51:1" : {
			"north" : "false",
			"west" : "false",
			"age" : "1",
			"up" : "false",
			"east" : "false",
			"south" : "false"
		},
		"51:2" : {
			"north" : "false",
			"west" : "false",
			"age" : "2",
			"up" : "false",
			"east" : "false",
			"south" : "false"
		},
		"51:3" : {
			"north" : "false",
			"west" : "false",
			"age" : "3",
			"up" : "false",
			"east" : "false",
			"south" : "false"
		},
		"51:4" : {
			"north" : "false",
			"west" : "false",
			"age" : "4",
			"up" : "false",
			"east" : "false",
			"south" : "false"
		},
		"51:5" : {
			"north" : "false",
			"west" : "false",
			"age" : "5",
			"up" : "false",
			"east" : "false",
			"south" : "false"
		},
		"51:6" : {
			"north" : "false",
			"west" : "false",
			"age" : "6",
			"up" : "false",
			"east" : "false",
			"south" : "false"
		},
		"51:7" : {
			"north" : "false",
			"west" : "false",
			"age" : "7",
			"up" : "false",
			"east" : "false",
			"south" : "false"
		},
		"51:8" : {
			"north" : "false",
			"west" : "false",
			"age" : "8",
			"up" : "false",
			"east" : "false",
			"south" : "false"
		},
		"51:9" : {
			"north" : "false",
			"west" : "false",
			"age" : "9",
			"up" : "false",
			"east" : "false",
			"south" : "false"
		},
		"51:10" : {
			"north" : "false",
			"west" : "false",
			"age" : "10",
			"up" : "false",
			"east" : "false",
			"south" : "false"
		},
		"51:11" : {
			"north" : "false",
			"west" : "false",
			"age" : "11",
			"up" : "false",
			"east" : "false",
			"south" : "false"
		},
		"51:12" : {
			"north" : "false",
			"west" : "false",
			"age" : "12",
			"up" : "false",
			"east" : "false",
			"south" : "false"
		},
		"51:13" : {
			"north" : "false",
			"west" : "false",
			"age" : "13",
			"up" : "false",
			"east" : "false",
			"south" : "false"
		},
		"51:14" : {
			"north" : "false",
			"west" : "false",
			"age" : "14",
			"up" : "false",
			"east" : "false",
			"south" : "false"
		},
		"51:15" : {
			"north" : "false",
			"west" : "false",
			"age" : "15",
			"up" : "false",
			"east" : "false",
			"south" : "false"
		},
		"52:0" : {},
		"53:0" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "bottom"
		},
		"53:1" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "bottom"
		},
		"53:2" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "bottom"
		},
		"53:3" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "bottom"
		},
		"53:4" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "top"
		},
		"53:5" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "top"
		},
		"53:6" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "top"
		},
		"53:7" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "top"
		},
		"54:2" : {
			"facing" : "north"
		},
		"54:3" : {
			"facing" : "south"
		},
		"54:4" : {
			"facing" : "west"
		},
		"54:5" : {
			"facing" : "east"
		},
		"55:0" : {
			"west" : "none",
			"east" : "none",
			"north" : "none",
			"south" : "none",
			"power" : "0"
		},
		"55:1" : {
			"west" : "none",
			"east" : "none",
			"north" : "none",
			"south" : "none",
			"power" : "1"
		},
		"55:2" : {
			"west" : "none",
			"east" : "none",
			"north" : "none",
			"south" : "none",
			"power" : "2"
		},
		"55:3" : {
			"west" : "none",
			"east" : "none",
			"north" : "none",
			"south" : "none",
			"power" : "3"
		},
		"55:4" : {
			"west" : "none",
			"east" : "none",
			"north" : "none",
			"south" : "none",
			"power" : "4"
		},
		"55:5" : {
			"west" : "none",
			"east" : "none",
			"north" : "none",
			"south" : "none",
			"power" : "5"
		},
		"55:6" : {
			"west" : "none",
			"east" : "none",
			"north" : "none",
			"south" : "none",
			"power" : "6"
		},
		"55:7" : {
			"west" : "none",
			"east" : "none",
			"north" : "none",
			"south" : "none",
			"power" : "7"
		},
		"55:8" : {
			"west" : "none",
			"east" : "none",
			"north" : "none",
			"south" : "none",
			"power" : "8"
		},
		"55:9" : {
			"west" : "none",
			"east" : "none",
			"north" : "none",
			"south" : "none",
			"power" : "9"
		},
		"55:10" : {
			"west" : "none",
			"east" : "none",
			"north" : "none",
			"south" : "none",
			"power" : "10"
		},
		"55:11" : {
			"west" : "none",
			"east" : "none",
			"north" : "none",
			"south" : "none",
			"power" : "11"
		},
		"55:12" : {
			"west" : "none",
			"east" : "none",
			"north" : "none",
			"south" : "none",
			"power" : "12"
		},
		"55:13" : {
			"west" : "none",
			"east" : "none",
			"north" : "none",
			"south" : "none",
			"power" : "13"
		},
		"55:14" : {
			"west" : "none",
			"east" : "none",
			"north" : "none",
			"south" : "none",
			"power" : "14"
		},
		"55:15" : {
			"west" : "none",
			"east" : "none",
			"north" : "none",
			"south" : "none",
			"power" : "15"
		},
		"56:0" : {},
		"57:0" : {},
		"58:0" : {},
		"59:0" : {
			"age" : "0"
		},
		"59:1" : {
			"age" : "1"
		},
		"59:2" : {
			"age" : "2"
		},
		"59:3" : {
			"age" : "3"
		},
		"59:4" : {
			"age" : "4"
		},
		"59:5" : {
			"age" : "5"
		},
		"59:6" : {
			"age" : "6"
		},
		"59:7" : {
			"age" : "7"
		},
		"60:0" : {
			"moisture" : "0"
		},
		"60:1" : {
			"moisture" : "1"
		},
		"60:2" : {
			"moisture" : "2"
		},
		"60:3" : {
			"moisture" : "3"
		},
		"60:4" : {
			"moisture" : "4"
		},
		"60:5" : {
			"moisture" : "5"
		},
		"60:6" : {
			"moisture" : "6"
		},
		"60:7" : {
			"moisture" : "7"
		},
		"61:2" : {
			"facing" : "north"
		},
		"61:3" : {
			"facing" : "south"
		},
		"61:4" : {
			"facing" : "west"
		},
		"61:5" : {
			"facing" : "east"
		},
		"62:2" : {
			"facing" : "north"
		},
		"62:3" : {
			"facing" : "south"
		},
		"62:4" : {
			"facing" : "west"
		},
		"62:5" : {
			"facing" : "east"
		},
		"63:0" : {
			"rotation" : "0"
		},
		"63:1" : {
			"rotation" : "1"
		},
		"63:2" : {
			"rotation" : "2"
		},
		"63:3" : {
			"rotation" : "3"
		},
		"63:4" : {
			"rotation" : "4"
		},
		"63:5" : {
			"rotation" : "5"
		},
		"63:6" : {
			"rotation" : "6"
		},
		"63:7" : {
			"rotation" : "7"
		},
		"63:8" : {
			"rotation" : "8"
		},
		"63:9" : {
			"rotation" : "9"
		},
		"63:10" : {
			"rotation" : "10"
		},
		"63:11" : {
			"rotation" : "11"
		},
		"63:12" : {
			"rotation" : "12"
		},
		"63:13" : {
			"rotation" : "13"
		},
		"63:14" : {
			"rotation" : "14"
		},
		"63:15" : {
			"rotation" : "15"
		},
		"64:0" : {
			"facing" : "east",
			"hinge" : "left",
			"powered" : "false",
			"open" : "false",
			"half" : "lower"
		},
		"64:1" : {
			"facing" : "south",
			"hinge" : "left",
			"powered" : "false",
			"open" : "false",
			"half" : "lower"
		},
		"64:2" : {
			"facing" : "west",
			"hinge" : "left",
			"powered" : "false",
			"open" : "false",
			"half" : "lower"
		},
		"64:3" : {
			"facing" : "north",
			"hinge" : "left",
			"powered" : "false",
			"open" : "false",
			"half" : "lower"
		},
		"64:4" : {
			"facing" : "east",
			"hinge" : "left",
			"powered" : "false",
			"open" : "true",
			"half" : "lower"
		},
		"64:5" : {
			"facing" : "south",
			"hinge" : "left",
			"powered" : "false",
			"open" : "true",
			"half" : "lower"
		},
		"64:6" : {
			"facing" : "west",
			"hinge" : "left",
			"powered" : "false",
			"open" : "true",
			"half" : "lower"
		},
		"64:7" : {
			"facing" : "north",
			"hinge" : "left",
			"powered" : "false",
			"open" : "true",
			"half" : "lower"
		},
		"64:8" : {
			"facing" : "north",
			"hinge" : "left",
			"powered" : "false",
			"open" : "false",
			"half" : "upper"
		},
		"64:9" : {
			"facing" : "north",
			"hinge" : "right",
			"powered" : "false",
			"open" : "false",
			"half" : "upper"
		},
		"64:10" : {
			"facing" : "north",
			"hinge" : "left",
			"powered" : "true",
			"open" : "false",
			"half" : "upper"
		},
		"64:11" : {
			"facing" : "north",
			"hinge" : "right",
			"powered" : "true",
			"open" : "false",
			"half" : "upper"
		},
		"65:2" : {
			"facing" : "north"
		},
		"65:3" : {
			"facing" : "south"
		},
		"65:4" : {
			"facing" : "west"
		},
		"65:5" : {
			"facing" : "east"
		},
		"67:0" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "bottom"
		},
		"67:1" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "bottom"
		},
		"67:2" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "bottom"
		},
		"67:3" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "bottom"
		},
		"67:4" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "top"
		},
		"67:5" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "top"
		},
		"67:6" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "top"
		},
		"67:7" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "top"
		},
		"68:2" : {
			"facing" : "north"
		},
		"68:3" : {
			"facing" : "south"
		},
		"69:0" : {
			"facing" : "down_x",
			"powered" : "false"
		},
		"69:1" : {
			"facing" : "east",
			"powered" : "false"
		},
		"69:2" : {
			"facing" : "west",
			"powered" : "false"
		},
		"69:3" : {
			"facing" : "south",
			"powered" : "false"
		},
		"69:4" : {
			"facing" : "north",
			"powered" : "false"
		},
		"69:5" : {
			"facing" : "up_z",
			"powered" : "false"
		},
		"69:6" : {
			"facing" : "up_x",
			"powered" : "false"
		},
		"69:7" : {
			"facing" : "down_z",
			"powered" : "false"
		},
		"69:8" : {
			"facing" : "down_x",
			"powered" : "true"
		},
		"69:9" : {
			"facing" : "east",
			"powered" : "true"
		},
		"69:10" : {
			"facing" : "west",
			"powered" : "true"
		},
		"69:11" : {
			"facing" : "south",
			"powered" : "true"
		},
		"69:12" : {
			"facing" : "north",
			"powered" : "true"
		},
		"69:13" : {
			"facing" : "up_z",
			"powered" : "true"
		},
		"69:14" : {
			"facing" : "up_x",
			"powered" : "true"
		},
		"69:15" : {
			"facing" : "down_z",
			"powered" : "true"
		},
		"71:0" : {
			"facing" : "east",
			"hinge" : "left",
			"powered" : "false",
			"open" : "false",
			"half" : "lower"
		},
		"71:1" : {
			"facing" : "south",
			"hinge" : "left",
			"powered" : "false",
			"open" : "false",
			"half" : "lower"
		},
		"71:2" : {
			"facing" : "west",
			"hinge" : "left",
			"powered" : "false",
			"open" : "false",
			"half" : "lower"
		},
		"71:3" : {
			"facing" : "north",
			"hinge" : "left",
			"powered" : "false",
			"open" : "false",
			"half" : "lower"
		},
		"71:4" : {
			"facing" : "east",
			"hinge" : "left",
			"powered" : "false",
			"open" : "true",
			"half" : "lower"
		},
		"71:5" : {
			"facing" : "south",
			"hinge" : "left",
			"powered" : "false",
			"open" : "true",
			"half" : "lower"
		},
		"71:6" : {
			"facing" : "west",
			"hinge" : "left",
			"powered" : "false",
			"open" : "true",
			"half" : "lower"
		},
		"71:7" : {
			"facing" : "north",
			"hinge" : "left",
			"powered" : "false",
			"open" : "true",
			"half" : "lower"
		},
		"71:8" : {
			"facing" : "north",
			"hinge" : "left",
			"powered" : "false",
			"open" : "false",
			"half" : "upper"
		},
		"71:9" : {
			"facing" : "north",
			"hinge" : "right",
			"powered" : "false",
			"open" : "false",
			"half" : "upper"
		},
		"71:10" : {
			"facing" : "north",
			"hinge" : "left",
			"powered" : "true",
			"open" : "false",
			"half" : "upper"
		},
		"71:11" : {
			"facing" : "north",
			"hinge" : "right",
			"powered" : "true",
			"open" : "false",
			"half" : "upper"
		},
		"72:0" : {
			"powered" : "false",
		},
		"72:1" : {
			"powered" : "true",
		},
		"73:0" : {},
		"74:0" : {},
		"79:0" : {},
		"80:0" : {},
		"82:0" : {},
		"84:0" : {
			"has_record" : "false"
		},
		"84:1" : {
			"has_record" : "true"
		},
		"85:0" : {
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"south" : "false"
		},
		"86:0" : {
			"facing" : "south"
		},
		"86:1" : {
			"facing" : "west"
		},
		"86:2" : {
			"facing" : "north"
		},
		"86:3" : {
			"facing" : "east"
		},
		"87:0" : {},
		"88:0" : {},
		"89:0" : {},
		"90:1" : {
			"axis" : "x"
		},
		"90:2" : {
			"axis" : "z"
		},
		"91:0" : {
			"facing" : "south"
		},
		"91:1" : {
			"facing" : "west"
		},
		"91:2" : {
			"facing" : "north"
		},
		"91:3" : {
			"facing" : "east"
		},
		"92:0" : {
			"bites" : "0"
		},
		"92:1" : {
			"bites" : "1"
		},
		"92:2" : {
			"bites" : "2"
		},
		"92:3" : {
			"bites" : "3"
		},
		"92:4" : {
			"bites" : "4"
		},
		"92:5" : {
			"bites" : "5"
		},
		"92:6" : {
			"bites" : "6"
		},
		"93:0" : {
			"delay" : "1",
			"facing" : "south",
			"locked" : "false"
		},
		"93:1" : {
			"delay" : "1",
			"facing" : "west",
			"locked" : "false"
		},
		"93:2" : {
			"delay" : "1",
			"facing" : "north",
			"locked" : "false"
		},
		"93:3" : {
			"delay" : "1",
			"facing" : "east",
			"locked" : "false"
		},
		"93:4" : {
			"delay" : "2",
			"facing" : "south",
			"locked" : "false"
		},
		"93:5" : {
			"delay" : "2",
			"facing" : "west",
			"locked" : "false"
		},
		"93:6" : {
			"delay" : "2",
			"facing" : "north",
			"locked" : "false"
		},
		"93:7" : {
			"delay" : "2",
			"facing" : "east",
			"locked" : "false"
		},
		"93:8" : {
			"delay" : "3",
			"facing" : "south",
			"locked" : "false"
		},
		"93:9" : {
			"delay" : "3",
			"facing" : "west",
			"locked" : "false"
		},
		"93:10" : {
			"delay" : "3",
			"facing" : "north",
			"locked" : "false"
		},
		"93:11" : {
			"delay" : "3",
			"facing" : "east",
			"locked" : "false"
		},
		"93:12" : {
			"delay" : "4",
			"facing" : "south",
			"locked" : "false"
		},
		"93:13" : {
			"delay" : "4",
			"facing" : "west",
			"locked" : "false"
		},
		"93:14" : {
			"delay" : "4",
			"facing" : "north",
			"locked" : "false"
		},
		"93:15" : {
			"delay" : "4",
			"facing" : "east",
			"locked" : "false"
		},
		"94:0" : {
			"delay" : "1",
			"facing" : "south",
			"locked" : "false"
		},
		"94:1" : {
			"delay" : "1",
			"facing" : "west",
			"locked" : "false"
		},
		"94:2" : {
			"delay" : "1",
			"facing" : "north",
			"locked" : "false"
		},
		"94:3" : {
			"delay" : "1",
			"facing" : "east",
			"locked" : "false"
		},
		"94:4" : {
			"delay" : "2",
			"facing" : "south",
			"locked" : "false"
		},
		"94:5" : {
			"delay" : "2",
			"facing" : "west",
			"locked" : "false"
		},
		"94:6" : {
			"delay" : "2",
			"facing" : "north",
			"locked" : "false"
		},
		"94:7" : {
			"delay" : "2",
			"facing" : "east",
			"locked" : "false"
		},
		"94:8" : {
			"delay" : "3",
			"facing" : "south",
			"locked" : "false"
		},
		"94:9" : {
			"delay" : "3",
			"facing" : "west",
			"locked" : "false"
		},
		"94:10" : {
			"delay" : "3",
			"facing" : "north",
			"locked" : "false"
		},
		"94:11" : {
			"delay" : "3",
			"facing" : "east",
			"locked" : "false"
		},
		"94:12" : {
			"delay" : "4",
			"facing" : "south",
			"locked" : "false"
		},
		"94:13" : {
			"delay" : "4",
			"facing" : "west",
			"locked" : "false"
		},
		"94:14" : {
			"delay" : "4",
			"facing" : "north",
			"locked" : "false"
		},
		"94:15" : {
			"delay" : "4",
			"facing" : "east",
			"locked" : "false"
		},
		"95:0" : {
			"color" : "white"
		},
		"95:1" : {
			"color" : "orange"
		},
		"95:2" : {
			"color" : "magenta"
		},
		"95:3" : {
			"color" : "light_blue"
		},
		"95:4" : {
			"color" : "yellow"
		},
		"95:5" : {
			"color" : "lime"
		},
		"95:6" : {
			"color" : "pink"
		},
		"95:7" : {
			"color" : "gray"
		},
		"95:8" : {
			"color" : "silver"
		},
		"95:9" : {
			"color" : "cyan"
		},
		"95:10" : {
			"color" : "purple"
		},
		"95:11" : {
			"color" : "blue"
		},
		"95:12" : {
			"color" : "brown"
		},
		"95:13" : {
			"color" : "green"
		},
		"95:14" : {
			"color" : "red"
		},
		"95:15" : {
			"color" : "black"
		},
		"96:0" : {
			"facing" : "north",
			"open" : "false",
			"half" : "bottom"
		},
		"96:1" : {
			"facing" : "south",
			"open" : "false",
			"half" : "bottom"
		},
		"96:2" : {
			"facing" : "west",
			"open" : "false",
			"half" : "bottom"
		},
		"96:3" : {
			"facing" : "east",
			"open" : "false",
			"half" : "bottom"
		},
		"96:4" : {
			"facing" : "north",
			"open" : "true",
			"half" : "bottom"
		},
		"96:5" : {
			"facing" : "south",
			"open" : "true",
			"half" : "bottom"
		},
		"96:6" : {
			"facing" : "west",
			"open" : "true",
			"half" : "bottom"
		},
		"96:7" : {
			"facing" : "east",
			"open" : "true",
			"half" : "bottom"
		},
		"96:8" : {
			"facing" : "north",
			"open" : "false",
			"half" : "top"
		},
		"96:9" : {
			"facing" : "south",
			"open" : "false",
			"half" : "top"
		},
		"96:10" : {
			"facing" : "west",
			"open" : "false",
			"half" : "top"
		},
		"96:11" : {
			"facing" : "east",
			"open" : "false",
			"half" : "top"
		},
		"96:12" : {
			"facing" : "north",
			"open" : "true",
			"half" : "top"
		},
		"96:13" : {
			"facing" : "south",
			"open" : "true",
			"half" : "top"
		},
		"96:14" : {
			"facing" : "west",
			"open" : "true",
			"half" : "top"
		},
		"96:15" : {
			"facing" : "east",
			"open" : "true",
			"half" : "top"
		},
		"97:0" : {
			"variant" : "stone"
		},
		"97:1" : {
			"variant" : "cobblestone"
		},
		"97:2" : {
			"variant" : "stone_brick"
		},
		"97:3" : {
			"variant" : "mossy_brick"
		},
		"97:4" : {
			"variant" : "cracked_brick"
		},
		"97:5" : {
			"variant" : "chiseled_brick"
		},
		"98:0" : {
			"variant" : "stonebrick"
		},
		"98:1" : {
			"variant" : "mossy_stonebrick"
		},
		"98:2" : {
			"variant" : "cracked_stonebrick"
		},
		"98:3" : {
			"variant" : "chiseled_stonebrick"
		},
		"99:0" : {
			"variant" : "all_inside"
		},
		"99:1" : {
			"variant" : "north_west"
		},
		"99:2" : {
			"variant" : "north"
		},
		"99:3" : {
			"variant" : "north_east"
		},
		"99:4" : {
			"variant" : "west"
		},
		"99:5" : {
			"variant" : "center"
		},
		"99:6" : {
			"variant" : "east"
		},
		"99:7" : {
			"variant" : "south_west"
		},
		"99:8" : {
			"variant" : "south"
		},
		"99:9" : {
			"variant" : "south_east"
		},
		"99:10" : {
			"variant" : "stem"
		},
		"99:14" : {
			"variant" : "all_outside"
		},
		"99:15" : {
			"variant" : "all_stem"
		},
		"100:0" : {
			"variant" : "all_inside"
		},
		"100:1" : {
			"variant" : "north_west"
		},
		"100:2" : {
			"variant" : "north"
		},
		"100:3" : {
			"variant" : "north_east"
		},
		"100:4" : {
			"variant" : "west"
		},
		"100:5" : {
			"variant" : "center"
		},
		"100:6" : {
			"variant" : "east"
		},
		"100:7" : {
			"variant" : "south_west"
		},
		"100:8" : {
			"variant" : "south"
		},
		"100:9" : {
			"variant" : "south_east"
		},
		"100:10" : {
			"variant" : "stem"
		},
		"100:14" : {
			"variant" : "all_outside"
		},
		"100:15" : {
			"variant" : "all_stem"
		},
		"101:0" : {
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"south" : "false"
		},
		"102:0" : {
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"south" : "false"
		},
		"103:0" : {},
		"104:0" : {
			"facing" : "up",
			"age" : "0"
		},
		"104:1" : {
			"facing" : "up",
			"age" : "1"
		},
		"104:2" : {
			"facing" : "up",
			"age" : "2"
		},
		"104:3" : {
			"facing" : "up",
			"age" : "3"
		},
		"104:4" : {
			"facing" : "up",
			"age" : "4"
		},
		"104:5" : {
			"facing" : "up",
			"age" : "5"
		},
		"104:6" : {
			"facing" : "up",
			"age" : "6"
		},
		"104:7" : {
			"facing" : "up",
			"age" : "7"
		},
		"105:0" : {
			"facing" : "up",
			"age" : "0"
		},
		"105:1" : {
			"facing" : "up",
			"age" : "1"
		},
		"105:2" : {
			"facing" : "up",
			"age" : "2"
		},
		"105:3" : {
			"facing" : "up",
			"age" : "3"
		},
		"105:4" : {
			"facing" : "up",
			"age" : "4"
		},
		"105:5" : {
			"facing" : "up",
			"age" : "5"
		},
		"105:6" : {
			"facing" : "up",
			"age" : "6"
		},
		"105:7" : {
			"facing" : "up",
			"age" : "7"
		},
		"106:0" : {
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"up" : "false",
			"south" : "false"
		},
		"106:1" : {
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"up" : "false",
			"south" : "true"
		},
		"106:2" : {
			"west" : "true",
			"east" : "false",
			"north" : "false",
			"up" : "false",
			"south" : "false"
		},
		"106:3" : {
			"west" : "true",
			"east" : "false",
			"north" : "false",
			"up" : "false",
			"south" : "true"
		},
		"106:4" : {
			"west" : "false",
			"east" : "false",
			"north" : "true",
			"up" : "false",
			"south" : "false"
		},
		"106:5" : {
			"west" : "false",
			"east" : "false",
			"north" : "true",
			"up" : "false",
			"south" : "true"
		},
		"106:6" : {
			"west" : "true",
			"east" : "false",
			"north" : "true",
			"up" : "false",
			"south" : "false"
		},
		"106:7" : {
			"west" : "true",
			"east" : "false",
			"north" : "true",
			"up" : "false",
			"south" : "true"
		},
		"106:8" : {
			"west" : "false",
			"east" : "true",
			"north" : "false",
			"up" : "false",
			"south" : "false"
		},
		"106:9" : {
			"west" : "false",
			"east" : "true",
			"north" : "false",
			"up" : "false",
			"south" : "true"
		},
		"106:10" : {
			"west" : "true",
			"east" : "true",
			"north" : "false",
			"up" : "false",
			"south" : "false"
		},
		"106:11" : {
			"west" : "true",
			"east" : "true",
			"north" : "false",
			"up" : "false",
			"south" : "true"
		},
		"106:12" : {
			"west" : "false",
			"east" : "true",
			"north" : "true",
			"up" : "false",
			"south" : "false"
		},
		"106:13" : {
			"west" : "false",
			"east" : "true",
			"north" : "true",
			"up" : "false",
			"south" : "true"
		},
		"106:14" : {
			"west" : "true",
			"east" : "true",
			"north" : "true",
			"up" : "false",
			"south" : "false"
		},
		"106:15" : {
			"west" : "true",
			"east" : "true",
			"north" : "true",
			"up" : "false",
			"south" : "true"
		},
		"107:0" : {
			"facing" : "south",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "false"
		},
		"107:1" : {
			"facing" : "west",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "false"
		},
		"107:2" : {
			"facing" : "north",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "false"
		},
		"107:3" : {
			"facing" : "east",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "false"
		},
		"107:4" : {
			"facing" : "south",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "false"
		},
		"107:5" : {
			"facing" : "west",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "false"
		},
		"107:6" : {
			"facing" : "north",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "false"
		},
		"107:7" : {
			"facing" : "east",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "false"
		},
		"107:8" : {
			"facing" : "south",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "true"
		},
		"107:9" : {
			"facing" : "west",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "true"
		},
		"107:10" : {
			"facing" : "north",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "true"
		},
		"107:11" : {
			"facing" : "east",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "true"
		},
		"107:12" : {
			"facing" : "south",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "true"
		},
		"107:13" : {
			"facing" : "west",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "true"
		},
		"107:14" : {
			"facing" : "north",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "true"
		},
		"107:15" : {
			"facing" : "east",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "true"
		},
		"108:0" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "bottom"
		},
		"108:1" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "bottom"
		},
		"108:2" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "bottom"
		},
		"108:3" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "bottom"
		},
		"108:4" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "top"
		},
		"108:5" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "top"
		},
		"108:6" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "top"
		},
		"108:7" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "top"
		},
		"109:0" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "bottom"
		},
		"109:1" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "bottom"
		},
		"109:2" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "bottom"
		},
		"109:3" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "bottom"
		},
		"109:4" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "top"
		},
		"109:5" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "top"
		},
		"109:6" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "top"
		},
		"109:7" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "top"
		},
		"110:0" : {
			"snowy" : "false"
		},
		"111:0" : {},
		"112:0" : {},
		"113:0" : {
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"south" : "false"
		},
		"114:0" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "bottom"
		},
		"114:1" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "bottom"
		},
		"114:2" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "bottom"
		},
		"114:3" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "bottom"
		},
		"114:4" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "top"
		},
		"114:5" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "top"
		},
		"114:6" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "top"
		},
		"114:7" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "top"
		},
		"115:0" : {
			"age" : "0"
		},
		"115:1" : {
			"age" : "1"
		},
		"115:2" : {
			"age" : "2"
		},
		"115:3" : {
			"age" : "3"
		},
		"116:0" : {},
		"117:0" : {
			"has_bottle_2" : "false",
			"has_bottle_0" : "false",
			"has_bottle_1" : "false"
		},
		"117:1" : {
			"has_bottle_2" : "false",
			"has_bottle_0" : "true",
			"has_bottle_1" : "false"
		},
		"117:2" : {
			"has_bottle_2" : "false",
			"has_bottle_0" : "false",
			"has_bottle_1" : "true"
		},
		"117:3" : {
			"has_bottle_2" : "false",
			"has_bottle_0" : "true",
			"has_bottle_1" : "true"
		},
		"117:4" : {
			"has_bottle_2" : "true",
			"has_bottle_0" : "false",
			"has_bottle_1" : "false"
		},
		"117:5" : {
			"has_bottle_2" : "true",
			"has_bottle_0" : "true",
			"has_bottle_1" : "false"
		},
		"117:6" : {
			"has_bottle_2" : "true",
			"has_bottle_0" : "false",
			"has_bottle_1" : "true"
		},
		"117:7" : {
			"has_bottle_2" : "true",
			"has_bottle_0" : "true",
			"has_bottle_1" : "true"
		},
		"118:0" : {
			"level" : "0"
		},
		"118:1" : {
			"level" : "1"
		},
		"118:2" : {
			"level" : "2"
		},
		"118:3" : {
			"level" : "3"
		},
		"119:0" : {},
		"120:0" : {
			"facing" : "south",
			"eye" : "false"
		},
		"120:1" : {
			"facing" : "west",
			"eye" : "false"
		},
		"120:2" : {
			"facing" : "north",
			"eye" : "false"
		},
		"120:3" : {
			"facing" : "east",
			"eye" : "false"
		},
		"120:4" : {
			"facing" : "south",
			"eye" : "true"
		},
		"120:5" : {
			"facing" : "west",
			"eye" : "true"
		},
		"120:6" : {
			"facing" : "north",
			"eye" : "true"
		},
		"120:7" : {
			"facing" : "east",
			"eye" : "true"
		},
		"121:0" : {},
		"122:0" : {},
		"123:0" : {},
		"124:0" : {},
		"125:0" : {
			"variant" : "oak"
		},
		"125:1" : {
			"variant" : "spruce"
		},
		"125:2" : {
			"variant" : "birch"
		},
		"125:3" : {
			"variant" : "jungle"
		},
		"125:4" : {
			"variant" : "acacia"
		},
		"125:5" : {
			"variant" : "dark_oak"
		},
		"126:0" : {
			"variant" : "oak",
			"half" : "bottom"
		},
		"126:1" : {
			"variant" : "spruce",
			"half" : "bottom"
		},
		"126:2" : {
			"variant" : "birch",
			"half" : "bottom"
		},
		"126:3" : {
			"variant" : "jungle",
			"half" : "bottom"
		},
		"126:4" : {
			"variant" : "acacia",
			"half" : "bottom"
		},
		"126:5" : {
			"variant" : "dark_oak",
			"half" : "bottom"
		},
		"126:8" : {
			"variant" : "oak",
			"half" : "top"
		},
		"126:9" : {
			"variant" : "spruce",
			"half" : "top"
		},
		"126:10" : {
			"variant" : "birch",
			"half" : "top"
		},
		"126:11" : {
			"variant" : "jungle",
			"half" : "top"
		},
		"126:12" : {
			"variant" : "acacia",
			"half" : "top"
		},
		"126:13" : {
			"variant" : "dark_oak",
			"half" : "top"
		},
		"127:0" : {
			"facing" : "south",
			"age" : "0"
		},
		"127:1" : {
			"facing" : "west",
			"age" : "0"
		},
		"127:2" : {
			"facing" : "north",
			"age" : "0"
		},
		"127:3" : {
			"facing" : "east",
			"age" : "0"
		},
		"127:4" : {
			"facing" : "south",
			"age" : "1"
		},
		"127:5" : {
			"facing" : "west",
			"age" : "1"
		},
		"127:6" : {
			"facing" : "north",
			"age" : "1"
		},
		"127:7" : {
			"facing" : "east",
			"age" : "1"
		},
		"127:8" : {
			"facing" : "south",
			"age" : "2"
		},
		"127:9" : {
			"facing" : "west",
			"age" : "2"
		},
		"127:10" : {
			"facing" : "north",
			"age" : "2"
		},
		"127:11" : {
			"facing" : "east",
			"age" : "2"
		},
		"128:0" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "bottom"
		},
		"128:1" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "bottom"
		},
		"128:2" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "bottom"
		},
		"128:3" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "bottom"
		},
		"128:4" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "top"
		},
		"128:5" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "top"
		},
		"128:6" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "top"
		},
		"128:7" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "top"
		},
		"129:0" : {},
		"130:2" : {
			"facing" : "north"
		},
		"130:3" : {
			"facing" : "south"
		},
		"130:4" : {
			"facing" : "west"
		},
		"130:5" : {
			"facing" : "east"
		},
		"131:0" : {
			"facing" : "south",
			"attached" : "false",
			"powered" : "false"
		},
		"131:1" : {
			"facing" : "west",
			"attached" : "false",
			"powered" : "false"
		},
		"131:2" : {
			"facing" : "north",
			"attached" : "false",
			"powered" : "false"
		},
		"131:3" : {
			"facing" : "east",
			"attached" : "false",
			"powered" : "false"
		},
		"131:4" : {
			"facing" : "south",
			"attached" : "true",
			"powered" : "false"
		},
		"131:5" : {
			"facing" : "west",
			"attached" : "true",
			"powered" : "false"
		},
		"131:6" : {
			"facing" : "north",
			"attached" : "true",
			"powered" : "false"
		},
		"131:7" : {
			"facing" : "east",
			"attached" : "true",
			"powered" : "false"
		},
		"131:8" : {
			"facing" : "south",
			"attached" : "false",
			"powered" : "true"
		},
		"131:9" : {
			"facing" : "west",
			"attached" : "false",
			"powered" : "true"
		},
		"131:10" : {
			"facing" : "north",
			"attached" : "false",
			"powered" : "true"
		},
		"131:11" : {
			"facing" : "east",
			"attached" : "false",
			"powered" : "true"
		},
		"131:12" : {
			"facing" : "south",
			"attached" : "true",
			"powered" : "true"
		},
		"131:13" : {
			"facing" : "west",
			"attached" : "true",
			"powered" : "true"
		},
		"131:14" : {
			"facing" : "north",
			"attached" : "true",
			"powered" : "true"
		},
		"131:15" : {
			"facing" : "east",
			"attached" : "true",
			"powered" : "true"
		},
		"132:14" : {
			"north" : "false",
			"powered" : "false",
			"west" : "false",
			"attached" : "false",
			"east" : "false",
			"disarmed" : "false",
			"south" : "false"
		},
		"132:13" : {
			"north" : "false",
			"powered" : "true",
			"west" : "false",
			"attached" : "true",
			"east" : "false",
			"disarmed" : "true",
			"south" : "false"
		},
		"133:0" : {},
		"134:0" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "bottom"
		},
		"134:1" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "bottom"
		},
		"134:2" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "bottom"
		},
		"134:3" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "bottom"
		},
		"134:4" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "top"
		},
		"134:5" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "top"
		},
		"134:6" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "top"
		},
		"134:7" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "top"
		},
		"135:0" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "bottom"
		},
		"135:1" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "bottom"
		},
		"135:2" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "bottom"
		},
		"135:3" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "bottom"
		},
		"135:4" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "top"
		},
		"135:5" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "top"
		},
		"135:6" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "top"
		},
		"135:7" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "top"
		},
		"136:0" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "bottom"
		},
		"136:1" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "bottom"
		},
		"136:2" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "bottom"
		},
		"136:3" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "bottom"
		},
		"136:4" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "top"
		},
		"136:5" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "top"
		},
		"136:6" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "top"
		},
		"136:7" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "top"
		},
		"137:0" : {
			"facing" : "down",
			"conditional" : "false"
		},
		"137:1" : {
			"facing" : "up",
			"conditional" : "false"
		},
		"137:2" : {
			"facing" : "north",
			"conditional" : "false"
		},
		"137:3" : {
			"facing" : "south",
			"conditional" : "false"
		},
		"137:4" : {
			"facing" : "west",
			"conditional" : "false"
		},
		"137:5" : {
			"facing" : "east",
			"conditional" : "false"
		},
		"137:8" : {
			"facing" : "down",
			"conditional" : "true"
		},
		"137:9" : {
			"facing" : "up",
			"conditional" : "true"
		},
		"137:10" : {
			"facing" : "north",
			"conditional" : "true"
		},
		"137:11" : {
			"facing" : "south",
			"conditional" : "true"
		},
		"137:12" : {
			"facing" : "west",
			"conditional" : "true"
		},
		"137:13" : {
			"facing" : "east",
			"conditional" : "true"
		},
		"138:0" : {},
		"139:0" : {
			"north" : "false",
			"west" : "false",
			"variant" : "cobblestone",
			"up" : "false",
			"east" : "false",
			"south" : "false"
		},
		"139:1" : {
			"north" : "false",
			"west" : "false",
			"variant" : "mossy_cobblestone",
			"up" : "false",
			"east" : "false",
			"south" : "false"
		},
		"141:0" : {
			"age" : "0"
		},
		"141:1" : {
			"age" : "1"
		},
		"141:2" : {
			"age" : "2"
		},
		"141:3" : {
			"age" : "3"
		},
		"141:4" : {
			"age" : "4"
		},
		"141:5" : {
			"age" : "5"
		},
		"141:6" : {
			"age" : "6"
		},
		"141:7" : {
			"age" : "7"
		},
		"144:0" : {
			"facing" : "down",
			"nodrop" : "false"
		},
		"144:1" : {
			"facing" : "up",
			"nodrop" : "false"
		},
		"144:2" : {
			"facing" : "north",
			"nodrop" : "false"
		},
		"144:3" : {
			"facing" : "south",
			"nodrop" : "false"
		},
		"144:4" : {
			"facing" : "west",
			"nodrop" : "false"
		},
		"144:5" : {
			"facing" : "east",
			"nodrop" : "false"
		},
		"144:8" : {
			"facing" : "down",
			"nodrop" : "true"
		},
		"144:9" : {
			"facing" : "up",
			"nodrop" : "true"
		},
		"144:10" : {
			"facing" : "north",
			"nodrop" : "true"
		},
		"144:11" : {
			"facing" : "south",
			"nodrop" : "true"
		},
		"144:12" : {
			"facing" : "west",
			"nodrop" : "true"
		},
		"144:13" : {
			"facing" : "east",
			"nodrop" : "true"
		},
		"146:2" : {
			"facing" : "north"
		},
		"146:3" : {
			"facing" : "south"
		},
		"146:4" : {
			"facing" : "west"
		},
		"146:5" : {
			"facing" : "east"
		},
		"151:0" : {
			"power" : "0"
		},
		"151:1" : {
			"power" : "1"
		},
		"151:2" : {
			"power" : "2"
		},
		"151:3" : {
			"power" : "3"
		},
		"151:4" : {
			"power" : "4"
		},
		"151:5" : {
			"power" : "5"
		},
		"151:6" : {
			"power" : "6"
		},
		"151:7" : {
			"power" : "7"
		},
		"151:8" : {
			"power" : "8"
		},
		"151:9" : {
			"power" : "9"
		},
		"151:10" : {
			"power" : "10"
		},
		"151:11" : {
			"power" : "11"
		},
		"151:12" : {
			"power" : "12"
		},
		"151:13" : {
			"power" : "13"
		},
		"151:14" : {
			"power" : "14"
		},
		"151:15" : {
			"power" : "15"
		},
		"152:0" : {},
		"153:0" : {},
		"154:0" : {
			"facing" : "down",
			"enabled" : "false"
		},
		"154:10" : {
			"facing" : "north",
			"enabled" : "true"
		},
		"154:11" : {
			"facing" : "south",
			"enabled" : "true"
		},
		"154:12" : {
			"facing" : "west",
			"enabled" : "true"
		},
		"154:13" : {
			"facing" : "east",
			"enabled" : "true"
		},
		"154:8" : {
			"facing" : "down",
			"enabled" : "true"
		},
		"155:0" : {
			"variant" : "default"
		},
		"155:1" : {
			"variant" : "chiseled"
		},
		"155:2" : {
			"variant" : "lines_y"
		},
		"155:3" : {
			"variant" : "lines_x"
		},
		"155:4" : {
			"variant" : "lines_z"
		},
		"156:0" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "bottom"
		},
		"156:1" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "bottom"
		},
		"156:2" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "bottom"
		},
		"156:3" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "bottom"
		},
		"156:4" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "top"
		},
		"156:5" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "top"
		},
		"156:6" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "top"
		},
		"156:7" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "top"
		},
		"157:0" : {
			"shape" : "north_south",
			"powered" : "false"
		},
		"157:1" : {
			"shape" : "east_west",
			"powered" : "false"
		},
		"157:2" : {
			"shape" : "ascending_east",
			"powered" : "false"
		},
		"157:3" : {
			"shape" : "ascending_west",
			"powered" : "false"
		},
		"157:4" : {
			"shape" : "ascending_north",
			"powered" : "false"
		},
		"157:5" : {
			"shape" : "ascending_south",
			"powered" : "false"
		},
		"157:8" : {
			"shape" : "north_south",
			"powered" : "true"
		},
		"157:9" : {
			"shape" : "east_west",
			"powered" : "true"
		},
		"157:10" : {
			"shape" : "ascending_east",
			"powered" : "true"
		},
		"157:11" : {
			"shape" : "ascending_west",
			"powered" : "true"
		},
		"157:12" : {
			"shape" : "ascending_north",
			"powered" : "true"
		},
		"157:13" : {
			"shape" : "ascending_south",
			"powered" : "true"
		},
		"158:0" : {
			"facing" : "down",
			"triggered" : "false"
		},
		"158:1" : {
			"facing" : "up",
			"triggered" : "false"
		},
		"158:2" : {
			"facing" : "north",
			"triggered" : "false"
		},
		"158:3" : {
			"facing" : "south",
			"triggered" : "false"
		},
		"158:4" : {
			"facing" : "west",
			"triggered" : "false"
		},
		"158:5" : {
			"facing" : "east",
			"triggered" : "false"
		},
		"158:8" : {
			"facing" : "down",
			"triggered" : "true"
		},
		"158:9" : {
			"facing" : "up",
			"triggered" : "true"
		},
		"158:10" : {
			"facing" : "north",
			"triggered" : "true"
		},
		"158:11" : {
			"facing" : "south",
			"triggered" : "true"
		},
		"158:12" : {
			"facing" : "west",
			"triggered" : "true"
		},
		"158:13" : {
			"facing" : "east",
			"triggered" : "true"
		},
		"159:0" : {
			"color" : "white"
		},
		"159:1" : {
			"color" : "orange"
		},
		"159:2" : {
			"color" : "magenta"
		},
		"159:3" : {
			"color" : "light_blue"
		},
		"159:4" : {
			"color" : "yellow"
		},
		"159:5" : {
			"color" : "lime"
		},
		"159:6" : {
			"color" : "pink"
		},
		"159:7" : {
			"color" : "gray"
		},
		"159:8" : {
			"color" : "silver"
		},
		"159:9" : {
			"color" : "cyan"
		},
		"159:10" : {
			"color" : "purple"
		},
		"159:11" : {
			"color" : "blue"
		},
		"159:12" : {
			"color" : "brown"
		},
		"159:13" : {
			"color" : "green"
		},
		"159:14" : {
			"color" : "red"
		},
		"159:15" : {
			"color" : "black"
		},
		"160:0" : {
			"color" : "white",
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"south" : "false"
		},
		"160:1" : {
			"color" : "orange",
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"south" : "false"
		},
		"160:2" : {
			"color" : "magenta",
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"south" : "false"
		},
		"160:3" : {
			"color" : "light_blue",
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"south" : "false"
		},
		"160:4" : {
			"color" : "yellow",
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"south" : "false"
		},
		"160:5" : {
			"color" : "lime",
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"south" : "false"
		},
		"160:6" : {
			"color" : "pink",
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"south" : "false"
		},
		"160:7" : {
			"color" : "gray",
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"south" : "false"
		},
		"160:8" : {
			"color" : "silver",
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"south" : "false"
		},
		"160:9" : {
			"color" : "cyan",
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"south" : "false"
		},
		"160:10" : {
			"color" : "purple",
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"south" : "false"
		},
		"160:11" : {
			"color" : "blue",
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"south" : "false"
		},
		"160:12" : {
			"color" : "brown",
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"south" : "false"
		},
		"160:13" : {
			"color" : "green",
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"south" : "false"
		},
		"160:14" : {
			"color" : "red",
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"south" : "false"
		},
		"160:15" : {
			"color" : "black",
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"south" : "false"
		},
		"161:0" : {
			"variant" : "acacia",
			"check_decay" : "false",
			"decayable" : "true"
		},
		"161:1" : {
			"variant" : "dark_oak",
			"check_decay" : "false",
			"decayable" : "true"
		},
		"161:4" : {
			"variant" : "acacia",
			"check_decay" : "false",
			"decayable" : "false"
		},
		"161:5" : {
			"variant" : "dark_oak",
			"check_decay" : "false",
			"decayable" : "false"
		},
		"161:8" : {
			"variant" : "acacia",
			"check_decay" : "true",
			"decayable" : "true"
		},
		"161:9" : {
			"variant" : "dark_oak",
			"check_decay" : "true",
			"decayable" : "true"
		},
		"161:12" : {
			"variant" : "acacia",
			"check_decay" : "true",
			"decayable" : "false"
		},
		"161:13" : {
			"variant" : "dark_oak",
			"check_decay" : "true",
			"decayable" : "false"
		},
		"162:0" : {
			"variant" : "acacia",
			"axis" : "y"
		},
		"162:1" : {
			"variant" : "dark_oak",
			"axis" : "y"
		},
		"162:4" : {
			"variant" : "acacia",
			"axis" : "x"
		},
		"162:5" : {
			"variant" : "dark_oak",
			"axis" : "x"
		},
		"162:8" : {
			"variant" : "acacia",
			"axis" : "z"
		},
		"162:9" : {
			"variant" : "dark_oak",
			"axis" : "z"
		},
		"162:12" : {
			"variant" : "acacia",
			"axis" : "none"
		},
		"162:13" : {
			"variant" : "dark_oak",
			"axis" : "none"
		},
		"163:0" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "bottom"
		},
		"163:1" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "bottom"
		},
		"163:2" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "bottom"
		},
		"163:3" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "bottom"
		},
		"163:4" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "top"
		},
		"163:5" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "top"
		},
		"163:6" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "top"
		},
		"163:7" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "top"
		},
		"164:0" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "bottom"
		},
		"164:1" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "bottom"
		},
		"164:2" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "bottom"
		},
		"164:3" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "bottom"
		},
		"164:4" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "top"
		},
		"164:5" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "top"
		},
		"164:6" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "top"
		},
		"164:7" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "top"
		},
		"165:0" : {},
		"166:0" : {},
		"167:0" : {
			"facing" : "north",
			"open" : "false",
			"half" : "bottom"
		},
		"167:1" : {
			"facing" : "south",
			"open" : "false",
			"half" : "bottom"
		},
		"167:2" : {
			"facing" : "west",
			"open" : "false",
			"half" : "bottom"
		},
		"167:3" : {
			"facing" : "east",
			"open" : "false",
			"half" : "bottom"
		},
		"167:4" : {
			"facing" : "north",
			"open" : "true",
			"half" : "bottom"
		},
		"167:5" : {
			"facing" : "south",
			"open" : "true",
			"half" : "bottom"
		},
		"167:6" : {
			"facing" : "west",
			"open" : "true",
			"half" : "bottom"
		},
		"167:7" : {
			"facing" : "east",
			"open" : "true",
			"half" : "bottom"
		},
		"167:8" : {
			"facing" : "north",
			"open" : "false",
			"half" : "top"
		},
		"167:9" : {
			"facing" : "south",
			"open" : "false",
			"half" : "top"
		},
		"167:10" : {
			"facing" : "west",
			"open" : "false",
			"half" : "top"
		},
		"167:11" : {
			"facing" : "east",
			"open" : "false",
			"half" : "top"
		},
		"167:12" : {
			"facing" : "north",
			"open" : "true",
			"half" : "top"
		},
		"167:13" : {
			"facing" : "south",
			"open" : "true",
			"half" : "top"
		},
		"167:14" : {
			"facing" : "west",
			"open" : "true",
			"half" : "top"
		},
		"167:15" : {
			"facing" : "east",
			"open" : "true",
			"half" : "top"
		},
		"168:0" : {
			"variant" : "prismarine"
		},
		"168:1" : {
			"variant" : "prismarine_bricks"
		},
		"168:2" : {
			"variant" : "dark_prismarine"
		},
		"169:0" : {},
		"170:0" : {
			"axis" : "y"
		},
		"170:4" : {
			"axis" : "x"
		},
		"170:8" : {
			"axis" : "z"
		},
		"171:0" : {
			"color" : "white"
		},
		"171:1" : {
			"color" : "orange"
		},
		"171:2" : {
			"color" : "magenta"
		},
		"171:3" : {
			"color" : "light_blue"
		},
		"171:4" : {
			"color" : "yellow"
		},
		"171:5" : {
			"color" : "lime"
		},
		"171:6" : {
			"color" : "pink"
		},
		"171:7" : {
			"color" : "gray"
		},
		"171:8" : {
			"color" : "silver"
		},
		"171:9" : {
			"color" : "cyan"
		},
		"171:10" : {
			"color" : "purple"
		},
		"171:11" : {
			"color" : "blue"
		},
		"171:12" : {
			"color" : "brown"
		},
		"171:13" : {
			"color" : "green"
		},
		"171:14" : {
			"color" : "red"
		},
		"171:15" : {
			"color" : "black"
		},
		"172:0" : {},
		"173:0" : {},
		"174:0" : {},
		"175:11" : {
			"facing" : "north",
			"variant" : "sunflower",
			"half" : "lower"
		},
		"175:1" : {
			"facing" : "north",
			"variant" : "syringa",
			"half" : "lower"
		},
		"175:2" : {
			"facing" : "north",
			"variant" : "double_grass",
			"half" : "lower"
		},
		"175:3" : {
			"facing" : "north",
			"variant" : "double_fern",
			"half" : "lower"
		},
		"175:4" : {
			"facing" : "north",
			"variant" : "double_rose",
			"half" : "lower"
		},
		"175:5" : {
			"facing" : "north",
			"variant" : "paeonia",
			"half" : "lower"
		},
		"175:8" : {
			"facing" : "north",
			"variant" : "sunflower",
			"half" : "upper"
		},
		"176:0" : {
			"rotation" : "0"
		},
		"176:1" : {
			"rotation" : "1"
		},
		"176:2" : {
			"rotation" : "2"
		},
		"176:3" : {
			"rotation" : "3"
		},
		"176:4" : {
			"rotation" : "4"
		},
		"176:5" : {
			"rotation" : "5"
		},
		"176:6" : {
			"rotation" : "6"
		},
		"176:7" : {
			"rotation" : "7"
		},
		"176:8" : {
			"rotation" : "8"
		},
		"176:9" : {
			"rotation" : "9"
		},
		"176:10" : {
			"rotation" : "10"
		},
		"176:11" : {
			"rotation" : "11"
		},
		"176:12" : {
			"rotation" : "12"
		},
		"176:13" : {
			"rotation" : "13"
		},
		"176:14" : {
			"rotation" : "14"
		},
		"176:15" : {
			"rotation" : "15"
		},
		"177:2" : {
			"facing" : "north"
		},
		"177:3" : {
			"facing" : "south"
		},
		"177:4" : {
			"facing" : "west"
		},
		"177:5" : {
			"facing" : "east"
		},
		"178:0" : {
			"power" : "0"
		},
		"178:1" : {
			"power" : "1"
		},
		"178:2" : {
			"power" : "2"
		},
		"178:3" : {
			"power" : "3"
		},
		"178:4" : {
			"power" : "4"
		},
		"178:5" : {
			"power" : "5"
		},
		"178:6" : {
			"power" : "6"
		},
		"178:7" : {
			"power" : "7"
		},
		"178:8" : {
			"power" : "8"
		},
		"178:9" : {
			"power" : "9"
		},
		"178:10" : {
			"power" : "10"
		},
		"178:11" : {
			"power" : "11"
		},
		"178:12" : {
			"power" : "12"
		},
		"178:13" : {
			"power" : "13"
		},
		"178:14" : {
			"power" : "14"
		},
		"178:15" : {
			"power" : "15"
		},
		"179:0" : {
			"type" : "red_sandstone"
		},
		"179:1" : {
			"type" : "chiseled_red_sandstone"
		},
		"179:2" : {
			"type" : "smooth_red_sandstone"
		},
		"180:0" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "bottom"
		},
		"180:1" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "bottom"
		},
		"180:2" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "bottom"
		},
		"180:3" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "bottom"
		},
		"180:4" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "top"
		},
		"180:5" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "top"
		},
		"180:6" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "top"
		},
		"180:7" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "top"
		},
		"181:0" : {
			"variant" : "red_sandstone",
			"seamless" : "false"
		},
		"181:8" : {
			"variant" : "red_sandstone",
			"seamless" : "true"
		},
		"182:0" : {
			"variant" : "red_sandstone",
			"half" : "bottom"
		},
		"182:8" : {
			"variant" : "red_sandstone",
			"half" : "top"
		},
		"183:0" : {
			"facing" : "south",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "false"
		},
		"183:1" : {
			"facing" : "west",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "false"
		},
		"183:2" : {
			"facing" : "north",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "false"
		},
		"183:3" : {
			"facing" : "east",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "false"
		},
		"183:4" : {
			"facing" : "south",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "false"
		},
		"183:5" : {
			"facing" : "west",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "false"
		},
		"183:6" : {
			"facing" : "north",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "false"
		},
		"183:7" : {
			"facing" : "east",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "false"
		},
		"183:8" : {
			"facing" : "south",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "true"
		},
		"183:9" : {
			"facing" : "west",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "true"
		},
		"183:10" : {
			"facing" : "north",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "true"
		},
		"183:11" : {
			"facing" : "east",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "true"
		},
		"183:12" : {
			"facing" : "south",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "true"
		},
		"183:13" : {
			"facing" : "west",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "true"
		},
		"183:14" : {
			"facing" : "north",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "true"
		},
		"183:15" : {
			"facing" : "east",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "true"
		},
		"184:0" : {
			"facing" : "south",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "false"
		},
		"184:1" : {
			"facing" : "west",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "false"
		},
		"184:2" : {
			"facing" : "north",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "false"
		},
		"184:3" : {
			"facing" : "east",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "false"
		},
		"184:4" : {
			"facing" : "south",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "false"
		},
		"184:5" : {
			"facing" : "west",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "false"
		},
		"184:6" : {
			"facing" : "north",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "false"
		},
		"184:7" : {
			"facing" : "east",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "false"
		},
		"184:8" : {
			"facing" : "south",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "true"
		},
		"184:9" : {
			"facing" : "west",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "true"
		},
		"184:10" : {
			"facing" : "north",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "true"
		},
		"184:11" : {
			"facing" : "east",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "true"
		},
		"184:12" : {
			"facing" : "south",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "true"
		},
		"184:13" : {
			"facing" : "west",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "true"
		},
		"184:14" : {
			"facing" : "north",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "true"
		},
		"184:15" : {
			"facing" : "east",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "true"
		},
		"185:0" : {
			"facing" : "south",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "false"
		},
		"185:1" : {
			"facing" : "west",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "false"
		},
		"185:2" : {
			"facing" : "north",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "false"
		},
		"185:3" : {
			"facing" : "east",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "false"
		},
		"185:4" : {
			"facing" : "south",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "false"
		},
		"185:5" : {
			"facing" : "west",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "false"
		},
		"185:6" : {
			"facing" : "north",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "false"
		},
		"185:7" : {
			"facing" : "east",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "false"
		},
		"185:8" : {
			"facing" : "south",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "true"
		},
		"185:9" : {
			"facing" : "west",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "true"
		},
		"185:10" : {
			"facing" : "north",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "true"
		},
		"185:11" : {
			"facing" : "east",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "true"
		},
		"185:12" : {
			"facing" : "south",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "true"
		},
		"185:13" : {
			"facing" : "west",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "true"
		},
		"185:14" : {
			"facing" : "north",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "true"
		},
		"185:15" : {
			"facing" : "east",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "true"
		},
		"186:0" : {
			"facing" : "south",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "false"
		},
		"186:1" : {
			"facing" : "west",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "false"
		},
		"186:2" : {
			"facing" : "north",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "false"
		},
		"186:3" : {
			"facing" : "east",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "false"
		},
		"186:4" : {
			"facing" : "south",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "false"
		},
		"186:5" : {
			"facing" : "west",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "false"
		},
		"186:6" : {
			"facing" : "north",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "false"
		},
		"186:7" : {
			"facing" : "east",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "false"
		},
		"186:8" : {
			"facing" : "south",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "true"
		},
		"186:9" : {
			"facing" : "west",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "true"
		},
		"186:10" : {
			"facing" : "north",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "true"
		},
		"186:11" : {
			"facing" : "east",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "true"
		},
		"186:12" : {
			"facing" : "south",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "true"
		},
		"186:13" : {
			"facing" : "west",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "true"
		},
		"186:14" : {
			"facing" : "north",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "true"
		},
		"186:15" : {
			"facing" : "east",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "true"
		},
		"187:0" : {
			"facing" : "south",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "false"
		},
		"187:1" : {
			"facing" : "west",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "false"
		},
		"187:2" : {
			"facing" : "north",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "false"
		},
		"187:3" : {
			"facing" : "east",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "false"
		},
		"187:4" : {
			"facing" : "south",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "false"
		},
		"187:5" : {
			"facing" : "west",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "false"
		},
		"187:6" : {
			"facing" : "north",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "false"
		},
		"187:7" : {
			"facing" : "east",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "false"
		},
		"187:8" : {
			"facing" : "south",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "true"
		},
		"187:9" : {
			"facing" : "west",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "true"
		},
		"187:10" : {
			"facing" : "north",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "true"
		},
		"187:11" : {
			"facing" : "east",
			"in_wall" : "false",
			"open" : "false",
			"powered" : "true"
		},
		"187:12" : {
			"facing" : "south",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "true"
		},
		"187:13" : {
			"facing" : "west",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "true"
		},
		"187:14" : {
			"facing" : "north",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "true"
		},
		"187:15" : {
			"facing" : "east",
			"in_wall" : "false",
			"open" : "true",
			"powered" : "true"
		},
		"188:0" : {
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"south" : "false"
		},
		"189:0" : {
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"south" : "false"
		},
		"190:0" : {
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"south" : "false"
		},
		"191:0" : {
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"south" : "false"
		},
		"192:0" : {
			"west" : "false",
			"east" : "false",
			"north" : "false",
			"south" : "false"
		},
		"193:0" : {
			"facing" : "east",
			"hinge" : "left",
			"powered" : "false",
			"open" : "false",
			"half" : "lower"
		},
		"193:1" : {
			"facing" : "south",
			"hinge" : "left",
			"powered" : "false",
			"open" : "false",
			"half" : "lower"
		},
		"193:2" : {
			"facing" : "west",
			"hinge" : "left",
			"powered" : "false",
			"open" : "false",
			"half" : "lower"
		},
		"193:3" : {
			"facing" : "north",
			"hinge" : "left",
			"powered" : "false",
			"open" : "false",
			"half" : "lower"
		},
		"193:4" : {
			"facing" : "east",
			"hinge" : "left",
			"powered" : "false",
			"open" : "true",
			"half" : "lower"
		},
		"193:5" : {
			"facing" : "south",
			"hinge" : "left",
			"powered" : "false",
			"open" : "true",
			"half" : "lower"
		},
		"193:6" : {
			"facing" : "west",
			"hinge" : "left",
			"powered" : "false",
			"open" : "true",
			"half" : "lower"
		},
		"193:7" : {
			"facing" : "north",
			"hinge" : "left",
			"powered" : "false",
			"open" : "true",
			"half" : "lower"
		},
		"193:8" : {
			"facing" : "north",
			"hinge" : "left",
			"powered" : "false",
			"open" : "false",
			"half" : "upper"
		},
		"193:9" : {
			"facing" : "north",
			"hinge" : "right",
			"powered" : "false",
			"open" : "false",
			"half" : "upper"
		},
		"193:10" : {
			"facing" : "north",
			"hinge" : "left",
			"powered" : "true",
			"open" : "false",
			"half" : "upper"
		},
		"193:11" : {
			"facing" : "north",
			"hinge" : "right",
			"powered" : "true",
			"open" : "false",
			"half" : "upper"
		},
		"195:0" : {
			"facing" : "east",
			"hinge" : "left",
			"powered" : "false",
			"open" : "false",
			"half" : "lower"
		},
		"195:1" : {
			"facing" : "south",
			"hinge" : "left",
			"powered" : "false",
			"open" : "false",
			"half" : "lower"
		},
		"195:2" : {
			"facing" : "west",
			"hinge" : "left",
			"powered" : "false",
			"open" : "false",
			"half" : "lower"
		},
		"195:3" : {
			"facing" : "north",
			"hinge" : "left",
			"powered" : "false",
			"open" : "false",
			"half" : "lower"
		},
		"195:4" : {
			"facing" : "east",
			"hinge" : "left",
			"powered" : "false",
			"open" : "true",
			"half" : "lower"
		},
		"195:5" : {
			"facing" : "south",
			"hinge" : "left",
			"powered" : "false",
			"open" : "true",
			"half" : "lower"
		},
		"195:6" : {
			"facing" : "west",
			"hinge" : "left",
			"powered" : "false",
			"open" : "true",
			"half" : "lower"
		},
		"195:7" : {
			"facing" : "north",
			"hinge" : "left",
			"powered" : "false",
			"open" : "true",
			"half" : "lower"
		},
		"195:8" : {
			"facing" : "north",
			"hinge" : "left",
			"powered" : "false",
			"open" : "false",
			"half" : "upper"
		},
		"195:9" : {
			"facing" : "north",
			"hinge" : "right",
			"powered" : "false",
			"open" : "false",
			"half" : "upper"
		},
		"195:10" : {
			"facing" : "north",
			"hinge" : "left",
			"powered" : "true",
			"open" : "false",
			"half" : "upper"
		},
		"195:11" : {
			"facing" : "north",
			"hinge" : "right",
			"powered" : "true",
			"open" : "false",
			"half" : "upper"
		},
		"197:0" : {
			"facing" : "east",
			"hinge" : "left",
			"powered" : "false",
			"open" : "false",
			"half" : "lower"
		},
		"197:1" : {
			"facing" : "south",
			"hinge" : "left",
			"powered" : "false",
			"open" : "false",
			"half" : "lower"
		},
		"197:2" : {
			"facing" : "west",
			"hinge" : "left",
			"powered" : "false",
			"open" : "false",
			"half" : "lower"
		},
		"197:3" : {
			"facing" : "north",
			"hinge" : "left",
			"powered" : "false",
			"open" : "false",
			"half" : "lower"
		},
		"197:4" : {
			"facing" : "east",
			"hinge" : "left",
			"powered" : "false",
			"open" : "true",
			"half" : "lower"
		},
		"197:5" : {
			"facing" : "south",
			"hinge" : "left",
			"powered" : "false",
			"open" : "true",
			"half" : "lower"
		},
		"197:6" : {
			"facing" : "west",
			"hinge" : "left",
			"powered" : "false",
			"open" : "true",
			"half" : "lower"
		},
		"197:7" : {
			"facing" : "north",
			"hinge" : "left",
			"powered" : "false",
			"open" : "true",
			"half" : "lower"
		},
		"197:8" : {
			"facing" : "north",
			"hinge" : "left",
			"powered" : "false",
			"open" : "false",
			"half" : "upper"
		},
		"197:9" : {
			"facing" : "north",
			"hinge" : "right",
			"powered" : "false",
			"open" : "false",
			"half" : "upper"
		},
		"197:10" : {
			"facing" : "north",
			"hinge" : "left",
			"powered" : "true",
			"open" : "false",
			"half" : "upper"
		},
		"197:11" : {
			"facing" : "north",
			"hinge" : "right",
			"powered" : "true",
			"open" : "false",
			"half" : "upper"
		},
		"198:0" : {
			"facing" : "down"
		},
		"198:1" : {
			"facing" : "up"
		},
		"198:2" : {
			"facing" : "north"
		},
		"198:3" : {
			"facing" : "south"
		},
		"198:4" : {
			"facing" : "west"
		},
		"198:5" : {
			"facing" : "east"
		},
		"199:0" : {
			"north" : "false",
			"west" : "false",
			"up" : "false",
			"down" : "false",
			"east" : "false",
			"south" : "false"
		},
		"200:0" : {
			"age" : "0"
		},
		"200:1" : {
			"age" : "1"
		},
		"200:2" : {
			"age" : "2"
		},
		"200:3" : {
			"age" : "3"
		},
		"200:4" : {
			"age" : "4"
		},
		"200:5" : {
			"age" : "5"
		},
		"201:0" : {},
		"202:0" : {
			"axis" : "y"
		},
		"202:4" : {
			"axis" : "x"
		},
		"202:8" : {
			"axis" : "z"
		},
		"203:0" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "bottom"
		},
		"203:1" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "bottom"
		},
		"203:2" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "bottom"
		},
		"203:3" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "bottom"
		},
		"203:4" : {
			"facing" : "east",
			"shape" : "straight",
			"half" : "top"
		},
		"203:5" : {
			"facing" : "west",
			"shape" : "straight",
			"half" : "top"
		},
		"203:6" : {
			"facing" : "south",
			"shape" : "straight",
			"half" : "top"
		},
		"203:7" : {
			"facing" : "north",
			"shape" : "straight",
			"half" : "top"
		},
		"204:0" : {
			"variant" : "default"
		},
		"205:0" : {
			"variant" : "default",
			"half" : "bottom"
		},
		"205:8" : {
			"variant" : "default",
			"half" : "top"
		},
		"206:0" : {},
		"208:0" : {},
		"209:0" : {},
		"210:0" : {
			"facing" : "down",
			"conditional" : "false"
		},
		"210:1" : {
			"facing" : "up",
			"conditional" : "false"
		},
		"210:2" : {
			"facing" : "north",
			"conditional" : "false"
		},
		"210:3" : {
			"facing" : "south",
			"conditional" : "false"
		},
		"210:4" : {
			"facing" : "west",
			"conditional" : "false"
		},
		"210:5" : {
			"facing" : "east",
			"conditional" : "false"
		},
		"210:8" : {
			"facing" : "down",
			"conditional" : "true"
		},
		"210:9" : {
			"facing" : "up",
			"conditional" : "true"
		},
		"210:10" : {
			"facing" : "north",
			"conditional" : "true"
		},
		"210:11" : {
			"facing" : "south",
			"conditional" : "true"
		},
		"210:12" : {
			"facing" : "west",
			"conditional" : "true"
		},
		"210:13" : {
			"facing" : "east",
			"conditional" : "true"
		},
		"211:0" : {
			"facing" : "down",
			"conditional" : "false"
		},
		"211:1" : {
			"facing" : "up",
			"conditional" : "false"
		},
		"211:2" : {
			"facing" : "north",
			"conditional" : "false"
		},
		"211:3" : {
			"facing" : "south",
			"conditional" : "false"
		},
		"211:4" : {
			"facing" : "west",
			"conditional" : "false"
		},
		"211:5" : {
			"facing" : "east",
			"conditional" : "false"
		},
		"211:8" : {
			"facing" : "down",
			"conditional" : "true"
		},
		"211:9" : {
			"facing" : "up",
			"conditional" : "true"
		},
		"211:10" : {
			"facing" : "north",
			"conditional" : "true"
		},
		"211:11" : {
			"facing" : "south",
			"conditional" : "true"
		},
		"211:12" : {
			"facing" : "west",
			"conditional" : "true"
		},
		"211:13" : {
			"facing" : "east",
			"conditional" : "true"
		},
		"212:0" : {
			"age" : "0"
		},
		"212:1" : {
			"age" : "1"
		},
		"212:2" : {
			"age" : "2"
		},
		"212:3" : {
			"age" : "3"
		},
		"213:0" : {},
		"214:0" : {},
		"215:0" : {},
		"216:0" : {
			"axis" : "y"
		},
		"216:4" : {
			"axis" : "x"
		},
		"216:8" : {
			"axis" : "z"
		},
		"217:0" : {},
		"255:0" : {
			"mode" : "save"
		},
		"255:1" : {
			"mode" : "load"
		},
		"255:2" : {
			"mode" : "corner"
		},
		"255:3" : {
			"mode" : "data"
		}
	},
    *
    * */

    /*public static final Block[] blocks = new Block[] {
            Blocks.AIR,
            Blocks.STONE,
            Blocks.GRASS_BLOCK,
            Blocks.DIRT,
            Blocks.COBBLESTONE,
            Blocks.OAK_WOOD,
            Blocks.OAK_SAPLING,
            Blocks.BEDROCK,
            null, // Water
            null, // Stationary water
            null, // Lava
            null, // Stationary lava
            Blocks.SAND,
            Blocks.GRAVEL,
            Blocks.GOLD_ORE,
            Blocks.IRON_ORE,
            Blocks.COAL_ORE,
            Blocks.OAK_LOG,
            Blocks.OAK_LEAVES,
            Blocks.SPONGE,
            Blocks.GLASS,
            null, // Red Cloth
            null, // Orange Cloth
            null, // Yellow Cloth
            null, // Lime Cloth
            null, // Green Cloth
            null, // Aqua green Cloth
            null, // Cyan Cloth
            null, // Blue Cloth
            null, // Purple Cloth
            null, // Indigo Cloth
            null, // Violet Cloth
            null, // Magenta Cloth
            null, // Pink Cloth
            null, // Black Cloth
            null, // Gray Cloth (White Cloth in Alpha)
            null, // White Cloth
            Blocks.DANDELION,
            Blocks.POPPY,
            null, // Brown Mushroom
            null, // Red Mushroom
            null, // Gold Block
            null, // Iron Block
            null, // Double Stair
            null, // Stair
            Blocks.BRICKS,
            Blocks.TNT,
            Blocks.BOOKSHELF,
            Blocks.MOSSY_COBBLESTONE,
            Blocks.OBSIDIAN,
            Blocks.TORCH,
            null, // Fire
            Blocks.SPAWNER,
            Blocks.OAK_STAIRS,
            Blocks.CHEST,
            Blocks.REDSTONE_WIRE,
            Blocks.DIAMOND_ORE,
            Blocks.DIAMOND_BLOCK,
            Blocks.CRAFTING_TABLE,
            null, // Crops
            null, // Soil
            Blocks.FURNACE,
            null, // Burning Furnace
            null, // Sign Post
            null, // Wooden Door, bottom half
            Blocks.LADDER,
            Blocks.RAIL,
            null, // Cobblestone Stairs
            null, // Sign
            Blocks.LEVER,
            null, // Stone Pressure Plate
            null, // Iron Door, bottom half
            null, // Wooden Pressure Plate
            Blocks.REDSTONE_ORE,
            null, // Lighted Redstone Ore
            null, // Redstone torch ("off" state)
            null, // Redstone torch ("on" state)
            Blocks.STONE_BUTTON,
            Blocks.SNOW,
            Blocks.ICE,
            Blocks.SNOW_BLOCK,
            Blocks.CACTUS,
            Blocks.CLAY,
            Blocks.SUGAR_CANE,
            Blocks.JUKEBOX
    };*/

    private static int makeId(int id1, int id2) {
        return (id1 << 8) | id2;
    }
}