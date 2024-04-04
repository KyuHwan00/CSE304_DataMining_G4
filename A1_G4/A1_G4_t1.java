import java.io.*;
import java.util.*;

public class A1_G4_t1 {

    static Map<Integer, LinkedHashSet<String>> dataSet = new HashMap<>();
    static Map<LinkedHashSet<String>, Integer> result = new HashMap<>();
    static Map<Integer, LinkedHashSet<LinkedHashSet<String>>> dataSet_tid = new HashMap<>();
    static Float minSupport;
    static int maxLeafSize = 1000;

    public static void main(String[] args) throws IOException {
        String inputFile = args[0];
        minSupport = Float.parseFloat(args[1]);
        exec("apriori", inputFile);
    }

    private static void exec(String flag, String inputFile) throws IOException {
        Date start = new Date();
        Set<LinkedHashSet<String>> itemSets_1 = dataLoader(inputFile);
        Set<LinkedHashSet<String>> l1 = gernerate(itemSets_1);

        if (flag.equals("apriori")) {
            apriori(l1);
        } else {
            aprioriTid(l1);
        }
        
        Date end = new Date();
        long time = end.getTime() - start.getTime();
        printResultsInAscendingOrder(result);
        // System.out.println("Execution time is " + time + " milliseconds");
    }

    private static Set<LinkedHashSet<String>> dataLoader(String inputFile) throws IOException {
        int tid = 0;
        Set<LinkedHashSet<String>> itemSets_1 = new HashSet<>();

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
        return itemSets_1;
    }

