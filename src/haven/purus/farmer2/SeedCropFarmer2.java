package haven.purus.farmer2;

import haven.Button;
import haven.Label;
import haven.Window;
import haven.*;
import haven.purus.BotUtils;
import haven.purus.pbot.PBotInventory;
import haven.purus.pbot.PBotItem;
import haven.purus.pbot.PBotUtils;
import haven.purus.pbot.PBotWindowAPI;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static haven.OCache.posres;

public class SeedCropFarmer2 extends Window implements Runnable {

	private Coord rc1, rc2;

	private boolean stopThread = false;

	private Label lblProg;

	private int stage;
	private String cropName;
	private String seedName;

	public SeedCropFarmer2(Coord rc1, Coord rc2, String cropName, String seedName, int stage) {
		super(new Coord(120, 65), cropName.substring(cropName.lastIndexOf("/") + 1).substring(0, 1).toUpperCase()
				+ cropName.substring(cropName.lastIndexOf("/") + 1).substring(1) + " Farmer");
		this.rc1 = rc1;
		this.rc2 = rc2;
		this.cropName = cropName;
		this.stage = stage;
		this.seedName = seedName;

		Label lblstxt = new Label("Progress:");
		add(lblstxt, new Coord(15, 35));
		lblProg = new Label("Initialising...");
		add(lblProg, new Coord(65, 35));

		Button stopBtn = new Button(120, "Stop") {
			@Override
			public void click() {
				stop();
			}
		};
		add(stopBtn, new Coord(0, 0));
	}

	private class GobCapacity {
		Gob g;
		int remainingCapacity;
		GobCapacity(Gob g, int remainingCapacity) {
			this.g = g;
			this.remainingCapacity = remainingCapacity;
		}
	}

	private class QualityItemGob implements Comparable {
		Gob g;
		double quality;
		Coord c;
		QualityItemGob(Gob g, Coord c, double quality) {
			this.g = g;
			this.c = c;
			this.quality = quality;
		}

		@Override
		public int compareTo(Object o) {
			QualityItemGob qg = (QualityItemGob)o;
			if(qg.quality > this.quality)
				return -1;
			else if(qg.quality < this.quality)
				return 1;
			if(qg.g.id > this.g.id)
				return 1;
			else if(qg.g.id < this.g.id)
				return -1;
			else
				return qg.c.compareTo(this.c);
		}
	}

	private class QualityItem implements Comparable {
		double quality;
		Coord c;
		QualityItem(Coord c, double quality) {
			this.c = c;
			this.quality = quality;
		}

		@Override
		public int compareTo(Object o) {
			QualityItem qg = (QualityItem)o;
			if(qg.quality > this.quality)
				return -1;
			else if(qg.quality < this.quality)
				return 1;
			else
				return qg.c.compareTo(this.c);
		}
	}

