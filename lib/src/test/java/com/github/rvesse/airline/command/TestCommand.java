/**
 * Copyright (C) 2010 the original author or authors.
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

package com.github.rvesse.airline.command;

import com.github.rvesse.airline.Cli;
import com.github.rvesse.airline.model.CommandMetadata;
import com.github.rvesse.airline.parser.ParseException;
import com.google.common.collect.Lists;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static com.github.rvesse.airline.TestingUtil.singleCommandParser;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static com.google.common.collect.Lists.newArrayList;

public class TestCommand
{
    @Test
    public void namedCommandTest1()
    {
        Cli<?> parser = Cli.builder("git")
                .withCommand(CommandAdd.class)
                .withCommand(CommandCommit.class)
                .build();

        Object command = parser.parse("add", "-i", "A.java");
        assertNotNull(command, "command is null");
        assertTrue(command instanceof CommandAdd);
        CommandAdd add = (CommandAdd) command;
        assertEquals(add.interactive.booleanValue(), true);
        assertEquals(add.patterns, Arrays.asList("A.java"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldComplainIfNoAnnotations()
    {
        singleCommandParser(String.class);
    }

    @Test
    public void commandTest2()
    {
        Cli<?> parser = Cli.builder("git")
                .withCommand(CommandAdd.class)
                .withCommand(CommandCommit.class)
                .build();
        parser.parse("-v", "commit", "--amend", "--author", "cbeust", "A.java", "B.java");

        Object command = parser.parse("-v", "commit", "--amend", "--author", "cbeust", "A.java", "B.java");
        assertNotNull(command, "command is null");
        assertTrue(command instanceof CommandCommit);
        CommandCommit commit = (CommandCommit) command;

        assertTrue(commit.commandMain.verbose);
        assertTrue(commit.amend);
        assertEquals(commit.author, "cbeust");
        assertEquals(commit.files, Arrays.asList("A.java", "B.java"));
    }

    @Test
    public void testExample() {
        Cli<?> parser = Cli.builder("git")
            .withCommand(CommandRemove.class)
            .build();

        final List<CommandMetadata> commandParsers = parser.getMetadata().getDefaultGroupCommands();

        assertEquals(1, commandParsers.size());

        CommandMetadata aMeta = commandParsers.get(0);

        assertEquals("remove", aMeta.getName());

        assertEquals(Lists.newArrayList("* The following is a usage example:",
                                               "\t$ git remove -i myfile.java"), aMeta.getExamples());
    }

    @Test
    public void testDiscussion() {
        Cli<?> parser = Cli.builder("git")
            .withCommand(CommandRemove.class)
            .build();

        final List<CommandMetadata> commandParsers = parser.getMetadata().getDefaultGroupCommands();

        assertEquals(1, commandParsers.size());

        CommandMetadata aMeta = commandParsers.get(0);

        assertEquals("remove", aMeta.getName());

        assertEquals(newArrayList("More details about how this removes files from the index."), aMeta.getDiscussion());
    }

    @Test
    public void testDefaultCommandInGroup() {
        Cli<?> parser = Cli.builder("git")
            .withCommand(CommandAdd.class)
            .withCommand(CommandCommit.class)
            .withDefaultCommand(CommandAdd.class)
            .build();

        Object command = parser.parse("-i", "A.java");

        assertNotNull(command, "command is null");
        assertTrue(command instanceof CommandAdd);
        CommandAdd add = (CommandAdd) command;
        assertEquals(add.interactive.booleanValue(), true);
        assertEquals(add.patterns, Arrays.asList("A.java"));
    }
    
    @Test
    public void testCommandWithArgsSeparator() {
    	Cli<?> parser = Cli.builder("git")
    	                .withCommand(CommandHighArityOption.class)
    	                .build();

        Object command = parser.parse("-v", "cmd", "--option", "val1", "val2", "val3", "val4", "--", "arg1", "arg2", "arg3");
        assertNotNull(command, "command is null");
        assertTrue(command instanceof CommandHighArityOption);
        CommandHighArityOption cmdHighArity = (CommandHighArityOption) command;

        assertTrue(cmdHighArity.commandMain.verbose);
        assertEquals(cmdHighArity.option, Arrays.asList("val1", "val2", "val3", "val4"));
        assertEquals(cmdHighArity.args, Arrays.asList("arg1", "arg2", "arg3"));
    }
    
    @Test
    public void testCommandHighArityOptionNoSeparator() {
    	Cli<?> parser = Cli.builder("git")
    	                .withCommand(CommandHighArityOption.class)
    	                .build();
    	
    	// it should be able to stop parsing option values for --option if it finds another valid option (--option2)
        Object command = parser.parse("-v", "cmd", "--option", "val1", "val2", "val3", "val4", "--option2", "val5", "arg1", "arg2", "arg3");
        assertNotNull(command, "command is null");
        assertTrue(command instanceof CommandHighArityOption);
        CommandHighArityOption cmdHighArity = (CommandHighArityOption) command;

        assertTrue(cmdHighArity.commandMain.verbose);
        assertEquals(cmdHighArity.option, Arrays.asList("val1", "val2", "val3", "val4"));
        assertEquals(cmdHighArity.option2, "val5");
        assertEquals(cmdHighArity.args, Arrays.asList("arg1", "arg2", "arg3"));
    }
    
    @Test
    public void abbreviatedCommands01() {
        Cli<?> parser = Cli.builder("git")
            .withCommand(CommandAdd.class)
            .withCommand(CommandCommit.class)
            .withDefaultCommand(CommandAdd.class)
            .withCommandAbbreviation()
            .build();

        Object command = parser.parse("ad", "-i", "A.java");

        assertNotNull(command, "command is null");
        assertTrue(command instanceof CommandAdd);
        CommandAdd add = (CommandAdd) command;
        assertEquals(add.interactive.booleanValue(), true);
        assertEquals(add.patterns, Arrays.asList("A.java"));
    }
    
    @Test(expectedExceptions = ParseException.class)
    public void abbreviatedCommands02() {
        Cli<?> parser = Cli.builder("git")
            .withCommand(CommandRemove.class)
            .withCommand(CommandRemote.class)
            .withCommandAbbreviation()
            .build();

        // Expect this to error as abbreviation is ambiguous
        parser.parse("rem");
    }
    
    @Test
    public void abbreviatedCommands03() {
        Cli<?> parser = Cli.builder("git")
            .withCommand(CommandRemove.class)
            .withCommand(CommandRemote.class)
            .withCommandAbbreviation()
            .build();

        // Abbreviation is non-ambigious
        Object command = parser.parse("remot");
        
        assertNotNull(command, "command is null");
        assertTrue(command instanceof CommandRemote);
    }
    
    @Test
    public void abbreviatedCommands04() {
        Cli<?> parser = Cli.builder("git")
            .withCommand(CommandRemove.class)
            .withCommand(CommandRemote.class)
            .withCommandAbbreviation()
            .build();

        // Abbreviation is non-ambigious
        Object command = parser.parse("remov");
        
        assertNotNull(command, "command is null");
        assertTrue(command instanceof CommandRemove);
    }
    
    @Test
    public void abbreviatedCommands05() {
        Cli<?> parser = Cli.builder("git")
            .withCommand(CommandRemotes.class)
            .withCommand(CommandRemote.class)
            .withCommandAbbreviation()
            .build();

        // Command name which is also an abbreviation of another command name
        Object command = parser.parse("remote");
        
        assertNotNull(command, "command is null");
        assertTrue(command instanceof CommandRemote);
    }
    
    @Test
    public void abbreviatedOptions01()
    {
        Cli<?> parser = Cli.builder("git")
                .withCommand(CommandCommit.class)
                .withOptionAbbreviation()
                .build();
        
        // Non-ambigious abbreviation of an option
        Object command = parser.parse("commit", "--am");
        
        assertNotNull(command);;
        assertTrue(command instanceof CommandCommit);
        CommandCommit commit = (CommandCommit) command;
        assertTrue(commit.amend);
    }
    
    @Test
    public void abbreviatedOptions02()
    {
        Cli<?> parser = Cli.builder("git")
                .withCommand(CommandCommit.class)
                .withOptionAbbreviation()
                .build();
        
        // Will parse but as abbreviation is ambigious will be treated as an argument and not an option 
        Object command = parser.parse("commit", "--a");
        assertTrue(command instanceof CommandCommit);
        CommandCommit commit = (CommandCommit) command;
        assertFalse(commit.amend);
        assertNull(commit.author);
        assertTrue(commit.files.contains("--a"));
    }
    
    @Test
    public void abbreviatedOptions03()
    {
        Cli<?> parser = Cli.builder("git")
                .withCommand(CommandCommits.class)
                .withOptionAbbreviation()
                .build();
        
        // Option name which is also an abbreviation of another option name
        Object command = parser.parse("commits", "--author", "test");
        
        assertNotNull(command);;
        assertTrue(command instanceof CommandCommits);
        CommandCommits commit = (CommandCommits) command;
        assertEquals(commit.author, "test");
    }
    
    @Test
    public void abbreviatedOptions04()
    {
        Cli<?> parser = Cli.builder("git")
                .withCommand(CommandCommits.class)
                .withOptionAbbreviation()
                .build();
        
        // Option name which is also an abbreviation of another option name
        Object command = parser.parse("commits", "--author", "test");
        
        assertNotNull(command);;
        assertTrue(command instanceof CommandCommits);
        CommandCommits commit = (CommandCommits) command;
        assertEquals(commit.author, "test");
    }
    
    @Test
    public void abbreviatedOptions05()
    {
        Cli<?> parser = Cli.builder("git")
                .withCommand(CommandCommits.class)
                .withOptionAbbreviation()
                .build();
        
        // Option name whose name is a super string of another option name
        Object command = parser.parse("commits", "--authors", "test");
        
        assertNotNull(command);;
        assertTrue(command instanceof CommandCommits);
        CommandCommits commit = (CommandCommits) command;
        assertTrue(commit.authors.contains("test"));
    }
}
