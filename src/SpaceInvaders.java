
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

public class SpaceInvaders extends JApplet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3212324234L;
	public final int STAT_INIT = 1;
	public final int STAT_FIRED = 2;
	public final int STAT_HITED = 3;
	public final int STAT_FINISHED = 4;

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

	private static ArrayList<Integer> listRandomInt(int min, int max, int amount) {
		ArrayList<Integer> al = new ArrayList<Integer>();
		Random r = new Random();
		int rint = showRandomInteger(min, max, r);
		al.add(rint);

		while (al.size() <= amount) {
			int count = 0;
			rint = showRandomInteger(min, max, r);
			for (int i = 0; i < al.size(); i++) {
				if (rint < al.get(i) - 20 || rint > al.get(i) + 20) {
					count++;
				}
			}
			if (count == al.size()) {
				al.add(rint);
			}
		}
		return al;
	}

	public class EnemyThread extends Thread {
		int x_position = 0;
		int y_position = 0;
		int size;
		int m_stat;

		public EnemyThread(int x, int y) {
			this.x_position = x;
			this.y_position = y;
			this.size = 20;
			this.m_stat = STAT_INIT;
		}

		public EnemyThread(int x, int y, int s) {
			this.x_position = x;
			this.y_position = y;
			this.size = s;
			this.m_stat = STAT_INIT;
		}

		public void run() {
			this.m_stat = STAT_FIRED;
			try {
				while (!interrupted() && this.m_stat != STAT_FINISHED) {
					switch (this.m_stat) {
					case STAT_HITED:
						for (int i = 0; i <= 2; i++) {
							this.y_position--;
							sleep(300);
						}
						this.m_stat = STAT_FINISHED;
						break;
					case STAT_FIRED:
						this.y_position++;
						sleep(100); // 10/1000th = 1/100th second
						break;
					}

				}
			} catch (InterruptedException e) {
			}
		}

	}

	public class TimerThread extends Thread {
		int Time = 0;

		public TimerThread(int t) {
			this.Time = t;
		}

		@Override
		public void run() {
			try {
				while (!interrupted()) {
					sleep(this.Time); // 10/1000th = 1/100th second
					// hitChecker();
					repaint();
				}
			} catch (InterruptedException e) {
			}
		}
	}

	public class BulletThread extends Thread {
		int x_position = 0;
		int y_position = 0;
		int m_stat;

		public BulletThread(int x, int y) {
			this.x_position = x + 10;
			// -20 for launcher height
			this.y_position = y - 20;
			this.m_stat = STAT_INIT;
		}

		@Override
		public void run() {
			this.m_stat = STAT_FIRED;
			try {
				while (!interrupted() && y_position > 10) {
					y_position--;
					sleep(10); // 10/1000th = 1/100th second
				}
				this.m_stat = STAT_FINISHED;
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public void init() {
		this.setSize(400, 400);
		setContentPane(new ContentPane());

	}

	class ContentPane extends JPanel implements KeyListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3905609900670542128L;
		private boolean isStarted = false;
		private Image m_launcher_img, m_em_img, m_lvup_img, m_start_img,
				m_hited_img, m_blt_img,m_gameover_img;

		private int m_x = 100; // Initial coordinates of launcher

		ArrayList<BulletThread> bltList = new ArrayList<BulletThread>();
		ArrayList<EnemyThread> emList = new ArrayList<EnemyThread>();

		TimerThread tt = new TimerThread(1000 / 24);
		boolean isGameOver=false;
		public ContentPane() {
			super();

			setBackground(Color.BLACK);

			m_launcher_img = getImage(getDocumentBase(),
					"images/launcher_green.jpg");
			m_em_img = getImage(getDocumentBase(), "images/enmey_white.jpg");
			m_lvup_img = getImage(getDocumentBase(), "images/lv_up.jpg");
			m_start_img = getImage(getDocumentBase(), "images/start.jpg");
			m_hited_img = getImage(getDocumentBase(), "images/boom_red.jpg");
			m_blt_img = getImage(getDocumentBase(), "images/bullet_red.jpg");
			m_gameover_img  = getImage(getDocumentBase(), "images/gameover.jpg");
			// addKeyListener() and setFocusable() must both be included in
			// order to allow key input
			addKeyListener(this);
			setFocusable(true);
		}

		// creat enemies
		public void initEnemy() {
			Random r;
			ArrayList<Integer> al = listRandomInt(10, 390, 10);
			for (int i : al) {
				emList.add(new EnemyThread(i, 20));
			}

			for (int j = 0; j < 10; j++) {
				r = new Random();
				al = listRandomInt(10, 390, 10);
				for (int i : al) {
					emList.add(new EnemyThread(i, -20 * j));
				}
			}

		}

		public void hitChecker(ArrayList<BulletThread> btList,
				ArrayList<EnemyThread> etList) {
			outter: for (BulletThread bt : btList) {
				// bullet is finished?
				if (bt.m_stat == STAT_FIRED) {
					for (EnemyThread et : etList) {
						// enemy is hited?
						if (et.m_stat == STAT_FIRED
								&& (bt.x_position <= et.x_position + et.size
										- 0 && bt.x_position >= et.x_position - 0)
								&& (bt.y_position >= et.y_position && bt.y_position <= et.y_position
										+ et.size)) {
							bt.m_stat = STAT_FINISHED;
							et.m_stat = STAT_HITED;

							continue outter;
						}
					}
				}
			}
		}

		public int hitedEnemy(ArrayList<EnemyThread> etList) {
			int count = 0;
			for (EnemyThread et : etList) {
				if (et.m_stat == STAT_FINISHED)
					count++;
			}
			return count;
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			Font m_smallFont,
	           m_bigFont;
			m_bigFont = new Font("Times New Roman", Font.BOLD, 50);
			m_smallFont= new Font("Times New Roman", Font.BOLD, 15);
			g.setFont(m_smallFont);
			
			for(EnemyThread et: emList)
			{
				if(et.y_position>this.getHeight()-50&&et.m_stat==STAT_FIRED)
				{
					isGameOver=true;
				}
			}
			
			if (hitedEnemy(emList) == emList.size() && emList.size() != 0) {
				g.drawImage(m_lvup_img, 0, 0, getWidth(), getHeight(), this);
			} else if (this.isStarted == false) {
				g.drawImage(m_start_img, 0, 0, getWidth(), getHeight(), this);
			} else if (this.isGameOver == true) {
				g.setFont(m_bigFont);
				g.drawImage(m_gameover_img, 0, 0, getWidth(), getHeight(), this);
				
				g.drawString("Score: "+this.hitedEnemy(emList), this.getWidth()/2-80, 350);
			}
			
			else {

				g.setColor(Color.white);
				int width = m_launcher_img.getWidth(this), height = m_launcher_img
						.getHeight(this);
				g.drawImage(m_launcher_img, m_x, this.getHeight() - 20, width,
						height, this);
				hitChecker(bltList, emList);

				// how many bullets have been fired
				
				g.drawString("Missile Fired: "+bltList.size(), this.getWidth()-120, 10);
				g.drawString("Score:" + this.hitedEnemy(emList), 10, 10);

				// g.drawString("^", m_x, this.getHeight());
				
				for (EnemyThread et : emList) {
					if (et.m_stat == STAT_HITED) {
						int width1 = m_hited_img.getWidth(this), height1 = m_hited_img
								.getHeight(this);

						g.drawImage(m_hited_img, et.x_position, et.y_position,
								width1, height1, this);
					}

					else if (et.m_stat == STAT_FIRED) {
						int width1 = m_em_img.getWidth(this), height1 = m_em_img
								.getHeight(this);
						// g.drawString("*", et.x, et.y);
						if(et.y_position>15)
						{
							g.drawImage(m_em_img, et.x_position, et.y_position,
									width1, height1, this);
						}
						
					}

				}
				for (BulletThread bt : bltList) {
					if (bt.m_stat == STAT_FIRED)
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
