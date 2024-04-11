import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

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
        construct_fptree(file);
        fpgrowth(fptree, sup, headerTable);
        printFP();
    }

    private void construct_fptree(File file) throws FileNotFoundException {
        Map<String, Integer> itemsMapToFrequencies = new HashMap<>();
        List<String> transactions = new ArrayList<>();
        transactionCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                transactions.add(line);
                Arrays.stream(line.split(","))
                        .forEach(item -> itemsMapToFrequencies.merge(item, 1, Integer::sum));
                transactionCount++;
            }
        } catch (IOException e) {
        }

        final int threshold = (int) Math.ceil(sup * transactionCount);
        List<String> frequentItemsSorted = itemsMapToFrequencies.entrySet().stream()
                .filter(entry -> entry.getValue() >= threshold)
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        fptree = new FPtree("null");
        fptree.item = null;
        fptree.root = true;
        headerTable = frequentItemsSorted.stream()
                .map(FPtree::new)
                .collect(Collectors.toCollection(ArrayList::new));

        transactions.forEach(transaction -> {
            List<String> sortedFrequentItems = Arrays.stream(transaction.split(","))
                    .filter(frequentItemsSorted::contains)
                    .sorted(Comparator.comparingInt(frequentItemsSorted::indexOf))
                    .collect(Collectors.toList());
            if (!sortedFrequentItems.isEmpty()) {
                insert_base(new ArrayList<>(sortedFrequentItems), fptree, headerTable);
            }
        });

        headerTable.forEach(item -> {
            int count = 0;
            FPtree temp = item;
            while (temp.next != null) {
                temp = temp.next;
                count += temp.count;
            }
            item.count = count;
        });
        headerTable.sort(Comparator.comparingInt(a -> a.count));
    }


    private FPtree construct_fpTree_cond(Map<String, Integer> conditionalPatternBase, Map<String, Integer> conditionalItemsMaptoFrequencies, float sup, ArrayList<FPtree> conditional_headerTable) {
        FPtree conditional_fptree = new FPtree("null");
        conditional_fptree.item = null;
        conditional_fptree.root = true;

        conditionalPatternBase.forEach((pattern, count) -> {
            List<String> patternVector = Arrays.stream(pattern.split(","))
                    .filter(item -> conditionalItemsMaptoFrequencies.getOrDefault(item, 0) / (float) transactionCount >= sup)
                    .collect(Collectors.toList());

            if (!patternVector.isEmpty()) {
                insert_cond(new ArrayList<>(patternVector), count, conditional_fptree, conditional_headerTable);
            }
        });

        return conditional_fptree;
    }

    void insert_base(ArrayList<String> transactionSortedByFrequencies, FPtree fptree, List<FPtree> headerTable) {
        if (transactionSortedByFrequencies.isEmpty()) {
            return;
        }

        String itemToAddToTree = transactionSortedByFrequencies.remove(0);
        FPtree newNode = fptree.children.stream()
                .filter(child -> child.item.equals(itemToAddToTree))
                .findFirst()
                .orElseGet(() -> {
                    FPtree node = new FPtree(itemToAddToTree);
                    node.parent = fptree;
                    fptree.children.add(node);
                    linkToHeaderTable(node, headerTable);
                    return node;
                });
        newNode.count++;

        insert_base(transactionSortedByFrequencies, newNode, headerTable);
    }

    private void linkToHeaderTable(FPtree node, List<FPtree> headerTable) {
        headerTable.stream()
                .filter(header -> header.item.equals(node.item))
                .findFirst()
                .ifPresent(header -> {
                    while (header.next != null) {
                        header = header.next;
                    }
                    header.next = node;
                });
    }


    private void insert_cond(ArrayList<String> patternVector, int countOfPattern, FPtree conditionalFptree, ArrayList<FPtree> conditionalHeaderTable) {
        if (patternVector.isEmpty()) {
            return;
        }

        String itemToAddToTree = patternVector.remove(0);
        FPtree newNode = findOrCreateNode(itemToAddToTree, conditionalFptree, countOfPattern, conditionalHeaderTable);
        newNode.count += countOfPattern;

        insert_cond(patternVector, countOfPattern, newNode, conditionalHeaderTable);
    }

    private FPtree findOrCreateNode(String item, FPtree conditionalFptree, int count, ArrayList<FPtree> conditionalHeaderTable) {
        for (FPtree child : conditionalFptree.children) {
            if (child.item.equals(item)) {
                return child;
            }
        }

        FPtree newNode = new FPtree(item);
        newNode.count = count;
        newNode.parent = conditionalFptree;
        conditionalFptree.children.add(newNode);
        linkNodeToHeaderTable(newNode, conditionalHeaderTable);
        return newNode;
    }

    private void linkNodeToHeaderTable(FPtree node, ArrayList<FPtree> headerTable) {
        for (FPtree header : headerTable) {
            if (header.item.equals(node.item)) {
                while (header.next != null) {
                    header = header.next;
                }
                header.next = node;
                break;
            }
        }
    }

    private void fpgrowth(FPtree fptree, Float sup, ArrayList<FPtree> headerTable) {
        frequentPatterns = new HashMap<String, Integer>();
        FPgrowth(fptree, null, sup, headerTable, frequentPatterns);
    }

    void FPgrowth(FPtree fptree, String base, Float sup, ArrayList<FPtree> headerTable, Map<String, Integer> frequentPatterns) {
        for (FPtree iteminTree : headerTable) {
            String currentPattern = (base != null ? base : "") + (base != null ? "," : "") + iteminTree.item;
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

    private void printFP() {
        frequentPatterns.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> System.out.println(entry.getKey() + " " + entry.getValue() / (float) transactionCount));
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
