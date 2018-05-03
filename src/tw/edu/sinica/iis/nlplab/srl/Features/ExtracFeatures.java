package tw.edu.sinica.iis.nlplab.srl.Features;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.Pair;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import tw.edu.sinica.iss.nlplab.srl.predictor.Parser;


/**
 *
 * @author mekjr1
 */
public class ExtracFeatures {

    private static ArrayList<Tree> getPredicateSubTrees(Tree tree, ArrayList<Tree> pred_trees) {

        ArrayList<String> preds = new ArrayList<>(Arrays.asList("VB", "VBZ", "VBN", "VBD", "VBG", "VBP"));
        //check if the tree's value is a verb form
        if (preds.contains(tree.value())) {
            //if yes add it to the list of predicates
            pred_trees.add(tree);
            //System.out.println("       Terno: " + tree.firstChild().value());
        }
        //for each subtree, do the same process over and over again, until no tree has subtrees
        for (Tree ch : tree.getChildrenAsList()) {
            //System.out.println(tree.value());
            getPredicateSubTrees(ch, pred_trees);
        }
        return pred_trees;
    }

    private static Pair<String, Integer> getNodeToNodePath(Tree tree, Tree cand, Tree predicateSubTree) {
        ArrayList<Tree> path2 = (ArrayList<Tree>) tree.pathNodeToNode(cand, predicateSubTree);
        StringBuilder path = new StringBuilder();
        for (Tree pathTree : path2) {
            path.append(pathTree.value());
        }
        return new Pair(path.toString(), path2.size());
    }

    private static Pair<String, String> getHeadPosition(HashMap treeHeadDict, Tree cand) {
        Pair<String, String> headPosition = new Pair();
        String head_position;
        String head;
        if (treeHeadDict.containsKey(cand.toString().replaceAll(" ", ""))) {
            Tree hsolver = Tree.valueOf((String) treeHeadDict.get(cand.toString().replaceAll(" ", "")));
            head = hsolver.firstChild().value();
            head_position = hsolver.value();

        } else {
            head_position = "no-h-pos";
            head = "no-h";
        }
        headPosition.setFirst(head);
        headPosition.setSecond(head_position);
        return headPosition;
    }

    private static Pair<String, String> getSubCatAtAndSubCatStart(Tree tree, Tree cand) {
        Tree parent = cand.parent(tree);
        //ArrayList<Tree> children = (ArrayList<Tree>) tree.pathNodeToNode(cand, predicateSubTree);
        StringBuilder sub = new StringBuilder(cand.value());
        StringBuilder subSt = new StringBuilder(parent.value());

        for (Tree pathTree : parent.children()) {
            if (!pathTree.isLeaf()) {
                subSt.append(pathTree.value());
            }
        }
        for (Tree pathTree : cand.children()) {
            if (!pathTree.isLeaf()) {
                sub.append(pathTree.value());
            }

        }

        return new Pair(sub.toString(), subSt.toString());
    }

    private static ArrayList<ArrayList> extractFeatures(HashMap treeHeadDict, Tree tree) {
        //Getting the predicates sub trees
        ArrayList<Tree> predicateSubTreeList = getPredicateSubTrees(tree, new ArrayList());
        //System.out.println(predicateSubTreeList.size());
        ArrayList<Tree> pruned_candidate;// = new ArrayList();
        ArrayList<String> identifierM = new ArrayList();
        ArrayList<String> classifierM = new ArrayList();
        ArrayList<String> predictorM = new ArrayList();

        ArrayList<ArrayList> files = new ArrayList();
        for (Tree predicateSubTree : predicateSubTreeList) {
           // System.out.println("Start");
            pruned_candidate = pruning(tree, tree, predicateSubTree, new ArrayList());
           // System.out.println("Pruned size: " + pruned_candidate.size());
            String tWord = predicateSubTree.firstChild().value();
            String subCat = getSubCatAtAndSubCatStart(tree, predicateSubTree.parent(tree)).first();
            for (Tree cand : pruned_candidate) {
                Pair<String, Integer> pathAndLength = getNodeToNodePath(tree, cand, predicateSubTree);
                //System.out.println(cand.toString().replaceAll(" ", ""));
                //Create a separate getHead methods
                Pair<String, String> headAndPosition = getHeadPosition(treeHeadDict, cand);
                Pair<String, String> subCatAtAndSubCatStart = getSubCatAtAndSubCatStart(tree, cand);
                //System.out.println("Tree  "+cand);
                //System.out.println(headAndPosition.first()+" "+headAndPosition.second());
                String path = pathAndLength.first();
                int distance = pathAndLength.second();
                String head = headAndPosition.first();
                String headPosition = headAndPosition.second();
                String pt = cand.value();
                String wordAndPt = tWord + pt;
                String wordAndHead = tWord + head;
                String distanceAndWord = distance + tWord;
                String subCatAt = subCatAtAndSubCatStart.first();
                String subCatStart = subCatAtAndSubCatStart.second();
                String flatArgument = getFlatArgument(cand);

                //System.out.println(subCat + " " + flatArgument + " " + head + " " + headPosition
                //        + " " + pt + " " + wordAndPt + " "
                //        + wordAndHead + " " + distanceAndWord + " " + subCatStart + " " + subCatAt);
                identifierM.add("h=" + head + " h_pos=" + headPosition + " path=" + path + " t_word_pls_pt=" + wordAndPt + " t_word_pls_h_word=" + wordAndHead + " distance_pls_t_word=" + distanceAndWord+" ?");
                classifierM.add("h=" + head + " h_pos=" + headPosition + " h_word=" + head + " h_word_pos=" + headPosition + " path=" + path + " t_word_pls_pt=" + wordAndPt + " t_word_pls_h_word=" + wordAndHead + " subcat=" + subCat + " subcatAt=" + subCatAt + " subcatStar=" + subCatStart+" ?");
                predictorM.add(tWord + ' ' + flatArgument);
            }
            //System.out.println("End");
        }
        files.add(0, identifierM);
        files.add(1, classifierM);
        files.add(2, predictorM);

        return files;
    }

