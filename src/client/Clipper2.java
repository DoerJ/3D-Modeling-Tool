package client;

import client.interpreter.SimpInterpreter;
import geometry.Point2D;
import geometry.Point3DH;
import geometry.Vertex3D;
import windowing.graphics.Color;
import java.util.ArrayList;

public class Clipper2 {
    private Point3DH[] front = new Point3DH[4];
    private Point3DH[] back = new Point3DH[4];

    private double near_plane = 0;
    private double far_plane = 0;
    private double x_low = 0;
    private double y_low = 0;
    private double x_high = 0;
    private double y_high = 0;

    // coefficient
    private double[] result_near_3D = new double[4];
    private double[] result_far_3D = new double[4];
    private double[] result_left_2D = new double[3];
    private double[] result_top_2D = new double[3];
    private double[] result_right_2D = new double[3];
    private double[] result_bottom_2D = new double[3];

    private double[] temp_matrix3 = new double[12];

    private static ArrayList<Vertex3D> points_list;

    public Clipper2(double near_plane, double far_plane, double x_low, double y_low,
                   double x_high, double y_high, Vertex3D p1, Vertex3D p2, Vertex3D p3){
        this.near_plane = near_plane;
        this.far_plane = far_plane;
        this.x_low = x_low;
        this.y_low = y_low;
        this.x_high = x_high;
        this.y_high = y_high;

        points_list = new ArrayList<>();
        determineFrontPoint();
        determineBackPoint();

        p1 = determine_new_point_under_perspective(p1, p2);
        p2 = determine_new_point_under_perspective(p2, p3);
        p3 = determine_new_point_under_perspective(p3, p1);

        Point2D top_left = new Point2D(x_low, y_high);
        Point2D top_right = new Point2D(x_high, y_high);
        Point2D bottom_left = new Point2D(x_low, y_low);
        Point2D bottom_right = new Point2D(x_high, y_low);

        determine_topPlane(top_left, top_right);

        determine_bottomPlane(bottom_left, bottom_right);

        determine_rightPlane(top_right, bottom_right);

        determine_leftPlane(top_left, bottom_left);

        Vertex3D point1_left = p1;
        Vertex3D point1_right = p1;
        Vertex3D point2_left = p2;
        Vertex3D point2_right = p2;
        Vertex3D point3_left = p3;
        Vertex3D point3_right = p3;

        determine_new_point_after_2D_clipping(point1_right, point2_left);
        determine_new_point_after_2D_clipping(point1_left, point3_right);
        determine_new_point_after_2D_clipping(point2_right, point3_left);
        determine_new_point_after_2D_clipping(point2_left, point1_right);
        determine_new_point_after_2D_clipping(point3_right, point1_left);
        determine_new_point_after_2D_clipping(point3_left, point2_right);
    }

    private void determineFrontPoint(){
        front[0] = new Point3DH( near_plane, -1 * near_plane, near_plane);
        front[3] = new Point3DH(-1 * near_plane, -1 * near_plane, near_plane);
        front[1] = new Point3DH(near_plane, near_plane, near_plane);
        front[2] = new Point3DH(-1 * near_plane, near_plane, near_plane);
        result_near_3D = determine3D_equation(front);
    }

    private void determineBackPoint(){
        back[0] = new Point3DH(far_plane, -1* far_plane, far_plane);
        back[3] = new Point3DH(-1 * far_plane, -1 * far_plane, far_plane);
        back[1] = new Point3DH(far_plane, far_plane,far_plane);
        back[2] = new Point3DH(-1 * far_plane, far_plane, far_plane);
        result_far_3D = determine3D_equation(back);
    }


    private double[] determine3D_equation(Point3DH[] points){
        Point3DH p1 = points[0];
        Point3DH p2 = points[1];
        Point3DH p3 = points[2];
        temp_matrix3[0] = p1.getX();
        temp_matrix3[4] = p2.getX();
        temp_matrix3[8] = p3.getX();
        temp_matrix3[3] = p1.getW();

        temp_matrix3[1] = p1.getY();
        temp_matrix3[5] = p2.getY();
        temp_matrix3[9] = p3.getY();
        temp_matrix3[7] = p2.getW();

        temp_matrix3[2] = p1.getZ();
        temp_matrix3[6] = p2.getZ();
        temp_matrix3[10] = p3.getZ();
        temp_matrix3[11] = p3.getW();

        return determine_individual_line_coefficient();
    }


