import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;


public class BlockGame {
	
	static class MyFrame extends JFrame {
		
		static int BALL_WIDTH = 20;
		static int BALL_HEIGHT = 20;
		static int BLOCK_ROWS = 5;
		static int BLOCK_COLUMNS = 10;
		static int BLOCK_WIDTH = 40;
		static int BLOCK_HEIGHT = 20;
		static int BLOCK_GAP = 3;
		static int BAR_WIDTH = 80;
		static int BAR_HEIGHT = 20;
		static int CANVAS_WIDTH = 413 + (BLOCK_GAP*BLOCK_COLUMNS)-BLOCK_GAP;
		static int CANVAS_HEIGHT = 600;
		
		static MyPanel mypanel = null;
		static int score = 0;
		static Timer timer = null;
		static Block[][] blocks = new Block[BLOCK_ROWS][BLOCK_COLUMNS];
		static Bar bar = new Bar();
		static Ball ball = new Ball();
		static int barXTarget = bar.x;
		static int path = 0; 
		static int ballSpeed = 5;
		static boolean isGameFinish = false;
		
		static class Ball {
			int x= CANVAS_WIDTH / 2 - BALL_WIDTH/2;
			int y = CANVAS_HEIGHT/2 - BALL_HEIGHT/2;
			int width = BALL_WIDTH;
			int height = BALL_HEIGHT;
			
			Point getCenter() {
				return new Point( x + (BALL_WIDTH/2), y+(BALL_HEIGHT/2));
			}
			Point getBottomCenter() {
				return new Point( x + (BALL_WIDTH/2), y + (BALL_HEIGHT));
			}
			Point getTopCenter() {
				return new Point( x + (BALL_WIDTH/2), y);
			}
			Point getLeftCenter() {
				return new Point( x , y+(BALL_HEIGHT/2));
			}
			Point getRightCenter() {
				return new Point( x + (BALL_WIDTH), y + (BALL_HEIGHT/2));
			}
		}
		
		static class Bar {
			int x = CANVAS_WIDTH/2 - BAR_WIDTH/2;
			int y = CANVAS_HEIGHT - 100;
			int width = BAR_WIDTH;
			int height = BAR_HEIGHT;
		}
		
		static class Block {
			int x = 0;
			int y = 0;
			int width = BLOCK_WIDTH;
			int height = BLOCK_HEIGHT;
			int color = 0;
			boolean isHidden = false;
		}
		
		
		static class MyPanel extends JPanel {
			public MyPanel() {
				this.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);
				this.setBackground(Color.BLACK);
			}
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				Graphics2D g2d = (Graphics2D)g;
				
