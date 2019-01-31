/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.edu.sinica.iss.nlplab.srl.label;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import tw.edu.sinica.iis.nlplab.srl.Features.ExtracFeatures;
import tw.edu.sinica.iss.nlplab.srl.predictor.Predict;

/**
 *
 * @author mekjr1
 */
public class Labeler {

    public static void getSemanticLabels() throws FileNotFoundException, IOException {

        //System.out.println("Semantic Role Labeling..."); 
        BufferedReader reader = new BufferedReader(new FileReader("identifier-output.txt"));
        BufferedReader classifier = new BufferedReader(new FileReader("classifier-output.txt"));
        BufferedReader preds = new BufferedReader(new FileReader("pred.test"));

        Writer statText = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("pred_arg.txt"))));

        String line;
        //System.out.println("oh");
        while ((line = reader.readLine()) != null) {
            //System.out.println("inside loop");
            String pred = preds.readLine();
            String indentifierOutput = line;
            String classifierOutput = classifier.readLine();

            if (indentifierOutput.startsWith("yes") == true) {
                //System.out.println("word");

                System.out.println(pred.split(" ")[0] + " " + classifierOutput.split(" ")[0] + " " + pred.split(" ")[1]);
                statText.write(pred.split(" ")[0] + " " + classifierOutput.split(" ")[0] + " " + pred.split(" ")[1]+"\n");

            }

        }
        statText.close();

    }

    public static void getConcepts() throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader("pred_arg.txt"));
        Writer statText = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("concepts.txt"))));
        String line;
        ArrayList <String> my;
        my = new ArrayList();
        while ((line = reader.readLine()) != null) {
             ArrayList <String> flat_arg_str_list= new ArrayList();
            
           flat_arg_str_list.addAll(Arrays.asList(line.split(" ")[2].split("and")));
            
            flat_arg_str_list.add(line.split(" ")[2]);
            System.out.println("flat arg" + flat_arg_str_list);

            for (String flat_arg : flat_arg_str_list) {
                String label = line.split(" ")[1];
            
                switch (label) {
                    case "ARG0":
                        my.add(flat_arg + "_" + line.split(" ")[0]);
                        //my.add(flat_arg_str)
                        break;
                    case "ARGM-COM":
                        my.add(line.split(" ")[0] + "_{with}_" + flat_arg);
                        my.add(flat_arg);
                        break;
                    case "ARGM-LOC":
                        my.add(line.split(" ")[0] + "_{in}_" + flat_arg);
                        my.add("{location}_" + flat_arg);
                        break;
                    case "ARGM-DIR":
                        my.add(line.split(" ")[0] + "_{direction}_" + flat_arg);
                        my.add("{direction}_" + flat_arg);
                        break;
                    case "ARGM-PRP":
                        my.add(line.split(" ")[0] + "_{in_order_to}_" + flat_arg);
                        my.add(flat_arg);
                        break;
                    case "ARGM-CAU":
                        my.add(line.split(" ")[0] + "_{because}_" + flat_arg);
                        my.add("{cause}_" + flat_arg);
                        break;
                    case "ARGM-NEG":
                        my.add(line.split(" ")[0] + "_{negation}_" + flat_arg);
                        my.add("{negation}_" + flat_arg);
                        break;
                    case "ARGM-GOL":
                        my.add(line.split(" ")[0] + "_" + flat_arg);
                        my.add("{goal}" + flat_arg);
                        break;
                    case "ARGM-MNR":
                        my.add(line.split(" ")[0] + "_" + flat_arg);
                        my.add("{manner}_" + flat_arg);
                        break;
                    case "ARGM-TMP":
                        my.add(line.split(" ")[0] + "_{when}_" + flat_arg);
                        my.add("{time}_" + flat_arg);
                        break;
                    case "ARGM-EXT":
                        my.add(line.split(" ")[0] + "_{by}_" + flat_arg);
                        my.add(flat_arg);
                        break;
                    default:
                        my.add(line.split(" ")[0] + "_" + flat_arg);
                        my.add(flat_arg);
                        break;
                }

            }
        }
        Set<String> uniques= new HashSet(my);
        for(String st: uniques){
            System.out.println(st);
            statText.write(st+"\n");
            
        }
        statText.close();
    }

    
    

    public static void init(String input) throws IOException {
        System.out.println("ayt");
        //Create an Hash to Hold Tree:Hade pairs 
        String treesFile = "headFinder/argument-trees2.txt";
        String headsFile = "headFinder/heads.txt";
        String identifierModel = "data/identifierModel.txt";
        String classifierModel = "data/classifierModel.txt";
        //Extracts the features and stores them in a file
        ExtracFeatures.extract(treesFile, headsFile, input);
        //Make predictions 
        Predict.makePrediction("identifier.test", identifierModel, "identifier-output.txt");
        Predict.makePrediction("classifier.test", classifierModel, "classifier-output.txt");
    }
}
