/*
 * Originally written by Chris 'iPeer' Wignall and Roxanne Newman of SiMPLE STUDIOS
 * Feel free to use this engine in your games and modify to your needs.
 * Just don't do evil.
 */

package com.simple.sjge.engine;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;

import com.simple.sjge.gfx.Colour;
import com.simple.sjge.gui.Gui;
import com.simple.sjge.util.Debug;

public class Engine extends Canvas implements Runnable {

	private static final long serialVersionUID = 1341257396583356312L;
	private static final String GAME_TITLE = "SJGE";
	private static final int BUFFER_LEVEL = 2;
	private static final int HEIGHT_RATIO = 9;
	private static final int WIDTH_RATIO = 16;
	private static final int GAME_WIDTH = 1024;
	private static final int GAME_HEIGHT = GAME_WIDTH * HEIGHT_RATIO / WIDTH_RATIO;
	private static final int IDLE_FRAME_LIMIT = 60;
	private static final double TICKS_PER_SECOND = 60.0;

	private static Engine engine;
	private KeyboardHandler input;
	static boolean GAME_RUNNING = false;
	
	private Gui currentGui = null;
	
	public static boolean DEBUG_ENABLED = false;

	public Engine() {
		// Class init
	}
	
	public static void main(String[] args) {
		if (args.length > 0)
			for (String line : args)
				if (line.startsWith("-debug="))
					DEBUG_ENABLED = Boolean.parseBoolean(line.split("=")[1]);
		engine = new Engine();
		Frame frame = new Frame(GAME_TITLE);
		engine.setPreferredSize(new Dimension(GAME_WIDTH - 10, GAME_HEIGHT - 10));
		frame.setLayout(new BorderLayout());
		frame.addWindowListener(new iWindowListener(engine));
		Debug.p("Added Window listener.");
		engine.addMouseListener(new iMouseListener(engine));
		Debug.p("Added Mouse listener.");
		engine.addMouseMotionListener(new iMouseMotionListener(engine));
		Debug.p("Added Mouse Motion listener.");
		engine.addComponentListener(new iComponentListener(engine));
		Debug.p("Added Component listener.");
		frame.add(engine, "Center");
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);
		engine.requestFocus();
		engine.start();
	}
	
	public void start() {
		try {
			new Thread(this).start();
			GAME_RUNNING = true;
		}
		catch (Exception e) {
			Debug.p("Unable to start Engine Thread!");
			System.exit(0);
		}
	}
	
	public void stop() {
		GAME_RUNNING = false;
		engine.stop();
	}
	
	public void init() {
		input = new KeyboardHandler(this);
	}
	
	public void run() {
		int ticks = 0;
		int frames = 0;
		long lastTime = System.nanoTime();
		double processQueue = 0.0;
		double ticksPerLoop = 1000000000 / TICKS_PER_SECOND;
		long lastTick = System.currentTimeMillis();
		init();
		while (GAME_RUNNING) {
			long now = System.nanoTime();
			processQueue += (double)(now - lastTime) / ticksPerLoop;
			lastTime = now;
			boolean shouldRender;
			for (shouldRender = true; processQueue >= 1.0; shouldRender = true) { // Tick
				ticks++;
				tick();
				processQueue--;
			}
			if (!hasFocus()) {
				try {
					Thread.sleep(1000 / IDLE_FRAME_LIMIT);
				} 
				catch (InterruptedException e) {
					Debug.p("Unable to enforece idle frame limit");
					e.printStackTrace();
				}
			}
			if (shouldRender) {
				render();
				frames++;
			}
			if (System.currentTimeMillis() - lastTick > 1000L) {
				System.out.println(frames+" fps, "+ticks+" ticks");
				frames = ticks = 0;
				lastTick = System.currentTimeMillis();
			}
		}
	}
	
	public void tick() {
		
		input.tick();
		
		if (currentGui != null)
			currentGui.tick();
		
	}
	
	public void render() { 
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(BUFFER_LEVEL);
			requestFocus();
			return;
		}
		
		Graphics2D g = (Graphics2D)bs.getDrawGraphics();
		g.setColor(Colour.BLACK);
		g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
		
		// Game rendering
		
		if (currentGui != null)
			currentGui.render();
		
		g.dispose();
		bs.show();
		
	}
	
	public void setGui(Gui gui) {
		this.currentGui = gui;
	}
	
}