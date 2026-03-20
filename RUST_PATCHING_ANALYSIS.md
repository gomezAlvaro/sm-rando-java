# Rust Project ROM Patching Analysis

## Executive Summary

The Rust project (`MapRandomizer`) uses a significantly more sophisticated ROM patching system than the current Java POC implementation. This document outlines the key differences and provides a roadmap for aligning the Java implementation with the Rust approach.

---

## Key Architecture Differences

### Current Java POC Approach
- Uses placeholder single-byte item values (0x01-0x12 for major items, 0xE5-0xE8 for tanks)
- Writes directly to hardcoded ROM addresses per location
- Does not account for different item container types
- Simple but inaccurate

### Rust Production Approach
- Uses PLM (Programmable Logic Module) type values
- PLM values range from 0xEED7 to 0xF0EE
- Different PLM values for different container types (pedestal, Chozo orb, hidden shot block)
- Uses node pointer maps for location addressing
- More complex but accurate

---

## Item to PLM Type Mapping

The Rust project defines a complete PLM type table that maps items to their ROM values based on container type:

### Container Types
- **0 = None/Pedestal**: Item appears on a pedestal or in plain sight
- **1 = Chozo Orb**: Item inside a glowing Chozo ball
- **2 = Hidden Shot Block**: Item inside a breakable block (must shoot to reveal)

### PLM Value Tables

#### Container 0: None/Pedestal
```
0xEED7 - Energy tank
0xEEDB - Missile tank
0xEEDF - Super missile tank
0xEEE3 - Power bomb tank
0xEEE7 - Bombs
0xEEEB - Charge beam
0xEEEF - Ice beam
0xEEF3 - Hi-jump
0xEEF7 - Speed booster
0xEEFB - Wave beam
0xEEFF - Spazer beam
0xEF03 - Spring ball
0xEF07 - Varia suit
0xEF0B - Gravity suit
0xEF0F - X-ray scope
0xEF13 - Plasma beam
0xEF17 - Grapple beam
0xEF1B - Space jump
0xEF1F - Screw attack
0xEF23 - Morph ball
0xEF27 - Reserve tank
0xF000 - Wall-jump boots
0xEEDB - Missile tank (nothing)
0xF0E2 - Spark Booster
0xF0EE - Blue Booster
```

#### Container 1: Chozo Orb
```
0xEF2B - Energy tank, chozo orb
0xEF2F - Missile tank, chozo orb
0xEF33 - Super missile tank, chozo orb
0xEF37 - Power bomb tank, chozo orb
0xEF3B - Bombs, chozo orb
0xEF3F - Charge beam, chozo orb
0xEF43 - Ice beam, chozo orb
0xEF47 - Hi-jump, chozo orb
0xEF4B - Speed booster, chozo orb
0xEF4F - Wave beam, chozo orb
0xEF53 - Spazer beam, chozo orb
0xEF57 - Spring ball, chozo orb
0xEF5B - Varia suit, chozo orb
0xEF5F - Gravity suit, chozo orb
0xEF63 - X-ray scope, chozo orb
0xEF67 - Plasma beam, chozo orb
0xEF6B - Grapple beam, chozo orb
0xEF6F - Space jump, chozo orb
0xEF73 - Screw attack, chozo orb
0xEF77 - Morph ball, chozo orb
0xEF7B - Reserve tank, chozo orb
0xF004 - Wall-jump boots, chozo orb
... (continues for all 25 items)
```

#### Container 2: Hidden Shot Block
```
0xEF7F - Energy tank, hidden block
0xEF83 - Missile tank, hidden block
... (continues for all 25 items)
```

---

## Item Placement Algorithm

### Rust Algorithm
```rust
fn place_items(&mut self) -> Result<()> {
    for (&item, &loc) in iter::zip(
        &self.randomization.item_placement,
        &self.game_data.item_locations,
    ) {
        // 1. Get the PLM pointer for this location
        let item_plm_ptr = self.game_data.node_ptr_map[&loc];

        // 2. Read the original PLM type from base ROM
        let orig_plm_type = self.orig_rom.read_u16(item_plm_ptr)?;

        // 3. Calculate new PLM type based on item and container
        let new_plm_type = item_to_plm_type(item, orig_plm_type);

        // 4. Write new PLM type to ROM
        self.rom.write_u16(item_plm_ptr, new_plm_type)?;

        // 5. Handle "Nothing" items specially
        if item == Item::Nothing {
            let idx = self.rom.read_u16(item_plm_ptr + 4).unwrap() as usize;
            self.nothing_item_bitmask[idx >> 3] |= 1 << (idx & 7);
        }
    }
    Ok(())
}
```

