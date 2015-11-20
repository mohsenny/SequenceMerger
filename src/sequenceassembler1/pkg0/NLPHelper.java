/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sequenceassembler1.pkg0;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

/**
 *
 * @author Mohsen
 */
public class NLPHelper {
    
    Properties props;
    StanfordCoreNLP pipeline;
    Map<Integer, Map<Integer, CoreMap>> fragmentsKeyowrdSetntenceMapper = new HashMap<>();
    Map<Integer, Map<Integer, CoreMap>> fragmentsSetntenceMapper = new HashMap<>();
    //Map<Integer, Integer> Seqs = new HashMap<>();
    //Multimap<Integer, Integer> Seqs = ArrayListMultimap.create();
    ArrayList<Integer> Seqs_keys = new ArrayList<>();
    ArrayList<Integer> Seqs_values = new ArrayList<>();
    //The bath leading from first fragment to las (or last to first?)
    List<Integer> assemblingPath = new ArrayList<>();
    //Keeps track of pairs of fragments related to eachother
    List<String> similareFragments = new ArrayList<>();
    //Beginnig of overlaps in Fi and Fj 
    List<String> assemblingProcLowBounderies = new ArrayList<>();
    //End of overlaps in Fi and Fj 
    List<String> assemblingProcHighBounderies = new ArrayList<>();
    //Keeps track of quantity of keywords in each fragments
    Map<Integer, Integer> fragmentsKeywordCounter = new HashMap<>();

    
    public NLPHelper(){
        this.props = new Properties();
        this.props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
        this.pipeline = new StanfordCoreNLP(this.props);
    }
        
    public List<CoreMap> getSentences(URI path) throws IOException{
        
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        String text = new String(encoded);
        
        Annotation document  = new Annotation(text);
        this.pipeline.annotate(document);
        
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        return sentences;
    }
    
     
    public List<CoreMap> getSentences(String str) throws IOException{
        
        String text = str;
        
        Annotation document  = new Annotation(text);
        this.pipeline.annotate(document);
        
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        return sentences;
    }
    
