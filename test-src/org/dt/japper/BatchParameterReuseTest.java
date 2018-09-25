package org.dt.japper;

import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;

import static org.dt.japper.Japper.paramLists;
import static org.dt.japper.Japper.params;
import static org.junit.Assert.assertEquals;

public class BatchParameterReuseTest {

    private static TestData testData;

    @BeforeClass
    public static void setupDB() throws Exception {
        testData = new TestData();
        testData.create();
    }

    @Test
    public void insertBatch() throws Exception {
        String sql =
                " insert into part(partno, description, part_type, dyn_field) " +
                        " values(:PARTNO, :DESCRIPTION, :PARTNO, :DYN_FIELD) "
                ;

        Connection conn = testData.connect();
        try {
            int rowsAffected = Japper.executeBatch(conn, sql, paramLists(
                    params("PARTNO", "ZZ1", "DESCRIPTION", "Batch part 1", "PART_TYPE", "FAB", "DYN_FIELD", "C:30")
                    , params("PARTNO", "ZZ2", "PART_TYPE", "BUY", "DESCRIPTION", "Batch part 2")
            ));
            assertEquals(2, rowsAffected);
        }
        finally {
            try { conn.close(); } catch (Exception ignored) {}
        }
    }
}
