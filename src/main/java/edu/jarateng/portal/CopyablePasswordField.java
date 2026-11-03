package edu.jarateng.portal;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import javax.swing.JPasswordField;

final class CopyablePasswordField extends JPasswordField {
    CopyablePasswordField() {
        super();
    }

    CopyablePasswordField(int columns) {
        super(columns);
    }

    @Override
    public void copy() {
        String selection = getSelectedText();
        if (selection != null && !selection.isEmpty()) {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(selection), null);
        }
    }
}
