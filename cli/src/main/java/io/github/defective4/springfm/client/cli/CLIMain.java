package io.github.defective4.springfm.client.cli;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import io.github.defective4.springfm.client.SpringFMClient;

public class CLIMain {

    private static final Options options = new Options()
            .addOption(Option.builder("h").desc("Print this help").longOpt("help").build())
            .addOption(Option.builder().desc("Probe profiles and services and exit").longOpt("probe").build())
            .addOption(Option.builder().desc("Play service").longOpt("play").build())
            .addOption(
                    Option.builder("p").desc("Profile to use").longOpt("profile").argName("profile").hasArg().build())
            .addOption(Option.builder("s").desc("Service ID to play").converter(Integer::parseInt).longOpt("service")
                    .hasArg().argName("service id").build())
            .addOption(Option.builder("f").desc("Force unsupported arguments").longOpt("force").build());

    public static void main(String[] args) {
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cli = parser.parse(options, args);
            if (cli.hasOption('h')) {
                printHelp(null);
                return;
            }
            if (cli.getArgs().length == 0) {
                printHelp("Missing backend URL");
                return;
            }

            String urlString = cli.getArgs()[0];
            URL baseUrl;
            try {
                baseUrl = URI.create(urlString).toURL();
            } catch (Exception e) {
                System.err.println("Invalid backend URL: " + urlString);
                System.exit(1);
                return;
            }

            LeafRadioApp app = new LeafRadioApp(new SpringFMClient(baseUrl));

            if (cli.hasOption("--probe")) {
                app.probeServices();
            } else if (cli.hasOption("--play")) {
                if (!cli.hasOption('p')) {
                    printHelp("--play requires --profile");
                    System.exit(2);
                    return;
                }
                int serviceId = cli.hasOption('s') ? cli.getParsedOptionValue('s') : -2;
                String profile = cli.getOptionValue('p');
                app.playService(profile, serviceId, cli.hasOption('f'));
            } else {
                printHelp("Either --probe or --play is required");
                System.exit(2);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printHelp(String msg) {
        String file = new File(CLIMain.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getName();
        new HelpFormatter().printHelp("java -jar " + file + " [options] [backend URL]",
                (msg == null ? "" : msg + "\n") + "\nAvailable options:", options, null);
    }
}
