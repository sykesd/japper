package org.dt.japper;

/*
 * Copyright (c) 2018, David Sykes and Tomasz Orzechowski
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * - Neither the name David Sykes nor Tomasz Orzechowski may be used to endorse
 * or promote products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 *
 */


import java.sql.Connection;
import java.util.List;

import org.dt.japper.testmodel.RecursiveIgnoredPartModel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleRecursiveModelTest {

    private static TestData testData;

    @BeforeAll
    public static void setupDB() throws Exception {
        testData = new TestData();
        testData.create();
    }

    @AfterAll
    public static void clearDB() throws Exception {
        if (testData != null) testData.destroy();
    }

    private static final String SQL_PARTS =
            "   SELECT *"
          + "     FROM part"
          + " ORDER BY partno"
            ;

    @Test
    public void mapPartsIgnoringRecursion() throws Exception {
        Connection conn = testData.connect();

        List<RecursiveIgnoredPartModel> parts = Japper.query(conn, RecursiveIgnoredPartModel.class, SQL_PARTS);
        assertEquals(3, parts.size());

        RecursiveIgnoredPartModel part = parts.get(0);
        assertEquals("123456", part.getPartno());
        assertEquals("FAB", part.getPartType());

        conn.close();
    }

}