				drawUI( g2d );
			}
			private void drawUI(Graphics g2d) {
				for(int i = 0; i <BLOCK_ROWS; i++) {
					for(int j = 0; j < BLOCK_COLUMNS; j++) {
						if(blocks[i][j].isHidden) {
							continue;
						}
						if(blocks[i][j].color == 0) {
							g2d.setColor(Color.WHITE);
						}
						else if(blocks[i][j].color == 1) {
							g2d.setColor(Color.YELLOW);
						}
						else if(blocks[i][j].color == 2) {
							g2d.setColor(Color.BLUE);
						}
						else if(blocks[i][j].color == 3) {
							g2d.setColor(Color.MAGENTA);
						}
						else if(blocks[i][j].color == 4) {
							g2d.setColor(Color.RED);
						}
						g2d.fillRect(blocks[i][j].x, blocks[i][j].y, blocks[i][j].width,  blocks[i][j].height);
					}
					
					g2d.setColor(Color.WHITE);
					g2d.setFont(new Font("TimesRoman", Font.BOLD, 20));
					if( isGameFinish ) {
						g2d.drawString("점수 : " + score, CANVAS_WIDTH/3+30, CANVAS_HEIGHT/2-200);
						g2d.drawString("GAME CLEAR!", CANVAS_WIDTH/3, CANVAS_HEIGHT/2-100);
					}
					else {
						g2d.drawString("점수 : " + score, CANVAS_WIDTH/2-45, 20);
					}
					
				
					
					g2d.setColor(Color.WHITE);
					g2d.fillOval(ball.x, ball.y, BALL_WIDTH, BALL_HEIGHT);
					
					
					g2d.setColor(Color.WHITE);
					g2d.fillRect(bar.x, bar.y, BAR_WIDTH, BAR_HEIGHT);
				}
			}
		}
		
		public MyFrame(String title) {
			super(title);
			this.setVisible(true);
			this.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);
			this.setLocation(400, 300);
			this.setLayout(new BorderLayout());
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			initData();
			
			MyPanel myPanel = new MyPanel();
			this.add("Center", myPanel);
			
			addKeyListener(new GameKeyListener());
			startTimer();
		}
		
		public void initData() {
			for(int i = 0; i <BLOCK_ROWS; i++) {
				for(int j = 0; j < BLOCK_COLUMNS; j++) {
					blocks[i][j] = new Block();
					blocks[i][j].x = BLOCK_WIDTH*j+BLOCK_GAP*j;
					blocks[i][j].y = 100 + BLOCK_HEIGHT*i+BLOCK_GAP*i;
					blocks[i][j].width = BLOCK_WIDTH;
					blocks[i][j].height = BLOCK_HEIGHT;
					blocks[i][j].color = 4-i;
					blocks[i][j].isHidden = false;
				}
			}
		}
		
		class GameKeyListener implements KeyListener {

			@Override
			public void keyTyped(KeyEvent e) {
				
			}

			@Override
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {
					case KeyEvent.VK_LEFT:
						bar.x= bar.x-15;
						break;
					case KeyEvent.VK_RIGHT:
						bar.x = bar.x+15;
						break;
				}
				repaint();
			}

			@Override
			public void keyReleased(KeyEvent e) {
				
			}
			
		}
		public void startTimer() {
			timer = new Timer(20, (ActionListener) new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					movement();
					checkCollision();
					checkCollisionBlock();
					repaint();
					
					isGameFinish();
				}

			});
			timer.start();
		}
		public void isGameFinish() {
			int count = 0;
			for(int i = 0; i < BLOCK_ROWS; i++) {
				for(int j = 0; j<BLOCK_COLUMNS; j++) {
					Block block = blocks[i][j];
					if( block.isHidden )
						count++;
				}
			}
			if (count == BLOCK_ROWS * BLOCK_COLUMNS) {
				timer.stop();
				isGameFinish = true;
			}
		}
		public void movement() {
			if(path == 0) { //0 : up-right
				ball.x += ballSpeed;
				ball.y -= ballSpeed;
			}else if(path==1) { //1 : down-right
				ball.x += ballSpeed;
				ball.y += ballSpeed;
			}else if(path==2) { //2 : up-left
				ball.x -= ballSpeed;
				ball.y -= ballSpeed;
			}else if(path==3) { //3 : down-left
				ball.x -= ballSpeed;
				ball.y += ballSpeed;
			}
		}
		public boolean duplRect(Rectangle rect1, Rectangle rect2) {
			return rect1.intersects(rect2); //rect1과 rect2이 서로 중복되는지
		}
		public void checkCollision() {
			if(path == 0) { //0 : up-right
				if(ball.y < 0) {
					path = 1;
				}
				if(ball.x > CANVAS_WIDTH-BALL_WIDTH*2) {
					path = 2;
				}
			}else if(path==1) { //1 : down-right
				if(ball.x>CANVAS_WIDTH-BALL_WIDTH*2) {
					path = 3;
				}
				if(ball.y>CANVAS_HEIGHT-BALL_HEIGHT*3) {
					
					path = 0;
					ball.x = CANVAS_WIDTH/2;
					ball.y = CANVAS_HEIGHT/2;
					score = 0;
				}
				if(ball.getBottomCenter().y >= bar.y) {
					if ( duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height), new Rectangle(bar.x, bar.y, bar.width, bar.height)))  {
						path = 0;
					}
				}
				if(ball.y  == CANVAS_HEIGHT) {
					ball.x = CANVAS_WIDTH/2;
					ball.y = CANVAS_HEIGHT/2;
					score = 0;
				}
			}else if(path==2) { //2 : up-left
				if(ball.x < 0) {
					path = 0;
				}
				if(ball.y<0) {
					path = 3;
				}
			}else if(path==3) { //3 : down-left
				if(ball.x < 0) {
					path = 1;
				}
				if(ball.y > CANVAS_HEIGHT-BALL_HEIGHT*3) {

					path = 0;
					ball.x = CANVAS_WIDTH/2;
					ball.y = CANVAS_HEIGHT/2;
					score = 0;
				}
				if(ball.getBottomCenter().y >= bar.y) {
					if ( duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height), new Rectangle(bar.x, bar.y, bar.width, bar.height)))  {
						path = 2;
					}
				}
				
			}
		}
		public void checkCollisionBlock(){
			for(int i = 0; i< BLOCK_ROWS; i++) {
				for(int j = 0; j < BLOCK_COLUMNS; j++) {
					Block block = blocks[i][j];
					if(block.isHidden == false) {
						if(path==0) {
							if ( duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height), new Rectangle(block.x, block.y, block.width, block.height)))  {
								if(ball.getRightCenter().x >= block.x+1 && ball.getRightCenter().x <= block.x + BLOCK_WIDTH+1) {
									path = 1;
								}
								else if(ball.y >= block.y-1 && ball.getRightCenter().x + BALL_WIDTH == block.x ){
									path = 2;
								}
								block.isHidden = true;
								if(block.color == 0) score += 5;
								else if(block.color == 1) score += 10;
								else if(block.color == 2) score += 30;
								else if(block.color == 3) score += 50;
								else if(block.color == 4) score += 100;
								
						}
						}
						else if(path == 1) {
							if ( duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height), new Rectangle(block.x, block.y, block.width, block.height)))  {
								if(ball.getRightCenter().x >= block.x + 1 && ball.getRightCenter().x <= block.x + BLOCK_WIDTH+1) {
									path = 0;
								}
								else if(ball.y >= block.y-1 && ball.getRightCenter().x + BALL_WIDTH == block.x){
									path = 3;
								}
								block.isHidden = true;
								if(block.color == 0) score += 5;
								else if(block.color == 1) score += 10;
								else if(block.color == 2) score += 30;
								else if(block.color == 3) score += 50;
								else if(block.color == 4) score += 100;
							}	
						}
						else if(path == 2) {
							if ( duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height), new Rectangle(block.x, block.y, block.width, block.height)))  {
								if(ball.getRightCenter().x >= block.x + 1 && ball.getRightCenter().x <= block.x + BLOCK_WIDTH+1) {
									path = 3;
								}
								else if(ball.y >= block.y-1 && ball.getLeftCenter().x == block.x + BLOCK_WIDTH){
									path = 0;
								}
								block.isHidden = true;
								if(block.color == 0) score += 5;
								else if(block.color == 1) score += 10;
								else if(block.color == 2) score += 30;
								else if(block.color == 3) score += 50;
								else if(block.color == 4) score += 100;
							}	
						}
						else if(path == 3) {
							if ( duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height), new Rectangle(block.x, block.y, block.width, block.height)))  {
								if(ball.getRightCenter().x >= block.x + 1 && ball.getRightCenter().x <= block.x + BLOCK_WIDTH+1) {
									path = 2;
								}
								else if(ball.y > block.y-1 && ball.getLeftCenter().x == block.x + BLOCK_WIDTH){
									path = 1;
								}
								block.isHidden = true;
								if(block.color == 0) score += 5;
								else if(block.color == 1) score += 10;
								else if(block.color == 2) score += 30;
								else if(block.color == 3) score += 50;
								else if(block.color == 4) score += 100;
							}	
						}
					}
				}
			}
		}
	}

	public void go() {
		new MyFrame("Block Game");
	}
}
