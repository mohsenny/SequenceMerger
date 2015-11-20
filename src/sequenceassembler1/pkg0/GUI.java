/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sequenceassembler1.pkg0;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.*;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 *
 * @author nypc
 */
public class GUI extends JFrame
{
    private SequenceAssembler runWorker;
    private JFrame fileFrame;
    private JPanel filePanel;
    private JPanel defaultPanel;
    private JPanel outputPanel;
    private JTextPane outputText2;
    private JTextArea outputText;
    private JButton fileButton;
    private JButton runButton;
    private JFileChooser fileChooser;
    private JScrollPane outputScroll;
    
    public int main_x, main_y, file_x, file_y;
    private File[] files;
    private int window;
    private int match;
    
    public GUI() 
    { 
        main_x = 1000;
        main_y = 750;
        file_x = 600;
        file_y = 400;
        
        // Define components 
        
        fileFrame = new JFrame("Select input fragments");
        JLabel matchLabel = new JLabel("Match size"); 
        JLabel windowLabel = new JLabel("Window size"); 
        JLabel fileLabel = new JLabel("Add fragments");
        JTextField matchText = new JTextField(2);
        JTextField windowText = new JTextField(2);
        
        // Outputs
        setLayout(null);
        defaultPanel = new JPanel();
        defaultPanel.setLayout(null);
        defaultPanel.setBounds(0, 0, main_x / 2 - 20, main_y);
        
        outputPanel = new JPanel();
        outputPanel.setBounds(main_x / 2 - 20, 10, main_x / 2, main_y - 50);
        outputPanel.setBackground(Color.yellow);
        outputPanel.setLayout(new GridLayout(1,1));
        
        fileFrame.setResizable(false);
        fileFrame.setBounds((main_x - file_x ) / 2, (main_y - file_y ) / 2, file_x + 10, file_y + 40);
        filePanel = new JPanel();
        filePanel.setLayout(null);
        filePanel.setVisible(false);
        filePanel.setBounds(0,0, file_x, file_y);
        
        EmptyBorder eb = new EmptyBorder(new Insets(10, 10, 10, 10));
        outputText2 = new JTextPane();
        outputText2.setBorder(eb);
        outputText2.setMargin(new Insets(5, 5, 5, 5));
        
        /*
        outputText = new JTextArea();
        outputText.setEditable(true);
        */
        DefaultCaret caret = (DefaultCaret)outputText2.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        outputScroll = new JScrollPane(outputText2);
        
        // Inputs
        
        matchText.setMaximumSize(matchText.getPreferredSize());
        windowText.setMaximumSize(windowText.getPreferredSize());
        fileButton = new JButton("Add...");
        runButton = new JButton("RUN");
        fileChooser = new JFileChooser("C:\\Users\\nypc\\Dropbox\\Public\\University\\UEF\\IT project\\Inputs\\29.10.2014_experiment\\txt\\", FileSystemView.getFileSystemView());
        fileChooser.setMultiSelectionEnabled(true);
        
        // Components' attributes
        
        fileFrame.setVisible(false); 
        fileFrame.setLayout(null);
        fileFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        outputPanel.setVisible(true);
        defaultPanel.setVisible(true);
        fileChooser.setVisible(false);
        
        windowLabel.setBounds(50, 50, 120, 20);
        windowText.setBounds(200, 50, 40, 20);
        matchLabel.setBounds(50, 100, 120, 20);
        matchText.setBounds(200, 100, 40, 20);
        fileLabel.setBounds(50, 150, 120, 20);
        fileButton.setBounds(200, 150, 80, 20);
        fileChooser.setBounds(0, 0, file_x, file_y);
        runButton.setBounds(140, 250, 100, 20);
        
        // Add components to frames
        
        filePanel.add(fileChooser);
        fileFrame.add(filePanel);
        defaultPanel.add(windowLabel);
        defaultPanel.add(windowText);
        defaultPanel.add(matchLabel);
        defaultPanel.add(matchText);
        defaultPanel.add(fileLabel);
        defaultPanel.add(fileButton);
        defaultPanel.add(runButton);
        outputPanel.add(outputScroll);
        add(defaultPanel);
        add(outputPanel);   
        
        // Adding ActionListener
        // TODO : Input verification, Error messages, etc for text inputs
        
        fileButton.addActionListener(new ActionListener() 
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                fileChooser.setVisible(true);
                fileFrame.setVisible(true);
                filePanel.setVisible(true);
            }
        });
        
        fileChooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) 
            {
                if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) 
                {
                    files = fileChooser.getSelectedFiles();
                    fileFrame.dispose();
                } 
                else if (e.getActionCommand().equals(JFileChooser.CANCEL_SELECTION)) 
                {
                    fileFrame.dispose();
                }
            }
        });
        
        runButton.addActionListener(new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e)
            {   
                try
                {
                    runButton.setEnabled(false);
                    runButton.setText("Running...");
                    //TODO : Verify/validate Window and Match values. Maybe use a dropdown manue?
                    window = Integer.parseInt(windowText.getText());
                    match = Integer.parseInt(matchText.getText()); 
                    
                    runWorker = new SequenceAssembler(files, window, match, outputText2);
                    runWorker.execute();
                    if (runWorker.isDone())
                    {
                        runButton.setEnabled(true);
                        runButton.setText("Run");
                    }
                }
                catch (Exception ex)
                {
                    String str = ex.getMessage();
                    System.out.println(str);
                    System.exit(0);
                }
            }
        });
        
    }
    
}
