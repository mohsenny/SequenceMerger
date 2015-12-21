/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sequenceassembler1.pkg0;

import edu.stanford.nlp.util.CoreMap; 
import java.awt.Color;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;

/**
 *
 * @author Mohsen
 */
public class SequenceAssembler extends SwingWorker<Integer, Void> 
{
    public static File[] inputs;
    public static int inputSize;
    public static int window;
    public static int match;
    //private static JTextArea outputText;
    public static JTextPane outputText;
            
    SequenceAssembler(File[] inputs, int window, int match, JTextPane outputText)
    {
        this.inputs = inputs;
        inputSize = inputs.length;
        this.window = window;
        this.match = match;
        this.outputText = outputText;
    }
    
    @Override
    protected Integer doInBackground() throws Exception 
    {
        Long startingTime;
        //Long endTime;
        Date date = new Date();
        startingTime = date.getTime();
        
        int num_of_frags = inputs.length;
        int window_length = window;
        int match_rule = match;
        
        double windowFactor = 0.9;
        double matchFactor = 0.8;
        
        // fetching inputs
        List<String> fragments = new LinkedList<String>();
        List<String> pureURIs = new LinkedList<String>();
        for (int i = 0; i < inputs.length; i++)
        {
            pureURIs.add(inputs[i].getPath());
            
            String path = inputs[i].getPath();
            path = "file:///" + path;
            path = path.replaceAll(" ", "%20");
            path = path.replace("\\", "/");
            
            fragments.add(path);
        }
        
        NLPHelper helper = new NLPHelper();
        
        /*
        *   Keyword Finding phase
        *   All keywords of all fragments are put into fragments_keywords
        *   fragments_keywords[m, n] = n'th Keyword in m'th fragment 
        */
        
        Map<Integer, List<String>> fragments_keywords = new HashMap<Integer, List<String>>();  
        for (int i = 0; i < num_of_frags; i++)
        {
             fragments_keywords.put(i, helper.getKeywords(new URI(fragments.get(i)), "", i+1, 1));
        }
        
        Map<Integer, List<String>> new_fragments_keywords = new HashMap<Integer, List<String>>(); 
        
        int iterationNumber = 0;
        int notNullSize = Utility.getMapNotNullSize(fragments_keywords);
        boolean cannotBeCombinedAnymore = false;

        while (notNullSize >= 1)
        {
            if (notNullSize == 1 || cannotBeCombinedAnymore)
            {
                if (notNullSize == 1)
                {
                    // This is the last remaining fragment
                    List<Integer> indices = Utility.getMapNotNullIncides(fragments_keywords);
                    int fragmentID = indices.get(0);
                    int fragmentSize = fragments_keywords.get(fragmentID).size();
                    String finalStory = Utility.makeFinalStory(fragmentID, fragmentSize, helper);
                    Utility.printResults(finalStory, fragments, pureURIs, startingTime);
                    return 1;
                }
                else
                {
                    List<Integer> indices = Utility.getMapNotNullIncides(fragments_keywords);
                    // Pick the bigger one
                    int biggerFragmentIndex = Utility.findBiggerFragmentIndex(fragments_keywords, indices);
                    int biggerFragmentSize = fragments_keywords.get(biggerFragmentIndex).size();
                    String finalStory = Utility.makeFinalStory(biggerFragmentIndex, biggerFragmentSize, helper);
                    Utility.printResults(finalStory, fragments, pureURIs, startingTime);
                    return 1;
                }
            }
            else if (notNullSize == 2)
            {
                // If both of them are assembled ones (their indices > num_of_frags) then choose the bigger one
                List<Integer> indices = Utility.getMapNotNullIncides(fragments_keywords);
                boolean checker = Utility.ifResultsAreAllOutputs(indices);
                if (checker)
                {
                    // Pick the bigger one
                    int biggerFragmentIndex = Utility.findBiggerFragmentIndex(fragments_keywords, indices);
                    int biggerFragmentSize = fragments_keywords.get(biggerFragmentIndex).size();
                    String finalStory = Utility.makeFinalStory(biggerFragmentIndex, biggerFragmentSize, helper);
                    Utility.printResults(finalStory, fragments, pureURIs, startingTime);
                    return 1;
                }
                else
                {
                    cannotBeCombinedAnymore = true;
                    // Ignore the combination process and enter the 
                    // if (notNullSize == 1 || cannotBeCombinedAnymore) block
                    continue;
                }
            }
            
            new_fragments_keywords = runApplication(fragments_keywords, num_of_frags, window_length, match_rule, helper, iterationNumber, outputText);
            notNullSize = Utility.getMapNotNullSize(new_fragments_keywords);
            iterationNumber++;     
            num_of_frags = new_fragments_keywords.size();
            // New window length
            window_length = Math.round((float)(window_length * windowFactor));
            // New match rule
            match_rule = Math.round((float)(match_rule * matchFactor));
            // Reseting some of lists, sets, etc 
            Utility.PrepareForNextPhase(helper);
        }
        return 1;
    }
    
    
    public static Map<Integer, List<String>> runApplication(Map<Integer, List<String>> fragments_keywords, int num_of_frags, int window_length, int match_rule, NLPHelper helper, int iterationNumber, JTextPane outputText) throws Exception
    {
        try{
            Utility.WriteOutput("\n", Color.WHITE, 1, true);
            Utility.WriteOutput("\n", Color.WHITE, 1, true);
            Utility.WriteOutput("Iteration number : ", Color.BLACK, 14, false);
            Utility.WriteOutput("" + ++iterationNumber, Color.RED, 14, true);
            Utility.WriteOutput("Window size : ", Color.BLACK, 14,false);
            Utility.WriteOutput("" + window_length, Color.RED, 14, true);
            Utility.WriteOutput("Match size : ", Color.BLACK, 14, false);
            Utility.WriteOutput("" + match_rule, Color.RED, 14, true);

            int notNullSize = Utility.getMapNotNullSize(fragments_keywords);

            Utility.WriteOutput("Fragments being processed in this iteration : " , Color.BLACK, 14, false);
            Utility.WriteOutput("" + notNullSize, Color.RED, 14, true);
            Utility.WriteOutput("\n", Color.WHITE, 1, true);
            Utility.WriteOutput("\n", Color.WHITE, 1, true);

            List<String> final_result = new ArrayList<>();

            /*
            *   Keyword analysis phase
            *   All fragments will be compared 1 by 1
            *
            */

            //a flag for checking if these 2 fragments are already smilare or not
            boolean hasSimilarityHappened;  
            //iteration over words in fragment i'th
            for (int i = 0; i < num_of_frags; i++)
            {
                // i_array contains all keywords in i'th fragment
                List<String> i_array = new ArrayList<>();
                i_array = fragments_keywords.get(i);

                // that element of fragments_keywords has removed before in previous phases
                if (i_array == null || i_array.size() < window_length)
                {
                    continue;
                }

                // iteration over words in fragment j'th
                for (int j = 0; j < num_of_frags; j++)
                {
                    if ( j > i)
                    {
                        hasSimilarityHappened = false;

                        //j_array contains all keywords in j'th fragment
                        List<String> j_array = new ArrayList<>();
                        j_array = fragments_keywords.get(j);

                        //that element of fragments_keywords has removed before in previous phases
                        if (j_array == null|| j_array.size() < match_rule)
                        {
                            continue;
                        }

                        Set<String> all_matches = new HashSet<>();

                        int assProcHostLowBound = 99999;
                        int assProcHostHighBound = 0;
                        int assProcTravLowBound = 99999;
                        int assProcTravHighBound = 0;
                        boolean hasOverlapsHappened = false;

                        //a window with the size of window_length traversing over fragment i'th
                        for ( int i_element = 0; i_element < i_array.size()-window_length; i_element++)
                        {
                            //If similarity is already detected then we stop furthur comparisons and go to next fragments
                            if (!hasSimilarityHappened)
                            {
                                //a second window with the size of window_length traversing over fragment j'th
                                for ( int j_element = 0; j_element < j_array.size()-window_length; j_element++)
                                {
                                    if (!hasSimilarityHappened)
                                    {
                                        //After each match, match_check will be increased by one
                                        int match_checker = 0;
                                        int[][] match_recorder = new int[match_rule][2];
                                        Utility.resetMatchRecorder(match_recorder, match_rule);

                                        //Getting rid of the repeatitions in match_recorder (?)
                                        boolean[] match_checking_conditions = new boolean[match_rule * (match_rule + 1)/ 2];
                                        for (int c = 0; c < match_checking_conditions.length; c++)
                                        {
                                            match_checking_conditions[c] = false;
                                        }
                                        //iternation over the first window
                                        for ( int i_window = 0; i_window <  window_length; i_window++)
                                        {   
                                            //if number of matches between 2 windows satisifes the match_rule minimum,
                                            //then furthur comparison between 2 windows (2 fragments?) will not continue
                                            if (match_checker == match_rule){
                                                hasSimilarityHappened = true;
                                                break;
                                            }
                                            else{
                                                //iternation over the second window
                                                for ( int j_window = 0; j_window <  window_length; j_window++)
                                                {
                                                    //if match_rule condition hasn't satisfied yet keep going.
                                                    if (match_checker < match_rule){
                                                        //Check if the word in host fragment and traversed fragment are similare
                                                        //by checking their synonimity
                                                        String i_word = i_array.get(i_window + i_element);
                                                        String j_word = j_array.get(j_window + j_element);

                                                        String[] j_synonyms = helper.getSynonyms(j_word);
                                                        //iterating over all j_word's synonyms to see if one of those is same as i_word
                                                        //TODO also iterate over i_word's synonyms
                                                        for (int synonym_cntr = 0; synonym_cntr < j_synonyms.length; synonym_cntr++)
                                                        {
                                                            String j_syn = j_synonyms[synonym_cntr];
                                                            if (i_word.equals(j_syn))
                                                            {
                                                                //storing the location where the similarity happened (why?)
                                                                //match_checker'th similarity's location in first fragment
                                                                match_recorder[match_checker][0] = i_window + i_element;
                                                                //match_checker'th similarity's location in second fragment
                                                                match_recorder[match_checker][1] = j_window + j_element;
                                                                match_checker++;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }//end of iteration over windows

                                        //checks inside of match_recorder for duplicates and in case that happens, 
                                        //resets match_checker flag and match_recorder array
                                        int condition_counter = 0;
                                        for (int c_h = 0; c_h < match_rule; c_h++)
                                        {
                                            for (int c_t = 0; c_t < match_rule; c_t++)
                                            {
                                                if (c_t > c_h && match_recorder[c_h][0] != -1 && match_recorder[c_h][1] != -1 && match_recorder[c_t][0] != -1 && match_recorder[c_t][1] != -1)
                                                {
                                                    if (match_recorder[c_h][0] == match_recorder[c_t][0] || 
                                                            match_recorder[c_h][1] == match_recorder[c_t][1])
                                                    {
                                                        match_checking_conditions[condition_counter] = true;
                                                    }
                                                    condition_counter++;
                                                }
                                            }
                                        }

                                        for (int c = 0; c < match_checking_conditions.length; c++)
                                        {
                                           if (match_checking_conditions[c])
                                           {
                                                Utility.resetMatchRecorder(match_recorder, match_rule);
                                                match_checker = 0;
                                           }
                                        }

                                        if (match_checker == match_rule)
                                        {   
                                            hasOverlapsHappened = true;
                                            String str = i + "-" + j;
                                            for (int st = 0; st < match_rule; st++)
                                            {
                                                str += "-" + match_recorder[st][0] + "-" + match_recorder[st][1];
                                                //Low boundry
                                                //setting assProcHostLowBound to minimum of match_recorder[i][0], [i+1][0], [i+2][0], ...
                                                //and assProcTravLowBound to minimum of match_recorder[i][1], [i+1][1], [i+2][1], ...
                                                if (match_recorder[st][0] < assProcHostLowBound)
                                                {
                                                    assProcHostLowBound = match_recorder[st][0];
                                                }
                                                if (match_recorder[st][1] < assProcTravLowBound)
                                                {
                                                    assProcTravLowBound = match_recorder[st][1];
                                                }

                                                //High boundry
                                                //setting assProcHostHighBound to maximum of match_recorder[i][0], [i+1][0], [i+2][0], ...
                                                //and assProcTravHighBound to maximum of match_recorder[i][1], [i+1][1], [i+2][1], ...
                                                //which finds the upper bracket of the matching window
                                                if (match_recorder[st][0] >  assProcHostHighBound)
                                                {
                                                    assProcHostHighBound = match_recorder[st][0];
                                                }
                                                if (match_recorder[st][1] >  assProcTravHighBound)
                                                {
                                                    assProcTravHighBound = match_recorder[st][1];
                                                }
                                            }
                                            all_matches.add(str);
                                        }

                                        //TODO 
                                        //if match_checker becomes equal to match_rule, then
                                        //matching sentence will be added to ????   
                                    }
                                }
                            }
                        }//end of iteration over the fragment's words

                        //Now iteration over words of fragments i and j is over
                        //and we know if i and j have overlaps and where its location is
                        if (hasOverlapsHappened)
                        {
                            helper.assemblingProcLowBounderies.add(assProcHostLowBound + "-" + assProcTravLowBound);
                            helper.assemblingProcHighBounderies.add(assProcHostHighBound + "-" + assProcTravHighBound);
                        }

                        HashSet<List<List<CoreMap>>> uniqueResults = new HashSet<List<List<CoreMap>>>();

                        for (String element : all_matches){
                            List<List<CoreMap>> result = new ArrayList<List<CoreMap>>();
                            result = helper.getSentenceWordMatches(element, match_rule);
                            uniqueResults.add(result);
                        }

                        for (List<List<CoreMap>> lists : uniqueResults)
                        {
                            List<CoreMap> set1 = new ArrayList<>();
                            List<CoreMap> set2 = new ArrayList<>();

                            set1 = lists.get(0);
                            set2 = lists.get(1);

                            for (CoreMap sentence : set1)
                            {
                                Utility.WriteOutput(sentence.toString(), Color.BLUE, 12, true); 
                            }

                            Utility.WriteOutput("* has an overlap with *", Color.RED, 14, true);

                            for (CoreMap sentence : set2)
                            {
                                Utility.WriteOutput(sentence.toString(), Color.BLUE, 12, true); 
                            }
                            Iterator iter = all_matches.iterator();
                            /*
                            while (iter.hasNext()) {
                                Utility.WriteOutput(iter.next().toString(), Color.GREEN, true);
                            }
                            */
                            Utility.WriteOutput("------------------------------", Color.BLACK, 12, true);
                        }
                        //assemblingProcFragments stores all the assemblable fragments
                        if (hasOverlapsHappened)
                        {
                            helper.similareFragments.add(i + "-" + j);
                        }
                    }
                }
            }// end of iteration over all fragemnts

            /*  
            * Assembling phase
            *
            * Assembling phase (following code will be executed only once in each iteration)
            * Here base on assemblingProcFragments, assemblingProcLowBounderies and assemblingProcHighBounderies
            * we try to create the big story
            */

            // Reading from the assembling path
            helper.assemblingPathFinder(helper.Seqs_keys, helper.Seqs_values, num_of_frags);

            // Printing and testing
            if (helper.assemblingPath.size() > 0){
                Utility.WriteOutput("Combined fragments", Color.RED, 14, true);
                for (int i = 0; i < helper.assemblingPath.size() - 1; i++)
                {
                    Utility.WriteOutput(helper.assemblingPath.get(i) + " - " + helper.assemblingPath.get(i+1), Color.BLUE, 12, true);
                }
            }
            else
            {
                Utility.WriteOutput("None of the fragments could be merged with current criteria", Color.BLUE, 14, true);
            }


            // Creating final story 
            for (int i = 0; i < helper.assemblingPath.size(); i++)
            {
                int last_j = 0;
                // Special Trav case refers to times where Bb comes after Ba
                boolean previousTravWasSpecial = false;
                if (i < helper.assemblingPath.size() - 1)
                {
                    boolean lastIteration = false;

                    int host = helper.assemblingPath.get(i);
                    int trav = helper.assemblingPath.get(i+1);

                    int j = Utility.findSimilarePairIndex(host, trav, helper.similareFragments);

                    /* 
                    i.e: A - B - C - D - E : size = 5 
                    but path size = (A - B) (B - C) (C - D) (D - E) : size = 4
                    and we shouyld stop at 3
                    */
                    if (i == helper.assemblingPath.size() - 2)
                    {
                        lastIteration = true;
                    }

                    int host_low;
                    int host_high;
                    int trav_low;
                    int trav_high ;

                    if (j < 0)
                    {
                        // Refer to findSimilarePairIndex() for more information.
                        j = Math.abs(j+1);
                        host_low = Integer.parseInt(helper.assemblingProcLowBounderies.get(j).split("-")[1]);
                        host_high = Integer.parseInt(helper.assemblingProcHighBounderies.get(j).split("-")[1]);
                        trav_low = Integer.parseInt(helper.assemblingProcLowBounderies.get(j).split("-")[0]);
                        trav_high = Integer.parseInt(helper.assemblingProcHighBounderies.get(j).split("-")[0]);
                    }
                    else
                    {
                        // Refer to findSimilarePairIndex() for more information.
                        j = j -1;
                        host_low = Integer.parseInt(helper.assemblingProcLowBounderies.get(j).split("-")[0]);
                        host_high = Integer.parseInt(helper.assemblingProcHighBounderies.get(j).split("-")[0]);
                        trav_low = Integer.parseInt(helper.assemblingProcLowBounderies.get(j).split("-")[1]);
                        trav_high = Integer.parseInt(helper.assemblingProcHighBounderies.get(j).split("-")[1]);
                    }

                    // 0's below are just for initialization and getting rid of exception
                    int next_host_low = 0;

                    // If not the last one
                    if (!lastIteration)
                    {
                        int next_host = trav;
                        int next_trav = helper.assemblingPath.get(i+2);
                        int new_j = Utility.findSimilarePairIndex(next_host, next_trav, helper.similareFragments);
                        // check if new_j is negative or positive, to know if host-trav is there or trav-host
                        if (new_j < 0)
                        {
                            // make it positive for furthur calculations
                            new_j = Math.abs(new_j + 1);
                            next_host_low = Integer.parseInt(helper.assemblingProcLowBounderies.get(new_j).split("-")[1]);
                        }
                        else
                        {
                            new_j = new_j - 1;
                            next_host_low = Integer.parseInt(helper.assemblingProcLowBounderies.get(new_j).split("-")[0]);
                        }
                    }

                    int start_point = 0;
                    String frag_pairs =  helper.similareFragments.get(j);
                    String[] frags = frag_pairs.split("-");
                    // Except the 1st iteration, the real host_low (B2) = host_low (Bb) - last_trav_high (end of Ba)
                    int last_trav_high = 0;

                    // Just in case there's not any space between two combined fragments.
                    //final_result.add(" ");

                    // If <B1BbB2> has used before, no need to check <BbCa> anymore, since Bb is used already
                    // and we go straight to the part that we add B2
                    if (!previousTravWasSpecial) 
                    {
                        // First iteration
                        if (i == 0)
                        {
                            // See whether A1 or B1 is bigger
                            // A1 >= B1
                            if (host_low >= trav_low)
                            {
                                for (int token = start_point; token < host_high ; token++)
                                {
                                    // Adding A1 and Aa to result
                                    final_result.add(helper.fragmentsKeyowrdSetntenceMapper.get(host).get(token).toString());
                                }
                            }
                            // B1 > A1
                            else
                            {
                                // If Ba comes after Bb
                                if (next_host_low >= trav_low)
                                {
                                    previousTravWasSpecial  = true;
                                }
                                for (int token = start_point; token < trav_high  ; token++)
                                {   
                                    // Adding B1 and Ba to result
                                    final_result.add(helper.fragmentsKeyowrdSetntenceMapper.get(trav).get(token).toString());
                                }  
                            }
                            last_trav_high = trav_high;
                            //final_result.add(".");
                        }
                        // All other iterations
                        else
                        {
                            int new_host_low = host_low - last_trav_high;
                            if (new_host_low >= trav_low)
                            {
                                start_point = last_trav_high;
                                for (int token = start_point; token < host_high ; token++)
                                {
                                    final_result.add(helper.fragmentsKeyowrdSetntenceMapper.get(host).get(token).toString());
                                }
                            }
                            else
                            {
                                start_point = 0;
                                if (next_host_low >= trav_low)
                                {
                                    previousTravWasSpecial  = true;
                                }
                                for (int token = start_point; token < trav_high  ; token++)
                                {   
                                    final_result.add(helper.fragmentsKeyowrdSetntenceMapper.get(trav).get(token).toString());
                                }  
                            }
                            last_trav_high = trav_high;
                            //final_result.add(".");
                        }
                    }
                    else
                    {
                        // Don't do anything at this point. Just add B2 to the result and go to next iteration
                        previousTravWasSpecial = false;
                    }
                    // Adding rest of the HOST sentences 
                    //(which is A2 for 1st, B3 for 2nd, C3 for 3rd, ...) to the result
                    for (int token = host_high; token < helper.fragmentsKeywordCounter.get(host); token++)
                    {
                        final_result.add(helper.fragmentsKeyowrdSetntenceMapper.get(host).get(token).toString());
                    } 
                    //final_result.add(".");
                }
            }

            // If a new story is made
            if (final_result.size() > 0)
            {
                HashSet<String> final_result_unique = new LinkedHashSet<>();
                String final_result_str = "";
                for (String s : final_result){
                    final_result_unique.add(s);
                }
                // Creating a string from final_result_unique
                for (String s : final_result_unique){
                    final_result_str += s;
                }

                // Remove 'assemblingPath' from 'fragments'
                for (int path_i = 0; path_i < helper.assemblingPath.size(); path_i++)
                {
                    fragments_keywords.put(helper.assemblingPath.get(path_i), null);
                }
                // Normalization
                final_result_str = helper.normalaizeString(final_result_str);
                //Add 
                fragments_keywords.put(num_of_frags, helper.getKeywords(null, final_result_str, num_of_frags + 1, 2));
            }

            return fragments_keywords;
        }
        catch(Exception e)
        {
            Utility.WriteOutput("An error occured:" + e.toString(), Color.red, 10, true);
            return null;
        }
        
    }
    

}
