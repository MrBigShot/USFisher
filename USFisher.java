import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.parabot.environment.api.interfaces.Paintable;
import org.parabot.environment.api.utils.Time;
import org.parabot.environment.api.utils.Timer;
import org.parabot.environment.scripts.Category;
import org.parabot.environment.scripts.Script;
import org.parabot.environment.scripts.ScriptManifest;
import org.parabot.environment.scripts.framework.Strategy;
import org.rev317.api.wrappers.hud.Item;
import org.rev317.api.wrappers.interactive.Npc;
import org.rev317.api.wrappers.scene.Area;
import org.rev317.api.wrappers.scene.SceneObject;
import org.rev317.api.wrappers.scene.Tile;
import org.rev317.api.wrappers.walking.TilePath;
import org.rev317.api.events.MessageEvent;
import org.rev317.api.events.listeners.MessageListener;
import org.rev317.api.methods.Camera;
import org.rev317.api.methods.Interfaces;
import org.rev317.api.methods.Inventory;
import org.rev317.api.methods.Npcs;
import org.rev317.api.methods.Players;
import org.rev317.api.methods.SceneObjects;
import org.rev317.api.methods.Skill;

@ScriptManifest( author = "BigShot", category = Category.FISHING, description = "Fishes on UltimateScape 2", name = "USFisher", servers = { "UltimateScape" }, version = 1.1 )
public class USFisher extends Script implements Paintable, MessageListener {
	
	private final ArrayList<Strategy> strategies = new ArrayList<Strategy>();
	public final Tile[] SOUTH_WALK = {new Tile(2608, 3415, 0), new Tile (2604, 3412, 0), new Tile(2600, 3406, 0), new Tile(2594, 3414, 0), new Tile(2586, 3420, 0)};
	public final Tile[] NORTH_WALK = {new Tile (2599, 3422, 0), new Tile(2594, 3417), new Tile(2586, 3420, 0)};
	public final Area northDock = new Area (new Tile (2596, 3420, 0), new Tile(2604, 3420, 0), new Tile(2604, 3425, 0), new Tile(2596, 3425, 0));
	public final Area southDock = new Area (new Tile (2603, 3416, 0), new Tile(2611, 3416), new Tile(2603, 3407, 0), new Tile(2611, 3407, 0));
	public final Area bank = new Area (new Tile(2584, 3417, 0), new Tile(2590, 3417, 0), new Tile(2584, 3423, 0), new Tile(2590, 3423, 0));
	Gui g = new Gui();
	public boolean guiWait = true;
	public int spotID;
	public int useDock;
	public int fishCount;
	public int expCount;
	private int curExp;
	private int startExp;
	public int[] fishIDs = {335, 331, 349, 317, 327, 353, 377, 383, 359, 371};
	public String fishString;
	private final Color color1 = new Color(255, 255, 255);
	private final Font font2 = new Font("Arial", 0, 14);
	private final Timer RUNTIME = new Timer();
	private static Image img1;

	@Override
	public boolean onExecute() {
		g.setVisible(true);
		img1 = getImage("http://i.imgur.com/fRVqz8M.png");
		while (guiWait) {
			Time.sleep(200);
		}
		startExp = Skill.FISHING.getExperience();
		curExp = Skill.FISHING.getExperience();
		Camera.setRotation(45);
		strategies.add(new Walk());
		strategies.add(new Fish());
		strategies.add(new Bank());
		provide(strategies);
		return true;
	}

	@Override
	public void onFinish() {
		
	}
	
	@Override
	public void messageReceived( MessageEvent me ) {
		if (me.getMessage().contains("You catch a")) {
			fishCount += 1;
		}
	}
	
	public void getExpCount() {
		curExp = Skill.FISHING.getExperience();
		expCount = ( curExp - startExp );
		return;
	}
	
	@Override
	public void paint(Graphics arg0) {

		Graphics2D g = (Graphics2D) arg0;
		g.drawImage(img1, 4, 23, null);
		g.setFont(font2);
		g.setColor(color1);
		g.drawString("" + fishCount, 82, 57);
		g.drawString("" + expCount, 82, 70);
		g.drawString("" + RUNTIME, 82, 83);
	}
	
	public static Image getImage(String url) {
		try {
			return ImageIO.read(new URL(url));
		} catch (IOException e) {
			return null;
		}
	}
	
	public class Walk implements Strategy {
		
		SceneObject getSceneObject(int id) {
			SceneObject[] objects = SceneObjects.getAllSceneObjects();

			for (int i = 0; i < objects.length; i++) {
				if (objects[i].getId() == id) {
					return objects[i];
				}
			}
			return null;
		}

		Npc getNpc(int id) {
			Npc[] npcs = Npcs.getNpcs();
			for (int i = 0; i < npcs.length; i++) {
				if (npcs[i].getDef().getId() == id) {
					return npcs[i];
				}
			}
			return null;
		}
		
		@Override
		public boolean activate() {
			SceneObject booth = getSceneObject(2213);
			Npc spot = getNpc(spotID);
			
			if (!Inventory.isFull() &&
					(spot == null || spot.getLocation().distanceTo() > 6)) {
				return true;
			} else if (Inventory.isFull()
					&& (booth == null || booth.getLocation().distanceTo() > 4)) {
				return true;
			} else
				return false;
		}

