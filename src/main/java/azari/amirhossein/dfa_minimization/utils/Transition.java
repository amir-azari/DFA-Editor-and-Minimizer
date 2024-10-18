package azari.amirhossein.dfa_minimization.utils;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

public class Transition {
    private State fromState;
    private State toState;
    private String symbol;

    private Shape line;
    private Polygon arrow;
    private Text text;
    private boolean isCurved;
    private Pane pane;

    public Transition(State fromState, State toState, String symbol, boolean isCurved) {
        this.fromState = fromState;
        this.toState = toState;
        this.symbol = symbol;
        this.isCurved = isCurved;

        createLine();
    }
    // Create line or curve line
    private void createLine() {
        double startX = fromState.getX();
        double startY = fromState.getY();
        double endX = toState.getX();
        double endY = toState.getY();

        double radius = Constants.RADIUS;
        double angle = Math.atan2(endY - startY, endX - startX);

        double lineStartX = startX + radius * Math.cos(angle);
        double lineStartY = startY + radius * Math.sin(angle);
        double lineEndX = endX - radius * Math.cos(angle);
        double lineEndY = endY - radius * Math.sin(angle);

        if (isCurved) {
            double midX = (lineStartX + lineEndX) / 2;
            double midY = (lineStartY + lineEndY) / 2;

            double controlOffsetX = (startY - endY) * 0.2;
            double controlOffsetY = (endX - startX) * 0.2;

            CubicCurve curve = new CubicCurve(
                    lineStartX, lineStartY,
                    midX + controlOffsetX, midY + controlOffsetY,
                    midX + controlOffsetX, midY + controlOffsetY,
                    lineEndX, lineEndY
            );

            curve.setFill(null);
            curve.setStroke(Color.web(Constants.COLOR_BLACK));
            curve.setStrokeWidth(2);
            line = curve;
        } else {
            Line straightLine = new Line(lineStartX, lineStartY, lineEndX, lineEndY);
            straightLine.setStroke(Color.web(Constants.COLOR_BLACK));
            straightLine.setStrokeWidth(2);
            line = straightLine;
        }
    }
    // Draw an arrow at the end of the line
    private void drawArrow(Pane pane) {
        double endX, endY, angle;

        if (line instanceof Line l) {
            endX = l.getEndX();
            endY = l.getEndY();
            angle = Math.atan2(l.getEndY() - l.getStartY(), l.getEndX() - l.getStartX());
        } else {
            CubicCurve c = (CubicCurve) line;
            endX = c.getEndX();
            endY = c.getEndY();
            double controlX = c.getControlX2();
            double controlY = c.getControlY2();
            double dx = endX - controlX;
            double dy = endY - controlY;
            angle = Math.atan2(dy, dx);
        }

        double arrowSize = Constants.ARROW_SIZE;

        double arrowX1 = endX - arrowSize * Math.cos(angle - Math.PI / 6);
        double arrowY1 = endY - arrowSize * Math.sin(angle - Math.PI / 6);
        double arrowX2 = endX - arrowSize * Math.cos(angle + Math.PI / 6);
        double arrowY2 = endY - arrowSize * Math.sin(angle + Math.PI / 6);

        arrow = new Polygon();
        arrow.getPoints().addAll(endX, endY, arrowX1, arrowY1, arrowX2, arrowY2);
        arrow.setFill(Color.web(Constants.COLOR_BLACK));

        pane.getChildren().add(arrow);
    }

    // Draw the symbol in the middle of the line
    private void drawSymbol(Pane pane) {
        double[] coordinates = calculateCoordinates();
        double x = coordinates[0];
        double y = coordinates[1];
        double nx = coordinates[2];
        double ny = coordinates[3];

        double offset = Constants.SYMBOL_OFFSET;
        x += nx * offset;
        y += ny * offset;

        text = new Text(symbol);
        text.setX(x - text.getBoundsInLocal().getWidth() / 2);
        text.setY(y + text.getBoundsInLocal().getHeight() / 4);
        pane.getChildren().add(text);
    }
    //calculate coordinates for curve line and straight line
    private double[] calculateCoordinates() {
        double x, y, nx, ny;
        if (line instanceof Line l) {
            x = (l.getStartX() + l.getEndX()) / 2;
            y = (l.getStartY() + l.getEndY()) / 2;
            double dx = l.getEndX() - l.getStartX();
            double dy = l.getEndY() - l.getStartY();
            double length = Math.sqrt(dx * dx + dy * dy);
            nx = -dy / length;
            ny = dx / length;
        } else {
            CubicCurve c = (CubicCurve) line;
            double t = 0.5;
            x = calculateBezierCoordinate(c.getStartX(), c.getControlX1(), c.getControlX2(), c.getEndX(), t);
            y = calculateBezierCoordinate(c.getStartY(), c.getControlY1(), c.getControlY2(), c.getEndY(), t);

            double dx = calculateBezierDerivative(c.getStartX(), c.getControlX1(), c.getControlX2(), c.getEndX(), t);
            double dy = calculateBezierDerivative(c.getStartY(), c.getControlY1(), c.getControlY2(), c.getEndY(), t);

            double length = Math.sqrt(dx * dx + dy * dy);
            nx = -dy / length;
            ny = dx / length;
        }
        return new double[]{x, y, nx, ny};
    }

    private double calculateBezierCoordinate(double p0, double p1, double p2, double p3, double t) {
        return Math.pow(1 - t, 3) * p0 +
                3 * Math.pow(1 - t, 2) * t * p1 +
                3 * (1 - t) * Math.pow(t, 2) * p2 +
                Math.pow(t, 3) * p3;
    }

    private double calculateBezierDerivative(double p0, double p1, double p2, double p3, double t) {
        return -3 * Math.pow(1 - t, 2) * p0 +
                3 * (Math.pow(1 - t, 2) - 2 * t * (1 - t)) * p1 +
                3 * (2 * t * (1 - t) - Math.pow(t, 2)) * p2 +
                3 * Math.pow(t, 2) * p3;
    }

    // add views to pane
    public void draw(Pane pane) {
        this.pane = pane;
        pane.getChildren().add(line);
        drawArrow(pane);
        drawSymbol(pane);
    }

    public void setCurved(boolean curved) {
        if (this.isCurved != curved) {
            this.isCurved = curved;
            if (pane != null) {
                pane.getChildren().remove(line);
                pane.getChildren().remove(arrow);
                pane.getChildren().remove(text);
            }
            createLine();
            if (pane != null) {
                // Redraw the transition
                draw(pane);
            }
        }
    }

    public Shape getLine() {
        return line;
    }

    public State getFromState() {
        return fromState;
    }

    public State getToState() {
        return toState;
    }

    public Polygon getArrow() {
        return arrow;
    }

    public Text getText() {
        return text;
    }
}