    private double[] determine_individual_line_coefficient(){
        double[] result = new double[4];
        double[] matrix1 = new double[9];
        double[] matrix2 = new double[9];
        double[] matrix3 = new double[9];
        double[] matrix4 = new double[9];

        matrix1[0] = temp_matrix3[1];
        matrix1[1] = temp_matrix3[2];
        matrix1[2] = temp_matrix3[3];
        matrix1[3] = temp_matrix3[5];
        matrix1[4] = temp_matrix3[6];
        matrix1[5] = temp_matrix3[7];
        matrix1[6] = temp_matrix3[9];
        matrix1[7] = temp_matrix3[10];
        matrix1[8] = temp_matrix3[11];
        result[0] = calculate_determinant_3D(matrix1);

        matrix2[0] = temp_matrix3[0];
        matrix2[1] = temp_matrix3[2];
        matrix2[2] = temp_matrix3[3];
        matrix2[3] = temp_matrix3[4];
        matrix2[4] = temp_matrix3[6];
        matrix2[5] = temp_matrix3[7];
        matrix2[6] = temp_matrix3[8];
        matrix2[7] = temp_matrix3[10];
        matrix2[8] = temp_matrix3[11];
        result[1] = -1 * calculate_determinant_3D(matrix2);

        matrix3[0] = temp_matrix3[0];
        matrix3[1] = temp_matrix3[1];
        matrix3[2] = temp_matrix3[3];
        matrix3[3] = temp_matrix3[4];
        matrix3[4] = temp_matrix3[5];
        matrix3[5] = temp_matrix3[7];
        matrix3[6] = temp_matrix3[8];
        matrix3[7] = temp_matrix3[10];
        matrix3[8] = temp_matrix3[11];
        result[2] = calculate_determinant_3D(matrix3);

        matrix4[0] = temp_matrix3[0];
        matrix4[1] = temp_matrix3[1];
        matrix4[2] = temp_matrix3[2];
        matrix4[3] = temp_matrix3[4];
        matrix4[4] = temp_matrix3[5];
        matrix4[5] = temp_matrix3[6];
        matrix4[6] = temp_matrix3[8];
        matrix4[7] = temp_matrix3[9];
        matrix4[8] = temp_matrix3[10];
        result[3] = -1 * calculate_determinant_3D(matrix4);

        return result;
    }

    // 3D clipping needed
    private boolean outside_nearPlane(Vertex3D point) {
        boolean is_near_clip_needed = false;
        double x = point.getX();
        double y = point.getY();
        double z = point.getZ();
        double w = point.getPoint3D().getW();
        double near_value = x * result_near_3D[0] + y * result_near_3D[1] + z * result_near_3D[2] + w * result_near_3D[3];
        if(near_value > 0){
            is_near_clip_needed = true;
        }
        return is_near_clip_needed;
    }

    private boolean outside_farPlane(Vertex3D point) {
        boolean is_far_clip_needed = false;
        double x = point.getX();
        double y = point.getY();
        double z = point.getZ();
        double w = point.getPoint3D().getW();
        double value = x * result_far_3D[0] + y * result_far_3D[1] + z * result_far_3D[2] + w * result_far_3D[3];
        if(value < 0){
            is_far_clip_needed = true;
        }
        return is_far_clip_needed;
    }

    private boolean outside_topPlane(Vertex3D point) {
        boolean is_top_line_clipping_needed = false;
        double x = point.getX();
        double y = point.getY();
        double w = point.getPoint3D().getW();
        double value = x * result_top_2D[0] + y * result_top_2D[1] + w * result_top_2D[2];
        if(value < 0){
            is_top_line_clipping_needed = true;
        }
        return is_top_line_clipping_needed;
    }