    // apriori algorithm
    public static void apriori(Set<LinkedHashSet<String>> l1) {
        /*
         * l1: the first candidate set
         */

        Set<LinkedHashSet<String>> supportedItemset = l1;
        for(int k = 2; !supportedItemset.isEmpty(); k++) {
            // candidate generation
            Set<LinkedHashSet<String>> candidateSet = join(supportedItemset, k);
            Set<LinkedHashSet<String>> prunedcandidateSet = prune(candidateSet, supportedItemset, k-1);
            
            // subset
            Map<LinkedHashSet<String>, Integer> C_t = subset(prunedcandidateSet, dataSet.values(), k);
            
            // calculate the support of an itemset and update the supported itemset
            supportedItemset.clear();
            for (Map.Entry<LinkedHashSet<String>, Integer> entry : C_t.entrySet()) {
                float support = entry.getValue() / (float) dataSet.size();
                if (support >= minSupport) {
                    supportedItemset.add(entry.getKey());
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    // apriori algorithm with tid
    public static void aprioriTid(Set<LinkedHashSet<String>> l1) {
        /*
         * l1: the first candidate set
         */
        Set<LinkedHashSet<String>> supportedItemset = l1;

        Map<Integer, LinkedHashSet<LinkedHashSet<String>>> C_tid = dataSet_tid;

        for(int k = 2; !supportedItemset.isEmpty(); k++) {
            // candidate generation
            Set<LinkedHashSet<String>> candidateSet = join(supportedItemset, k);
            Set<LinkedHashSet<String>> prunedcandidateSet = prune(candidateSet, supportedItemset, k-1);
            
            // tid operation
            Map<Integer, LinkedHashSet<LinkedHashSet<String>>> C_newtid = new HashMap<>();
            Map<LinkedHashSet<String>, Integer> C_k = new HashMap<>();
            tidHelper(C_tid, C_k, C_newtid, prunedcandidateSet, k);


            // calculate the support of an itemset and update the supported itemset
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

    private static void tidHelper(Map<Integer, LinkedHashSet<LinkedHashSet<String>>> C_tid, Map<LinkedHashSet<String>, Integer> C_k, Map<Integer, LinkedHashSet<LinkedHashSet<String>>> C_newtid, Set<LinkedHashSet<String>> prunedcandidateSet, int k) {
        /*
         * C_tid: the candidate set with tid
         * C_k: for calculating the support of an itemset
         * C_newtid: the new candidate set with tid
         * prunedcandidateSet: the pruned candidate set
         * k: the number of items in the candidate set
         */

        for (Integer tid : C_tid.keySet()) {
            LinkedHashSet<LinkedHashSet<String>> C_new = new LinkedHashSet<>();
            LinkedHashSet<LinkedHashSet<String>> entry = C_tid.get(tid);

            for (LinkedHashSet<String> item : prunedcandidateSet) {
                List<String> removed_k = new ArrayList<>(item);
                List<String> removed_k_1 = new ArrayList<>(item);
                removed_k.remove(k-1);
                removed_k_1.remove(k-2);
                boolean indicator_k = false;
                boolean indicator_k_1 = false;
                // check if the k-1 subset and k-2 subset are in the transaction
                for (LinkedHashSet<String> comb : entry) {
                    if (comb.containsAll(removed_k)) {
                        indicator_k = true;
                    } else if (comb.containsAll(removed_k_1)) {
                        indicator_k_1 = true;
                    }

                    // if both k-1 subset and k-2 subset are in the transaction, add the item to the new candidate set
                    if (indicator_k && indicator_k_1) {
                        C_new.add(item);
                        C_k.put(item, C_k.getOrDefault(item, 0) + 1);
                        break;
                    }
                }
            }
            // update the new candidate tidset
            if (!C_new.isEmpty()) {
                C_newtid.putIfAbsent(tid, C_new);
            }
        }
        // update the candidate set with tid
        C_tid = C_newtid;
    }

    public static Set<LinkedHashSet<String>> gernerate(Set<LinkedHashSet<String>> itemset) {
        // generate the candidate set
        Set<LinkedHashSet<String>> map = new HashSet<>();
        for (LinkedHashSet<String> comb : itemset) {
            Boolean islarge = checkSupport(comb);
            if (islarge) {
                map.add(comb);
            }
        }
        return map;
    }

    public static Boolean checkSupport(LinkedHashSet<String> itemset) {
        // check the support of an itemset
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
    
    public static Map<LinkedHashSet<String>, Integer> subset(Set<LinkedHashSet<String>> C_k, Collection<LinkedHashSet<String>> transactions, int k) {
        /*
         * C_k: the candidate set
         * transactions: the transaction set
         * k: the number of items in the candidate set
         */

        Map<LinkedHashSet<String>, Integer> C_t = new HashMap<>();

        // subset with simple subset operation

        // for (LinkedHashSet<String> item : C_k) {
        //     for (LinkedHashSet<String> transaction : transactions) {
        //         if (transaction.containsAll(item)) {
        //             C_t.put(item, C_t.getOrDefault(item, 0) + 1);
        //         }
        //     }
        // }

        // subset with hash tree
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
        return C_t;
    }

    public static Set<LinkedHashSet<String>> join(Set<LinkedHashSet<String>> itemSet, int k) {
        Set<LinkedHashSet<String>> resultSet = new HashSet<>();
        for (LinkedHashSet<String> i : itemSet) {
            for (LinkedHashSet<String> j : itemSet) {
                if (i.equals(j)) {
                    continue;
                }
                LinkedHashSet<String> union = new LinkedHashSet<>();
                Iterator<String> iIterator = i.iterator();
                Iterator<String> jIterator = j.iterator();
                // check if the first k-2 items are the same
                for (int z = 0; z < k - 2; z++) {
                    String iNext = iIterator.next();
                    String jNext = jIterator.next();
                    if (!iNext.equals(jNext)) {
                        break;
                    }
                    
                    union.add(iNext);
                }
                // add the last two items in lexographical order
                String iNext = iIterator.next();
                String jNext = jIterator.next();
                if (iNext.compareTo(jNext) < 0) {
                    union.add(iNext);
                    union.add(jNext);
                } else {
                    union.add(jNext);
                    union.add(iNext);
                }
                if (union.size() == k) {
                    resultSet.add(union);
                }
            }
        }
        return resultSet;
    }

    public static Set<LinkedHashSet<String>> prune(Set<LinkedHashSet<String>> candidateSet, Set<LinkedHashSet<String>> prevFreqSet, int k_1) {
        /*
         * candidateSet: the candidate set
         * prevFreqSet: the previous frequent set
         * k_1: the number of items in the previous frequent set
         */
        Set<LinkedHashSet<String>> prunedSet = new HashSet<>(candidateSet);
        for (LinkedHashSet<String> item : candidateSet) {
            Set<LinkedHashSet<String>> subsets = pruneSub(item, k_1);
            for (Set<String> subset : subsets) {
                if (!prevFreqSet.contains(subset)) {
                    prunedSet.remove(item);
                    break;
                }
            }
        }
        return prunedSet;
    }

    private static Set<LinkedHashSet<String>> pruneSub(LinkedHashSet<String> set, int k_1) {
        Set<LinkedHashSet<String>> result = new HashSet<>();
        pruneSubHelper(set, k_1, new LinkedHashSet<>(), result);
        return result;
    }

    private static void pruneSubHelper(LinkedHashSet<String> set, int k_1, LinkedHashSet<String> current, Set<LinkedHashSet<String>> result) {
        if (current.size() == k_1) {
            result.add(new LinkedHashSet<>(current));
            return;
        }
        LinkedHashSet<String> remaining = new LinkedHashSet<>(set);
        for (String s : set) {
            current.add(s);
            remaining.remove(s);
            pruneSubHelper(remaining, k_1, current, result);
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
}

//HashTree
class HashTree {
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
}

class Node {
    boolean isLeaf;
    List<List<String>> itemsets;
    Map<Integer, Node> children;
    int level;
    int maxLeafSize;
    int modBase;

    Node(boolean isLeaf, int maxLeafSize, int level) {
        this.isLeaf = isLeaf;
        this.maxLeafSize = maxLeafSize;
        this.level = level;
        this.itemsets = new ArrayList<>();
        this.children = new HashMap<>();
        this.modBase = 101;
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
        // Subset operation to find all itemsets within a transaction

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


