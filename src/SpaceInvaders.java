import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JApplet;
import javax.swing.JPanel;

enum GameState {
	INIT, FIRED, HITED, FINISHED;
}

public class SpaceInvaders extends JApplet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3212324234L;
	private final int SCREEN_SIZE = 400;
	private final int ENMEY_SIZE= 20;
	private final int LAUNCHER_SIZE= 20;
	private final int BULLET_SPEED = 1;
	private final int SCREEN_REFRESH_RATE = 24;	
	private final int ENMEY_OFFSET = 20;  //density of enmey
	
	private final String IMG_BOOM_RED = "images/boom_red.jpg";
	private final String IMG_BULLET_RED = "images/bullet_red.jpg";
	private final String IMG_ENMEY_WHITE = "images/enmey_white.jpg";
	private final String IMG_GAMEOVER = "images/gameover.jpg";
	private final String IMG_LAUNCHER_GREEN = "images/launcher_green.jpg";
	private final String IMG_LV_UP = "images/lv_up.jpg";
	private final String IMG_START = "images/start.jpg";
	
	
	
	private final int NUMBER_OF_ENMEY = 10;

	private static int showRandomInteger(int aStart, int aEnd, Random aRandom) {
		if (aStart > aEnd) {
		}
		// get the range, casting to long to avoid overflow problems
		long range = (long) aEnd - (long) aStart + 1;
		// compute a fraction of the range, 0 <= frac < range
		long fraction = (long) (range * aRandom.nextDouble());
		int randomNumber = (int) (fraction + aStart);

		return randomNumber;
	}

	private static ArrayList<Integer> listRandomInt(int min, int max, int numOfInt, int offset) {
		ArrayList<Integer> randomIntList = new ArrayList<Integer>();
		Random randomGenerator = new Random();
		int randomInt = showRandomInteger(min, max, randomGenerator);
		randomIntList.add(randomInt);

		while (randomIntList.size() <= numOfInt) {
			int count = 0;
			randomInt = showRandomInteger(min, max, randomGenerator);
			
			//check new int is within offset
			for (int i = 0; i < randomIntList.size(); i++) {
				if (randomInt < randomIntList.get(i) - offset
						|| randomInt > randomIntList.get(i) + offset) {
					count++;
				}
			}
			if (count == randomIntList.size()) {
				randomIntList.add(randomInt);
			}
		}
		return randomIntList;
	}

	public class EnemyThread extends Thread {
		int x_position = 0;
		int y_position = 0;
		int size;
		GameState state;

		public EnemyThread(int x, int y) {
			this.x_position = x;
			this.y_position = y;
			this.size = ENMEY_SIZE;
			this.state = GameState.INIT;
		}

		public void run() {
			this.state = GameState.FIRED;
			try {
				while (!interrupted() && this.state != GameState.FINISHED) {
					switch (this.state) {
					case HITED:
						for (int i = 0; i < 2; i++) {
							// hitted enmey goes backward 2 times
							this.y_position--;
							sleep(300);
						}
						this.state = GameState.FINISHED;
						break;
					case FIRED:
						this.y_position++;
						sleep(100); // 100ms
						break;
					default:
						break;
					}
				}
			} catch (InterruptedException e) {
			}
		}

	}
	/**
	 * Thread for refresh screen
	 * @author yangsun
	 *
	 */
	public class TimerThread extends Thread {
		int Time = 0;

		public TimerThread(int t) {
			this.Time = t;
		}

		@Override
		public void run() {
			try {
				while (!interrupted()) {
					sleep(this.Time); 
					repaint();
				}
			} catch (InterruptedException e) {
			}
		}
	}

	public class BulletThread extends Thread {
		int x_position = 0;
		int y_position = 0;
		GameState m_stat;

		public BulletThread(int x, int y) {
			this.x_position = x + LAUNCHER_SIZE/2;
			this.y_position = y - LAUNCHER_SIZE;
			this.m_stat = GameState.INIT;
		}

		@Override
		public void run() {
			this.m_stat = GameState.FIRED;
			try {
				while (!interrupted() && y_position > 10) {
					y_position-=BULLET_SPEED;
					sleep(10); // 10ms
				}
				this.m_stat = GameState.FINISHED;
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public void init() {
		this.setSize(SCREEN_SIZE, SCREEN_SIZE);
		setContentPane(new ContentPane());

	}

	class ContentPane extends JPanel implements KeyListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3905609900670542128L;
		private boolean isStarted = false;
		private Image m_launcher_img, m_em_img, m_lvup_img, m_start_img,
				m_hited_img, m_blt_img, m_gameover_img;

		private int m_x = 100; // Initial coordinates of launcher

		ArrayList<BulletThread> bltList = new ArrayList<BulletThread>();
		ArrayList<EnemyThread> emList = new ArrayList<EnemyThread>();

		TimerThread tt = new TimerThread(1000 / SCREEN_REFRESH_RATE);
		boolean isGameOver = false;

		public ContentPane() {
			super();

			setBackground(Color.BLACK);

			m_launcher_img = getImage(getDocumentBase(),IMG_LAUNCHER_GREEN);
			m_em_img = getImage(getDocumentBase(), IMG_ENMEY_WHITE);
			m_lvup_img = getImage(getDocumentBase(), IMG_LV_UP);
			m_start_img = getImage(getDocumentBase(), IMG_START);
			m_hited_img = getImage(getDocumentBase(), IMG_BOOM_RED);
			m_blt_img = getImage(getDocumentBase(), IMG_BULLET_RED);
			m_gameover_img = getImage(getDocumentBase(), IMG_GAMEOVER);
			// addKeyListener() and setFocusable() must both be included in
			// order to allow key input
			addKeyListener(this);
			setFocusable(true);
		}

		// create enemies
		public void initEnemy() {

			ArrayList<Integer> al = listRandomInt(ENMEY_SIZE/2, SCREEN_SIZE-ENMEY_SIZE/2, NUMBER_OF_ENMEY, ENMEY_OFFSET);
			for (int i : al) {
				emList.add(new EnemyThread(i, 20));
			}

			for (int j = 0; j < 10; j++) {
				al = listRandomInt(10, 390, 10, 20);
				for (int i : al) {
					emList.add(new EnemyThread(i, -20 * j));
				}
			}

		}

		public void hitChecker(ArrayList<BulletThread> btList,
				ArrayList<EnemyThread> etList) {
			outter: for (BulletThread bt : btList) {
				// bullet is finished?
				if (bt.m_stat == GameState.FIRED) {
					for (EnemyThread et : etList) {
						// enemy is hitted?
						if (et.state == GameState.FIRED
								&& (bt.x_position <= et.x_position + et.size
										- 0 && bt.x_position >= et.x_position - 0)
								&& (bt.y_position >= et.y_position && bt.y_position <= et.y_position
										+ et.size)) {
							bt.m_stat = GameState.FINISHED;
							et.state = GameState.HITED;

							continue outter;
						}
					}
				}
			}
		}

		public int hitedEnemy(ArrayList<EnemyThread> etList) {
			int count = 0;
			for (EnemyThread et : etList) {
				if (et.state == GameState.FINISHED)
					count++;
			}
			return count;
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			Font m_smallFont, m_bigFont;
			m_bigFont = new Font("Times New Roman", Font.BOLD, 50);
			m_smallFont = new Font("Times New Roman", Font.BOLD, 15);
			g.setFont(m_smallFont);

			for (EnemyThread et : emList) {
				if (et.y_position > this.getHeight() - 50
						&& et.state == GameState.FIRED) {
					isGameOver = true;
				}
			}

			if (hitedEnemy(emList) == emList.size() && emList.size() != 0) {
				g.drawImage(m_lvup_img, 0, 0, getWidth(), getHeight(), this);
			} else if (this.isStarted == false) {
				g.drawImage(m_start_img, 0, 0, getWidth(), getHeight(), this);
			} else if (this.isGameOver == true) {
				g.setFont(m_bigFont);
				g.drawImage(m_gameover_img, 0, 0, getWidth(), getHeight(), this);

				g.drawString("Score: " + this.hitedEnemy(emList),
						this.getWidth() / 2 - 80, 350);
			}

			else {
				g.setColor(Color.white);
				int width = m_launcher_img.getWidth(this), height = m_launcher_img
						.getHeight(this);
				g.drawImage(m_launcher_img, m_x, this.getHeight() - 20, width,
						height, this);
				hitChecker(bltList, emList);

				// how many bullets have been fired

				g.drawString("Missile Fired: " + bltList.size(),
						this.getWidth() - 120, 10);
				g.drawString("Score:" + this.hitedEnemy(emList), 10, 10);

				for (EnemyThread et : emList) {
					if (et.state == GameState.HITED) {
						int width1 = m_hited_img.getWidth(this), height1 = m_hited_img
								.getHeight(this);

						g.drawImage(m_hited_img, et.x_position, et.y_position,
								width1, height1, this);
					}

					else if (et.state == GameState.FIRED) {
						int width1 = m_em_img.getWidth(this), height1 = m_em_img
								.getHeight(this);
						// g.drawString("*", et.x, et.y);
						if (et.y_position > 15) {
							g.drawImage(m_em_img, et.x_position, et.y_position,
									width1, height1, this);
						}
					}
				}
				for (BulletThread bt : bltList) {
					if (bt.m_stat == GameState.FIRED)
						g.drawImage(m_blt_img, bt.x_position, bt.y_position,
								this);

				}
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {
			// allow user to select a letter
			char c = e.getKeyChar();
			if ((c >= 'A') && (c <= 'z')) {
				// m_message = String.valueOf(c);
			}

			// allow user to use the shift key
			if (e.isShiftDown()) {
				this.setFont(new Font("timesroman", Font.ITALIC, 60));
			} else {
				// this.setBackground(Color.YELLOW);
			}

			// allow user to select an arrow key
			switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT: {
				if (m_x - 1 >= 0)
					m_x--;
				break;
			}
			case KeyEvent.VK_RIGHT: {
				if (m_x + 15 <= this.getWidth())
					m_x++;
				break;
			}
			case KeyEvent.VK_SPACE: {
				break;
			}
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (this.isStarted == false) {
				this.isStarted = true;
				initEnemy();
				tt.start();
				for (EnemyThread et : emList) {
					et.start();
				}
				this.repaint();
			} else {
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					BulletThread blt = new BulletThread(m_x, this.getHeight());
					bltList.add(blt);
					blt.start();
				}
			}

		}

		@Override
		public void keyTyped(KeyEvent e) {
		}
	}
}
