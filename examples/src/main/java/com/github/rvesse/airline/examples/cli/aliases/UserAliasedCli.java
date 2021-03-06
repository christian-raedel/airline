package com.github.rvesse.airline.examples.cli.aliases;

import java.io.IOException;

import com.github.rvesse.airline.Cli;
import com.github.rvesse.airline.builder.CliBuilder;
import com.github.rvesse.airline.examples.ExampleExecutor;
import com.github.rvesse.airline.examples.ExampleRunnable;
import com.github.rvesse.airline.examples.cli.commands.Help;
import com.github.rvesse.airline.examples.inheritance.Child;
import com.github.rvesse.airline.examples.inheritance.GoodGrandchild;
import com.github.rvesse.airline.examples.inheritance.Parent;
import com.github.rvesse.airline.examples.simple.Simple;

/**
 * An example of creating a CLI that takes advantage of the aliases feature.
 * <p>
 * Aliases provide a means by which you can define additional top level commands
 * that simply delegate to actual commands. Often you actually want to leave
 * alias definition up to end users and so Airline supports reading in aliases
 * from a user configuration file out of the box
 * </p>
 *
 */
public class UserAliasedCli {

    public static void main(String[] args) {
        //@formatter:off
        @SuppressWarnings("unchecked")
        // The program name is cli
        CliBuilder<ExampleRunnable> builder = Cli.<ExampleRunnable>builder("cli")
                                                 // Add a description
                                                 .withDescription("A simple CLI with several commands available")
                                                 // Define some commands
                                                 .withCommand(Simple.class)
                                                 .withCommands(Parent.class, Child.class, GoodGrandchild.class)
                                                 .withCommand(Help.class);
        //@formatter:on
        // Read aliases from user configuration file
        // You can find this example configuration under
        // src/main/resources/aliases.config
        try {
            builder.withUserAliases("aliases.config", null, "src/main/resources/");
        } catch (IOException e) {
            System.out.println("Failed to read in aliases.config");
        }

        ExampleExecutor.executeCli(builder.build(), args);
    }

}
