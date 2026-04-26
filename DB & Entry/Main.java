package ums;

import ums.gui.LoginFrame;
import ums.gui.UITheme;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        UITheme.applyLAF();
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}
