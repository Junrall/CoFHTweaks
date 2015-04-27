package cofh.tweak.asmhooks.world;

import cofh.repack.cofh.lib.util.LinkedHashList;

import java.util.BitSet;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class ClientChunk extends Chunk {

	private static ChunkThread worker = new ChunkThread();
	static {
		worker.start();
	}
	private static class ChunkThread extends Thread {

		public LinkedHashList<ClientChunk> data = new LinkedHashList<ClientChunk>();

		@Override
		public void run() {

			for (;;) {

				for (int i = 0; data.size() > 0; ++i) {
					data.shift().buildSides();
					if (i > 5) {
						i = 0;
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
						}
					}
				}
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public BitSet solidSides = new BitSet(6 * 16);
	private BitSet[][] internalSides = new BitSet[16][6];

	private static void init(BitSet[][] internalSides) {

		for (int i = 0; i < 16; ++i) {
			for (int j = 0; j < 6; ++j) {
				internalSides[i][j] = new BitSet(256);
			}
		}
	}

	public ClientChunk(World world, int x, int z) {

		super(world, x, z);
		init(internalSides);
	}

	public ClientChunk(World world, Block[] blocks, int x, int z) {

		super(world, blocks, x, z);
		init(internalSides);
	}

	public ClientChunk(World world, Block[] blocks, byte[] metas, int x, int z) {

		super(world, blocks, metas, x, z);
		init(internalSides);
	}

	void checkPosSolid(int x, int y, int z, Block block) {

		if (y > 255 || y < 0)
			return;
		int yp = 1 + (y >> 4);
		BitSet[] internalSides = this.internalSides[yp - 1];
		if (block == null) {
			block = getBlock(x, y, z);
		}
		y &= 15;

		if (y == 0) {
			boolean solid = block.isOpaqueCube();
			BitSet side = internalSides[0];
			side.set(x + z * 16, solid);
			if (solid) {
				solid = side.nextClearBit(0) == 256;
			}
			solidSides.set(yp * 1, solid);
		} else if (y == 15) {
			boolean solid = block.isOpaqueCube();
			BitSet side = internalSides[1];
			side.set(x + z * 16, solid);
			if (solid) {
				solid = side.nextClearBit(0) == 256;
			}
			solidSides.set(yp * 2, solid);
		}

		if (x == 0) {
			boolean solid = block.isOpaqueCube();
			BitSet side = internalSides[2];
			side.set(y + z * 16, solid);
			if (solid) {
				solid = side.nextClearBit(0) == 256;
			}
			solidSides.set(yp * 3, solid);
		} else if (x == 15) {
			boolean solid = block.isOpaqueCube();
			BitSet side = internalSides[3];
			side.set(y + z * 16, solid);
			if (solid) {
				solid = side.nextClearBit(0) == 256;
			}
			solidSides.set(yp * 4, solid);
		}

		if (z == 0) {
			boolean solid = block.isOpaqueCube();
			BitSet side = internalSides[4];
			side.set(x + y * 16, solid);
			if (solid) {
				solid = side.nextClearBit(0) == 256;
			}
			solidSides.set(yp * 5, solid);
		} else if (z == 15) {
			boolean solid = block.isOpaqueCube();
			BitSet side = internalSides[5];
			side.set(x + y * 16, solid);
			if (solid) {
				solid = side.nextClearBit(0) == 256;
			}
			solidSides.set(yp * 6, solid);
		}
	}

	@Override
	public boolean func_150807_a(int x, int y, int z, Block block, int meta) {

		boolean r = super.func_150807_a(x, y, z, block, meta);
		if (r) {
			checkPosSolid(x & 15, y, z & 15, block);
		}
		return r;
	}

	@Override
	public void fillChunk(byte[] data, int x, int z, boolean flag) {

		super.fillChunk(data, x, z, flag);
		worker.data.add(this);
	}

	void buildSides() {

		if (!this.worldObj.chunkExists(xPosition, zPosition)) {
			return;
		}
		for (int i = 0; i < 16; ++i) {
			for (int j = 0; j < 16; ++j) {
				for (int y = 0; y < 256; ++y) {
					checkPosSolid(i, y, j, null);
				}
			}
		}
	}

}
