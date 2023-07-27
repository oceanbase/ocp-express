/*
 * oceanbase.com Inc.
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.oceanbase.ocp.core.util;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.oceanbase.ocp.core.property.InitProperties;

public class YamlUtilsTest {

    @Test
    public void dumpAndLoad_okay() throws IOException {
        Path source = Paths.get(this.getClass().getResource("/").getPath());
        Path newFolder = Paths.get(source.toAbsolutePath() + "/test/");
        Files.createDirectories(newFolder);
        File file = new File(newFolder.toFile(), "test.yaml");
        InitProperties info = new InitProperties();
        info.setAgentUsername("whatever");
        info.setAgentPassword("whatever");

        YamlUtils.dump(file, info);

        InitProperties info1 = YamlUtils.loadResourceAs(file, InitProperties.class);
        assertEquals(info.getAgentUsername(), info1.getAgentUsername());
        assertEquals(info.getAgentPassword(), info1.getAgentPassword());
    }

}
