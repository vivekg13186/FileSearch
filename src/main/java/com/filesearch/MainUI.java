package com.filesearch;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static javax.swing.text.DefaultCaret.ALWAYS_UPDATE;

public class MainUI implements  SearchListener {
    private JTextField folderPathTextBox;
    private JButton browseButton;
    private JTextArea searchForTextField;
    private JButton searchButton;
    private JPanel UIPanel;
    private JTextArea logTextArea;
    private JProgressBar progressBar;
    private SearchEngine searchEngine;
    private boolean disableOCR;

    private boolean isFolder(String path) {
        File f = new File(path);
        return f.isDirectory() && f.canRead();
    }

    private void search() {
        String query = searchForTextField.getText();
        SearchListener self = this;
        if (query != null && !query.isEmpty()) {
            if (searchEngine != null && searchEngine.readyForSearch()) {
                SwingWorker worker = new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        List<SearchResult> result = searchEngine.search(query, self);
                        if (result != null) {
                            self.log("===========Search Result Start=============");
                            for (SearchResult sr : result) {
                                self.log(sr.filename);
                            }
                            self.log("===========Search Result End=============");
                            self.log("Number of documents found : " + result.size());
                        }
                        return true;
                    }
                };
                worker.execute();
            }
        }
    }

    private void processFolder() {
        SearchListener self = this;
        SwingWorker worker = new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground() throws Exception {
                String path = folderPathTextBox.getText();
                if (isFolder(path)) {
                    System.out.println(path);
                    try {
                        searchEngine = new SearchEngine(path,disableOCR);
                        searchEngine.startIndexing(self);
                    } catch (IOException ioException) {
                        error(ioException.getMessage());
                    }
                }
                return true;
            }
        };
        worker.execute();

    }

    public void setDisableOCR(boolean flag){
        disableOCR = flag;
    }
    public MainUI() {
        folderPathTextBox.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                processFolder();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {

            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                processFolder();
            }
        });
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                search();
            }
        });
        DefaultCaret caret = (DefaultCaret) logTextArea.getCaret();
        caret.setUpdatePolicy(ALWAYS_UPDATE);


        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser j = new JFileChooser();
                j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                Integer opt = j.showOpenDialog(UIPanel);
                if(opt==JFileChooser.APPROVE_OPTION){
                    File f = j.getSelectedFile();
                    folderPathTextBox.setText(f.getAbsolutePath());
                }
            }
        });
    }

    public JPanel getUIPanel() {
        return UIPanel;
    }

    public void log(String msg) {
        logTextArea.append(msg + "\n");
    }

    public void error(String msg) {
        logTextArea.append(msg + "\n");
    }

    @Override
    public void setProgress(int total, int amt, String message) {
        logTextArea.append(message + "\n");
        progressBar.setMaximum(total);
        progressBar.setValue(amt);
    }

    public void clearConsole() {
        logTextArea.setText("");
    }

    public void copyToClip() {
        StringSelection selection = new StringSelection(logTextArea.getText());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    public void reIndexFolder() {
        processFolder();
    }

    public void selectAll() {
        System.out.println("selecting all");
        logTextArea.selectAll();
    }
}
