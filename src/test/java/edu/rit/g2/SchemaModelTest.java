package edu.rit.g2;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import edu.rit.g2.model.SchemaModel;


public class SchemaModelTest {

    private SchemaModel schemaModel;

    @BeforeEach
    void setUp() {
        schemaModel = new SchemaModel("test");
    }

    @Test
    void test_valid_tables() {
        List<Map<String, Object>> tables = new ArrayList<>();
        Map<String, Object> table1 = new HashMap<>();
        table1.put("name", "users");
        table1.put("rows", 10);
        tables.add(table1);

        schemaModel.setTables(tables);

        assertNotNull(schemaModel.getTables(), "getTables() should not return null after setting tables");
        assertEquals(1, schemaModel.getTables().size(), "List should contain one table entry");
        assertEquals("users", schemaModel.getTables().get(0).get("name"), "Table name should match expected value");
    }

    @Test
    void test_null_tables() {
        schemaModel.setTables(null);
        assertNull(schemaModel.getTables(), "getTables() should return null when tables not initialized");
    }

    @Test
    void test_null_entry() {
        List<Map<String, Object>> tables = new ArrayList<>();
        tables.add(null); 

        schemaModel.setTables(tables);

        List<Map<String, Object>> result = schemaModel.getTables();
        assertNotNull(result, "getTables() should not be null even if list contains null entries");
        assertEquals(1, result.size(), "List should still have one element (null entry)");
        assertNull(result.get(0), "First element should be null as inserted");
    }
}