package io.github.defective4.springfm.client.cli;

import java.io.IOException;
import java.util.List;

import io.github.defective4.springfm.client.SpringFMClient;
import io.github.defective4.springfm.server.data.AnalogTuningInformation;
import io.github.defective4.springfm.server.data.AuthResponse;
import io.github.defective4.springfm.server.data.DigitalTuningInformation;
import io.github.defective4.springfm.server.data.GainInformation;
import io.github.defective4.springfm.server.data.ProfileInformation;
import io.github.defective4.springfm.server.data.ServiceInformation;

public class LeafRadioApp {
    private final SpringFMClient client;

    public LeafRadioApp(SpringFMClient client) {
        this.client = client;
    }

    public void probeServices() throws IOException {
        System.err.println("Probing available services on " + client.getBaseURL() + "...");
        AuthResponse auth = client.auth();
        List<ProfileInformation> profiles = auth.getProfiles();
        for (ProfileInformation profile : profiles) {
            String name = profile.getName();
            System.out.println("Profile \"" + name + "\"");
            List<ServiceInformation> services = profile.getServices();
            for (ServiceInformation service : services) {
                System.out.println("  Service #" + service.getIndex() + ":");
                System.out.println("    - Name: " + service.getName());
                System.out.println("    - Tuning type: " + switch (service.getTuningType()) {
                    case ServiceInformation.TUNING_TYPE_ANALOG -> "Analog";
                    case ServiceInformation.TUNING_TYPE_DIGITAL -> "Digital";
                    default -> "Unknown";
                });
                System.out.print("      - Gain: ");
                GainInformation gainInfo = service.getGainInfo();
                System.out.println(gainInfo.isGainSupported() ? gainInfo.getMaxGain() + " db" : "Unsupported");
                System.out.print("      - Analog tuning: ");
                AnalogTuningInformation analogTuning = service.getAnalogTuning();
                if (analogTuning == null) {
                    System.out.println("Unsupported");
                } else {
                    System.out.println();
                    System.out.println("        Min. frequency: " + analogTuning.getMinFreq() + " Hz");
                    System.out.println("        Max. frequency: " + analogTuning.getMaxFreq() + " Hz");
                    System.out.println("        Step: " + analogTuning.getStep() + " Hz");
                }
                System.out.print("      - Digital tuning: ");
                DigitalTuningInformation digitalTuning = service.getDigitalTuning();
                if (digitalTuning == null) {
                    System.out.println("Unsupported");
                } else {
                    System.out.println();
                    System.out.println("        Stations:");
                    for (String station : digitalTuning.getStations()) {
                        System.out.println("            - " + station);
                    }
                }
            }
            System.out.println();
        }
    }
}
