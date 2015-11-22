/*
 * @author Mohsen Nasiri
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sequenceassembler1.pkg0;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 *
 * @author nypc
 */
public class Main {
    public static void main(String[] args) throws Exception
    {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final GUI defaultFrame = new GUI();
                defaultFrame.setTitle("Sequence Assembler v1.0");
                defaultFrame.setVisible(true); 
                defaultFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                //defaultFrame.setLayout(null);
                defaultFrame.setSize(defaultFrame.main_x, defaultFrame.main_y); 
                defaultFrame.setResizable(false);
            }
        });
        //gui = new GUI();
    }
    
}
