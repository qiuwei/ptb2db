package es.qiu.utils.ptb2db;

import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.Tree;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Transforms Penn Treebank to the Dan Bikel's parser format
 *
 */
public class Transformer
{
    private static final String MODEL_PATH = "models/english-left3words-distsim.tagger";
    private static final MaxentTagger tagger = new MaxentTagger(MODEL_PATH);

    private static PennTreeReader loadPTBCorpus(String fileName){
        PennTreeReader ptReader = null;
        try {
            ptReader = new PennTreeReader(new BufferedReader(new FileReader(new File(fileName))));
        } catch (FileNotFoundException e) {
           e.printStackTrace();
        }
        return ptReader;
    }

    public static void transform(String fileName, Option option) throws IOException{
        PennTreeReader ptReader = loadPTBCorpus(fileName);
        Tree currentTree = ptReader.readTree();
        while (currentTree != null) {
            switch (option) {
                case empty: System.out.println(transformEmpty(currentTree)); break;
                case gold: System.out.println(transformGold(currentTree)); break;
                case stanford: System.out.println(transformStanford(currentTree)); break;
            }
            currentTree = ptReader.readTree();
        }
    }

    private static String transformGold(Tree tree) {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        List<Tree> nodes = tree.getLeaves();
        List<Label> labels = tree.preTerminalYield();
        Iterator<Tree> inodes = nodes.iterator();
        Iterator<Label> ilabels = labels.iterator();
        while(inodes.hasNext() && ilabels.hasNext()){
            String s = MessageFormat.format("({0} ({1}))", inodes.next(), ilabels.next());
            sb.append(s);
            sb.append(' ');
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(')');
        return sb.toString();
    }

    private static String transformEmpty(Tree tree){
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        List<Tree> nodes = tree.getLeaves();
        for (Tree node : nodes) {
            sb.append(node);
            sb.append(' ');
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(')');
        return sb.toString();
    }

    private static String transformStanford(Tree tree){
        List<Word> sentence = new ArrayList<Word>();
        for (Tree node : tree.getLeaves()) {
            sentence.add(new Word(node.label()));
        }
        List<TaggedWord> taggedSentence = tagger.tagSentence(sentence);
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for(TaggedWord taggedWord : taggedSentence){
            String s = MessageFormat.format("({0} ({1}))", taggedWord.word(), taggedWord.tag());
            sb.append(s);
            sb.append(' ');
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(')');
        return sb.toString();
    }


    public static void main( String[] args )
    {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("ptb2db").description("Transform Penn Treebank into Dan Bikel's Parser Format");
        parser.addArgument("file").metavar("F").help("Name of the file which contains Penn Treebank style data");
        parser.addArgument("tag").metavar("T").choices("empty", "gold", "stanford").help("Type of tags used");
        try {
            Namespace res = parser.parseArgs(args);
            transform((String)res.get("file"), Option.valueOf((String)res.get("tag")));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArgumentParserException e) {
            e.printStackTrace();
        }
    }
}
