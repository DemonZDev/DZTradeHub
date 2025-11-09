package online.demonzdevelopment.dztradehub.managers;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Area;
import online.demonzdevelopment.dztradehub.data.Shop;
import online.demonzdevelopment.dztradehub.data.ShopItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Expanded default shops setup with 10+ shops per area and 40+ items per shop
 */
public class ExpandedDefaultShopsSetup {

    /**
     * Create SuperMarket area with 12 premium shops and checkout system
     */
    public static void createExpandedSuperMarket(DZTradeHub plugin, Location loc) {
        Area area = new Area("SuperMarket", "§a§lSuper Market", Area.AreaType.SUPERMARKET, loc);
        area.setDescription(List.of("§7Premium marketplace", "§7with checkout system"));
        plugin.getShopManager().registerArea(area);
        plugin.getFileStorageManager().saveArea(area);

        // Shop 1: Food Shop (50 items)
        createShop(plugin, "SuperMarket", "FoodShop", "§c§lFood Shop", Shop.ShopType.BUY_ONLY, true, false,
            new Material[]{
                Material.BREAD, Material.COOKED_BEEF, Material.APPLE, Material.GOLDEN_APPLE, Material.COOKED_PORKCHOP,
                Material.BAKED_POTATO, Material.COOKIE, Material.CAKE, Material.PUMPKIN_PIE, Material.MUSHROOM_STEW,
                Material.COOKED_CHICKEN, Material.COOKED_MUTTON, Material.COOKED_RABBIT, Material.COOKED_COD, Material.COOKED_SALMON,
                Material.HONEY_BOTTLE, Material.SWEET_BERRIES, Material.GLOW_BERRIES, Material.MELON_SLICE, Material.CHORUS_FRUIT,
                Material.DRIED_KELP, Material.SUSPICIOUS_STEW, Material.RABBIT_STEW, Material.BEETROOT_SOUP, Material.CARROT,
                Material.POTATO, Material.POISONOUS_POTATO, Material.BEETROOT, Material.TROPICAL_FISH, Material.PUFFERFISH,
                Material.GOLDEN_CARROT, Material.ENCHANTED_GOLDEN_APPLE, Material.MILK_BUCKET, Material.SUGAR, Material.EGG,
                Material.WHEAT, Material.COCOA_BEANS, Material.PUMPKIN, Material.MELON, Material.NETHER_WART,
                Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.WARPED_FUNGUS, Material.CRIMSON_FUNGUS, Material.KELP,
                Material.SEAGRASS, Material.SEA_PICKLE, Material.BAMBOO, Material.GLISTERING_MELON_SLICE, Material.FERMENTED_SPIDER_EYE
            },
            15.0, 100.0, ShopItem.Currency.MONEY);

        // Shop 2: Building Shop (45 items)
        createShop(plugin, "SuperMarket", "BuildingShop", "§e§lBuilding Shop", Shop.ShopType.BUY_ONLY, true, false,
            new Material[]{
                Material.STONE, Material.COBBLESTONE, Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS, Material.MOSSY_STONE_BRICKS,
                Material.GRANITE, Material.POLISHED_GRANITE, Material.DIORITE, Material.POLISHED_DIORITE, Material.ANDESITE,
                Material.POLISHED_ANDESITE, Material.DEEPSLATE, Material.POLISHED_DEEPSLATE, Material.DEEPSLATE_BRICKS, Material.CRACKED_DEEPSLATE_BRICKS,
                Material.SANDSTONE, Material.RED_SANDSTONE, Material.SMOOTH_SANDSTONE, Material.CUT_SANDSTONE, Material.CHISELED_SANDSTONE,
                Material.BRICKS, Material.NETHER_BRICKS, Material.RED_NETHER_BRICKS, Material.END_STONE_BRICKS, Material.PRISMARINE,
                Material.PRISMARINE_BRICKS, Material.DARK_PRISMARINE, Material.QUARTZ_BLOCK, Material.SMOOTH_QUARTZ, Material.QUARTZ_BRICKS,
                Material.PURPUR_BLOCK, Material.PURPUR_PILLAR, Material.TERRACOTTA, Material.WHITE_TERRACOTTA, Material.BLACK_TERRACOTTA,
                Material.RED_CONCRETE, Material.WHITE_CONCRETE, Material.BLACK_CONCRETE, Material.GLASS, Material.WHITE_STAINED_GLASS,
                Material.GLOWSTONE, Material.SEA_LANTERN, Material.LANTERN, Material.SOUL_LANTERN, Material.SHROOMLIGHT
            },
            8.0, 120.0, ShopItem.Currency.MONEY);

        // Shop 3: Tool & Equipment Shop (45 items)
        createShop(plugin, "SuperMarket", "ToolShop", "§b§lTool & Equipment Shop", Shop.ShopType.BUY_ONLY, true, false,
            new Material[]{
                Material.WOODEN_PICKAXE, Material.WOODEN_AXE, Material.WOODEN_SHOVEL, Material.WOODEN_HOE, Material.WOODEN_SWORD,
                Material.STONE_PICKAXE, Material.STONE_AXE, Material.STONE_SHOVEL, Material.STONE_HOE, Material.STONE_SWORD,
                Material.IRON_PICKAXE, Material.IRON_AXE, Material.IRON_SHOVEL, Material.IRON_HOE, Material.IRON_SWORD,
                Material.GOLDEN_PICKAXE, Material.GOLDEN_AXE, Material.GOLDEN_SHOVEL, Material.GOLDEN_HOE, Material.GOLDEN_SWORD,
                Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.DIAMOND_SHOVEL, Material.DIAMOND_HOE, Material.DIAMOND_SWORD,
                Material.NETHERITE_PICKAXE, Material.NETHERITE_AXE, Material.NETHERITE_SHOVEL, Material.NETHERITE_HOE, Material.NETHERITE_SWORD,
                Material.SHEARS, Material.FISHING_ROD, Material.FLINT_AND_STEEL, Material.COMPASS, Material.CLOCK,
                Material.BUCKET, Material.WATER_BUCKET, Material.LAVA_BUCKET, Material.POWDER_SNOW_BUCKET, Material.MILK_BUCKET,
                Material.BOW, Material.CROSSBOW, Material.ARROW, Material.SPECTRAL_ARROW, Material.SHIELD
            },
            40.0, 800.0, ShopItem.Currency.MONEY);

        // Shop 4: Farming & Agriculture Shop (45 items)
        createShop(plugin, "SuperMarket", "FarmShop", "§2§lFarming & Agriculture", Shop.ShopType.BUY_ONLY, true, false,
            new Material[]{
                Material.WHEAT_SEEDS, Material.BEETROOT_SEEDS, Material.MELON_SEEDS, Material.PUMPKIN_SEEDS, Material.CARROT,
                Material.POTATO, Material.SWEET_BERRIES, Material.GLOW_BERRIES, Material.COCOA_BEANS, Material.SUGAR_CANE,
                Material.BAMBOO, Material.CACTUS, Material.KELP, Material.SEAGRASS, Material.NETHER_WART,
                Material.OAK_SAPLING, Material.BIRCH_SAPLING, Material.SPRUCE_SAPLING, Material.JUNGLE_SAPLING, Material.ACACIA_SAPLING,
                Material.DARK_OAK_SAPLING, Material.CHERRY_SAPLING, Material.MANGROVE_PROPAGULE, Material.CRIMSON_FUNGUS, Material.WARPED_FUNGUS,
                Material.BONE_MEAL, Material.COMPOSTER, Material.HAY_BLOCK, Material.WHEAT, Material.DRIED_KELP_BLOCK,
                Material.MOSS_BLOCK, Material.MOSS_CARPET, Material.AZALEA, Material.FLOWERING_AZALEA, Material.BIG_DRIPLEAF,
                Material.SMALL_DRIPLEAF, Material.SPORE_BLOSSOM, Material.GLOW_LICHEN, Material.VINE, Material.WEEPING_VINES,
                Material.TWISTING_VINES, Material.CHORUS_PLANT, Material.CHORUS_FLOWER, Material.SEA_PICKLE, Material.HONEYCOMB
            },
            4.0, 50.0, ShopItem.Currency.MONEY);

        // Shop 5: Decoration & Furniture Shop (45 items)
        createShop(plugin, "SuperMarket", "DecorShop", "§d§lDecoration & Furniture", Shop.ShopType.BUY_ONLY, true, false,
            new Material[]{
                Material.PAINTING, Material.ITEM_FRAME, Material.GLOW_ITEM_FRAME, Material.ARMOR_STAND, Material.PLAYER_HEAD,
                Material.FLOWER_POT, Material.DECORATED_POT, Material.RED_BANNER, Material.WHITE_BANNER, Material.BLACK_BANNER,
                Material.YELLOW_CARPET, Material.WHITE_CARPET, Material.RED_CARPET, Material.BLUE_CARPET, Material.GREEN_CARPET,
                Material.TORCH, Material.SOUL_TORCH, Material.REDSTONE_TORCH, Material.LANTERN, Material.SOUL_LANTERN,
                Material.CANDLE, Material.WHITE_CANDLE, Material.RED_CANDLE, Material.CAMPFIRE, Material.SOUL_CAMPFIRE,
                Material.END_ROD, Material.LIGHTNING_ROD, Material.CHAIN, Material.BELL, Material.LECTERN,
                Material.BOOKSHELF, Material.CHISELED_BOOKSHELF, Material.CRAFTING_TABLE, Material.CARTOGRAPHY_TABLE, Material.FLETCHING_TABLE,
                Material.SMITHING_TABLE, Material.LOOM, Material.STONECUTTER, Material.GRINDSTONE, Material.ANVIL,
                Material.ENCHANTING_TABLE, Material.BREWING_STAND, Material.CAULDRON, Material.BARREL, Material.CHEST
            },
            12.0, 90.0, ShopItem.Currency.MONEY);

        // Continuing with more shops...
        // Shop 6: Armor & Protection Shop (42 items)
        createShop(plugin, "SuperMarket", "ArmorShop", "§7§lArmor & Protection", Shop.ShopType.BUY_ONLY, true, false,
            new Material[]{
                Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, Material.LEATHER_HORSE_ARMOR,
                Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS, Material.IRON_HORSE_ARMOR,
                Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS, Material.GOLDEN_HORSE_ARMOR,
                Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS, Material.DIAMOND_HORSE_ARMOR,
                Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS, Material.NETHERITE_HELMET,
                Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS, Material.TURTLE_HELMET, Material.SHIELD,
                Material.TOTEM_OF_UNDYING, Material.ELYTRA, Material.SADDLE, Material.WOLF_ARMOR, Material.TURTLE_SCUTE,
                Material.TRIDENT, Material.NAUTILUS_SHELL, Material.HEART_OF_THE_SEA, Material.CONDUIT, Material.RECOVERY_COMPASS,
                Material.ECHO_SHARD, Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE
            },
            80.0, 1200.0, ShopItem.Currency.MONEY);

        // Shop 7: Redstone & Technical Shop (45 items)
        createShop(plugin, "SuperMarket", "RedstoneShop", "§4§lRedstone & Technical", Shop.ShopType.BUY_ONLY, true, false,
            new Material[]{
                Material.REDSTONE, Material.REDSTONE_TORCH, Material.REDSTONE_BLOCK, Material.REDSTONE_LAMP, Material.REPEATER,
                Material.COMPARATOR, Material.OBSERVER, Material.PISTON, Material.STICKY_PISTON, Material.SLIME_BLOCK,
                Material.HONEY_BLOCK, Material.DISPENSER, Material.DROPPER, Material.HOPPER, Material.CHEST,
                Material.TRAPPED_CHEST, Material.TNT, Material.LEVER, Material.STONE_BUTTON, Material.OAK_BUTTON,
                Material.STONE_PRESSURE_PLATE, Material.OAK_PRESSURE_PLATE, Material.LIGHT_WEIGHTED_PRESSURE_PLATE, Material.HEAVY_WEIGHTED_PRESSURE_PLATE, Material.TRIPWIRE_HOOK,
                Material.RAIL, Material.POWERED_RAIL, Material.DETECTOR_RAIL, Material.ACTIVATOR_RAIL, Material.MINECART,
                Material.CHEST_MINECART, Material.FURNACE_MINECART, Material.TNT_MINECART, Material.HOPPER_MINECART, Material.COMMAND_BLOCK_MINECART,
                Material.NOTE_BLOCK, Material.JUKEBOX, Material.TARGET, Material.DAYLIGHT_DETECTOR, Material.SCULK_SENSOR,
                Material.CALIBRATED_SCULK_SENSOR, Material.LIGHTNING_ROD, Material.COPPER_BULB, Material.CRAFTER, Material.TRIAL_SPAWNER
            },
            15.0, 200.0, ShopItem.Currency.MOBCOIN);

        // Shop 8: Precious Materials Shop (42 items)
        createShop(plugin, "SuperMarket", "PreciousShop", "§6§lPrecious Materials", Shop.ShopType.BUY_ONLY, true, false,
            new Material[]{
                Material.COAL, Material.COAL_BLOCK, Material.CHARCOAL, Material.IRON_INGOT, Material.IRON_BLOCK,
                Material.RAW_IRON, Material.RAW_IRON_BLOCK, Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE, Material.COPPER_INGOT,
                Material.COPPER_BLOCK, Material.RAW_COPPER, Material.RAW_COPPER_BLOCK, Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
                Material.GOLD_INGOT, Material.GOLD_BLOCK, Material.RAW_GOLD, Material.RAW_GOLD_BLOCK, Material.GOLD_ORE,
                Material.DEEPSLATE_GOLD_ORE, Material.NETHER_GOLD_ORE, Material.DIAMOND, Material.DIAMOND_BLOCK, Material.DIAMOND_ORE,
                Material.DEEPSLATE_DIAMOND_ORE, Material.EMERALD, Material.EMERALD_BLOCK, Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
                Material.LAPIS_LAZULI, Material.LAPIS_BLOCK, Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE, Material.REDSTONE,
                Material.REDSTONE_BLOCK, Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE, Material.QUARTZ, Material.NETHER_QUARTZ_ORE,
                Material.AMETHYST_SHARD, Material.AMETHYST_BLOCK
            },
            25.0, 300.0, ShopItem.Currency.MOBCOIN);

        // Shop 9: Nether & End Materials Shop (45 items)
        createShop(plugin, "SuperMarket", "NetherEndShop", "§5§lNether & End Materials", Shop.ShopType.BUY_ONLY, true, false,
            new Material[]{
                Material.NETHERRACK, Material.CRIMSON_NYLIUM, Material.WARPED_NYLIUM, Material.SOUL_SAND, Material.SOUL_SOIL,
                Material.BASALT, Material.SMOOTH_BASALT, Material.POLISHED_BASALT, Material.BLACKSTONE, Material.POLISHED_BLACKSTONE,
                Material.GILDED_BLACKSTONE, Material.NETHER_BRICKS, Material.RED_NETHER_BRICKS, Material.CRACKED_NETHER_BRICKS, Material.CHISELED_NETHER_BRICKS,
                Material.GLOWSTONE, Material.MAGMA_BLOCK, Material.SHROOMLIGHT, Material.NETHER_WART_BLOCK, Material.WARPED_WART_BLOCK,
                Material.CRIMSON_STEM, Material.WARPED_STEM, Material.CRIMSON_HYPHAE, Material.WARPED_HYPHAE, Material.CRIMSON_PLANKS,
                Material.WARPED_PLANKS, Material.CRYING_OBSIDIAN, Material.RESPAWN_ANCHOR, Material.LODESTONE, Material.ANCIENT_DEBRIS,
                Material.NETHERITE_SCRAP, Material.NETHERITE_INGOT, Material.NETHERITE_BLOCK, Material.END_STONE, Material.END_STONE_BRICKS,
                Material.PURPUR_BLOCK, Material.PURPUR_PILLAR, Material.CHORUS_FRUIT, Material.CHORUS_PLANT, Material.CHORUS_FLOWER,
                Material.DRAGON_HEAD, Material.DRAGON_EGG, Material.ELYTRA, Material.SHULKER_BOX, Material.END_CRYSTAL
            },
            30.0, 400.0, ShopItem.Currency.GEM);

        // Shop 10: Exotic & Rare Items Shop (45 items)
        createShop(plugin, "SuperMarket", "ExoticShop", "§b§lExotic & Rare Items", Shop.ShopType.BUY_ONLY, true, false,
            new Material[]{
                Material.SPONGE, Material.WET_SPONGE, Material.PRISMARINE_CRYSTALS, Material.PRISMARINE_SHARD, Material.NAUTILUS_SHELL,
                Material.HEART_OF_THE_SEA, Material.CONDUIT, Material.TURTLE_EGG, Material.TURTLE_SCUTE, Material.TRIDENT,
                Material.PHANTOM_MEMBRANE, Material.SHULKER_SHELL, Material.DRAGON_BREATH, Material.DRAGON_HEAD, Material.ELYTRA,
                Material.NETHER_STAR, Material.BEACON, Material.TOTEM_OF_UNDYING, Material.ECHO_SHARD, Material.RECOVERY_COMPASS,
                Material.MUSIC_DISC_13, Material.MUSIC_DISC_CAT, Material.MUSIC_DISC_BLOCKS, Material.MUSIC_DISC_CHIRP, Material.MUSIC_DISC_FAR,
                Material.MUSIC_DISC_MALL, Material.MUSIC_DISC_MELLOHI, Material.MUSIC_DISC_STAL, Material.MUSIC_DISC_STRAD, Material.MUSIC_DISC_WARD,
                Material.MUSIC_DISC_11, Material.MUSIC_DISC_WAIT, Material.MUSIC_DISC_PIGSTEP, Material.MUSIC_DISC_OTHERSIDE, Material.MUSIC_DISC_5,
                Material.DISC_FRAGMENT_5, Material.SCULK, Material.SCULK_VEIN, Material.SCULK_CATALYST, Material.SCULK_SHRIEKER,
                Material.REINFORCED_DEEPSLATE, Material.SNIFFER_EGG, Material.PITCHER_POD, Material.TORCHFLOWER_SEEDS, Material.SUSPICIOUS_SAND
            },
            100.0, 1500.0, ShopItem.Currency.GEM);

        // Shop 11: Dyes & Colors Shop (42 items)
        createShop(plugin, "SuperMarket", "DyeShop", "§e§lDyes & Colors", Shop.ShopType.BUY_ONLY, true, false,
            new Material[]{
                Material.WHITE_DYE, Material.LIGHT_GRAY_DYE, Material.GRAY_DYE, Material.BLACK_DYE, Material.BROWN_DYE,
                Material.RED_DYE, Material.ORANGE_DYE, Material.YELLOW_DYE, Material.LIME_DYE, Material.GREEN_DYE,
                Material.CYAN_DYE, Material.LIGHT_BLUE_DYE, Material.BLUE_DYE, Material.PURPLE_DYE, Material.MAGENTA_DYE,
                Material.PINK_DYE, Material.WHITE_WOOL, Material.LIGHT_GRAY_WOOL, Material.GRAY_WOOL, Material.BLACK_WOOL,
                Material.BROWN_WOOL, Material.RED_WOOL, Material.ORANGE_WOOL, Material.YELLOW_WOOL, Material.LIME_WOOL,
                Material.GREEN_WOOL, Material.CYAN_WOOL, Material.LIGHT_BLUE_WOOL, Material.BLUE_WOOL, Material.PURPLE_WOOL,
                Material.MAGENTA_WOOL, Material.PINK_WOOL, Material.WHITE_CONCRETE, Material.LIGHT_GRAY_CONCRETE, Material.GRAY_CONCRETE,
                Material.BLACK_CONCRETE, Material.RED_CONCRETE, Material.ORANGE_CONCRETE, Material.YELLOW_CONCRETE, Material.LIME_CONCRETE,
                Material.GREEN_CONCRETE, Material.CYAN_CONCRETE
            },
            5.0, 40.0, ShopItem.Currency.MONEY);

        // Shop 12: Flowers & Plants Shop (43 items)
        createShop(plugin, "SuperMarket", "FlowerShop", "§a§lFlowers & Plants", Shop.ShopType.BUY_ONLY, true, false,
            new Material[]{
                Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID, Material.ALLIUM, Material.AZURE_BLUET,
                Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP, Material.OXEYE_DAISY,
                Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY, Material.WITHER_ROSE, Material.SUNFLOWER, Material.LILAC,
                Material.ROSE_BUSH, Material.PEONY, Material.TALL_GRASS, Material.LARGE_FERN, Material.FERN,
                Material.DEAD_BUSH, Material.SHORT_GRASS, Material.SEAGRASS, Material.TALL_SEAGRASS, Material.SEA_PICKLE,
                Material.LILY_PAD, Material.SUGAR_CANE, Material.BAMBOO, Material.CACTUS, Material.VINE,
                Material.GLOW_LICHEN, Material.MOSS_CARPET, Material.MOSS_BLOCK, Material.HANGING_ROOTS, Material.BIG_DRIPLEAF,
                Material.SMALL_DRIPLEAF, Material.SPORE_BLOSSOM, Material.AZALEA, Material.FLOWERING_AZALEA, Material.PINK_PETALS,
                Material.TORCHFLOWER, Material.PITCHER_PLANT, Material.CHERRY_LEAVES
            },
            3.0, 30.0, ShopItem.Currency.MONEY);
    }

