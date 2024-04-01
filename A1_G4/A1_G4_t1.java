import java.io.*;
import java.util.*;

public class A1_G4_t1 {

    static Map<Integer, LinkedHashSet<String>> dataSet = new HashMap<>();
    static Map<LinkedHashSet<String>, Integer> result = new HashMap<>();
    static Map<Integer, LinkedHashSet<LinkedHashSet<String>>> dataSet_tid = new HashMap<>();
    static Float minSupport;
    static int modBase = 101;
    static int maxLeafSize = 1000;

    public static void main(String[] args) {
        // init the input file, minimum support from command line
        String inputFile = args[0];
        minSupport = Float.parseFloat(args[1]);

        // for itemset of size 1 (later using with L1)
        Set<LinkedHashSet<String>> itemSets_1 = new HashSet<>();
        int tid = 0;

        // read the input file and sorted the items in each transaction using lexographical order
        String line;
        try(BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            while ((line = br.readLine()) != null) {
                String[] lineStrings = line.split(",");
                Arrays.sort(lineStrings);
                LinkedHashSet<String> itemSet = new LinkedHashSet<>(Arrays.asList(lineStrings));
                for (String item : itemSet) {
                    LinkedHashSet<String> comb = new LinkedHashSet<>();
                    comb.add(item);
                    itemSets_1.add(comb);
                    dataSet_tid.putIfAbsent(tid, new LinkedHashSet<>());
                    dataSet_tid.get(tid).add(comb);
                }
                dataSet.put(tid, itemSet);
                tid++;
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // call the apriori algorithm
        calRuntime("apriori",false, itemSets_1);
        printResultsInAscendingOrder(result);


    }
    private static void calRuntime(String flag, boolean isprint,Set<LinkedHashSet<String>> itemSet) {
        Date start = new Date();
        if (flag.equals("apriori")) {
            apriori(itemSet);
        } else {
            aprioriTid(itemSet);
        }
        Date end = new Date();
        long time = end.getTime() - start.getTime();
        if (isprint) {
            System.out.println("Execution time is " + time + " milliseconds");

        }
    }

    // apriori algorithm
    public static void apriori(Set<LinkedHashSet<String>> itemSet_1) {

        Set<LinkedHashSet<String>> l1 = getSupportedItemset(itemSet_1);
        Set<LinkedHashSet<String>> supportedItemset = l1;
        for(int k = 2; !supportedItemset.isEmpty(); k++) {
            // candidate generation
            Set<LinkedHashSet<String>> candidateSet = getUnion(supportedItemset, k);
            Set<LinkedHashSet<String>> candidateSetPruned = pruning(candidateSet, supportedItemset, k-1);
            // subset
            supportedItemset = getAboveMinSup(candidateSetPruned, dataSet.values(), k);
        }
    }

    // apriori algorithm with tid
    public static void aprioriTid(Set<LinkedHashSet<String>> itemSet_1) {
        Set<LinkedHashSet<String>> l1 = getSupportedItemset(itemSet_1);
        Set<LinkedHashSet<String>> supportedItemset = l1;

        Map<Integer, LinkedHashSet<LinkedHashSet<String>>> C_tid = dataSet_tid;

        for(int k = 2; !supportedItemset.isEmpty(); k++) {

            // candidate generation
            Set<LinkedHashSet<String>> candidateSet = getUnion(supportedItemset, k);
            Set<LinkedHashSet<String>> candidateSetPruned = pruning(candidateSet, supportedItemset, k-1);

            Map<Integer, Set<LinkedHashSet<String>>> C_newtid = new HashMap<>();
            Map<LinkedHashSet<String>, Integer> C_k = new HashMap<>();

            for (Integer tid : C_tid.keySet()) {
                LinkedHashSet<LinkedHashSet<String>> C_new = new LinkedHashSet<>();
                LinkedHashSet<LinkedHashSet<String>> entry = C_tid.get(tid);
                for (LinkedHashSet<String> item : candidateSetPruned) {
                    List<String> removed_k = new ArrayList<>(item);
                    List<String> removed_k_1 = new ArrayList<>(item);
                    removed_k.remove(k-1);
                    removed_k_1.remove(k-2);
                    boolean indicator_k = false;
                    boolean indicator_k_1 = false;
                    for (LinkedHashSet<String> comb : entry) {
                        if (comb.containsAll(removed_k)) {
                            indicator_k = true;
                        } else if (comb.containsAll(removed_k_1)) {
                            indicator_k_1 = true;
                        }
                        if (indicator_k && indicator_k_1) {
                            C_new.add(item);
                            C_k.put(item, C_k.getOrDefault(item, 0) + 1);

                            break;
                        }
                    }
                }

                if (!C_new.isEmpty()) {
                    C_newtid.putIfAbsent(tid, C_new);
                }
            }

            supportedItemset.clear();
            for (Map.Entry<LinkedHashSet<String>, Integer> entry : C_k.entrySet()) {
                float support = entry.getValue() / (float) dataSet.size();
                if (support >= minSupport) {
                    supportedItemset.add(entry.getKey());
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        }        
    }


    public static Set<LinkedHashSet<String>> getSupportedItemset(Set<LinkedHashSet<String>> itemset) {
        Set<LinkedHashSet<String>> map = new HashSet<>();
        for (LinkedHashSet<String> comb : itemset) {
            Boolean islarge = calcSupport(comb);
            if (islarge) {
                map.add(comb);
            }
        }
        
        return map;
    }
    // calculate the support of an itemset
    public static Boolean calcSupport(LinkedHashSet<String> itemset) {
        Integer freq = 0;
        Boolean islarge = false;
        for (LinkedHashSet<String> item : dataSet.values()) {
            if (item.containsAll(itemset)) {
                freq++;
            }
        }
        Float support = freq / ((float)dataSet.size());
        if (support >= minSupport) {
            islarge = true;
            result.put(itemset, freq);
        }
        return islarge;
    }
    
    public static Set<LinkedHashSet<String>> getAboveMinSup(Set<LinkedHashSet<String>> C_k, Collection<LinkedHashSet<String>> transactions, int k) {
        Set<LinkedHashSet<String>> supportedSet = new HashSet<>();
        Map<LinkedHashSet<String>, Integer> C_t = new HashMap<>();


        // for (LinkedHashSet<String> item : C_k) {
        //     for (LinkedHashSet<String> transaction : transactions) {
        //         if (transaction.containsAll(item)) {
        //             C_t.put(item, C_t.getOrDefault(item, 0) + 1);
        //         }
        //     }
        // }

        HashTree root = new HashTree(maxLeafSize);
        for (LinkedHashSet<String> item : C_k) {
            root.insert(new ArrayList<>(item));
        }

        for (LinkedHashSet<String> transaction : transactions) {
            List<String> transactionList = new ArrayList<>(transaction);
            Set<List<String>> identifiedSet = new HashSet<>();
            identifiedSet = root.findSubsets(transactionList, k);
            for (List<String> item : identifiedSet) {
                if (transaction.containsAll(item)) {
                    C_t.put(new LinkedHashSet<>(item), C_t.getOrDefault(new LinkedHashSet<>(item), 0) + 1);
                }
            }
        }
        
        for (Map.Entry<LinkedHashSet<String>, Integer> entry : C_t.entrySet()) {
            float support = entry.getValue() / (float) dataSet.size();
            if (support >= minSupport) {
                supportedSet.add(entry.getKey());
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return supportedSet;
    }

    public static Set<LinkedHashSet<String>> getUnion(Set<LinkedHashSet<String>> itemSet, int length) {
        Set<LinkedHashSet<String>> resultSet = new HashSet<>();
        for (LinkedHashSet<String> i : itemSet) {
            for (LinkedHashSet<String> j : itemSet) {
                if (i.equals(j)) {
                    continue;
                }
                LinkedHashSet<String> union = new LinkedHashSet<>();
                Iterator<String> iIterator = i.iterator();
                Iterator<String> jIterator = j.iterator();
                for (int k = 0; k < length - 2; k++) {
                    String iNext = iIterator.next();
                    String jNext = jIterator.next();
                    if (!iNext.equals(jNext)) {
                        break;
                    }
                    
                    union.add(iNext);
                }
                String iNext = iIterator.next();
                String jNext = jIterator.next();
                if (iNext.compareTo(jNext) < 0) {
                    union.add(iNext);
                    union.add(jNext);
                } else {
                    union.add(jNext);
                    union.add(iNext);
                }
                if (union.size() == length) {
                    resultSet.add(union);
                }
            }
        }
        return resultSet;
    }

    public static Set<LinkedHashSet<String>> pruning(Set<LinkedHashSet<String>> candidateSet, Set<LinkedHashSet<String>> prevFreqSet, int length) {
        Set<LinkedHashSet<String>> prunedSet = new HashSet<>(candidateSet);
        for (LinkedHashSet<String> item : candidateSet) {
            Set<LinkedHashSet<String>> subsets = getSubsets(item, length);
            for (Set<String> subset : subsets) {
                if (!prevFreqSet.contains(subset)) {
                    prunedSet.remove(item);
                    break;
                }
            }
        }
        return prunedSet;
    }

    private static Set<LinkedHashSet<String>> getSubsets(LinkedHashSet<String> set, int length) {
        Set<LinkedHashSet<String>> result = new HashSet<>();
        getSubsetsHelper(set, length, new LinkedHashSet<>(), result);
        return result;
    }

    private static void getSubsetsHelper(LinkedHashSet<String> set, int length, LinkedHashSet<String> current, Set<LinkedHashSet<String>> result) {
        if (current.size() == length) {
            result.add(new LinkedHashSet<>(current));
            return;
        }
        LinkedHashSet<String> remaining = new LinkedHashSet<>(set);
        for (String s : set) {
            current.add(s);
            remaining.remove(s);
            getSubsetsHelper(remaining, length, current, result);
            current.remove(s);
        }
    }

    public static void printResultsInAscendingOrder(Map<LinkedHashSet<String>, Integer> result) {
        List<Map.Entry<String, Float>> itemList = new ArrayList<>();
        int total = dataSet.size();
        for (LinkedHashSet<String> item : result.keySet()) {
            String itemString = String.join(", ", item);
            itemList.add(new AbstractMap.SimpleEntry<>(itemString, result.get(item) / ((float) total)));
        }

        // Sorting based on support value in ascending order
        itemList.sort(Map.Entry.comparingByValue());

        for (Map.Entry<String, Float> item : itemList) {
            System.out.println(item.getKey() + " : " + String.format("%.7f", item.getValue()).replaceFirst("0*$", ""));
        }
    }

    public static class HashTree {
        private Node root;
        public HashTree(int maxLeafSize) {
            this.root = new Node(false, maxLeafSize, 1);
        }

        public void insert(List<String> itemset) {
            root.insert(itemset, 0);
        }

        // Subset operation to find all itemsets within a transaction
        public Set<List<String>> findSubsets(List<String> transaction, int k) {
            return root.findSubsets(transaction, 0, k);
        }

        private static class Node {
            boolean isLeaf;
            List<List<String>> itemsets;
            Map<Integer, Node> children;
            int level;
            int maxLeafSize;

            Node(boolean isLeaf, int maxLeafSize, int level) {
                this.isLeaf = isLeaf;
                this.maxLeafSize = maxLeafSize;
                this.level = level;
                this.itemsets = new ArrayList<>();
                this.children = new HashMap<>();
            }

            void insert(List<String> itemset, int k) {
                if (k >= itemset.size()) {
                    itemsets.add(itemset);
                    return;
                }
                if (isLeaf) {
                    itemsets.add(itemset);
                    if (itemsets.size() > maxLeafSize) {
                        split(k);
                    }
                } else {
                    int hash = itemset.get(k).hashCode() % modBase;
                    children.putIfAbsent(hash, new Node(true, maxLeafSize, level + 1));
                    children.get(hash).insert(itemset, k + 1);
                }
            }

            void split(int k) {
                isLeaf = false;
                List<List<String>> tempItemsets = new ArrayList<>(itemsets);
                itemsets.clear();
                for (List<String> itemset : tempItemsets) {
                    insert(itemset, k);
                }
            }

            Set<List<String>> findSubsets(List<String> transaction, int index, int k) {
                Set<List<String>> subsets = new HashSet<>();
                if (index >= transaction.size()) {
                    subsets.addAll(itemsets);
                    return subsets;
                }
                if (isLeaf) {
                    subsets.addAll(itemsets);
                } else {
                    for (int i = index; i < transaction.size()-k + level; i++) {
                        int hash = transaction.get(i).hashCode() % modBase;
                        Node child = children.get(hash);
                        if (child != null) {
                            subsets.addAll(child.findSubsets(transaction, index + 1, k));
                        }
                    }
                }
                return subsets;
            }
        }
    }
}


