package cancerwatches;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

public class CancerWatches
{
	public static void main(String args[])
	{
		new CancerWatches();
	}

	public CancerWatches()
	{
		final JFrame frame = new JFrame("Cancer Watches");
		frame.setUndecorated(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setLayout(new BorderLayout());

		final ImagePanel panel = new ImagePanel("background.gif");

		// on ESC key close frame
		frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Cancel");
		frame.getRootPane().getActionMap().put("Cancel", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				frame.setVisible(false);
				System.exit(0);
			}
		});

		// on close window the close method is called
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(java.awt.event.WindowEvent evt)
			{
				frame.setVisible(false);
				System.exit(0);
			}
		});

		frame.add(panel, BorderLayout.CENTER);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);
	}
}

class ImagePanel extends JPanel
{
	private Image	bg;
	private Eye		r_eye;
	private Eye		l_eye;

	private Point	mouse	= new Point(0, 0);

	Timer			t		= new Timer();

	public ImagePanel(String img)
	{
		bg = new ImageIcon(ClassLoader.getSystemResource("background.png")).getImage();
		r_eye = new Eye(new ImageIcon(ClassLoader.getSystemResource("right.png")).getImage(), 126, 86, 37, 60);
		l_eye = new Eye(new ImageIcon(ClassLoader.getSystemResource("left.png")).getImage(), 84, 100, 52, 45);

		Dimension size = new Dimension(bg.getWidth(null), bg.getHeight(null));
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
		setSize(size);

		t.scheduleAtFixedRate(new TimerTask()
		{
			@Override
			public void run()
			{
				mouse = MouseInfo.getPointerInfo().getLocation();
				try
				{
					mouse.x = mouse.x - getLocationOnScreen().x;
					mouse.y = mouse.y - getLocationOnScreen().y;
				}
				catch(Exception e)
				{
				}

				repaint();
			}
		}, 10, 10);
	}

	public void stop()
	{
		t.cancel();
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponents(g);
		Graphics2D g2 = (Graphics2D) g;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2.drawImage(bg, 0, 0, getWidth(), getHeight(), null);

		r_eye.draw(mouse, g2);
		l_eye.draw(mouse, g2);
	}

	class Eye extends Thread
	{
		private Image	img;

		private int		img_w;
		private int		img_h;

		private int		irisDiam;	// Iris size, in pixels

		// Radii of the inner ellipse that the iris slides along
		private int		SMALLXRAD;
		private int		SMALLYRAD;

		private int		x, y;		// Current position of the eye
		private double	newx, newy; // Position of the iris

		public Eye(Image img, int x, int y, int w, int h)
		{
			this.img = img;

			this.x = x;
			this.y = y;

			img_w = img.getWidth(null);
			img_h = img.getHeight(null);

			irisDiam = (int) Math.sqrt((img_w * img_w) + (img_h * img_h)) - 10;

			// Radii of the inner ellipse that the iris slides along
			SMALLXRAD = (w - irisDiam) / 2;
			SMALLYRAD = (h - irisDiam) / 2;
		}

		private void draw(Point mouse, Graphics2D g)
		{
			if(mouse.x == x)
			{ // mouse is vertical above eye center
				newx = mouse.x;

				if(Math.abs(y - mouse.y) >= SMALLYRAD)
				{ // Pointer is outside the eye
					if((y - mouse.y) > 0)
						newy = y - SMALLYRAD;
					else
						newy = y + SMALLYRAD;
				}
				else
					// pointer is in the eye
					newy = mouse.y;
			}
			else
			{
				// Find intersection point of line to mouse with eye ellipse
				double slope = (double) (mouse.y - y) / (double) (mouse.x - x);
				double numerator = SMALLXRAD * SMALLXRAD * SMALLYRAD * SMALLYRAD;
				double denominator = SMALLYRAD * SMALLYRAD + slope * slope * SMALLXRAD * SMALLXRAD;
				newx = Math.sqrt(numerator / denominator);
				newy = slope * newx;

				// Choose appropriate intersection point
				if(mouse.x < x)
					newx = -Math.abs(newx);
				else
					newx = Math.abs(newx);

				if(mouse.y < y)
					newy = -Math.abs(newy);
				else
					newy = Math.abs(newy);

				newx += x;
				newy += y;

				if((double) (mouse.x - x) * (mouse.x - x) / (SMALLXRAD * SMALLXRAD) + (double) (mouse.y - y) * (mouse.y - y) / (SMALLYRAD * SMALLYRAD) < 1)
				{ // Mouse is inside of the eye
					newx = mouse.x;
					newy = mouse.y;
				}
			}

			g.drawImage(img, (int) newx - img_w / 2, (int) newy - img_h / 2, img.getWidth(null), img.getHeight(null), null);
			// Draw the eye outline
			// g.setColor(Color.black);
			// g.drawOval(x - w / 2, y - h / 2, w, h);
		}

		public int getX()
		{
			return x;
		}

		public int getY()
		{
			return y;
		}

		public Image getImage()
		{
			return img;
		}
	}
}
