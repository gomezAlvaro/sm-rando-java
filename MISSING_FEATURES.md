# Missing Features for Complete Difficulty Implementation

## ✅ Implemented (Complete)
- Difficulty data loading from JSON
- ItemPoolFactory with progressionRate and fillerItemRate scaling
- Starting items system (GameState.withStartingItems)
- Tech assumptions (TraversalState.setDifficultyTechLevel)
- Location loading from data (CRITICAL FIX - was returning empty list)
- Service integration (SeedGenerationService uses difficulty)
- 13 integration tests passing
- **Removed unused game balance multipliers** (not in original Rust project)

## ❌ Still Missing

### CRITICAL (Blocks full functionality)
1. **None** - All critical features implemented

### IMPORTANT (Should implement for proper difficulty)

2. **Start Location** - Setting not implemented:
   - Would allow different spawn points per difficulty
   - Currently always starts at default location
   - Not in original Rust project - would be new feature

3. **Movement Restrictions** - Could add for nightmare:
   - Could prevent certain advanced movement options
   - Could add "can_use_shortcuts" tech requirement
   - Not in original Rust project - would be new feature

4. **Ammo Drop Reduction** - Could add for nightmare:
   - Would reduce ammo/health drops from enemies
   - Would require ResourceManager modifications
   - Not in original Rust project - would be new feature

### NICE TO HAVE (Polish)

5. **Seed Verification** - Difficulty not considered:
   - SeedVerifier should use difficulty tech assumptions
   - Should verify seeds are beatable given difficulty settings
   - Current verification uses default tech level

6. **Quality Metrics** - Difficulty not factored in:
   - Quality calculation should consider difficulty level
   - A "good" nightmare seed might have different criteria than casual
   - Path quality scores should be difficulty-relative

7. **Spoiler Log Enhancement** - Missing difficulty info:
   - Spoiler logs don't show difficulty preset used
   - Should include starting items list
   - Should show tech assumptions

8. **Documentation** - No user guide:
   - Document what each difficulty changes
   - Explain tech assumptions to players
   - Provide difficulty selection guide

## Implementation Priority

### Phase 1 (Complete)
✅ Difficulty data loading
✅ Item pool scaling
✅ Starting items
✅ Tech assumptions
✅ Location loading fix
✅ Removed unused multipliers

### Phase 2 (Recommended)
🔄 Start location support (new feature)
🔄 Movement restrictions (new feature)
🔄 Ammo drop reduction (new feature)

### Phase 3 (Enhancement)
⏳ Seed verification integration
⏳ Quality metrics adjustment
⏳ Spoiler log improvements
⏳ User documentation

## Technical Debt

1. **createLocationsFromData()** - Was returning empty list, now fixed
2. **DifficultyConfig** - Duplicate tech system (DifficultyData vs DifficultyConfig)
3. **Tech mapping** - Some tech requirements may not map correctly

## What Was Removed

The following fields were removed from difficulties.json as they don't exist in the original Rust project:
- `enemyDamage` multiplier - Not in Rust, requires ROM patching
- `enemyHealth` multiplier - Not in Rust, requires ROM patching
- `resourceMultiplier` - Not in Rust, requires ROM patching
- `startLocation` - Not in Rust, would be new feature
- `restrictMovement` - Not in Rust, would be new feature
- `reduceAmmoDrops` - Not in Rust, would be new feature

These were placeholders that were never implemented and are outside the scope of a randomizer (which shuffles items, doesn't rebalance the game).

## Next Steps

1. Consider adding start location support (new feature)
2. Consider adding movement restrictions (new feature)
3. Update SeedVerifier to use difficulty
4. Enhance spoiler logs with difficulty info
5. Write user documentation
