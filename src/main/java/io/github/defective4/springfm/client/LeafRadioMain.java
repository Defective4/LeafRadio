package io.github.defective4.springfm.client;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;

import io.github.defective4.springfm.client.components.ProgressDialog;
import io.github.defective4.springfm.client.components.RadioComponents;
import io.github.defective4.springfm.client.components.ServerConnectDialog;
import io.github.defective4.springfm.client.player.PlayerEventListener;
import io.github.defective4.springfm.client.player.RadioPlayer;
import io.github.defective4.springfm.client.util.DialogUtils;
import io.github.defective4.springfm.client.util.FontUtils;
import io.github.defective4.springfm.client.web.SpringFMClient;
import io.github.defective4.springfm.server.data.AnalogTuningInformation;
import io.github.defective4.springfm.server.data.AudioAnnotation;
import io.github.defective4.springfm.server.data.AuthResponse;
import io.github.defective4.springfm.server.data.DigitalTuningInformation;
import io.github.defective4.springfm.server.data.ProfileInformation;
import io.github.defective4.springfm.server.data.ServiceInformation;

public class LeafRadioMain {

    public static final LeafRadioMain INSTANCE = new LeafRadioMain();

    private SpringFMClient client;
    private Label connectedLabel;

    private MenuItem connectItem;
    private Label descriptionLabel;
    private MenuItem disconnectItem;
    private Scale freqScale;
    private RadioPlayer player;
    private Menu profilesMenu;
    private Combo serviceCombo;
    private Combo stationCombo;

    private Composite stationSettingPanel;
    private Label titleLabel;
    protected Shell shell;

    public void layout(boolean changed, boolean all) {
        shell.layout(changed, all);
        adjustMinSize();
    }

