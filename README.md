# Semantic-Role-Labeling
This is the java/python implementation of SRL from SINICA
The system includes three major components:
    a syntactic parser, 
    a semantic role labeler, and 
    a concept formulation component. 

The input sentence is first transformed into a syntactic parse tree through a syntactical analysis step that almost all automatic semantic role labeling systems require (Johansson and Nugues 2008). Here
the Stanford parser (Klein and Manning 2003) is utilized.
After users input a sentence, the system will automatically parse, label semantic roles and report the related concepts for it. 

To develop a SRL system, a total of 33 features including features related to the head word related features, target word related features, grammar related features, and semantic type related features, are collected from related work (Xue, 2008; Ding and Chang, 2008; Sun and Jurafsky
2004; Gildea and Jurafsky 2002). Then the baseline maximum entropy system is developed using these features (Manning and Schutze, 1999). 
