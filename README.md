# Map Randomizer - Java Proof of Concept

A Java implementation of core concepts from the Super Metroid Map Randomizer, demonstrating item randomization algorithms and game state management.

## 🎯 Project Overview

This is a **learning project** that ports core components of the Super Metroid Map Randomizer from Rust to Java. It demonstrates fundamental randomization concepts while providing a functional proof-of-concept.

### What This Project Does

✅ **Core Data Models** - Java representations of game entities (items, resources, inventory)
✅ **Basic Inventory System** - Item collection and resource management
✅ **Simple Randomization Logic** - Basic item placement algorithms
✅ **Game State Management** - Tracking player state and inventory
✅ **Working Demonstration** - A runnable example showing the concept
✅ **JSON Data Loading** - Load items, locations, and requirements from external JSON files

### What This Project Does NOT Do (Yet)

❌ Full ROM patching system
❌ Complex graph traversal algorithms
❌ Web interface
❌ Complete logic engine

## 📁 Project Structure

```
map-randomizer-poc/
├── pom.xml                                    # Maven configuration
├── README.md                                  # This file
└── src/
    ├── main/java/com/maprando/
    │   ├── model/                            # Core data structures
    │   │   ├── Item.java                     # 24 major items enum
    │   │   ├── ResourceType.java             # Resource types (energy, missiles, etc.)
    │   │   ├── ResourceLevel.java            # Resource consumption tracking
    │   │   ├── Inventory.java                # Player inventory management
    │   │   └── GameState.java                # Game state representation
    │   ├── logic/                            # Game logic and rules
    │   │   ├── ItemCollector.java            # Item pickup logic
    │   │   ├── ResourceManager.java          # Resource availability checks
    │   │   ├── RequirementChecker.java       # Requirement satisfaction logic
    │   │   └── DamageCalculator.java         # Simple damage calculations
    │   ├── randomize/                        # Randomization algorithms
    │   │   ├── Location.java                 # Item placement locations
    │   │   ├── ItemPool.java                 # Available items for placement
    │   │   ├── BasicRandomizer.java          # Simple randomization algorithm
    │   │   └── RandomizationResult.java      # Output of randomization
    │   └── demo/                             # Demonstration programs
    │       ├── SimpleDemo.java               # Main demonstration
    │       └── PrintableSpoiler.java         # Formatted spoiler output
    └── test/java/com/maprando/
        ├── model/
        ├── logic/
        └── randomize/
```

## 🚀 Getting Started

### Prerequisites

- **Java 21** or higher (for modern language features: records, pattern matching)
- **Maven 3.8+** (for dependency management)

### Building the Project

```bash
# Clone or navigate to the project directory
cd sm-java

# Compile the project
mvn clean compile

# Run tests (when available)
mvn test

# Run the demonstration
mvn exec:java -Dexec.mainClass="com.maprando.demo.SimpleDemo"
```

### Running the Demo

The demonstration program shows:
1. Creating a game state with starting items
2. Demonstrating item collection
3. Demonstrating resource management
4. Creating an item pool
5. Defining item locations
6. Running randomization
7. Printing a spoiler log
8. Verifying completion

```bash
mvn exec:java -Dexec.mainClass="com.maprando.demo.SimpleDemo"
```

## 📚 Key Concepts

### 1. Item System

The `Item` enum defines 24 major items including:
- **Beams**: Charge, Ice, Wave, Spazer, Plasma
- **Morph Ball**: Morph Ball, Bomb, Spring Ball, Power Bomb
- **Movement**: Hi-Jump Boots, Speed Booster, Space Jump, Screw Attack
- **Suits**: Varia Suit, Gravity Suit
- **Keys**: Area keys for boss access
- **Tanks**: Energy, Missile, Super Missile, Power Bomb

```java
// Check if player can morph
boolean canMorph = RequirementChecker.canMorph(gameState);

// Collect an item
ItemCollector.collectItem(gameState, Item.ICE_BEAM);
```

### 2. Resource Management

Resources track both capacity and consumption:
- **Energy**: Health points (100-299)
- **Missiles**: Missile ammo (0-250)
- **Super Missiles**: Super missile ammo (0-50)
- **Power Bombs**: Power bomb ammo (0-50)

```java
// Check resource availability
boolean canShoot = ResourceManager.hasResource(state, ResourceType.MISSILE, 5);

// Consume resources
ResourceManager.consumeResource(state, ResourceType.MISSILE, 5);
```

### 3. Game State

`GameState` tracks everything about the player:
- Inventory (collected items)
- Resource capacities and consumption
- Current position
- Health

```java
// Create a standard starting state
GameState state = GameState.standardStart();

// Clone state for traversal simulations
GameState clonedState = state.clone();
```

### 4. Randomization

The basic randomizer uses a simple progression algorithm:
1. Separate locations into early (no requirements) and late (has requirements)
2. Place progression items in early locations first
3. Place remaining progression items in late locations
4. Fill remaining locations with filler items (tanks)

```java
// Create a randomizer with a seed
BasicRandomizer randomizer = new BasicRandomizer("my-seed-123");

// Add locations
randomizer.addLocation(location);

// Set item pool
randomizer.setItemPool(ItemPool.createStandardPool());

// Run randomization
RandomizationResult result = randomizer.randomize();

// Print spoiler log
System.out.println(result.generateSpoilerLog());
```

## 🏗️ Architecture Decisions

