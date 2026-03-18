# Architecture Diagram

## Package Structure

```
com.maprando
│
├── model (Data Layer)
│   ├── Item                    ─────┐
│   ├── ResourceType            │    │ Core
│   ├── ResourceLevel           │    │ Data
│   ├── Inventory               │    │ Models
│   └── GameState               ─────┘
│
├── logic (Business Logic Layer)
│   ├── ItemCollector           ───┐
│   ├── ResourceManager         │  │ Game
│   ├── RequirementChecker      ├──┤ Logic
│   └── DamageCalculator        ───┘
│
├── randomize (Algorithm Layer)
│   ├── Location                ────┐
│   ├── ItemPool                │   │
│   ├── BasicRandomizer         ├───┤ Randomization
│   └── RandomizationResult     ────┘
│
└── demo (Presentation Layer)
    ├── SimpleDemo              ────┐
    └── PrintableSpoiler        ────┘ Demonstration
```

## Class Relationships

```
┌─────────────────────────────────────────────────────────────┐
│                      MODEL LAYER                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   ┌─────────────┐       ┌─────────────────────────────┐   │
│   │    Item     │       │       GameState             │   │
│   │   (enum)    │       │  ┌─────────────────────┐    │   │
│   └─────────────┘       │  │    Inventory        │    │   │
│                         │  │  ┌───────────────┐  │    │   │
│   ┌─────────────┐       │  │  │ EnumSet<Item>│  │    │   │
│   │ResourceType │       │  │  └───────────────┘  │    │   │
│   │   (enum)    │       │  └─────────────────────┘    │   │
│   └─────────────┘       │  ┌─────────────────────┐    │   │
│                         │  │ ResourceLevel Map   │    │   │
│                         │  └─────────────────────┘    │   │
│                         └─────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ uses
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   LOGIC LAYER                                │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   ┌──────────────┐     ┌──────────────────┐               │
│   │ItemCollector │────▶│  GameState       │               │
│   └──────────────┘     └──────────────────┘               │
│                                                             │
│   ┌─────────────────┐   ┌──────────────────┐              │
│   │ResourceManager  │──▶│  GameState       │              │
│   └─────────────────┘   └──────────────────┘              │
│                                                             │
│   ┌───────────────────┐ ┌──────────────────┐              │
│   │RequirementChecker │─▶│  GameState       │              │
│   └───────────────────┘ └──────────────────┘              │
│                                                             │
│   ┌──────────────────┐  ┌──────────────────┐              │
│   │DamageCalculator  │─▶│  GameState       │              │
│   └──────────────────┘  └──────────────────┘              │
│                                                             │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ uses
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                RANDOMIZATION LAYER                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   ┌──────────────┐     ┌──────────────────┐               │
│   │   Location   │────▶│ Item             │               │
│   │   (Builder)  │     │ (enum)           │               │
│   └──────────────┘     └──────────────────┘               │
│                                                             │
│   ┌──────────────┐     ┌──────────────────┐               │
│   │  ItemPool    │────▶│ Item             │               │
│   │              │     │ (enum)           │               │
│   └──────────────┘     └──────────────────┘               │
│                         ▲                                   │
│   ┌──────────────┐     │                                   │
│   │              │     │                                   │
│   │BasicRandomizer│─────┘                                   │
│   │              │                                         │
│   └──────┬───────┘                                         │
│          │ creates                                        │
│          ▼                                                │
│   ┌──────────────────┐                                    │
│   │Randomization     │                                    │
│   │Result            │                                    │
│   │  (Builder)       │                                    │
│   └──────────────────┘                                    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ uses
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                  DEMO LAYER                                 │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   ┌──────────────┐     ┌──────────────────┐               │
│   │  SimpleDemo  │────▶│BasicRandomizer   │               │
│   │              │     │                  │               │
│   └──────────────┘     └──────────────────┘               │
│                         ▲                                   │
│   ┌──────────────┐     │                                   │
│   │Printable     │─────┘                                   │
│   │Spoiler       │                                         │
│   │              │                                         │
│   └──────────────┘                                         │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Data Flow

```
┌─────────┐
│  Seed   │
└────┬────┘
     │
     ▼
┌──────────────────┐
│ BasicRandomizer  │
└────┬─────────────┘
     │
     ├──▶ ┌─────────────────┐
     │   │   ItemPool      │
     │   │ (creates items) │
     │   └─────────────────┘
     │
     ├──▶ ┌─────────────────┐
     │   │   Locations     │
     │   │ (places items)  │
     │   └─────────────────┘
     │
     ▼
┌──────────────────────┐
│ RandomizationResult  │
└────┬─────────────────┘
     │
     ├──▶ ┌──────────────────┐
     │   │ generateSpoiler() │
     │   └──────────────────┘
     │
     └──▶ ┌────────────────────┐
         │ PrintableSpoiler   │
         └────────────────────┘
```

## Key Design Patterns

### 1. Builder Pattern
```java
Location.builder()
    .id("loc1")
    .name("Name")
    .region("Region")
    .build();

RandomizationResult.builder()
    .seed("seed")
    .successful(true)
    .build();
```

### 2. Strategy Pattern (Functional)
```java
Requirement req = state -> state.getEnergy() > 50;
RequirementChecker.meetsRequirements(state, req);
```

### 3. Immutable Objects (Records)
```java
public record ResourceLevel(ResourceType type, int maxCapacity, int consumed)
```

### 4. Cloneable Pattern
```java
GameState original = state.clone();
// Modify cloned state
state.takeDamage(100);
```

## Layer Responsibilities

| Layer | Responsibility | Key Classes |
|-------|----------------|-------------|
| **Model** | Data structures | Item, GameState, Inventory |
| **Logic** | Game rules | ItemCollector, RequirementChecker |
| **Randomize** | Algorithms | BasicRandomizer, ItemPool |
| **Demo** | Presentation | SimpleDemo, PrintableSpoiler |

## Dependencies

```
Demo Layer
    ↓ depends on
Randomize Layer
    ↓ depends on
Logic Layer
    ↓ depends on
Model Layer
```

**No circular dependencies!** Each layer only depends on layers below it.

## External Dependencies

```
┌─────────────────────────────────┐
│         Maven                   │
│  ┌───────────────────────────┐  │
│  │ Apache Commons Lang 3.13.0│  │
│  └───────────────────────────┘  │
│  ┌───────────────────────────┐  │
│  │ Guava 32.1.3-jre          │  │
│  └───────────────────────────┘  │
│  ┌───────────────────────────┐  │
│  │ JUnit 5.10.0              │  │
│  └───────────────────────────┘  │
└─────────────────────────────────┘
```

## Threading Model

- **Immutable classes** (Item, ResourceType, ResourceLevel): Thread-safe
- **Mutable classes** (GameState, Inventory, ItemPool): Not thread-safe
- **Clone support**: All state classes support cloning for parallel traversal

## Memory Efficiency

| Class | Storage | Notes |
|-------|---------|-------|
| Item | Enum (1 byte/item) | Flyweight pattern |
| Inventory | EnumSet | Efficient bitset |
| ResourceLevel | Record | Compact storage |
| GameState | ~200 bytes | Clonable for traversal |