    // Helper method to create shop with items
    private static void createShop(DZTradeHub plugin, String areaName, String shopName, String displayName,
                                   Shop.ShopType shopType, boolean checkout, boolean reception,
                                   Material[] items, double minPrice, double maxPrice, ShopItem.Currency currency) {
        Shop shop = new Shop(shopName, displayName, shopType);

        // Configure queue systems
        if (checkout) {
            shop.setCheckoutEnabled(true);
            shop.setCheckoutNumber(3);
            shop.setCheckoutTimeKick(5);
            shop.setQueueType(Shop.QueueType.CASH_COUNTER);
        } else if (reception) {
            shop.setReceptionEnabled(true);
            shop.setReceptionNumber(2);
            shop.setReceptionTimeKick(300);
            shop.setReceptionAfkKick(60);
            shop.setQueueType(Shop.QueueType.RECEPTION);
        }

        // Add items to shop
        for (Material material : items) {
            ItemStack itemStack = new ItemStack(material, 1);
            double buyPrice = minPrice + (maxPrice - minPrice) * 0.7;
            double sellPrice = minPrice + (maxPrice - minPrice) * 0.3;

            ShopItem shopItem = new ShopItem(itemStack, buyPrice, sellPrice);
            shopItem.setCurrency(currency);
            shopItem.setMinPrice(minPrice);
            shopItem.setMaxPrice(maxPrice);
            shopItem.setCurrentStock(64);
            shopItem.setMaxStock(640);
            shopItem.setRefillInterval("DAILY");
            shopItem.setRefillAmount(64);
            shopItem.setDynamicPricingEnabled(true);

            // Set transaction type based on shop type
            if (shopType == Shop.ShopType.BUY_ONLY) {
                shopItem.setTransactionType(ShopItem.TransactionType.BUY_ONLY);
            } else if (shopType == Shop.ShopType.SELL_ONLY) {
                shopItem.setTransactionType(ShopItem.TransactionType.SELL_ONLY);
            } else {
                shopItem.setTransactionType(ShopItem.TransactionType.BOTH);
            }

            shop.addItem(shopItem);
        }

        plugin.getShopManager().registerShop(areaName, shop);
        plugin.getFileStorageManager().saveShop(areaName, shop);
        plugin.getFileStorageManager().saveShopItems(areaName, shop);
    }

    private static void linkShopToSelf(DZTradeHub plugin, String areaName, String shopName) {
        Shop shop = plugin.getShopManager().getShop(areaName, shopName);
        if (shop != null) {
            shop.setLinkedShopName(shopName);
            plugin.getFileStorageManager().saveShop(areaName, shop);
        }
    }

    private static void linkShop(DZTradeHub plugin, String fromArea, String fromShop, String toArea, String toShop) {
        Shop sellShop = plugin.getShopManager().getShop(fromArea, fromShop);
        if (sellShop != null) {
            // Use consistent format: just the shop name if same area, otherwise area:shop
            if (fromArea.equals(toArea)) {
                sellShop.setLinkedShopName(toShop);
            } else {
                sellShop.setLinkedShopName(toArea + ":" + toShop);
            }
            plugin.getFileStorageManager().saveShop(fromArea, sellShop);
        }
    }
}
