import java.io.*;
import java.util.*;

public class A1_G4_t2 {

    public static void main(String[] args) throws FileNotFoundException {
        new FPGrowth(new File(args[0]), Float.parseFloat(args[1]));
    }
}

class FPGrowth {
    float sup;
    int transactionCount;
    ArrayList<FPtree> headerTable;
    FPtree fptree;
    Map<String, Integer> frequentPatterns;


    public FPGrowth(File file, Float sup) throws FileNotFoundException {
        long start = System.currentTimeMillis();

        this.sup = sup; // support threshold
        this.transactionCount = 0;
        fptree(file);
        fpgrowth(fptree, sup, headerTable);
        long time = System.currentTimeMillis() - start;
        print();
        System.out.println("Execution time is " + time + " milliseconds");
    }

    private void fptree(File file) throws FileNotFoundException {
        Map<String, Integer> itemsMaptoFrequencies = new HashMap<String, Integer>();
        List<String> sortedItemsbyFrequencies = new LinkedList<String>();
        ArrayList<String> itemstoRemove = new ArrayList<String>();
        preProcessing(file, itemsMaptoFrequencies, sortedItemsbyFrequencies, itemstoRemove);
        construct_fpTree(file, itemsMaptoFrequencies, sortedItemsbyFrequencies, itemstoRemove);
    }