    public void open() {
        Display display = Display.getDefault();
        createContents();
        shell.open();
        shell.layout();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    public void setConnetedLabelText(String text) {
        connectedLabel.setText(text);
        connectedLabel.getParent().layout(true, true);
        connectedLabel.getParent().getParent().layout(true, true);
    }

    private void adjustMinSize() {
        Point current = shell.getSize();
        shell.pack();
        shell.setMinimumSize(shell.getSize());
        shell.setSize(current);
    }

    private void connectProfile(ProfileInformation profile) {
        try {
            player.start(profile);
            List<ServiceInformation> services = profile.getServices();
            String[] items = new String[services.size() + 1];
            for (int i = 0; i < services.size(); i++) {
                items[i + 1] = services.get(i).getName();
            }
            items[0] = "(No service)";
            serviceCombo.setItems(items);
            serviceCombo.setEnabled(true);
            serviceCombo.select(0);
            updateStationControls();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disconnect(boolean profileOnly) {
        player.stop();

        if (!profileOnly) {
            player.setClient(null);
            client = null;
            RadioComponents.createProfileItems(profilesMenu, null, prof -> {});
            disconnectItem.setEnabled(false);
            connectItem.setEnabled(true);
            setConnetedLabelText("Not connected");
        }

        descriptionLabel.setText("Select a profile to start listening");
        titleLabel.setText("Not connected");

        serviceCombo.setItems(new String[] { "Not connected" });
        serviceCombo.setEnabled(false);
        serviceCombo.select(0);

        updateStationControls();
    }

    private void updateStationControls() {
        int index = serviceCombo.getSelectionIndex() - 1;
        for (Control ctl : stationSettingPanel.getChildren()) ctl.dispose();
        if (index >= 0) {
            ServiceInformation svcInfo = player.getProfile().getServices().get(index);
            Button applyBtn;
            switch (svcInfo.getTuningType()) {
                case ServiceInformation.TUNING_TYPE_DIGITAL -> {
                    DigitalTuningInformation tuningInfo = svcInfo.getDigitalTuning();
                    stationCombo = RadioComponents.createStationComboPanel(stationSettingPanel,
                            tuningInfo.getStations());
                    applyBtn = RadioComponents.createApplyStationButton(stationSettingPanel);
                }
                case ServiceInformation.TUNING_TYPE_ANALOG -> {
                    AnalogTuningInformation tuningInfo = svcInfo.getAnalogTuning();
                    AtomicReference<Button> buttonRef = new AtomicReference<>();
                    freqScale = RadioComponents.createStationFreqPanel(stationSettingPanel, tuningInfo.getMinFreq(),
                            tuningInfo.getMaxFreq(), tuningInfo.getStep(), panel -> {
                                Button btn = RadioComponents.createApplyStationButton(panel);
                                buttonRef.set(btn);
                                return btn;
                            });
                    applyBtn = buttonRef.get();
                }
                default -> {
                    applyBtn = RadioComponents.createApplyStationButton(stationSettingPanel);
                }
            }
            if (freqScale != null && !freqScale.isDisposed()) {
                applyBtn.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            client.analogTune(player.getProfile().getName(), freqScale.getSelection());
                            freqScale.setEnabled(false);
                            applyBtn.setEnabled(false);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            DialogUtils.showException(shell, e2);
                        }
                    }
                });
                freqScale.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        applyBtn.setEnabled(true);
                    }
                });
            }
            if (stationCombo != null && !stationCombo.isDisposed()) {
                applyBtn.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            client.digitalTune(player.getProfile().getName(), stationCombo.getSelectionIndex());
                            stationCombo.setEnabled(false);
                            applyBtn.setEnabled(false);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                            DialogUtils.showException(shell, e1);
                        }
                    }
                });
                stationCombo.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        applyBtn.setEnabled(true);
                    }
                });
            }
            layout(true, true);
        }
    }

    protected void createContents() {
        shell = new Shell();
        shell.setSize(425, 300);
        shell.setText("LeafRadio");
        shell.setLayout(new GridLayout(1, false));

        titleLabel = new Label(shell, SWT.CENTER);
        titleLabel.setFont(FontUtils.deriveFont(titleLabel.getFont(), 24, SWT.BOLD));
        titleLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        descriptionLabel = new Label(shell, SWT.CENTER);
        descriptionLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        stationSettingPanel = new Composite(shell, SWT.NONE);
        stationSettingPanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        stationSettingPanel.setLayout(new GridLayout(2, false));

        Composite serviceSettingPanel = new Composite(shell, SWT.NONE);
        serviceSettingPanel.setLayout(new GridLayout(2, false));
        serviceSettingPanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label serviceLabel = new Label(serviceSettingPanel, SWT.NONE);
        serviceLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        serviceLabel.setText("Service: ");

        serviceCombo = new Combo(serviceSettingPanel, SWT.READ_ONLY);
        serviceCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        serviceCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    client.setService(player.getProfile().getName(), serviceCombo.getSelectionIndex() - 1);
                    serviceCombo.setEnabled(false);
                    updateStationControls();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    DialogUtils.showException(shell, e1);
                }
            }
        });

        Menu menu = new Menu(shell, SWT.BAR);
        shell.setMenuBar(menu);

        MenuItem serverMenu = new MenuItem(menu, SWT.CASCADE);
        serverMenu.setText("Server");

        Menu connectMenu = new Menu(serverMenu);
        serverMenu.setMenu(connectMenu);

        connectItem = new MenuItem(connectMenu, SWT.NONE);
        connectItem.setText("Connect...");

        disconnectItem = new MenuItem(connectMenu, SWT.NONE);
        disconnectItem.setEnabled(false);
        disconnectItem.setText("Disconnect");

        new MenuItem(connectMenu, SWT.SEPARATOR);

        MenuItem profilesItem = new MenuItem(connectMenu, SWT.CASCADE);
        profilesItem.setText("Profile");

        profilesMenu = new Menu(profilesItem);
        profilesItem.setMenu(profilesMenu);

        RadioComponents.createProfileItems(profilesMenu, null, prof -> {});

        Label spacerLabel = new Label(shell, SWT.NONE);
        spacerLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));

        Label label = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Composite composite = new Composite(shell, SWT.NONE);
        composite.setLayout(new RowLayout(SWT.HORIZONTAL));
        composite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

        connectedLabel = new Label(composite, SWT.NONE);
        connectedLabel.setBounds(0, 0, 62, 19);

        disconnectItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                disconnect(false);
            }
        });

        connectItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                URL url = new ServerConnectDialog(shell).open();
                if (url != null) {
                    new ProgressDialog(shell, "Connecting...").open(shell -> {
                        SpringFMClient client = new SpringFMClient(url);
                        AuthResponse response = client.authenticate();
                        Display.getDefault().asyncExec(() -> {
                            RadioComponents.createProfileItems(profilesMenu, response.getProfiles(), profile -> {
                                if (profile == null) {
                                    disconnect(true);
                                    setConnetedLabelText("Authenticated");
                                } else
                                    connectProfile(profile);
                            });
                            disconnectItem.setEnabled(true);
                            connectItem.setEnabled(false);
                            setConnetedLabelText("Authenticated");
                            MessageBox box = new MessageBox(LeafRadioMain.this.shell, SWT.OK);
                            box.setText("Success");
                            box.setMessage("Connected to \"" + response.getInstanceName()
                                    + "\"!\nChoose a profile from the Server menu.");
                            box.open();
                        });
                        LeafRadioMain.this.client = client;
                        player.setClient(client);
                    });
                }
            }

        });

        player = new RadioPlayer(new PlayerEventListener() {

            @Override
            public void analogTune(int freq) {
                if (freqScale != null && !freqScale.isDisposed()) {
                    freqScale.setSelection(freq);
                    freqScale.setEnabled(true);
                    Event e = new Event();
                    freqScale.getTypedListeners(SWT.Selection, SelectionListener.class).findFirst().get()
                            .widgetSelected(null);
                }
            }

            @Override
            public void audioAnnotationReceived(AudioAnnotation annotation) {
                titleLabel.setText(annotation.getTitle() == null ? "<No title>" : annotation.getTitle());
                descriptionLabel.setText(annotation.getDescription() == null ? "" : annotation.getDescription());
            }

            @Override
            public void digitalTune(int index) {
                if (stationCombo != null && !stationCombo.isDisposed()) {
                    stationCombo.select(index);
                    stationCombo.setEnabled(true);
                }
            }

            @Override
            public void playerErrored(Exception ex) {
                DialogUtils.showException(shell, ex);
                new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        disconnect(true);
                    }
                }.widgetSelected(null);
            }

            @Override
            public void serviceChanged(int index) {
                serviceCombo.select(index + 1);
                serviceCombo.setEnabled(true);
                updateStationControls();
            }
        });

        disconnect(false);

        adjustMinSize();
    }

    public static void main(String[] args) {
        try {
            INSTANCE.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
