import java.io.*;
import java.util.*;

public class A1_G4_t2 {

    public static void main(String[] args) throws FileNotFoundException {
        long start = System.currentTimeMillis();
        new FPGrowth(new File(args[0]), Float.parseFloat(args[1]));
        // System.out.println((System.currentTimeMillis() - start));
    }
}

class FPGrowth {
    float sup;
    int transactionCount;
    ArrayList<FPtree> headerTable;
    FPtree fptree;
    Map<String, Integer> frequentPatterns;


    public FPGrowth(File file, Float sup) throws FileNotFoundException {
        this.sup = sup; // support threshold
        this.transactionCount = 0;
        fptree(file);
        fpgrowth(fptree, sup, headerTable);
        print();
    }

    private void fptree(File file) throws FileNotFoundException {
        Map<String, Integer> itemsMaptoFrequencies = new HashMap<String, Integer>();
        List<String> sortedItemsbyFrequencies = new LinkedList<String>();
        ArrayList<String> itemstoRemove = new ArrayList<String>();
        preProcessing(file, itemsMaptoFrequencies, sortedItemsbyFrequencies, itemstoRemove);
        construct_fpTree(file, itemsMaptoFrequencies, sortedItemsbyFrequencies, itemstoRemove);
    }

    private void preProcessing(File file, Map<String, Integer> itemsMaptoFrequencies, List<String> sortedItemsbyFrequencies, ArrayList<String> itemstoRemove) throws FileNotFoundException {
        String line;
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            while ((line = br.readLine()) != null) {
                String[] lineStrings = line.split(",");
                for (String item : lineStrings) {
                    itemsMaptoFrequencies.put(item, itemsMaptoFrequencies.getOrDefault(item, 0) + 1);
                }
                transactionCount++;
            }
        } catch (IOException e) {
        }

        sortedItemsbyFrequencies.add("null");
        itemsMaptoFrequencies.put("null", 0);

        for (String item : itemsMaptoFrequencies.keySet()) {
            int count = itemsMaptoFrequencies.get(item);
            int i = 0;
            
            for (String listItem : sortedItemsbyFrequencies) {
                if (itemsMaptoFrequencies.get(listItem) < count) {
                    sortedItemsbyFrequencies.add(i, item);
                    break;
                }
                i++;
            }
        }

