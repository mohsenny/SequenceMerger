/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sequenceassembler1.pkg0;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author nypc
 */
public class ResultObject {
    
    int numOfFragments;
    List<String> fragments = new LinkedList<String>();
    
    ResultObject(int numOfFragments, List<String> fragments)
    {
        this.fragments = fragments;
        this.numOfFragments = numOfFragments;
    }
}
