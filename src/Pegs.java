import java.util.Random;

/**
 * Used to spawn pegs and move the pegs that are on the board
 * 
 * @author Stefan Mitic, Henry Moon, Steven Gorodetsky
 *
 */
public class Pegs {
	int row, col = 9;
	BoardTester bt;
	String color;
	int place;
	
	/**
	 * A method that spawns random pegs
	 */
	public void put() {
		Random r = new Random();
		bt.randomKey = r.nextInt(4);

		if (bt.randomKey == 0) {
			bt.b.putPeg("red", 0, 9);
			bt.spawn = 0;
			col = 9;
			row = 0;
			color = "red";
			
		} else if (bt.randomKey == 1) {
			bt.b.putPeg("blue", 1, 9);
			bt.spawn = 1;
			col = 9;
			row = 1;
			color = "blue";
		} else if (bt.randomKey == 2) {
			bt.b.putPeg("green", 2, 9);
			bt.spawn = 2;
			col = 9;
			row = 2;
			color = "green";
		} else if (bt.randomKey == 3) {
			bt.b.putPeg("yellow", 3, 9);
			bt.spawn = 3;
			col = 9;
			row = 3;
			color = "yellow";
		}
		place = bt.randomKey;
	}
	
	/**
	 * A method that moves the pegs on the board
	 */
	public void move() {
		bt.b.removePeg(row, col);
		col--;
		if (col > -1)	
			bt.b.putPeg(color, row, col);
	}
}