    private boolean outside_bottomPlane(Vertex3D point){
        boolean is_bottom_line_clipping_needed = false;
        double x = point.getX();
        double y = point.getY();
        double w = point.getPoint3D().getW();
        double value = x * result_bottom_2D[0] + y * result_bottom_2D[1] + w * result_bottom_2D[2];
        if(value > 0){
            is_bottom_line_clipping_needed = true;
        }
        return is_bottom_line_clipping_needed;
    }

    private boolean outside_rightPlane(Vertex3D point) {
        boolean is_right_line_clipping_needed = false;
        double x = point.getX();
        double y = point.getY();
        double w = point.getPoint3D().getW();
        double value = x * result_right_2D[0] + y * result_right_2D[1] + w * result_right_2D[2];
        if(value < 0){
            is_right_line_clipping_needed = true;
        }
        return is_right_line_clipping_needed;
    }

    private boolean outside_leftPlane(Vertex3D point) {
        boolean is_left_line_clipping_needed = false;
        double x = point.getX();
        double y = point.getY();
        double w = point.getPoint3D().getW();
        double value = x * result_left_2D[0] + y * result_left_2D[1] + w * result_left_2D[2];
        if(value > 0){
            is_left_line_clipping_needed = true;
        }
        return is_left_line_clipping_needed;
    }

    private Vertex3D determine_new_point_under_perspective(Vertex3D p1, Vertex3D p2) {
        Vertex3D temp = p1;
        boolean near_plane_clip = outside_nearPlane(p1);
        boolean far_plane_clip = outside_farPlane(temp);
        if(near_plane_clip) {
            p1 = find_point_against_near_clip(p1, p2);
            near_plane_clip = false;
        }
        if(far_plane_clip) {
            p1 = find_point_against_far_clip(p1, p2);
            far_plane_clip = false;
        }
        return p1;
    }

    private Vertex3D find_point_against_near_clip(Vertex3D p1, Vertex3D p2) {
        double deltaR = p2.getColor().getIntR() - p1.getColor().getIntR();
        double deltaG = p2.getColor().getIntG() - p1.getColor().getIntG();
        double deltaB = p2.getColor().getIntB() - p1.getColor().getIntB();
        double deltaX = p2.getX() - p1.getX();
        double deltaY = p2.getY() - p1.getY();
        double deltaZ = p2.getZ() - p1.getZ();
        double deno =  Math.abs(deltaZ) > Math.abs(deltaY) ? deltaZ : deltaY;
        deno = Math.abs(deltaX) > Math.abs(deno) ? deltaX : deno;

        double slopeX = deltaX / deno;
        double slopeY = deltaY / deno;
        double slopeR = deltaR / deno;
        double slopeG = deltaG / deno;
        double slopeB = deltaB / deno;

        double x_initial = p1.getX();
        double y_initial = p1.getY();
        double z_initial = p1.getZ();
        double r_initial = p1.getColor().getIntR();
        double g_initial = p1.getColor().getIntG();
        double b_initial = p1.getColor().getIntB();

        while(z_initial >= near_plane) {
            x_initial += slopeX;
            y_initial += slopeY;
            r_initial += slopeR;
            g_initial += slopeG;
            b_initial += slopeB;
            z_initial--;
            if(z_initial == p2.getZ()) {
                return p1;
            }
        }
        Color color = Color.fromARGB(Color.makeARGB((int)Math.round(r_initial),
                (int)Math.round(g_initial), (int)Math.round(b_initial)));
        return new Vertex3D(x_initial, y_initial, z_initial, color);

    }

