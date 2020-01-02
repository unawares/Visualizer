import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

public class VisualizerStep extends Application {

    interface Pointable {
        double getX();
        double getY();
        void setX(double x);
        void setY(double y);
    }

    private class Point implements Pointable {

        private double x;
        private double y;

        public Point() {
            x = 0;
            y = 0;
        }

        public Point(double x, double y ) {
            this.x = x;
            this.y = y;
        }

        /**
         * @return the x
         */
        @Override
        public double getX() {
            return x;
        }

        /**
         * @return the y
         */
        @Override
        public double getY() {
            return y;
        }

        /**
         * @param x the x to set
         */
        @Override
        public void setX(double x) {
            this.x = x;
        }
        
        /**
         * @param y the y to set
         */
        @Override
        public void setY(double y) {
            this.y = y;
        }

        @Override
        public String toString() {
            return String.format("(%f, %f)", x, y);
        }

    }

    private class Vector implements Pointable {

        private double x;
        private double y;

        public Vector() {
            x = 0;
            y = 0;
        }

        public Vector(double x, double y ) {
            this.x = x;
            this.y = y;
        }

        public Vector add(Vector other) {
            return new Vector(this.x + other.x, this.y + other.y);
        }

        public Vector subtract(Vector other) {
            return new Vector(this.x - other.x, this.y - other.y);
        }

        public double dotProduct(Vector other) {
            return this.x * other.x + this.y * other.y;
        }

        public Vector multiply(double scalar) {
            return new Vector(x * scalar, y * scalar);
        }

        /**
         * @return the x
         */
        @Override
        public double getX() {
            return x;
        }

        /**
         * @return the y
         */
        @Override
        public double getY() {
            return y;
        }

        /**
         * @param x the x to set
         */
        @Override
        public void setX(double x) {
            this.x = x;
        }

        /**
         * @param y the y to set
         */
        @Override
        public void setY(double y) {
            this.y = y;
        }

        @Override
        public String toString() {
            return String.format("<%f, %f>", x, y);
        }
    }

    private class VectorsGroup {

        private ArrayList<Vector> vectors;

        public VectorsGroup() {
            vectors = new ArrayList<>();
        }

        public VectorsGroup(ArrayList<Vector> vectors) {
            this.vectors = vectors;
        }

        public void addVector(Vector vector) {
            vectors.add(vector);
        }
        
        public void clear() {
            vectors.clear();
        }

        public PointsGroup toPointsGroup() {
            ArrayList<Point> points = new ArrayList<>();
            Vector start = new Vector();
            points.add(new Point(start.getX(), start.getY()));
            for (Vector vector : vectors) {
                start = start.add(vector);
                points.add(new Point(start.getX(), start.getY()));
            }
            return new PointsGroup(points);
        }

        /**
         * @return the vectors
         */
        public ArrayList<Vector> getVectors() {
            return vectors;
        }

        /**
         * @param vectors the vectors to set
         */
        public void setVectors(ArrayList<Vector> vectors) {
            this.vectors = vectors;
        }

        public int size() {
            return vectors.size();
        }

        @Override
        public String toString() {
            return vectors.toString();
        }

    }

    private class PointsGroup {

        private ArrayList<Point> points;

        public PointsGroup() {
            points = new ArrayList<>();
        }

        public PointsGroup(ArrayList<Point> points) {
            this.points = points;
        }

        public void addPoint(Point point) {
            points.add(point);
        }
        
        public void clear() {
            points.clear();
        }

        public VectorsGroup toVectorsGroup() {
            ArrayList<Vector> vectors = new ArrayList<>();
            Point last = null;
            for (Point point : points) {
                if (last != null) {
                    vectors.add(new Vector(point.x - last.x, point.y - last.y));
                }
                last = point;
            }
            return new VectorsGroup(vectors);
        }

        /**
         * @return the points
         */
        public ArrayList<Point> getPoints() {
            return points;
        }

        /**
         * @param points the points to set
         */
        public void setPoints(ArrayList<Point> points) {
            this.points = points;
        }

        public int size() {
            return points.size();
        }

        @Override
        public String toString() {
            return points.toString();
        }

    }

    private class BezierCurve {

        PointsGroup pointsGroup;

        public BezierCurve(PointsGroup pointsGroup) {
            this.pointsGroup = pointsGroup;
        }

        public BezierCurve(VectorsGroup vectorsGroup) {
            pointsGroup = vectorsGroup.toPointsGroup();
        }

        /**
         * @return the pointsGroup
         */
        public PointsGroup getPointsGroup() {
            return pointsGroup;
        }

        /**
         * @param pointsGroup the pointsGroup to set
         */
        public void setPointsGroup(PointsGroup pointsGroup) {
            this.pointsGroup = pointsGroup;
        }

        public Point getPoint(double t) {
            Point[] points = Arrays.copyOf(
                this.pointsGroup.getPoints().toArray(),
                this.pointsGroup.getPoints().size(),
                Point[].class
            );
            while (points.length > 1) {
                Point[] ps = new Point[points.length - 1];
                for (int i = 0; i < ps.length; i++) {
                    Point p1 = points[i];
                    Point p2 = points[i + 1];
                    ps[i] = new Point(
                        p1.getX() + t * (p2.getX() - p1.getX()),
                        p1.getY() + t * (p2.getY() - p1.getY())
                    );
                }
                points = ps;
            }
            if (points.length == 1) {
                return points[0];
            } else {
                return null;
            }
        }

