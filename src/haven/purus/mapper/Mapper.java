package haven.purus.mapper;

import haven.*;
import integrations.map.MinimapImageGenerator;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class Mapper {
	public static String apiURL = Config.pastaMapperUrl + "//";

	private static ScheduledExecutorService executor;

	private static AtomicReference<String> charname = new AtomicReference<>();
	private static AtomicReference<Coord> playerGridOfs = new AtomicReference<>();
	private static AtomicLong playerGridId = new AtomicLong();
	private static AtomicReference<String> hatres = new AtomicReference<>();

	private static Runnable locUpd =  new Runnable() {
				@Override
				public void run() {
					if(charname.get() == null || playerGridOfs.get() == null)
						return;
					try {
						HttpURLConnection conn = (HttpURLConnection) new URL(new URL(apiURL), "charloc").openConnection();
						conn.setRequestMethod("POST");
						conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
						conn.setDoOutput(true);

						JSONObject obj = new JSONObject();
						obj.put("hatres", hatres.get());
						obj.put("ofsX", playerGridOfs.get().x);
						obj.put("ofsY", playerGridOfs.get().y);
						obj.put("gridId", playerGridId.get());
						obj.put("charname", charname.get());

						DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
						dos.write(obj.toString().getBytes(StandardCharsets.UTF_8));
						dos.close();
						conn.getResponseCode();
					} catch(IOException e) {
						e.printStackTrace();
					}
				}
			};

	static {
		executor = Executors.newScheduledThreadPool(1);
		if(Config.pastaMapper) {
			if(Config.pastaMapper)
				executor.scheduleWithFixedDelay(locUpd, 5, 5, TimeUnit.SECONDS);
		}
	}

	public static void setHat(String res, boolean cosmetic) {
		if(cosmetic || hatres.get() == null)
			hatres.set(res);
	}

	public static void setPlayerLoc(Coord2d c, long gridId) {
		playerGridId.set(gridId);
		playerGridOfs.set(c.floor().div(11).mod(new Coord(100, 100)));
	}

	public static void setCharacterName(String name) {
		charname.set(name);
	}

	public static void receiveGrid(MCache.Grid grid, MCache.Grid top, MCache.Grid right, MCache.Grid down, MCache.Grid left) {
		sendGridData(grid, top, right, down, left);

		Glob glob = Glob.getByReference();
		BufferedImage maptile = null;
		while(maptile == null) {
			try {
				maptile = MinimapImageGenerator.drawmap(glob.map, grid);
			} catch(Loading l) {
				try {
					l.waitfor();
				} catch(InterruptedException e) {
				}
			}
		}
		sendMaptile(grid.id, maptile);
	}

	public static void sendMarkerData(long gridId, int ofsX, int ofsY, String resname, String name) {
		Runnable run = new Runnable() {
			@Override
			public void run() {
				try {
					JSONArray arr = new JSONArray();
					JSONObject obj = new JSONObject();
					obj.put("gridId", gridId);
					obj.put("ofsX", ofsX);
					obj.put("ofsY", ofsY);

					obj.put("res", resname);
					obj.put("name", name);
					arr.put(obj);


					HttpURLConnection conn = (HttpURLConnection) new URL(new URL(apiURL), "waypoints/client/markers").openConnection();
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
					conn.setDoOutput(true);
					DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
					dos.write(arr.toString().getBytes(StandardCharsets.UTF_8));
					dos.close();
					conn.getResponseCode();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		};
		executor.schedule(run, 0, TimeUnit.SECONDS);
	}

	public static void sendMarkerData(MapFile mapFile) {
		Runnable run = new Runnable() {
			@Override
			public void run() {
				try {
					JSONArray arr = new JSONArray();
					if (mapFile.lock.readLock().tryLock()) {
						mapFile.markers.stream().forEach((marker) -> {
							if(marker instanceof MapFile.SMarker) {
								Coord markerOfs = new Coord(marker.tc.x, marker.tc.y);
								Coord offsetTiles = markerOfs.mod(new Coord(100, 100));
								MapFile.Segment seg = mapFile.segments.get(marker.seg);
								JSONObject obj = new JSONObject();
								obj.put("gridId", seg.map.get(markerOfs.div(100)));
								obj.put("ofsX", offsetTiles.x);
								obj.put("ofsY", offsetTiles.y);

								obj.put("res", ((MapFile.SMarker) marker).res.name);
								obj.put("name", marker.nm);
								arr.put(obj);
							}
						});
						mapFile.lock.readLock().unlock();
					} else {
						System.out.println("Sending markers failed!");
						return;
					}


					HttpURLConnection conn = (HttpURLConnection) new URL(new URL(apiURL), "waypoints/client/markers").openConnection();
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
					conn.setDoOutput(true);
					DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
					dos.write(arr.toString().getBytes(StandardCharsets.UTF_8));
					dos.close();
					conn.getResponseCode();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		};
		executor.schedule(run, 5, TimeUnit.SECONDS);
	}

	private static void sendGridData(MCache.Grid... grids) {
		Runnable run = new Runnable() {
			@Override
			public void run() {
				if(grids.length <= 1)
					return;
				try {
					HttpURLConnection conn = (HttpURLConnection) new URL(new URL(apiURL), "grid").openConnection();
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
					conn.setDoOutput(true);

					JSONArray ar = new JSONArray();

					for(MCache.Grid g : grids) {
						if(g == null)
							continue;
						JSONObject obj = new JSONObject();
						obj.append("id", g.id);
						obj.append("x", g.gc.x);
						obj.append("y", g.gc.y);
						ar.put(obj);
					}
					DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
					dos.write(ar.toString().getBytes(StandardCharsets.UTF_8));
					dos.close();
					conn.getResponseCode();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		};
		executor.execute(run);
	}

	private static void sendMaptile(long gridId, BufferedImage maptile) {
		Runnable run = new Runnable() {
			@Override
			public void run() {
				try {
					HttpURLConnection conn = (HttpURLConnection) new URL(new URL(apiURL), "maptile").openConnection();
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
					conn.setDoOutput(true);

					ByteArrayOutputStream os = new ByteArrayOutputStream();
					ImageIO.write(maptile, "png", os);
					os.flush();

					JSONObject obj = new JSONObject();
					obj.append("gridId", gridId);
					obj.append("maptile", new String(Base64.getEncoder().encode(os.toByteArray()), StandardCharsets.UTF_8));

					DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
					dos.write(obj.toString().getBytes(StandardCharsets.UTF_8));
					dos.close();
					conn.getResponseCode();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		};
		executor.execute(run);
	}
}
