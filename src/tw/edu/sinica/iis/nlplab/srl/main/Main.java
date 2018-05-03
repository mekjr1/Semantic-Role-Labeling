/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.edu.sinica.iis.nlplab.srl.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import opennlp.maxent.DataStream;
import opennlp.maxent.PlainTextByLineDataStream;
import tw.edu.sinica.iss.nlplab.srl.label.Labeler;

/**
 *
 * @author mekjr1
 */
public class Main {

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {  
       
        //Use the parser class to parse the input sentence
        DataStream ds = new PlainTextByLineDataStream(new FileReader(new File("data/input.txt")));
        
        
        String input = (String) ds.nextToken();
       
        Labeler.init(input);
        Labeler.getSemanticLabels();
        Labeler.getConcepts();
        
       
        
    }
    
}
