package xyz.xszq;
import com.csvreader.CsvReader;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SimulatedData {
    public int _class;
    public double p_pdg;
    public double p_tpt;
    public double t_tpt;
    public double p_mon_ckp;
    public double t_jus_ckp;
    public SimulatedData(String _class, String p_pdg, String p_tpt, String t_tpt, String p_mon_ckp, String t_jus_ckp) {
        this._class = Integer.parseInt(_class);
        this.p_pdg = Double.parseDouble(p_pdg);
        this.p_tpt = Double.parseDouble(p_tpt);
        this.t_tpt = Double.parseDouble(t_tpt);
        this.p_mon_ckp = Double.parseDouble(p_mon_ckp);
        this.t_jus_ckp = Double.parseDouble(t_jus_ckp);
    }

    /**
     * Read String from InputStream.
     * @param inputStream the target stream
     * @return content
     */
    private static String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

    /**
     * Read String from the File of the provided path.
     * @param path path to the file
     * @return content
     */
    private static String readFile(String path) throws IOException {
        ClassLoader classLoader = JavaML.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(path);
        String data = readFromInputStream(inputStream);
        assert inputStream != null;
        inputStream.close();
        return data;
    }

    /**
     * Parse CSV dataset to a List&lt;SimulatedData&gt;.
     * @param path path to the csv file
     * @return parsed list
     */
    private static List<SimulatedData> readCsv(String path) throws IOException {
        List<SimulatedData> result = new ArrayList<>();
        CsvReader reader = CsvReader.parse(readFile(path));
        // Skip the header
        reader.readHeaders();
        // Parse
        while (reader.readRecord()) {
            SimulatedData data = new SimulatedData(reader.get(9), reader.get(1), reader.get(2), reader.get(3),
                    reader.get(4), reader.get(5));
            result.add(data);
        }
        return result;
    }

    /**
     * Read dataset files from a folder and do the preprocessing.
     * @param folder path to the folder
     * @param begin begin id of the SIMULATED_XXXXX.csv
     * @param end end id of the SIMULATED_XXXXX.csv
     * @return Dataset
     */
    public static Dataset readFolder(String folder, int begin, int end) throws IOException {
        Dataset dataset = new DefaultDataset();
        for (int i = begin; i <= end; i ++) {
            List<SimulatedData> raw = readCsv(String.format("%s/SIMULATED_%05d.csv", folder, i));
            // Preprocessing
            raw.forEach(data -> {
                double[] attrs = { data.p_pdg, data.p_tpt, data.t_tpt, data.p_mon_ckp, data.t_jus_ckp };
                Instance instance = new DenseInstance(attrs, data._class);
                dataset.add(instance);
            });
        }
        return dataset;
    }
}
