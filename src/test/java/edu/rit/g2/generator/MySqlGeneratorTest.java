package edu.rit.g2.generator;

import edu.rit.g2.model.SchemaModel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class MySqlGeneratorTest {

    private final MySqlGenerator generator = new MySqlGenerator();

    @Test
    public void testGenerate_Valid() {
        SchemaModel schema = new SchemaModel();
        schema.setTables(new ArrayList<>()); 

        Map<String, Object> table = new HashMap<>();
        table.put("tableName", "users");

        List<Map<String, String>> columns = new ArrayList<>();
        Map<String, String> col1 = new HashMap<>();
        col1.put("name", "user_id");
        col1.put("type", "INT");
        Map<String, String> col2 = new HashMap<>();
        col2.put("name", "name");
        col2.put("type", "VARCHAR(50)");

        columns.add(col1);
        columns.add(col2);
        table.put("columns", columns);

        schema.setTables(List.of(table)); 

        String result = generator.generate(schema);

        assertTrue(result.contains("CREATE TABLE IF NOT EXISTS users"));
        assertTrue(result.contains("PRIMARY KEY (user_id)"));
        assertTrue(result.contains("SET FOREIGN_KEY_CHECKS=0;"));
        assertTrue(result.contains("SET FOREIGN_KEY_CHECKS=1;"));
    }

    @Test
    public void testGenerate_Null() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            generator.generate(null);
        });
        assertEquals("SchemaModel cannot be null", ex.getMessage());
    }

    @Test
    public void testGenerate_Empty() {
        SchemaModel schema = new SchemaModel();
        schema.setTables(Collections.emptyList());

        String result = generator.generate(schema);

        assertTrue(result.contains("SET FOREIGN_KEY_CHECKS=0;"));
        assertTrue(result.contains("SET FOREIGN_KEY_CHECKS=1;"));
        assertFalse(result.contains("CREATE TABLE"));
    }
}
