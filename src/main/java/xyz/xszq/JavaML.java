package xyz.xszq;
import net.sf.javaml.classification.Classifier;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.*;

@SuppressWarnings("SameParameterValue")
public class JavaML {
    private static final int imgWidth = 800;
    private static final int imgHeight = 600;
    private static final int fontSize = 20;
    private static final String fontName = "Consolas";
    private static final int xParam = 1;
    private static final int yParam = 4;
    private static final Map<Integer, Paint> colors = Map.of(0, Color.yellow, 2, Color.pink, 102, Color.blue);

    /**
     * Entry point of the JavaML project.
     */
    public static void main(String[] args) throws IOException {
        /* Read datasets */
        Dataset data = SimulatedData.readFolder("data/2", 5, 5);
        Dataset dataForClassification = SimulatedData.readFolder("data/2", 1, 16);

        /* Train classifier */
        Scanner sc = new Scanner(System.in);
        System.out.println("Please input the target classifier name:");
        String classifierName = sc.next();
        Classifier classifier = ClassifierFactory.creator(classifierName);
        System.out.printf("Result of %s:\n", classifierName);
        long startTimestamp = System.currentTimeMillis();
        classifier.buildClassifier(data);
        long endTimestamp = System.currentTimeMillis();
        System.out.printf("Training time elapsed: %d ms\n", endTimestamp - startTimestamp);

        /* Verify */
        ArrayList<Instance> correct = new ArrayList<>();
        ArrayList<Instance> wrong = new ArrayList<>();
        startTimestamp = System.currentTimeMillis();
        for (Instance inst : dataForClassification) {
            Object predictedClassValue = classifier.classify(inst);
            Object realClassValue = inst.classValue();
            if (predictedClassValue.equals(realClassValue))
                correct.add(inst);
            else
                wrong.add(inst);
        }
        endTimestamp = System.currentTimeMillis();
        System.out.printf("Inference time elapsed: %d ms\n", endTimestamp - startTimestamp);
        System.out.printf("Correct predictions: %.2f%% (%d/%d)\n",
                100.0 * correct.size() / dataForClassification.size(),
                correct.size(), dataForClassification.size());
        System.out.printf("Wrong predictions: %.2f%% (%d/%d)\n",
                100.0 * wrong.size() / dataForClassification.size(),
                wrong.size(), dataForClassification.size());

        /* Visualize part */
        visualize(data, dataForClassification, correct, wrong);
    }

    /**
     * Show the image in a popup window.
     * @param img the BufferedImage to show.
     */
    public static void showImage(BufferedImage img) {
        JLabel ic = new JLabel(new ImageIcon(img));
        JScrollPane scroller = new JScrollPane(ic);
        JDialog popup = new JDialog();
        popup.getContentPane().setLayout(new FlowLayout());
        popup.getContentPane().add(scroller);
        popup.getContentPane().validate();
        popup.setModal(true);
        popup.pack();
        popup.setVisible(true);
    }

    /**
     * Random select Instances from the list.
     * @param before target List&lt;Instance&gt;
     * @param num size of the result list
     * @return List
     */
    private static List<Instance> randomSelect(List<Instance> before, int num) {
        Random rand = new Random();
        List<Instance> result = new ArrayList<>();
        for (int i = 0; i < num; i ++) {
            int index = rand.nextInt(before.size());
            result.add(before.get(index));
            before.remove(index);
        }
        return result;
    }

    /**
     * Get data range of every params. This is useful in normalizing points
     * @param paramNums how many params are there in datasets
     * @param datasets datasets to handle
     * @return params range
     */
    private static double[][] getDataRange(int paramNums, Dataset... datasets) {
        double[][] range = new double[paramNums][2];
        for (int i = 0; i < paramNums; i++) {
            range[i][0] = 1e100;
            range[i][1] = 1e-100;
        }
        for (Dataset data: datasets) {
            for (Instance inst : data) {
                for (int i = 0; i < paramNums; i ++) {
                    double value = inst.get(i);
                    if (value < range[i][0])
                        range[i][0] = value;
                    if (value > range[i][1])
                        range[i][1] = value;
                }
            }
        }
        return range;
    }

