package io.github.defective4.springfm.cli;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import io.github.defective4.springfm.cli.util.IndentationPrinter;
import io.github.defective4.springfm.client.SpringFMClient;
import io.github.defective4.springfm.server.data.AnalogTuningInformation;
import io.github.defective4.springfm.server.data.AuthResponse;
import io.github.defective4.springfm.server.data.DigitalTuningInformation;
import io.github.defective4.springfm.server.data.GainInformation;
import io.github.defective4.springfm.server.data.ProfileInformation;
import io.github.defective4.springfm.server.data.ServiceInformation;

public class LeafRadioCLIApp {

    public static class Builder {
        private final SpringFMClient client;
        private String profile;
        private boolean verbose;

        public Builder(SpringFMClient client) {
            this.client = Objects.requireNonNull(client);
        }

        public LeafRadioCLIApp build() {
            return new LeafRadioCLIApp(client, profile, verbose);
        }

        public Builder profile(String profile) {
            this.profile = profile;
            return this;
        }

        public Builder verbose() {
            verbose = true;
            return this;
        }
    }

    private final SpringFMClient client;
    private final String profile;
    private final boolean verbose;

    private LeafRadioCLIApp(SpringFMClient client, String profile, boolean verbose) {
        this.client = client;
        this.profile = profile;
        this.verbose = verbose;
    }

    public void probe() throws IOException {
        logVerbose("Connecting to the server...");
        AuthResponse auth = client.auth();
        logVerbose(String.format("Received %s profiles with %s services total", auth.getProfiles().size(),
                auth.getProfiles().stream().mapToInt(p -> p.getServices().size()).sum()));
        List<ProfileInformation> toPrint;
        if (profile != null) {
            toPrint = auth.getProfiles().stream().filter(p -> p.getName().equals(profile)).toList();
            if (toPrint.isEmpty()) throw new IllegalArgumentException("Profile \"" + profile + "\" not found.");
        } else
            toPrint = auth.getProfiles();

        IndentationPrinter printer = new IndentationPrinter();

        for (ProfileInformation profile : toPrint) {
            printer.println(profile.getName() + ":");
            printer.indentationUp();
            for (ServiceInformation service : profile.getServices()) {
                printer.println("Service #" + service.getIndex() + ":");
                printer.indentationUp();

                printer.println("Name: " + service.getName());

                GainInformation gain = service.getGainInfo();
                printer.println("Gain: " + (gain.isGainSupported() ? gain.getMaxGain() + " dB" : "Unsupported"));

                printer.println("Tuning type: " + switch (service.getTuningType()) {
                    case ServiceInformation.TUNING_TYPE_ANALOG -> "Analog";
                    case ServiceInformation.TUNING_TYPE_DIGITAL -> "Digital";
                    default -> "Unknown";
                });

                AnalogTuningInformation analog = service.getAnalogTuning();
                if (analog != null) {
                    printer.println("Analog tuning:");
                    printer.indentationUp();
                    printer.println("Min frequency: " + analog.getMinFreq() + " Hz");
                    printer.println("Max frequency: " + analog.getMaxFreq() + " Hz");
                    printer.println("Step: " + analog.getStep() + " Hz");
                    printer.indentationDown();
                }

                DigitalTuningInformation digital = service.getDigitalTuning();
                if (digital != null) {
                    printer.println("Digital tuning:");
                    printer.indentationUp();
                    printer.println("Stations:");
                    printer.indentationUp();
                    List<String> stations = digital.getStations();
                    for (int i = 0; i < stations.size(); i++) {
                        String station = stations.get(i);
                        printer.printls(i + ": " + station);
                    }
                    printer.indentationDown();
                    printer.indentationDown();
                }

                printer.indentationDown();
            }
            printer.indentationDown();
        }
    }

    private void logVerbose(String msg) {
        if (!verbose) return;
        System.err.println(msg);
    }
}
