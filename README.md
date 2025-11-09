# DZTradeHub - Complete Minecraft Economy Plugin

[![PaperMC](https://img.shields.io/badge/PaperMC-1.21.1-blue)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)
[![Status](https://img.shields.io/badge/status-Production%20Ready-brightgreen)](https://github.com/DemonZDev/DZTradeHub)
[![Security](https://img.shields.io/badge/security-Hardened-success)](SECURITY_CHANGELOG.md)

A comprehensive economy plugin featuring **Bank System**, **Bounty System**, **Casino Games**, **Auction House**, **Item Selling**, **Kits System**, and **Advanced Marketplace** for PaperMC 1.21.1

## 🔐 Security & Quality Update (v1.1.0)

**COMPREHENSIVE SECURITY & IMPROVEMENT UPDATE**

### Version 1.1.0 - New Features:

#### 🛡️ Security Enhancements
- ✅ **Queue Session Protection** - Prevents session hijacking on disconnect/reconnect
- ✅ **GUI Permission Re-checks** - Validates permissions on every GUI interaction
- ✅ **Secure Error Handling** - Prevents information disclosure through error messages
- ✅ **Admin Audit Logging** - Full accountability trail for all admin actions
- ✅ **Password Logging Audit** - Verified no sensitive data in logs

#### ⚡ Performance Improvements
- ✅ **Async Account Loading** - Non-blocking startup for faster server boot
- ✅ **Database Health Checks** - Auto-reconnect on connection loss (every 5 mins)
- ✅ **Command Rate Limiting** - Prevents spam and DoS attacks

#### 🎯 New Features
- ✅ **Password Strength Meter** - Real-time feedback on password quality
- ✅ **Transaction Export** - Export bank history to CSV format
- ✅ **Automated Backups** - Pre-critical-operation database backups (keeps last 10)
- ✅ **Enhanced Security Logging** - Dedicated security event log files

### Previous Security Fixes (v1.0.1):
- ✅ **PBKDF2 Password Hashing** - Secure bank account passwords (65K iterations)
- ✅ **Brute-Force Protection** - 3-attempt lockout system (5 minutes)
- ✅ **Transaction Atomicity** - Zero money loss on crashes
- ✅ **Thread Safety** - Prevents money duplication exploits

📄 [Full Changelog](CHANGELOG.md) | 📋 [Remaining Issues](REMAINING_ISSUES.md)

---

## ✨ New Features

### 🏦 Complete Bank System
- **7 Default Banks** with different currency support
- **3 Account Types** (Savings, Interest, Business)
- **Loan System** with interest tracking
- **Currency Conversion** between Money, MobCoin, Gem
- **Inter-bank Transfers** and account-to-account transfers
- **Level Progression** (5 levels) to reduce taxes
- **Password Protection** for secure accounts
- **Bank Payment Integration** in marketplace checkout

### 🛒 Item Selling System
- Sell items to shops with `/sell`, `/sellall`, `/sellhand`
- GUI-based item selection
- Auto-return unsellable items
- Supports all shop currencies

### 📦 Enhanced Kits
- `/kit` command for Kit area access (shops)
- `/kits` command for claimable kit packs
- Link kits to shops for selling
- Cooldown and permission system

## 🎯 Core Features

### 1. 🏦 Bank System (NEW!)
**Complete banking solution with 7 default banks!**

Store your money safely, earn interest, take loans, and transfer between banks!

**7 Default Banks:**
- **Money Bank** - MONEY currency only, secure storage
- **MobCoin Bank** - MOBCOIN currency only
- **Gem Bank** - GEM currency only
- **MMo Bank** - MONEY + MOBCOIN dual currency
- **MG Bank** - MONEY + GEM dual currency
- **MoG Bank** - MOBCOIN + GEM dual currency
- **Central Bank** - All 3 currencies, premium features

**Features:**
- **3 Account Types:**
  - **Savings Account** - Basic storage, full transfer rights (1.2x deposit, 1.3x withdrawal tax)
  - **Interest Account** - Earn interest hourly, no transfers (0.5x deposit, 0.8x withdrawal tax)
  - **Business Account** - Higher limits for traders (1.5x deposit, 1.8x withdrawal tax)
  
- **Currency Conversion** - Convert between currencies at configured rates (if bank supports 2+ currencies)
- **Account Transfers** - Transfer currency to other accounts in the same bank
- **Bank Transfers** - Transfer currency between different banks
- **Loan System** - Take loans with interest, payment period tracking, penalty interest for missed payments
- **Level System** - Upgrade your account (5 levels) to reduce taxes and increase interest rates
- **Global Taxes** - Deposit and withdrawal taxes (reduced by account level)
- **Interest Payouts** - Automatic hourly interest for Interest accounts
- **Max Storage Limits** - Per currency, per level
- **Queue System** - Reception queue for bank access
- **Password Protection** - Secure your account with password

**Commands:**
- `/bank` - Open bank list GUI
- `/bank list` - List all available banks
- `/<bank_name>` - Quick access to specific bank (e.g., `/money-bank`, `/central-bank`)
- `/<bank_name> create <SAVINGS|INTEREST|BUSINESS> <password>` - Create account
- `/<bank_name> help` - Show bank help

**Admin Commands:**
- `/bank create <name>` - Create new bank (requires config file)
- `/bank delete <name>` - Delete bank
- `/bank rename <old> <new>` - Rename bank
- `/bank config <name>` - Open bank configuration GUI

**Bank Payment Integration:**
- Pay for marketplace purchases using bank accounts
- Select payment method: Pocket or Bank during checkout
- Password authentication for bank payments
- Transfer from your banks or other player's banks

### 2. 💰 Bounty System
Place bounties on players with currency and item rewards!

**Features:**
- Create bounties with 3 currencies (Money, Mobcoin, Gem) + multiple items
- GUI-based bounty creation and management
- Automatic reward distribution on player kill
- Bounty numbers for easy management
- Privacy protection (others can't see reward amounts)
- Full refund system on deletion
- Persistent database storage

**Commands:**
- `/bounty` - Open bounty GUI
- `/bounty create <player>` - Create bounty (opens GUI)
- `/bounty delete <number>` - Delete your bounty
- `/bounty list` - List all bounties
- `/bounty manage [number]` - Manage bounties
- `/bounty help` - Show help

### 2. 🎰 Casino System

#### Coin Flip
**Single Player Mode:**
- Bet any currency and choose heads/tails
- 2x multiplier on win (configurable per rank)
- Animated coin flip with sounds
- Quick command: `/coinflip single <currency> <amount> <heads|tails>`

**Double Player Mode (Challenge):**
- Challenge other players to coin flip
- Request system with expiry (2 minutes)
- Accept/deny requests by number
- Winner takes 2x the bet
- Commands:
  - `/coinflip double <currency> <amount> <heads|tails> <player>` - Challenge
  - `/coinflip accept [number]` - Accept request
  - `/coinflip deny [number]` - Deny request
  - `/coinflip requests` - View all requests (GUI)
  - `/coinflip requests list` - List in chat

#### Jackpot
Spin the slots with 3, 4, or 5 rows!

**Multipliers:**
- **3 Rows:** 2 match = 0.8x | 3 match = 2x
- **4 Rows:** 2 match = 0.5x | 3 match = 1x | 4 match = 2x  
- **5 Rows:** 2 match = 0.4x | 3 match = 0.8x | 4 match = 1.5x | 5 match = 3x

**Special Symbols:**
- 7️⃣ 7️⃣ 7️⃣ → 10x-20x (depends on rows)
- 💎 💎 💎 → 7x-15x
- ⭐ ⭐ ⭐ → 5x-10x

**Commands:**
- `/jackpot` - Open jackpot GUI
- `/jackpot <currency> <amount> <rows>` - Quick spin
- `/jackpot help` - Show multipliers

**Supported Currencies:** Money, Mobcoin, Gem (via DZEconomy)

### 3. 🔨 Auction House
Player-to-player item trading with advanced features!

**Auction Types:**
- **Price Reduction:** Price drops over time until sold
- **Bidding Queue:** Players queue up, price increases when queue fills

**Features:**
- Item numbers for easy management
- Freeze/resume auctions
- Auto-expiry after configured days
- Queue refunds on cancellation
- Rank-based listing limits and fees
- Multiple currency support
- Persistent storage with auto-save

**Commands:**
- `/ah` - Browse auctions
- `/ah add` - Create auction (GUI)
- `/ah add-hand-item <currency> <actual> <maxdrop> <drop> <time> <unit> <queue> <increase>` - Quick list
- `/ah list` - List your auctions
- `/ah manage [number]` - Manage auctions
- `/ah remove <number>` - Cancel auction
- `/ah help` - Show help

### 4. 🛒 Item Selling System (NEW!)
**Sell items to shops with multiple methods!**

**Commands:**
- `/sell` - Open area & shop selection GUI
- `/sell <area>` - Open shop selection for area
- `/sell <area> <shop>` - Open sell interface for specific shop
- `/sellall` - Mark all inventory items for sale (requires area & shop args)
- `/sellall <area> <shop>` - Sell all inventory items to shop
- `/sellhand` - Mark hand item for sale (requires area & shop args)
- `/sellhand <area> <shop>` - Sell item in hand to shop

**Features:**
- GUI-based item selection
- Auto-return unsellable items
- Stock tracking (increases shop stock when you sell)
- Support for all shop currencies (Money, MobCoin, Gem)
- Instant payment

### 5. 📦 Kits System
**Starter kits and reward packs!**

**Note:** `/kit` vs `/kits`
- `/kit` - Opens the Kit Area (shops selling starter kits)
- `/kits` - Manages claimable kit packs (different system)

**Kit Packs (Claimable Items):**
- Create custom item packs with cooldowns
- Rank-based permissions
- Price per kit
- Link kits to shops for selling
- Cooldown system

**Commands:**
- `/kit` - Access Kit area shops
- `/kits` - Browse claimable kits
- `/kits claim <name>` - Claim a kit
- `/kits list` - List all kits

**Admin Commands:**
- `/kits create <name>` - Create kit (GUI)
- `/kits create <name> <currency> <price> <cooldown> <item:amount>...` - Quick create
- `/kits delete <name>` - Delete kit
- `/kits link <kit> <area> <shop>` - Link kit to shop
- `/kits unlink <kit>` - Unlink kit

### 6. 🏪 Advanced Marketplace System

**6 Default Areas** (56+ shops, 2000+ items):
- **SuperMarket** - Premium area with checkout system
- **Bazar** - Normal marketplace with reception queue
- **PawnShop** - Sell area linked to SuperMarket
- **Junkyard** - Low-price instant access area
- **BlackMarket** - Exclusive items with both systems
- **Kits** - Starter kit shop

#### Queue Systems
**Reception Queue:**
- Multiple players can shop simultaneously (configurable slots)
- Time limits per player
- AFK detection and auto-kick
- FIFO queue with position tracking

**Checkout Queue:**
- Multiple checkout counters
- Time-based processing (time per item)
- Automatic queue progression

**Note:** Reception and Checkout are mutually exclusive per shop

#### Shop Management
- **Buy/Sell/Both** transaction types
- **Dynamic Pricing** based on supply/demand
- **Automatic Stock Refills** (hourly/daily/weekly/monthly)
- **Shop Linking** for stock transfers
- **Min/Max Price Bounds**
- **Per-item configuration**

#### Admin Commands
**Area Management:**
- `/dzth create-area <name>` - Create marketplace area
- `/dzth delete-area <name>` - Delete area
- `/dzth rename-area <old> <new>` - Rename area

**Shop Management:**
- `/dzth create-shop <area> <shop>` - Create shop
- `/dzth delete-shop <area> <shop>` - Delete shop
- `/dzth rename-shop <area> <old> <new>` - Rename shop
- `/dzth config <area> <shop> <reception|checkout> ...` - Configure queues
- `/dzth link-shop <area> <sell_shop> <buy_shop>` - Link shops

**Item Management:**
- `/dzth add-item <area> <shop>` - Add items (GUI or command)
- `/dzth remove-item <area> <shop> <item>` - Remove item

**List Commands:**
- `/dzth list` - Complete overview
- `/dzth areas list` - List all areas
- `/dzth shops list` - List all shops
- `/dzth items list` - Show item counts

**System:**
- `/dzth migrate <flatfile|mysql|sqlite>` - Migrate storage
- `/dzth reload` - Reload configs
- `/dzth credits` - Show credits

#### Member Commands
- `/tradehub` - Browse all areas (GUI)
- `/<area_name>` - Quick access to specific area (e.g., `/supermarket`)
- `/tradehub help` - Show help

## 📦 Dependencies

**Required:**
- **PaperMC 1.21.1** - Server software
- **DZEconomy Plugin** - Currency system (Money, Mobcoin, Gem)
- **Java 21** - Runtime environment

**Bundled (Shaded):**
- HikariCP 5.1.0 - Connection pooling
- SQLite JDBC 3.45.0.0 - Database driver

## 🛠️ Installation

1. **Install Java 21:**
   ```bash
   # Download and install JDK 21
   cd /tmp
   wget https://download.oracle.com/java/21/latest/jdk-21_linux-aarch64_bin.tar.gz
   tar -xzf jdk-21_linux-aarch64_bin.tar.gz
   mv jdk-21.0.9 /usr/local/jdk-21
   update-alternatives --install /usr/bin/java java /usr/local/jdk-21/bin/java 2111
   update-alternatives --install /usr/bin/javac javac /usr/local/jdk-21/bin/javac 2111
   ```

2. **Install Plugin:**
   - Download `DZTradeHub.jar` from releases
   - Place in `plugins/` folder
   - Ensure DZEconomy is installed
   - Restart server

3. **First Run:**
   - Plugin creates 6 default areas with 56+ shops
   - Database tables auto-created
   - Config files generated in `plugins/DZTradeHub/`

## 🔐 Permissions

```yaml
# Core Permissions
dztradehub.admin - Full admin access (op only)
dztradehub.use - Access marketplace (default: true)
dztradehub.auction - Use auction house (default: true)
dztradehub.casino - Use casino games (default: true)
dztradehub.bounty - Use bounty system (default: true)
dztradehub.bank - Use bank system (default: true)
dztradehub.bank.admin - Admin bank management (default: op)

# Rank Permissions
dztradehub.rank.default - Default rank (default: true)
dztradehub.rank.vip - VIP rank (default: false)
dztradehub.rank.admin - Admin rank (default: op)
```

## ⚙️ Configuration Files

### 📝 Core Configuration
- **config.yml** - Main plugin settings, database configuration
- **ranks.yml** - Rank permissions, limits, multipliers
- **kits.yml** - Starter kit definitions
- **banks.yml** - Global bank settings, currency conversion rates

### 🎮 System Configuration
- **bounty.yml** - Bounty system settings, cleanup intervals
- **cashino.yml** - Casino game settings, multipliers, request expiry
- **auction.yml** - Auction house settings, fees, limits

### 🏦 Bank Configuration
**Individual bank configs in `banks/` folder:**
- **money-bank.yml** - Money-only bank configuration
- **mobcoin-bank.yml** - MobCoin-only bank configuration
- **gem-bank.yml** - Gem-only bank configuration
- **mmo-bank.yml** - Money + MobCoin dual-currency bank
- **mg-bank.yml** - Money + Gem dual-currency bank
- **mog-bank.yml** - MobCoin + Gem dual-currency bank
- **central-bank.yml** - All currencies premium bank

**Each bank config includes:**
- Enabled currencies
- Account types available
- Loan settings (min/max amounts, interest rate)
- Global taxes (deposit/withdrawal percentages)
- Max storage per currency per level
- Level progression costs and benefits
- Reception slots and time limits
- Account creation costs

### 🏪 Dynamic Files (Auto-generated)
```
plugins/DZTradeHub/Areas/
├── SuperMarket/
│   ├── SuperMarket.yml
│   └── Shops/
│       ├── FoodShop/
│       │   ├── foodshop.yml
│       │   └── items.yml
│       └── [more shops...]
└── [more areas...]
```

## 🗄️ Database & Storage

### Storage Types
- **FlatFile (Default)** - YAML-based, no setup needed
- **SQLite** - Embedded database, automatic setup
- **MySQL** - External database server

### Migration
Change storage types without data loss:
```
/dzth migrate <flatfile|mysql|sqlite>
```

### Database Tables
- `bounties` - Active bounties with rewards
- `auctions` - Auction listings with queue data
- `shops` - Shop definitions and settings
- `shop_items` - Items, prices, stock levels
- `transactions` - Purchase/sell history
- `kit_cooldowns` - Kit claim timestamps
- `player_carts` - Shopping cart data
- `banks` - Bank definitions and settings
- `bank_accounts` - Player bank accounts with balances
- `bank_loans` - Active loans with payment tracking
- `bank_transactions` - Deposit, withdrawal, transfer history

### Features
- ✅ **HikariCP Connection Pooling** - Efficient database connections
- ✅ **Async Operations** - Non-blocking I/O with CompletableFuture
- ✅ **Auto-cleanup** - 30-day retention for old records
- ✅ **Auto-save** - Periodic saves (configurable interval)

## 🔧 Building from Source

### Requirements
- Java 21 JDK
- Maven 3.8+

### Build Steps
```bash
# Install Java 21 (if not installed)
apt-get update && apt-get install maven -y

# Install Java 21 (example for Linux ARM64)
cd /tmp
wget https://download.oracle.com/java/21/latest/jdk-21_linux-aarch64_bin.tar.gz
tar -xzf jdk-21_linux-aarch64_bin.tar.gz
mv jdk-21.0.9 /usr/local/jdk-21
export JAVA_HOME=/usr/local/jdk-21

# Clone and build
git clone https://github.com/DemonZDev/DZTradeHub.git
cd DZTradeHub
mvn clean package

# Output: target/DZTradeHub.jar (470KB)
```

### Build Features
- ✅ Maven Shade Plugin - Dependencies bundled
- ✅ HikariCP relocated - Avoids conflicts
- ✅ Java 21 compiler - Modern syntax
- ✅ Resource filtering - Version substitution

## 📚 API Usage

```java
// Get API instance
DZTradeHubAPI api = (DZTradeHubAPI) Bukkit.getServicesManager()
    .getRegistration(DZTradeHubAPI.class)
    .getProvider();

// Bounty System
BountyManager bountyManager = api.getBountyManager();
Bounty bounty = new Bounty(targetUUID, creatorUUID);
bounty.setMoneyReward(1000.0);
bountyManager.addBounty(bounty);

// Casino System
CasinoManager casinoManager = api.getCasinoManager();
casinoManager.performSinglePlayerCoinFlip(player, "MONEY", 100.0, CoinSide.HEAD);
casinoManager.performJackpotSpin(player, "MOBCOIN", 500.0, 5); // 5 rows

// Auction House
AuctionManager auctionManager = api.getAuctionManager();
Auction auction = new Auction(playerUUID, itemStack, "GEM", 1000, 500, 50, 2, 5, 20);
auctionManager.createAuction(auction);

// Marketplace
ShopManager shopManager = api.getShopManager();
List<Area> areas = shopManager.getAllAreas();
Shop shop = shopManager.getShop("Bazar", "FoodShop");
ShopItem item = new ShopItem(itemStack, 100.0, 50.0);
shopManager.addItemToShop("Bazar", "FoodShop", item);

// Queue Management
QueueManager queueManager = api.getQueueManager();
queueManager.joinReceptionQueue(player, shop);
Map<String, Object> stats = queueManager.getQueueStats(shop);

// Stock Management
StockManager stockManager = api.getStockManager();
stockManager.processRestocking(); // Manual restock trigger
stockManager.transferStock("Bazar", sellShop, item, 10); // Transfer stock

// Dynamic Pricing
DynamicPricingManager pricingManager = api.getDynamicPricingManager();
pricingManager.recordPurchase(item, 5);
double demand = pricingManager.getDemandRatio(item); // 0.0 - 1.0

// Bank System
BankManager bankManager = api.getBankManager();
List<Bank> banks = bankManager.getAllBanks();
Bank bank = bankManager.getBankByName("money-bank");

BankAccountManager accountManager = api.getBankAccountManager();
BankAccount account = accountManager.createAccount(playerUUID, playerName, bank, AccountType.SAVINGS, password);
boolean deposited = accountManager.deposit(player, account, CurrencyType.MONEY, 1000.0);
boolean withdrawn = accountManager.withdraw(player, account, CurrencyType.MONEY, 500.0);

BankLoanManager loanManager = api.getBankLoanManager();
BankLoan loan = loanManager.issueLoan(account, CurrencyType.MONEY, 10000.0);
loanManager.processLoanPayment(player, loan);
```

## 🎮 Complete Command Reference

### Bank Commands
```
# Player Commands
/bank - Open bank list GUI
/bank list - List all available banks
/<bank_name> - Access specific bank (e.g., /money-bank, /central-bank)
/<bank_name> create <type> <password> - Create account (SAVINGS, INTEREST, BUSINESS)
/<bank_name> help - Show bank help

# Admin Commands
/bank create <name> - Create new bank
/bank delete <name> - Delete bank
/bank rename <old> <new> - Rename bank
/bank config <name> - Configure bank settings (GUI)
/bank help - Show help
```

### Sell Commands
```
/sell - Open area & shop selection GUI
/sell <area> - Open shop selection for area
/sell <area> <shop> - Open sell interface
/sellall <area> <shop> - Sell all inventory
/sellhand <area> <shop> - Sell item in hand
```

### Kits Commands
```
# Kit Area Access
/kit - Open Kit area shops

# Kit Pack Management
/kits - Browse claimable kits
/kits claim <name> - Claim a kit
/kits list - List all kits

# Admin
/kits create <name> - Create kit (GUI)
/kits create <name> <curr> <price> <cd> <items> - Quick create
/kits delete <name> - Delete kit
/kits link <kit> <area> <shop> - Link to shop
/kits unlink <kit> - Unlink kit
```

### Bounty Commands
```
/bounty - Open bounty GUI
/bounty create <player> - Create bounty
/bounty delete <number> - Delete bounty
/bounty list - List all bounties
/bounty manage [number] - Manage bounties
/bounty help - Show help
```

### Casino Commands
```
# Coin Flip
/coinflip - Open coin flip GUI
/coinflip single - Open single mode
/coinflip single <currency> <amount> <heads|tails> - Quick flip
/coinflip double - Open challenge mode
/coinflip double <currency> <amount> <heads|tails> <player> - Challenge
/coinflip accept [number] - Accept request
/coinflip deny [number] - Deny request
/coinflip requests - View requests (GUI)
/coinflip requests list - List requests
/coinflip help - Show help

# Jackpot
/jackpot - Open jackpot GUI
/jackpot <currency> <amount> <rows> - Spin (3-5 rows)
/jackpot help - Show multipliers
```

### Auction House Commands
```
/ah - Browse auctions
/ah add - Create auction (GUI)
/ah add-hand-item <currency> <actual> <maxdrop> <drop> <time> <unit> <queue> <increase>
/ah list - List your auctions
/ah manage [number] - Manage auctions
/ah remove <number> - Cancel auction
/ah help - Show help
```

### Marketplace Commands (Admin)
```
# Area Management
/dzth create-area <name> - Create area
/dzth delete-area <name> - Delete area
/dzth rename-area <old> <new> - Rename area

# Shop Management
/dzth create-shop <area> <shop> - Create shop
/dzth delete-shop <area> <shop> - Delete shop
/dzth rename-shop <area> <old> <new> - Rename shop
/dzth config <area> <shop> ... - Configure queue system
/dzth link-shop <area> <sell> <buy> - Link shops
/dzth unlink-shop <area> <shop> - Unlink shop

# Item Management
/dzth add-item <area> <shop> - Add items (GUI)
/dzth add-item <area> <shop> <item> <currency> <min> <max> <buy|sell|both>
/dzth remove-item <area> <shop> <item> - Remove item

# Listing
/dzth list - Complete overview
/dzth areas list - List areas
/dzth shops list - List all shops
/dzth <area> shops list - List shops in area
/dzth items list - Show item counts

# System
/dzth migrate <type> - Migrate storage
/dzth reload - Reload configs
/dzth credits - Show credits
/dzth help - Show help
```

### Marketplace Commands (Members)
```
/tradehub - Browse areas (GUI)
/<area_name> - Quick access (e.g., /supermarket, /bazar)
/tradehub help - Show help
```

## 🎯 Default Areas & Shops

### Included on First Run (56+ Shops, 2000+ Items)

**1. SuperMarket** (Premium with Checkout)
- 12+ shops: Food, Building, Tools, Farming, Decoration, Combat, Mining, Enchanting, Potions, Redstone, Transport, Music
- 40-50 items per shop
- Buy only, checkout system

**2. Bazar** (Normal with Reception)
- 10+ shops: General Store, Clothing, Jewelry, Meat Market, Vegetables, Flowers, Pet Supplies, Books, Toys, Electronics
- 35-40 items per shop
- Buy & sell, reception queue

**3. PawnShop** (Sell Area)
- 8+ shops: Scrap Metals, Old Tools, Used Gear, Antiques, Collectibles, Relics, Salvage, Broken Items
- 40+ items per shop
- Sell only, linked to SuperMarket

**4. Junkyard** (Low Price)
- 8+ shops: Junk Deals, Cheap Tools, Scrap Heap, Recycling, Disposal, Waste Management, Discount Bin, Clearance
- 35+ items per shop
- Buy & sell, instant access

**5. BlackMarket** (Exclusive)
- 10+ shops: Illegal Weapons, Rare Items, Contraband, Stolen Goods, Forbidden Magic, Cursed Artifacts, Exotic Creatures, Blackmail, Counterfeits, Smuggled Wares
- 30-40 items per shop
- Buy & sell, both queue systems

**6. Kits** (Starter Kits)
- 8+ shops: Starter, Basic, Survival, Mining, Farming, Combat, Builder, Explorer
- 20-30 items per shop
- Sell (one-time purchase)

## ⚡ Performance Features

- **Async Database Operations** - Non-blocking I/O
- **Connection Pooling** - HikariCP for efficiency
- **Thread Safety** - ConcurrentHashMap for caching
- **Scheduled Tasks** - Batch processing for pricing, stock, cleanup
- **Efficient Caching** - In-memory maps for quick access
- **Lazy Loading** - Areas loaded on demand

## 🔒 Security Features

- Permission checks on all commands
- Input validation and sanitization
- SQL injection prevention (prepared statements)
- Admin-only commands protected
- Player data privacy (bounty rewards hidden)
- Balance verification before transactions

## 📊 Statistics & Monitoring

- Transaction history tracking
- Queue statistics per shop
- Demand ratio calculations for pricing
- Stock level monitoring
- Auction activity logs
- Bounty claim records

## 🤝 Contributing

Contributions welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Follow code style (Java 21, modern patterns)
4. Test thoroughly
5. Submit pull request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Developer

**DemonZDev**
- GitHub: [@DemonZDev](https://github.com/DemonZDev)
- Repository: [DZTradeHub](https://github.com/DemonZDev/DZTradeHub)
- Website: [demonzdevelopment.online](https://demonzdevelopment.online)

## 🐛 Bug Reports & Support

**Found a bug?** Open an [issue](https://github.com/DemonZDev/DZTradeHub/issues) with:
- Server version (PaperMC 1.21.1)
- Plugin version
- DZEconomy version
- Steps to reproduce
- Error logs from console
- Expected vs actual behavior

**Need help?**
- Check [VERIFICATION_REPORT.md](VERIFICATION_REPORT.md) for detailed feature documentation
- Open an [issue](https://github.com/DemonZDev/DZTradeHub/issues) for questions
- Check the [wiki](https://github.com/DemonZDev/DZTradeHub/wiki) (if available)

## 📋 System Requirements

- **Server:** PaperMC 1.21.1 or higher
- **Java:** Java 21 or higher
- **RAM:** Minimum 2GB allocated to server
- **Dependencies:** DZEconomy plugin (required)
- **Storage:** 50MB+ for plugin and data

## 🎓 Key Technologies

- **Java 21** - Modern language features (records, switch expressions, pattern matching)
- **PaperMC API 1.21.1** - Latest Minecraft server API
- **HikariCP 5.1.0** - High-performance connection pool
- **SQLite 3.45** - Embedded database
- **MySQL 8.0** - External database support
- **Maven** - Build and dependency management

---

## 🚀 Quick Start

```bash
# 1. Install Java 21 and DZEconomy

# 2. Download and install
wget [plugin-url]/DZTradeHub.jar
mv DZTradeHub.jar /path/to/server/plugins/

# 3. Restart server
# Plugin creates default areas automatically

# 4. Configure (optional)
nano plugins/DZTradeHub/config.yml

# 5. Reload
/dzth reload
```

---

## 🛠️ New Utility Systems (v1.1.0)

### Security & Audit Tools

#### SecureErrorHandler
```java
// Prevents information disclosure through error messages
SecureErrorHandler.handleError(plugin, player, "Transaction failed", exception);
```

#### AuditLogger
```java
// Logs all admin actions with full accountability
AuditLogger.logAdminAction(plugin, sender, "BANK_DELETE", bankName, "Deleted via command");
```

#### SecurityLogger
```java
// Dedicated security event logging
SecurityLogger.logFailedLogin(plugin, accountId, playerName, attemptNumber);
SecurityLogger.logAccountLockout(plugin, accountId, reason);
```

### Password & Authentication

#### PasswordStrength
```java
// Evaluate password quality
PasswordStrength.Strength strength = PasswordStrength.evaluate(password);
player.sendMessage(strength.getDisplay() + " - " + strength.getDescription());
```

### Data Management

#### TransactionExport
```java
// Export transaction history to CSV
File export = TransactionExport.exportTransactions(plugin, player, account);
```

#### BackupManager
```java
// Create automated backups
BackupManager.backupBeforeCriticalOp(plugin, "account_deletion");
```

### Performance & Protection

#### CommandRateLimiter
```java
// Prevent command spam
CommandRateLimiter rateLimiter = new CommandRateLimiter(1000); // 1 second cooldown
if (!rateLimiter.canExecute(player, "bank")) {
    player.sendMessage("§cPlease wait before using this command again!");
    return;
}
```

#### Database Health Checks
- Automatic monitoring every 5 minutes
- Auto-reconnect on connection loss
- Zero downtime for players

---

**⚠️ Important Note**: This plugin requires **DZEconomy** for currency management (Money, Mobcoin, Gem). It is **not compatible with Vault**.

**✅ Production Ready** - Fully tested and verified for PaperMC 1.21.1 servers!

---

## 📈 Version History

- **v1.1.0** - Security & Quality of Life Update (14 fixes + improvements)
- **v1.0.1** - Critical Security Fixes (PBKDF2, Brute-Force Protection, Thread Safety)
- **v1.0.0** - Initial Release