    /**
     * Draw points from the dataset with the process of normalizing
     * @param g2d Graphics2D of the BufferedImage
     * @param range params range
     * @param data dataset
     * @param color point color, where null means choose color from the <pre>colors</pre> table by classValue
     */
    private static void drawPoints(Graphics2D g2d, double[][] range, List<Instance> data, Paint color) {
        for (Instance inst : data) {
            if (color == null)
                g2d.setPaint(colors.get((int) inst.classValue()));
            else
                g2d.setPaint(color);
            int x = (int) ((inst.get(xParam) - range[xParam][0]) * imgWidth * 0.83 /
                    (range[xParam][1] - range[xParam][0])) + 60;
            int y = (int) ((inst.get(yParam) - range[yParam][0]) * imgHeight * 0.83 /
                    (range[yParam][1] - range[yParam][0])) + 40;
            g2d.fillOval(x, y, 8, 8);
        }
    }

    /**
     * Draw legend part with given info
     * @param g2d Graphics2D of the BufferedImage
     * @param name name of the type
     * @param color the color representing the type
     * @param x x axis
     * @param y y axis
     */
    private static void drawLegend(Graphics2D g2d, String name, Paint color, int x, int y) {
        g2d.setPaint(Color.black);
        g2d.drawString(name, x, y);
        g2d.setPaint(color);
        g2d.fillOval(x - 20, y - 11, 8, 8);
        g2d.setPaint(Color.lightGray);
        g2d.drawOval(x - 20, y - 11, 8, 8);
    }

    /**
     * Perform the visualization task
     * @param data train dataset
     * @param dataForClassification classification dataset
     * @param correct correct predictions
     * @param wrong wrong predictions
     */
    private static void visualize(Dataset data, Dataset dataForClassification,
                                  List<Instance> correct, List<Instance> wrong) {
        double[][] range = getDataRange(5, data, dataForClassification);
        BufferedImage image = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Set background color
        g2d.setPaint(Color.white);
        g2d.fillRect(0, 0, imgWidth, imgHeight);

        // Draw x-y coordinate lines
        g2d.setPaint(Color.darkGray);
        g2d.fillRect(50, 50 + (int)(imgHeight * 0.83),  (int)(imgWidth * 0.83) + 20, 1);
        g2d.fillRect(50, 30, 1, (int)(imgHeight * 0.83) + 20);

        // Transform for rotating 90 degrees
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.rotate(Math.toRadians(90), 0, 0);

        // Draw the name of params
        Font paramFont = new Font(fontName, Font.BOLD, fontSize);
        g2d.setFont(paramFont);
        g2d.setPaint(Color.black);
        g2d.drawString("P-TPT", 380, 570);
        Font rotatedFont = paramFont.deriveFont(affineTransform);
        g2d.setFont(rotatedFont);
        g2d.drawString("T-JUS-CKP", 30, 260);

        // Draw the data range
        Font dataFont = new Font(fontName, Font.PLAIN, fontSize);
        g2d.setFont(dataFont);
        g2d.setPaint(Color.gray);
        g2d.drawString(String.format("%.3f", range[xParam][0]), 50, 570);
        g2d.drawString(String.format("%.3f", range[xParam][1]), 620, 570);
        rotatedFont = dataFont.deriveFont(affineTransform);
        g2d.setFont(rotatedFont);
        g2d.drawString(String.format("%.3f", range[yParam][0]), 30, 480);
        g2d.drawString(String.format("%.3f", range[yParam][1]), 30, 20);
        // Draw the legend
        Font legendFont = new Font(fontName, Font.BOLD, fontSize);
        g2d.setFont(legendFont);
        drawLegend(g2d, "Class 0", colors.get(0), 680, 60);
        drawLegend(g2d, "Class 2", colors.get(2), 680, 80);
        drawLegend(g2d, "Class 102", colors.get(102), 680, 100);
        drawLegend(g2d, "Correct", Color.green, 680, 120);
        drawLegend(g2d, "Wrong", Color.red, 680, 140);

        // Draw points
        drawPoints(g2d, range, randomSelect(correct, 900), Color.green);
        drawPoints(g2d, range, randomSelect(wrong, 100), Color.red);
        drawPoints(g2d, range, randomSelect(data, 1000), null);
        g2d.dispose();
        showImage(image);
    }
}
