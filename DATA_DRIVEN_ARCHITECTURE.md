# Data-Driven Architecture Foundation

## Overview

This document describes the data-driven architecture that aligns with the original Rust MapRandomizer project. The goal is to move from an enum-based system to a fully data-driven system.

## Current State (Enum-Based)

### Item Enum System
```java
public enum Item {
    CHARGE_BEAM, ICE_BEAM, WAVE_BEAM, ...,
    MISSILE_TANK, ENERGY_TANK, ...
}
```

### Inventory with EnumSet
```java
public class Inventory {
    private final EnumSet<Item> collectedItems;
    public boolean hasItem(Item item) {
        return collectedItems.contains(item);
    }
}
```

### Pros
- Type-safe
- Easy to use
- Good IDE support
- Works well with switch statements

### Cons
- Not data-driven (items are hardcoded)
- Can't add items without recompiling
- Doesn't align with original Rust architecture
- Requires code changes for new items

## Target State (Data-Driven)

### ItemDefinition Class
```java
public class ItemDefinition {
    private final String id;
    private final String displayName;
    private final String description;
    private final String category;
    private final boolean isProgression;
    private final int index; // For boolean array tracking
}
```

### ItemRegistry
```java
public class ItemRegistry {
    private final Map<String, ItemDefinition> itemsById;
    private final List<ItemDefinition> itemsByIndex;

    public void registerItem(ItemDefinition definition);
    public ItemDefinition getById(String id);
    public ItemDefinition getByIndex(int index);
    public boolean[] createInventoryArray();
}
```

### DataDrivenInventory with Boolean Arrays
```java
public class DataDrivenInventory {
    private final boolean[] collectedItems;
    private final ItemRegistry itemRegistry;

    public boolean hasItem(String itemId) {
        return itemRegistry.hasItemById(collectedItems, itemId);
    }
}
```

### Pros
- Fully data-driven (items from JSON)
- Can add items without recompiling
- Aligns with original Rust architecture
- More flexible for modding
- Boolean arrays are memory-efficient

### Cons
- Loses compile-time type safety
- More error-prone (typos in IDs)
- Requires runtime validation
- More complex setup

## Migration Strategy

### Phase 1: Foundation (Complete ✅)
- [x] Create ItemDefinition class
- [x] Create ItemRegistry class
- [x] Create DataDrivenInventory class
- [x] Update items.json with index field
- [x] Update ItemData model with index field
- [x] Update DataLoader to populate ItemRegistry

### Phase 2: Parallel Operation (Future)
- Keep both enum-based and data-driven systems working
- Use enum-based for existing code
- Use data-driven for new features
- Create adapters for interoperability

### Phase 3: Gradual Migration (Future)
Migrate components in order of dependency:

1. **Model Layer**
   - [ ] Update GameState to use DataDrivenInventory
   - [ ] Keep Item enum for backward compatibility
   - [ ] Create conversion methods between systems

2. **Logic Layer**
   - [ ] Update ItemCollector to work with item IDs
   - [ ] Update RequirementChecker for data-driven items
   - [ ] Add runtime validation

3. **Randomization Layer**
   - [ ] Update ItemPool to use item IDs
   - [ ] Update Location to use item IDs
   - [ ] Update RandomizationResult to use item IDs

4. **Traversal Layer**
   - [ ] Update TraversalState to use DataDrivenInventory
   - [ ] Update ReachabilityAnalysis for data-driven items
   - [ ] Update SeedVerifier for data-driven items

5. **Tests**
   - [ ] Update all tests to use data-driven system
   - [ ] Add validation tests for JSON data
   - [ ] Add conversion tests between systems

### Phase 4: Cleanup (Future)
- [ ] Remove Item enum
- [ ] Remove old Inventory class
- [ ] Update all documentation
- [ ] Update CLAUDE.md

## Original Rust Architecture

### Inventory Storage (Rust)
```rust
pub struct Inventory {
    pub items: Vec<bool>,  // Boolean array for item tracking
    pub max_energy: Capacity,
    pub max_missiles: Capacity,
    pub max_supers: Capacity,
    pub max_power_bombs: Capacity,
    pub max_reserves: Capacity,
}
```

### Item Access (Rust)
```rust
// Check if player has morph ball
if inventory.items[Item::Morph as usize] {
    // Player can morph
}

// Count collected items
let collected = inventory.items.iter().filter(|&&x| x).count();
```

### Tech System (Rust)
The original Rust uses a complex tech system with IDs like:
- `TECH_ID_CAN_MORPH`
- `TECH_ID_CAN_GRAPPLE`
- `TECH_ID_CAN_SHINESPARK`
- `TECH_ID_CAN_WALLJUMP`
- `TECH_ID_CAN_MOONFALL`
- `TECH_ID_CAN_XRAYCRAWL`
- And many more...

These techs are loaded from data and checked dynamically, not hardcoded.

## Data Files

### items.json Structure
```json
{
  "items": [
    {
      "id": "CHARGE_BEAM",
      "displayName": "Charge Beam",
      "description": "Allows charge shots",
      "category": "beam",
      "isProgression": true,
      "index": 0,
      "damageMultiplier": 3.0
    }
  ]
}
```

### Future: tech.json
```json
{
  "techs": [
    {
      "id": "can_morph",
      "name": "Can Morph",
      "description": "Can roll into morph ball",
      "index": 0
    },
    {
      "id": "can_shinespark",
      "name": "Can Shinespark",
      "description": "Can execute shinespark",
      "index": 1,
      "requires": ["speed_booster"]
    }
  ]
}
```

## Usage Examples

### Enum-Based (Current)
```java
// Create inventory
Inventory inv = new Inventory();

// Add item
inv.addItem(Item.MORPH_BALL);

// Check item
if (inv.hasItem(Item.MORPH_BALL)) {
    // Can morph
}
```

### Data-Driven (Target)
```java
// Load data
DataLoader loader = new DataLoader();
loader.loadAllData();
ItemRegistry registry = loader.getItemRegistry();

// Create inventory
DataDrivenInventory inv = new DataDrivenInventory(registry);

// Add item
inv.addItem("MORPH_BALL");

// Check item
if (inv.hasItem("MORPH_BALL")) {
    // Can morph
}
```

## Recommendations

### For New Code
- Use DataDrivenInventory for new features
- Use ItemRegistry for item lookups
- Add items to items.json, not as enum values

### For Existing Code
- Keep using enum-based system for now
- Migrate when making significant changes
- Don't break working tests

### For Data Changes
- Edit JSON files directly
- No recompilation needed
- Add new items with indices
- Update documentation

## Implementation Notes

### Index Assignment
- Each item must have a unique index
- Indices must be sequential (0, 1, 2, ...)
- No gaps in indices
- Used for boolean array addressing

### Validation
- DataLoader validates JSON on load
- Checks for duplicate IDs
- Checks for duplicate indices
- Validates references (requirements, enables)

### Performance
- Boolean arrays are very memory-efficient
- Index-based access is O(1)
- No boxing/unboxing overhead
- Better cache locality than EnumSet

## Conclusion

The data-driven architecture provides:
1. Alignment with original Rust MapRandomizer
2. Flexibility for adding items without recompilation
3. Better support for modding and customization
4. More maintainable long-term

The enum-based system remains useful for:
1. Type safety during development
2. Compile-time error detection
3. IDE auto-completion
4. Existing working code

Both systems can coexist during the migration period.
