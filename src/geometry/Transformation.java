package geometry;

public class Transformation {
	
	private static double Transform[][];
	
	public Transformation() {
		Transform = new double[4][4];
	}
	public static Transformation identity() {
		Transformation trans = new Transformation();
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				if(i == j) {
					Transform[i][j] = 1;
				}
				else {
					Transform[i][j] = 0;
				}
			}
		}
		return trans;
	}
	public static Transformation allZero() {
		Transformation trans = new Transformation();
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				Transform[i][j] = 0;
			}
		}
		return trans;
	}
	
	// 
	public static void transformMatrix(int row, int col, double transform) {
		Transform[row][col] = transform;
	}
	public double[][] getMatrix() {
		return Transform;
	}
}
