import java.io.*;
import java.util.*;

public class A2_G4_t1 {
    static int n_clusters;

    public static void main(String[] args) throws IOException {
        String inputFilePath = args[0];

        if (args.length < 2) {
//            n_clusters = estimate_k();
            n_clusters = 15; // estimate_k 구현 전까지는 임의로 지정
        } else {
            n_clusters = Integer.parseInt(args[1]);
        }
        exec(inputFilePath, "k-means++");
    }

    private static void exec(String inputFilePath, String initFlag) throws IOException {
        List<Point> data = dataLoader(inputFilePath);

        Date start = new Date();

        KMeans kmeans = new KMeans();
        List<Cluster> result = kmeans.clustering(n_clusters, initFlag, data);

        Date end = new Date();
        long time = end.getTime() - start.getTime();
        printResult(result);
        System.out.println("Execution time is " + time + " milliseconds");
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
                        coordinates[i - 1] = Float.parseFloat(data[i]);
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
}

class Cluster {
    private int clusterId;
    private double[] centroid;
    private List<Point> points;

    public Cluster(int clusterId, double[] centroid) {
        this.clusterId = clusterId;
        this.centroid = centroid;
        this.points = new ArrayList<>();
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

        for (int i = 0; i < points.size(); i++) {
            for (int j = 0; j < newCentroid.length; j++) {
                newCentroid[j] = (newCentroid[j] + points.get(i).getCoordinates()[j]) * ((i+1)/(i+2));
            }
        }
        this.centroid = newCentroid;
        return newCentroid;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void addPoint(Point point) {
        points.add(point);
    }

    public void removePoint(Point point) {
        points.remove(point);
    }

    public void removePointById(String id) {
        points.removeIf(p -> p.getId().equals(id));
    }


}

class KMeans {
    private int k;
    private List<Point> points;
    private List<Cluster> clusters;
    private List<Point> dataset;

    public KMeans() {
        this.points = new ArrayList<>();
        this.clusters = new ArrayList<>();
    }

    public List<Cluster> clustering(int n_clusters, String initFlag, List<Point> data) { // main function
        this.k = n_clusters;
        this.dataset = data;

        initialization(initFlag);

        while (!isFinished()) {
            updateClusterAssignments();
            updateCentroids();
        }

        return clusters;
    }

    private double getDistance(Point p1, Point p2) { // 나중에 제곱을 l제곱으로 변경해서 generalize할 수 있음
        double sumSquaredDiffs = 0.0;
        for (int i = 0; i < p1.getCoordinates().length; i++) {
            double diff = p1.getCoordinates()[i] - p2.getCoordinates()[i];
            sumSquaredDiffs += diff * diff;
        }
        return sumSquaredDiffs;
    }

    private void initialization(String initFlag) {
        if (initFlag=="k-means++") {
            // 초기값을 k-means++에 따라 할당
            KMeansPlusPlus();
        } else if (initFlag=="random") {
            // 초기값을 랜덤으로 할당
        }
    }

    private void KMeansPlusPlus() {

    }

    private void updateClusterAssignments() {

    }

    private void updateCentroids() {
        for (int i = 0; i < clusters.size(); i++) {
            clusters.get(i).recalculateCentroid();
        }
    }

    private boolean isFinished(){
        return false;
    }

}