        public boolean isValid() {
            return this.pointsGroup.size() > 1;
        }

    }

    public static void normalize(double[] values) {
        if (values.length > 0) {
            int minIndex = 0;
            int maxIndex = 0;
            double sum = 0;
            for (int i = 0; i < values.length; i++) {
                sum += values[i];
                if (values[i] < values[minIndex]) {
                    minIndex = i;
                }
                if (values[i] > values[maxIndex]) {
                    maxIndex = i;
                }
            }
            double min = values[minIndex];
            double max = values[maxIndex];
            double avg = sum / values.length;
            for (int i = 0; i < values.length; i++) {
                values[i] = ((values[i] - avg) / (max - min));
            }
        }
    }

    public static double easeOutCubic(double t) {
        return (--t) * t * t + 1;
    }

    public static double easeOutQuint(double t) {
        return 1 + (--t) * t * t * t * t;
    }

    public static double easeOutQuart(double t) {
        return 1 - (--t) * t * t * t;
    }

    private int INTERVAL = 20;
    private int NANOS = 1000 * 1000;
    private int BANDS = 48;
    double[] magnitudesBuffer = new double[BANDS];
    final URL resource = getClass().getResource("music.mp3");
    final Media media = new Media(resource.toString());
    final MediaPlayer mediaPlayer = new MediaPlayer(media);

    @Override
    public void start(Stage primaryStage) {
        Pane pane = new Pane();
        Scene scene = new Scene(pane, 800, 600);
        // BANDS = mediaPlayer.getAudioSpectrumNumBands();
        // double[] values = new double[BANDS];
        mediaPlayer.setAudioSpectrumListener(new AudioSpectrumListener(){
            long lastStep = 0;
            @Override
            public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases) {
                if (System.nanoTime() - lastStep > INTERVAL / 2 * NANOS) {
                    lastStep = System.nanoTime();
                    double sum = 0;
                    double max = 0.00000001;
                    for (int i = 0; i < magnitudes.length; i++) {
                        double v = magnitudes[i] - mediaPlayer.getAudioSpectrumThreshold();
                        if (v > max) {
                            max = v;
                        }
                    }
                    for (int i = 0; i < magnitudes.length; i++) {
                        double v = magnitudes[i] - mediaPlayer.getAudioSpectrumThreshold();
                        sum += v / max;
                    }
                    for (int i = magnitudesBuffer.length - 1; i > 0; i--) {
                        magnitudesBuffer[i] = magnitudesBuffer[i - 1];
                    }
                    magnitudesBuffer[0] = easeOutQuart(sum / magnitudes.length);
                    System.out.println(magnitudesBuffer[0]);
                } 
            }

        });

        mediaPlayer.setAudioSpectrumInterval(INTERVAL / 1000D);
        primaryStage.setScene(scene);
        primaryStage.show();
        mediaPlayer.play();

        new AnimationTimer() {

            long lastNanoTime = 0;

            @Override
            public void handle(long currentNanoTime) {
                if (currentNanoTime - lastNanoTime > 10 * NANOS) {
                    draw(pane, magnitudesBuffer);
                    lastNanoTime = System.nanoTime();
                }
            }

        }.start();
    }

    public void draw(Pane pane, double[] values) {
        final double horizontalPadding = 200;
        final double verticalPadding = 400;
        final double height = 800;
        final double width = 400;
        PointsGroup pointsGroup = new PointsGroup();
        pane.getChildren().clear();
        for (int i = 0; i < values.length; i += 1) {
            double x1 = (width / (values.length)) * i;
            double y1 = values[i] * (height / 2);
            // double x2 = (width / (values.length)) * (i + 1);
            // double y2 = -(values[i + 1] / 30) * (height / 2);
            pointsGroup.addPoint(new Point(x1, -y1));
            // pointsGroup.addPoint(new Point(x2, y2));
        }
        BezierCurve bezierCurve = new BezierCurve(pointsGroup);
        Point last = null;
        // int red = rand.nextInt(256);
        // int green = rand.nextInt(256);
        // int blue = rand.nextInt(256);
        for (double t = 0; t <= 1; t += 0.005) {
            Point point = bezierCurve.getPoint(t);
            if (last != null) {
                Line line = new Line(
                    last.x + horizontalPadding, last.y + verticalPadding,
                    point.getX() + horizontalPadding,
                    point.getY() + verticalPadding
                );                
                //  line.setStroke(Color.rgb(red, green, blue));
                line.setStrokeWidth(2);
                pane.getChildren().add(line);
            }
            last = point;
        }
        Point startPoint = pointsGroup.getPoints().get(0);
        Point endPoint = pointsGroup.getPoints().get(pointsGroup.getPoints().size() - 1);
        Line line = new Line(
            last.x + horizontalPadding, last.y + verticalPadding,
            endPoint.getX() + horizontalPadding,
            endPoint.getY() + verticalPadding
        );
        line.setStrokeWidth(2);
        pane.getChildren().add(line);
        Circle startCircle = new Circle(
            startPoint.getX() + horizontalPadding,
            startPoint.getY() + verticalPadding,
            7
        );
        Circle endCircle = new Circle(
            endPoint.getX() + horizontalPadding,
            endPoint.getY() + verticalPadding,
            7
        );
        pane.getChildren().add(startCircle);
        pane.getChildren().add(endCircle);
    }

}