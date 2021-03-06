/*
 * Copyright (C) 2012 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.rvesse.airline;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.rvesse.airline.Cli;
import com.github.rvesse.airline.SingleCommand;
import com.github.rvesse.airline.builder.CliBuilder;
import com.github.rvesse.airline.Git.Add;
import com.github.rvesse.airline.Git.RemoteAdd;
import com.github.rvesse.airline.Git.RemoteShow;
import com.github.rvesse.airline.args.Args1;
import com.github.rvesse.airline.args.Args2;
import com.github.rvesse.airline.args.ArgsAllowedValues;
import com.github.rvesse.airline.args.ArgsArityString;
import com.github.rvesse.airline.args.ArgsBooleanArity;
import com.github.rvesse.airline.args.ArgsExitCodes;
import com.github.rvesse.airline.args.ArgsInherited;
import com.github.rvesse.airline.args.ArgsMultiLineDescription;
import com.github.rvesse.airline.args.ArgsMultiParagraphDiscussion;
import com.github.rvesse.airline.args.ArgsRequired;
import com.github.rvesse.airline.args.CommandHidden;
import com.github.rvesse.airline.args.GlobalOptionsHidden;
import com.github.rvesse.airline.args.OptionsHidden;
import com.github.rvesse.airline.args.OptionsRequired;
import com.github.rvesse.airline.command.CommandRemove;
import com.github.rvesse.airline.help.Help;
import com.github.rvesse.airline.help.UsageHelper;
import com.github.rvesse.airline.help.cli.CliCommandUsageGenerator;
import com.github.rvesse.airline.help.cli.CliGlobalUsageSummaryGenerator;
import com.github.rvesse.airline.help.ronn.RonnCommandUsageGenerator;
import com.github.rvesse.airline.help.ronn.RonnGlobalUsageGenerator;
import com.github.rvesse.airline.help.ronn.RonnMultiPageGlobalUsageGenerator;
import com.github.rvesse.airline.model.CommandMetadata;
import com.google.common.collect.ImmutableList;

import org.testng.Assert;
import org.testng.annotations.Test;

import static com.github.rvesse.airline.SingleCommand.singleCommand;
import static com.google.common.base.Predicates.compose;
import static com.google.common.base.Predicates.equalTo;
import static com.google.common.collect.Iterables.find;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test
@SuppressWarnings("unchecked")
public class TestHelp {
    private final Charset utf8 = Charset.forName("utf-8");

    /**
     * Helper method for if you're trying to determine the differences betweeon
     * actual and expected output when debugging a new test and can't visually
     * see the difference e.g. differing white space
     * 
     * @param actual
     *            Actual
     * @param expected
     *            Expected
     */
    private void testStringAssert(String actual, String expected) {
        if (!actual.equals(expected)) {
            if (actual.length() != expected.length()) {
                System.err.println("Different lengths, expected " + expected.length() + " but got " + actual.length());
            }
            for (int i = 0; i < expected.length(); i++) {
                char e = expected.charAt(i);
                if (i >= actual.length()) {
                    System.err.println("Expected character '" + e + "' (Code " + (int) e + ") is at position " + i
                            + " which is beyond the length of the actual string");
                    break;
                }
                char a = actual.charAt(i);
                if (e != a) {
                    System.err.println("Expected character '" + e + "' (Code " + (int) e + ") at position " + i
                            + " does not match actual character '" + a + "' (Code " + (int) a + ")");
                    int start = Math.max(0, i - 10);
                    int end = Math.min(expected.length(), i + 10);
                    System.err.println("Expected Context:");
                    System.err.println(expected.substring(start, end));
                    System.err.println("Actual Context:");
                    end = Math.min(actual.length(), i + 10);
                    System.err.println(actual.substring(start, end));
                    break;
                }
            }
        }
        assertEquals(actual, expected);
    }

    /**
     * Helper method that reads in the given file
     * 
     * @param f
     *            File
     * @return File contents
     * @throws IOException
     */
    private String readFile(File f) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(f));
        String line = reader.readLine();
        StringBuilder builder = new StringBuilder();
        while (line != null) {
            builder.append(line).append('\n');
            line = reader.readLine();
        }
        reader.close();
        return builder.toString();
    }

    public void testMultiLineDescriptions() throws IOException {
        SingleCommand<ArgsMultiLineDescription> cmd = singleCommand(ArgsMultiLineDescription.class);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Help.help(cmd.getCommandMetadata(), out);
        //@formatter:off
        assertEquals(new String(out.toByteArray(), utf8), 
                "NAME\n" +
                "        ArgsMultiLineDescription - Has\n" +
                "        some\n" +
                "        new lines\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        ArgsMultiLineDescription [ -v ]\n" +
                "\n" +
                "OPTIONS\n" + 
                "        -v\n" +
                "            Verbose descriptions\n" +
                "            have new lines\n" +
                "\n");
        //@formatter:on
    }
    
    public void testMultiParagraphDiscussion() throws IOException {
        SingleCommand<ArgsMultiParagraphDiscussion> cmd = singleCommand(ArgsMultiParagraphDiscussion.class);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Help.help(cmd.getCommandMetadata(), out);
        //@formatter:off
        testStringAssert(new String(out.toByteArray(), utf8), 
                "NAME\n" +
                "        ArgsMultiParagraphDiscussion -\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        ArgsMultiParagraphDiscussion\n" +
                "\n" +
                "DISCUSSION\n" +
                "        First paragraph\n" +
                "\n" +
                "        Middle paragraph\n" +
                "\n" +
                "        Final paragraph\n" + 
                "\n");
        //@formatter:on
    }
    
    public void testMultiParagraphDiscussionRonn() throws IOException {
        SingleCommand<ArgsMultiParagraphDiscussion> cmd = singleCommand(ArgsMultiParagraphDiscussion.class);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RonnCommandUsageGenerator generator = new RonnCommandUsageGenerator();
        generator.usage(null, null, "ArgsMultiParagraphDiscussion", cmd.getCommandMetadata(), out);
        //@formatter:off
        testStringAssert(new String(out.toByteArray(), utf8), 
                "ArgsMultiParagraphDiscussion(1) -- null\n" +
                "==========\n" +
                "\n" +
                "## SYNOPSIS\n" +
                "\n" +
                " `ArgsMultiParagraphDiscussion` \n" +
                "\n" +
                "## DISCUSSION\n" +
                "\n" +
                "First paragraph\n" +
                "\n" +
                "Middle paragraph\n" +
                "\n" +
                "Final paragraph\n" + 
                "\n");
        //@formatter:on
    }

    public void testGit() throws IOException {
        //@formatter:off
        CliBuilder<Runnable> builder = Cli.<Runnable>builder("git")
                .withDescription("the stupid content tracker")
                .withDefaultCommand(Help.class)
                .withCommand(Help.class)
                .withCommand(Add.class);

        builder.withGroup("remote")
                .withDescription("Manage set of tracked repositories")
                .withDefaultCommand(RemoteShow.class)
                .withCommand(RemoteShow.class)
                .withCommand(RemoteAdd.class);

        Cli<Runnable> gitParser = builder.build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Help.help(gitParser.getMetadata(), ImmutableList.<String>of(), out);
        assertEquals(new String(out.toByteArray(), utf8), 
                "usage: git [ -v ] <command> [ <args> ]\n" +
                "\n" +
                "Commands are:\n" +
                "    add      Add file contents to the index\n" +
                "    help     Display help information\n" +
                "    remote   Manage set of tracked repositories\n" +
                "\n" +
                "See 'git help <command>' for more information on a specific command.\n");

        out = new ByteArrayOutputStream();
        Help.help(gitParser.getMetadata(), ImmutableList.of("add"), out);
        assertEquals(new String(out.toByteArray(), utf8), 
                "NAME\n" +
                "        git add - Add file contents to the index\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        git [ -v ] add [ -i ] [--] [ <patterns>... ]\n" +
                "\n" +
                "OPTIONS\n" +
                "        -i\n" +
                "            Add modified contents interactively.\n" +
                "\n" +
                "        -v\n" +
                "            Verbose mode\n" +
                "\n" +
                "        --\n" +
                "            This option can be used to separate command-line options from the\n" +
                "            list of argument, (useful when arguments might be mistaken for\n" +
                "            command-line options)\n" +
                "\n" +
                "        <patterns>\n" +
                "            Patterns of files to be added\n" +
                "\n");

        out = new ByteArrayOutputStream();
        Help.help(gitParser.getMetadata(), ImmutableList.of("remote"), out);
        assertEquals(new String(out.toByteArray(), utf8), 
                "NAME\n" +
                "        git remote - Manage set of tracked repositories\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        git [ -v ] remote { add | show* } [--] [cmd-options] <cmd-args>\n" +
                "\n" +
                "        Where command-specific options [cmd-options] are:\n" +
                "            add: [ -t <branch> ]\n" +
                "            show: [ -n ]\n" +
                "\n" +
                "        Where command-specific arguments <cmd-args> are:\n" +
                "            add: [ <name> <url>... ]\n" +
                "            show: [ <remote> ]\n" +
                "\n" +
                "        * show is the default command\n" +
                "        See 'git help remote <command>' for more information on a specific command.\n" +
                "OPTIONS\n" +
                "        -v\n" +
                "            Verbose mode\n" +
                "\n");
        
        out = new ByteArrayOutputStream();
        Help.help(gitParser.getMetadata(), ImmutableList.of("remote", "add"), out);
        assertEquals(new String(out.toByteArray(), utf8), 
                "NAME\n" +
                "        git remote add - Adds a remote\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        git [ -v ] remote add [ -t <branch> ] [--] [ <name> <url>... ]\n" +
                "\n" +
                "OPTIONS\n" +
                "        -t <branch>\n" +
                "            Track only a specific branch\n" +
                "\n" +
                "        -v\n" +
                "            Verbose mode\n" +
                "\n" +
                "        --\n" +
                "            This option can be used to separate command-line options from the\n" +
                "            list of argument, (useful when arguments might be mistaken for\n" +
                "            command-line options)\n" +
                "\n" +
                "        <name> <url>\n" +
                "            Name and URL of remote repository to add\n" +
                "\n"
                );
        //@formatter:on
    }

    @Test
    public void testArgs1() throws IOException {
        //@formatter:off
        CliBuilder<Object> builder = Cli.builder("test")
                .withDescription("Test commandline")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class,
                        Args1.class);

        Cli<Object> parser = builder.build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Help.help(parser.getMetadata(), ImmutableList.of("Args1"), out);
        testStringAssert(new String(out.toByteArray(), utf8),
                "NAME\n" +
                "        test Args1 - args1 description\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        test Args1 [ -bigdecimal <bigd> ] [ -date <date> ] [ -debug ]\n" +
                "                [ -double <doub> ] [ -float <floa> ] [ -groups <groups> ]\n" +
                "                [ {-log | -verbose} <verbose> ] [ -long <l> ] [--] [ <parameters>... ]\n" +
                "\n" +
                "OPTIONS\n" +
                "        -bigdecimal <bigd>\n" +
                "            A BigDecimal number\n" +
                "\n" +
                "        -date <date>\n" +
                "            An ISO 8601 formatted date.\n" +
                "\n" +
                "        -debug\n" +
                "            Debug mode\n" +
                "\n" +
                "        -double <doub>\n" +
                "            A double number\n" +
                "\n" +
                "        -float <floa>\n" +
                "            A float number\n" +
                "\n" +
                "        -groups <groups>\n" +
                "            Comma-separated list of group names to be run\n" +
                "\n" +
                "        -log <verbose>, -verbose <verbose>\n" +
                "            Level of verbosity\n" +
                "\n" +
                "        -long <l>\n" +
                "            A long number\n" +
                "\n" +
                "        --\n" +
                "            This option can be used to separate command-line options from the\n" +
                "            list of argument, (useful when arguments might be mistaken for\n" +
                "            command-line options)\n" +
                "\n" +
                "        <parameters>\n" +
                "\n" +
                "\n");
        //@formatter:on
    }

    @Test
    public void testArgs2() throws IOException {
        //@formatter:off
        CliBuilder<Object> builder = Cli.builder("test")
                .withDescription("Test commandline")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class,
                        Args2.class);

        Cli<Object> parser = builder.build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Help.help(parser.getMetadata(), ImmutableList.of("Args2"), out);
        assertEquals(new String(out.toByteArray(), utf8), 
                "NAME\n" +
                "        test Args2 -\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        test Args2 [ -debug ] [ -groups <groups> ] [ -host <hosts>... ]\n" +
                "                [ {-log | -verbose} <verbose> ] [--] [ <parameters>... ]\n" +
                "\n" +
                "OPTIONS\n" +
                "        -debug\n" +
                "            Debug mode\n" +
                "\n" +
                "        -groups <groups>\n" +
                "            Comma-separated list of group names to be run\n" +
                "\n" +
                "        -host <hosts>\n" +
                "            The host\n" +
                "\n" +
                "        -log <verbose>, -verbose <verbose>\n" +
                "            Level of verbosity\n" +
                "\n" +
                "        --\n" +
                "            This option can be used to separate command-line options from the\n" +
                "            list of argument, (useful when arguments might be mistaken for\n" +
                "            command-line options)\n" +
                "\n" +
                "        <parameters>\n" +
                "            List of parameters\n" +
                "\n");
        //@formatter:on
    }

    @Test
    public void testArgsAllowedValues() throws IOException {
        //@formatter:off
        SingleCommand<ArgsAllowedValues> command = singleCommand(ArgsAllowedValues.class);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new CliCommandUsageGenerator().usage("test", null, command.getCommandMetadata().getName(), command.getCommandMetadata(), out);
        assertEquals(new String(out.toByteArray(), utf8),
                "NAME\n" +
                "        test ArgsAllowedValues - ArgsAllowedValues description\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        test ArgsAllowedValues [ -mode <mode> ]\n" +
                "\n" +
                "OPTIONS\n" +
                "        -mode <mode>\n" +
                "            A string from a restricted set of values\n" +
                "\n" +
                "            This options value is restricted to the following value(s):\n" +
                "                a\n" +
                "                b\n" +
                "                c\n" +
                "\n");
        
        //@formatter:on
    }

    @Test
    public void testArgsAllowedValuesRonn() throws IOException {
        //@formatter:off
        SingleCommand<ArgsAllowedValues> command = singleCommand(ArgsAllowedValues.class);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new RonnCommandUsageGenerator().usage("test", null, command.getCommandMetadata().getName(), command.getCommandMetadata(), out);
        assertEquals(new String(out.toByteArray(), utf8),
                "test-ArgsAllowedValues(1) -- ArgsAllowedValues description\n" +
                "==========\n" +
                "\n" +
                "## SYNOPSIS\n" +
                "\n" +
                "`test` `ArgsAllowedValues` [ -mode <mode> ]\n" +
                "\n" +
                "## OPTIONS\n" +
                "\n" +
                "* `-mode` <mode>:\n" +
                "A string from a restricted set of values\n" +
                "\n" +
                "  This options value is restricted to the following value(s): [a, b, c]");
        
        //@formatter:on
    }

    @Test
    public void testArgsAritySting() throws IOException {
        //@formatter:off
        CliBuilder<Object> builder = Cli.builder("test")
                .withDescription("Test commandline")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class,
                        ArgsArityString.class);

        Cli<Object> parser = builder.build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Help.help(parser.getMetadata(), ImmutableList.of("ArgsArityString"), out);
        assertEquals(new String(out.toByteArray(), utf8),
                "NAME\n" +
                "        test ArgsArityString -\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        test ArgsArityString [ -pairs <pairs>... ] [--] [ <rest>... ]\n" +
                "\n" +
                "OPTIONS\n" +
                "        -pairs <pairs>\n" +
                "            Pairs\n" +
                "\n" +
                "        --\n" +
                "            This option can be used to separate command-line options from the\n" +
                "            list of argument, (useful when arguments might be mistaken for\n" +
                "            command-line options)\n" +
                "\n" +
                "        <rest>\n" +
                "            Rest\n" +
                "\n");
        //@formatter:on
    }

    @Test
    public void testArgsBooleanArity() throws IOException {
        //@formatter:off
        CliBuilder<Object> builder = Cli.builder("test")
                .withDescription("Test commandline")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class,
                        ArgsBooleanArity.class);

        Cli<Object> parser = builder.build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Help.help(parser.getMetadata(), ImmutableList.of("ArgsBooleanArity"), out);
        assertEquals(new String(out.toByteArray(), utf8),
                "NAME\n" +
                "        test ArgsBooleanArity -\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        test ArgsBooleanArity [ -debug <debug> ]\n" +
                "\n" +
                "OPTIONS\n" +
                "        -debug <debug>\n" +
                "\n" +
                "\n");
        //@formatter:on
    }

    @Test
    public void testArgsInherited() throws IOException {
        //@formatter:off
        CliBuilder<Object> builder = Cli.builder("test")
                .withDescription("Test commandline")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class,
                        ArgsInherited.class);

        Cli<Object> parser = builder.build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Help.help(parser.getMetadata(), ImmutableList.of("ArgsInherited"), out);
        assertEquals(new String(out.toByteArray(), utf8), "NAME\n" +
                "        test ArgsInherited -\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        test ArgsInherited [ -child <child> ] [ -debug ] [ -groups <groups> ]\n" +
                "                [ -level <level> ] [ -log <log> ] [--] [ <parameters>... ]\n" +
                "\n" +
                "OPTIONS\n" +
                "        -child <child>\n" +
                "            Child parameter\n" +
                "\n" +
                "        -debug\n" +
                "            Debug mode\n" +
                "\n" +
                "        -groups <groups>\n" +
                "            Comma-separated list of group names to be run\n" +
                "\n" +
                "        -level <level>\n" +
                "            A long number\n" +
                "\n" +
                "        -log <log>\n" +
                "            Level of verbosity\n" +
                "\n" +
                "        --\n" +
                "            This option can be used to separate command-line options from the\n" +
                "            list of argument, (useful when arguments might be mistaken for\n" +
                "            command-line options)\n" +
                "\n" +
                "        <parameters>\n" +
                "\n" +
                "\n");
        //@formatter:on
    }

    @Test
    public void testArgsRequired() throws IOException {
        //@formatter:off
        CliBuilder<Object> builder = Cli.builder("test")
                .withDescription("Test commandline")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class,
                        ArgsRequired.class);

        Cli<Object> parser = builder.build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Help.help(parser.getMetadata(), ImmutableList.of("ArgsRequired"), out);
        assertEquals(new String(out.toByteArray(), utf8),
                "NAME\n" +
                "        test ArgsRequired -\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        test ArgsRequired [--] <parameters>...\n" +
                "\n" +
                "OPTIONS\n" +
                "        --\n" +
                "            This option can be used to separate command-line options from the\n" +
                "            list of argument, (useful when arguments might be mistaken for\n" +
                "            command-line options)\n" +
                "\n" +
                "        <parameters>\n" +
                "            List of files\n" +
                "\n");
        //@formatter:on
    }

    @Test
    public void testOptionsRequired() throws IOException {
        //@formatter:off
        CliBuilder<Object> builder = Cli.builder("test")
                .withDescription("Test commandline")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class,
                        OptionsRequired.class);

        Cli<Object> parser = builder.build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Help.help(parser.getMetadata(), ImmutableList.of("OptionsRequired"), out);
        assertEquals(new String(out.toByteArray(), utf8),
                "NAME\n" +
                "        test OptionsRequired -\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        test OptionsRequired [ --optional <optionalOption> ]\n" +
                "                --required <requiredOption>\n" +
                "\n" +
                "OPTIONS\n" +
                "        --optional <optionalOption>\n" +
                "\n" +
                "\n" +
                "        --required <requiredOption>\n" +
                "\n" +
                "\n");
        //@formatter:on
    }

    @Test
    public void testOptionsHidden01() throws IOException {
        //@formatter:off
        CliBuilder<Object> builder = Cli.builder("test")
                .withDescription("Test commandline")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class,
                        OptionsHidden.class);

        Cli<Object> parser = builder.build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Help.help(parser.getMetadata(), ImmutableList.of("OptionsHidden"), out);
        assertEquals(new String(out.toByteArray(), utf8), 
                "NAME\n" +
                "        test OptionsHidden -\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        test OptionsHidden [ --optional <optionalOption> ]\n" +
                "\n" +
                "OPTIONS\n" +
                "        --optional <optionalOption>\n" +
                "\n" +
                "\n");
        //@formatter:on
    }
    
    @Test
    public void testOptionsHidden02() throws IOException {
        //@formatter:off
        CliBuilder<Object> builder = Cli.builder("test")
                .withDescription("Test commandline")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class,
                        OptionsHidden.class);

        Cli<Object> parser = builder.build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CliCommandUsageGenerator generator = new CliCommandUsageGenerator(true);
        CommandMetadata metadata = find(parser.getMetadata().getDefaultGroupCommands(), compose(equalTo("OptionsHidden"), CommandMetadata.nameGetter()), null);
        Assert.assertNotNull(metadata);
        generator.usage("test", null, "OptionsHidden", metadata, out);
        
        assertEquals(new String(out.toByteArray(), utf8), 
                "NAME\n" +
                "        test OptionsHidden -\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        test OptionsHidden [ --hidden <hiddenOption> ]\n" +
                "                [ --optional <optionalOption> ]\n" +
                "\n" +
                "OPTIONS\n" +
                "        --hidden <hiddenOption>\n" +
                "\n\n" +
                "        --optional <optionalOption>\n" +
                "\n" +
                "\n");
        //@formatter:on
    }

    @Test
    public void testGlobalOptionsHidden() throws IOException {
        //@formatter:off
        CliBuilder<Object> builder = Cli.builder("test")
                .withDescription("Test commandline")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class,
                        GlobalOptionsHidden.class);

        Cli<Object> parser = builder.build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Help.help(parser.getMetadata(), ImmutableList.of("GlobalOptionsHidden"), out);
        assertEquals(new String(out.toByteArray(), utf8),
                "NAME\n" +
                "        test GlobalOptionsHidden -\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        test [ {-op | --optional} ] GlobalOptionsHidden\n" +
                "\n" +
                "OPTIONS\n" +
                "        -op, --optional\n" +
                "\n" +
                "\n");
        //@formatter:on
    }

    @Test
    public void testCommandHidden() throws IOException {
        //@formatter:off
        CliBuilder<Object> builder = Cli.builder("test")
                .withDescription("Test commandline")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class,
                        ArgsRequired.class, CommandHidden.class);

        Cli<Object> parser = builder.build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Help.help(parser.getMetadata(), ImmutableList.<String>of(), out);
        assertEquals(new String(out.toByteArray(), utf8),
                "usage: test <command> [ <args> ]\n" +
                "\n" +
                "Commands are:\n" +
                "    ArgsRequired\n" +
                "    help           Display help information\n" +
                "\n" +
                "See 'test help <command>' for more information on a specific command.\n");

        out = new ByteArrayOutputStream();
        Help.help(parser.getMetadata(), ImmutableList.of("CommandHidden"), out);
        assertEquals(new String(out.toByteArray(), utf8),
                "NAME\n" +
                "        test CommandHidden -\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        test CommandHidden [ --optional <optionalOption> ]\n" +
                "\n" +
                "OPTIONS\n" +
                "        --optional <optionalOption>\n" +
                "\n" +
                "\n");
        //@formatter:on
    }

    @Test
    public void testGroups() throws IOException {
        //@formatter:off
        CliBuilder<Object> builder = Cli.builder("test")
                .withDescription("Test commandline")
                .withCommand(Help.class);
        builder.withGroup("visible")
               .withDescription("Visible group")
               .withCommands(ArgsRequired.class);

        Cli<Object> parser = builder.build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Help.help(parser.getMetadata(), ImmutableList.<String>of(), out);
        Assert.assertEquals(new String(out.toByteArray(), utf8),
                "usage: test <command> [ <args> ]\n" +
                "\n" +
                "Commands are:\n" +
                "    help      Display help information\n" +
                "    visible   Visible group\n" +
                "\n" +
                "See 'test help <command>' for more information on a specific command.\n");
        //@formatter:on
    }
    
    @Test
    public void testGroupsHidden01() throws IOException {
        //@formatter:off
        CliBuilder<Object> builder = Cli.builder("test")
                .withDescription("Test commandline")
                .withCommand(Help.class);
        builder.withGroup("visible")
               .withDescription("Visible group")
               .withCommands(ArgsRequired.class);
        builder.withGroup("hidden")
               .withDescription("Hidden group")
               .withCommands(ArgsRequired.class)
               .makeHidden();

        Cli<Object> parser = builder.build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Help.help(parser.getMetadata(), ImmutableList.<String>of(), out);
        assertEquals(new String(out.toByteArray(), utf8),
                "usage: test <command> [ <args> ]\n" +
                "\n" +
                "Commands are:\n" +
                "    help      Display help information\n" +
                "    visible   Visible group\n" +
                "\n" +
                "See 'test help <command>' for more information on a specific command.\n");
        //@formatter:on
    }
    
    @Test
    public void testGroupsHidden02() throws IOException {
        //@formatter:off
        CliBuilder<Object> builder = Cli.builder("test")
                .withDescription("Test commandline")
                .withCommand(Help.class);
        builder.withGroup("visible")
               .withDescription("Visible group")
               .withCommands(ArgsRequired.class);
        builder.withGroup("hidden")
               .withDescription("Hidden group")
               .withCommands(ArgsRequired.class)
               .makeHidden();

        Cli<Object> parser = builder.build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CliGlobalUsageSummaryGenerator generator = new CliGlobalUsageSummaryGenerator(true);
        generator.usage(parser.getMetadata(), out);
        assertEquals(new String(out.toByteArray(), utf8),
                "usage: test <command> [ <args> ]\n" +
                "\n" +
                "Commands are:\n" +
                "    help      Display help information\n" +
                "    hidden    Hidden group\n" +
                "    visible   Visible group\n" +
                "\n" +
                "See 'test help <command>' for more information on a specific command.\n");
        //@formatter:on
    }

    @Test
    public void testExamplesAndDiscussion() throws IOException {
        //@formatter:off
        Cli<?> parser = Cli.builder("git")
            .withCommand(CommandRemove.class)
            .build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Help.help(parser.getMetadata(), ImmutableList.<String>of("remove"), out);

        String discussion = "DISCUSSION\n" +
        "        More details about how this removes files from the index.\n" +
        "\n";

        String examples = "EXAMPLES\n" +
        "        * The following is a usage example:\n\n" +
        "        \t$ git remove -i myfile.java\n";

        String usage = new String(out.toByteArray(), utf8);
        assertTrue(usage.contains(discussion), "Expected the discussion section to be present in the help");
        assertTrue(usage.contains(examples), "Expected the examples section to be present in the help");
        //@formatter:on
    }

    @Test
    public void testSingleCommandArgs1() throws IOException {
        //@formatter:off
        SingleCommand<Args1> command = singleCommand(Args1.class);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new CliCommandUsageGenerator().usage(null, null, "test", command.getCommandMetadata(), out);
        assertEquals(new String(out.toByteArray(), utf8),
                "NAME\n" +
                "        test - args1 description\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        test [ -bigdecimal <bigd> ] [ -date <date> ] [ -debug ]\n" +
                "                [ -double <doub> ] [ -float <floa> ] [ -groups <groups> ]\n" +
                "                [ {-log | -verbose} <verbose> ] [ -long <l> ] [--] [ <parameters>... ]\n" +
                "\n" +
                "OPTIONS\n" +
                "        -bigdecimal <bigd>\n" +
                "            A BigDecimal number\n" +
                "\n" +
                "        -date <date>\n" +
                "            An ISO 8601 formatted date.\n" +
                "\n" +
                "        -debug\n" +
                "            Debug mode\n" +
                "\n" +
                "        -double <doub>\n" +
                "            A double number\n" +
                "\n" +
                "        -float <floa>\n" +
                "            A float number\n" +
                "\n" +
                "        -groups <groups>\n" +
                "            Comma-separated list of group names to be run\n" +
                "\n" +
                "        -log <verbose>, -verbose <verbose>\n" +
                "            Level of verbosity\n" +
                "\n" +
                "        -long <l>\n" +
                "            A long number\n" +
                "\n" +
                "        --\n" +
                "            This option can be used to separate command-line options from the\n" +
                "            list of argument, (useful when arguments might be mistaken for\n" +
                "            command-line options)\n" +
                "\n" +
                "        <parameters>\n" +
                "\n" +
                "\n");
        //@formatter:on
    }

    @Test
    public void testRonn() throws IOException {
        //@formatter:off
        CliBuilder<Runnable> builder = Cli.<Runnable>builder("git")
                .withDescription("the stupid content tracker")
                .withDefaultCommand(Help.class)
                .withCommand(Help.class)
                .withCommand(Add.class);

        builder.withGroup("remote")
                .withDescription("Manage set of tracked repositories")
                .withDefaultCommand(RemoteShow.class)
                .withCommand(RemoteShow.class)
                .withCommand(RemoteAdd.class);

        Cli<Runnable> gitParser = builder.build();
        
        RonnGlobalUsageGenerator generator = new RonnGlobalUsageGenerator();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        generator.usage(gitParser.getMetadata(), out);
        String usage = new String(out.toByteArray(), utf8);
        assertEquals(usage,
                "git(1) -- the stupid content tracker\n" +
                "==========\n" +
                "\n" +
                "## SYNOPSIS\n" +
                "\n" +
                "`git` [ -v ] [<group>] <command> [command-args]\n" +
                "\n" +
                "## GLOBAL OPTIONS\n" +
                "\n" +
                "* `-v`:\n" +
                "Verbose mode\n" +
                "\n" +
                "## COMMAND GROUPS\n" +
                "\n" +
                "Commands are grouped as follows:\n" +
                "\n" +
                "* Default (no <group> specified)\n" +
                "\n" +
                "  * `add`:\n" +
                "  Add file contents to the index\n" +
                "\n" +
                "  * `help`:\n" +
                "  Display help information\n" +
                "\n" +
                "* **remote**\n" +
                "\n" +
                "  Manage set of tracked repositories\n" +
                "\n" +
                "  * `add`:\n" +
                "  Adds a remote\n" +
                "\n" +
                "  * `show`:\n" +
                "  Gives some information about the remote <name>\n" +
                "\n" +
                "---\n" +
                "\n" +
                "## git-add(1)\n" +
                "\n" +
                "### SYNOPSIS\n" +
                "\n" +
                "`git` [ -v ] `add` [ -i ] [--] [ <patterns>... ]\n" +
                "\n" +
                "Add file contents to the index\n" +
                "\n" +
                "### OPTIONS\n" +
                "\n" +
                "* `-i`:\n" +
                "Add modified contents interactively.\n" +
                "\n" +
                "* `-v`:\n" +
                "Verbose mode\n" +
                "\n" +
                "* `--`:\n" +
                "This option can be used to separate command-line options from the list of arguments (useful when arguments might be mistaken for command-line options).\n" +
                "\n" +
                "* <patterns>:\n" +
                "Patterns of files to be added\n" +
                "\n" +
                "---\n" +
                "\n" +
                "## git-help(1)\n" +
                "\n" +
                "### SYNOPSIS\n" +
                "\n" +
                "`git` `help`  [--] [ <command>... ]\n" +
                "\n" +
                "Display help information\n" +
                "\n" +
                "### OPTIONS\n" +
                "\n" +
                "* `--`:\n" +
                "This option can be used to separate command-line options from the list of arguments (useful when arguments might be mistaken for command-line options).\n" +
                "\n" +
                "* <command>:\n" +
                "\n" +
                "\n" +
                "---\n" +
                "\n" +
                "## git-remote-add(1)\n" +
                "\n" +
                "### SYNOPSIS\n" +
                "\n" +
                "`git` [ -v ] `remote` `add` [ -t <branch> ] [--] [ <name> <url>... ]\n" +
                "\n" +
                "Adds a remote\n" +
                "\n" +
                "### OPTIONS\n" +
                "\n" +
                "* `-t` <branch>:\n" +
                "Track only a specific branch\n" +
                "\n" +
                "* `-v`:\n" +
                "Verbose mode\n" +
                "\n" +
                "* `--`:\n" +
                "This option can be used to separate command-line options from the list of arguments (useful when arguments might be mistaken for command-line options).\n" +
                "\n" +
                "* <name> <url>:\n" +
                "Name and URL of remote repository to add\n" +
                "\n" +
                "---\n" +
                "\n" +
                "## git-remote-show(1)\n" +
                "\n" +
                "### SYNOPSIS\n" +
                "\n" +
                "`git` [ -v ] `remote` `show` [ -n ] [--] [ <remote> ]\n" +
                "\n" +
                "Gives some information about the remote <name>\n" +
                "\n" +
                "### OPTIONS\n" +
                "\n" +
                "* `-n`:\n" +
                "Do not query remote heads\n" +
                "\n" +
                "* `-v`:\n" +
                "Verbose mode\n" +
                "\n" +
                "* `--`:\n" +
                "This option can be used to separate command-line options from the list of arguments (useful when arguments might be mistaken for command-line options).\n" +
                "\n" +
                "* <remote>:\n" +
                "Remote to show\n" +
                "\n" +
                "---\n" +
                "\n");
        //@formatter:on
    }

    @Test
    public void testRonnMultiPage() throws IOException {
        //@formatter:off
        CliBuilder<Runnable> builder = Cli.<Runnable>builder("git")
                .withDescription("the stupid content tracker")
                .withDefaultCommand(Help.class)
                .withCommand(Help.class)
                .withCommand(Add.class);

        builder.withGroup("remote")
                .withDescription("Manage set of tracked repositories")
                .withDefaultCommand(RemoteShow.class)
                .withCommand(RemoteShow.class)
                .withCommand(RemoteAdd.class);

        Cli<Runnable> gitParser = builder.build();
        
        RonnGlobalUsageGenerator generator = new RonnMultiPageGlobalUsageGenerator();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        generator.usage(gitParser.getMetadata(), out);
        String usage = new String(out.toByteArray(), utf8);
        assertEquals(usage,
                "git(1) -- the stupid content tracker\n" +
                "==========\n" +
                "\n" +
                "## SYNOPSIS\n" +
                "\n" +
                "`git` [ -v ] [<group>] <command> [command-args]\n" +
                "\n" +
                "## GLOBAL OPTIONS\n" +
                "\n" +
                "* `-v`:\n" +
                "Verbose mode\n" +
                "\n" +
                "## COMMAND GROUPS\n" +
                "\n" +
                "Commands are grouped as follows:\n" +
                "\n" +
                "* Default (no <group> specified)\n" +
                "\n" +
                "  * `git-add(1)`:\n" +
                "  Add file contents to the index\n" +
                "\n" +
                "  * `git-help(1)`:\n" +
                "  Display help information\n" +
                "\n" +
                "* **remote**\n" +
                "\n" +
                "  Manage set of tracked repositories\n" +
                "\n" +
                "  * `git-remote-add(1)`:\n" +
                "  Adds a remote\n" +
                "\n" +
                "  * `git-remote-show(1)`:\n" +
                "  Gives some information about the remote <name>");
        
        File gitHelp = new File("git-help.1.ronn");
        Assert.assertTrue(gitHelp.exists());
        usage = readFile(gitHelp);
        assertEquals(usage,
                "git-help(1) -- Display help information\n" +
                "==========\n" +
                "\n" +
                "## SYNOPSIS\n" +
                "\n" +
                "`git` `help`  [--] [ <command>... ]\n" +
                "\n" +
                "## OPTIONS\n" +
                "\n" +
                "* `--`:\n" +
                "This option can be used to separate command-line options from the list of arguments (useful when arguments might be mistaken for command-line options).\n" +
                "\n" +
                "* <command>:\n" +
                "\n\n" +
                "## GIT\n" +
                "\n" +
                "Part of the `git(1)` suite\n");
        gitHelp.delete();
        
        File gitAdd = new File("git-add.1.ronn");
        Assert.assertTrue(gitAdd.exists());
        usage = readFile(gitAdd);
        assertEquals(usage,
                "git-add(1) -- Add file contents to the index\n" +
                "==========\n" +
                "\n" +
                "## SYNOPSIS\n" +
                "\n" +
                "`git` [ -v ] `add` [ -i ] [--] [ <patterns>... ]\n" +
                "\n" +
                "## OPTIONS\n" +
                "\n" +
                "* `-i`:\n" +
                "Add modified contents interactively.\n" +
                "\n" +
                "* `-v`:\n" +
                "Verbose mode\n" +
                "\n" +
                "* `--`:\n" +
                "This option can be used to separate command-line options from the list of arguments (useful when arguments might be mistaken for command-line options).\n" +
                "\n" +
                "* <patterns>:\n" +
                "Patterns of files to be added\n" +
                "\n" +
                "## GIT\n" +
                "\n" +
                "Part of the `git(1)` suite\n");
        gitAdd.delete();
        
        File gitRemoteShow = new File("git-remote-show.1.ronn");
        Assert.assertTrue(gitRemoteShow.exists());
        usage = readFile(gitRemoteShow);
        assertEquals(usage,
                "git-remote-show(1) -- Gives some information about the remote <name>\n" +
                "==========\n" +
                "\n" +
                "## SYNOPSIS\n" +
                "\n" +
                "`git` [ -v ] `remote` `show` [ -n ] [--] [ <remote> ]\n" +
                "\n" +
                "## OPTIONS\n" +
                "\n" +
                "* `-n`:\n" +
                "Do not query remote heads\n" +
                "\n" +
                "* `-v`:\n" +
                "Verbose mode\n" +
                "\n" +
                "* `--`:\n" +
                "This option can be used to separate command-line options from the list of arguments (useful when arguments might be mistaken for command-line options).\n" +
                "\n" +
                "* <remote>:\n" +
                "Remote to show\n" +
                "\n" +
                "## GIT\n" +
                "\n" +
                "Part of the `git(1)` suite\n");
        gitRemoteShow.delete();
        
        File gitRemoteAdd = new File("git-remote-add.1.ronn");
        Assert.assertTrue(gitRemoteAdd.exists());
        usage = readFile(gitRemoteAdd);
        assertEquals(usage,
                "git-remote-add(1) -- Adds a remote\n" +
                "==========\n" +
                "\n" +
                "## SYNOPSIS\n" +
                "\n" +
                "`git` [ -v ] `remote` `add` [ -t <branch> ] [--] [ <name> <url>... ]\n" +
                "\n" +
                "## OPTIONS\n" +
                "\n" +
                "* `-t` <branch>:\n" +
                "Track only a specific branch\n" +
                "\n" +
                "* `-v`:\n" +
                "Verbose mode\n" +
                "\n" +
                "* `--`:\n" +
                "This option can be used to separate command-line options from the list of arguments (useful when arguments might be mistaken for command-line options).\n" +
                "\n" +
                "* <name> <url>:\n" +
                "Name and URL of remote repository to add\n" +
                "\n" +
                "## GIT\n" +
                "\n" +
                "Part of the `git(1)` suite\n");
        gitRemoteAdd.delete();
        //@formatter:on
    }

    @Test
    public void testExitCodes() throws IOException {
        //@formatter:off
        SingleCommand<ArgsExitCodes> command = singleCommand(ArgsExitCodes.class);
    
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new CliCommandUsageGenerator().usage(null, null, "test", command.getCommandMetadata(), out);
        assertEquals(new String(out.toByteArray(), utf8),
                "NAME\n" +
                "        test - ArgsExitCodes description\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        test\n" +
                "\n" +
                "EXIT STATUS\n" +
                "        The test command exits with one of the following values:\n" +
                "\n" +
                "        0\n" +
                "            Success\n" +
                "        1\n" +
                "\n" +
                "        2\n" +
                "            Error 2\n");
        //@formatter:on
    }

    @Test
    public void testExitCodesRonn() throws IOException {
        //@formatter:off
        SingleCommand<ArgsExitCodes> command = singleCommand(ArgsExitCodes.class);
    
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new RonnCommandUsageGenerator().usage(null, null, "test", command.getCommandMetadata(), out);
        assertEquals(new String(out.toByteArray(), utf8),
                "test(1) -- ArgsExitCodes description\n" +
                "==========\n" +
                "\n" +
                "## SYNOPSIS\n" +
                "\n" +
                " `test` \n" +
                "\n" +
                "## EXIT STATUS\n" +
                "\n" +
                "The `test(1)` command exits with one of the following values:\n" +
                "\n" +
                "* **0** - Success\n" +
                "* **1**\n" +
                "* **2** - Error 2\n");
        //@formatter:on
    }

    @Test
    public void testCommandSorting() {
        //@formatter:off
        CliBuilder<Runnable> builder = Cli.<Runnable>builder("git")
                .withDescription("the stupid content tracker")
                .withDefaultCommand(Help.class)
                .withCommand(Help.class)
                .withCommand(Add.class);

        builder.withGroup("remote")
                .withDescription("Manage set of tracked repositories")
                .withDefaultCommand(RemoteShow.class)
                .withCommand(RemoteShow.class)
                .withCommand(RemoteAdd.class);

        Cli<Runnable> gitParser = builder.build();
        
        List<CommandMetadata> defCommands = new ArrayList<>(gitParser.getMetadata().getDefaultGroupCommands());
        Collections.sort(defCommands, UsageHelper.DEFAULT_COMMAND_COMPARATOR);
        
        Assert.assertEquals(defCommands.get(0).getName(), "add");
        Assert.assertEquals(defCommands.get(1).getName(), "help");
        
        // Check sort is stable
        Collections.sort(defCommands, UsageHelper.DEFAULT_COMMAND_COMPARATOR);
        
        Assert.assertEquals(defCommands.get(0).getName(), "add");
        Assert.assertEquals(defCommands.get(1).getName(), "help");
        
        //@formatter:on
    }
}