### Design Patterns

- **Strategy Pattern**: For different randomization algorithms (extensible)
- **Builder Pattern**: For complex object construction (Location, RandomizationResult)
- **Immutable Objects**: For game state (easier reasoning about state)
- **Record Classes**: For immutable data structures (ResourceLevel)

### Performance Considerations

- **EnumSet**: For efficient item collection storage
- **Bitwise Operations**: For capacity tracking where applicable
- **Immutable State**: Enables safe cloning for traversal
- **Lazy Evaluation**: Expensive computations only when needed

## 📖 Learning Outcomes

This project demonstrates:

1. **Rust-to-Java Translation**: How to think about ownership and borrowing in Java terms
2. **Game State Management**: Complex state tracking and updates
3. **Randomization Algorithms**: Progressive item placement and difficulty balancing
4. **Functional Programming**: Using Java's functional features for data transformations
5. **Performance Optimization**: When and how to optimize Java code for game logic

## 🔧 Extending the Project

### Future Expansion Ideas

1. **JSON Data Loading** - Load real game data from external files
2. **Graph Traversal** - Implement reachability analysis for proper verification
3. **Difficulty Tiers** - Add difficulty levels for randomization
4. **Advanced Algorithms** - Implement more sophisticated randomization strategies
5. **ROM Patching** - Add ability to modify actual ROM files
6. **Web Interface** - Create a web UI for seed generation

### Adding a New Item

```java
// In Item.java
NEW_ITEM("New Item", "Description"),

// Add helper methods if needed
public boolean isNewItem() {
    return this == NEW_ITEM;
}
```

### Adding a New Randomization Algorithm

```java
public class AdvancedRandomizer extends BasicRandomizer {
    @Override
    public RandomizationResult randomize() {
        // Implement advanced logic
    }
}
```

## 📊 Code Statistics

- **Total Files**: 27 Java classes + 4 JSON data files
- **Lines of Code**: ~3,500 (excluding JSON and tests)
- **Packages**: 6 (model, logic, randomize, demo, data, test)
- **Dependencies**: 4 (Apache Commons Lang, Guava, JUnit 5, Jackson)

## 🧪 Testing

The project includes test directories for unit tests. To run tests:

```bash
mvn test
```

Test structure:
```
src/test/java/com/maprando/
├── model/       # Test data models
├── logic/       # Test game logic
└── randomize/   # Test randomization algorithms
```

## 📝 Example Output

### Spoiler Log

```
=== MAP RANDOMIZER SPOILER LOG ===
Seed: demo-seed-123
Timestamp: 2026-03-17T12:34:56.789
Algorithm: Basic Progression Randomizer
Status: SUCCESS
Items Placed: 10

[Brinstar]
  Morph Ball Room (Brinstar) -> Morph Ball
  Charge Beam Room (Brinstar) -> Energy Tank

[Norfair]
  Ice Beam Room (Norfair) -> Ice Beam
  Speed Booster Room (Norfair) -> Varia Suit
...
```

## 📊 JSON Data Loading System

The project now supports loading game data from external JSON files, making it easy to configure without recompiling.

### Running the JSON Demo

```bash
mvn exec:java -Dexec.mainClass="com.maprando.demo.JsonDataDemo"
```

### JSON Data Files

Located in `src/main/resources/data/`:

**items.json** - 27 items with properties:
- Categories (beam, movement, suit, key, tank, etc.)
- Progression flags
- Damage multipliers and bonuses
- Requirements and enables

**locations.json** - 15 locations:
- Area and region information
- Accessibility requirements
- Early game flags
- Boss location flags

**requirements.json** - Requirement definitions:
- Tech requirements (can_morph, can_survive_heat, etc.)
- Item dependencies
- Logical conditions

**difficulties.json** - 5 difficulty presets:
- Casual, Normal, Hard, Expert, Nightmare
- Enemy damage/health multipliers
- Resource multipliers
- Starting items

### Customizing Game Data

Simply edit the JSON files and restart the application:

```json
{
  "id": "NEW_ITEM",
  "displayName": "New Item",
  "category": "utility",
  "isProgression": true,
  "description": "Custom item for testing"
}
```

### Data Loading API

```java
// Load all JSON data
DataLoader dataLoader = new DataLoader();
dataLoader.loadAllData();

// Access item definitions
ItemData.ItemDefinition itemDef = dataLoader.getItemDefinition("CHARGE_BEAM");

// Create game objects from JSON
ItemPool pool = createItemPoolFromJson(dataLoader);
List<Location> locations = createLocationsFromJson(dataLoader);
```

## 🤝 Contributing

This is a learning project. Suggestions and improvements are welcome!

### Areas for Contribution

1. Add unit tests for existing classes
2. Implement graph traversal for proper reachability
3. Add more sophisticated randomization algorithms
4. Create additional demo programs
5. Improve documentation and examples

## 📄 License

This is a proof-of-concept for educational purposes. The original MapRandomizer project has its own license.

## 🙏 Acknowledgments

- Original **MapRandomizer** project (Rust implementation)
- **Super Metroid** - The game this randomizer is based on
- Java community for excellent libraries and tools

## 📞 Support

For questions or issues:
1. Check the code comments and documentation
2. Review the demonstration program
3. Examine the test cases (when available)

---

**Status**: Proof of Concept ✅
**Last Updated**: March 2026
**Java Version**: 21
