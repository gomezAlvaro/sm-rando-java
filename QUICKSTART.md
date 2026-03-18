# Quick Reference Guide

## Common Tasks

### Working with Items

```java
// Collect an item
ItemCollector.collectItem(gameState, Item.MORPH_BALL);

// Check if player has an item
boolean hasMorph = RequirementChecker.canMorph(gameState);

// Check if player can place bombs
boolean canBomb = RequirementChecker.canPlaceBombs(gameState);

// Check if player has ice beam
boolean hasIce = RequirementChecker.hasIceBeam(gameState);
```

### Working with Resources

```java
// Get current resource level
ResourceLevel energy = state.getResourceLevel(ResourceType.ENERGY);
System.out.println("Energy: " + energy.getRemaining() + "/" + energy.maxCapacity());

// Check if player has enough of a resource
boolean canShoot = ResourceManager.hasResource(state, ResourceType.MISSILE, 5);

// Consume a resource
ResourceManager.consumeResource(state, ResourceType.MISSILE, 5);

// Check if critically low on resources
boolean critical = ResourceManager.isCriticallyLow(state, ResourceType.ENERGY);
```

### Working with Game State

```java
// Create a new game state
GameState state = new GameState();

// Create a game state with starting items
GameState state = GameState.standardStart(); // Has morph ball + charge beam

// Create a game state with specific items
GameState state = GameState.withItems(Item.MORPH_BALL, Item.BOMB, Item.ICE_BEAM);

// Clone a state (for simulation)
GameState clonedState = state.clone();

// Get/set energy
state.setEnergy(100);
state.addEnergy(50);
boolean survived = state.takeDamage(30);

// Get inventory
Inventory inv = state.getInventory();
int itemCount = inv.getItemCount();
```

### Damage Calculations

```java
// Calculate shot damage
int damage = DamageCalculator.calculateShotDamage(gameState);

// Calculate missile damage
int missileDamage = DamageCalculator.calculateMissileDamage(gameState);

// Calculate damage taken from enemy
int baseDamage = 30;
int actualDamage = DamageCalculator.calculateDamageTaken(gameState, baseDamage);

// Check if player would survive
boolean survives = DamageCalculator.wouldSurvive(gameState, 50);

// Get damage reduction percentage
double reduction = DamageCalculator.getDamageReduction(gameState); // 0.0 to 1.0
```

### Randomization

```java
// Create a randomizer with a seed
BasicRandomizer randomizer = new BasicRandomizer("my-seed-123");

// Or use a random seed
BasicRandomizer randomizer = new BasicRandomizer(null); // Auto-generates seed

// Add locations
Location loc = Location.builder()
    .id("brinstar_01")
    .name("Morph Ball Room")
    .region("Brinstar")
    .requirements(Set.of())
    .build();
randomizer.addLocation(loc);

// Set item pool
randomizer.setItemPool(ItemPool.createStandardPool());
// Or use minimal pool for testing
randomizer.setItemPool(ItemPool.createMinimalPool());

// Run randomization
RandomizationResult result = randomizer.randomize();

// Check result
if (result.isSuccessful()) {
    System.out.println("Randomization successful!");
    System.out.println("Placed " + result.getPlacementCount() + " items");
}

// Print spoiler log
System.out.println(result.generateSpoilerLog());

// Or use formatted spoiler
PrintableSpoiler.printSpoiler(result);
```

### Creating Locations

```java
// Simple location (no requirements)
Location loc1 = Location.builder()
    .id("loc_01")
    .name("Item Room 1")
    .region("Brinstar")
    .build();

// Location with requirements
Location loc2 = Location.builder()
    .id("loc_02")
    .name("Behind Bomb Wall")
    .region("Norfair")
    .requirements(Set.of("can_morph", "has_bombs"))
    .build();

// Check location properties
String id = loc2.getId();
String name = loc2.getName();
String region = loc2.getRegion();
boolean hasReqs = loc2.hasRequirements();
Set<String> reqs = loc2.getRequirements();

// Place an item
loc2.placeItem(Item.ICE_BEAM);
Item placed = loc2.getPlacedItem();
boolean isPlaced = loc2.isPlaced();
```

### Working with Item Pools

```java
// Create standard pool
ItemPool pool = ItemPool.createStandardPool();

// Create minimal pool (for testing)
ItemPool pool = ItemPool.createMinimalPool();

// Or create custom pool
ItemPool pool = new ItemPool();
pool.addItem(Item.MORPH_BALL, 1, true);  // progression item
pool.addItem(Item.ENERGY_TANK, 5, false); // filler item

// Check pool contents
int totalItems = pool.getTotalItemCount();
int progressionItems = pool.getProgressionItemCount();
int fillerItems = pool.getFillerItemCount();

// Pick random items
Item progressionItem = pool.pickRandomProgressionItem(random);
Item fillerItem = pool.pickRandomFillerItem(random);
Item anyItem = pool.pickRandomItem(random);

// Check pool status
boolean isEmpty = pool.isEmpty();
```