    private Vertex3D find_point_against_far_clip(Vertex3D p1, Vertex3D p2) {
        double deltaR = p2.getColor().getIntR() - p1.getColor().getIntR();
        double deltaG = p2.getColor().getIntG() - p1.getColor().getIntG();
        double deltaB = p2.getColor().getIntB() - p1.getColor().getIntB();
        double deltaX = p2.getX() - p1.getX();
        double deltaY = p2.getY() - p1.getY();
        double deltaZ = p2.getZ() - p1.getZ();
        double deno =  Math.abs(deltaZ) > Math.abs(deltaY) ? deltaZ : deltaY;
        deno = Math.abs(deltaX) > Math.abs(deno) ? deltaX : deno;

        double slopeX = deltaX / deno;
        double slopeY = deltaY / deno;
        double slopeR = deltaR / deno;
        double slopeG = deltaG / deno;
        double slopeB = deltaB / deno;

        double x_initial = p1.getX();
        double y_initial = p1.getY();
        double z_initial = p1.getZ();
        double r_initial = p1.getColor().getIntR();
        double g_initial = p1.getColor().getIntG();
        double b_initial = p1.getColor().getIntB();

        while(z_initial <= far_plane) {
            x_initial += slopeX;
            y_initial += slopeY;
            r_initial += slopeR;
            g_initial += slopeG;
            b_initial += slopeB;
            z_initial ++;
            if(z_initial != p2.getZ()) {
                return p1;
            }
        }
        Color color = Color.fromARGB(Color.makeARGB((int)Math.round(r_initial),
                (int)Math.round(g_initial), (int)Math.round(b_initial)));
        return new Vertex3D(x_initial, y_initial, z_initial, color);
    }


    private void determine_new_point_after_2D_clipping(Vertex3D p1, Vertex3D p2) {
        boolean top_line ;
        boolean bottom_line;
        boolean right_line;
        boolean left_line;

        top_line = outside_topPlane(p1);
        bottom_line = outside_bottomPlane(p1);
        right_line = outside_rightPlane(p1);
        left_line = outside_leftPlane(p1);

        if (right_line) {
            p1 = right_line_clipping(p1, p2);
            right_line = false;
        }
        if (top_line) {
            p1 = top_line_clipping(p1, p2);
            top_line = false;
        }
        if (bottom_line) {
            p1 = bottom_line_clipping(p1, p2);
            bottom_line = false;
        }
        if (left_line) {
            p1 = left_line_clipping(p1, p2);
            left_line = false;
        }
        if(!top_line && !bottom_line && !left_line && !right_line) {
            add_to_the_list(p1);
        }
    }

    private void add_to_the_list(Vertex3D point) {
        boolean not_in_the_list = true;
        double x = point.getX();
        double y = point.getY();
        int color = point.getColor().asARGB();
        for (Vertex3D comparing_point : points_list) {
            double comparing_x = comparing_point.getX();
            double comparing_y = comparing_point.getY();
            int comparing_color = comparing_point.getColor().asARGB();
            if (x == comparing_x && y == comparing_y && color == comparing_color) {
                not_in_the_list = false;
            }
        }
        if(not_in_the_list) {
            points_list.add(point);
        }
    }

    private Vertex3D top_line_clipping(Vertex3D p1, Vertex3D p2) {
        double deltaR = p2.getColor().getIntR() - p1.getColor().getIntR();
        double deltaG = p2.getColor().getIntG() - p1.getColor().getIntG();
        double deltaB = p2.getColor().getIntB() - p1.getColor().getIntB();
        double deltaX = p2.getX() - p1.getX();
        double deltaY = p2.getY() - p1.getY();
        double deltaZ = p2.getZ() - p1.getZ();

        double x = p1.getX();
        double y = p1.getY();
        double z = p1.getZ();
        double r = p1.getColor().getIntR();
        double g = p1.getColor().getIntG();
        double b = p1.getColor().getIntB();
        double deno = Math.abs(deltaX) >= Math.abs(deltaY) ? deltaX : deltaY;

        double slopeX = deltaX / deno;
        double slopeZ = deltaZ / deno;
        double slopeR = deltaR / deno;
        double slopeG = deltaG / deno;
        double slopeB = deltaB / deno;

        while(y >= y_high) {
            y --;
            z += slopeZ;
            x += slopeX;
            r += slopeR;
            g += slopeG;
            b += slopeB;
            if(y == p2.getY()) {
                return p1;
            }
        }

        Color color = Color.fromARGB(Color.makeARGB((int)Math.round(r), (int)Math.round(g), (int)Math.round(b)));
        return new Vertex3D(x, y, z, color);
    }

