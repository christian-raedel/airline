package com.github.rvesse.airline.command;

import java.util.List;

import com.github.rvesse.airline.Arguments;
import com.github.rvesse.airline.Command;
import com.github.rvesse.airline.Group;
import com.github.rvesse.airline.Option;

@Group(name = "singleGroup", description = "a single group", defaultCommand = CommandWithGroupAnnotation.class,commands = {CommandAdd.class})
@Command(name = "commandWithGroup", description = "A command with a group annotation")
public class CommandWithGroupAnnotation
{
    @Arguments(description = "Patterns of files to be added")
    public List<String> patterns;

    @Option(name = "-i")
    public Boolean interactive = false;
}
