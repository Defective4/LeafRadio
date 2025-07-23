package io.github.defective4.springfm.client;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import io.github.defective4.springfm.client.util.FontUtils;

public class LeafRadioMain {

    protected Shell shlLeafradio;

    public void open() {
        Display display = Display.getDefault();
        createContents();
        shlLeafradio.open();
        shlLeafradio.layout();
        while (!shlLeafradio.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    protected void createContents() {
        shlLeafradio = new Shell();
        shlLeafradio.setSize(450, 300);
        shlLeafradio.setText("LeafRadio");
        shlLeafradio.setLayout(new GridLayout(1, false));

        Label titleLabel = new Label(shlLeafradio, SWT.NONE);
        titleLabel.setFont(FontUtils.deriveFont(titleLabel.getFont(), 24, SWT.BOLD));
        titleLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
        titleLabel.setText("Not connected");

        Label descriptionLabel = new Label(shlLeafradio, SWT.NONE);
        descriptionLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        descriptionLabel.setText("Connect to a server to start listening");

        Composite stationSettingPanel = new Composite(shlLeafradio, SWT.NONE);
        stationSettingPanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        stationSettingPanel.setLayout(new GridLayout(2, false));

        Composite serviceSettingPanel = new Composite(shlLeafradio, SWT.NONE);
        serviceSettingPanel.setLayout(new GridLayout(3, false));
        serviceSettingPanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label serviceLabel = new Label(serviceSettingPanel, SWT.NONE);
        serviceLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        serviceLabel.setText("Service: ");

        Combo serviceCombo = new Combo(serviceSettingPanel, SWT.READ_ONLY);
        serviceCombo.setItems(new String[] { "Not connected" });
        serviceCombo.setEnabled(false);
        serviceCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        serviceCombo.select(0);

        Button connectButton = new Button(serviceSettingPanel, SWT.NONE);
        connectButton.setText("Connect");
    }

    public static void main(String[] args) {
        try {
            LeafRadioMain window = new LeafRadioMain();
            window.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
