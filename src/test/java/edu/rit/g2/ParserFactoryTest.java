package edu.rit.g2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import edu.rit.g2.factory.ParserFactory;
import edu.rit.g2.parser.JsonSchemaParser;
import edu.rit.g2.parser.SchemaParser;

public class ParserFactoryTest {
    


    // good
    @Test
    void testGet_Json() {
        SchemaParser parser = ParserFactory.get("json");
        assertNotNull(parser);
        assertTrue(parser instanceof JsonSchemaParser, "Expected instance of JsonSchemaParser");
    }

    //bad 1
    @Test
    void testGet_Null() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ParserFactory.get(null);
        });
        assertEquals("Format cannot be null", exception.getMessage());
    }

    // bad 2
    @Test 
    void testGet_UnsupportedFormat() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ParserFactory.get("yaml");
        });
        assertEquals("Unsupported format: yaml", exception.getMessage());
    }
}
