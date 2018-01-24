import java.io.*;
import java.util.Scanner;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JOptionPane;

// import sun.audio.AudioPlayer;
// import sun.audio.AudioStream;

/**
 * This the main class for creating the whole game and is used to access the
 * board class to create the main board.
 * 
 * @author Stefan Mitic, Henry Moon, Steven Gorodetsky
 */

public class BoardTester {
	static int y = 0, x, timeCount = 0, randomKey = 5, counter = -1, counter2 = 0, points = 0, place = 0;
	static Board b;
	static int[][] grid;
	static int spawn;
	static Pegs p[] = new Pegs[1000];
	static int gO = 0;
	static int highScore[] = new int[5];
	static String names[] = new String[5];
	// static AudioStream as;
	static File soundFile;
	static AudioInputStream in;
	static Clip clip;
	static String song;
	static boolean firstTime = true, songPicked = false;

	/**
	 * A method that creates a JOptionPane with a start menu
	 */
	public static void startMenu() {
		Object[] options = { "Start", "Instructions", "Leaderboard" };
		int action = JOptionPane.showOptionDialog(null, "", "Menu", JOptionPane.DEFAULT_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		switch (action) {
		case 0:
			songsOption();
			break;
		case 1:
			instructions();
			break;
		case 2:
			leaderBoard();
			break;
		case JOptionPane.CLOSED_OPTION:
			System.exit(1);
		}

	}

	/**
	 * A method that a JOptionPane with song options
	 */
	public static void songsOption() {
		Object[] options = { "Global Deejays", "Give It Up Now", "Flower Dance" };
		int action = JOptionPane.showOptionDialog(null, "Choose a song:", "Song Menu", JOptionPane.DEFAULT_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		switch (action) {
		case 0:
			song = "Global Deejays.wav";
			songPicked = true;
			break;
		case 1:
			song = "Give It Up Now.wav";
			songPicked = true;
			break;
		case 2:
			song = "song.wav";
			songPicked = true;
			break;
		case JOptionPane.CLOSED_OPTION:
			startMenu();
		}

		clip.stop();
		// AudioPlayer.player.stop(as);
		songPicked = false;

		if (firstTime) {
			b = new Board(4, 10);
			play();
		} else {
			play();
		}

	}

	/**
	 * A method that creates a JOptionPane with instructions
	 */
	public static void instructions() {

		JOptionPane.showConfirmDialog(null,
				"Controls: A S D F \nIn this game you must tap the peg at the right time to get a point. "
						+ "\nTo tap the peg you must tap the corresponding key. "
						+ "\nThe pegs move across from left to right and each row has a certain key assigned to it.",
				"Instructions", JOptionPane.CLOSED_OPTION);
		startMenu();

	}

	/**
	 * A method that creates a JOptionPane with the top five scores in the game
	 */
	public static void leaderBoard() {
		JOptionPane.showConfirmDialog(null,
				names[0] + " " + highScore[0] + "\n" + names[1] + " " + highScore[1] + "\n" + names[2] + " "
						+ highScore[2] + "\n" + names[3] + " " + highScore[3] + "\n" + names[4] + " " + highScore[4],
				"Leaderboard", JOptionPane.CLOSED_OPTION);

		startMenu();
	}

	/**
	 * A method that saves the top 5 high scores in the game
	 */
	public static void scoreSaver() {
		File file = new File("score");
		Scanner fileIn = null;
		PrintWriter output = null;
		clip.stop();
		// AudioPlayer.player.stop(as);

		try {
			soundFile = new File("gameover.wav");
			in = AudioSystem.getAudioInputStream(soundFile);
			clip = AudioSystem.getClip();
			clip.open(in);
			clip.start();
			clip.loop(Clip.LOOP_CONTINUOUSLY);
			// in = new FileInputStream("gameover.wav");
			// as = new AudioStream(in);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// AudioPlayer.player.start(as);

		try {
			fileIn = new Scanner(new FileReader("score"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		for (int counter = 0; fileIn.hasNext(); counter++) {
			names[counter] = fileIn.next();
			highScore[counter] = fileIn.nextInt();
		}
		String name = JOptionPane.showInputDialog(null, "Enter Name:", "Game Over", JOptionPane.OK_CANCEL_OPTION);

		if (name != null && name.length() > 0) {
			try {
				output = new PrintWriter(file);
			} catch (FileNotFoundException ex) {
				System.out.println("Cannot open");
				System.exit(1);
			}

			for (int counter = 0, next = 0; next != 1; counter++) {
				if (points >= highScore[counter]) {
					place = counter;
					for (int counter1 = 4; counter1 > place; counter1--) {
						highScore[counter1] = highScore[counter1 - 1];
					}
					for (int counter1 = 4; counter1 > place; counter1--) {
						names[counter1] = names[counter1 - 1];
					}
					highScore[counter] = points;
					names[counter] = name;
					next = 1;
				}
			}

			for (int counter = 0; counter < 5; counter++) {
				output.format("%10s %9s", names[counter], highScore[counter] + "\n");
			}

			output.close();

			gameOverBox("Back to Main Menu?");
		} else {
			gameOverBox("Back to Main Menu?");
		}

	}

	/**
	 * A method that creates a JOptionPane with a game over option to quit or go to
	 * the start menu
	 * 
	 * @param messageBox
	 */
	public static void gameOverBox(String messageBox) {
		int box = JOptionPane.showConfirmDialog(null, messageBox, "Game Over", JOptionPane.YES_NO_OPTION);
		if (box == JOptionPane.YES_OPTION) {
			try {
				soundFile = new File("Start Song.wav");
				in = AudioSystem.getAudioInputStream(soundFile);
				clip = AudioSystem.getClip();
				clip.open(in);
				clip.start();
				clip.loop(Clip.LOOP_CONTINUOUSLY);
				// in = new FileInputStream("Start Song.wav");
				// as = new AudioStream(in);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// AudioPlayer.player.start(as);
			startMenu();
			gO = 1;
		} else {
			System.exit(1);
		}
	}

	/**
	 * A method that defines the rules of the game when the player clicks on the
	 * keyboard
	 */
	public static void rules() {
		if (p[counter2].col == 1) {
			if (p[counter2].place == 0 && b.getKey() == 'a') {
				b.removePeg(p[counter2].row, p[counter2].col);
				counter2++;
				b.setKey('q');
				points++;
			}
			if (p[counter2].place == 1 && b.getKey() == 's') {
				b.removePeg(p[counter2].row, p[counter2].col);
				counter2++;
				b.setKey('q');
				points++;
			}
			if (p[counter2].place == 2 && b.getKey() == 'd') {
				b.removePeg(p[counter2].row, p[counter2].col);
				counter2++;
				b.setKey('q');
				points++;
			}
			if (p[counter2].place == 3 && b.getKey() == 'f') {
				b.removePeg(p[counter2].row, p[counter2].col);
				counter2++;
				b.setKey('q');
				points++;
			}
		}
	}

	/**
	 * A constructor that starts the game by accessing the startMenu() and Start
	 * Song
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			soundFile = new File("Start Song.wav");
			in = AudioSystem.getAudioInputStream(soundFile);
			clip = AudioSystem.getClip();
			clip.open(in);
			clip.start();
			clip.loop(Clip.LOOP_CONTINUOUSLY);
			// in = new FileInputStream("Start Song.wav");
			// as = new AudioStream(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// AudioPlayer.player.start(as);
		startMenu();
	}

	/**
	 * A method that runs the pegs throughout the game
	 */
	public static void play() {
		firstTime = false;
		do {
			try {
				soundFile = new File(song);
				in = AudioSystem.getAudioInputStream(soundFile);
				clip = AudioSystem.getClip();
				clip.open(in);
				clip.start();
				clip.loop(Clip.LOOP_CONTINUOUSLY);
				// in = new FileInputStream(song);
				// as = new AudioStream(in);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// AudioPlayer.player.start(as);

			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 10; j++) {
					b.removePeg(i, j);
				}
			}

			boolean gameOver = false;
			int lives = 3;
			int speed = 200;
			int spawnTime = 5;
			b.displayMessage("Lives: " + lives + "  Points: " + points);
			timeCount = 0;
			randomKey = 5;
			counter = -1;
			counter2 = 0;
			points = 0;

			while (!gameOver) {
				p[counter + 1] = new Pegs();
				timeCount++;
				if (timeCount == spawnTime) {
					counter++;
					p[counter].put();
					timeCount = 0;
				}

				if (p[counter2].col == 0) {
					counter2++;
					lives--;
					b.displayMessage("Lives: " + lives + "  Points: " + points);
				}

				try {
					Thread.sleep(speed);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
				rules();
				b.displayMessage("Lives: " + lives + "  Points: " + points);

				if (lives == 0) {
					gameOver = true;
					scoreSaver();
				} else if (points == 200) {
					gameOver = true;
					scoreSaver();
				}

				if (points == 20) {
					spawnTime = 4;
				} else if (points == 40) {
					spawnTime = 3;
					speed = 250;
				} else if (points == 50) {
					speed = 225;
				} else if (points == 80) {
					speed = 200;
				} else if (points == 100) {
					speed = 175;
				} else if (points == 120) {
					speed = 150;
				} else if (points == 140) {
					speed = 150;
				} else if (points == 160) {
					speed = 125;
				} else if (points == 180) {
					speed = 100;
				}

				for (int i = counter2; i <= counter; i++) {
					p[i].move();
				}
				if (counter2 > 0) {
					if (p[counter2 - 1].col - 1 == -1) {
						p[counter2 - 1].move();
					}
				}
			}
		} while (gO == 1);

		System.exit(1);
	}

}
