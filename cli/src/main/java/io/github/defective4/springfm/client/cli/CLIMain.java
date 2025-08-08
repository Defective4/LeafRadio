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
            .addOption(Option.builder("p").desc("Probe profiles and services and exit").longOpt("probe").build())
            .addOption(Option.builder("s").desc("Play service").longOpt("play").build());

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

            if (cli.hasOption('p')) {
                app.probeServices();
            } else if (cli.hasOption('s')) {

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