        for (String listItem : sortedItemsbyFrequencies) {
            if (itemsMaptoFrequencies.get(listItem) / (float) transactionCount < sup) {
                itemstoRemove.add(listItem);
            }
        }
        for (String itemtoRemove : itemstoRemove) {
            sortedItemsbyFrequencies.remove(itemtoRemove);
        }
    }

    private void construct_fpTree(File file, Map<String, Integer> itemsMaptoFrequencies, List<String> sortedItemsbyFrequencies, ArrayList<String> itemstoRemove) throws FileNotFoundException {
        headerTable = new ArrayList<FPtree>();
        for (String itemsforTable : sortedItemsbyFrequencies) {
            headerTable.add(new FPtree(itemsforTable));
        }
        
        fptree = new FPtree("null");
        fptree.item = null;
        fptree.root = true;
        
        String line;
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            while ((line = br.readLine()) != null) {
                ArrayList<String> transactionSortedbyFrequencies = new ArrayList<String>();
                String[] lineStrings = line.split(",");

                for (String item : lineStrings) {
                    if (itemstoRemove.contains(item)) {
                        continue;
                    }
                    int index = 0;
                    for (String vectorString : transactionSortedbyFrequencies) {
                        if (itemsMaptoFrequencies.get(vectorString) < itemsMaptoFrequencies.get(item) || ((itemsMaptoFrequencies.get(vectorString) == itemsMaptoFrequencies.get(item)) && (vectorString.compareToIgnoreCase(item) < 0 ? true : false))) {
                            transactionSortedbyFrequencies.add(index, item);
                            break;
                        }
                        index++;
                    }

                    if (!transactionSortedbyFrequencies.contains(item)) {
                        transactionSortedbyFrequencies.add(item);
                    }
                }
                insert_base(transactionSortedbyFrequencies, fptree, headerTable);
                transactionSortedbyFrequencies.clear();
            }
        } catch (IOException e) {
        }

        for (FPtree item : headerTable) {
            int count = 0;
            FPtree itemtemp = item;
            while (itemtemp.next != null) {
                itemtemp = itemtemp.next;
                count += itemtemp.count;
            }
            item.count = count;
        }

        FrequencyComparatorInHeaderTable c = new FrequencyComparatorInHeaderTable();
        Collections.sort(headerTable, c);
    }

    private FPtree construct_fpTree_cond(Map<String, Integer> conditionalPatternBase, Map<String, Integer> conditionalItemsMaptoFrequencies, float sup, ArrayList<FPtree> conditional_headerTable) {
        FPtree conditional_fptree = new FPtree("null");
        conditional_fptree.item = null;
        conditional_fptree.root = true;

        for (String pattern : conditionalPatternBase.keySet()) {
            ArrayList<String> pattern_vector = new ArrayList<String>();
            StringTokenizer tokenizer = new StringTokenizer(pattern, ",");
            while (tokenizer.hasMoreTokens()) {
                String fix = tokenizer.nextToken();
                if (conditionalItemsMaptoFrequencies.get(fix) / (float) transactionCount >= sup) {
                    pattern_vector.add(fix);
                }
            }
            insert_cond(pattern_vector, conditionalPatternBase.get(pattern), conditional_fptree, conditional_headerTable);
        }
        return conditional_fptree;
    }
    
    void insert_base(ArrayList<String> transactionSortedByFrequencies, FPtree fptree, List<FPtree> headerTable) {
        if (transactionSortedByFrequencies.isEmpty()) {
            return;
        }
        String itemToAddToTree = transactionSortedByFrequencies.get(0);
        FPtree newNode = null;
        boolean isDone = false;
        for (FPtree child : fptree.children) {
            if (child.item.equals(itemToAddToTree)) {
                newNode = child;
                child.count++;
                isDone = true;
                break;
            }
        }
        if (!isDone) {
            newNode = new FPtree(itemToAddToTree);
            newNode.count = 1;
            newNode.parent = fptree;
            fptree.children.add(newNode);
            for (FPtree headerPointer : headerTable) {
                if (headerPointer.item.equals(itemToAddToTree)) {
                    while (headerPointer.next != null) {
                        headerPointer = headerPointer.next;
                    }
                    headerPointer.next = newNode;
                    break;
                }
            }
        }
        transactionSortedByFrequencies.remove(0);
        insert_base(transactionSortedByFrequencies, newNode, headerTable);
    }

    private void insert_cond(ArrayList<String> pattern_vector, int count_of_pattern, FPtree conditional_fptree, ArrayList<FPtree> conditional_headerTable) {
        if (pattern_vector.isEmpty()) {
            return;
        }
        String itemtoAddtotree = pattern_vector.get(0);
        FPtree newNode = null;
        boolean ifisdone = false;
        for (FPtree child : conditional_fptree.children) {
            if (child.item.equals(itemtoAddtotree)) {
                newNode = child;
                child.count += count_of_pattern;
                ifisdone = true;
                break;
            }
        }
        if (!ifisdone) {
            for (FPtree headerPointer : conditional_headerTable) {
                //this if also gurantees removing og non frequets
                if (headerPointer.item.equals(itemtoAddtotree)) {
                    newNode = new FPtree(itemtoAddtotree);
                    newNode.count = count_of_pattern;
                    newNode.parent = conditional_fptree;
                    conditional_fptree.children.add(newNode);
                    while (headerPointer.next != null) {
                        headerPointer = headerPointer.next;
                    }
                    headerPointer.next = newNode;
                }
            }
        }
        pattern_vector.remove(0);
        insert_cond(pattern_vector, count_of_pattern, newNode, conditional_headerTable);
    }

    private void fpgrowth(FPtree fptree, Float sup, ArrayList<FPtree> headerTable) {
        frequentPatterns = new HashMap<String, Integer>();
        FPgrowth(fptree, null, sup, headerTable, frequentPatterns);
    }

    void FPgrowth(FPtree fptree, String base, Float sup, ArrayList<FPtree> headerTable, Map<String, Integer> frequentPatterns) {
        for (FPtree iteminTree : headerTable) {
            String currentPattern = (base != null ? base : "") + (base != null ? ", " : "") + iteminTree.item;
            int supportofCurrentPattern = 0;
            Map<String, Integer> conditionalPatternBase = new HashMap<String, Integer>();
            while (iteminTree.next != null) {
                iteminTree = iteminTree.next;
                supportofCurrentPattern += iteminTree.count;
                String conditionalPattern = null;
                FPtree conditionalItem = iteminTree.parent;

                while (!conditionalItem.isRoot()) {
                    conditionalPattern = conditionalItem.item + (conditionalPattern != null ? "," +conditionalPattern : "");
                    conditionalItem = conditionalItem.parent;
                }

                if (conditionalPattern != null) {
                    conditionalPatternBase.put(conditionalPattern, iteminTree.count);
                }
            }
            frequentPatterns.put(currentPattern, supportofCurrentPattern);

            Map<String, Integer> conditionalItemsMaptoFrequencies = new HashMap<String, Integer>();
            for (String conditionalPattern : conditionalPatternBase.keySet()) {
                StringTokenizer tokenizer = new StringTokenizer(conditionalPattern, ",");
                while (tokenizer.hasMoreTokens()) {
                    String item = tokenizer.nextToken();
                    if (conditionalItemsMaptoFrequencies.containsKey(item)) {
                        int count = conditionalItemsMaptoFrequencies.get(item);
                        count += conditionalPatternBase.get(conditionalPattern);
                        conditionalItemsMaptoFrequencies.put(item, count);
                    } else {
                        conditionalItemsMaptoFrequencies.put(item, conditionalPatternBase.get(conditionalPattern));
                    }
                }
            }

            ArrayList<FPtree> conditional_headerTable = new ArrayList<FPtree>();

            for (String itemsforTable : conditionalItemsMaptoFrequencies.keySet()) {
                int count = conditionalItemsMaptoFrequencies.get(itemsforTable);

                if (count / (float) transactionCount < sup) {
                    continue;
                }
                FPtree f = new FPtree(itemsforTable);
                f.count = count;
                conditional_headerTable.add(f);
            }


            FPtree conditional_fptree = construct_fpTree_cond(conditionalPatternBase, conditionalItemsMaptoFrequencies, sup, conditional_headerTable);
            Collections.sort(conditional_headerTable, new FrequencyComparatorInHeaderTable());
            if (!conditional_fptree.children.isEmpty()) {
                FPgrowth(conditional_fptree, currentPattern, sup, conditional_headerTable, frequentPatterns);
            }

        }
    }

    private void print() {
        // sort the frequent patterns by value
        List<Map.Entry<String, Integer>> sortedFrequentPatterns = new ArrayList<>(frequentPatterns.entrySet());
        sortedFrequentPatterns.sort(Map.Entry.comparingByValue(Comparator.naturalOrder()));
        for (Map.Entry<String, Integer> frequentPattern : sortedFrequentPatterns) {
            System.out.println(frequentPattern.getKey() + " " + frequentPattern.getValue()/ (float) transactionCount);
        }
    }
}

class FPtree {

    boolean root;
    String item;
    ArrayList<FPtree> children;
    FPtree parent;
    FPtree next;
    int count;

    public FPtree(String item) {
        this.item = item;
        next = null;
        children = new ArrayList<>();
        root = false;
    }

    boolean isRoot(){
        return root;
    }

}

class FrequencyComparatorInHeaderTable implements Comparator<FPtree> {
    @Override
    public int compare(FPtree a, FPtree b) {
        return Integer.compare(a.count, b.count);
    }
}
