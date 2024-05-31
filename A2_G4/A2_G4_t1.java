import java.io.*;
import java.util.*;

public class A2_G4_t1 {
    static int n_clusters;

    // select k-means++ or random(k-means)
    static String kMeansInitFlag = "k-means++";
//    static String kMeansInitFlag = "random";
    public static void main(String[] args) throws IOException {
        String inputFilePath = args[0];

        // If the number of clusters is not provided, estimate k
        if (args.length < 2) {
            n_clusters = estimateK(dataLoader(inputFilePath));
            System.out.println("estimated k: " + n_clusters);
        } else {
            n_clusters = Integer.parseInt(args[1]);
        }
        exec(inputFilePath, kMeansInitFlag);
    }

    // Execute the k-means or k-means++ clustering according to initFlag
    private static void exec(String inputFilePath, String initFlag) throws IOException {
        List<Point> dataset = dataLoader(inputFilePath);

        Date start = new Date();

        KMeans kmeans = new KMeans();
        List<Cluster> result = kmeans.clustering(n_clusters, initFlag, dataset);

        Date end = new Date();
        long time = end.getTime() - start.getTime();
//        writeClustersToCSV(result, "./output/output.csv");
        printResult(result);
//         System.out.println(calculatePhi(result));
//         System.out.println("Execution time is " + time + " milliseconds");
    }

    // Load data points from a CSV file
    public static List<Point> dataLoader(String filePath) {
        List<Point> points = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 2) { // At least 1 ID, and 1 coordinate.
                    String id = data[0];
                    double[] coordinates = new double[data.length - 2];
                    for (int i = 1; i < data.length - 1; i++) {
                        coordinates[i - 1] = Double.parseDouble(data[i]);
                    }
                    Point point = new Point(id, coordinates);
                    points.add(point);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error reading file or parsing: " + e.getMessage());
        }
        return points;
    }

    // Print the clustering result
    public static void printResult(List<Cluster> result) {
        result.sort(Comparator.comparing(Cluster::getClusterId));
        int clusterIndex = 0;
        for (Cluster cluster : result) {
            String output = "Cluster #" + ++clusterIndex + " => ";
            output += cluster.toString();
            System.out.println(output);
        }
    }

//    public static double calculatePhi(List<Cluster> result) {
//        double phi = 0.0;
//        KMeans kmeans = new KMeans();
//
//        for (Cluster cluster : result) {
//            for (Point p : cluster.getPoints()) {
//                phi += kmeans.getDistanceOfCoordinates(p.getCoordinates(), cluster.getCentroid());
//            }
//        }
//        return phi;
//    }
//
//    public static void writeClustersToCSV(List<Cluster> clusters, String filePath) {
//        try {
//            File file = new File(filePath);
//            file.getParentFile().mkdirs(); // Create the directory if it does not exist
//            file.createNewFile(); // Create the file if it does not exist
//
//            try (PrintWriter writer = new PrintWriter(file)) {
//                // Write the header
//                writer.println("point_id,x_coordinate,y_coordinate,cluster_id");
//
//                // Write the data
//                for (Cluster cluster : clusters) {
//                    for (Point point : cluster.getPoints()) {
//                        double[] coordinates = point.getCoordinates();
//                        writer.printf("%s,%f,%f,%d\n", point.getId(), coordinates[0], coordinates[1], cluster.getClusterId());
//                    }
//                }
//            }
//        } catch (IOException e) {
//            System.err.println("Error writing to file: " + e.getMessage());
//        }
//    }

    // Estimate the number of clusters using the BIC method
    public static int estimateK(List<Point> dataset) {
        int maxK = (int) Math.sqrt(dataset.size()/2);
        KMeans kmeans = new KMeans();

        double bestBICScore = Double.POSITIVE_INFINITY;
        double currentBICScore;

        int k = 1;
        int bestK = 1;

        while (k < maxK) {
            for (Point point : dataset) {
                point.setClusterId(-1);
            }
            List<Cluster> clusters = kmeans.clustering(k, kMeansInitFlag, dataset);

            currentBICScore = calculateBIC(clusters, k);

//            System.out.println("k: " + k + ", BIC Score: " + currentBICScore + ", Best BIC Score: " + bestBICScore);
            if (currentBICScore < bestBICScore) {
                bestK = k;
                bestBICScore = currentBICScore;
            } else if (Double.isInfinite(currentBICScore) || Double.isNaN(currentBICScore)) {
                break;
            }
            k++;
        }

        return bestK;
    }

    // Calculate the BIC score for the given clusters
    public static double calculateBIC(List<Cluster> clusters, int k) {
        double logLikelihood = 0;
        int R = 0;

        for (Cluster cluster : clusters) {
            R += cluster.getPoints().size();
        }

        int numParameters = k - 1 + k * clusters.get(0).getCentroid().length + 1;

        for (Cluster cluster : clusters) {
            int M = cluster.getCentroid().length;
            int R_n = cluster.getPoints().size();

            logLikelihood -= 0.5 * R_n * Math.log(2 * Math.PI);
            logLikelihood -= 0.5 * R_n * M * Math.log(cluster.getVariance(k));
            logLikelihood -= 0.5 * (R_n - k);
            logLikelihood += R_n * Math.log(R_n);
            logLikelihood -= R_n * Math.log(R);
        }

        // Calculate BIC
        double bic = - 2 * logLikelihood + numParameters * Math.log(R);
        return bic;
    }

    //silhouette