	public void run() {
		try {
			// Initialise crop list
			List<Gob> crops = Gobs(cropName).stream().filter((gob)-> gob.getStage() == stage).collect(Collectors.toList());
			ArrayList<GobCapacity> troughs = new ArrayList<>();
			ArrayList<GobCapacity> freeChests = new ArrayList<>();

			TreeSet<QualityItemGob> chestCarrots = new TreeSet<>();

			int totalCrops = crops.size();
			int cropsHarvested = 0;
			lblProg.settext(cropsHarvested + "/" + totalCrops);
			ArrayList<Coord> replantTiles = new ArrayList<>();

			for(Gob g : Gobs("gfx/terobjs/trough")) {
				BotUtils.pfGobClick(g, 3,0);
				while(PBotWindowAPI.getTooltips(PBotWindowAPI.waitForWindow("Trough")).size() == 0)
					PBotUtils.sleep(15);
				String tooltip = PBotWindowAPI.getTooltips(PBotWindowAPI.waitForWindow("Trough")).get(0);
				int remainingCapacity = 2000-(int)Math.floor(Float.parseFloat(tooltip.split(" ")[1].split("/")[0]));
				troughs.add(new GobCapacity(g, remainingCapacity));
			}
			for(Gob g : Gobs("gfx/terobjs/chest")) {
				BotUtils.pfGobClick(g, 3, 0);
				Window wnd = PBotWindowAPI.waitForWindow("Chest");
				PBotInventory inv = PBotWindowAPI.getInventories(wnd).get(0);
				int remainingCapacity = inv.freeSlotsInv();
				if(remainingCapacity > 0) {
					freeChests.add(new GobCapacity(g, remainingCapacity));
				}
				PBotUtils.sleep(100);
				for(PBotItem itm : inv.getInventoryItemsByResnames("gfx/invobjs/carrot")) {
					chestCarrots.add(new QualityItemGob(g, itm.getInvLoc(), itm.getQuality2()));
				}
			}

			for(Gob g : crops) {
				if(stopThread)
					return;
				// Check if stamina is under 30%, drink if so
				GameUI gui = HavenPanel.lui.root.findchild(GameUI.class);
				IMeter.Meter stam = gui.getmeter("stam", 0);
				if(stam.a <= 30) {
					BotUtils.drink();
				}

				if(stopThread)
					return;

				// Walk to the crop
				BotUtils.pfLeftClick(g.rc.floor());

				if(stopThread)
					return;

				BotUtils.doClick(g, 3, 0);
				// Wait for harvest menu to appear
				while(ui.root.findchild(FlowerMenu.class) == null) {
					if(stopThread)
						return;
					BotUtils.sleep(10);
				}

				if(BotUtils.invFreeSlots() < 4 || totalCrops-cropsHarvested == 0) { // Replant to previous tiles and handle carrots to trough & chests TODO FIXME also replant in the endd..
					PBotUtils.closeFlowermenu();
					PBotInventory inv = PBotUtils.playerInventory();

					if(stopThread)
						return;

					// Get HQ carrots
					ArrayList<QualityItem> invCarrots = new ArrayList<>();
					for(PBotItem itm : inv.getInventoryItemsByResnames("gfx/invobjs/carrot")) {
						invCarrots.add(new QualityItem(itm.getInvLoc(), itm.getQuality2()));
					}
					invCarrots.sort(QualityItem::compareTo);
					ArrayList<QualityItem> carrotsToPlant = new ArrayList<>();
					long openGobId = -1;
					for(int i=0; i<replantTiles.size(); i++) {
						if(chestCarrots.size() != 0 && invCarrots.get(invCarrots.size()-1).quality + 0.001 < chestCarrots.last().quality) {
							if(openGobId != chestCarrots.last().g.id) {
								openGobId = chestCarrots.last().g.id;
								BotUtils.pfGobClick(BotUtils.findObjectById(chestCarrots.last().g.id), 3, 0);
								if(stopThread)
									return;
								PBotWindowAPI.waitForWindow("Chest", 1000000);
							}
							PBotUtils.playerInventory().getItemFromInventoryAtLocation(invCarrots.get(invCarrots.size()-1).c.x, invCarrots.get(invCarrots.size()-1).c.y).takeItem(false);
							PBotWindowAPI.getInventories(PBotWindowAPI.waitForWindow("Chest", 1000000)).get(0).dropItemToInventory(chestCarrots.last().c);
							QualityItemGob qig = chestCarrots.last();
							chestCarrots.remove(chestCarrots.last());
							qig.quality = invCarrots.get(invCarrots.size()-1).quality;
							chestCarrots.add(qig);
							PBotUtils.playerInventory().dropItemToInventory(invCarrots.get(invCarrots.size()-1).c);
							if(stopThread)
								return;
						} else {
							// Inv higher q, plant from inv
						}
						carrotsToPlant.add(invCarrots.get(invCarrots.size()-1));
						invCarrots.remove(invCarrots.size()-1);
					}
					// Plant
					for(Coord tileCoord : replantTiles) {
						PBotUtils.pfLeftClick(tileCoord.x, tileCoord.y);
						if(stopThread)
							return;
						PBotUtils.playerInventory().getItemFromInventoryAtLocation(carrotsToPlant.get(carrotsToPlant.size()-1).c.x,carrotsToPlant.get(carrotsToPlant.size()-1).c.y).takeItem(true);
						carrotsToPlant.remove(carrotsToPlant.size()-1);
						BotUtils.mapInteractClick(0);
						while(BotUtils.findNearestStageCrop(2, 0, cropName) == null && (BotUtils.getItemAtHand() != null)) {
							if(stopThread)
								return;
							BotUtils.sleep(10);
						}
					}
					replantTiles.clear();
					openGobId = -1;
					// Store remaining to chests/troughs
					ArrayList<Coord> freeCoords = new ArrayList<>();
					ArrayList<QualityItem> troughCarrots = new ArrayList<>();
					for(QualityItem itm : invCarrots) {
						if(!freeChests.isEmpty()) {
							if(openGobId != freeChests.get(freeChests.size() - 1).g.id) {
								BotUtils.pfGobClick(BotUtils.findObjectById(freeChests.get(freeChests.size() - 1).g.id), 3, 0);
								openGobId = freeChests.get(freeChests.size() - 1).g.id;
								Window wnd = PBotWindowAPI.waitForWindow("Chest", 1000000);
								if(stopThread)
									return;
								freeCoords.clear();
								int[][] cmat = PBotWindowAPI.getInventories(wnd).get(0).containerMatrix();
								for(int i = 0; i < cmat.length; i++) {
									for(int j = 0; j < cmat[0].length; j++) {
										if(cmat[j][i] == 0) {
											freeCoords.add(new Coord(j, i));
										}
									}
								}
							}
							if(stopThread)
								return;
							PBotUtils.playerInventory().getItemFromInventoryAtLocation(itm.c.x, itm.c.y).takeItem(false);
							PBotWindowAPI.getInventories(PBotWindowAPI.waitForWindow("Chest", 1000000)).get(0).dropItemToInventory(freeCoords.get(freeCoords.size() - 1));
							if(stopThread)
								return;
							chestCarrots.add(new QualityItemGob(freeChests.get(freeChests.size() - 1).g, freeCoords.get(freeCoords.size() - 1), itm.quality));
							freeCoords.remove(freeCoords.size() - 1);
							if(freeChests.get(freeChests.size() - 1).remainingCapacity-- == 1)
								freeChests.remove(freeChests.get(freeChests.size() - 1));
						} else {
							if(!chestCarrots.isEmpty() && chestCarrots.first().quality + 0.001 < itm.quality) {
								if(openGobId != chestCarrots.first().g.id) {
									BotUtils.pfGobClick(BotUtils.findObjectById(chestCarrots.first().g.id), 3, 0);
									openGobId = chestCarrots.first().g.id;
								}
								if(stopThread)
									return;
								Window wnd = PBotWindowAPI.waitForWindow("Chest", 1000000);
								PBotUtils.playerInventory().getItemFromInventoryAtLocation(itm.c.x, itm.c.y).takeItem(false);
								PBotWindowAPI.getInventories(wnd).get(0).dropItemToInventory(chestCarrots.first().c);
								QualityItemGob qig = chestCarrots.first();
								chestCarrots.remove(chestCarrots.first());
								qig.quality = itm.quality;
								chestCarrots.add(qig);
								PBotUtils.playerInventory().dropItemToInventory(itm.c);
							}
							troughCarrots.add(itm);
						}
					}

					for(QualityItem itm : troughCarrots) {
						if(!troughs.isEmpty()) {
							if(openGobId != troughs.get(troughs.size() - 1).g.id) {
								BotUtils.pfGobClick(troughs.get(troughs.size() - 1).g, 3, 0);
								openGobId = troughs.get(troughs.size() - 1).g.id;
								PBotWindowAPI.waitForWindow("Trough");
							}
							if(stopThread)
								return;
							PBotUtils.playerInventory().getItemFromInventoryAtLocation(itm.c.x, itm.c.y).takeItem(true);
							BotUtils.itemClick(BotUtils.findObjectById(troughs.get(troughs.size() - 1).g.id), 0);
							while(PBotUtils.getItemAtHand() != null)
								PBotUtils.sleep(5);
							if(troughs.get(troughs.size() - 1).remainingCapacity-- == 1) {
								troughs.remove(troughs.size() - 1);
							}
						} else {
							break;
						}
					}

					// Drop remaining ones if trough/chests are full
					for(Widget w = BotUtils.playerInventory().child; w != null; w = w.next) {
						if(w instanceof GItem && ((GItem) w).resource().name.equals(seedName)) {
							GItem item = (GItem) w;
							try {
								item.wdgmsg("drop", Coord.z);
							} catch(Exception e) {
							}
						}
					}

					// Walk to the crop
					BotUtils.pfLeftClick(g.rc.floor());
					if(stopThread)
						return;
					BotUtils.doClick(g, 3, 0);
					// Wait for harvest menu to appear
					while(ui.root.findchild(FlowerMenu.class) == null) {
						if(stopThread)
							return;
						BotUtils.sleep(10);
					}
				}

				if(stopThread)
					return;

				replantTiles.add(g.rc.floor());

				FlowerMenu menu = ui.root.findchild(FlowerMenu.class);
				if(menu != null) {
					for(FlowerMenu.Petal opt : menu.opts) {
						if(opt.name.equals("Harvest")) {
							menu.choose(opt);
							menu.destroy();
						}
					}
				}
				while(BotUtils.findObjectById(g.id) != null) {
					if(stopThread)
						return;
					BotUtils.sleep(10);
				}
				if(stopThread)
					return;
				cropsHarvested++;
				lblProg.settext(cropsHarvested + "/" + totalCrops);
			}
			BotUtils.sysMsg(cropName.substring(cropName.lastIndexOf("/") + 1).substring(0, 1).toUpperCase()
					+ cropName.substring(cropName.lastIndexOf("/") + 1).substring(1)
					+ " Farmer finished!", Color.white);
			this.destroy();
		} catch(Resource.Loading l) {
		}
	}

