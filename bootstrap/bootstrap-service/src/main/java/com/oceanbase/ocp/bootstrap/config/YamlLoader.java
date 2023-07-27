/*
 * Copyright (c) 2023 OceanBase
 * OCP Express is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.oceanbase.ocp.bootstrap.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import com.oceanbase.ocp.bootstrap.config.env.Const;
import com.oceanbase.ocp.bootstrap.config.env.ELEnv;
import com.oceanbase.ocp.bootstrap.config.env.ELExpr;
import com.oceanbase.ocp.bootstrap.config.env.JavaxELEnv;
import com.oceanbase.ocp.bootstrap.config.env.ResourceFile;
import com.oceanbase.ocp.bootstrap.config.env.ResourceYaml;
import com.oceanbase.ocp.bootstrap.config.env.Transform;
import com.oceanbase.ocp.bootstrap.util.ResourceUtils;

public class YamlLoader {

    private final ELEnv elEnv;

    public YamlLoader() {
        this(new JavaxELEnv());
    }

    public YamlLoader(ELEnv elEnv) {
        this.elEnv = elEnv;
    }

    static class CustomRepresenter extends Representer {

        CustomRepresenter() {
            super(defaultDumpOptions());
            this.representers.put(com.oceanbase.ocp.bootstrap.core.def.Const.class, data -> {
                String s = ((com.oceanbase.ocp.bootstrap.core.def.Const) data).getValue();
                return new ScalarNode(new Tag("!const"), s, null, null, ScalarStyle.PLAIN);
            });
        }

        static DumperOptions defaultDumpOptions() {
            DumperOptions dumperOptions = new DumperOptions();
            dumperOptions.setIndent(4);
            dumperOptions.setWidth(120);
            dumperOptions.setSplitLines(false);
            return dumperOptions;
        }
    }

    Yaml newYaml() {
        // yaml is not thread safe
        Yaml ret = new Yaml(new CustomRepresenter());
        ret.addTypeDescription(new ELExpr(elEnv));
        ret.addTypeDescription(new ResourceFile());
        ret.addTypeDescription(new ResourceYaml());
        ret.addTypeDescription(new Const());
        ret.addTypeDescription(new Transform());
        return ret;
    }

    public <T> T loadResourceAs(String path, Class<? super T> cls) {
        String content = ResourceUtils.loadResource(path);
        try {
            return loadAs(content, cls);
        } catch (Exception e) {
            throw new IllegalStateException("load yaml from resource failed: " + path, e);
        }
    }

    public <T> T loadAs(String content, Class<? super T> cls) {
        return newYaml().loadAs(content, cls);
    }

    @SuppressWarnings("unchecked")
    public <T> T load(String content) {
        return (T) loadAs(content, Object.class);
    }

    public String toYaml(Object o) {
        return newYaml().dump(o);
    }

}
