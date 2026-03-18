# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Super Metroid Map Randomizer - Java proof-of-concept implementing item randomization with intelligent placement algorithms. This is a learning project that has evolved through 5 expansion phases into a production-quality system with ~7,500 LOC and 304 tests.

**Current Status**: Phase 5 foundation - Data-Driven Architecture with ItemDefinition, ItemRegistry, and DataDrivenInventory. NOTE: Some tests currently failing (5 failures out of 297 tests).

**Architecture Note**: The project currently uses an enum-based Item system but has a data-driven foundation (ItemDefinition, ItemRegistry, DataDrivenInventory) that aligns with the original Rust MapRandomizer. Both systems coexist; new code should prefer the data-driven approach. See DATA_DRIVEN_ARCHITECTURE.md for migration guide.

## Build & Test Commands

```bash
# Build and compile
mvn clean compile

# Run full test suite (297 tests, ~2 seconds)
mvn test

# Run specific test class
mvn test -Dtest=ForesightRandomizerTest

# Run simple demo
mvn exec:java -Dexec.mainClass="com.maprando.demo.SimpleDemo"

# Run JSON data demo
mvn exec:java -Dexec.mainClass="com.maprando.demo.JsonDataDemo"
```

**Java Version**: 21 (required for modern features)
**Maven Version**: 3.8+

## Architecture

### Package Structure (Layered Architecture)

```
com.maprando/
├── model/              # Core data structures (Item, ItemDefinition, ItemRegistry, GameState, Inventory, DataDrivenInventory)
├── logic/              # Game rules and business logic
├── randomize/          # Basic randomization algorithms
├── randomize/advanced/ # Advanced algorithms with reachability analysis
├── traversal/          # Graph traversal and reachability analysis
├── data/               # JSON data loading system
├── util/               # Utility classes and helpers
└── demo/               # Demonstration programs
```

### Dependency Flow (No Circular Dependencies)

```
Demo → Randomize/Advanced → Traversal → Logic → Model
                ↓                ↓          ↓       ↓
              Data ───────────┴──────────┴───────┘
```

### Key Architectural Patterns

- **Strategy Pattern**: RequirementChecker functional interface, pluggable randomization algorithms
- **Builder Pattern**: Location, RandomizationResult for complex object construction
- **State Pattern**: TraversalState for reachability analysis
- **Record Classes**: ResourceLevel and other immutable data structures
- **Cloneable Pattern**: GameState for state simulation during traversal

### Core Systems

**1. Model Layer** (`model/`)
- `Item` enum: 28 items (beams, morph ball, movement, suits, keys, tanks)
- `GameState`: Complete game state with cloning support
- `Inventory`: EnumSet-based efficient item storage

**2. Logic Layer** (`logic/`)
- `ItemCollector`: Item pickup logic with special effects
- `ResourceManager`: Resource availability and consumption
- `RequirementChecker`: Functional requirement satisfaction interface

**3. Randomization Layer** (`randomize/`, `randomize/advanced/`)
- `BasicRandomizer`: Simple progression-based placement
- `ForesightRandomizer`: Intelligent reachability-based placement with backtracking
- `BalancedProgressionAlgorithm`: Region-aware balanced distribution
- `QualityMetricsCalculator`: Seed quality analysis and scoring

**4. Traversal Layer** (`traversal/`)
- `GameGraph`: Graph representation of game world
- `ReachabilityAnalysis`: BFS-based location accessibility analysis
- `SeedVerifier`: Validates seeds are beatable
- `TraversalState`: Player state tracking during analysis

**5. Data Layer** (`data/`)
- `DataLoader`: Jackson-based JSON loading from `src/main/resources/data/`
- `ItemRegistry`: Registry for data-driven item definitions
- JSON files: items.json, locations.json, requirements.json, difficulties.json

**6. Data-Driven Architecture** (`model/`)
- `ItemDefinition`: Data-driven item representation (replaces Item enum)
- `ItemRegistry`: Registry with index-based boolean array tracking
- `DataDrivenInventory`: Boolean array-based inventory (aligns with original Rust)
- See DATA_DRIVEN_ARCHITECTURE.md for migration guide

## Critical Development Workflow: Test-First Development

**IMPORTANT**: This project follows strict Test-Driven Development (TDD) workflow based on user feedback.

**Always write tests FIRST before implementing new features.**

### TDD Workflow

1. **Write comprehensive tests FIRST** covering all expected behaviors
2. **Create implementation** to satisfy test requirements
3. **Run tests** and fix failures
4. **Iterate** until tests pass

This workflow achieved:
- Phase 3: 238 tests, 97% success rate
- Phase 4: 307 tests, 96% success rate
- Current: 297 tests (some failures may exist due to ongoing development)

### Test Organization

Tests mirror source structure in `src/test/java/com/maprando/`:
- `model/`, `logic/`, `randomize/`, `traversal/`, `randomize/advanced/`

## JSON Data System

Game data is externalized to JSON files in `src/main/resources/data/`:

- **items.json**: 21 items with categories, indices, requirements, damage multipliers
- **locations.json**: 15 locations with areas, requirements, early game flags
- **requirements.json**: Tech requirements and logical conditions
- **difficulties.json**: 5 difficulty presets (Casual to Nightmare)

**Modifying data**: Edit JSON files directly - no recompilation needed.

## Key Algorithms

### 1. Basic Randomization
```java
BasicRandomizer randomizer = new BasicRandomizer("seed");
randomizer.setItemPool(ItemPool.createStandardPool());
randomizer.addLocations(locations);
RandomizationResult result = randomizer.randomize();
```

### 2. Reachability Analysis
```java
TraversalState state = new TraversalState(GameState.standardStart());
ReachabilityAnalysis analysis = new ReachabilityAnalysis(dataLoader, state);
Set<String> reachable = analysis.getReachableLocations();
```

### 3. Seed Verification
```java
SeedVerifier verifier = new SeedVerifier(dataLoader);
SeedVerificationResult result = verifier.verifySeed(randomizationResult);
boolean isBeatable = result.isBeatable();
```

### 4. Advanced Randomization with Backtracking
```java
ForesightRandomizer randomizer = new ForesightRandomizer("seed", dataLoader);
randomizer.setItemPool(itemPool);
randomizer.addLocations(locations);
RandomizationResult result = randomizer.randomize();
SeedQualityMetrics metrics = randomizer.getQualityMetrics();
```

## Performance Considerations

- **EnumSet** for item storage (1 bit per item)
- **Record classes** for immutable data (compact storage)
- **GameState cloning** for traversal simulations
- **BFS traversal** for reachability analysis (O(V + E))
- **Lazy evaluation** for expensive quality metric calculations

## Common Patterns

### Creating a randomizer
```java
// 1. Load data
DataLoader dataLoader = new DataLoader();
dataLoader.loadAllData();

// 2. Create item pool and locations
ItemPool pool = ItemPool.createStandardPool();
List<Location> locations = createLocationsFromJson(dataLoader);

// 3. Configure and run randomizer
BasicRandomizer randomizer = new BasicRandomizer("seed");
randomizer.setItemPool(pool);
randomizer.addLocations(locations);
RandomizationResult result = randomizer.randomize();
```

### Working with requirements
```java
// Functional requirement checking
Requirement canMorph = state -> state.getInventory().hasItem(Item.MORPH_BALL);
boolean satisfied = RequirementChecker.meetsRequirements(gameState, canMorph);

// Complex requirements
Requirement canSurviveHeat = state ->
    state.getInventory().hasItem(Item.VARIA_SUIT) ||
    state.getInventory().hasItem(Item.GRAVITY_SUIT);
```

### State simulation
```java
// Clone state for what-if scenarios
GameState original = GameState.standardStart();
GameState simulation = original.clone();
simulation.collectItem(Item.ICE_BEAM);
// original is unchanged
```

### Data-driven inventory (new approach)
```java
// Load data
DataLoader dataLoader = new DataLoader();
dataLoader.loadAllData();
ItemRegistry registry = dataLoader.getItemRegistry();

// Create data-driven inventory
DataDrivenInventory inventory = new DataDrivenInventory(registry);
inventory.addItem("MORPH_BALL");

// Check items by ID
if (inventory.hasItem("MORPH_BALL")) {
    // Player can morph
}
```

## Project Statistics (Current)

- **60 Java classes** (~6,000 LOC excluding tests, ~5,000 LOC tests)
- **297 tests** (note: some test failures may exist due to ongoing development)
- **5 main packages** + advanced algorithms subpackage + util package
- **Dependencies**: Apache Commons Lang 3.13.0, Guava 32.1.3-jre, Jackson 2.15.2, JUnit 5.10.0
- **Data-driven foundation**: ItemDefinition, ItemRegistry, DataDrivenInventory

**Note**: Some tests may currently fail. Run `mvn test` to check current status before making changes.

## Important Notes

1. **Test-First Development**: Always create comprehensive tests before implementation (user requirement)
2. **No Circular Dependencies**: Package structure enforces clean layering
3. **Immutable by Default**: Use records and final fields where possible
4. **GameState Cloning**: Essential for traversal simulations
5. **JSON Over Hardcoding**: Prefer external JSON data to hardcoded values
6. **EnumSet for Items**: Use EnumSet instead of HashSet for item collections (enum-based system)
7. **Data-Driven Migration**: New code should prefer DataDrivenInventory and ItemRegistry (aligns with original Rust)
8. **Both Systems Coexist**: Enum-based and data-driven systems work together during migration

## Future Expansion Areas

- **Complete Data-Driven Migration**: Replace Item enum with data-driven system (DATA_DRIVEN_ARCHITECTURE.md)
- **Tech System**: Implement TECH_ID_* system from original Rust (can_morph, can_shinespark, can_walljump, etc.)
- **ROM patching integration**: Patch actual Super Metroid ROM files
- **Web interface**: Web interface for seed generation
- **Enhanced requirement system**: AND/OR/NOT logic for requirements
- **More sophisticated difficulty balancing**: Advanced difficulty algorithms
- **Multi-world support**: Multi-world randomization