    private Vertex3D bottom_line_clipping(Vertex3D p1, Vertex3D p2){
        double deltaR = p2.getColor().getIntR() - p1.getColor().getIntR();
        double deltaG = p2.getColor().getIntG() - p1.getColor().getIntG();
        double deltaB = p2.getColor().getIntB() - p1.getColor().getIntB();
        double deltaX = p2.getX() - p1.getX();
        double deltaY = p2.getY() - p1.getY();
        double deltaZ = p2.getY() - p1.getY();

        double x = p1.getX();
        double y = p1.getY();
        double z = p1.getZ();
        double r = p1.getColor().getIntR();
        double g = p1.getColor().getIntG();
        double b = p1.getColor().getIntB();
        double deno = Math.abs(deltaX) >= Math.abs(deltaY) ? deltaX : deltaY;

        double slopeX = deltaX / deno;
        double slopeZ = deltaZ / deno;
        double slopeR = deltaR / deno;
        double slopeG = deltaG / deno;
        double slopeB = deltaB / deno;

        while(y <= y_low){
            y ++;
            z += slopeZ;
            x += slopeX;
            r += slopeR;
            g += slopeG;
            b += slopeB;
            if(y == p2.getY()) {
                return p1;
            }
        }
        Color color = Color.fromARGB(Color.makeARGB((int)Math.round(r), (int)Math.round(g), (int)Math.round(b)));
        return new Vertex3D(x, y, z, color);
    }

    private Vertex3D left_line_clipping(Vertex3D p1, Vertex3D p2) {
        double deltaR = p2.getColor().getIntR() - p1.getColor().getIntR();
        double deltaG = p2.getColor().getIntG() - p1.getColor().getIntG();
        double deltaB = p2.getColor().getIntB() - p1.getColor().getIntB();
        double deltaX = p2.getX() - p1.getX();
        double deltaY = p2.getY() - p1.getY();
        double deltaZ = p2.getZ() - p1.getZ();

        double x = p1.getX();
        double y = p1.getY();
        double z = p1.getZ();
        double r = p1.getColor().getIntR();
        double g = p1.getColor().getIntG();
        double b = p1.getColor().getIntB();
        double deno = Math.abs(deltaX) >= Math.abs(deltaY) ? deltaX : deltaY;

        double slopeY = deltaY / deno;
        double slopeZ = deltaZ / deno;
        double slopeR = deltaR / deno;
        double slopeG = deltaG / deno;
        double slopeB = deltaB / deno;
        while(x <= x_low) {
            x ++;
            z += slopeZ;
            y += slopeY;
            r += slopeR;
            g += slopeG;
            b += slopeB;
            if(x == p2.getX()) {
                return p1;
            }
        }
        Color color = Color.fromARGB(Color.makeARGB((int)Math.round(r), (int)Math.round(g), (int)Math.round(b)));
        return new Vertex3D(x, y, z, color);
    }

    private Vertex3D right_line_clipping(Vertex3D p1, Vertex3D p2) {
    	double x = p1.getX();
        double y = p1.getY();
        double z = p1.getZ();
        double deltaR = p2.getColor().getIntR() - p1.getColor().getIntR();
        double deltaG = p2.getColor().getIntG() - p1.getColor().getIntG();
        double deltaB = p2.getColor().getIntB() - p1.getColor().getIntB();
        double deltaX = p2.getX() - p1.getX();
        double deltaY = p2.getY() - p1.getY();
        double deltaZ = p2.getZ() - p1.getZ();

        double r = p1.getColor().getIntR();
        double g = p1.getColor().getIntG();
        double b = p1.getColor().getIntB();
        double deno = Math.abs(deltaX) >= Math.abs(deltaY) ? deltaX : deltaY;

        double slopeY = deltaY / deno;
        double slopeZ = deltaZ / deno;

        double slopeR = deltaR / deno;
        double slopeG = deltaG / deno;
        double slopeB = deltaB / deno;

        while(x >= x_high) {
            x--;
            z += slopeZ;
            y += slopeY;
            r += slopeR;
            g += slopeG;
            b += slopeB;
            if(x == p2.getX())
                return p1;
        }
        Color color = Color.fromARGB(Color.makeARGB((int)Math.round(r), (int)Math.round(g), (int)Math.round(b)));
        return new Vertex3D(x, y, z, color);
    }