//    public static int estimateK(List<Point> dataset) {
//        int maxK = (int) Math.sqrt(dataset.size() / 2);
//        KMeans kmeans = new KMeans();
//
//        int bestK = 1;
//        double bestSilhouetteScore = -1;
//
//        int k = 2;
//        while (k < maxK) {
//            for (Point point : dataset) {
//                point.setClusterId(-1);
//            }
//
//            List<Cluster> clusters = kmeans.clustering(k, kMeansInitFlag, dataset);
//            //printResult(clusters);
//
//            // Compute silhouette score
//            double silhouetteScore = calculateSilhouetteScore(clusters, dataset);
//
//            System.out.println("k: " + k + ", Silhouette Score: " + silhouetteScore + ", Best Silhouette Score: " + bestSilhouetteScore);
//            if (silhouetteScore > bestSilhouetteScore) {
//                bestK = k;
//                bestSilhouetteScore = silhouetteScore;
//            }
//            k++;
//        }
//        return bestK;
//    }
//
//    private static double calculateSilhouetteScore(List<Cluster> clusters, List<Point> dataset) {
//        double totalSilhouetteScore = 0.0;
//        int totalPoints = dataset.size();
//
//        for (Point point : dataset) {
//            Cluster currentCluster = null;
//            for (Cluster c : clusters) {
//                if (c.getPoints().contains(point)) {
//                    currentCluster = c;
//                    break;
//                }
//            }
//            double a = averageDistanceToCluster(point, currentCluster);
//            double b = clusters.stream().filter(c -> !c.getPoints().contains(point))
//                    .mapToDouble(c -> averageDistanceToCluster(point, c))
//                    .min().orElse(Double.MAX_VALUE);
//
//            double silhouette = (b - a) / Math.max(a, b);
//            totalSilhouetteScore += silhouette;
//        }
//
//        return totalSilhouetteScore / totalPoints;
//    }
//
//    private static double averageDistanceToCluster(Point point, Cluster cluster) {
//        return cluster.getPoints().stream()
//                .mapToDouble(p -> distance(point, p))
//                .average().orElse(Double.MAX_VALUE);
//    }
//
//    private static double distance(Point p1, Point p2) {
//        double[] p1Coordinates = p1.getCoordinates();
//        double[] p2Coordinates = p2.getCoordinates();
//        double sum = 0.0;
//        for (int i = 0; i < p1Coordinates.length; i++) {
//            sum += Math.pow(p1Coordinates[i] - p2Coordinates[i], 2);
//        }
//        return Math.sqrt(sum);
//    }
}

class Point {
    private final String id;
    private final double[] coordinates;
    private int clusterId;

