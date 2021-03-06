package com.github.rvesse.airline.help.ronn;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.github.rvesse.airline.help.AbstractCommandUsageGenerator;
import com.github.rvesse.airline.model.CommandMetadata;
import com.github.rvesse.airline.model.OptionMetadata;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * A command usage generator which generates help in <a
 * href="http://rtomayko.github.io/ronn/">Ronn format</a> which can then be
 * transformed into man pages or HTML pages as desired using the Ronn tooling
 * 
 */
public class RonnCommandUsageGenerator extends AbstractCommandUsageGenerator {

    private final int manSection;
    private final boolean standalone;
    /**
     * Constant for new paragraph
     */
    protected static final String NEW_PARA = "\n\n";
    /**
     * Constant for horizontal rule
     */
    protected static final String HORIZONTAL_RULE = "---";

    public RonnCommandUsageGenerator() {
        this(ManSections.GENERAL_COMMANDS, false, true);
    }

    /**
     * Creates a new RONN usage generator
     * 
     * @param manSection
     *            Man section to which this command belongs, use constants from
     *            {@link ManSections}
     * @param standalone
     *            Whether this is a stand-alone RONN file, this controls the
     *            formatting of the title which is significant when using this
     *            in conjunction with things like the
     *            {@link RonnGlobalUsageGenerator} where the output from this is
     *            output a fragment of a larger document and RONN will not
     *            render the titles if stand-alone is enabled
     */
    public RonnCommandUsageGenerator(int manSection, boolean includeHidden, boolean standalone) {
        super(includeHidden);
        this.manSection = manSection;
        this.standalone = standalone;
    }

    @Override
    public void usage(String programName, String groupName, String commandName, CommandMetadata command,
            OutputStream output) throws IOException {
        String SECTION_HEADER = "## ";

        // Fall back to metadata declared name if necessary
        if (commandName == null)
            commandName = command.getName();

        Writer writer = new OutputStreamWriter(output);

        SECTION_HEADER = outputTitle(writer, programName, groupName, commandName, command, SECTION_HEADER);

        List<OptionMetadata> options = outputSynopsis(writer, programName, groupName, commandName, command,
                SECTION_HEADER);

        if (options.size() > 0 || command.getArguments() != null) {
            outputOptions(writer, command, options, SECTION_HEADER);
        }
        if (command.getDiscussion() != null && !command.getDiscussion().isEmpty()) {
            outputDiscussion(writer, command, SECTION_HEADER);
        }
        if (command.getExamples() != null && !command.getExamples().isEmpty()) {
            outputExamples(writer, command, SECTION_HEADER);
        }
        if (command.getExitCodes() != null && !command.getExitCodes().isEmpty()) {
            outputExitCodes(writer, programName, groupName, commandName, command, SECTION_HEADER);
        }

        // Flush the output
        writer.flush();
        output.flush();
    }

    /**
     * Outputs a documentation section detailing the options and their usages
     * 
     * @param writer
     *            Writer
     * @param command
     *            Command
     * @param options
     *            Option meta-data
     * @param sectionHeader
     *            Section header
     * 
     * @throws IOException
     */
    protected void outputOptions(Writer writer, CommandMetadata command, List<OptionMetadata> options,
            String sectionHeader) throws IOException {
        writer.append(NEW_PARA).append(sectionHeader).append("OPTIONS");
        options = sortOptions(options);

        for (OptionMetadata option : options) {
            // skip hidden options
            if (option.isHidden() && !this.includeHidden()) {
                continue;
            }

            // option names
            writer.append(NEW_PARA).append("* ").append(toDescription(option)).append(":\n");

            // description
            writer.append(option.getDescription());

            // allowedValues
            if (option.getAllowedValues() != null && option.getAllowedValues().size() > 0 && option.getArity() >= 1) {
                outputAllowedValues(writer, option);
            }
        }

        if (command.getArguments() != null) {
            // "--" option
            writer.append(NEW_PARA).append("* `--`:\n");

            // description
            writer.append("This option can be used to separate command-line options from the "
                    + "list of arguments (useful when arguments might be mistaken for command-line options).");

            // arguments name
            writer.append(NEW_PARA).append("* ").append(toDescription(command.getArguments())).append(":\n");

            // description
            writer.append(command.getArguments().getDescription());
        }
    }

    /**
     * Outputs a documentation section detailing the allowed values for an
     * option
     * 
     * @param writer
     *            Writer
     * @param option
     *            Option meta-data
     * @throws IOException
     */
    protected void outputAllowedValues(Writer writer, OptionMetadata option) throws IOException {
        writer.append(NEW_PARA).append("  This options value");
        if (option.getArity() == 1) {
            writer.append(" is ");
        } else {
            writer.append("s are ");
        }
        writer.append("restricted to the following value(s): [");

        boolean first = true;
        for (String value : option.getAllowedValues()) {
            if (first) {
                first = false;
            } else {
                writer.append(", ");
            }
            writer.append(value);
        }
        writer.append("]");
    }

