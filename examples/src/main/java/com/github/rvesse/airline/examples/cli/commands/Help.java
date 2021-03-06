package com.github.rvesse.airline.examples.cli.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.github.rvesse.airline.Arguments;
import com.github.rvesse.airline.Command;
import com.github.rvesse.airline.CompletionBehaviour;
import com.github.rvesse.airline.Option;
import com.github.rvesse.airline.examples.ExampleRunnable;
import com.github.rvesse.airline.model.GlobalMetadata;

@Command(name = "help", description = "A command that provides help on other commands")
public class Help implements ExampleRunnable {

    @Inject
    private GlobalMetadata global;

    @Arguments(description = "Provides the name of the commands you want to provide help for", completionBehaviour = CompletionBehaviour.CLI_COMMANDS)
    private List<String> commandNames = new ArrayList<String>();
    
    @Option(name = "--include-hidden", description = "When set hidden commands and options are shown in help", hidden = true)
    private boolean includeHidden = false;

    @Override
    public int run() {
        try {
            com.github.rvesse.airline.help.Help.help(global, commandNames, this.includeHidden);
        } catch (IOException e) {
            System.err.println("Failed to output help: " + e.getMessage());
            e.printStackTrace(System.err);
            return 1;
        }
        return 0;
    }

}