	public ArrayList<Gob> Gobs(String resname) {
		// Initialises list of crops to harvest between the selected coordinates
		ArrayList<Gob> gobs = new ArrayList<Gob>();
		double bigX = rc1.x > rc2.x ? rc1.x : rc2.x;
		double smallX = rc1.x < rc2.x ? rc1.x : rc2.x;
		double bigY = rc1.y > rc2.y ? rc1.y : rc2.y;
		double smallY = rc1.y < rc2.y ? rc1.y : rc2.y;
		synchronized (ui.sess.glob.oc) {
			for (Gob gob : ui.sess.glob.oc) {
				if (gob.rc.x <= bigX && gob.rc.x >= smallX && gob.getres() != null && gob.rc.y <= bigY
						&& gob.rc.y >= smallY && gob.getres().name.equals(resname)) {
					gobs.add(gob);
				}
			}
		}
		gobs.sort(new CoordSort());
		return gobs;
	}

	@Override
	public void wdgmsg(Widget sender, String msg, Object... args) {
		if (sender == cbtn) {
			stop();
			reqdestroy();
		} else
			super.wdgmsg(sender, msg, args);
	}

	// Sorts coordinate array to efficient sequence
	class CoordSort implements Comparator<Gob> {
		public int compare(Gob a, Gob b) {
			if (a.rc.floor().x == b.rc.floor().x) {
				if (a.rc.floor().x % 2 == 0)
					return (a.rc.floor().y < b.rc.floor().y) ? 1 : (a.rc.floor().y > b.rc.floor().y) ? -1 : 0;
				else
					return (a.rc.floor().y < b.rc.floor().y) ? -1 : (a.rc.floor().y > b.rc.floor().y) ? 1 : 0;
			} else
				return (a.rc.floor().x < b.rc.floor().x) ? -1 : (a.rc.floor().x > b.rc.floor().x) ? 1 : 0;
		}
	}

	public void stop() {
		// Stops thread
		BotUtils.sysMsg(cropName.substring(cropName.lastIndexOf("/") + 1).substring(0, 1).toUpperCase()
						+ cropName.substring(cropName.lastIndexOf("/") + 1).substring(1)
						+ " Farmer stopped!", Color.white);
		gameui().map.wdgmsg("click", Coord.z, gameui().map.player().rc.floor(posres), 1, 0);
		if (gameui().map.pfthread != null) {
			gameui().map.pfthread.interrupt();
		}
		stopThread = true;
		this.destroy();
	}
}