package com.github.rvesse.airline.help.ronn;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Set;

import com.github.rvesse.airline.help.AbstractGlobalUsageGenerator;
import com.github.rvesse.airline.help.CommandUsageGenerator;
import com.github.rvesse.airline.model.CommandGroupMetadata;
import com.github.rvesse.airline.model.CommandMetadata;
import com.github.rvesse.airline.model.GlobalMetadata;
import com.github.rvesse.airline.model.OptionMetadata;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * <p>
 * A global usage generator which generates help in <a
 * href="http://rtomayko.github.io/ronn/">Ronn format</a> which can then be
 * transformed into man pages or HTML pages as desired using the Ronn tooling.
 * </p>
 * <p>
 * The individual sections of the documentation are each generated by a
 * protected method so this class can be used as a base and extended if you wish
 * to customise how sections are output
 * </p>
 */
public class RonnGlobalUsageGenerator extends AbstractGlobalUsageGenerator {

    protected final CommandUsageGenerator commandUsageGenerator;
    protected final int manSection;
    /**
     * Constant for a new paragraph
     */
    protected static final String NEW_PARA = "\n\n";
    /**
     * Constant for a horizontal rule
     */
    protected static final String HORIZONTAL_RULE = "---";

    public RonnGlobalUsageGenerator() {
        this(ManSections.GENERAL_COMMANDS, new RonnCommandUsageGenerator(ManSections.GENERAL_COMMANDS, false, false));
    }

    public RonnGlobalUsageGenerator(int manSection) {
        this(manSection, new RonnCommandUsageGenerator(manSection, false, false));
    }

    public RonnGlobalUsageGenerator(int manSection, boolean includeHidden) {
        this(manSection, new RonnCommandUsageGenerator(manSection, includeHidden, false));
    }

    protected RonnGlobalUsageGenerator(int manSection, CommandUsageGenerator commandUsageGenerator) {
        this.commandUsageGenerator = commandUsageGenerator;
        this.manSection = manSection;
    }

    @Override
    public void usage(GlobalMetadata global, OutputStream output) throws IOException {
        Writer writer = new OutputStreamWriter(output);

        outputTitle(global, writer);

        List<OptionMetadata> options = newArrayList();
        if (global.getOptions() != null && global.getOptions().size() > 0) {
            options.addAll(global.getOptions());
            options = sortOptions(options);
        }
        outputSynopsis(writer, global);

        if (options.size() > 0) {
            outputGlobalOptions(writer, options);
        }

        // TODO If we add Discussion and Examples to global meta-data reinstate
        // this
        //@formatter:off
//        if (global.getDiscussion() != null) {
//            writer.append(NEW_PARA).append("## DISCUSSION").append(NEW_PARA);
//            writer.append(global.getDiscussion());
//        }
//
//        if (global.getExamples() != null && !global.getExamples().isEmpty()) {
//            writer.append(NEW_PARA).append("## EXAMPLES");
//
//            // this will only work for "well-formed" examples
//            for (int i = 0; i < global.getExamples().size(); i += 3) {
//                String aText = global.getExamples().get(i).trim();
//
//                if (aText.startsWith("*")) {
//                    aText = aText.substring(1).trim();
//                }
//
//                writer.append(NEW_PARA).append("* ").append(aText).append(":\n");
//            }
//        }
        //@formatter:on

        writer.flush();
        output.flush();

        if (global.getCommandGroups().size() > 0) {
            // Command Groups
            outputGroupCommandList(writer, global);
            outputCommandUsages(output, writer, global);
        } else {
            // No Groups
            outputCommandList(writer, global);
            outputCommandUsages(output, writer, global);
        }

        // Flush the output
        writer.flush();
        output.flush();
    }

    /**
     * Outputs a documentation section that lists the available groups and the
     * commands they contain
     * <p>
     * Used only when a CLI has command groups, if no groups are present then
     * {@link #outputCommandList(Writer, GlobalMetadata)} is used instead.
     * </p>
     * 
     * @param writer
     *            Writer
     * @param global
     *            Global meta-data
     * 
     * @throws IOException
     */
    protected void outputGroupCommandList(Writer writer, GlobalMetadata global) throws IOException {
        writer.append(NEW_PARA).append("## COMMAND GROUPS").append(NEW_PARA);
        writer.append("Commands are grouped as follows:");

        if (global.getDefaultGroupCommands().size() > 0) {
            writer.append(NEW_PARA).append("* Default (no <group> specified)");
            for (CommandMetadata command : sortCommands(global.getDefaultGroupCommands())) {
                if (command.isHidden() && !this.includeHidden())
                    continue;

                writer.append(NEW_PARA).append("  * `").append(getCommandName(global, null, command)).append("`:\n");
                writer.append("  ").append(command.getDescription());
            }
        }

        for (CommandGroupMetadata group : sortCommandGroups(global.getCommandGroups())) {
            if (group.isHidden() && !this.includeHidden())
                continue;
            
            writer.append(NEW_PARA).append("* **").append(group.getName()).append("**").append(NEW_PARA);
            writer.append("  ").append(group.getDescription());

            for (CommandMetadata command : sortCommands(group.getCommands())) {
                if (command.isHidden() && !this.includeHidden())
                    continue;

                writer.append(NEW_PARA).append("  * `").append(getCommandName(global, group.getName(), command))
                        .append("`:\n");
                writer.append("  ").append(command.getDescription());
            }
        }
    }