    //Finds all the synonyms of the given word, according to WordNet
    //Returns an array of String
    //Ignores duplications
    public String[] getSynonyms(String word){
        System.setProperty("wordnet.database.dir", "C:\\Program Files (x86)\\WordNet\\2.1\\dict");
        WordNetDatabase database = WordNetDatabase.getFileInstance();
        Synset[] synsets = database.getSynsets(word);
        List<String> AllResults = new ArrayList<String>();
        if (synsets.length > 0)
        {
            for (int i = 0; i < synsets.length; i++)
            {
                //System.out.println("");
                String[] wordForms = synsets[i].getWordForms();
                for (int j = 0; j < wordForms.length; j++)
                {
                    if (wordForms[j].equals(word))
                    {
                        //ignoring the original word itself
                    }
                    else
                    {
                        AllResults.add(wordForms[j]);
                    }
                }
            }
        }
        //Adding the original word for once
        AllResults.add(word);

        String[] convertedArr = AllResults.toArray(new String[0]);
        Set<String> temp = new HashSet<String>(Arrays.asList(convertedArr));
        String[] DistincResults = temp.toArray(new String[0]);


        return  DistincResults;
    }
    
    
    
    
    // Returns all the keywords (NNP, NN, NNS, NNPS, CD and Names) in a stroy fragment
    // Flag indicates wethere the fragments are comnig from input files (step 1) or not
    public List<String> getKeywords(URI path, String input, int fragment_number, int flag) throws IOException{
        List<CoreMap> sentences;
        if (flag == 1)
            sentences = getSentences(path);
        else
            sentences = getSentences(input);
            
        List<String> Keywords = new ArrayList<String>();
        //Maps keywords to the sentence they belong too
        Map<Integer, CoreMap> keywordSentenceMapper = new HashMap<>();    
        //keeps track of order and number of each sentence
        Map<Integer, CoreMap> sentenceMapper = new HashMap<>(); 
                          
        Integer keyword_counter = 0;
        Integer sentence_counter = 0;
        for(CoreMap sentence: sentences) {
            
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                sentenceMapper.put(sentence_counter, sentence);
                sentence_counter++;     
                // this is the text of the token
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);  
                //System.out.printf("%s\n%s\n%s\n\n\n", word, pos, ne);
                          
                //if (pos.equals("NNP") || pos.equals("NN") || pos.equals("NNS") || pos.equals("NNPS") || pos.equals("CD") && !ne.equals("PERSON")){
                if (pos.equals("NNP") || pos.equals("NN") || pos.equals("NNS") || pos.equals("NNPS") || pos.equals("CD") || pos.equals("VB")  || ne.equals("PERSON") || pos.equals("VBD")  || pos.equals("VBG")  || pos.equals("VBN")  ){
                    //keywords.add(word + " - " + pos + " - " + ne);
                    keywordSentenceMapper.put(keyword_counter, sentence);
                    keyword_counter++;
                    Keywords.add(word);
                }     
            }
        }
        fragmentsKeyowrdSetntenceMapper.put(fragment_number-1, keywordSentenceMapper);
        fragmentsSetntenceMapper.put(fragment_number-1, sentenceMapper);
        fragmentsKeywordCounter.put(fragment_number-1, keyword_counter);
                
        return Keywords;
    }
    
    
    public boolean isItName(String word, CoreMap sentence){
        for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
            String Tword = token.get(CoreAnnotations.TextAnnotation.class);
            String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);  
            if (word.equals(Tword)){
                if(ne.equals("PERSON")){
                    return true;
                }
                else{
                    return false;
                }
            }
        }
        return false;
    }
    
    
    public List getSentenceWordMatches(String element, int match_rule){    
        String[] split = element.split("-");
        int host_fragment_number = Integer.parseInt(split[0]);
        int trav_fragment_number = Integer.parseInt(split[1]);
        //Following IF is for cases like that we have (1 - 3) already and we want
        //to add (1 - 4). So, in these cases we try to add (4 - 1) instead
        int temp_key = host_fragment_number;
        int temp_val = trav_fragment_number;
        
        Seqs_keys.add(host_fragment_number);
        Seqs_values.add(trav_fragment_number);
        
        
        Map<Integer, CoreMap> host_fragment_key_sentence_mapper = new HashMap<>();
        Map<Integer, CoreMap> trav_fragment_key_sentence_mapper = new HashMap<>();
         
        

        host_fragment_key_sentence_mapper = fragmentsKeyowrdSetntenceMapper.get(host_fragment_number);
        trav_fragment_key_sentence_mapper = fragmentsKeyowrdSetntenceMapper.get(trav_fragment_number);

            
        List<CoreMap> Overlaps1 = new ArrayList<>();
        List<CoreMap> Overlaps2 = new ArrayList<>();

        Iterator<Map.Entry<Integer, CoreMap>> entries = host_fragment_key_sentence_mapper.entrySet().iterator();
        while (entries.hasNext())
        {
            Map.Entry<Integer, CoreMap> entry = entries.next();

            for (int cond = 1; cond < match_rule; cond++)
            {
                int host_1 = Integer.parseInt(split[(2 * (cond+1)) - 1]);
                int host_2 = Integer.parseInt(split[(2 * (cond+1)) + 1]);
                if (host_1 > host_2)
                {
                    split[(2 * (cond+1)) - 1] = "" + host_2;
                    split[(2 * (cond+1)) + 1] = "" + host_1;
                }
            }

            int first_host = Integer.parseInt(split[2]);
            int last_host = Integer.parseInt(split[2 * match_rule]);

            if (entry.getKey() >= first_host && entry.getKey() <= last_host)
            {
                Overlaps1.add(entry.getValue());
            }
        }

        Iterator<Map.Entry<Integer, CoreMap>> entries_t = trav_fragment_key_sentence_mapper.entrySet().iterator();
        while (entries_t.hasNext())
        {
            Map.Entry<Integer, CoreMap> entry = entries_t.next();

            for (int cond = 1; cond < match_rule; cond++)
            {
                int trav_1 = Integer.parseInt(split[(2 * (cond+1)) - 1]);
                int trav_2 = Integer.parseInt(split[(2 * (cond+1)) + 1]);
                if (trav_1 > trav_2)
                {
                    split[(2 * (cond+1)) - 1] = "" + trav_2;
                    split[(2 * (cond+1)) + 1] = "" + trav_1;
                }
            }

            int first_trav = Integer.parseInt(split[3]);
            int last_Trav = Integer.parseInt(split[2 * match_rule + 1]);
            if (entry.getKey() >= first_trav && entry.getKey() <=  last_Trav)
            {
                Overlaps2.add(entry.getValue());
            }
        }

        List<List<CoreMap>> result = new ArrayList<List<CoreMap>>();
        
        Set<CoreMap> temp1 = new HashSet<>();
        temp1.addAll(Overlaps1);
        Overlaps1.clear();
        Overlaps1.addAll(temp1);
        
        Set<CoreMap> temp2 = new HashSet<>();
        temp2.addAll(Overlaps2);
        Overlaps2.clear();
        Overlaps2.addAll(temp2);
        
        result.add(Overlaps1);
        result.add(Overlaps2);
        
        return result;
    }
    
    public void swap(int num_1, int num_2){
        int temp = num_1;
        num_1 = num_2;
        num_2 = temp;
    }
    
    public void assemblingPathFinder(ArrayList<Integer> seqs_keys, ArrayList<Integer> seqs_values, int num_of_frags)
    {
        
        int total_input_size = seqs_keys.size();
        Stack path_stack = new Stack();
        Map<Integer, ArrayList<Integer>> inputs = new HashMap<>();
        Map<Integer, Integer> value_counters = new HashMap<>();
        //organized_inputs array gets created in this loop
        for (int i = 0; i < total_input_size; i++)
        {
            
            int current_key = seqs_keys.get(i);
            int current_value = seqs_values.get(i);
            if (inputs.containsKey(current_key))
               {
                //to avoid duplicates for values
                if (inputs.get(current_key).contains(current_value))
                {
                    continue;
                }
                else
                {
                    inputs.get(current_key).add(current_value);
                }
            }
            if (inputs.containsKey(current_value))
            {
                if (inputs.get(current_value).contains(current_key))
                {
                    continue;
                }
                else
                {
                    inputs.get(current_value).add(current_key);
                }
            }
            if (!inputs.containsKey(current_key))
            {
                ArrayList<Integer> value_array = new ArrayList<>();
                value_array.add(current_value);
                inputs.put(current_key, value_array);
            }
            if (!inputs.containsKey(current_value)){
                ArrayList<Integer> value_array = new ArrayList<>();
                value_array.add(current_key);
                inputs.put(current_value, value_array);
            }
        }
        
        int value_counter;
        int key, value;
        int longest_length = 0;
        List<Integer> assembling_path = new ArrayList<>();
        for (int i = 0; i < num_of_frags; i++)
        {
            //if organized_inputs[i] has something
            //if (!inputs.get(i).isEmpty())
            if (inputs.containsKey(i))
            {
                key = i;
                path_stack.push(i);
                value_counters.put(i, 0);
                if (path_stack.size() > longest_length)
                {
                    longest_length = path_stack.size();
                    store_longest_path(path_stack);
                    /* print*/
                    //print_longest_path(assembling_path);
                }
                while (!path_stack.isEmpty())
                {
                    value = -1;
                    //if value_counter is already set for this key
                    if (value_counters.containsKey(key)){
                        value_counter = value_counters.get(key);
                    }
                    else
                    {
                    value_counters.put(key, 0);
                        value_counter = 0;
                    }
                    //checking if organized_inputs[key]'s array has enoguh elements
                    if (inputs.get(key).size() > value_counter)
                    {
                        value = inputs.get(key).get(value_counter);
                        //If the value isn't already in the stack, then add it
                        if (!path_stack.contains(value))
                        {
                            path_stack.push(value);
                            if (path_stack.size() > longest_length)
                            {
                                longest_length = path_stack.size();
                                store_longest_path(path_stack);
                                /* print*/
                                //print_longest_path(assembling_path);
                            }
                            /* print*/
                            //print_stack(path_stack);
                            //we go ahead....
                            key = value;
                            value_counters.replace(key, 0);
                        }
                        //if it is already in stack, check the next value of that array
                        else if (inputs.get(key).size() > value_counter + 1)
                        {
                            if (value_counters.containsKey(key))
                            {
                                value_counters.replace(key, value_counter+1);
                            }
                            else
                            {
                               value_counters.put(key, 0);
                            }
                        }
                        else
                        {
                            //Roll back
                            path_stack.pop();
                            /* print*/
                            //print_stack(path_stack);
                            key = (Integer)path_stack.peek();
                            value_counter = value_counters.get(key);
                            value_counters.replace(key, value_counter+1);
                        }
                    }
                    else
                    {
                        //Roll back
                        path_stack.pop();
                        if (!path_stack.isEmpty())
                        {
                            value_counters.replace((int)path_stack.peek(), 0);
                        }
                        /* print*/
                        //print_stack(path_stack);
                    }
                }
            }
        }
    }
    
    public void store_longest_path(Stack<Integer> path_stack)
    {
        assemblingPath.clear();
        Iterator<Integer> iter = path_stack.iterator();
        while (iter.hasNext()){
            assemblingPath.add(iter.next());
        }
    }
    
    
}
