package io.github.defective4.leafradio.discord;

import java.time.Instant;

import de.jcm.discordgamesdk.ActivityManager;
import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.CreateParams.Flags;
import de.jcm.discordgamesdk.LogLevel;
import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.activity.ActivityButton;
import de.jcm.discordgamesdk.activity.ActivityButtonsMode;
import de.jcm.discordgamesdk.activity.ActivityType;

public class DiscordIntegration {
    private final Activity activity = new Activity();
    private final long appId;
    private Core core;
    private final ActivityManager manager;

    @SuppressWarnings("resource")
    public DiscordIntegration(long appId) {
        this.appId = appId;
        ActivityManager manager;
        try {
            CreateParams params = new CreateParams();
            params.setClientID(appId);
            params.setFlags(Flags.DEFAULT);
            core = new Core(params);
            core.setLogHook(LogLevel.DEBUG, (t, u) -> {});
            manager = core.activityManager();
            activity.timestamps().setStart(Instant.now());
            activity.setType(ActivityType.LISTENING);
            activity.setActivityButtonsMode(ActivityButtonsMode.BUTTONS);
            activity.addButton(new ActivityButton("Show on GitHub", "https://github.com/Defective4/LeafRadio"));
        } catch (Exception e) {
            manager = null;
            System.err.println("Failed to start discord activity");
        }
        this.manager = manager;
    }

    public boolean isAvailable() {
        return manager != null;
    }

    public synchronized void update(String state, String details) {
        if (!isAvailable()) return;
        activity.setState(state);
        activity.setDetails(details);
        manager.updateActivity(activity);
    }

}
