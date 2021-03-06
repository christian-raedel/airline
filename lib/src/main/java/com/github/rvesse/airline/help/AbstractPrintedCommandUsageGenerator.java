package com.github.rvesse.airline.help;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Comparator;

import com.github.rvesse.airline.model.CommandMetadata;
import com.github.rvesse.airline.model.OptionMetadata;
import com.google.common.base.Preconditions;

/**
 * Abstract command usage generator for generators that use a
 * {@link UsagePrinter} to generate the documentation
 */
public abstract class AbstractPrintedCommandUsageGenerator extends AbstractCommandUsageGenerator {

    private final int columnSize;

    public AbstractPrintedCommandUsageGenerator(int columns, Comparator<? super OptionMetadata> optionComparator,
            boolean includeHidden) {
        super(optionComparator, includeHidden);
        Preconditions.checkArgument(columns > 0, "columns must be greater than 0");
        this.columnSize = columns;
    }

    /**
     * Generate the help and output is using the provided {@link UsagePrinter}
     * 
     * @param programName
     *            Program Name
     * @param groupName
     *            Group Name
     * @param commandName
     *            Command Name
     * @param command
     *            Command Metadata
     * @param out
     *            Usage printer to output with
     * @throws IOException
     */
    protected abstract void usage(String programName, String groupName, String commandName, CommandMetadata command,
            UsagePrinter out) throws IOException;

    /**
     * Creates a usage printer for the given stream
     * 
     * @param out
     *            Output stream
     * @return Usage Printer
     */
    protected UsagePrinter createUsagePrinter(OutputStream out) {
        Preconditions.checkNotNull(out, "OutputStream cannot be null");
        OutputStreamWriter writer = new OutputStreamWriter(out);
        return new UsagePrinter(writer, columnSize);
    }

    @Override
    public void usage(String programName, String groupName, String commandName, CommandMetadata command,
            OutputStream out) throws IOException {
        UsagePrinter printer = createUsagePrinter(out);
        usage(programName, groupName, commandName, command, printer);
        printer.flush();
    }

}
