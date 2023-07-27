package com.oceanbase.ocp.bootstrap.config;

import org.junit.Test;

public class ConfigLoaderTest {

    @Test
    public void loadAll() {
        ConfigLoader configLoader = new ConfigLoader();
        AllDataConfig dataConfig = configLoader.loadAll();
        System.out.println(dataConfig);
    }

}
