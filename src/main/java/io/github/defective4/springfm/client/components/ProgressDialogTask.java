package io.github.defective4.springfm.client.components;

import org.eclipse.swt.widgets.Shell;

public interface ProgressDialogTask {
    void run(Shell shell) throws Exception;
}
