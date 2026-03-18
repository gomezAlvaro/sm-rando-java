# Rust vs Java Implementation Differences

## Overview
This document outlines the key differences between the original Rust MapRandomizer implementation and the Java proof-of-concept port.

## Critical Differences

### 1. Item Enum Structure

**Rust (25 items, indices 0-24):**
```rust
pub enum Item {
    ETank, Missile, Super, PowerBomb, Bombs, Charge, Ice, HiJump,
    SpeedBooster, Wave, Spazer, SpringBall, Varia, Gravity, XRayScope,
    Plasma, Grapple, SpaceJump, ScrewAttack, Morph, ReserveTank,
    WallJump, Nothing, SparkBooster, BlueBooster,
}
```

**Java (27 items):**
- Different structure with separate categories
- Missing: ReserveTank, WallJump, Nothing, SparkBooster, BlueBooster
- Added: Boss tokens (GRAVITY_BOSS, RIDLEY), Area keys
- Different organizational approach

**Impact**: Medium - The Java version has different items but follows the same conceptual model.

### 2. ResourceLevel Representation

**Rust (Enum with variants):**
```rust
pub enum ResourceLevel {
    Consumed(Capacity),  // Tracks amount consumed
    Remaining(Capacity), // Tracks amount remaining
}
```

**Java (Record class):**
```java
public record ResourceLevel(ResourceType type, int maxCapacity, int consumed)
```

**Impact**: Low - The Java version tracks both states simultaneously, which is actually more convenient for POC purposes.

### 3. Starting Energy

**Rust:**
```rust
energy: ResourceLevel::Remaining(1)  // Starts with 1 energy for traversal
```

**Java:**
```java
this.energy = ResourceType.ENERGY.getBaseAmount(); // 100
```

**Impact**: Low - The Rust version starts with minimal energy for graph traversal calculations, while Java starts with full base energy for gameplay simulation. Both are valid for their use cases.

### 4. State Management Architecture

**Rust (Separate structures):**
- `GlobalState` - Persistent inventory, flags, weapon mask
- `LocalState` - Traversal state (energy, ammo, shinecharge, etc.)

**Java (Combined structure):**
- `GameState` - Combines inventory, resources, and position

**Impact**: Low - The Java version simplifies the architecture for POC purposes.

### 5. Item Collection Logic

**Rust (With ammo fraction):**
```rust
Item::Missile => {
    self.inventory.collectible_missile_packs += 1;
    let new_max_missiles = (ammo_collect_fraction
        * self.inventory.collectible_missile_packs as f32)
        .round() as Capacity * 5;
    self.inventory.max_missiles = new_max_missiles;
}
```

**Java (Direct increment):**
```java
case MISSILE_TANK -> inventory.increaseResourceCapacity(ResourceType.MISSILE, 5);
```

**Impact**: Medium - The Rust version supports partial ammo collection for difficulty settings, while Java always grants full capacity.

### 6. Consistent Logic (✅ Match)

**Energy Tanks:**
- Both add +100 to max energy ✅

**Ammo Packs:**
- Both add +5 to capacity ✅

**Item Uniqueness:**
- Both handle unique items vs collectible tanks ✅

## Recommendations

### For POC Consistency:
1. ✅ **Keep current Java approach** - The simplifications are appropriate for a learning POC
2. ✅ **Document differences** - This file serves that purpose
3. ✅ **Maintain core logic** - Energy (+100) and ammo (+5) increments match

### For Future Full Implementation:
1. Add missing Rust items (ReserveTank, WallJump, etc.)
2. Implement ammo_collect_fraction for difficulty settings
3. Separate GlobalState and LocalState for proper traversal
4. Consider using enum for ResourceLevel to match Rust exactly
5. Implement graph traversal for reachability analysis

## Conclusion

The Java implementation successfully captures the **core concepts** of the Rust MapRandomizer while making appropriate simplifications for a proof-of-concept learning project. The fundamental logic (item collection, resource management, randomization) is consistent with the original design.

**Status**: ✅ Core logic is consistent - acceptable differences for POC purposes
