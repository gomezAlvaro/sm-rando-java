package com.maprando.data.model;

import com.maprando.data.model.ItemData.ItemDefinition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Unit tests for the ItemData model class.
 */
@DisplayName("ItemData Model Tests")
class ItemDataModelTest {

    private ItemData itemData;

    @BeforeEach
    void setUp() {
        itemData = new ItemData();
    }

    @Test
    @DisplayName("ItemData should be created successfully")
    void testItemDataCreation() {
        assertNotNull(itemData, "ItemData should be created");
        // Items list is null until loaded from JSON or set manually
        // This is expected behavior for JSON deserialization
    }

    @Test
    @DisplayName("Set and get items should work correctly")
    void testSetGetItems() {
        List<ItemDefinition> items = List.of(
            createTestItem("CHARGE_BEAM", "beam", true),
            createTestItem("MORPH_BALL", "movement", true)
        );

        itemData.setItems(items);

        assertEquals(2, itemData.getItems().size(), "Should have 2 items");
        assertEquals("CHARGE_BEAM", itemData.getItems().get(0).getId(), "First item ID should match");
    }

    @Test
    @DisplayName("ItemDefinition should store all properties correctly")
    void testItemDefinitionProperties() {
        ItemDefinition item = createTestItem("TEST_ITEM", "utility", false);

        assertEquals("TEST_ITEM", item.getId(), "ID should match");
        assertEquals("Test Item", item.getDisplayName(), "Display name should match");
        assertEquals("Test Description", item.getDescription(), "Description should match");
        assertEquals("utility", item.getCategory(), "Category should match");
        assertFalse(item.isProgression(), "Progression flag should match");
    }

    @Test
    @DisplayName("ItemDefinition with damage multiplier should store correctly")
    void testItemDefinitionWithDamageMultiplier() {
        ItemDefinition item = new ItemDefinition();
        item.setId("PLASMA_BEAM");
        item.setDamageMultiplier(2.0);

        assertEquals(2.0, item.getDamageMultiplier(), 0.001,
                "Damage multiplier should be stored correctly");
    }

    @Test
    @DisplayName("ItemDefinition with damage bonus should store correctly")
    void testItemDefinitionWithDamageBonus() {
        ItemDefinition item = new ItemDefinition();
        item.setId("ICE_BEAM");
        item.setDamageBonus(5);

        assertEquals(5, item.getDamageBonus(), "Damage bonus should be stored correctly");
    }

    @Test
    @DisplayName("ItemDefinition with requirements should store correctly")
    void testItemDefinitionWithRequirements() {
        ItemDefinition item = new ItemDefinition();
        item.setId("BOMB");
        item.setRequires(List.of("can_morph"));

        assertNotNull(item.getRequires(), "Requirements should be stored");
        assertEquals(1, item.getRequires().size(), "Should have 1 requirement");
        assertEquals("can_morph", item.getRequires().get(0), "Requirement should match");
    }

    @Test
    @DisplayName("ItemDefinition with enables should store correctly")
    void testItemDefinitionWithEnables() {
        ItemDefinition item = new ItemDefinition();
        item.setId("MORPH_BALL");
        item.setEnables(List.of("can_morph", "can_fit_small_spaces"));

        assertNotNull(item.getEnables(), "Enables should be stored");
        assertEquals(2, item.getEnables().size(), "Should have 2 enables");
    }

    @Test
    @DisplayName("ItemDefinition with damage reduction should store correctly")
    void testItemDefinitionWithDamageReduction() {
        ItemDefinition item = new ItemDefinition();
        item.setId("VARIA_SUIT");
        item.setDamageReduction(0.5);

        assertEquals(0.5, item.getDamageReduction(), 0.001,
                "Damage reduction should be stored correctly");
    }

    @Test
    @DisplayName("ItemDefinition with resource type should store correctly")
    void testItemDefinitionWithResourceType() {
        ItemDefinition item = new ItemDefinition();
        item.setId("MISSILE_TANK");
        item.setResourceType("MISSILE");
        item.setCapacityIncrease(5);

        assertEquals("MISSILE", item.getResourceType(), "Resource type should match");
        assertEquals(5, item.getCapacityIncrease(), "Capacity increase should match");
    }

    @Test
    @DisplayName("Multiple ItemDefinitions should be stored independently")
    void testMultipleItemDefinitions() {
        List<ItemDefinition> items = List.of(
                createTestItem("ITEM_1", "beam", true),
                createTestItem("ITEM_2", "movement", false),
                createTestItem("ITEM_3", "tank", false)
        );

        itemData.setItems(items);

        assertEquals(3, itemData.getItems().size(), "Should have 3 items");
        assertEquals("ITEM_1", itemData.getItems().get(0).getId());
        assertEquals("ITEM_2", itemData.getItems().get(1).getId());
        assertEquals("ITEM_3", itemData.getItems().get(2).getId());
    }

    @Test
    @DisplayName("ItemDefinition with null optional fields should work")
    void testItemDefinitionWithNullOptionals() {
        ItemDefinition item = new ItemDefinition();
        item.setId("SIMPLE_ITEM");
        item.setDisplayName("Simple Item");
        item.setCategory("utility");
        item.setProgression(false);

        // Optional fields should be null
        assertNull(item.getDamageMultiplier(), "Damage multiplier should be null");
        assertNull(item.getDamageBonus(), "Damage bonus should be null");
        assertNull(item.getRequires(), "Requirements should be null");
        assertNull(item.getEnables(), "Enables should be null");
        assertNull(item.getDamageReduction(), "Damage reduction should be null");
        assertNull(item.getResourceType(), "Resource type should be null");
        assertNull(item.getCapacityIncrease(), "Capacity increase should be null");
    }

    // Helper method to create test items
    private ItemDefinition createTestItem(String id, String category, boolean isProgression) {
        ItemDefinition item = new ItemDefinition();
        item.setId(id);
        // Convert ID to title case for display name
        String displayName = id.replace("_", " ");
        // Capitalize first letter of each word
        String[] words = displayName.toLowerCase().split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }
        displayName = result.toString().trim();
        item.setDisplayName(displayName);
        item.setCategory(category);
        item.setProgression(isProgression);
        item.setDescription("Test Description");
        return item;
    }
}
