package io.github.defective4.springfm.client.components;

import java.util.List;

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

import io.github.defective4.springfm.client.util.RadioUnits;
import io.github.defective4.springfm.server.data.ProfileInformation;

public class RadioComponents {

    public static void createApplyStationButton(Composite stationSettingPanel) {
        Button applyStationButton = new Button(stationSettingPanel, SWT.NONE);
        applyStationButton.setEnabled(false);
        applyStationButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        applyStationButton.setText("Apply");
    }

    public static void createProfileItems(Menu profilesMenu, List<ProfileInformation> profiles) {
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
            for (ProfileInformation info : profiles) {
                MenuItem item = new MenuItem(profilesMenu, 0);
                item.setText(info.getName());
            }
        }
    }

    public static void createStationComboPanel(Composite stationSettingPanel) {
        Composite stationPanel = new Composite(stationSettingPanel, SWT.NONE);
        stationPanel.setLayout(new GridLayout(2, false));
        stationPanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label stationLabel = new Label(stationPanel, SWT.NONE);
        stationLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        stationLabel.setText("Station: ");

        Combo stationCombo = new Combo(stationPanel, SWT.READ_ONLY);
        stationCombo.setItems(new String[] {});
        stationCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    }

    public static void createStationFreqPanel(Composite stationSettingPanel, float minFreq, int maxFreq, float step) {
        Composite frequencyPanel = new Composite(stationSettingPanel, SWT.NONE);
        frequencyPanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        frequencyPanel.setLayout(new GridLayout(5, false));

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
        new Label(frequencyPanel, SWT.NONE);
        new Label(frequencyPanel, SWT.NONE);

        Label lblFreqCurrent = new Label(frequencyPanel, SWT.NONE);
        lblFreqCurrent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
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
    }

}
