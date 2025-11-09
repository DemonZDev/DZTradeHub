package online.demonzdevelopment.dztradehub.managers;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Area;
import online.demonzdevelopment.dztradehub.data.Shop;
import online.demonzdevelopment.dztradehub.data.ShopItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DefaultShopsSetup {

    /**
     * Create SuperMarket area with premium items and checkout system
     */
    public static void createSuperMarket(DZTradeHub plugin, Location loc) {
        Area area = new Area("SuperMarket", "§a§lSuper Market", Area.AreaType.SUPERMARKET, loc);
        area.setDescription(List.of("§7Premium marketplace", "§7with checkout system"));
        plugin.getShopManager().registerArea(area);
        plugin.getFileStorageManager().saveArea(area);

        // Create 5 shops with 10 items each
        createShop(plugin, "SuperMarket", "FoodShop", "§c§lFood Shop", Shop.ShopType.BUY_ONLY, true, false, 
            new Material[]{Material.BREAD, Material.COOKED_BEEF, Material.APPLE, Material.GOLDEN_APPLE, 
                Material.COOKED_PORKCHOP, Material.BAKED_POTATO, Material.COOKIE, Material.CAKE, 
                Material.PUMPKIN_PIE, Material.MUSHROOM_STEW},
            20.0, 50.0, ShopItem.Currency.MONEY);

        createShop(plugin, "SuperMarket", "BuildingShop", "§e§lBuilding Shop", Shop.ShopType.BUY_ONLY, true, false,
            new Material[]{Material.STONE, Material.OAK_PLANKS, Material.GLASS, Material.BRICK, 
                Material.COBBLESTONE, Material.SANDSTONE, Material.QUARTZ_BLOCK, Material.WHITE_CONCRETE, 
                Material.TERRACOTTA, Material.GLOWSTONE},
            10.0, 100.0, ShopItem.Currency.MONEY);

        createShop(plugin, "SuperMarket", "ToolShop", "§b§lTool Shop", Shop.ShopType.BUY_ONLY, true, false,
            new Material[]{Material.IRON_PICKAXE, Material.IRON_AXE, Material.IRON_SHOVEL, Material.IRON_HOE, 
                Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.SHEARS, Material.FISHING_ROD, 
                Material.FLINT_AND_STEEL, Material.COMPASS},
            50.0, 500.0, ShopItem.Currency.MONEY);

        createShop(plugin, "SuperMarket", "FarmShop", "§2§lFarm Shop", Shop.ShopType.BUY_ONLY, true, false,
            new Material[]{Material.WHEAT_SEEDS, Material.CARROT, Material.POTATO, Material.BEETROOT_SEEDS, 
                Material.MELON_SEEDS, Material.PUMPKIN_SEEDS, Material.BONE_MEAL, Material.OAK_SAPLING, 
                Material.BIRCH_SAPLING, Material.SPRUCE_SAPLING},
            5.0, 30.0, ShopItem.Currency.MONEY);

        createShop(plugin, "SuperMarket", "DecorShop", "§d§lDecor Shop", Shop.ShopType.BUY_ONLY, true, false,
            new Material[]{Material.PAINTING, Material.FLOWER_POT, Material.RED_BANNER, Material.WHITE_CARPET, 
                Material.TORCH, Material.LANTERN, Material.CANDLE, Material.ITEM_FRAME, 
                Material.ARMOR_STAND, Material.END_ROD},
            15.0, 80.0, ShopItem.Currency.MONEY);
    }

    /**
     * Create Bazar area with normal prices and reception system
     */
    public static void createBazar(DZTradeHub plugin, Location loc) {
        Area area = new Area("Bazar", "§6§lBazar Market", Area.AreaType.BAZAR, loc);
        area.setDescription(List.of("§7Affordable marketplace", "§7with reception system"));
        plugin.getShopManager().registerArea(area);
        plugin.getFileStorageManager().saveArea(area);

        // Create 5 shops with buy and sell, linked to self
        createShop(plugin, "Bazar", "MeatShop", "§c§lMeat Shop", Shop.ShopType.BUY_SELL, false, true,
            new Material[]{Material.BEEF, Material.PORKCHOP, Material.CHICKEN, Material.MUTTON, 
                Material.RABBIT, Material.COD, Material.SALMON, Material.TROPICAL_FISH, 
                Material.PUFFERFISH, Material.ROTTEN_FLESH},
            5.0, 25.0, ShopItem.Currency.MONEY);
        linkShopToSelf(plugin, "Bazar", "MeatShop");

        createShop(plugin, "Bazar", "VegetableShop", "§2§lVegetable Shop", Shop.ShopType.BUY_SELL, false, true,
            new Material[]{Material.WHEAT, Material.CARROT, Material.POTATO, Material.BEETROOT, 
                Material.MELON_SLICE, Material.PUMPKIN, Material.SUGAR_CANE, Material.KELP, 
                Material.BAMBOO, Material.SWEET_BERRIES},
            3.0, 15.0, ShopItem.Currency.MONEY);
        linkShopToSelf(plugin, "Bazar", "VegetableShop");

        createShop(plugin, "Bazar", "OreShop", "§7§lOre Shop", Shop.ShopType.BUY_SELL, false, true,
            new Material[]{Material.COAL, Material.IRON_INGOT, Material.GOLD_INGOT, Material.DIAMOND, 
                Material.EMERALD, Material.LAPIS_LAZULI, Material.REDSTONE, Material.QUARTZ, 
                Material.COPPER_INGOT, Material.NETHERITE_INGOT},
            10.0, 100.0, ShopItem.Currency.MOBCOIN);
        linkShopToSelf(plugin, "Bazar", "OreShop");

        createShop(plugin, "Bazar", "WoodShop", "§6§lWood Shop", Shop.ShopType.BUY_SELL, false, true,
            new Material[]{Material.OAK_LOG, Material.BIRCH_LOG, Material.SPRUCE_LOG, Material.JUNGLE_LOG, 
                Material.ACACIA_LOG, Material.DARK_OAK_LOG, Material.CRIMSON_STEM, Material.WARPED_STEM, 
                Material.CHERRY_LOG, Material.MANGROVE_LOG},
            5.0, 20.0, ShopItem.Currency.MONEY);
        linkShopToSelf(plugin, "Bazar", "WoodShop");

        createShop(plugin, "Bazar", "MobDropShop", "§4§lMob Drop Shop", Shop.ShopType.BUY_SELL, false, true,
            new Material[]{Material.BONE, Material.STRING, Material.GUNPOWDER, Material.SPIDER_EYE, 
                Material.ENDER_PEARL, Material.BLAZE_ROD, Material.GHAST_TEAR, Material.SLIME_BALL, 
                Material.PHANTOM_MEMBRANE, Material.PRISMARINE_SHARD},
            8.0, 40.0, ShopItem.Currency.MOBCOIN);
        linkShopToSelf(plugin, "Bazar", "MobDropShop");
    }

    /**
     * Create PawnShop area for selling items (linked to SuperMarket)
     */
    public static void createPawnShop(DZTradeHub plugin, Location loc) {
        Area area = new Area("PawnShop", "§e§lPawn Shop", Area.AreaType.PAWNAREA, loc);
        area.setDescription(List.of("§7Sell your items", "§7for good prices"));
        plugin.getShopManager().registerArea(area);
        plugin.getFileStorageManager().saveArea(area);

        // Sell-only shops linked to SuperMarket
        createShop(plugin, "PawnShop", "SellFood", "§c§lSell Food", Shop.ShopType.SELL_ONLY, false, true,
            new Material[]{Material.BREAD, Material.COOKED_BEEF, Material.APPLE, Material.GOLDEN_APPLE, 
                Material.COOKED_PORKCHOP, Material.BAKED_POTATO, Material.COOKIE, Material.CAKE, 
                Material.PUMPKIN_PIE, Material.MUSHROOM_STEW},
            15.0, 40.0, ShopItem.Currency.MONEY);
        linkShop(plugin, "PawnShop", "SellFood", "SuperMarket", "FoodShop");

        createShop(plugin, "PawnShop", "SellBuilding", "§e§lSell Building Materials", Shop.ShopType.SELL_ONLY, false, true,
            new Material[]{Material.STONE, Material.OAK_PLANKS, Material.GLASS, Material.BRICK, 
                Material.COBBLESTONE, Material.SANDSTONE, Material.QUARTZ_BLOCK, Material.WHITE_CONCRETE, 
                Material.TERRACOTTA, Material.GLOWSTONE},
            8.0, 80.0, ShopItem.Currency.MONEY);
        linkShop(plugin, "PawnShop", "SellBuilding", "SuperMarket", "BuildingShop");

        createShop(plugin, "PawnShop", "SellTools", "§b§lSell Tools", Shop.ShopType.SELL_ONLY, false, true,
            new Material[]{Material.IRON_PICKAXE, Material.IRON_AXE, Material.IRON_SHOVEL, Material.IRON_HOE, 
                Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.SHEARS, Material.FISHING_ROD, 
                Material.FLINT_AND_STEEL, Material.COMPASS},
            40.0, 400.0, ShopItem.Currency.MONEY);
        linkShop(plugin, "PawnShop", "SellTools", "SuperMarket", "ToolShop");

        createShop(plugin, "PawnShop", "SellFarm", "§2§lSell Farm Items", Shop.ShopType.SELL_ONLY, false, true,
            new Material[]{Material.WHEAT_SEEDS, Material.CARROT, Material.POTATO, Material.BEETROOT_SEEDS, 
                Material.MELON_SEEDS, Material.PUMPKIN_SEEDS, Material.BONE_MEAL, Material.OAK_SAPLING, 
                Material.BIRCH_SAPLING, Material.SPRUCE_SAPLING},
            4.0, 25.0, ShopItem.Currency.MONEY);
        linkShop(plugin, "PawnShop", "SellFarm", "SuperMarket", "FarmShop");

        createShop(plugin, "PawnShop", "SellDecor", "§d§lSell Decorations", Shop.ShopType.SELL_ONLY, false, true,
            new Material[]{Material.PAINTING, Material.FLOWER_POT, Material.RED_BANNER, Material.WHITE_CARPET, 
                Material.TORCH, Material.LANTERN, Material.CANDLE, Material.ITEM_FRAME, 
                Material.ARMOR_STAND, Material.END_ROD},
            12.0, 65.0, ShopItem.Currency.MONEY);
        linkShop(plugin, "PawnShop", "SellDecor", "SuperMarket", "DecorShop");
    }

    /**
     * Create Junkyard area with low prices, no systems
     */
    public static void createJunkyard(DZTradeHub plugin, Location loc) {
        Area area = new Area("Junkyard", "§8§lJunkyard", Area.AreaType.JUNKYARD, loc);
        area.setDescription(List.of("§7Cheap items", "§7no queues"));
        plugin.getShopManager().registerArea(area);
        plugin.getFileStorageManager().saveArea(area);

        // Buy and sell, linked to self, no systems
        createShop(plugin, "Junkyard", "ScrapShop", "§7§lScrap Shop", Shop.ShopType.BUY_SELL, false, false,
            new Material[]{Material.COBBLESTONE, Material.DIRT, Material.GRAVEL, Material.SAND, 
                Material.NETHERRACK, Material.SOUL_SAND, Material.BASALT, Material.BLACKSTONE, 
                Material.ANDESITE, Material.DIORITE},
            1.0, 5.0, ShopItem.Currency.MONEY);
        linkShopToSelf(plugin, "Junkyard", "ScrapShop");

        createShop(plugin, "Junkyard", "JunkMaterials", "§8§lJunk Materials", Shop.ShopType.BUY_SELL, false, false,
            new Material[]{Material.STICK, Material.BOWL, Material.LEATHER, Material.FEATHER, 
                Material.FLINT, Material.CLAY_BALL, Material.SNOWBALL, Material.EGG, 
                Material.PAPER, Material.BOOK},
            2.0, 8.0, ShopItem.Currency.MONEY);
        linkShopToSelf(plugin, "Junkyard", "JunkMaterials");

        createShop(plugin, "Junkyard", "CommonOres", "§7§lCommon Ores", Shop.ShopType.BUY_SELL, false, false,
            new Material[]{Material.COAL, Material.IRON_INGOT, Material.COPPER_INGOT, Material.RAW_IRON, 
                Material.RAW_COPPER, Material.IRON_ORE, Material.COPPER_ORE, Material.COAL_ORE, 
                Material.RAW_GOLD, Material.GOLD_ORE},
            5.0, 15.0, ShopItem.Currency.MONEY);
        linkShopToSelf(plugin, "Junkyard", "CommonOres");

        createShop(plugin, "Junkyard", "PlantShop", "§2§lPlant Shop", Shop.ShopType.BUY_SELL, false, false,
            new Material[]{Material.WHEAT_SEEDS, Material.BEETROOT_SEEDS, Material.MELON_SEEDS, Material.PUMPKIN_SEEDS, 
                Material.OAK_SAPLING, Material.BIRCH_SAPLING, Material.TALL_GRASS, Material.FERN, 
                Material.DANDELION, Material.POPPY},
            1.0, 5.0, ShopItem.Currency.MONEY);
        linkShopToSelf(plugin, "Junkyard", "PlantShop");

        createShop(plugin, "Junkyard", "JunkTools", "§8§lJunk Tools", Shop.ShopType.BUY_SELL, false, false,
            new Material[]{Material.WOODEN_PICKAXE, Material.WOODEN_AXE, Material.WOODEN_SHOVEL, Material.WOODEN_HOE, 
                Material.STONE_PICKAXE, Material.STONE_AXE, Material.STONE_SHOVEL, Material.STONE_HOE, 
                Material.BUCKET, Material.SHEARS},
            3.0, 10.0, ShopItem.Currency.MONEY);
        linkShopToSelf(plugin, "Junkyard", "JunkTools");
    }

    /**
     * Create BlackMarket area with high prices and both systems
     */
    public static void createBlackMarket(DZTradeHub plugin, Location loc) {
        Area area = new Area("BlackMarket", "§4§lBlack Market", Area.AreaType.BLACKMARKET, loc);
        area.setDescription(List.of("§7Rare items", "§7premium prices"));
        plugin.getShopManager().registerArea(area);
        plugin.getFileStorageManager().saveArea(area);

        // Buy and sell, both systems, linked to self
        createShop(plugin, "BlackMarket", "RareItems", "§5§lRare Items", Shop.ShopType.BUY_SELL, true, true,
            new Material[]{Material.DIAMOND, Material.EMERALD, Material.NETHERITE_INGOT, Material.ANCIENT_DEBRIS, 
                Material.NETHERITE_SCRAP, Material.DRAGON_HEAD, Material.ELYTRA, Material.TRIDENT, 
                Material.HEART_OF_THE_SEA, Material.NETHER_STAR},
            100.0, 1000.0, ShopItem.Currency.GEM);
        linkShopToSelf(plugin, "BlackMarket", "RareItems");

        createShop(plugin, "BlackMarket", "EnchantedBooks", "§b§lEnchanted Books", Shop.ShopType.BUY_SELL, true, true,
            new Material[]{Material.ENCHANTED_BOOK, Material.BOOK, Material.BOOKSHELF, Material.LECTERN, 
                Material.WRITABLE_BOOK, Material.WRITTEN_BOOK, Material.KNOWLEDGE_BOOK, Material.ANVIL, 
                Material.ENCHANTING_TABLE, Material.EXPERIENCE_BOTTLE},
            50.0, 500.0, ShopItem.Currency.GEM);
        linkShopToSelf(plugin, "BlackMarket", "EnchantedBooks");

        createShop(plugin, "BlackMarket", "PotionShop", "§d§lPotion Shop", Shop.ShopType.BUY_SELL, true, true,
            new Material[]{Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.GLASS_BOTTLE, 
                Material.BREWING_STAND, Material.CAULDRON, Material.BLAZE_POWDER, Material.NETHER_WART, 
                Material.FERMENTED_SPIDER_EYE, Material.GLISTERING_MELON_SLICE},
            30.0, 300.0, ShopItem.Currency.MOBCOIN);
        linkShopToSelf(plugin, "BlackMarket", "PotionShop");

        createShop(plugin, "BlackMarket", "SpawnEggs", "§e§lSpawn Eggs", Shop.ShopType.BUY_SELL, true, true,
            new Material[]{Material.COW_SPAWN_EGG, Material.PIG_SPAWN_EGG, Material.SHEEP_SPAWN_EGG, Material.CHICKEN_SPAWN_EGG, 
                Material.HORSE_SPAWN_EGG, Material.WOLF_SPAWN_EGG, Material.CAT_SPAWN_EGG, Material.PARROT_SPAWN_EGG, 
                Material.VILLAGER_SPAWN_EGG, Material.IRON_GOLEM_SPAWN_EGG},
            80.0, 800.0, ShopItem.Currency.GEM);
        linkShopToSelf(plugin, "BlackMarket", "SpawnEggs");

        createShop(plugin, "BlackMarket", "SpecialBlocks", "§6§lSpecial Blocks", Shop.ShopType.BUY_SELL, true, true,
            new Material[]{Material.BEACON, Material.CONDUIT, Material.DRAGON_EGG, Material.END_CRYSTAL, 
                Material.SHULKER_BOX, Material.ENDER_CHEST, Material.SPAWNER, Material.COMMAND_BLOCK, 
                Material.STRUCTURE_BLOCK, Material.JIGSAW},
            200.0, 2000.0, ShopItem.Currency.GEM);
        linkShopToSelf(plugin, "BlackMarket", "SpecialBlocks");
    }

    /**
     * Create Kits area for selling special kits
     */
    public static void createKitsArea(DZTradeHub plugin, Location loc) {
        Area area = new Area("Kits", "§3§lKits Market", Area.AreaType.KITS, loc);
        area.setDescription(List.of("§7Special kits", "§7one-time purchase"));
        plugin.getShopManager().registerArea(area);
        plugin.getFileStorageManager().saveArea(area);

        // Sell-only kits
        createShop(plugin, "Kits", "StarterKit", "§a§lStarter Kit", Shop.ShopType.SELL_ONLY, false, false,
            new Material[]{Material.WOODEN_PICKAXE, Material.WOODEN_AXE, Material.WOODEN_SHOVEL, Material.BREAD, 
                Material.COOKED_BEEF, Material.TORCH, Material.CRAFTING_TABLE, Material.FURNACE, 
                Material.CHEST, Material.WHITE_BED},
            50.0, 100.0, ShopItem.Currency.MONEY);

        createShop(plugin, "Kits", "MinerKit", "§7§lMiner Kit", Shop.ShopType.SELL_ONLY, false, false,
            new Material[]{Material.IRON_PICKAXE, Material.IRON_SHOVEL, Material.TORCH, Material.LADDER, 
                Material.BUCKET, Material.COOKED_BEEF, Material.CRAFTING_TABLE, Material.FURNACE, 
                Material.CHEST, Material.ANVIL},
            150.0, 300.0, ShopItem.Currency.MONEY);

        createShop(plugin, "Kits", "FarmerKit", "§2§lFarmer Kit", Shop.ShopType.SELL_ONLY, false, false,
            new Material[]{Material.IRON_HOE, Material.WHEAT_SEEDS, Material.CARROT, Material.POTATO, 
                Material.BONE_MEAL, Material.BUCKET, Material.SHEARS, Material.COMPOSTER, 
                Material.BARREL, Material.HOPPER},
            120.0, 250.0, ShopItem.Currency.MONEY);

        createShop(plugin, "Kits", "BuilderKit", "§e§lBuilder Kit", Shop.ShopType.SELL_ONLY, false, false,
            new Material[]{Material.OAK_PLANKS, Material.STONE, Material.GLASS, Material.BRICK, 
                Material.OAK_STAIRS, Material.STONE_SLAB, Material.OAK_DOOR, Material.TORCH, 
                Material.GLOWSTONE, Material.CRAFTING_TABLE},
            180.0, 350.0, ShopItem.Currency.MONEY);

        createShop(plugin, "Kits", "WarriorKit", "§c§lWarrior Kit", Shop.ShopType.SELL_ONLY, false, false,
            new Material[]{Material.IRON_SWORD, Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, 
                Material.IRON_BOOTS, Material.SHIELD, Material.BOW, Material.ARROW, 
                Material.COOKED_BEEF, Material.GOLDEN_APPLE},
            300.0, 600.0, ShopItem.Currency.MOBCOIN);
    }

    // Helper methods
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
        Shop shop = plugin.getShopManager().getShop(fromArea, fromShop);
        if (shop != null) {
            shop.setLinkedShopName(toArea + ":" + toShop);
            plugin.getFileStorageManager().saveShop(fromArea, shop);
        }
    }
}
