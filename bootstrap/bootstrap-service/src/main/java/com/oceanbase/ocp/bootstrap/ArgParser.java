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

package com.oceanbase.ocp.bootstrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.oceanbase.ocp.bootstrap.Params.PropertyPair;
import com.oceanbase.ocp.bootstrap.core.Action;

public class ArgParser {

    public ArgParser() {}

    /**
     * --help : output help info <br>
     * --bootstrap : open upgrade mode <br>
     * P.S. All args below only take effect under bootstrap mode <br>
     * --interactive : Interactive mode. <br>
     * --port=<http-port> </http-port>: Listen port, default value is 8180, and this
     * port will reused by OCP Express server <br>
     * --meta-address=<meta-address></meta-address> : Data source address (with url
     * and port) <br>
     * --meta-database=<meta-database></meta-database> : Data source database name.
     * <br>
     * --meta-password=<meta-password>/meta-password>* : Data source password <br>
     * --with-property=<name1>:<value1>,<name2>:<value2> : Specify OCP init
     * properties<br>
     */

    static String optHelp = "help";
    static String optBootStrap = "bootstrap";
    static String optPort = "port";
    static String optAuth = "auth";
    static String optInstall = "install";
    static String optUpgrade = "upgrade";
    static String optProgressLogPath = "progress-log";
    static String optMetaAddress = "meta-address";
    static String optMetaDatabase = "meta-database";
    static String optMetaUser = "meta-user";
    static String optMetaPassword = "meta-password";
    static String optMetaPubKey = "meta-pub-key";

    static String optWithProperty = "with-property";

    static Options options = new Options();
    static {
        options.addOption(Option.builder("h").longOpt(optHelp).desc("help").build());
        options.addOption(Option.builder().longOpt(optBootStrap).desc("upgrading mode").build());
        options.addOption(
                Option.builder().longOpt(optPort).hasArg().desc("listened port. 8180 by default").build());
        options.addOption(Option.builder().longOpt(optAuth).hasArg()
                .desc("auth username and password for bootstrap web. e.g. username:password").build());
        options.addOption(Option.builder().longOpt(optInstall).desc("install OCP").build());
        options.addOption(Option.builder().longOpt(optUpgrade).desc("upgrade OCP").build());
        options.addOption(Option.builder().longOpt(optProgressLogPath).hasArg().desc("progress log path").build());
        options.addOption(Option.builder().longOpt(optMetaAddress).hasArg().desc("meta database address").build());
        options.addOption(Option.builder().longOpt(optMetaDatabase).hasArg().desc("meta database name").build());
        options.addOption(Option.builder().longOpt(optMetaUser).hasArg().desc("meta database username").build());
        options.addOption(Option.builder().longOpt(optMetaPassword).hasArg().desc("meta database password").build());
        options.addOption(Option.builder().longOpt(optMetaPubKey).hasArg()
                .desc("meta database password encryption pub key").build());
        options.addOption(Option.builder().longOpt(optWithProperty).hasArg()
                .desc("other necessary configs. like monitor db or elasticsearch configs").build());
    }

    public Params parse(String[] args) {
        CommandLineParser cmdLineParser = new DefaultParser() {

            @Override
            public CommandLine parse(Options options, String[] arguments) throws ParseException {
                final List<String> knownArgs = new ArrayList<>();
                for (String argument : arguments) {
                    String argName = argument.split("=", 2)[0];
                    if (options.hasOption(argName)) {
                        knownArgs.add(argument);
                    }
                }
                return super.parse(options, knownArgs.toArray(new String[0]));
            }
        };

        try {
            CommandLine cmd = cmdLineParser.parse(options, args);
            return toParams(options, cmd);
        } catch (Exception e) {
            System.err.printf("parse option args failed: %s.\nUse -h for usage\n", e.getMessage()); // NOPMD
            System.exit(1);
            return null;
        }
    }

    @Nonnull
    private Params toParams(Options options, CommandLine cmd) {
        Params ret = new Params();
        if (cmd.hasOption(optBootStrap)) {
            ret.enabled = true;
        }

        if (ret.enabled && cmd.hasOption(optInstall) && cmd.hasOption(optUpgrade)) {
            String errMsg = "must specify only one of --install or --upgrade";
            System.err.printf("option action failed: %s.\nUse -h for usage", errMsg); // NOPMD
            System.exit(1);
            return null;
        }
        if (cmd.hasOption(optInstall)) {
            ret.action = Action.INSTALL;
        } else if (cmd.hasOption(optUpgrade)) {
            ret.action = Action.UPGRADE;
        } else {
            ret.action = Action.UNKNOWN;
        }

        if (cmd.hasOption(optAuth)) {
            String[] userPassword = cmd.getOptionValue(optAuth, "").split(":", 2);
            if (userPassword.length == 2) {
                ret.auth = new Auth(userPassword[0], userPassword[1]);
            }
        }
        ret.progressLogPath = System.getProperty("user.home") + "/logs/ocp/bootstrap.log";
        if (cmd.hasOption(optProgressLogPath)) {
            ret.progressLogPath = cmd.getOptionValue(optProgressLogPath);
        }
        if (cmd.hasOption(optHelp)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("BootStrap", options, true);
            System.exit(0);
            return null;
        }

        if (cmd.hasOption(optPort)) {
            String portStr = cmd.getOptionValue(optPort, "-1");
            try {
                ret.port = Integer.parseInt(portStr);
            } catch (Exception e) {
                System.err.printf("option port parseInt failed: %s.\nUse -h for usage\n", e.getMessage()); // NOPMD
                System.exit(1);
                return null;
            }
        } else {
            ret.port = -1;
        }

        if (cmd.hasOption(optMetaAddress)) {
            ret.metaAddress = cmd.getOptionValue(optMetaAddress);
        }

        if (cmd.hasOption(optMetaDatabase)) {
            ret.metaDatabase = cmd.getOptionValue(optMetaDatabase);
        }

        if (cmd.hasOption(optMetaUser)) {
            ret.metaUsername = cmd.getOptionValue(optMetaUser);
        }

        if (cmd.hasOption(optMetaPassword)) {
            ret.metaPassword = cmd.getOptionValue(optMetaPassword);
        }

        if (cmd.hasOption(optMetaPubKey)) {
            ret.metaPubKey = cmd.getOptionValue(optMetaPubKey);
        }

        if (cmd.hasOption(optWithProperty)) {
            String[] properties = cmd.getOptionValues(optWithProperty);
            List<PropertyPair> propertyPairs = new ArrayList<>();
            for (String property : properties) {
                String[] splitProperty = property.split(":", 2);
                if (splitProperty.length == 2) {
                    propertyPairs.add(new PropertyPair(splitProperty[0], splitProperty[1]));
                }
            }
            ret.configProperties = propertyPairs;
        } else {
            ret.configProperties = Collections.emptyList();
        }
        return ret;
    }
}
