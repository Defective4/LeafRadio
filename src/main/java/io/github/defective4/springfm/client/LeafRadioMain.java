package io.github.defective4.springfm.client;

import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import io.github.defective4.springfm.client.components.ProgressDialog;
import io.github.defective4.springfm.client.components.RadioComponents;
import io.github.defective4.springfm.client.components.ServerConnectDialog;
import io.github.defective4.springfm.client.event.PlayerEventListener;
import io.github.defective4.springfm.client.event.RadioPlayer;
import io.github.defective4.springfm.client.util.DialogUtils;
import io.github.defective4.springfm.client.util.FontUtils;
import io.github.defective4.springfm.client.web.SpringFMClient;
import io.github.defective4.springfm.server.data.AudioAnnotation;
import io.github.defective4.springfm.server.data.AuthResponse;

public class LeafRadioMain {

    private SpringFMClient client;
    private MenuItem connectItem;

    private Label descriptionLabel;
    private MenuItem disconnectItem;
    private RadioPlayer player;
    private Menu profilesMenu;
    private Combo serviceCombo;
    private Label titleLabel;
    protected Shell shell;

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

    private void disconnect() {
        player.stop();
        player.setClient(null);
        client = null;

        RadioComponents.createProfileItems(profilesMenu, null, prof -> {});
        disconnectItem.setEnabled(false);
        connectItem.setEnabled(true);

        descriptionLabel.setText("Select a profile to start listening");
        titleLabel.setText("Not connected");

        serviceCombo.setItems(new String[] { "Not connected" });
        serviceCombo.setEnabled(false);
        serviceCombo.select(0);
    }

    protected void createContents() {
        shell = new Shell();
        shell.setSize(400, 275);
        shell.setText("LeafRadio");
        shell.setLayout(new GridLayout(1, false));

        titleLabel = new Label(shell, SWT.CENTER);
        titleLabel.setFont(FontUtils.deriveFont(titleLabel.getFont(), 24, SWT.BOLD));
        titleLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        descriptionLabel = new Label(shell, SWT.CENTER);
        descriptionLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Composite stationSettingPanel = new Composite(shell, SWT.NONE);
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

        disconnectItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                disconnect();
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
                                player.stop();
                                try {
                                    player.start(profile);
                                } catch (Exception e2) {
                                    DialogUtils.showException(shell, e2);
                                }
                            });
                            disconnectItem.setEnabled(true);
                            connectItem.setEnabled(false);
                            MessageBox box = new MessageBox(LeafRadioMain.this.shell, SWT.OK);
                            box.setText("Success");
                            box.setMessage("Connected to " + response.getInstanceName()
                                    + "!\nChoose a profile from the Server menu.");
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
            public void audioAnnotationReceived(AudioAnnotation annotation) {
                titleLabel.setText(annotation.getTitle() == null ? "<No title>" : annotation.getTitle());
                descriptionLabel.setText(annotation.getDescription() == null ? "" : annotation.getDescription());
            }

            @Override
            public void playerErrored(Exception ex) {
                DialogUtils.showException(shell, ex);
                new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        disconnect();
                    }
                }.widgetSelected(null);
            }
        });

        disconnect();
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