    /**
     * Outputs a synopsis section for the documentation showing how to use a
     * command
     * 
     * @param writer
     *            Writer
     * @param programName
     *            Program name
     * @param groupName
     *            Group name
     * @param commandName
     *            Command name
     * @param command
     *            Command
     * @param sectionHeader
     *            Section header
     * @return List of all the available options (global, group and command)
     * @throws IOException
     */
    protected List<OptionMetadata> outputSynopsis(Writer writer, String programName, String groupName,
            String commandName, CommandMetadata command, String sectionHeader) throws IOException {
        writer.append(NEW_PARA).append(sectionHeader).append("SYNOPSIS").append(NEW_PARA);
        List<OptionMetadata> options = newArrayList();
        List<OptionMetadata> aOptions;
        if (programName != null) {
            writer.append("`").append(programName).append("`");
            aOptions = command.getGlobalOptions();
            if (aOptions != null && aOptions.size() > 0) {
                writer.append(" ").append(Joiner.on(" ").join(toSynopsisUsage(sortOptions(aOptions))));
                options.addAll(aOptions);
            }
        }
        if (groupName != null) {
            writer.append(" `").append(groupName).append("`");
            aOptions = command.getGroupOptions();
            if (aOptions != null && aOptions.size() > 0) {
                writer.append(" ").append(Joiner.on(" ").join(toSynopsisUsage(sortOptions(aOptions))));
                options.addAll(aOptions);
            }
        }
        aOptions = command.getCommandOptions();
        writer.append(" `").append(commandName).append("` ")
                .append(Joiner.on(" ").join(toSynopsisUsage(sortOptions(aOptions))));
        options.addAll(aOptions);

        // command arguments (optional)
        if (command.getArguments() != null) {
            writer.append(" [--] ").append(toUsage(command.getArguments()));
        }

        if (!this.standalone) {
            writer.append(NEW_PARA).append(command.getDescription());
        }
        return options;
    }

    /**
     * Outputs an exit codes section for the documentation
     * 
     * @param writer
     *            Writer
     * @param programName
     *            Program name
     * @param groupName
     *            Group name
     * @param commandName
     *            Command name
     * @param command
     *            Command meta-data
     * @param sectionHeader
     *            Section header
     * 
     * @throws IOException
     */
    protected void outputExitCodes(Writer writer, String programName, String groupName, String commandName,
            CommandMetadata command, String sectionHeader) throws IOException {
        writer.append(NEW_PARA).append(sectionHeader).append("EXIT STATUS");
        writer.append(NEW_PARA).append("The `");
        writeFullCommandName(programName, groupName, commandName, writer);
        writer.append("` command exits with one of the following values:");
        writer.append(NEW_PARA);

        for (Entry<Integer, String> exit : sortExitCodes(Lists.newArrayList(command.getExitCodes().entrySet()))) {
            // Print the exit code
            writer.append("* **").append(exit.getKey().toString()).append("**");

            // Include description if available
            if (!StringUtils.isEmpty(exit.getValue())) {
                writer.append(" - ").append(exit.getValue());
            }

            writer.append('\n');
        }
    }

    /**
     * Outputs an examples section for the documentation
     * 
     * @param writer
     *            Writer
     * @param command
     *            Command meta-data
     * @param sectionHeader
     *            Section header
     * @throws IOException
     */
    protected void outputExamples(Writer writer, CommandMetadata command, String sectionHeader) throws IOException {
        writer.append(NEW_PARA).append(sectionHeader).append("EXAMPLES");

        for (String example : command.getExamples()) {
            writer.append(NEW_PARA).append(example);
        }
    }

    /**
     * Outputs a discussion section for the documentation
     * 
     * @param writer
     *            Writer
     * @param command
     *            Command meta-data
     * @param sectionHeader
     *            Section header
     * @throws IOException
     */
    protected void outputDiscussion(Writer writer, CommandMetadata command, String sectionHeader) throws IOException {
        if (command.getDiscussion() == null || command.getDiscussion().isEmpty())
            return;
        
        writer.append(NEW_PARA).append(sectionHeader).append("DISCUSSION").append(NEW_PARA);
        for (String discussionPara : command.getDiscussion()) {
            if (StringUtils.isEmpty(discussionPara))
                continue;
            writer.append(discussionPara).append(NEW_PARA);
        }
    }

    /**
     * Outputs a title section for the document
     * 
     * @param writer
     *            Writer
     * @param programName
     *            Program name
     * @param groupName
     *            Group name
     * @param commandName
     *            Command name
     * @param command
     *            Command meta-data
     * @param sectionHeader
     *            Section header
     * @return Section header
     * @throws IOException
     */
    protected String outputTitle(Writer writer, String programName, String groupName, String commandName,
            CommandMetadata command, String sectionHeader) throws IOException {
        if (!this.standalone) {
            writer.append(sectionHeader);
            sectionHeader = "#" + sectionHeader;
        }
        writeFullCommandName(programName, groupName, commandName, writer);
        if (this.standalone) {
            writer.append(" -- ");
            writer.append(command.getDescription()).append("\n");
            writer.append("==========");
        }
        return sectionHeader;
    }

    /**
     * Writes the full command name in man page syntax
     * 
     * @param programName
     *            Program name
     * @param groupName
     *            Group name
     * @param command
     *            Command meta-data
     * @param writer
     *            Writer
     * @throws IOException
     */
    protected void writeFullCommandName(String programName, String groupName, String commandName, Writer writer)
            throws IOException {
        if (programName != null) {
            writer.append(programName).append("-");
        }
        if (groupName != null) {
            writer.append(groupName).append("-");
        }
        writer.append(commandName).append("(").append(Integer.toString(this.manSection)).append(")");
    }

    @Override
    protected String toDescription(OptionMetadata option) {
        Set<String> options = option.getOptions();
        StringBuilder stringBuilder = new StringBuilder();

        final String argumentString;
        if (option.getArity() > 0) {
            argumentString = Joiner.on(" ").join(
                    Lists.transform(ImmutableList.of(option.getTitle()), new Function<String, String>() {
                        public String apply(String argument) {
                            return "<" + argument + ">";
                        }
                    }));
        } else {
            argumentString = null;
        }

        Joiner.on(", ").appendTo(stringBuilder, transform(options, new Function<String, String>() {
            public String apply(String option) {
                if (argumentString != null) {
                    return "`" + option + "` " + argumentString;
                }
                return "`" + option + "`";
            }
        }));

        return stringBuilder.toString();
    }
}
