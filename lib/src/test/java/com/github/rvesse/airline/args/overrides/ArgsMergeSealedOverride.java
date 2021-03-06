package com.github.rvesse.airline.args.overrides;

import com.github.rvesse.airline.Command;
import com.github.rvesse.airline.Option;

@Command(name = "ArgsMergeSealedOverride")
public class ArgsMergeSealedOverride extends ArgsMergeSealed {

    /**
     * This is an illegal override because the parent option we are trying to override is marked as sealed
     */
    @Option(name = "--hidden", description = "Hidden again", hidden = true, override = true)
    private boolean hidden = false;
}
