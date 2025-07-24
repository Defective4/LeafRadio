package io.github.defective4.springfm.client.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class DialogUtils {

    public static void showException(Shell parent, Exception fex) {
        MessageBox exBox = new MessageBox(parent, SWT.OK);
        exBox.setText("An error occured");
        exBox.setMessage(fex.toString());
        exBox.open();
    }

}