    public Point(String id, double[] coordinates) {
        this.id = id;
        this.coordinates = coordinates;
        this.clusterId = -1; // Initial cluster ID set, -1 indicating unassigned
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public int getClusterId() {
        return clusterId;
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    public String getId() {
        return id;
    }

    public int getNumberId() {
        String numberId = getId().replaceAll("[^\\d]", "");
        return Integer.parseInt(numberId);
    }
}

class Cluster {
    private final int clusterId;
    private double[] centroid;
    private List<Point> clusterPoints;

    public Cluster(int clusterId, double[] centroid) {
        this.clusterId = clusterId;
        this.centroid = centroid;
        this.clusterPoints = new ArrayList<>();
    }

    public int getClusterId() {
        return clusterId;
    }

    public double[] getCentroid() {
        return centroid;
    }

    public void setCentroid(double[] centroid) {
        this.centroid = centroid;
    }

    // Recalculate and return the new centroid of the cluster
    public double[] recalculateCentroid() {
        double[] newCentroid = new double[centroid.length];

        for (int i = 0; i < newCentroid.length; i++) {
            for (Point point : getPoints()) {
                newCentroid[i] = newCentroid[i] + point.getCoordinates()[i];
            }
            newCentroid[i] /= getPoints().size();
        }
        setCentroid(newCentroid);
        return newCentroid;
    }

    public List<Point> getPoints() {
        return this.clusterPoints;
    }

    public void addPoint(Point point) {
        this.clusterPoints.add(point);
    }

    public void removePoint(Point point) {
        this.clusterPoints.remove(point);
    }

    public void removePointById(String id) {
        this.clusterPoints.removeIf(p -> p.getId().equals(id));
    }

    public String toString() {
        String output = "";
        clusterPoints.sort(Comparator.comparing(Point::getNumberId));

        for (Point point : clusterPoints) output += point.getId() + " ";
        return output;
    }

    // Calculate and return the variance of the cluster
    public double getVariance(int k) {
        int dimension = centroid.length;
        int numPoints = clusterPoints.size();
        double variance = 0.0;
        for (Point p : clusterPoints) {
            double[] coords = p.getCoordinates();
            for (int i = 0; i < dimension; i++) {
                variance += Math.pow(coords[i] - centroid[i], 2);
            }
        }

        if (numPoints <= k) variance /= 1;
        else variance /= numPoints- k;
        return variance;
    }
}

class KMeans {
    private int k;
    private List<Point> points;
    private List<Cluster> clusters;
    private List<double[]> previousCentroids;
    private List<double[]> currentCentroids;
    private static final double EPSILON = 1e-9;

    public KMeans() {
        this.points = new ArrayList<>();
        this.clusters = new ArrayList<>();
        this.previousCentroids = new ArrayList<>();
        this.currentCentroids = new ArrayList<>();
    }

    public List<Cluster> clustering(int n_clusters, String initFlag, List<Point> dataset) { // main function
        this.k = n_clusters;
        this.points = dataset;

        initialization(initFlag, k); // Initialize centroids

        clusteringHelper(); // Perform the clustering process

        return clusters;
    }

    public void clusteringHelper() {
        // Continue updating clusters until centroids converge
        while (!isFinished()) {
            updateClusterAssignments();
            updateCentroids();
        }
    }

    public double getDistanceOfCoordinates(double[] p1, double[] p2) { // 나중에 제곱을 l제곱으로 변경해서 generalize할 수 있음
        // Calculates the squared Euclidean distance between two points.
        double sumSquaredDiffs = 0.0;
        for (int i = 0; i < p1.length; i++) {
            double diff = p1[i] - p2[i];
            sumSquaredDiffs += diff * diff;
        }
        return sumSquaredDiffs;
    }

    // Initializes the centroids for the clusters.
    private void initialization(String initFlag, int n_clusters) {
        int seed = 304;
        this.clusters = new ArrayList<>();
        this.previousCentroids = new ArrayList<>();
        this.currentCentroids = new ArrayList<>();

        if (initFlag.equals("k-means++")) {
            KMeansPlusPlus(n_clusters, seed);
        } else if (initFlag.equals("random")) {
            Random random = new Random(seed);
            HashSet<Integer> chosenIndices = new HashSet<>();
            List<double[]> centroids = new ArrayList<>();

            clusters = new ArrayList<>();
            int clusterIdIndex = 0;

            // Randomly select initial centroids
            while (clusters.size() < n_clusters) {
                int randomPointIndex = random.nextInt(points.size());

                if (chosenIndices.add(randomPointIndex)) {
                    centroids.add(points.get(randomPointIndex).getCoordinates());
                    clusters.add(new Cluster(clusterIdIndex, points.get(randomPointIndex).getCoordinates()));
                    clusterIdIndex++;
                }
            }
            currentCentroids = centroids;
        } else {
            throw new IllegalArgumentException("Invalid initialization flag: " + initFlag);
        }
    }

    private void KMeansPlusPlus(int n_clusters, int randomSeed) {
        clusters = new ArrayList<>();
        List<double[]> centroids = new ArrayList<>();
        int clusterIdIndex = 0;

        Random random = new Random(randomSeed);
        // First cluster(clusterIndex=0) is randomly selected.
        double[] initialCentroid = points.get(random.nextInt(points.size())).getCoordinates();
        centroids.add(initialCentroid);
        clusters.add(new Cluster(clusterIdIndex, initialCentroid));
        clusterIdIndex++;

        // Pick other centroids with cluster.
        while (clusterIdIndex < n_clusters) {
            double[] distances = new double[points.size()];
            double totalDist = 0;

            // Calculate distance of each point from the nearest centroid
            for (int i = 0; i < points.size(); i++) {
                double shortestDist = Double.MAX_VALUE;
                for (double[] centroid : centroids) {
                    double dist = getDistanceOfCoordinates(points.get(i).getCoordinates(), centroid);
                    shortestDist = Math.min(shortestDist, dist);
                }
                distances[i] = shortestDist;
                totalDist += shortestDist;
            }

            // Select new centroid based on distance
            double randomValue = random.nextDouble() * totalDist;
            for (int i = 0; i < distances.length; i++) {
                randomValue -= distances[i];
                if (randomValue <= 0) {
                    centroids.add(points.get(i).getCoordinates());
                    clusters.add(new Cluster(clusterIdIndex, points.get(i).getCoordinates()));
                    clusterIdIndex++;
                    break;
                }
            }
        }
        currentCentroids = centroids;
    }

    // Assigns points to the nearest cluster based on current centroids.
    private void updateClusterAssignments() {
        for (Point point : points) {
            double closestDist = Double.MAX_VALUE;
            if (point.getClusterId() != -1) {
                closestDist = getDistanceOfCoordinates(point.getCoordinates(), clusters.get(point.getClusterId()).getCentroid());
            }

            // // Find the nearest centroid and assign the point to that cluster
            for (Cluster cluster : clusters) {
                double dist = getDistanceOfCoordinates(point.getCoordinates(), cluster.getCentroid());

                if (closestDist > dist) {
                    // Find the closest cluster
                    closestDist = dist;

                    if (point.getClusterId() != -1) {
                        clusters.get(point.getClusterId()).removePoint(point);
                    }
                    cluster.addPoint(point);
                    point.setClusterId(cluster.getClusterId());
                }
            }
        }
    }

    // Updates the centroids of the clusters based on current assignments.
    private void updateCentroids() {
        previousCentroids = new ArrayList<>(currentCentroids);
        List<double[]> centroids = new ArrayList<>();

        // Recalculate centroid for each cluster
        for (int i = 0; i < clusters.size(); i++) {
            centroids.add(clusters.get(i).recalculateCentroid());
        }

        currentCentroids = centroids;
    }

    // Checks if the centroids have converged (no significant changes).
    private boolean isFinished(){
        if (previousCentroids == null || currentCentroids == null || previousCentroids.size() != currentCentroids.size()) {
            return false;
        }
        for (int i = 0; i < previousCentroids.size(); i++) {
            double[] prevSubList = previousCentroids.get(i);
            double[] currSubList = currentCentroids.get(i);

            if (prevSubList == null || currSubList == null || prevSubList.length != currSubList.length) {
                System.out.println("False: "+prevSubList.toString() + currSubList.toString());
                return false;
            }

            for (int j = 0; j < prevSubList.length; j++) {
                if (Math.abs(prevSubList[j] - currSubList[j]) >= EPSILON) {
                    return false;
                }
            }
        }
        return true;
    }
}