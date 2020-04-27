package haven.purus;

import haven.Button;
import haven.Window;
import haven.*;
import haven.automation.AreaSelectCallback;
import haven.automation.GobSelectCallback;
import haven.purus.farmer2.SeedCropFarmer2;

import java.awt.*;

public class Farmer2 extends Window implements AreaSelectCallback, GobSelectCallback {

	private Coord a, b;
	private boolean container = false, replant = true;
	private CheckBox replantChkbox, fillContainerChkbox;
	private Gob barrel;

	public Farmer2() {
		super(new Coord(180, 615), "Farming Bots V2 (WIP)");
		int y = 0;
		Button carrotBtn = new Button(140, "Carrot") {
			@Override
			public void click() {
				if(container) {
					BotUtils.sysMsg("Choose replant for carrots!", Color.WHITE);
				} else if (a != null && b != null) {
					gameui().map.unregisterAreaSelect();
					// Start carrot farmer and close this window
					SeedCropFarmer2 SCF = new SeedCropFarmer2(b, a, "gfx/terobjs/plants/carrot", "gfx/invobjs/carrot", 4);
					gameui().add(SCF,
							new Coord(gameui().sz.x / 2 - SCF.sz.x / 2, gameui().sz.y / 2 - SCF.sz.y / 2 - 200));
					new Thread(SCF).start();
					this.parent.destroy();
				} else {
					BotUtils.sysMsg("Area not selected!", Color.WHITE);
				}
			}
		};
		add(carrotBtn, new Coord(20, y));
		y += 35;

		/*Button onionBtn = new Button(140, "Yellow Onion") {
			@Override
			public void click() {
				if(container) {
					BotUtils.sysMsg("Choose replant for onions!", Color.WHITE);
				} else if (a != null && b != null) {
					// Start yellow onion farmer and close this window
					SeedCropFarmer bf =
							new SeedCropFarmer(a, b, "gfx/terobjs/plants/yellowonion", "gfx/invobjs/yellowonion", 3, container, barrel);

					gameui().add(bf, new Coord(gameui().sz.x / 2 - bf.sz.x / 2, gameui().sz.y / 2 - bf.sz.y / 2 - 200));
					new Thread(bf).start();
					this.parent.destroy();
				} else {
					BotUtils.sysMsg("Area not selected!", Color.WHITE);
				}
			}
		};
		add(onionBtn, new Coord(20, y));
		y += 35;

		Button redOnionBtn = new Button(140, "Red Onion") {
			@Override
			public void click() {
				if(container) {
					BotUtils.sysMsg("Choose replant for onions!", Color.WHITE);
				} else if (a != null && b != null) {
					// Start yellow onion farmer and close this window
					SeedCropFarmer bf =
							new SeedCropFarmer(a, b, "gfx/terobjs/plants/redonion", "gfx/invobjs/redonion", 3, container, barrel);

					gameui().add(bf, new Coord(gameui().sz.x / 2 - bf.sz.x / 2, gameui().sz.y / 2 - bf.sz.y / 2 - 200));
					new Thread(bf).start();
					this.parent.destroy();
				} else {
					BotUtils.sysMsg("Area not selected!", Color.WHITE);
				}
			}
		};
		add(redOnionBtn, new Coord(20, y));
		y += 35;

		Button beetBtn = new Button(140, "Beetroot") {
			@Override
			public void click() {
				if(container) {
					BotUtils.sysMsg("Choose replant for beetroots!", Color.WHITE);
				} else if (a != null && b != null) {
					// Start beetroot onion farmer and close this window
					SeedCropFarmer bf = new SeedCropFarmer(a, b, "gfx/terobjs/plants/beet", "gfx/invobjs/beet", 3, container, barrel);

					gameui().add(bf, new Coord(gameui().sz.x / 2 - bf.sz.x / 2, gameui().sz.y / 2 - bf.sz.y / 2 - 200));
					new Thread(bf).start();
					this.parent.destroy();
				} else {
					BotUtils.sysMsg("Area not selected!", Color.WHITE);
				}
			}
		};
		add(beetBtn, new Coord(20, y));
		y += 35;

		Button barleyBtn = new Button(140, "Barley") {
			@Override
			public void click() {
				if (a != null && b != null) {
					System.out.println(a + "" + b);
					// Start barley farmer and close this window
					SeedCropFarmer bf =
							new SeedCropFarmer(a, b, "gfx/terobjs/plants/barley", "gfx/invobjs/seed-barley", 3, container, barrel);

					gameui().add(bf, new Coord(gameui().sz.x / 2 - bf.sz.x / 2, gameui().sz.y / 2 - bf.sz.y / 2 - 200));
					new Thread(bf).start();
					this.parent.destroy();
				} else {
					BotUtils.sysMsg("Area not selected!", Color.WHITE);
				}
			}
		};
		add(barleyBtn, new Coord(20, y));
		y += 35;

		Button wheatBtn = new Button(140, "Wheat") {
			@Override
			public void click() {
				if (a != null && b != null) {
					// Start yellow onion farmer and close this window
					SeedCropFarmer bf =
							new SeedCropFarmer(a, b, "gfx/terobjs/plants/wheat", "gfx/invobjs/seed-wheat", 3, container, barrel);

					gameui().add(bf, new Coord(gameui().sz.x / 2 - bf.sz.x / 2, gameui().sz.y / 2 - bf.sz.y / 2 - 200));
					new Thread(bf).start();
					this.parent.destroy();
				} else {
					BotUtils.sysMsg("Area not selected!", Color.WHITE);
				}
			}
		};
		add(wheatBtn, new Coord(20, y));
		y += 35;

		Button flaxBtn = new Button(140, "Flax") {
			@Override
			public void click() {
				if (a != null && b != null) {
					// Start flax farmer and close this window
					SeedCropFarmer bf = new SeedCropFarmer(a, b, "gfx/terobjs/plants/flax", "gfx/invobjs/seed-flax", 3, container, barrel);

					gameui().add(bf, new Coord(gameui().sz.x / 2 - bf.sz.x / 2, gameui().sz.y / 2 - bf.sz.y / 2 - 200));
					new Thread(bf).start();
					this.parent.destroy();
				} else {
					BotUtils.sysMsg("Area not selected!", Color.WHITE);
				}
			}
		};
		add(flaxBtn, new Coord(20, y));
		y += 35;

		Button poppyBtn = new Button(140, "Poppy") {
			@Override
			public void click() {
				if (a != null && b != null) {
					// Start poppy farmer and close this window
					SeedCropFarmer bf =
							new SeedCropFarmer(a, b, "gfx/terobjs/plants/poppy", "gfx/invobjs/seed-poppy", 4, container, barrel);

					gameui().add(bf, new Coord(gameui().sz.x / 2 - bf.sz.x / 2, gameui().sz.y / 2 - bf.sz.y / 2 - 200));
					new Thread(bf).start();
					this.parent.destroy();
				} else {
					BotUtils.sysMsg("Area not selected!", Color.WHITE);
				}
			}
		};
		add(poppyBtn, new Coord(20, y));
		y += 35;

		Button pipeweedBtn = new Button(140, "Pipeweed") {
			@Override
			public void click() {
				if (a != null && b != null) {
					// Start poppy farmer and close this window
					SeedCropFarmer bf =
							new SeedCropFarmer(a, b, "gfx/terobjs/plants/pipeweed", "gfx/invobjs/seed-pipeweed", 4, container, barrel);

					gameui().add(bf, new Coord(gameui().sz.x / 2 - bf.sz.x / 2, gameui().sz.y / 2 - bf.sz.y / 2 - 200));
					new Thread(bf).start();
					this.parent.destroy();
				} else {
					BotUtils.sysMsg("Area not selected!", Color.WHITE);
				}
			}
		};
		add(pipeweedBtn, new Coord(20, y));
		y += 35;

		Button lettuceBtn = new Button(140, "Lettuce") {
			@Override
			public void click() {
				if (a != null && b != null) {
					// Start poppy farmer and close this window
					SeedCropFarmer bf =
							new SeedCropFarmer(a, b, "gfx/terobjs/plants/lettuce", "gfx/invobjs/seed-lettuce", 4, container, barrel);

					gameui().add(bf, new Coord(gameui().sz.x / 2 - bf.sz.x / 2, gameui().sz.y / 2 - bf.sz.y / 2 - 200));
					new Thread(bf).start();
					this.parent.destroy();
				} else {
					BotUtils.sysMsg("Area not selected!", Color.WHITE);
				}
			}
		};
		add(lettuceBtn, new Coord(20, y));
		y += 35;

		Button hempBtn = new Button(140, "Hemp") {
			@Override
			public void click() {
				if (a != null && b != null) {
					// Start hemp farmer and close this window
					SeedCropFarmer bf = new SeedCropFarmer(a, b, "gfx/terobjs/plants/hemp", "gfx/invobjs/seed-hemp", 4, container, barrel);

					gameui().add(bf, new Coord(gameui().sz.x / 2 - bf.sz.x / 2, gameui().sz.y / 2 - bf.sz.y / 2 - 200));
					new Thread(bf).start();
					this.parent.destroy();
				} else
					BotUtils.sysMsg("Area not selected!", Color.WHITE);
			}
		};
		add(hempBtn, new Coord(20, y));
		y += 35;

		Button hempBudBtn = new Button(140, "Hemp-Buds") {
			@Override
			public void click() {
				if (a != null && b != null) {
					// Start hemp farmer and close this window
					SeedCropFarmer bf = new SeedCropFarmer(a, b, "gfx/terobjs/plants/hemp", "gfx/invobjs/hemp-fresh", 3, container, barrel);

					gameui().add(bf, new Coord(gameui().sz.x / 2 - bf.sz.x / 2, gameui().sz.y / 2 - bf.sz.y / 2 - 200));
					new Thread(bf).start();
					this.parent.destroy();
				} else
					BotUtils.sysMsg("Area not selected!", Color.WHITE);
			}
		};
		add(hempBudBtn, new Coord(20, y));
		y += 35;

		Button trelHarBtn = new Button(140, "Trellis harvest") {
			@Override
			public void click() {
				if (a != null && b != null) {
					// Start yellow onion farmer and close this window
					TrellisFarmer bf = new TrellisFarmer(a, b, true, false, false);

					gameui().add(bf, new Coord(gameui().sz.x / 2 - bf.sz.x / 2, gameui().sz.y / 2 - bf.sz.y / 2 - 200));
					new Thread(bf).start();
					this.parent.destroy();
				} else {
					BotUtils.sysMsg("Area not selected!", Color.WHITE);
				}
			}
		};
		add(trelHarBtn, new Coord(20, y));
		y += 35;

		Button trelDesBtn = new Button(140, "Trellis destroy") {
			@Override
			public void click() {
				if (a != null && b != null) {
					// Start yellow onion farmer and close this window
					TrellisFarmer bf = new TrellisFarmer(a, b, false, true, false);

					gameui().add(bf, new Coord(gameui().sz.x / 2 - bf.sz.x / 2, gameui().sz.y / 2 - bf.sz.y / 2 - 200));
					new Thread(bf).start();
					this.parent.destroy();
				} else {
					BotUtils.sysMsg("Area not selected!", Color.WHITE);
				}
			}
		};
		add(trelDesBtn, new Coord(20, y));
		y += 35;

		Button trelPlantBtn = new Button(140, "Trellis plant") {
			@Override
			public void click() {
				if (a != null && b != null) {
					// Start yellow onion farmer and close this window
					TrellisFarmer bf = new TrellisFarmer(a, b, false, false, true);
					gameui().add(bf, new Coord(gameui().sz.x / 2 - bf.sz.x / 2, gameui().sz.y / 2 - bf.sz.y / 2 - 200));
					new Thread(bf).start();
					this.parent.destroy();
				} else {
					BotUtils.sysMsg("Area not selected!", Color.WHITE);
				}
			}
		};
		add(trelPlantBtn, new Coord(20, y));
		y += 35;*/

		Button areaSelBtn = new Button(140, "Select Area") {
			@Override
			public void click() {
				BotUtils.sysMsg("Drag an area over crops, small chests and troughs", Color.WHITE);
				gameui().map.farmSelect = true;
			}
		};
		add(areaSelBtn, new Coord(20, y));
		y += 35;

	}

	private void registerGobSelect() {
		synchronized (GobSelectCallback.class) {
			BotUtils.gui.map.registerGobSelect(this);
		}
	}

	public void areaselect(Coord a, Coord b) {
		this.a = a.mul(MCache.tilesz2);
		this.b = b.mul(MCache.tilesz2).add(11, 11);
		BotUtils.sysMsg("Area selected!", Color.WHITE);
		BotUtils.gui.map.unregisterAreaSelect();
	}

	@Override
	public void wdgmsg(Widget sender, String msg, Object... args) {
		if (sender == cbtn)
			reqdestroy();
		else
			super.wdgmsg(sender, msg, args);
	}

	@Override
	public void gobselect(Gob gob) {
		if (gob.getres().basename().contains("barrel")) {
			barrel = gob;
			BotUtils.sysMsg("Barrel selected!", Color.WHITE);
		} else {
			BotUtils.sysMsg("Please choose a barrel as a container!", Color.WHITE);
		}
		gameui().map.unregisterGobSelect();
	}
}
