/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/* @test
   @bug 4199622
   @summary RFE: JComboBox shouldn't send ActionEvents for keyboard navigation
   @author Vladislav Karnaukhov
   @run main bug4199622
*/

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import sun.awt.OSInfo;
import sun.awt.SunToolkit;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;

public class bug4199622 extends JFrame implements ActionListener {

    static final int nElems = 20;
    static JComboBox<String> cb = null;

    bug4199622(LookAndFeel laf) {
        super();

        try {
            UIManager.setLookAndFeel(laf);
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException("Test failed", e);
        }

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        cb = new JComboBox<>();
        for (int i = 0; i < nElems; i++) {
            cb.addItem(String.valueOf(i + 1));
        }
        cb.addActionListener(this);
        add(cb);

        setSize(300, 300);
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (UIManager.getBoolean("ComboBox.noActionOnKeyNavigation") && cb.isPopupVisible()) {
            throw new RuntimeException("Test failed. actionPerformed generated");
        }
    }

    static Robot robot = null;
    static SunToolkit toolkit = null;

    static void doTest() {
        if (robot == null) {
            try {
                robot = new Robot();
                robot.setAutoDelay(20);
            } catch (AWTException e) {
                throw new RuntimeException("Can't create robot. Test failed", e);
            }
        }

        toolkit = (SunToolkit) Toolkit.getDefaultToolkit();
        if (toolkit == null) {
            throw new RuntimeException("Can't get the toolkit. Test failed");
        }
        toolkit.realSync();

        doActualTest();

        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    cb.hidePopup();
                    cb.setEditable(true);
                    cb.updateUI();
                }
            });
        } catch (InterruptedException e) {
            throw new RuntimeException("Test failed", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Test failed", e);
        }

        toolkit.realSync();
        doActualTest();
    }

    static void doActualTest() {
        UIManager.put("ComboBox.noActionOnKeyNavigation", true);
        doTestUpDown();
        UIManager.put("ComboBox.noActionOnKeyNavigation", false);
        doTestUpDown();

        UIManager.put("ComboBox.noActionOnKeyNavigation", true);
        doTestPgUpDown();
        UIManager.put("ComboBox.noActionOnKeyNavigation", false);
        doTestPgUpDown();

        UIManager.put("ComboBox.noActionOnKeyNavigation", true);
        doTestHomeEnd();
        UIManager.put("ComboBox.noActionOnKeyNavigation", false);
        doTestHomeEnd();
    }

    static void doTestHomeEnd() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    cb.hidePopup();
                    cb.setSelectedIndex(0);
                }
            });
        } catch (InterruptedException e) {
            throw new RuntimeException("Test failed", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Test failed", e);
        }
        toolkit.realSync();

        robot.keyPress(KeyEvent.VK_END);
        toolkit.realSync();
        robot.keyPress(KeyEvent.VK_HOME);
        toolkit.realSync();
    }

    static void doTestUpDown() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    cb.hidePopup();
                    cb.setSelectedIndex(0);
                }
            });
        } catch (InterruptedException e) {
            throw new RuntimeException("Test failed", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Test failed", e);
        }
        toolkit.realSync();

        for (int i = 0; i < nElems; i++) {
            robot.keyPress(KeyEvent.VK_DOWN);
            toolkit.realSync();
        }

        for (int i = 0; i < nElems; i++) {
            robot.keyPress(KeyEvent.VK_UP);
            toolkit.realSync();
        }
    }

    static void doTestPgUpDown() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    cb.hidePopup();
                    cb.setSelectedIndex(0);
                }
            });
        } catch (InterruptedException e) {
            throw new RuntimeException("Test failed", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Test failed", e);
        }
        toolkit.realSync();

        int listHeight = cb.getMaximumRowCount();
        for (int i = 0; i < nElems; i += listHeight) {
            robot.keyPress(KeyEvent.VK_PAGE_DOWN);
            toolkit.realSync();
        }

        for (int i = 0; i < nElems; i += listHeight) {
            robot.keyPress(KeyEvent.VK_PAGE_UP);
            toolkit.realSync();
        }
    }

    public static void main(String[] args) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    bug4199622 test = new bug4199622(new MetalLookAndFeel());
                    test.setVisible(true);
                }
            });
        } catch (InterruptedException e) {
            throw new RuntimeException("Test failed", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Test failed", e);
        }
        doTest();

        if (OSInfo.getOSType() == OSInfo.OSType.WINDOWS) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        bug4199622 test = new bug4199622(new WindowsLookAndFeel());
                        test.setVisible(true);
                    }
                });
            } catch (InterruptedException e) {
                throw new RuntimeException("Test failed", e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Test failed", e);
            }
            doTest();
        }
    }
}