    // all line equations after times the perspective matrix
    private void determine_topPlane(Point2D p1, Point2D p2) {
        Point2D[] points = new Point2D[2];
        points[0] = p1;
        points[1] = p2;

        result_top_2D = determine2D_equation(points);
    }

    private void determine_rightPlane(Point2D p1, Point2D p2) {
        Point2D[] points = new Point2D[2];
        points[0] = p1;
        points[1] = p2;

        result_right_2D = determine2D_equation(points);
    }

    private void determine_bottomPlane(Point2D p1, Point2D p2) {
        Point2D[] points = new Point2D[2];
        points[0] = p1;
        points[1] = p2;

        result_bottom_2D = determine2D_equation(points);
    }

    private void determine_leftPlane(Point2D p1, Point2D p2) {
        Point2D[] points = new Point2D[2];
        points[0] = p1;
        points[1] = p2;

        result_left_2D = determine2D_equation(points);
    }

    // determine A, B, C for the equation
    private double[] determine2D_equation(Point2D[] points) {
        double[] result = new double[3];
        Point2D p1 = points[0];
        Point2D p2 = points[1];
        double x1 = p1.getX();
        double x2 = p2.getX();
        double y1 = p1.getY();
        double y2 = p2.getY();
        double z1 = 1.0;
        double z2 = 1.0;
        double[] matrix1 = new double[4];
        double[] matrix2 = new double[4];
        double[] matrix3 = new double[4];
        matrix1[0] = y1;
        matrix1[1] = z1;
        matrix1[2] = y2;
        matrix1[3] = z2;

        result[0] = calculate_determinant_2D(matrix1);

        matrix2[0] = x1;
        matrix2[1] = z1;
        matrix2[2] = x2;
        matrix2[3] = z2;
        result[1] = -1 * calculate_determinant_2D(matrix2);

        matrix3[0] = x1;
        matrix3[1] = y1;
        matrix3[2] = x2;
        matrix3[3] = y2;
        result[2] = calculate_determinant_2D(matrix3);

        return result;
    }
    
    private double calculate_determinant_2D(double[] values) {
        double on_diagonal = values[0] * values[3];
        double off_diagonal = values[1] * values[2];
        return on_diagonal - off_diagonal;
    }

    private double calculate_determinant_3D(double[] values) {
        double on_diagonal_1 = values[0] * values[4] * values[8];
        double on_diagonal_2 = values[1] * values[5] * values[6];
        double on_diagonal_3 = values[3] * values[7] * values[2];

        double off_diagonal_1 = values[2] * values[4] * values[6];
        double off_diagonal_2 = values[1] * values[3] * values[8];
        double off_diagonal_3 = values[0] * values[5] * values[7];

        return on_diagonal_1 + on_diagonal_2 + on_diagonal_3 - off_diagonal_1 - off_diagonal_2 - off_diagonal_3;
    }
    
    public ArrayList<Vertex3D> getVertex(){
        
        ArrayList<Vertex3D> points = new ArrayList<>();

        for (Vertex3D current : points_list) {
            double[] current_point = new double[4];
            current_point[0] = current.getX();
            current_point[1] = current.getY();
            current_point[2] = current.getZ();
            current_point[3] = current.getPoint3D().getW();
            Color current_color = current.getColor();
            Point3DH new_point = new Point3DH(current_point[0], current_point[1], current_point[2], current_point[3]).euclidean(); 
            new_point.setZ(current.getZ());
            Vertex3D point = new Vertex3D(new_point, current_color);

            points.add(point);
        }
        return points;
    }

}