    private static void writeFiles(FileWriter fileWriter, ArrayList<String> features) throws IOException {
        for (String str : features) {
            fileWriter.write(str + "\n");
        }
        fileWriter.close();
    }

    private static String getFlatArgument(Tree cand) {
        StringBuilder flatArgument = new StringBuilder();
        for (Tree leaf : cand.getLeaves()) {
            if (!leaf.parent(cand).value().equals("IN") && !leaf.parent(cand).value().equals("TO")) {
                flatArgument.append(leaf.value()).append("_");
            }
        }
        String string = flatArgument.toString();
        if (string.endsWith("_")) {

            string = string.substring(0, string.length() - 1);
        }
        return string;
    }

    public static ArrayList<Tree> getCandidateList(Tree parsed, Tree tree, Tree targetTree, ArrayList<Tree> candidates) {
        Tree temp_parent;
        Tree temp_node;
        //ArrayList<Tree> candidates = new ArrayList<>();
        if (tree == null) {
            return null;
        }
        for (Tree ch : tree.getChildrenAsList()) {
            //consider using !ch.equals(node)
            if (!ch.equals(targetTree)) {
                if (ch.value().equals("PP")) {
                    candidates.add(ch);
                    for (Tree c : ch.getChildrenAsList()) {
                        candidates.add(c);
                    }
                } else {
                    candidates.add(ch);
                }
            }
        }
        temp_parent = tree.parent(parsed);
        temp_node = tree;
        getCandidateList(parsed, temp_parent, temp_node, candidates);
        return candidates;

    }

    public static ArrayList<Tree> pruning(Tree targetTree, Tree parsed, Tree predicateTree, ArrayList<Tree> candidates) {
        int terNo = 0;
        if (targetTree.equals(predicateTree)) {
            //node value was node.terNo
            //revisit this candidates = pruning(ch,pred,predNum,candidates);
            Tree parent = targetTree.parent(parsed);
            candidates = getCandidateList(parsed, parent, targetTree, candidates);
            return candidates;
        } else {//node value was node.terNo
        }
        List<Tree> children = targetTree.getChildrenAsList();
        for (Tree ch : children) {
            pruning(ch, predicateTree, parsed, candidates);
        }
        return candidates;
    }

    public static void extract(String treeFile, String headsFile, String inputString) throws FileNotFoundException, IOException {

        
        HashMap treeHeadDict = creatTreeHeadDict(treeFile, headsFile);
        //String str = "I love my mom, she always cook for me.";
        Parser parser = new Parser();
        Tree tree = parser.parse(inputString);
        try (PrintWriter writer = new PrintWriter("parser-output.txt", "UTF-8")) {
            writer.println(tree);
        }

        ArrayList<ArrayList> features = extractFeatures(treeHeadDict, tree);

        FileWriter identifierWriter = new FileWriter("identifier.test");
        FileWriter classifierWriter = new FileWriter("classifier.test");
        FileWriter predictorWriter = new FileWriter("pred.test");

        writeFiles(identifierWriter, features.get(0));
        writeFiles(classifierWriter, features.get(1));
        writeFiles(predictorWriter, features.get(2));

    }

    public static HashMap creatTreeHeadDict(String treeFile, String headFile) throws FileNotFoundException, IOException {
        BufferedReader treesBr = new BufferedReader(new FileReader(treeFile));
        BufferedReader headsBr = new BufferedReader(new FileReader(headFile));
        HashMap tree_head_dict = new HashMap();
        String treeBr;//String to store the readlines from the tree

        while ((treeBr = treesBr.readLine()) != null) {
            //System.out.println(treeBr);
            tree_head_dict.put(treeBr.replaceAll(" ", ""), headsBr.readLine().trim());
        }

        return tree_head_dict;
    }
}
