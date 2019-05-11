import java.io.*;
import java.util.Random;

class TrainingData {
    private String path;
    private String[] files;
    private double maxDist;
    private double trainRatio;
    private double testRatio;
    private String dataType;

    TrainingData(String path, String[] files, double maxDist, double sampleRatio, String dataType) {
        this.path = path;
        this.files = files;
        this.maxDist = maxDist;
        this.trainRatio = sampleRatio;
        this.testRatio = sampleRatio / 5;
        this.dataType = dataType;
    }
    void generateSiameseSamples() throws IOException {
        Random random = new Random();
        BufferedReader p1Reader, p2Reader, thresholdReader;
        BufferedWriter trainWriter = new BufferedWriter(new FileWriter(new File(path + "/trainingData.csv")));
        BufferedWriter testWriter = new BufferedWriter(new FileWriter(new File(path + "/validationData.csv")));
        //format: P1, P2, distance, kNN distance of P1
        trainWriter.write("P1,P2,distance,cutoff\n");
        testWriter.write("P1,P2,distance,cutoff\n");
        String[] lines = new String[2];
        Object[][] points = new Object[2][];

        //iterates each file
        for(int i = 0; i < files.length; i++) {
            p1Reader = new BufferedReader(new FileReader(new File(path + "/" + files[i])));
            thresholdReader = new BufferedReader(new FileReader(new File(path + "/threshold-" + files[i])));
            String threshold;
            int marker = 0;
            while((lines[0] = p1Reader.readLine()) != null &&
                    (threshold = thresholdReader.readLine()) != null) {
                marker++;
                if(marker % 100 == 0)
                    System.out.println(String.valueOf(marker) + " siamese samples generated");
                if(random.nextDouble() < 0.8) continue;
                points[0] = Utils.getValuesFromLine(lines[0], " ", dataType);
                double thres = Double.parseDouble(threshold);
                for(int j = i; j < files.length; j++) {
                    p2Reader = new BufferedReader(new FileReader(new File(path + "/" + files[j])));
                    while((lines[1] = p2Reader.readLine()) != null) {
                        points[1] = Utils.getValuesFromLine(lines[1], " ", dataType);
                        //double sim = Utils.computeSimilarity(points[0], points[1], maxDist, "Euclidean", "staircase");
                        double dist = Utils.computeEuclideanDist(points[0], points[1]);
                        if(random.nextDouble() < trainRatio || dist < thres)
                            trainWriter.write(lines[0] + "," + lines[1] + "," + String.valueOf(dist) + "," + threshold + "\n");
                        if(random.nextDouble() < testRatio || dist < thres)
                            testWriter.write(lines[0] + "," + lines[1] + "," + String.valueOf(dist) + "," + threshold + "\n");
                    }
                    p2Reader.close();
                }
            }
            p1Reader.close();
        }
        trainWriter.flush(); trainWriter.close();
        testWriter.flush(); testWriter.close();
    }

    void generateTripletSamples() throws IOException {
        Random random = new Random();
        BufferedReader anchorReader, positiveReader, negativeReader;
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path + "/TripletData.csv")));
        writer.write("anchor,positive,negative,diff\n");
        String[] lines = new String[3];//anchor, positive, negative
        Object[][] points = new Object[3][];
        //iterates each file
        for(int i = 0; i < files.length; i++) {
            anchorReader = new BufferedReader(new FileReader(new File(path + "/" + files[i])));
            int marker = 0;
            while((lines[0] = anchorReader.readLine()) != null) {
                marker++;
                if(marker % 100 == 0)
                    System.out.println(String.valueOf(marker) + " triplet samples generated");
                points[0] = Utils.getValuesFromLine(lines[0], " ", dataType);
                positiveReader = new BufferedReader(new FileReader(new File(path + "/" + files[i])));
                while((lines[1] = positiveReader.readLine()) != null) {
                    if(random.nextDouble() > Math.sqrt(trainRatio))
                        continue;
                    points[1] = Utils.getValuesFromLine(lines[1], " ", dataType);
                    for(int j = i + 1; j < files.length; j++) {

                        negativeReader = new BufferedReader(new FileReader(new File(path + "/" + files[j])));
                        while((lines[2] = negativeReader.readLine()) != null) {
                            if(random.nextDouble() > Math.sqrt(trainRatio))
                                continue;
                            points[2] = Utils.getValuesFromLine(lines[2], " ", dataType);
                            //double simAP = Utils.computeSimilarity(points[0], points[1], maxDist, "Euclidean", "staircase");
                            //double simAN = Utils.computeSimilarity(points[0], points[2], maxDist, "Euclidean", "staircase");
                            double simAP = Utils.computeEuclideanDist(points[0], points[1]);
                            double simAN = Utils.computeEuclideanDist(points[0], points[2]);
                            if(simAP < simAN) continue;
                            writer.write(lines[0] + "," + lines[1] + "," + lines[2] + "," + String.valueOf(simAP - simAN) + "\n");
                        }
                        negativeReader.close();
                    }
                }
                positiveReader.close();
            }
            anchorReader.close();
        }
        writer.flush();
        writer.close();
    }
}