package io.github.defective4.springfm.client.components;

import java.util.List;
import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Scale;

import io.github.defective4.springfm.client.ProfileSelectionCallback;
import io.github.defective4.springfm.client.util.RadioUnits;
import io.github.defective4.springfm.server.data.ProfileInformation;

public class RadioComponents {

    public static Button createApplyStationButton(Composite stationSettingPanel) {
        Button applyStationButton = new Button(stationSettingPanel, SWT.NONE);
        applyStationButton.setEnabled(false);
        applyStationButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        applyStationButton.setText("Apply");
        return applyStationButton;
    }

    public static void createProfileItems(Menu profilesMenu, List<ProfileInformation> profiles,
            ProfileSelectionCallback callback) {
        for (MenuItem item : profilesMenu.getItems()) item.dispose();
        if (profiles == null) {
            MenuItem disabled = new MenuItem(profilesMenu, 0);
            disabled.setEnabled(false);
            disabled.setText("(Connect to see available profiles)");
        } else if (profiles.isEmpty()) {
            MenuItem disabled = new MenuItem(profilesMenu, 0);
            disabled.setEnabled(false);
            disabled.setText("(No available profiles)");
        } else {
            MenuItem deselectItem = new MenuItem(profilesMenu, 0);
            deselectItem.setText("(No profile)");
            deselectItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    callback.profileSelected(null);
                }
            });

            new MenuItem(profilesMenu, SWT.SEPARATOR);
            for (ProfileInformation info : profiles) {
                MenuItem item = new MenuItem(profilesMenu, 0);
                item.setText(info.getName());
                item.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        callback.profileSelected(info);
                    }
                });
            }
        }
    }

    public static Combo createStationComboPanel(Composite stationSettingPanel, List<String> stations) {
        Composite stationPanel = new Composite(stationSettingPanel, SWT.NONE);
        stationPanel.setLayout(new GridLayout(2, false));
        stationPanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label stationLabel = new Label(stationPanel, SWT.NONE);
        stationLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        stationLabel.setText("Station: ");

        Combo stationCombo = new Combo(stationPanel, SWT.READ_ONLY);
        stationCombo.setItems(stations.toArray(new String[0]));
        stationCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        return stationCombo;
    }

    public static Scale createStationFreqPanel(Composite stationSettingPanel, float minFreq, float maxFreq, float step,
            Function<Composite, Button> applyButton) {
        Composite frequencyPanel = new Composite(stationSettingPanel, SWT.NONE);
        frequencyPanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        frequencyPanel.setLayout(new GridLayout(6, false));

        Label lblFreqMin = new Label(frequencyPanel, SWT.NONE);
        lblFreqMin.setText(RadioUnits.toHzUnits(minFreq));

        new Label(frequencyPanel, SWT.NONE);

        Scale freqScale = new Scale(frequencyPanel, SWT.NONE);
        freqScale.setMaximum((int) (maxFreq / step));
        freqScale.setMinimum((int) (minFreq / step));
        freqScale.setSelection((int) (minFreq / step));
        freqScale.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        new Label(frequencyPanel, SWT.NONE);

        Label lblFreqMax = new Label(frequencyPanel, SWT.NONE);
        lblFreqMax.setText(RadioUnits.toHzUnits(maxFreq));
        applyButton.apply(frequencyPanel);
        new Label(frequencyPanel, SWT.NONE);
        new Label(frequencyPanel, SWT.NONE);

        Label lblFreqCurrent = new Label(frequencyPanel, SWT.NONE);
        lblFreqCurrent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        new Label(frequencyPanel, SWT.NONE);
        lblFreqCurrent.setText("Current: 88 MHz");
        new Label(frequencyPanel, SWT.NONE);
        new Label(frequencyPanel, SWT.NONE);
        new Label(stationSettingPanel, SWT.NONE);

        freqScale.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                lblFreqCurrent.setText("Current: " + RadioUnits.toHzUnits(freqScale.getSelection() * step));
            }
        });

        return freqScale;
    }

}
