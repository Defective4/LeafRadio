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
import org.eclipse.swt.widgets.Shell;

import io.github.defective4.springfm.client.util.FontUtils;

public class LeafRadioMain {

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

    protected void createContents() {
        shell = new Shell();
        shell.setSize(400, 275);
        shell.setText("LeafRadio");
        shell.setLayout(new GridLayout(1, false));

        Label titleLabel = new Label(shell, SWT.NONE);
        titleLabel.setFont(FontUtils.deriveFont(titleLabel.getFont(), 24, SWT.BOLD));
        titleLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
        titleLabel.setText("Not connected");

        Label descriptionLabel = new Label(shell, SWT.NONE);
        descriptionLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        descriptionLabel.setText("Connect to a server to start listening");

        Composite stationSettingPanel = new Composite(shell, SWT.NONE);
        stationSettingPanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        stationSettingPanel.setLayout(new GridLayout(2, false));

        Composite serviceSettingPanel = new Composite(shell, SWT.NONE);
        serviceSettingPanel.setLayout(new GridLayout(2, false));
        serviceSettingPanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label serviceLabel = new Label(serviceSettingPanel, SWT.NONE);
        serviceLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        serviceLabel.setText("Service: ");

        Combo serviceCombo = new Combo(serviceSettingPanel, SWT.READ_ONLY);
        serviceCombo.setItems(new String[] { "Not connected" });
        serviceCombo.setEnabled(false);
        serviceCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        serviceCombo.select(0);

        Menu menu = new Menu(shell, SWT.BAR);
        shell.setMenuBar(menu);

        MenuItem serverMenu = new MenuItem(menu, SWT.CASCADE);
        serverMenu.setText("Server");

        Menu connectMenu = new Menu(serverMenu);
        serverMenu.setMenu(connectMenu);

        MenuItem connectItem = new MenuItem(connectMenu, SWT.NONE);
        connectItem.setText("Connect...");

        MenuItem disconnectItem = new MenuItem(connectMenu, SWT.NONE);
        disconnectItem.setEnabled(false);
        disconnectItem.setText("Disconnect");

        connectItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                URL url = new ServerConnectDialog(shell).open();
                if (url != null) {
                    new ProgressDialog(shell, "Connecting...").open(() -> {

                    });
                }
            }
        });
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
