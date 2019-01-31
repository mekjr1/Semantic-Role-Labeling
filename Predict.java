package tw.edu.sinica.iss.nlplab.srl.predictor;

///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2001 Chieu Hai Leong and Jason Baldridge
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////   


import java.io.FileReader;

import opennlp.maxent.BasicContextGenerator;
import opennlp.maxent.ContextGenerator;
import opennlp.maxent.DataStream;
import opennlp.maxent.PlainTextByLineDataStream;
import opennlp.model.GenericModelReader;
import opennlp.model.MaxentModel;
import opennlp.maxent.ModelTrainer;
import opennlp.maxent.TrainEval;

import opennlp.model.RealValueFileEventStream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Test the model on some input.
 *
 * @author Jason Baldridge
 * @version $Revision: 1.4 $, $Date: 2008/11/06 20:00:34 $
 */
public class Predict {

    MaxentModel _model;
    ContextGenerator _cg = new BasicContextGenerator();

    public Predict(MaxentModel m) {
        _model = m;
    }

    private void eval(String predicates) {
        eval(predicates, false);
    }

    private String eval(String predicates, boolean real) {
        String[] contexts = predicates.split(" ");
        double[] ocs;
        String AllOutcomes;
        String BestOutcomes;
        int best_idx;
        double[] ocs_best;
        int start1, end1;
        if (!real) {
            ocs = _model.eval(contexts);
        } else {
            float[] values = RealValueFileEventStream.parseContexts(contexts);
            ocs = _model.eval(contexts, values);
        }
        AllOutcomes = _model.getAllOutcomes(ocs);
        //System.out.println("best outcme: \n" + _model.getBestOutcome(ocs) + "\n");
        //System.out.println("all: \n" + _model.getAllOutcomes(ocs) + "\n");
        BestOutcomes = _model.getBestOutcome(ocs);

        start1 = AllOutcomes.indexOf(BestOutcomes) + BestOutcomes.length() + 1;
        //start1=AllOutcomes.indexOf(BestOutcomes);
        end1 = start1 + 6;
        //System.out.println(_model.getBestOutcome(ocs)+" "+AllOutcomes.substring(start1,end1));
        return (_model.getBestOutcome(ocs) + " " + AllOutcomes.substring(start1, end1) + "\n");
        //System.out.println(AllOutcomes);

    }

    private static void usage() {

    }

    public static void makePrediction(String dataFileName, String modelFileName, String outputFileName) {

        boolean real = false;

        Predict predictor = null;
        try {

            MaxentModel m = new GenericModelReader(new File(modelFileName)).getModel();
            predictor = new Predict(m);
        } catch (Exception e) {
            e.printStackTrace();
            //C:\Users\mekjr1\Documents\sinica-srl\data\input.txt
            System.exit(0);
        }

        try {

            DataStream ds = new PlainTextByLineDataStream(new FileReader(new File(dataFileName)));
            File statText = new File(outputFileName);
            FileOutputStream is = new FileOutputStream(statText);
            OutputStreamWriter osw = new OutputStreamWriter(is);
            Writer w = new BufferedWriter(osw);

            while (ds.hasNext()) {
                String s = (String) ds.nextToken();
                String output = predictor.eval(s.substring(0, s.lastIndexOf(' ')), real);
                //System.out.println(output);
                w.write(output);
            }
            w.close();
            return;
        } catch (Exception e) {
            System.out.println("Unable to read from specified file: " + modelFileName);
            System.out.println();
            e.printStackTrace();
        }

    }

}