### Container Detection
```rust
// Container type is derived from the original PLM type
let item_container = (orig_plm_type - 0xEED7) / 84;
// Result: 0 = none, 1 = chozo orb, 2 = hidden shot block
```

### PLM Type Selection
```rust
fn item_to_plm_type(item: Item, orig_plm_type: isize) -> isize {
    let item_id = item as isize;
    let item_container = (orig_plm_type - 0xEED7) / 84;

    // Look up PLM value from 3x25 table [container][item]
    let plm_table: [[isize; 25]; 3] = [...];

    plm_table[item_container][item_id]
}
```

---

## Location Addressing

### Rust Approach: Node Pointer Map
- Uses `(room_id, node_id)` tuples as location identifiers
- Maintains `node_ptr_map: HashMap<(usize, usize), usize>` to map locations to PLM pointers
- PLM pointers are ROM addresses where item PLM definitions are stored
- Each room has multiple item nodes with unique PLM pointers

### Example Flow
```
Location: (room_id=5, node_id=2)
↓
node_ptr_map.get(&(5, 2)) → 0x848200 (PLM pointer address)
↓
ROM[0x848200] = 0xEED7 (original PLM type for Energy Tank pedestal)
↓
item_to_plm_type(Item::Grapple, 0xEED7) → 0xEF17 (Grapple pedestal)
↓
ROM[0x848200] = 0xEF17 (write new PLM type)
```

---

## Seed Metadata Storage

### Rust Seed Metadata Locations
```
0xFFC0     - ROM header ("SUPERMETROID MAPRANDO")
0xDFFEF0   - Seed name (16 bytes, null-terminated string)
0xDFFF00   - Display seed (4 bytes, u32 little-endian)
```

### Seed Writing Code
```rust
fn apply_seed_identifiers(&mut self) -> Result<()> {
    // Update ROM header
    let cartridge_name = "SUPERMETROID MAPRANDO";
    self.rom.write_n(0x7FC0, cartridge_name.as_bytes())?;

    // Write seed name (URL-safe, used for website lookup)
    assert!(self.randomization.seed_name.len() < 16);
    self.rom.write_n(snes2pc(0xdffef0), &[0; 16])?;
    self.rom.write_n(snes2pc(0xdffef0), self.randomization.seed_name.as_bytes())?;

    // Write display seed (used by enemy name display ASM)
    let seed_bytes = (self.randomization.display_seed as u32).to_le_bytes();
    self.rom.write_n(snes2pc(0xdfff00), &seed_bytes)?;

    Ok(())
}
```

### Current Java POC Locations
```
0x82FF00   - Seed ID (32 bytes, null-terminated)
0x82FF20   - Timestamp (7 bytes)
0x82FF27   - Algorithm (32 bytes)
```

---

## Item Enum Definition

### Rust Item Enum (25 items)
```rust
#[repr(usize)]
pub enum Item {
    ETank,        // 0
    Missile,      // 1
    Super,        // 2
    PowerBomb,    // 3
    Bombs,        // 4
    Charge,       // 5
    Ice,          // 6
    HiJump,       // 7
    SpeedBooster, // 8
    Wave,         // 9
    Spazer,       // 10
    SpringBall,   // 11
    Varia,        // 12
    Gravity,      // 13
    XRay,         // 14
    Plasma,       // 15
    Grapple,      // 16
    SpaceJump,    // 17
    ScrewAttack,  // 18
    Morph,        // 19
    Reserve,      // 20
    WallJumpBoots, // 21
    Nothing,      // 22
    SparkBooster, // 23
    BlueBooster,  // 24
}
```

---

## Required Changes for Java Implementation

### 1. Update ItemPatcher.java
- [ ] Replace single-byte item values with 16-bit PLM type values
- [ ] Implement container type detection from original PLM
- [ ] Add complete PLM type table (3 containers × 25 items = 75 values)
- [ ] Update `getItemByteValue()` to return `int` instead of `byte`
- [ ] Update ROM writing to use `writeU16()` instead of `writeU8()`

### 2. Update Location Addressing
- [ ] Replace hardcoded ROM addresses with node pointer system
- [ ] Add `node_ptr_map` to map (room_id, node_id) → PLM pointer
- [ ] Update LocationDefinition to use (room_id, node_id) instead of romAddress
- [ ] Load node pointer mappings from data files or calculate from room data

