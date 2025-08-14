package io.github.defective4.springfm.cli;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import io.github.defective4.leafradio.discord.DiscordIntegration;
import io.github.defective4.springfm.cli.util.IndentationPrinter;
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

public class LeafRadioCLIApp {

    public static class Builder {
        private AnnotationFormat annotationsFormat = AnnotationFormat.TEXT;
        private boolean changeFrequency;
        private boolean changeGain;
        private boolean changeService;
        private boolean changeStation;
        private final SpringFMClient client;
        private long discordAppId = DEFAULT_DISCORD_APP_ID;
        private boolean displayAnnotations;
        private boolean enableDiscord;
        private int frequency = -1;
        private float gain;
        private String profile;
        private int service;
        private int station = -1;
        private boolean verbose;

        public Builder(SpringFMClient client) {
            this.client = Objects.requireNonNull(client);
        }

        public Builder annotationsFormat(AnnotationFormat annotationsFormat) {
            this.annotationsFormat = Objects.requireNonNull(annotationsFormat);
            return this;
        }

        public LeafRadioCLIApp build() {
            if (changeFrequency && !changeService)
                throw new IllegalArgumentException("You need to specify a service index to change frequency.");
            if (changeGain && !changeService)
                throw new IllegalArgumentException("You need to specify a service index to adjust gain.");
            return new LeafRadioCLIApp(client, profile, verbose, changeService, service, changeFrequency, frequency,
                    changeGain, gain, changeStation, station, displayAnnotations, annotationsFormat, enableDiscord,
                    discordAppId);
        }

        public Builder discordAppId(long discordAppId) {
            if (discordAppId < 0) throw new IllegalArgumentException("Illegal discord application id");
            this.discordAppId = discordAppId;
            return this;
        }

        public Builder displayAnnotations() {
            displayAnnotations = true;
            return this;
        }

        public Builder enableDiscord() {
            enableDiscord = true;
            return this;
        }

        public Builder frequency(int frequency) {
            this.frequency = frequency;
            changeFrequency = true;
            return this;
        }

        public Builder gain(float gain) {
            if (gain < 0) throw new IllegalArgumentException("gain < 0");
            this.gain = gain;
            changeGain = true;
            return this;
        }

        public Builder profile(String profile) {
            this.profile = Objects.requireNonNull(profile);
            return this;
        }

        public Builder service(int service) {
            if (service < -1) throw new IllegalArgumentException("service < -1");
            this.service = service;
            changeService = true;
            return this;
        }

        public Builder setStation(int station) {
            if (station < 0) throw new IllegalArgumentException("station < 0");
            this.station = station;
            changeStation = true;
            return this;
        }

        public Builder verbose() {
            verbose = true;
            return this;
        }
    }

    private static final long DEFAULT_DISCORD_APP_ID = 1403442344556232774L;

    private final AnnotationFormat annotationsFormat;
    private final AudioPlayer audioPlayer;
    private AuthResponse auth;
    private final boolean changeFrequency;
    private final boolean changeGain;
    private final boolean changeService;
    private final boolean changeStation;
    private final SpringFMClient client;
    private ServiceInformation currentService;
    private final DiscordIntegration discord;
    private final boolean displayAnnotations;

    private int frequency;
    private float gain;
    private AudioAnnotation lastAnnotation;
    private long lastAnnotationTime = 0;
    private MessageDigest md;
    private ProfileInformation profile;
    private final String profileName;
    private final int service;

    private int station;
    private final boolean verbose;

