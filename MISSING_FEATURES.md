# Missing Features for Complete Difficulty Implementation

## ✅ Implemented (Complete)
- Difficulty data loading from JSON
- ItemPoolFactory with progressionRate and fillerItemRate scaling
- Starting items system (GameState.withStartingItems)
- Tech assumptions (TraversalState.setDifficultyTechLevel)
- Location loading from data (CRITICAL FIX - was returning empty list)
- Service integration (SeedGenerationService uses difficulty)
- 13 integration tests passing

## ❌ Still Missing

### CRITICAL (Blocks full functionality)
1. **None** - All critical features implemented

### IMPORTANT (Should implement for proper difficulty)

2. **Game Balance Settings** - Loaded but never used:
   - `enemyDamage` multiplier (0.5x - 3.0x)
   - `enemyHealth` multiplier (0.7x - 2.0x)
   - `resourceMultiplier` (0.5x - 1.5x)
   - These are loaded into DifficultyData but never applied anywhere

3. **Start Location** - Setting not implemented:
   - difficulties.json has `startLocation` (landing_site, random, random_hard)
   - GameState needs to support different starting locations
   - Currently always starts at default location

4. **Movement Restrictions** - Nightmare flag not implemented:
   - `restrictMovement: true` for nightmare difficulty
   - Should prevent certain advanced movement options
   - Could add "can_use_shortcuts" tech requirement

5. **Ammo Drop Reduction** - Nightmare flag not implemented:
   - `reduceAmmoDrops: true` for nightmare difficulty
   - Should reduce ammo/health drops from enemies
   - Would require ResourceManager modifications

### NICE TO HAVE (Polish)

6. **Seed Verification** - Difficulty not considered:
   - SeedVerifier should use difficulty tech assumptions
   - Should verify seeds are beatable given difficulty settings
   - Current verification uses default tech level

7. **Quality Metrics** - Difficulty not factored in:
   - Quality calculation should consider difficulty level
   - A "good" nightmare seed might have different criteria than casual
   - Path quality scores should be difficulty-relative

8. **Spoiler Log Enhancement** - Missing difficulty info:
   - Spoiler logs don't show difficulty preset used
   - Should include starting items list
   - Should show tech assumptions

9. **Documentation** - No user guide:
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

### Phase 2 (Recommended)
🔄 Game balance settings (enemyDamage, enemyHealth, resourceMultiplier)
🔄 Start location support
🔄 Movement restrictions
🔄 Ammo drop reduction

### Phase 3 (Enhancement)
⏳ Seed verification integration
⏳ Quality metrics adjustment
⏳ Spoiler log improvements
⏳ User documentation

## Technical Debt

1. **createLocationsFromData()** - Was returning empty list, now fixed
2. **DifficultyConfig** - Duplicate tech system (DifficultyData vs DifficultyConfig)
3. **Tech mapping** - Some tech requirements may not map correctly
4. **Resource multipliers** - Loaded but no system to apply them

## Next Steps

1. Implement game balance settings (modify GameState/ResourceManager)
2. Add start location support to GameState
3. Implement movement restrictions system
4. Update SeedVerifier to use difficulty
5. Enhance spoiler logs with difficulty info
6. Write user documentation
