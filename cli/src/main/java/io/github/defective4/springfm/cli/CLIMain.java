package io.github.defective4.springfm.cli;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import io.github.defective4.springfm.client.SpringFMClient;
import io.github.defective4.springfm.client.utils.RadioUtils;

public class CLIMain {

    private static final Options OPTIONS = new Options().addOption(Option.builder("p").hasArg().argName("profile").desc(
            "Profile to connect to. If used with probe command, it will only show services belonging to this profile.")
            .longOpt("profile").build())
            .addOption(Option.builder("v").desc("Be more verbose.").longOpt("verbose").build())
            .addOption(Option.builder("s").longOpt("service")
                    .desc("Service index to play. Set to -1 to use empty service.").argName("index").hasArg()
                    .converter(Integer::parseInt).build())
            .addOption(Option.builder("F").longOpt("frequency")
                    .desc("Tune current service. Can only be used with analog services. Requires the --service option.")
                    .argName("Hz").hasArg().build())
            .addOption(Option.builder("G").longOpt("gain").desc(
                    "Set service gain. Can only be used with services that support gain adjusting. Requires the --service option.")
                    .argName("dB").hasArg().converter(Float::parseFloat).build());

    public static void main(String[] args) {
        if (args.length < 1) {
            printHelp("Missing command");
            System.exit(1);
            return;
        }

        try {
            String[] subargs = new String[args.length - 1];
            System.arraycopy(args, 1, subargs, 0, subargs.length);
            CommandLine cli = new DefaultParser().parse(OPTIONS, subargs);
            if (cli.getArgs().length == 0) {
                printHelp("Missing backend URL");
                System.exit(1);
                return;
            }

            URL backendURL;
            try {
                backendURL = URI.create(cli.getArgs()[0]).toURL();
            } catch (Exception e) {
                printHelp("Invalid backend URL: " + cli.getArgs()[0]);
                System.exit(1);
                return;
            }

            SpringFMClient client = new SpringFMClient(backendURL);
            LeafRadioCLIApp.Builder builder = new LeafRadioCLIApp.Builder(client);

            if (cli.hasOption('v')) builder.verbose();
            if (cli.hasOption('p')) builder.profile(cli.getOptionValue('p'));
            if (cli.hasOption('s')) builder.service(cli.getParsedOptionValue('s'));
            if (cli.hasOption('F')) {
                String freqString = cli.getOptionValue('F');
                builder.frequency(RadioUtils.parseFrequencyString(freqString));
            }
            if (cli.hasOption('G')) builder.gain(cli.getParsedOptionValue('G'));

            LeafRadioCLIApp app = builder.build();
            switch (args[0].toLowerCase()) {
                case "play" -> app.play();
                case "probe" -> app.probe();
                default -> {
                    printHelp("Unrecognized command: " + args[0]);
                    System.exit(1);
                }
            }
        } catch (ParseException e) {
            printHelp("Invalid command line argument: "
                    + (e.getCause() == null ? e.getMessage() : e.getCause().getMessage()));
            System.exit(4);
            return;
        } catch (IllegalArgumentException e) {
            printHelp(e.getMessage());
            System.exit(3);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
            return;
        }
    }

    private static void printHelp(String msg) {
        String file = new File(CLIMain.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getName();
        new HelpFormatter().printHelp(file + " [probe|play] [options] [base URL]", msg == null ? "\n" : msg + "\n\n",
                OPTIONS, null);
    }
}
