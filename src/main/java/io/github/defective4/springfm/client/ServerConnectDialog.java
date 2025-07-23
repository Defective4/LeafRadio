package io.github.defective4.springfm.client;

import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class ServerConnectDialog extends Dialog {

    private URL result;
    private Shell shell;

    public ServerConnectDialog(Shell parent) {
        super(parent, SWT.APPLICATION_MODAL | SWT.CLOSE);
        setText("Connect to a server...");
    }

    public URL open() {
        createContents();
        shell.open();
        shell.layout();
        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return result;
    }

    private void createContents() {
        shell = new Shell(getParent(), SWT.CLOSE | SWT.APPLICATION_MODAL);
        shell.setText(getText());
        GridLayout gl_shell = new GridLayout(2, false);
        gl_shell.marginTop = 5;
        gl_shell.marginBottom = 16;
        gl_shell.marginLeft = 16;
        gl_shell.marginRight = 16;
        shell.setLayout(gl_shell);

        new Label(shell, SWT.NONE).setText("Enter URL of a SpringFM server to connect to:");
        new Label(shell, SWT.NONE);

        Combo combo = new Combo(shell, SWT.NONE);
        combo.setItems(new String[] { "http://localhost:8080" }); // TODO
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Button btnConnect = new Button(shell, SWT.NONE);
        btnConnect.setEnabled(false);
        btnConnect.setText("Connect");

        shell.pack();

        combo.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent var1) {
                try {
                    URL url = new URL(combo.getText());
                    if (!url.getProtocol().startsWith("http") || url.getHost().isBlank())
                        throw new IllegalArgumentException();
                    btnConnect.setEnabled(true);
                } catch (Exception e) {
                    btnConnect.setEnabled(false);
                }
            }
        });
        btnConnect.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent ev) {
                try {
                    URL url = new URL(combo.getText());
                    if (!url.getProtocol().startsWith("http") || url.getHost().isBlank())
                        throw new IllegalArgumentException();
                    result = url;
                    shell.dispose();
                } catch (Exception e) {
                    btnConnect.setEnabled(false);
                }
            }
        });
    }
}
