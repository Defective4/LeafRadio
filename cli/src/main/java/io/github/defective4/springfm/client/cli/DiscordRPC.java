package io.github.defective4.springfm.client.cli;

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

public class DiscordRPC {

    private final Activity act;
    private final ActivityManager activityManager;
    private final long clientId = 1403442344556232774L;
    private final Core core;

    public DiscordRPC() {
        Core core;
        ActivityManager activityManager;
        Activity act;
        try {
            CreateParams params = new CreateParams();
            params.setClientID(clientId);
            params.setFlags(Flags.DEFAULT);
            core = new Core(params);
            core.setLogHook(LogLevel.VERBOSE, (t, u) -> {});
            activityManager = core.activityManager();
            act = new Activity();
            act.setDetails("");
            act.setState("Idling");
            act.setType(ActivityType.LISTENING);
            act.setActivityButtonsMode(ActivityButtonsMode.BUTTONS);
            act.addButton(new ActivityButton("Show on GitHub", "https://github.com/Defective4/LeafRadio"));
            act.timestamps().setStart(Instant.now());
        } catch (Exception e) {
            act = null;
            activityManager = null;
            core = null;
        }
        this.core = core;
        this.act = act;
        this.activityManager = activityManager;
    }

    public boolean isAvailable() {
        return activityManager != null && act != null;
    }

    public void setActivity(String state, String details) {
        if (!isAvailable()) return;
        act.setState(state);
        act.setDetails(details);
        activityManager.updateActivity(act);
    }
}
