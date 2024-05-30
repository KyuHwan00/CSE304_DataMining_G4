import java.io.*;
import java.util.*;

public class A2_G4_t1 {
    static int n_clusters;

    public static void main(String[] args) throws IOException {
        String inputFilePath = args[0];

        if (args.length < 2) {
//            n_clusters = estimate_k();
            n_clusters = 15; // estimate_k 구현 전까지는 임의로 지정
            System.out.println("estimated k: " + n_clusters);
        } else {
            n_clusters = Integer.parseInt(args[1]);
        }
        exec(inputFilePath, "k-means++");
    }

    private static void exec(String inputFilePath, String initFlag) throws IOException {
        List<Point> dataset = dataLoader(inputFilePath);

        Date start = new Date();

        KMeans kmeans = new KMeans();
        List<Cluster> result = kmeans.clustering(n_clusters, initFlag, dataset);

        Date end = new Date();
        long time = end.getTime() - start.getTime();
        printResult(result);
        // System.out.println("Execution time is " + time + " milliseconds");
    }

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

    public static int estimate_k(){
        int k_proper;

        k_proper = 0;

        return k_proper;
    }

    public static void printResult(List<Cluster> result) {
        result.sort(Comparator.comparing(Cluster::getClusterId));
        int clusterIndex = 0;
        for (Cluster cluster : result) {
            String output = "Cluster #" + ++clusterIndex + " => ";
            output += cluster.toString();
            System.out.println(output);
        }
    }
}

class Point {
    private String id;
    private double[] coordinates;
    private int clusterId;

    public Point(String id, double[] coordinates) {
        this.id = id;
        this.coordinates = coordinates;
        this.clusterId = -1; // 초기 클러스터 ID 설정, -1은 할당되지 않음을 의미
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
    private int clusterId;
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

    public double[] setCentroid(double[] centroid) {
        this.centroid = centroid;
        return centroid;
    }

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
        clusterPoints.add(point);
    }

    public void removePoint(Point point) {
        clusterPoints.remove(point);
    }

    public void removePointById(String id) {
        clusterPoints.removeIf(p -> p.getId().equals(id));
    }

    public String toString() {
        String output = "";
        clusterPoints.sort(Comparator.comparing(Point::getNumberId));

        for (Point point : clusterPoints) {
            output += point.getId() + " ";
        }
        return output;
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

        initialization(initFlag, k);

        while (!isFinished()) {
            updateClusterAssignments();
            updateCentroids();
        }

        return clusters;
    }

    private double getDistanceOfPoints(Point p1, Point p2) { // 나중에 제곱을 l제곱으로 변경해서 generalize할 수 있음
        double sumSquaredDiffs = 0.0;
        for (int i = 0; i < p1.getCoordinates().length; i++) {
            double diff = p1.getCoordinates()[i] - p2.getCoordinates()[i];
            sumSquaredDiffs += diff * diff;
        }
        return sumSquaredDiffs;
    }

    private double getDistanceOfCoordinates(double[] p1, double[] p2) { // 나중에 제곱을 l제곱으로 변경해서 generalize할 수 있음
        double sumSquaredDiffs = 0.0;
        for (int i = 0; i < p1.length; i++) {
            double diff = p1[i] - p2[i];
            sumSquaredDiffs += diff * diff;
        }
        return sumSquaredDiffs;
    }

    private void initialization(String initFlag, int n_clusters) {
        int seed = 12345;
        if (initFlag.equals("k-means++")) {
            KMeansPlusPlus(n_clusters, seed);
        } else if (initFlag.equals("random")) {
            Random random = new Random(seed);
            HashSet<Integer> chosenIndices = new HashSet<>();
            List<double[]> centroids = new ArrayList<>();

            clusters = new ArrayList<>();
            int clusterIdIndex = 0;

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
            double[] distances = new double[points.size()]; // points에 맞게 distances 저장
            double totalDist = 0;

            for (int i = 0; i < points.size(); i++) {
                double shortestDist = Double.MAX_VALUE;
                for (double[] centroid : centroids) {
                    double dist = getDistanceOfCoordinates(points.get(i).getCoordinates(), centroid);
                    shortestDist = Math.min(shortestDist, dist);
                }
                distances[i] = shortestDist;
                totalDist += shortestDist;
            }

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

    private void updateClusterAssignments() {
        for (Point point : points) {
            double closestDist = Double.MAX_VALUE;
            if (point.getClusterId() != -1) {
                closestDist = getDistanceOfCoordinates(point.getCoordinates(), clusters.get(point.getClusterId()).getCentroid());
            }

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

    private List<double[]> updateCentroids() {
        previousCentroids = new ArrayList<>(currentCentroids);
        List<double[]> centroids = new ArrayList<>();

        for (int i = 0; i < clusters.size(); i++) {
            centroids.add(clusters.get(i).recalculateCentroid());
        }

        currentCentroids = centroids;
        return centroids;
    }

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