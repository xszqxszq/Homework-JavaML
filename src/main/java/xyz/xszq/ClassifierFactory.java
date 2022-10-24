package xyz.xszq;
import net.sf.javaml.classification.*;

public class ClassifierFactory {
    public static Classifier creator(String classifierName) {
        return switch (classifierName) {
            case "KDtreeKNN" -> new KDtreeKNN(5);
            case "KNearestNeighbors" -> new KNearestNeighbors(5);
            case "MeanFeatureVotingClassifier" -> new MeanFeatureVotingClassifier();
            case "NearestMeanClassifier" -> new NearestMeanClassifier();
            case "ZeroR" -> new ZeroR();
            default -> throw new UnknownError();
        };
    }
}