    private LeafRadioCLIApp(SpringFMClient client, String profile, boolean verbose, boolean changeService, int service,
            boolean changeFrequency, int frequency, boolean changeGain, float gain, boolean changeStation, int station,
            boolean displayAnnotations, AnnotationFormat annotationsFormat, boolean enableDiscord, long discordAppId) {
        this.client = client;
        profileName = profile;
        this.verbose = verbose;
        this.changeService = changeService;
        this.service = service;
        this.changeFrequency = changeFrequency;
        this.frequency = frequency;
        this.changeGain = changeGain;
        this.gain = gain;
        this.changeStation = changeStation;
        this.station = station;
        this.displayAnnotations = displayAnnotations;
        this.annotationsFormat = annotationsFormat;
        audioPlayer = new AudioPlayer(client);
        audioPlayer.addListener(new AudioPlayerEventAdapter() {

            @Override
            public void analogTuned(float frequency) {
                float step = currentService == null || currentService.getAnalogTuning() == null ? 1
                        : currentService.getAnalogTuning().getStep();
                LeafRadioCLIApp.this.frequency = (int) (frequency * step);
                logVerbose("Server changed frequency to " + RadioUtils.createFrequencyString(frequency * step));
                updateDiscordStatus();
            }

            @Override
            public void annotationReceived(AudioAnnotation annotation) {
                if (!annotation.equals(lastAnnotation)) {
                    lastAnnotation = annotation;
                    if (displayAnnotations) annotationsFormat.getPrinter().accept(annotation);
                } else if (System.currentTimeMillis() - lastAnnotationTime < 5000) return;
                lastAnnotationTime = System.currentTimeMillis();
                updateDiscordStatus();
            }

            @Override
            public void audioFormatChanged(AudioFormat newFormat) {
                logVerbose("Server has changed the audio format: " + newFormat);
            }

            @Override
            public void digitalTuned(int index) {
                LeafRadioCLIApp.this.station = index;
                String station;
                DigitalTuningInformation digital = currentService == null ? null : currentService.getDigitalTuning();
                if (index >= 0 && digital != null && index < digital.getStations().size())
                    station = digital.getStations().get(index);
                else
                    station = "Unknown";
                logVerbose("Server changed station to " + station + " (#" + index + ")");
                updateDiscordStatus();
            }

            @Override
            public void gainChanged(float newGain) {
                LeafRadioCLIApp.this.gain = newGain;
                logVerbose("Server changed gain to " + newGain + " dB");
            }

            @Override
            public void playerErrored(Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void playerStopped() {
                System.exit(5);
            }

            @Override
            public void serviceChanged(int serviceIndex) {
                if (serviceIndex < 0 || serviceIndex >= LeafRadioCLIApp.this.profile.getServices().size())
                    currentService = null;
                else
                    currentService = LeafRadioCLIApp.this.profile.getServices().get(serviceIndex);
                logVerbose("Service changed to "
                        + (currentService == null ? "none" : currentService.getName() + " (#" + serviceIndex + ")"));
            }

        });
        discord = enableDiscord ? new DiscordIntegration(discordAppId) : null;
    }

    public void play()
            throws IOException, UnsupportedAudioFileException, LineUnavailableException, NoSuchAlgorithmException {
        if (profileName == null) throw new IllegalArgumentException("Profile name is required to play the stream");
        logVerbose("Authenticating with the server");
        auth = client.auth();
        md = MessageDigest.getInstance(auth.getHashAlgo());
        profile = auth.getProfiles().stream().filter(p -> p.getName().equals(profileName)).findAny()
                .orElseThrow(() -> new IllegalArgumentException("Profile \"" + profileName + "\" was not found."));

        validateService();
        validateFrequency();
        validateStation();
        validateGain();

        changeService();
        changeFrequency();
        changeStation();
        changeGain();

        logVerbose("Starting player");
        audioPlayer.setProfileVerifier(md, auth);
        audioPlayer.start(profile.getName());
        logVerbose("Started audio player with format " + audioPlayer.getFormat());

        updateDiscordStatus();

        synchronized (LeafRadioCLIApp.class) {
            try {
                LeafRadioCLIApp.class.wait();
            } catch (InterruptedException e) {}
        }
    }

    public void probe() throws IOException {
        logVerbose("Connecting to the server...");
        auth = client.auth();
        logVerbose(String.format("Received %s profiles with %s services total", auth.getProfiles().size(),
                auth.getProfiles().stream().mapToInt(p -> p.getServices().size()).sum()));
        List<ProfileInformation> toPrint;
        if (profileName != null) {
            toPrint = auth.getProfiles().stream().filter(p -> p.getName().equals(profileName)).toList();
            if (toPrint.isEmpty()) throw new IllegalArgumentException("Profile \"" + profileName + "\" was not found.");
        } else
            toPrint = auth.getProfiles();

        IndentationPrinter printer = new IndentationPrinter();

        printer.println("Instance name: " + auth.getInstanceName());
        printer.println("Config hash algorithm: " + auth.getHashAlgo());
        printer.println("Profiles:");
        printer.indentationUp();

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

    private void changeFrequency() throws IOException {
        if (changeFrequency) {
            logVerbose("Sending frequency change command (frequency = " + RadioUtils.createFrequencyString(frequency)
                    + ")");
            client.analogTune(profile.getName(), (int) (frequency / currentService.getAnalogTuning().getStep()));
        }
    }

    private void changeGain() throws IOException {
        if (changeGain) {
            logVerbose("Sending gain adjust command (gain = " + gain + " dB)");
            client.setGain(profile.getName(), gain);
        }
    }

    private void changeService() throws IOException {
        if (changeService) {
            logVerbose("Sending service change command (service = " + service + ")");
            client.setService(profile.getName(), service);
        }
    }

    private void changeStation() throws IOException {
        if (changeStation) {
            logVerbose("Sending digital tune command (station = " + station + ")");
            client.digitalTune(profile.getName(), station);
        }
    }

    private void logVerbose(String msg) {
        if (!verbose) return;
        System.err.println(msg);
    }

    private void updateDiscordStatus() {
        if (discord == null) return;
        if (lastAnnotation != null) {
            discord.update(lastAnnotation.getDescription(),
                    lastAnnotation.getTitle() == null ? null : lastAnnotation.getTitle().trim());
            return;
        }
        if (frequency >= 0) {
            discord.update("Listening to " + RadioUtils.createFrequencyString(frequency), null);
            return;
        }
        if (station >= 0) {
            if (profile != null) {
                if (service >= 0 && service < profile.getServices().size()) {
                    ServiceInformation svc = profile.getServices().get(service);
                    DigitalTuningInformation digital = svc.getDigitalTuning();
                    if (digital != null && station < digital.getStations().size()) {
                        discord.update("Listening to " + digital.getStations().get(station), null);
                        return;
                    }
                }
            }
        }
        discord.update("Listening", null);
    }

    private void validateFrequency() {
        if (changeFrequency) {
            if (currentService == null || currentService.getAnalogTuning() == null)
                throw new IllegalArgumentException("This service doesn't support analog tuning");
            AnalogTuningInformation analog = currentService.getAnalogTuning();
            float min = analog.getMinFreq();
            float max = analog.getMaxFreq();
            if (frequency < min || frequency > max)
                throw new IllegalArgumentException(String.format("Frequency %s is out of range of %s - %s",
                        RadioUtils.createFrequencyString(frequency), RadioUtils.createFrequencyString(min),
                        RadioUtils.createFrequencyString(max)));
        }
    }

    private void validateGain() {
        if (changeGain) {
            if (currentService == null || !currentService.getGainInfo().isGainSupported())
                throw new IllegalArgumentException("This service doesn't support gain adjusting");
            float maxGain = currentService.getGainInfo().getMaxGain();
            if (gain > maxGain) throw new IllegalArgumentException(
                    String.format("Gain %s dB is out of range of 0 - %s dB", gain, maxGain));
        }
    }

    private void validateService() {
        if (changeService) {
            if (service >= profile.getServices().size())
                throw new IllegalArgumentException("Service index out of bounds (index = " + service + ", size = "
                        + profile.getServices().size() + ")");
            currentService = service < 0 ? null : profile.getServices().get(service);
        }
    }

    private void validateStation() {
        if (changeStation) {
            if (currentService == null || currentService.getDigitalTuning() == null)
                throw new IllegalArgumentException("This service doesn't support digital tuning");
            int stations = currentService.getDigitalTuning().getStations().size();
            if (station >= stations) throw new IllegalArgumentException(
                    String.format("Station #%s is out of range of 0 - %s", station, stations - 1));
        }
    }
}
