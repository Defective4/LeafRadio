package io.github.defective4.springfm.client.cli;

import java.io.IOException;
import java.util.List;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import io.github.defective4.springfm.client.SpringFMClient;
import io.github.defective4.springfm.client.audio.AudioPlayer;
import io.github.defective4.springfm.client.audio.AudioPlayerEventAdapter;
import io.github.defective4.springfm.client.utils.RadioUtils;
import io.github.defective4.springfm.server.data.AnalogTuningInformation;
import io.github.defective4.springfm.server.data.AudioAnnotation;
import io.github.defective4.springfm.server.data.AuthResponse;
import io.github.defective4.springfm.server.data.DigitalTuningInformation;
import io.github.defective4.springfm.server.data.GainInformation;
import io.github.defective4.springfm.server.data.ProfileInformation;
import io.github.defective4.springfm.server.data.ServiceInformation;

public class LeafRadioApp {
    private final SpringFMClient client;
    private final boolean enableDiscordPresence;
    private AudioAnnotation lastAnnotation;

    private float lastFreq = -1;
    private boolean muteNonMusic;
    private final AudioPlayer player;
    private ProfileInformation prof;
    private DiscordRPC rpc;
    private ServiceInformation serviceInfo;

    public LeafRadioApp(SpringFMClient client, boolean enableDiscordPresence) {
        this.client = client;
        this.enableDiscordPresence = enableDiscordPresence;
        player = new AudioPlayer(client);
        player.addListener(new AudioPlayerEventAdapter() {
            private long lastAnnotationTime = 0;

            @Override
            public void analogTuned(float frequency) {
                if (serviceInfo != null && serviceInfo.getAnalogTuning() != null)
                    frequency *= serviceInfo.getAnalogTuning().getStep();
                if (frequency == lastFreq) return;
                lastFreq = frequency;
                updateRPC();
                System.err.println("Server changed frequency to " + RadioUtils.createFrequencyString(frequency));
            }

            @Override
            public void annotationReceived(AudioAnnotation annotation) {
                if (muteNonMusic) player.setMuted(annotation.isNonMusic());
                if (annotation.equals(lastAnnotation) && System.currentTimeMillis() - lastAnnotationTime < 5000) return;
                lastAnnotationTime = System.currentTimeMillis();
                updateRPC();
                if (!annotation.equals(lastAnnotation))
                    System.err.println("Server sent an audio annotation: " + annotation.getTitle() + " | "
                            + annotation.getDescription() + " | Music: " + !annotation.isNonMusic());
                lastAnnotation = annotation;
            }

            @Override
            public void gainChanged(float newGain) {
                System.err.println("Server changed gain to " + newGain + " dB");
            }

            @Override
            public void playerErrored(Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void playerStopped() {
                System.exit(1);
            }

            @Override
            public void serviceChanged(int serviceIndex) {
                lastAnnotation = null;
                lastFreq = -1;
                updateRPC();
                System.err.println("Server changed service to #" + serviceIndex);
                if (prof != null) {
                    serviceInfo = serviceIndex >= 0 && serviceIndex < prof.getServices().size()
                            ? prof.getServices().get(serviceIndex)
                            : null;
                }
            }
        });
    }

    public void playService(String profile, int service, boolean force, int frequency, float gain, boolean muteNonMusic)
            throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        this.muteNonMusic = muteNonMusic;
        System.err.println("Authenticating with " + client.getBaseURL() + "...");
        AuthResponse auth = client.auth();
        if (!force) {
            boolean found = auth.getProfiles().stream().anyMatch(p -> p.getName().equals(profile));
            if (!found) {
                System.err.println("Profile \"" + profile
                        + "\" was not found in data returned by server. Use --force to bypass this check");
                return;
            }
        }
        if (service >= -1) {
            System.err.println("Setting service to " + service + "...");
            client.setService(profile, service);
            prof = auth.getProfiles().stream().filter(p -> p.getName().equals(profile)).findAny().orElse(null);
            if (prof != null) {
                if (service >= 0 && service < prof.getServices().size()) {
                    serviceInfo = prof.getServices().get(service);
                }
            }
            if (!analogTune(profile, frequency) || !adjustGain(profile, gain)) return;
        }
        System.err.println("Starting player...");
        player.start(profile);
        updateRPC();
        synchronized (LeafRadioApp.class) {
            try {
                LeafRadioApp.class.wait();
            } catch (InterruptedException e) {}
        }
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
                System.out.print("    - Gain: ");
                GainInformation gainInfo = service.getGainInfo();
                System.out.println(gainInfo.isGainSupported() ? gainInfo.getMaxGain() + " db" : "Unsupported");
                System.out.print("    - Analog tuning: ");
                AnalogTuningInformation analogTuning = service.getAnalogTuning();
                if (analogTuning == null) {
                    System.out.println("Unsupported");
                } else {
                    System.out.println();
                    System.out.println("        Min. frequency: " + analogTuning.getMinFreq() + " Hz");
                    System.out.println("        Max. frequency: " + analogTuning.getMaxFreq() + " Hz");
                    System.out.println("        Step: " + analogTuning.getStep() + " Hz");
                }
                System.out.print("    - Digital tuning: ");
                DigitalTuningInformation digitalTuning = service.getDigitalTuning();
                if (digitalTuning == null) {
                    System.out.println("Unsupported");
                } else {
                    System.out.println();
                    System.out.println("        Stations:");
                    for (String station : digitalTuning.getStations()) {
                        System.out.println("        - " + station);
                    }
                }
            }
            System.out.println();
        }
    }

    private boolean adjustGain(String profile, float gain) throws IOException {
        if (gain >= 0) {
            if (serviceInfo == null) {
                System.err.println("Invalid service or profile - gain adjusting is not available.");
                return false;
            }
            if (!serviceInfo.getGainInfo().isGainSupported()) {
                System.err.println("This service doesn't support gain adjusting.");
                return false;
            }
            if (gain > serviceInfo.getGainInfo().getMaxGain()) {
                System.err.println("Gain value out of range");
                return false;
            }
            System.err.println("Setting gain to " + gain + " dB");
            client.setGain(profile, gain);
        }
        return true;
    }

    private boolean analogTune(String profile, int frequency) throws IOException {
        if (frequency != -1) {
            if (serviceInfo == null) {
                System.err.println("Invalid service or profile - tuning is not available.");
                return false;
            }
            if (serviceInfo.getAnalogTuning() == null) {
                System.err.println("This service doesn't support analog tuning.");
                return false;
            }
            System.err.println("Tuning to " + RadioUtils.createFrequencyString(frequency));
            client.analogTune(profile, (int) (frequency / serviceInfo.getAnalogTuning().getStep()));
        }
        return true;
    }

    private synchronized void updateRPC() {
        if (!enableDiscordPresence) return;
        if (rpc == null) {
            rpc = new DiscordRPC();
        }
        if (lastAnnotation != null) {
            String title = lastAnnotation.getTitle();
            if (title != null) title = title.strip();
            rpc.setActivity(lastAnnotation.getDescription(), title);
        } else if (lastFreq >= 0) {
            rpc.setActivity("Listening to " + RadioUtils.createFrequencyString(lastFreq), null);
        } else {
            rpc.setActivity("Listening", null);
        }
    }
}