		@Override
		public void execute() {
			if (Inventory.isFull()) {
				if (useDock == 1) {
					TilePath path = new TilePath(NORTH_WALK);
					if (path != null) {
						path.traverse();
						Time.sleep(1000);
					}
				} else if (useDock == 2) {
					TilePath path = new TilePath(SOUTH_WALK);
					if (path != null) {
						path.traverse();
						Time.sleep(1000);
					}
				}
			} else if (!Inventory.isFull()) {
				if (useDock == 1) {
					TilePath path = new TilePath(NORTH_WALK).reverse();
					if (path != null) {
						path.traverse();
						Time.sleep(1000);
					}
				} else if (useDock == 2) {
					TilePath path = new TilePath(SOUTH_WALK).reverse();
					if (path != null) {
						path.traverse();
						Time.sleep(1000);
					}
				}
			}
		}
	}
	
	public class Fish implements Strategy {
		
		Npc getNpc(int id) {
			Npc[] npcs = Npcs.getNpcs();
			for (int i = 0; i < npcs.length; i++) {
				if (npcs[i].getDef().getId() == id) {
					return npcs[i];
				}
			}
			return null;
		}
		
		@Override
		public boolean activate() {
			Npc spot = getNpc(spotID);
			return spot != null
					&& !Inventory.isFull()
					&& spot.getLocation().distanceTo() < 6;
		}

		@Override
		public void execute() {
			final Npc fishSpot[] = Npcs.getNearest(spotID);
			final Npc fish = fishSpot[0];
			if (fish != null && fish.isOnScreen() && Players.getLocal().getAnimation() == -1) {
				try {
					getExpCount();
					Npcs.getNearest(spotID)[0].interact(fishString);
				} catch (Exception e) {
					
				}
				Time.sleep(2000);
			} else if (fish != null && !fish.isOnScreen() && Players.getLocal().getAnimation() == -1 && !Players.getLocal().isWalking()) {
				fish.getLocation().clickMM();
				Time.sleep(500);
			}
			while (Players.getLocal().getAnimation() != -1) {
				getExpCount();
				Time.sleep(200);
			}
		}
	}
	
	public class Bank implements Strategy {
		
		SceneObject getSceneObject(int id) {
			SceneObject[] objects = SceneObjects.getAllSceneObjects();

			for (int i = 0; i < objects.length; i++) {
				if (objects[i].getId() == id) {
					return objects[i];
				}
			}
			return null;
		}
		
		@Override
		public boolean activate() {
			SceneObject booth = getSceneObject(2213);
			return (Inventory.isFull()
					&& booth != null
					&& booth.getLocation().distanceTo() < 8
					&& !Players.getLocal().isWalking());
		}

		@Override
		public void execute() {

			final SceneObject Booths[] = SceneObjects.getNearest(2213);
			final SceneObject Banker = Booths[0];
			if (!Interfaces.getInterface(5292).isVisible() && Banker != null) {
				try {
				SceneObjects.getNearest(2213)[0].interact("");
				} catch(Exception e) {
					
				}
				Time.sleep(200);
			} else if (Interfaces.getInterface(5292).isVisible()) {
				Time.sleep(500);
				for (Item i : Inventory.getItems(fishIDs)) {
					try {
						i.interact("Store All");
					} catch(Exception e) {
						break;
					}
				}
				Time.sleep(1000);
			} else if (!Banker.isOnScreen()) {
				Banker.getLocation().clickMM();
			}
			Time.sleep(100);
		}
	}
	
	public class Gui extends JFrame {

		private JPanel contentPane;

		public void main(String[] args) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						Gui frame = new Gui();
						frame.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		
		 public Gui() {
			initComponents();
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setBounds(100, 100, 150, 180);
			contentPane = new JPanel();
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			setContentPane(contentPane);
			contentPane.setLayout(null);

			lblUSFisher = new JLabel("USFisher");
			lblUSFisher.setFont(new Font("Arial", Font.PLAIN, 20));
			lblUSFisher.setBounds(15, 11, 101, 16);
			contentPane.add(lblUSFisher);

			lblWhatFish = new JLabel("Choose Fish");
			lblWhatFish.setBounds(17, 49, 82, 14);
			contentPane.add(lblWhatFish);

			fishToUse = new JComboBox();
			fishToUse.setModel(new DefaultComboBoxModel(new String[] {"Shrimp", "Trout", "Lobster", "Tuna/Swordfish", "Shark"}));
			fishToUse.setBounds(17, 75, 82, 20);
			contentPane.add(fishToUse);

			btnStart = new JButton("Start");
			btnStart.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String chosen = fishToUse.getSelectedItem().toString();
					if(fishToUse.getSelectedItem().equals("Shrimp")) {
						spotID = 320;
						useDock = 2;
						fishString = "Net";
					} else if (fishToUse.getSelectedItem().equals("Trout")) {
						spotID = 328;
						useDock = 1;
						fishString = "Lure";
					} else if (fishToUse.getSelectedItem().equals("Lobster")) {
						spotID = 321;
						useDock = 2;
						fishString = "Cage";
					} else if (fishToUse.getSelectedItem().equals("Tuna/Swordfish")) {
						spotID = 321;
						useDock = 2;
						fishString = "Harpoon";
					} else if (fishToUse.getSelectedItem().equals("Shark")) {
						spotID = 322;
						useDock = 2;
						fishString = "Harpoon";
					} 
					guiWait= false;
					g.dispose();
				}
			});
			btnStart.setBounds(10, 112, 89, 23);
			contentPane.add(btnStart);
		 }
		 private void initComponents() {
			 lblUSFisher = new JLabel();
			 lblWhatFish = new JLabel();
			 fishToUse = new JComboBox();

		 }
		 private JLabel lblUSFisher;
		 private JButton btnStart;
		 private JComboBox fishToUse;
		 private JLabel lblWhatFish;

	}
}
