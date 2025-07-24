package io.github.defective4.springfm.client.components;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import io.github.defective4.springfm.client.util.DialogUtils;

public class ProgressDialog extends Dialog {

    private static final ExecutorService THR_POOL = Executors.newVirtualThreadPerTaskExecutor();

    private final String label;

    private Shell shell;

    public ProgressDialog(Shell parent, String label) {
        super(parent);
        this.label = label;
        setText("SWT Dialog");
    }

    public void open(ProgressDialogTask task) {
        createContents();
        shell.open();
        shell.layout();
        Display display = getParent().getDisplay();
        THR_POOL.submit(() -> {
            Exception ex;
            try {
                task.run(shell);
                ex = null;
            } catch (Exception e) {
                ex = e;
            }
            Exception fex = ex;
            Display.getDefault().asyncExec(() -> {
                shell.dispose();
            });
            if (fex != null) {
                DialogUtils.showException(getParent(), fex);
            }
        });
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    private void createContents() {
        shell = new Shell(getParent(), SWT.TITLE | SWT.APPLICATION_MODAL);
        shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(ShellEvent e) {
                e.doit = false;
            }
        });
        shell.setText("Please wait...");
        GridLayout gl_shlPleaseWait = new GridLayout(1, false);
        gl_shlPleaseWait.marginBottom = 5;
        gl_shlPleaseWait.marginTop = 5;
        gl_shlPleaseWait.marginRight = 16;
        gl_shlPleaseWait.marginLeft = 16;
        shell.setLayout(gl_shlPleaseWait);

        Label text = new Label(shell, SWT.NONE);
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        text.setText(label);

        ProgressBar progressBar = new ProgressBar(shell, SWT.INDETERMINATE);
        progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        shell.pack();
    }
}