## Item Categories

### Beams
- `CHARGE_BEAM` - Allows charge shots
- `ICE_BEAM` - Freezes enemies
- `WAVE_BEAM` - Shoots through walls
- `SPAZER_BEAM` - Three-way shot
- `PLASMA_BEAM` - Pierces enemies

### Morph Ball Abilities
- `MORPH_BALL` - Roll into a ball
- `BOMB` - Morph ball bombs
- `SPRING_BALL` - Jump in morph ball
- `POWER_BOMB` - Large morph ball explosions

### Movement
- `HI_JUMP_BOOTS` - Higher jumps
- `SPEED_BOOSTER` - Running speed boost
- `SPACE_JUMP` - Infinite mid-air jumps
- `SCREW_ATTACK` - Jumping damages enemies

### Suits
- `VARIA_SUIT` - Reduces damage taken (50%)
- `GRAVITY_SUIT` - Lava protection + damage reduction (75%)

### Keys
- `BRINSTAR_KEY` - Access Brinstar boss
- `NORFAIR_KEY` - Access Norfair boss
- `MARIDIA_KEY` - Access Maridia boss
- `LOWER_NORFAIR_KEY` - Access Lower Norfair
- `WRECKED_SHIP_KEY` - Access Wrecked Ship boss

### Tanks
- `ENERGY_TANK` - +100 energy capacity
- `MISSILE_TANK` - +5 missile capacity
- `SUPER_MISSILE_TANK` - +5 super missile capacity
- `POWER_BOMB_TANK` - +5 power bomb capacity

### Special
- `XRAY_SCOPE` - See hidden blocks
- `GRAVITY_BOSS` - Gravity Suit boss token
- `RIDLEY` - Ridley boss token

## Resource Types

- `ENERGY` - Health points (100-299)
- `MISSILE` - Missile ammo (0-250)
- `SUPER_MISSILE` - Super missile ammo (0-50)
- `POWER_BOMB` - Power bomb ammo (0-50)

## Common Patterns

### Checking Requirements

```java
// Single requirement
if (RequirementChecker.canMorph(state)) {
    // Player can morph
}

// Multiple requirements (all must be satisfied)
if (RequirementChecker.meetsRequirements(state,
    RequirementChecker.Requirements.canMorph(),
    RequirementChecker.Requirements.canPlaceBombs(),
    RequirementChecker.Requirements.hasIceBeam())) {
    // All requirements met
}

// Custom requirement
Requirement customReq = s -> s.getEnergy() > 50;
if (customReq.isSatisfied(state)) {
    // Custom requirement met
}
```

### State Simulation

```java
// Save current state
GameState originalState = state.clone();

// Try something
state.takeDamage(100);
if (!canSurviveDamage(state, 50)) {
    // Restore original state
    state = originalState;
}
```

### Batch Item Collection

```java
// Collect multiple items at once
List<Item> items = List.of(
    Item.MORPH_BALL,
    Item.BOMB,
    Item.ICE_BEAM
);
ItemCollector.collectAll(state, items);
```

## Tips

1. **Always clone state before simulation** - Game state is mutable
2. **Use RequirementChecker for access checks** - Centralized logic
3. **Check resource availability before consuming** - ResourceManager methods
4. **Use standard pools for consistency** - createStandardPool() or createMinimalPool()
5. **Verify randomization results** - Use verifyCompletable() method
6. **Print spoiler logs for debugging** - result.generateSpoilerLog()

## Running Tests

```bash
# Compile
mvn clean compile

# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ItemCollectorTest

# Run with coverage
mvn clean test jacoco:report
```

## Demo Programs

### SimpleDemo
Main demonstration showing all features:
```bash
mvn exec:java -Dexec.mainClass="com.maprando.demo.SimpleDemo"
```

### Creating Your Own Demo

```java
public class MyDemo {
    public static void main(String[] args) {
        // Create game state
        GameState state = GameState.standardStart();

        // Create randomizer
        BasicRandomizer randomizer = new BasicRandomizer("my-seed");
        randomizer.setItemPool(ItemPool.createMinimalPool());

        // Add locations
        // ... add locations

        // Run randomization
        RandomizationResult result = randomizer.randomize();

        // Print result
        PrintableSpoiler.printSpoiler(result);
    }
}
```
