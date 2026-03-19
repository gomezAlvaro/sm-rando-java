# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Super Metroid Map Randomizer - Java proof-of-concept implementing item randomization with intelligent placement algorithms. This is a learning project that has evolved through 6 expansion phases into a production-quality system with web UI and comprehensive testing.

**Current Status**: Phase 5 Complete - Data-Driven Architecture. All JSON item properties are loaded and utilized. Complete tech system implemented with 10 tech abilities. Dynamic requirement checking and data-driven damage calculations. Phase 6 (Web UI) also complete with Spring Boot REST API and Vue.js frontend.

**Recent Additions**:
- Complete tech system (TechDefinition, TechRegistry, 10 tech abilities)
- Dynamic requirement checking from JSON data
- Data-driven damage calculations (multipliers, bonuses, reductions)
- Enhanced ItemDefinition with all JSON properties
- ItemIds constant class for centralized ID management
- Removed enum dependencies in favor of String-based architecture

**Test Status**: All tests passing (463 tests, 100% pass rate). Comprehensive test coverage for all new features.

## Build & Test Commands

```bash
# Build and compile
mvn clean compile

# Run full test suite
mvn test

# Run specific test class
mvn test -Dtest=ForesightRandomizerTest

# Run web layer tests
mvn test -Dtest=*web*

# Start Spring Boot backend (port 8080)
mvn spring-boot:run
# Or package and run
mvn clean package -DskipTests && java -jar target/map-randomizer-poc-1.0-SNAPSHOT.jar

# Start Vue.js frontend (port 5173+)
cd frontend
npm run dev

# Build frontend for production
cd frontend
npm run build
```

**Java Version**: 21 (required for modern features)
**Maven Version**: 3.8+
**Node.js**: 18+ for frontend development

## Architecture

### Package Structure (Layered Architecture)

```
com.maprando/
├── model/              # Core data structures (Item, ItemDefinition, ItemRegistry, GameState, Inventory, DataDrivenInventory)
│                     # NEW: TechDefinition, TechRegistry, ItemIds
├── logic/              # Game rules and business logic
│                     # NEW: DynamicRequirementChecker
├── randomize/          # Basic randomization algorithms
├── randomize/advanced/ # Advanced algorithms with reachability analysis
├── traversal/          # Graph traversal and reachability analysis
├── data/               # JSON data loading system
│   └── model/         # NEW: TechData for JSON parsing
├── util/               # Utility classes and helpers
├── web/                # Web layer
│   ├── controller/     # REST API controllers (SeedApiController, DownloadController, HealthController)
│   ├── dto/            # Data transfer objects (SeedRequest, SeedResponse, QualityMetricsDto)
│   ├── service/        # Web-specific services (SeedGenerationService, FilesystemSeedStorageService)
│   ├── config/         # Spring configuration (WebConfig, RandomizerConfig)
│   └── exception/      # Exception handling (GlobalExceptionHandler)
└── demo/               # Demonstration programs

frontend/                # Vue.js SPA
├── src/
│   ├── components/     # Vue components (SeedGenerator, SeedDetails, QualityMetrics)
│   ├── views/          # Page components (HomeView, GenerateView, SeedDetailsView)
│   ├── services/       # API clients (seedApi.js)
│   ├── router/         # Vue Router configuration
│   └── App.vue         # Root component
└── package.json        # npm dependencies
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

- **86 Java classes** (~9,700 LOC excluding tests, ~6,500 LOC tests)
- **463 tests** (100% pass rate, comprehensive coverage)
- **6 main packages** + web layer + advanced algorithms subpackage + util package
- **Dependencies**: Apache Commons Lang 3.13.0, Guava 32.1.3-jre, Jackson 2.15.2, Spring Boot 3.2.0, JUnit 5.10.0
- **Data-driven foundation**: ItemDefinition, ItemRegistry, TechDefinition, TechRegistry, DataDrivenInventory
- **Web UI**: Vue.js 3 + Vite + Tailwind CSS frontend

## Important Notes

1. **Test-First Development**: Always create comprehensive tests before implementation (user requirement)
2. **No Circular Dependencies**: Package structure enforces clean layering
3. **Immutable by Default**: Use records and final fields where possible
4. **GameState Cloning**: Essential for traversal simulations
5. **JSON Over Hardcoding**: Prefer external JSON data to hardcoded values
6. **Data-Driven Architecture**: All systems use data-driven ItemDefinition and ItemRegistry
7. **Tech System**: 10 tech abilities (can_morph, can_shinespark, etc.) enable advanced capabilities
8. **String-Based IDs**: Use ItemIds constants for item/tech IDs instead of enums
9. **Dynamic Requirements**: Use DynamicRequirementChecker for validating item/tech requirements
10. **Damage Calculations**: All damage values come from JSON (with fallbacks for backward compatibility)

## Future Expansion Areas

- **ROM Patching Integration**: Generate playable .smc ROM files with seed data
- **Enhanced Web UI**: Add tech visualization, seed comparison tools, user accounts
- **Advanced Requirement Logic**: Implement AND/OR/NOT logic for complex requirements
- **Multi-World Randomization**: Extend data-driven system for multi-world seeds
- **Difficulty Preset Enhancement**: Use tech system for more sophisticated difficulty balancing
- **Performance Optimization**: Profile and optimize hot paths in randomization algorithms

## Web Application Usage

### Starting the Applications

```bash
# Terminal 1: Start Spring Boot backend (port 8080)
mvn spring-boot:run
# Or: java -jar target/map-randomizer-poc-1.0-SNAPSHOT.jar

# Terminal 2: Start Vue.js frontend (port 5173+)
cd frontend
npm run dev
```

### Access Points

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080
- **API Docs**: http://localhost:8080/api/health (health check)

### API Endpoints

- `POST /api/seeds/generate` - Generate a new seed
- `GET /api/seeds/{seedId}` - Get seed details
- `GET /api/seeds/recent` - List recent seeds
- `GET /seed/{seedId}/spoiler` - Download spoiler log

### Frontend Features

- **Seed Generation Form**: Configure seed, algorithm, difficulty, and options
- **Quality Metrics Display**: Visual representation of seed quality
- **Spoiler Log Download**: Get complete item placement information
- **Responsive Design**: Works on mobile and desktop

### Web Architecture Notes

The web layer follows Spring Boot best practices:
- **RESTful API**: JSON-based stateless API design
- **Layer Separation**: Controllers → Services → Data/Logic
- **Dependency Injection**: Spring manages component lifecycle
- **Exception Handling**: Centralized error handling with proper HTTP status codes
- **CORS Configuration**: Frontend-backend communication properly configured
- **Database integration**: Replace filesystem storage with proper database
- **Multi-world support**: Multi-world randomization
