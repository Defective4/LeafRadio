package io.github.defective4.springfm.client.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class DialogUtils {

    public static void showDialog(Shell parent, String title, String text) {
        MessageBox box = new MessageBox(parent, SWT.NONE);
        box.setText(text);
        box.setMessage(text);
        box.open();
    }

    public static void showException(Shell parent, Exception fex) {
        Display.getDefault().asyncExec(() -> {
            MessageBox exBox = new MessageBox(parent, SWT.OK);
            exBox.setText("An error occured");
            exBox.setMessage(fex.toString());
            exBox.open();
        });
    }

}
