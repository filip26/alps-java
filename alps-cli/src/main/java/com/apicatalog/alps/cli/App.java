package com.apicatalog.alps.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;

@Command(
    name = "alps", 
    description = "Transform and validate ALPS documents",
    subcommands = {Validator.class, Transformer.class},
//    synopsisSubcommandLabel = "(validate | transform)",
    mixinStandardHelpOptions = false,
    descriptionHeading = "%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n",
    commandListHeading = "%nCommands:%n"
    )
public final class App {

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "display a help message")
    boolean help = false;
    
    public static void main(String[] args) {

        final CommandLine cli = new CommandLine(new App());
        cli.setCaseInsensitiveEnumValuesAllowed(true);
        
        try {
            
            final ParseResult result = cli.parseArgs(args);
            
            if (cli.isUsageHelpRequested()) {
    
                if (result.subcommand() != null) {
                    result.subcommand().commandSpec().commandLine().usage(cli.getOut());
                    return;
                }
                
                cli.usage(cli.getOut());
                return;
            }
    
            cli.execute(args);

        } catch (Exception ex) {
            ex.printStackTrace(cli.getErr());
            System.exit(cli.getCommandSpec().exitCodeOnExecutionException());
        }
    }
}
