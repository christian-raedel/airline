package com.github.rvesse.airline.builder;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.github.rvesse.airline.Cli;
import com.github.rvesse.airline.CommandFactory;
import com.github.rvesse.airline.CommandFactoryDefault;
import com.github.rvesse.airline.TypeConverter;
import com.github.rvesse.airline.parser.AliasArgumentsParser;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class CliBuilder<C> extends AbstractBuilder<Cli<C>> {

    protected final String name;
    protected String description;
    protected TypeConverter typeConverter = new TypeConverter();
    protected String optionSeparators;
    private Class<? extends C> defaultCommand;
    private final List<Class<? extends C>> defaultCommandGroupCommands = newArrayList();
    protected final Map<String, AliasBuilder<C>> aliases = newHashMap();
    protected final Map<String, GroupBuilder<C>> groups = newHashMap();
    protected CommandFactory<C> commandFactory = new CommandFactoryDefault<C>();
    protected boolean allowAbbreviatedCommands, allowAbbreviatedOptions, aliasesOverrideBuiltIns;

    public CliBuilder(String name) {
        checkNotBlank(name, "Program name");
        this.name = name;
    }

    public CliBuilder<C> withDescription(String description) {
        checkNotEmpty(description, "Description");
        this.description = description;
        return this;
    }

    public CliBuilder<C> withCommandFactory(CommandFactory<C> commandFactory) {
        this.commandFactory = commandFactory;
        return this;
    }

    public CliBuilder<C> withDefaultCommand(Class<? extends C> defaultCommand) {
        this.defaultCommand = defaultCommand;
        return this;
    }

    public CliBuilder<C> withCommand(Class<? extends C> command) {
        this.defaultCommandGroupCommands.add(command);
        return this;
    }

    @SuppressWarnings("unchecked")
    public CliBuilder<C> withCommands(Class<? extends C> command, Class<? extends C>... moreCommands) {
        this.defaultCommandGroupCommands.add(command);
        this.defaultCommandGroupCommands.addAll(ImmutableList.copyOf(moreCommands));
        return this;
    }

    public CliBuilder<C> withCommands(Iterable<Class<? extends C>> commands) {
        this.defaultCommandGroupCommands.addAll(ImmutableList.copyOf(commands));
        return this;
    }

    public GroupBuilder<C> withGroup(String name) {
        checkNotBlank(name, "Group name");

        if (groups.containsKey(name)) {
            return groups.get(name);
        }

        GroupBuilder<C> group = new GroupBuilder<C>(name);
        groups.put(name, group);
        return group;
    }

    public GroupBuilder<C> getGroup(final String name) {
        checkNotBlank(name, "Group name");
        Preconditions.checkArgument(groups.containsKey(name), "Group %s has not been declared", name);

        return groups.get(name);
    }

    public AliasBuilder<C> withAlias(final String name) {
        checkNotBlank(name, "Alias name");

        if (aliases.containsKey(name)) {
            return aliases.get(name);
        }

        AliasBuilder<C> alias = new AliasBuilder<C>(name);
        aliases.put(name, alias);
        return alias;
    }

    public AliasBuilder<C> getAlias(final String name) {
        checkNotBlank(name, "Alias name");
        Preconditions.checkArgument(aliases.containsKey(name), "Alias %s has not been declared", name);

        return aliases.get(name);
    }

    /**
     * Reads in user aliases from the default configuration file in the default
     * location.
     * <p>
     * The default configuration file name is constructed by appending the
     * {@code .config} extension to the defined program name
     * </p>
     * <p>
     * The default search location is a {@code .program} directory under the
     * users home directory where {@code program} is the defined program name.
     * </p>
     * <p>
     * If you prefer to control these values explicitly and for more detail on the
     * configuration format please see the
     * {@link #withUserAliases(String, String, String...)} method
     * </p>
     * 
     * @return Builder
     * @throws IOException
     */
    public CliBuilder<C> withUserAliases() throws IOException {
        // Use default filename and search location
        return withUserAliases(this.name + ".config", null, System.getProperty("user.home") + "/." + this.name + "/");
    }

    /**
     * Reads in user aliases from the default configuration file in the default
     * location
     * <p>
     * The default configuration file name is constructed by appending the
     * {@code .config} extension to the defined program name
     * </p>
     * <p>
     * If you prefer to control this value explicitly and for more detail on the
     * configuration format please see the
     * {@link #withUserAliases(String, String, String...)} method
     * </p>
     * 
     * @param searchLocation
     *            Location to search
     * 
     * @return Builder
     * @throws IOException
     */
    public CliBuilder<C> withUserAliases(String searchLocation) throws IOException {
        // Use default filename
        return withUserAliases(this.name + ".config", null, searchLocation);
    }

    /**
     * Reads in user aliases from the default configuration file in the default
     * location
     * <p>
     * This file is in standard Java properties format with the key being the
     * alias and the value being the arguments for this alias. Arguments are
     * whitespace separated though quotes ({@code "}) may be used to wrap
     * arguments that need to contain whitespace. Quotes may be escaped within
     * quoted arguments and whitespace may be escaped within unquoted arguments.
     * Note that since Java property values are interpreted as Java strings it
     * is necessary to double escape the backslash i.e. {@code \\"} for this to
     * work properly.
     * </p>
     * <pre>
     * example=command --option value
     * quoted=command "long argument"
     * escaped=command whitespace\\ escape "quote\\"escape"
     * </pre>
     * <p>
     * The search locations should be given in order of preference, the file
     * will be loaded from all search locations in which it exists such that
     * values from the locations occurring first in the search locations list
     * take precedence. This allows for having multiple locations for your
     * configuration file and layering different sets of aliases over each other
     * e.g. system, user and local aliases.
     * </p>
     * <p>
     * The {@code prefix} is used to filter properties from the properties file
     * such that you can include aliases with other configuration settings in
     * your configuration files. When a prefix is used only properties that
     * start with the prefix are interpreted as alias definitions and the actual
     * alias is the property name with the prefix removed. For example if your
     * prefix was {@code alias.} and you had a property {@code alias.foo} the
     * resulting alias would be {@code foo}.
     * </p>
     * <h3>Notes</h3>
     * <ul>
     * <li>Recursive aliases are not supported and will result in errors when
     * used</li>
     * <li>Aliases cannot override built-ins unless you have called
     * {@link #withAliasesOverridingBuiltIns()} on your builder</li>
     * </ul>
     * 
     * @return
     * @throws IOException
     */
    public CliBuilder<C> withUserAliases(final String filename, final String prefix, final String... searchLocations)
            throws IOException {
        // Search locations in reverse order overwriting previously found values
        // each time. Thus the first location in the list has highest precedence
        Properties properties = new Properties();
        for (int i = searchLocations.length - 1; i >= 0; i--) {
            File f = new File(searchLocations[i]);
            f = new File(f, filename);
            if (f.exists() && f.isFile() && f.canRead()) {
                try (FileInputStream input = new FileInputStream(f)) {
                    properties.load(input);
                } finally {
                    // No clean up actions, try-with-resources does clean up for
                    // us
                }
            }
        }

        // Strip any irrelevant properties
        if (StringUtils.isNotEmpty(prefix)) {
            List<Object> keysToRemove = new ArrayList<Object>();
            for (Object key : properties.keySet()) {
                if (!key.toString().startsWith(prefix))
                    keysToRemove.add(key);
            }
            for (Object key : keysToRemove) {
                properties.remove(key);
            }
        }

        // Generate the aliases
        for (Object key : properties.keySet()) {
            String name = key.toString();
            if (prefix != null)
                name = name.substring(prefix.length());
            AliasBuilder<C> alias = this.withAlias(name);

            String value = properties.getProperty(key.toString());
            if (StringUtils.isEmpty(value))
                continue;

            // Process property value into arguments
            List<String> args = AliasArgumentsParser.parse(value);
            alias.withArguments(args.toArray(new String[args.size()]));
        }

        return this;
    }

    public CliBuilder<C> withAliasesOverridingBuiltIns() {
        this.aliasesOverrideBuiltIns = true;
        return this;
    }

    public CliBuilder<C> withCommandAbbreviation() {
        this.allowAbbreviatedCommands = true;
        return this;
    }

    public CliBuilder<C> withOptionAbbreviation() {
        this.allowAbbreviatedOptions = true;
        return this;
    }

    @Override
    public Cli<C> build() {
        return new Cli<C>(name, description, typeConverter, defaultCommand, commandFactory,
                defaultCommandGroupCommands, groups.values(), aliases.values(), aliasesOverrideBuiltIns,
                allowAbbreviatedCommands, allowAbbreviatedOptions);
    }
}
