package istic.ia.tp3;

import ca.pfv.spmf.algorithms.frequentpatterns.lcm.AlgoLCM;
import ca.pfv.spmf.algorithms.frequentpatterns.lcm.Dataset;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.AlgoCloSpan;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.SequenceDatabase;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.creators.AbstractionCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.creators.AbstractionCreator_Qualitative;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.List;

/**
 * Hello world!
 */
public class App {

    private static InputStreamReader[] inputs;
    private static File output_file, result_file;
    private static String method;

    public static void main(String[] args) {
        Options optionsHelp = new Options();

        /*
            Help option group
         */
        {
            Option option = new Option("h", "help", false, "print this message.");
            optionsHelp.addOption(option);
        }

        /*
         * File option group
         */

        Options options = new Options();


        {
            Option option = new Option("s", "source", true, "source file, defaut : console");
            option.setRequired(true);
            options.addOption(option);
        }

        {
            Option option = new Option("o", "output", true, "output file, default : console");
            option.setRequired(false);
            options.addOption(option);
        }

        {
            Option option = new Option("m", "method", true, "method to use, must be one of A or B");
            option.setRequired(true);
            option.setArgs(1);
            options.addOption(option);
        }

        {
            Option option = new Option("t", "minsup", true, "Minsup Thresold, in percent");
            option.setRequired(true);
            option.setArgs(1);
            options.addOption(option);
        }

        {
            Option option = new Option("p", "pattern", true, "Max Pattern Length, default 4");
            option.setRequired(false);
            option.setArgs(1);
            options.addOption(option);
        }

        {
            Option option = new Option("r", "result", true, "Result of the computation algorithm, default : result.txt");
            option.setRequired(false);
            option.setArgs(1);
            options.addOption(option);
        }

        {
            Option option = new Option("f", "fullname", false, "Print the full name of card instead of number");
            option.setRequired(false);
            options.addOption(option);
        }


        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(optionsHelp, args, true);

            if (cmd.hasOption("help")) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("[-h] | -m <method> [-s <files>] [-o file]", options);
                System.exit(0);
            } else {
                cmd = parser.parse(options, args);
                {
                    String file_name = cmd.getOptionValue("o", "output.txt");
                    output_file = new File(file_name);
                }

                {
                    String file_name = cmd.getOptionValue("r", "result.txt");
                    result_file = new File(file_name);
                }
                String[] files_name = cmd.getOptionValues("s");

                inputs = new InputStreamReader[files_name.length];

                for (int i = 0; i < inputs.length; i++) {
                    try {
                        inputs[i] = new FileReader(new File(files_name[i]));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                method = cmd.getOptionValue("m");


                int minsup_percent = Integer.parseInt(cmd.getOptionValue("t"));
                float minsup = minsup_percent / 100.0f;

                boolean fullname = cmd.hasOption("fullname");

                CardMap map = new CardMap();

                try (PrintWriter pw = new PrintWriter(new FileOutputStream(output_file))) {

                    pw.append(parse(map, method));

                    pw.flush();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                switch (method) {
                    case "A":
                        LLCM(map, minsup, fullname);
                        break;
                    case "B":
                        CloSpan(map, minsup, fullname);
                        break;
                    default:
                        throw new IllegalArgumentException("Method " + method + " does not exists, please choose one between A or B");
                }

            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static void CloSpan(CardMap map, float minsup, boolean fullname) {

        boolean keepPatterns = true;
        boolean verbose = true;
        boolean findClosedPatterns = true;
        boolean executePruningMethods = true;

        boolean outputSequenceIdentifiers = false;

        AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();

        SequenceDatabase sequenceDatabase = new SequenceDatabase();

        try {
            sequenceDatabase.loadFile(output_file.toString(), minsup);

            AlgoCloSpan algorithm = new AlgoCloSpan(minsup, abstractionCreator, findClosedPatterns, executePruningMethods);

            algorithm.runAlgorithm(sequenceDatabase, keepPatterns, verbose, result_file.toString(), outputSequenceIdentifiers);
            System.out.println(algorithm.getNumberOfFrequentPatterns() + " pattern found.");

            if (keepPatterns) {
                System.out.println(algorithm.printStatistics());
            }

            StringBuilder builder = new StringBuilder();
            if(fullname) {
                BufferedReader br = new BufferedReader(new FileReader(result_file));

                String line = null;
                while((line = br.readLine()) != null) {
                    String[] cline = line.split("#");

                    String[] seq = cline[0].split(" ");


                    for(String cn : seq) {
                        int num = Integer.valueOf(cn);
                        if(num == -1) {
                            builder.append(", ");
                        } else {
                            builder.append(map.reverse(num).getName());
                        }
                    }



                    for(int i = 1; i < cline.length; i++) {
                        builder.append("#"+cline[i]);
                    }

                    builder.append("\n");

                }

                br.close();


                PrintWriter pr = new PrintWriter(result_file);

                pr.append(builder.toString());

                pr.flush();
                pr.close();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private static void LLCM(CardMap map, float minsup, boolean fullname) {
        Dataset dataset = null;
        try {
            dataset = new Dataset(output_file.toString());

            AlgoLCM algo = new AlgoLCM();
            Itemsets itemsets = algo.runAlgorithm(minsup, dataset, null);
            algo.printStats();

            try (PrintWriter pw = new PrintWriter(new FileOutputStream(result_file))) {
                pw.append(printIS(itemsets, fullname, map));

                pw.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String printIS(Itemsets itemsets, boolean fullname, CardMap map) {
        int patternCount = 0;
        int levelCount = 0;

        StringBuilder ret = new StringBuilder();

        for (Iterator var5 = itemsets.getLevels().iterator(); var5.hasNext(); ++levelCount) {
            List<Itemset> level = (List) var5.next();
            ret.append("  L" + levelCount + " \n");
            Iterator var7 = level.iterator();

            while (var7.hasNext()) {
                Itemset itemset = (Itemset) var7.next();
                ret.append("  pattern " + patternCount + ":  ");

                String sets = itemset.toString();
                if (fullname) {
                    String[] cards = sets.split(" ");

                    for (String c : cards) {
                        CardMap.Card card = map.reverse(Integer.valueOf(c));
                        ret.append(card.getName() + ", ");
                    }
                } else {
                    ret.append(sets);
                }


                ret.append("support :  " + itemset.getAbsoluteSupport());
                ++patternCount;
                ret.append("\n");
            }
        }
        return ret.toString();
    }

    private static String methodB(CardMap map, BufferedReader br) throws IOException {

        ArrayList<Sequence> seqs = new ArrayList<>();

        String line;
        Sequence seq = null;
        Deck deck = null;
        int turn = 0;
        char player = 'X';
        while ((line = br.readLine()) != null) {
            String[] dataLine = line.split(" ");
            if (dataLine.length == 3) {
                if (dataLine[1].equals("Begin")) {
                    if (seq != null) {
                        seqs.add(seq);
                    }
                    seq = new Sequence();
                } else {
                    char oldPlayer = player;
                    int oldTurn = turn;

                    player = dataLine[1].charAt(0);
                    turn = Integer.valueOf(dataLine[2]);

                    if (oldPlayer != player || oldTurn != turn) {
                        if (deck != null && deck.size() > 0) {
                            seq.addSequence(deck);
                        }
                        deck = new Deck();
                    }

                    if (player == 'M' || player == 'O') {
                        deck.addCard(map.getCard(dataLine[1].substring(1)));
                    } else if (!dataLine[1].equals("Begin")) {
                        throw new InputMismatchException("Miss match input at : " + dataLine[1]);
                    }
                }
            } else {
                throw new InputMismatchException("Miss match input at : " + line);
            }

        }

        StringBuilder ret = new StringBuilder();

        for (Sequence s : seqs) {
            ret.append(s + "\n");
        }

        return ret.toString();
    }

    private static String parse(CardMap map, String method) {
        StringBuilder ret = new StringBuilder();
        for (InputStreamReader isr : inputs) {
            BufferedReader br = new BufferedReader(isr);
            try {
                switch (method) {
                    case "A":
                        ret.append(methodA(map, br));
                        break;
                    case "B":
                        ret.append(methodB(map, br));
                        break;

                    default:
                        throw new IllegalArgumentException("Method " + method + " does not exists, please choose one between A or B");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return ret.toString();
    }

    private static String methodA(CardMap map, BufferedReader br) throws IOException {
        ArrayList<Deck> decks = new ArrayList<>();

        String line;
        int me = 0, op = 1;
        Deck[] playerDecks = new Deck[2];
        while ((line = br.readLine()) != null) {
            String[] dataLine = line.split(" ");
            if (dataLine.length == 3) {
                if (dataLine[1].equals("Begin")) {
                    for (Deck d : playerDecks) {
                        if (d != null) {
                            decks.add(d);
                        }
                    }
                    playerDecks[me] = new Deck();
                    playerDecks[op] = new Deck();
                } else {
                    if (dataLine[1].startsWith("M")) {
                        playerDecks[me].addCard(map.getCard(dataLine[1].substring(1)));
                    } else if (dataLine[1].startsWith("O")) {
                        playerDecks[op].addCard(map.getCard(dataLine[1].substring(1)));
                    } else {
                        throw new InputMismatchException("Missmatch input at : " + dataLine[1]);
                    }
                }
            } else {
                throw new InputMismatchException("Missmatch input at : " + line);
            }
        }

        StringBuilder ret = new StringBuilder();

        for (Deck d : decks) {
            ret.append(d + "\n");
        }

        return ret.toString();
    }
}