    /**
     * Outputs a documentation section that lists the available commands
     * <p>
     * Used only when a CLI does not have command groups, if groups are present
     * then {@link #outputGroupCommandList(Writer, GlobalMetadata)} is used
     * instead.
     * </p>
     * 
     * @param writer
     *            Writer
     * @param global
     *            Global meta-data
     * 
     * @throws IOException
     */
    protected void outputCommandList(Writer writer, GlobalMetadata global) throws IOException {
        writer.append(NEW_PARA).append("## COMMANDS");

        for (CommandMetadata command : sortCommands(global.getDefaultGroupCommands())) {
            if (command.isHidden() && !this.includeHidden())
                continue;

            writer.append(NEW_PARA).append("* `").append(getCommandName(global, null, command)).append("`:\n");
            writer.append(command.getDescription());
        }
    }

    /**
     * Outputs a documentation section detailing the available global options
     * 
     * @param writer
     *            Writer
     * @param options
     *            Options
     * @throws IOException
     */
    protected void outputGlobalOptions(Writer writer, List<OptionMetadata> options) throws IOException {
        writer.append(NEW_PARA).append("## GLOBAL OPTIONS");
        options = sortOptions(options);

        for (OptionMetadata option : options) {
            // skip hidden options
            if (option.isHidden() && !this.includeHidden())
                continue;

            // option names
            writer.append(NEW_PARA).append("* ").append(toDescription(option)).append(":\n");

            // description
            writer.append(option.getDescription());

            // allowedValues
            if (option.getAllowedValues() != null && option.getAllowedValues().size() > 0 && option.getArity() >= 1) {
                outputAllowedValues(writer, option);
            }
        }
    }

    /**
     * Outputs a documentation section detailing the allowed values for an
     * option
     * 
     * @param writer
     *            Writer
     * @param option
     *            Option
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
     * Outputs a documentation section with a synopsis of how to use the CLI
     * 
     * @param writer
     *            Writer
     * @param global
     *            Global meta-data
     * 
     * @return
     * @throws IOException
     */
    protected void outputSynopsis(Writer writer, GlobalMetadata global) throws IOException {
        writer.append(NEW_PARA).append("## SYNOPSIS").append(NEW_PARA);
        writer.append("`").append(global.getName()).append("`");
        if (global.getOptions() != null && global.getOptions().size() > 0) {
            writer.append(" ").append(Joiner.on(" ").join(toSynopsisUsage(sortOptions(global.getOptions()))));
        }
        if (global.getCommandGroups().size() > 0) {
            writer.append(" [<group>] <command> [command-args]");
        } else {
            writer.append(" <command> [command-args]");
        }
    }

    /**
     * Outputs the title section for the documentation
     * 
     * @param global
     *            Global meta-data
     * @param writer
     *            Writer
     * @throws IOException
     */
    protected void outputTitle(GlobalMetadata global, Writer writer) throws IOException {
        writer.append(global.getName()).append("(").append(Integer.toString(this.manSection)).append(") -- ");
        writer.append(global.getDescription()).append("\n");
        writer.append("==========");
    }

    /**
     * Outputs the command usages for all groups
     * 
     * @param output
     *            Output stream
     * @param writer
     *            Writer
     * @param global
     *            Global meta-data
     * 
     * @throws IOException
     */
    protected void outputCommandUsages(OutputStream output, Writer writer, GlobalMetadata global) throws IOException {
        writer.append(NEW_PARA).append(HORIZONTAL_RULE).append(NEW_PARA);

        // Default group usages
        outputDefaultGroupCommandUsages(output, writer, global);

        // Other group usages
        for (CommandGroupMetadata group : sortCommandGroups(global.getCommandGroups())) {
            if (group.isHidden() && !this.includeHidden())
                continue;
            
            outputGroupCommandUsages(output, writer, global, group);
        }
    }

    /**
     * Gets the display name for a command
     * 
     * @param global
     *            Global meta-data
     * @param groupName
     *            Group name (may be null)
     * @param command
     *            Command meta-data
     * @return Display name for the command
     */
    protected String getCommandName(GlobalMetadata global, String groupName, CommandMetadata command) {
        return command.getName();
    }

    /**
     * Outputs the command usages for the commands in the given group
     * 
     * @param output
     *            Output
     * @param writer
     *            Writer
     * @param global
     *            Global Meta-data
     * @param group
     *            Group Meta-data
     * 
     * @throws IOException
     */
    protected void outputGroupCommandUsages(OutputStream output, Writer writer, GlobalMetadata global,
            CommandGroupMetadata group) throws IOException {
        for (CommandMetadata command : sortCommands(group.getCommands())) {
            if (command.isHidden() && !this.includeHidden())
                continue;

            writer.flush();
            output.flush();
            commandUsageGenerator.usage(global.getName(), group.getName(), command.getName(), command, output);
            writer.append(NEW_PARA).append(HORIZONTAL_RULE).append(NEW_PARA);
        }
    }

    /**
     * Outputs the command usages for the commands in the default group
     * 
     * @param output
     *            Output
     * @param writer
     *            Writer
     * @param global
     *            Global meta-data
     * 
     * @throws IOException
     */
    protected void outputDefaultGroupCommandUsages(OutputStream output, Writer writer, GlobalMetadata global)
            throws IOException {
        for (CommandMetadata command : sortCommands(global.getDefaultGroupCommands())) {
            if (command.isHidden() && !this.includeHidden())
                continue;

            writer.flush();
            output.flush();
            commandUsageGenerator.usage(global.getName(), null, command.getName(), command, output);
            writer.append(NEW_PARA).append(HORIZONTAL_RULE).append(NEW_PARA);
        }
    }

    /**
     * Converts an option to its description form
     */
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