### 3. Update SeedPatcher.java
- [ ] Move seed metadata from 0x82FF00 to 0xDFFEF0 (matches Rust)
- [ ] Update ROM header to "SUPERMETROID MAPRANDO" at 0x7FC0
- [ ] Use 16-byte seed name (null-terminated)
- [ ] Use 4-byte display seed (u32 little-endian)
- [ ] Consider storing timestamp in seed name or separate location

### 4. Update locations.json
- [ ] Remove `romAddress` field
- [ ] Add `roomId` field
- [ ] Add `nodeId` field
- [ ] Maintain compatibility with existing location IDs

### 5. Add Item Enum or Constants
- [ ] Create Item enum with 25 items matching Rust
- [ ] Or update ItemIds constants to include all 25 items
- [ ] Ensure ordering matches PLM table indices

### 6. Add Special Item Handling
- [ ] Implement "Nothing" item bitmask tracking
- [ ] Handle Bomb Torizo room special case (invisible fake item)
- [ ] Add logic for item dot visibility on map

---

## Migration Priority

### Phase 1: Core PLM System (High Priority)
1. Implement PLM type table (all 75 values)
2. Update ItemPatcher to use PLM types
3. Update to use `writeU16()` instead of `writeU8()`

### Phase 2: Container Detection (High Priority)
1. Read original PLM type from base ROM
2. Detect container type from original PLM
3. Select correct PLM value from table

### Phase 3: Location Addressing (Medium Priority)
1. Add node pointer map system
2. Update location definitions to use (room_id, node_id)
3. Load node pointer mappings from data

### Phase 4: Seed Metadata (Medium Priority)
1. Move seed storage to 0xDFFEF0
2. Update ROM header
3. Match Rust seed format

### Phase 5: Advanced Features (Low Priority)
1. Nothing item bitmask
2. Bomb Torizo special case
3. Item dot visibility
4. Title screen customization

---

## Data Files Needed

### Current: locations.json
```json
{
  "id": "brinstar_morph_ball_room",
  "name": "Morph Ball Room",
  "region": "Brinstar",
  "romAddress": "0x8282F5",
  ...
}
```

### Proposed: locations.json (matches Rust)
```json
{
  "id": "brinstar_morph_ball_room",
  "name": "Morph Ball Room",
  "region": "Brinstar",
  "roomId": 5,
  "nodeId": 2,
  "area": "Brinstar",
  ...
}
```

### New: node_ptr_map.json (or derive from room geometry)
```json
{
  "5_2": "0x848200",
  "19_7": "0x84A100",
  ...
}
```

---

## Testing Strategy

### Unit Tests
- Test PLM type table lookups for all 25 items × 3 containers
- Test container type detection from PLM values
- Test item placement with original ROM reading
- Test seed metadata writing to new locations

### Integration Tests
- Generate seed with new system
- Verify ROM has correct PLM values
- Verify seed metadata at correct addresses
- Test with real ROM in emulator

---

## Compatibility Notes

### Breaking Changes
- Location definitions will change (romAddress → roomId/nodeId)
- Seed storage location changes (0x82FF00 → 0xDFFEF0)
- Item representation changes (byte → 16-bit PLM type)

### Backward Compatibility
- Maintain seed ID format for spoiler logs
- Keep existing randomization algorithms
- Keep existing web API endpoints

---

## References

### Rust Source Files
- `rust/maprando/src/patch.rs` - Main patching logic
- `rust/maprando-game/src/lib.rs` - Game data definitions
- `patches/rom_map/Bank *.txt` - ROM bank free space maps

### Key Functions
- `item_to_plm_type()` - Converts item to PLM value
- `place_items()` - Main item placement loop
- `apply_seed_identifiers()` - Seed metadata writing

### Key Data Structures
- `node_ptr_map: HashMap<(usize, usize), usize>` - Location addressing
- `plm_table: [[isize; 25]; 3]` - Item to PLM mapping
- `item_placement: Vec<Item>` - Randomization result

---

## Conclusion

The Rust project's ROM patching system is significantly more sophisticated than the current Java POC. The main improvements are:

1. **PLM-based system** instead of simple byte values
2. **Container-aware** item placement (preserves Chozo orbs, hidden blocks)
3. **Node pointer addressing** instead of hardcoded addresses
4. **Standard seed storage** for website integration

Migrating to this approach will require substantial changes to the Java ItemPatcher, LocationRomAddressMapper, and SeedPatcher classes, as well as updates to the data files. However, this will make the Java randomizer compatible with the Rust randomizer's ROM format and enable cross-platform seed sharing.
