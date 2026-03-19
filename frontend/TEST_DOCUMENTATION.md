# Frontend Test Suite Documentation

## Overview

Comprehensive test suite for the Vue.js 3 frontend using Vitest and Vue Test Utils. The test suite covers components, stores, services, and API interactions.

## Test Structure

```
frontend/src/
├── components/
│   └── __tests__/
│       ├── SeedGenerator.spec.js       # GenerateView component tests
│       └── SeedDetails.spec.js         # SeedDetailsView component tests
├── services/
│   └── __tests__/
│       └── seedApi.spec.js             # API service tests
├── stores/
│   └── __tests__/
│       └── useSeedStore.spec.js        # Pinia store tests
└── test/
    └── setup.js                        # Test configuration and mocks
```

## Running Tests

### Run All Tests
```bash
cd frontend
npm test
```

### Run Tests in UI Mode
```bash
npm run test:ui
```

### Run Tests with Coverage
```bash
npm run test:coverage
```

### Run Specific Test File
```bash
npm test -- SeedGenerator.spec.js
```

### Run Tests Matching Pattern
```bash
npm test -- --grep "generateSeed"
```

## Test Coverage

### Component Tests (~150 assertions)

#### SeedGenerator.spec.js
- **Component Rendering** (9 tests)
  - Form elements rendering
  - Input fields, selects, checkboxes
  - Generate button

- **Form Initialization** (3 tests)
  - Default form values
  - Initial loading/error states

- **User Input Handling** (5 tests)
  - Seed input updates
  - Algorithm and difficulty selection
  - Checkbox toggles

- **Form Submission** (3 tests)
  - Generate method calls
  - Loading states during submission
  - Navigation after success

- **Error Handling** (2 tests)
  - Error message display
  - Error clearing

- **Loading States** (3 tests)
  - Button disable/enable
  - Loading text display

- **Form Validation** (4 tests)
  - Empty seed handling
  - Valid algorithms
  - Valid difficulties

#### SeedDetails.spec.js
- **Component Rendering** (3 tests)
  - Container and title
  - Download button

- **Loading State** (2 tests)
  - Loading indicator
  - Loading text

- **Error State** (2 tests)
  - Error display
  - No error state

- **Seed Display** (9 tests)
  - Seed metadata display
  - Quality metrics display
  - Timestamp formatting

- **Quality Metrics Display** (4 tests)
  - Rating color coding
  - Different rating levels

- **Warnings Display** (2 tests)
  - Warnings shown
  - No warnings hidden

- **Spoiler Download** (1 test)
  - Download button functionality

- **Lifecycle Methods** (1 test)
  - Load seed on mount

- **Empty State** (1 test)
  - Null seed handling

### Store Tests (~50 assertions)

#### useSeedStore.spec.js
- **Initial State** (4 tests)
  - Empty state initialization

- **generateSeed** (8 tests)
  - Successful generation
  - Recent seeds management
  - 10-seed limit
  - Error handling
  - Loading states
  - Error clearing

- **loadSeed** (3 tests)
  - Successful load
  - Error handling
  - Loading states

- **loadRecentSeeds** (3 tests)
  - Successful load
  - Default limit
  - Error handling

- **downloadSpoiler** (2 tests)
  - Successful download
  - Error handling

- **State Management** (2 tests)
  - Clear current seed
  - Clear error

- **Advanced Scenarios** (2 tests)
  - State persistence
  - Concurrent operations

### Service Tests (~40 assertions)

#### seedApi.spec.js
- **generateSeed** (3 tests)
  - POST request
  - Error handling
  - Content type headers

- **getSeed** (3 tests)
  - GET request
  - Error handling
  - Special characters in IDs

- **getRecentSeeds** (4 tests)
  - GET request with limit
  - Default limit
  - Different limit values
  - Error handling

- **hasSpoiler** (3 tests)
  - GET request
  - True/False responses
  - Error handling

- **downloadSpoiler** (4 tests)
  - GET request with blob
  - Response type
  - Error handling
  - Large file handling

- **API Configuration** (2 tests)
  - Base URL
  - Default headers

- **Error Handling** (4 tests)
  - Network errors
  - Timeout errors
  - Server errors
  - Client errors

- **Data Integrity** (2 tests)
  - Request format
  - Response format

## Total Test Coverage

- **Backend Tests**: 80+ test methods across 5 test classes
- **Frontend Tests**: 240+ assertions across 4 test suites
- **Integration Tests**: 15+ end-to-end test scenarios

## Test Quality Metrics

### Backend
- **Service Layer**: Comprehensive coverage of business logic
- **Controller Layer**: Full REST API endpoint testing
- **Integration Tests**: End-to-end flow validation
- **Error Handling**: Robust error scenario coverage

### Frontend
- **Component Testing**: Full user interaction coverage
- **State Management**: Complete store action testing
- **API Service**: Mocked API interaction testing
- **Edge Cases**: Special character and error handling

## Key Testing Patterns

### 1. Mock Strategy
- **Vue Router**: Mocked for navigation testing
- **API Calls**: Mocked with vi.fn() for service testing
- **DOM APIs**: Mocked (ResizeObserver, matchMedia, etc.)

### 2. Async Testing
- **Promises**: Proper async/await handling
- **Loading States**: Testing intermediate states
- **Concurrent Operations**: Testing parallel requests

### 3. Component Testing
- **User Interactions**: Input changes, button clicks
- **Props & Events**: Component communication
- **Lifecycle Hooks**: onMount, onUnmount

### 4. Store Testing
- **Actions**: Async action testing
- **State Mutations**: State change verification
- **Getters**: Computed property testing

## Continuous Integration

### Test Commands
```bash
# Run all tests
mvn test && cd frontend && npm test

# Run with coverage
mvn test && cd frontend && npm run test:coverage

# Run specific test suites
mvn test -Dtest=SeedApiControllerTest
npm test -- --grep "generateSeed"
```

### Coverage Goals
- **Backend**: >80% code coverage
- **Frontend**: >70% code coverage
- **Critical Paths**: 100% coverage

## Best Practices

1. **Test Isolation**: Each test is independent
2. **Clear Naming**: Descriptive test names
3. **Arrange-Act-Assert**: Consistent test structure
4. **Mocking**: Proper external dependency mocking
5. **Edge Cases**: Testing boundary conditions
6. **Error Scenarios**: Comprehensive error handling

## Future Enhancements

1. **E2E Testing**: Playwright for full user flows
2. **Visual Regression**: Screenshot comparison testing
3. **Performance Testing**: Component render performance
4. **Accessibility**: ARIA compliance testing