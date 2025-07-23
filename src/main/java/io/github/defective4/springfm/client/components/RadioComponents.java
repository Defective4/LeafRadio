package io.github.defective4.springfm.client.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;

public class RadioComponents {

    public static void createApplyStationButton(Composite stationSettingPanel) {
        Button applyStationButton = new Button(stationSettingPanel, SWT.NONE);
        applyStationButton.setEnabled(false);
        applyStationButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        applyStationButton.setText("Apply");
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

    public static void createStationFreqPanel(Composite serviceSettingPanel) {
        Composite frequencyPanel = new Composite(serviceSettingPanel, SWT.NONE);
        frequencyPanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        frequencyPanel.setLayout(new GridLayout(7, false));

        Label lblFreqMin = new Label(frequencyPanel, SWT.NONE);
        lblFreqMin.setText("88 Mhz");

        new Label(frequencyPanel, SWT.NONE);

        Slider slider = new Slider(frequencyPanel, SWT.NONE);
        slider.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        new Label(frequencyPanel, SWT.NONE);

        Label lblFreqMax = new Label(frequencyPanel, SWT.NONE);
        lblFreqMax.setText("108 MHz");
        new Label(frequencyPanel, SWT.NONE);
        new Label(frequencyPanel, SWT.NONE);
    }

}