    private void preProcessing(File file, Map<String, Integer> itemsMapToFrequencies, List<String> sortedItemsByFrequencies, ArrayList<String> itemsToRemove) throws FileNotFoundException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] items = line.split(",");
                for (String item : items) {
                    itemsMapToFrequencies.merge(item, 1, Integer::sum);
                }
                transactionCount++;
            }
        } catch (IOException e) {
        }

        sortedItemsByFrequencies.add("null");
        itemsMapToFrequencies.put("null", 0);

        itemsMapToFrequencies.keySet().forEach(item -> {
            int count = itemsMapToFrequencies.get(item);
            int insertIndex = 0;
            for (String listItem : sortedItemsByFrequencies) {
                if (itemsMapToFrequencies.getOrDefault(listItem, 0) < count) {
                    sortedItemsByFrequencies.add(insertIndex, item);
                    return;
                }
                insertIndex++;
            }
        });

        sortedItemsByFrequencies.removeAll(itemsToRemove);
        itemsToRemove.clear();
        sortedItemsByFrequencies.forEach(item -> {
            if (itemsMapToFrequencies.getOrDefault(item, 0) / (float) transactionCount < sup) {
                itemsToRemove.add(item);
            }
        });
        sortedItemsByFrequencies.removeAll(itemsToRemove);
    }


    private void construct_fpTree(File file, Map<String, Integer> itemsMapToFrequencies, List<String> sortedItemsByFrequencies, ArrayList<String> itemsToRemove) throws FileNotFoundException {
        headerTable = new ArrayList<>();
        for (String item : sortedItemsByFrequencies) {
            if (!itemsToRemove.contains(item)) {
                headerTable.add(new FPtree(item));
            }
        }

        fptree = new FPtree("null");
        fptree.item = null;
        fptree.root = true;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                List<String> transaction = Arrays.asList(line.split(","));
                List<String> filteredTransaction = new ArrayList<>();
                for (String item : transaction) {
                    if (!itemsToRemove.contains(item) && itemsMapToFrequencies.containsKey(item)) {
                        filteredTransaction.add(item);
                    }
                }

                filteredTransaction.sort((item1, item2) -> {
                    int frequencyCompare = itemsMapToFrequencies.get(item2).compareTo(itemsMapToFrequencies.get(item1));
                    if (frequencyCompare == 0) {
                        return item1.compareToIgnoreCase(item2);
                    }
                    return frequencyCompare;
                });

                insertTransaction(filteredTransaction, fptree, headerTable);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        updateHeaderTableCounts();
    }

    private void insertTransaction(List<String> transaction, FPtree node, List<FPtree> headerTable) {
        if (transaction.isEmpty()) return;

        String currentItem = transaction.get(0);
        transaction.remove(0);
        FPtree child = null;
        for (FPtree tempChild : node.children) {
            if (tempChild.item.equals(currentItem)) {
                child = tempChild;
                break;
            }
        }
        if (child == null) {
            child = new FPtree(currentItem);
            child.parent = node;
            node.children.add(child);
            linkToHeaderTable(child, headerTable);
        }
        child.count++;
        insertTransaction(new ArrayList<>(transaction), child, headerTable);
    }

    private void linkToHeaderTable(FPtree node, List<FPtree> headerTable) {
        for (FPtree headerNode : headerTable) {
            if (headerNode.item.equals(node.item)) {
                while (headerNode.next != null) {
                    headerNode = headerNode.next;
                }
                headerNode.next = node;
                break;
            }
        }
    }

    private void updateHeaderTableCounts() {
        for (FPtree header : headerTable) {
            int count = 0;
            FPtree temp = header.next;
            while (temp != null) {
                count += temp.count;
                temp = temp.next;
            }
            header.count = count;
        }
        headerTable.sort(Comparator.comparingInt(a -> a.count));
    }

    private FPtree construct_fpTree_cond(Map<String, Integer> conditionalPatternBase, Map<String, Integer> conditionalItemsMaptoFrequencies, float sup, ArrayList<FPtree> conditional_headerTable) {
        FPtree conditional_fptree = new FPtree("null");
        conditional_fptree.item = null;
        conditional_fptree.root = true;

        for (Map.Entry<String, Integer> entry : conditionalPatternBase.entrySet()) {
            String pattern = entry.getKey();
            int patternCount = entry.getValue();

            List<String> patternVector = new ArrayList<>();
            for (String item : pattern.split(",")) {
                if (conditionalItemsMaptoFrequencies.containsKey(item) && conditionalItemsMaptoFrequencies.get(item) / (float) transactionCount >= sup) {
                    patternVector.add(item);
                }
            }

            if (!patternVector.isEmpty()) {
                insert_cond(patternVector, patternCount, conditional_fptree, conditional_headerTable);
            }
        }
        return conditional_fptree;
    }

    private void insert_cond(List<String> patternVector, int countOfPattern, FPtree conditionalFptree, ArrayList<FPtree> conditionalHeaderTable) {
        if (patternVector.isEmpty()) {
            return;
        }

        String itemToAddToTree = patternVector.remove(0);
        FPtree newNode = findOrCreateChild(conditionalFptree, itemToAddToTree);
        newNode.count += countOfPattern;

        if (newNode.parent == null) {
            newNode.parent = conditionalFptree;
            conditionalFptree.children.add(newNode);
            linkNodeToConditionalHeaderTable(newNode, conditionalHeaderTable);
        }

        insert_cond(patternVector, countOfPattern, newNode, conditionalHeaderTable);
    }

    private FPtree findOrCreateChild(FPtree parent, String item) {
        for (FPtree child : parent.children) {
            if (child.item.equals(item)) {
                return child;
            }
        }
        return new FPtree(item);
    }

    private void linkNodeToConditionalHeaderTable(FPtree node, ArrayList<FPtree> conditionalHeaderTable) {
        for (FPtree header : conditionalHeaderTable) {
            if (header.item.equals(node.item)) {
                while (header.next != null) {
                    header = header.next;
                }
                header.next = node;
                break;
            }
        }
    }

    void fpgrowth(FPtree fptree, Float sup, ArrayList<FPtree> headerTable) {
        frequentPatterns = new HashMap<>();
        FPgrowth(fptree, "", sup, headerTable, frequentPatterns);
    }

    void FPgrowth(FPtree fptree, String base, Float sup, ArrayList<FPtree> headerTable, Map<String, Integer> frequentPatterns) {
        if (fptree == null || fptree.children.isEmpty()) {
            return;
        }

        for (FPtree header : headerTable) {
            String newPattern = base.isEmpty() ? header.item : base + "," + header.item;
            int supportOfCurrentPattern = 0;

            Map<String, Integer> conditionalPatternBase = new HashMap<>();
            FPtree temp = header.next;
            while (temp != null) {
                supportOfCurrentPattern += temp.count;
                List<String> path = new ArrayList<>();
                FPtree parent = temp.parent;
                while (parent != null && parent.item != null) {
                    path.add(parent.item);
                    parent = parent.parent;
                }
                if (!path.isEmpty()) {
                    Collections.reverse(path);
                    String pathStr = String.join(",", path);
                    conditionalPatternBase.put(pathStr, temp.count);
                }
                temp = temp.next;
            }

            if (supportOfCurrentPattern >= sup) {
                frequentPatterns.put(newPattern, supportOfCurrentPattern);
            }

            if (!conditionalPatternBase.isEmpty()) {
                Map<String, Integer> conditionalItemsMapToFrequencies = new HashMap<>();
                for (String pattern : conditionalPatternBase.keySet()) {
                    String[] items = pattern.split(",");
                    for (String item : items) {
                        conditionalItemsMapToFrequencies.merge(item, conditionalPatternBase.get(pattern), Integer::sum);
                    }
                }
                conditionalItemsMapToFrequencies.entrySet().removeIf(e -> e.getValue() / (float) transactionCount < sup);

                ArrayList<FPtree> newHeaderTable = new ArrayList<>();
                for (String item : conditionalItemsMapToFrequencies.keySet()) {
                    FPtree node = new FPtree(item);
                    node.count = conditionalItemsMapToFrequencies.get(item);
                    newHeaderTable.add(node);
                }

                FPtree conditionalFPTree = construct_fpTree_cond(conditionalPatternBase, conditionalItemsMapToFrequencies, sup, newHeaderTable);
                if (!conditionalFPTree.children.isEmpty()) {
                    FPgrowth(conditionalFPTree, newPattern, sup, newHeaderTable, frequentPatterns);
                }
            }
        }
    }


    private void print() {
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

