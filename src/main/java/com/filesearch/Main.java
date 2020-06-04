package com.filesearch;

import javax.swing.*;
import javax.swing.plaf.basic.BasicLookAndFeel;

import com.bulenkov.darcula.*;

import java.awt.event.*;

public class Main implements ActionListener,
        ItemListener {

    private boolean disableOCR=true;
private MainUI mainUI;

    Main(){
        BasicLookAndFeel darculaLookAndFeel = new DarculaLaf();
        try {
            UIManager.setLookAndFeel(darculaLookAndFeel);
        } catch (UnsupportedLookAndFeelException ex) {
            // ups!
        }
        JFrame jFrame = new JFrame("File Search");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setSize(600,700);
          mainUI =new MainUI();
        jFrame.add(mainUI.getUIPanel());

        //Menu
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu  = new JMenu("File");
        JMenuItem exitMenuItem  = new JMenuItem("Close");
        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        exitMenuItem.addActionListener(this);
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);

        //console menu
        JMenu consoleMenu  = new JMenu("Console");

        /*JMenuItem consoleselectAllMenuItem  = new JMenuItem("Select All");
        consoleselectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        consoleselectAllMenuItem.addActionListener(this);
        consoleMenu.add(consoleselectAllMenuItem);*/

        JMenuItem consoleClearMenuItem  = new JMenuItem("Clear");
        consoleClearMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        consoleClearMenuItem.addActionListener(this);
        consoleMenu.add(consoleClearMenuItem);

        JMenuItem copyClearMenuItem  = new JMenuItem("Copy to Clipboard");
        copyClearMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        copyClearMenuItem.addActionListener(this);
        consoleMenu.add(copyClearMenuItem);
        menuBar.add(consoleMenu);

        //searh menu
        JMenu searchMenu  = new JMenu("Search");
        JMenuItem reindexMenuItem  = new JMenuItem("Reindex folder");
        reindexMenuItem.addActionListener(this);
        searchMenu.add(reindexMenuItem);

        /*JMenuItem includeImageMenuItem  = new JCheckBoxMenuItem("Disable OCR",true);
        includeImageMenuItem.addItemListener(this);
        searchMenu.add(includeImageMenuItem);*/
        menuBar.add(searchMenu);


        jFrame.setJMenuBar(menuBar);
        jFrame.setVisible(true);
    }

    public static void main(String[] args){
       FileContentReader.loadTextTypes();
        new Main();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
       String command = e.getActionCommand();
       switch (command){
           case "Clear":
               mainUI.clearConsole();
               break;
           case "Copy to Clipboard":
               mainUI.copyToClip();
               break;
           case "Close":
               System.exit(0);
               break;
           case "Reindex folder":
               mainUI.reIndexFolder();
               break;
           case "Select All":
               mainUI.selectAll();
               break;
           default:
               System.out.println("unimplemented command : "+command);
       }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {

    }
}
