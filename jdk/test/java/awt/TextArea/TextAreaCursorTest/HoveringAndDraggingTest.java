/*
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
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
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

/*
  test
  @bug 6497109
  @summary Mouse cursor icons for TextArea should be correct in case of hovering or dragging mouse over different subcomponents.
  @author Konstantin Voloshin: area=awt.TextArea
  @run applet/manual=yesno HoveringAndDraggingTest.html
*/

/**
 * HoveringAndDraggingTest.java
 *
 * summary: Mouse cursor icons for TextArea should be correct in case
 *   of hovering or dragging mouse over different subcomponents.
 */

import java.awt.Frame;
import java.awt.Panel;
import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.Dialog;

public class HoveringAndDraggingTest extends java.applet.Applet {
    public void start() {
        String[] instructions = new String[] {
            "1. Notice components in test window: main-panel, box-for-text,"
                +" 2 scroll-sliders, and 4 scroll-buttons.",
            "2. Hover mouse over box-for-text."
                +" Make sure, that mouse cursor is TextCursor (a.k.a. \"beam\").",
            "3. Hover mouse over each of components (see item 1), except for box-for-text."
                +" Make sure, that cursor is DefaultCursor (arrow).",
            "4. Drag mouse (using any mouse button) from box-for-text to every"
                +" component in item 1, and also outside application window."
                +" Make sure, that cursor remains TextCursor while mouse button is pressed.",
            "5. Repeat item 4 for each other component in item 1, except for box-for-text,"
                +" _but_ now make sure that cursor is DefaultCursor.",
            "6. If cursor behaves as described in items 2-3-4-5, then test passed; otherwise it failed."
        };
        Sysout.createDialogWithInstructions( instructions );

        Panel panel = new Panel();
        panel.setLayout( new GridLayout(3,3) );

        for( int y=0; y<3; ++y ) {
            for( int x=0; x<3; ++x ) {
                if( x==1 && y==1 ) {
                    panel.add( new TextArea( bigString() ) );
                } else {
                    panel.add( new Panel() );
                }
            }
        }

        Frame frame = new Frame( "TextArea cursor icon test" );
        frame.setSize( 300, 300 );
        frame.add( panel );
        frame.setVisible( true );
    }

    static String bigString() {
        String s = "";
        for( int lines=0; ; ++lines ) {
            for( int symbols=0; symbols<100; ++symbols ) {
                s += "0";
            }
            if( lines<50 ) {
                s += "\n";
            } else {
                break;
            }
        }
        return s;
    }
}


/****************************************************
 Standard Test Machinery
 DO NOT modify anything below -- it's a standard
  chunk of code whose purpose is to make user
  interaction uniform, and thereby make it simpler
  to read and understand someone else's test.
 ****************************************************/

/**
 This is part of the standard test machinery.
 It creates a dialog (with the instructions), and is the interface
  for sending text messages to the user.
 To print the instructions, send an array of strings to Sysout.createDialog
  WithInstructions method.  Put one line of instructions per array entry.
 To display a message for the tester to see, simply call Sysout.println
  with the string to be displayed.
 This mimics System.out.println but works within the test harness as well
  as standalone.
 */

class Sysout
{
    private static TestDialog dialog;

    public static void createDialogWithInstructions( String[] instructions )
    {
        dialog = new TestDialog( new Frame(), "Instructions" );
        dialog.printInstructions( instructions );
        dialog.setVisible(true);
        println( "Any messages for the tester will display here." );
    }

    public static void createDialog( )
    {
        dialog = new TestDialog( new Frame(), "Instructions" );
        String[] defInstr = { "Instructions will appear here. ", "" } ;
        dialog.printInstructions( defInstr );
        dialog.setVisible(true);
        println( "Any messages for the tester will display here." );
    }


    public static void printInstructions( String[] instructions )
    {
        dialog.printInstructions( instructions );
    }


    public static void println( String messageIn )
    {
        dialog.displayMessage( messageIn );
    }

}// Sysout  class

/**
  This is part of the standard test machinery.  It provides a place for the
   test instructions to be displayed, and a place for interactive messages
   to the user to be displayed.
  To have the test instructions displayed, see Sysout.
  To have a message to the user be displayed, see Sysout.
  Do not call anything in this dialog directly.
  */
class TestDialog extends Dialog
{

    TextArea instructionsText;
    TextArea messageText;
    int maxStringLength = 80;

    //DO NOT call this directly, go through Sysout
    public TestDialog( Frame frame, String name )
    {
        super( frame, name );
        int scrollBoth = TextArea.SCROLLBARS_BOTH;
        instructionsText = new TextArea( "", 15, maxStringLength, scrollBoth );
        add( "North", instructionsText );

        messageText = new TextArea( "", 5, maxStringLength, scrollBoth );
        add("Center", messageText);

        pack();

        setVisible(true);
    }// TestDialog()

    //DO NOT call this directly, go through Sysout
    public void printInstructions( String[] instructions )
    {
        //Clear out any current instructions
        instructionsText.setText( "" );

        //Go down array of instruction strings

        String printStr, remainingStr;
        for( int i=0; i < instructions.length; i++ )
        {
            //chop up each into pieces maxSringLength long
            remainingStr = instructions[ i ];
            while( remainingStr.length() > 0 )
            {
                //if longer than max then chop off first max chars to print
                if( remainingStr.length() >= maxStringLength )
                {
                    //Try to chop on a word boundary
                    int posOfSpace = remainingStr.
                        lastIndexOf( ' ', maxStringLength - 1 );

                    if( posOfSpace <= 0 ) posOfSpace = maxStringLength - 1;

                    printStr = remainingStr.substring( 0, posOfSpace + 1 );
                    remainingStr = remainingStr.substring( posOfSpace + 1 );
                }
                //else just print
                else
                {
                    printStr = remainingStr;
                    remainingStr = "";
                }

                instructionsText.append( printStr + "\n" );

            }// while

        }// for

    }//printInstructions()

    //DO NOT call this directly, go through Sysout
    public void displayMessage( String messageIn )
    {
        messageText.append( messageIn + "\n" );
        System.out.println(messageIn);
    }

}// TestDialog  class
