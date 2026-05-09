package slimeknights.tconstruct.world.worldgen.trees.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.HugeFungusConfiguration;
import net.minecraft.world.level.levelgen.feature.HugeFungusFeature;
import net.minecraft.world.level.levelgen.feature.WeepingVinesFeature;
import slimeknights.tconstruct.world.worldgen.trees.config.SlimeFungusConfig;

/** Slime huge fungus: validates ground via tag ({@link SlimeFungusConfig}); vanilla stem/hat logic is duplicated because parent helpers are private. */
public class SlimeFungusFeature extends HugeFungusFeature {
  public SlimeFungusFeature(Codec<HugeFungusConfiguration> codec) {
    super(codec);
  }

  @Override
  public boolean place(FeaturePlaceContext<HugeFungusConfiguration> context) {
    if (!(context.config() instanceof SlimeFungusConfig config)) {
      return super.place(context);
    }
    WorldGenLevel level = context.level();
    BlockPos pos = context.origin();
    if (!level.getBlockState(pos.below()).is(config.getGroundTag())) {
      return false;
    }
    RandomSource random = context.random();
    ChunkGenerator chunkGenerator = context.chunkGenerator();
    int height = Mth.nextInt(random, 4, 13);
    if (random.nextInt(12) == 0) {
      height *= 2;
    }
    if (!config.planted && pos.getY() + height + 1 >= chunkGenerator.getGenDepth()) {
      return false;
    }
    boolean huge = !config.planted && random.nextFloat() < 0.06F;
    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 4);
    tconPlaceStem(level, random, config, pos, height, huge);
    tconPlaceHat(level, random, config, pos, height, huge);
    return true;
  }

  private static boolean tconIsReplaceable(WorldGenLevel level, BlockPos pos, HugeFungusConfiguration config, boolean stems) {
    if (level.isStateAtPosition(pos, BlockBehaviour.BlockStateBase::canBeReplaced)) {
      return true;
    }
    return stems && config.replaceableBlocks.test(level, pos);
  }

  private void tconPlaceStem(
      WorldGenLevel level, RandomSource random, HugeFungusConfiguration config, BlockPos origin, int height, boolean huge) {
    BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
    BlockState stem = config.stemState;
    int spread = huge ? 1 : 0;

    for (int ox = -spread; ox <= spread; ox++) {
      for (int oz = -spread; oz <= spread; oz++) {
        boolean corners = huge && Mth.abs(ox) == spread && Mth.abs(oz) == spread;

        for (int dy = 0; dy < height; dy++) {
          mut.setWithOffset(origin, ox, dy, oz);
          if (tconIsReplaceable(level, mut, config, true)) {
            if (config.planted) {
              if (!level.getBlockState(mut.below()).isAir()) {
                level.destroyBlock(mut, true);
              }
              level.setBlock(mut, stem, 3);
            } else if (corners) {
              if (random.nextFloat() < 0.1F) {
                this.setBlock(level, mut, stem);
              }
            } else {
              this.setBlock(level, mut, stem);
            }
          }
        }
      }
    }
  }

  private void tconPlaceHat(
      WorldGenLevel level,
      RandomSource random,
      HugeFungusConfiguration config,
      BlockPos origin,
      int height,
      boolean huge) {
    BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
    boolean netherWartHat = config.hatState.is(Blocks.NETHER_WART_BLOCK);
    int i = Math.min(random.nextInt(1 + height / 3) + 5, height);
    int j = height - i;

    for (int y = j; y <= height; y++) {
      int radius = y < height - random.nextInt(3) ? 2 : 1;
      if (i > 8 && y < j + 4) {
        radius = 3;
      }

      if (huge) {
        radius++;
      }

      for (int ox = -radius; ox <= radius; ox++) {
        for (int oz = -radius; oz <= radius; oz++) {
          boolean edgeX = ox == -radius || ox == radius;
          boolean edgeZ = oz == -radius || oz == radius;
          boolean inner = !edgeX && !edgeZ && y != height;
          boolean corner = edgeX && edgeZ;
          boolean lowerBand = y < j + 3;
          mut.setWithOffset(origin, ox, y, oz);
          if (tconIsReplaceable(level, mut, config, false)) {
            if (config.planted && !level.getBlockState(mut.below()).isAir()) {
              level.destroyBlock(mut, true);
            }

            if (lowerBand) {
              if (!inner) {
                tconPlaceHatDropBlock(level, random, mut, config.hatState, netherWartHat);
              }
            } else if (inner) {
              tconPlaceHatBlock(level, random, config, mut, 0.1F, 0.2F, netherWartHat ? 0.1F : 0.0F);
            } else if (corner) {
              tconPlaceHatBlock(level, random, config, mut, 0.01F, 0.7F, netherWartHat ? 0.083F : 0.0F);
            } else {
              tconPlaceHatBlock(level, random, config, mut, 5.0E-4F, 0.98F, netherWartHat ? 0.07F : 0.0F);
            }
          }
        }
      }
    }
  }

  private void tconPlaceHatBlock(
      LevelAccessor level,
      RandomSource random,
      HugeFungusConfiguration config,
      BlockPos.MutableBlockPos mut,
      float decorChance,
      float hatChance,
      float vineChance) {
    if (random.nextFloat() < decorChance) {
      this.setBlock(level, mut, config.decorState);
    } else if (random.nextFloat() < hatChance) {
      this.setBlock(level, mut, config.hatState);
      if (random.nextFloat() < vineChance) {
        tconTryPlaceWeepingVines(mut, level, random);
      }
    }
  }

  private void tconPlaceHatDropBlock(LevelAccessor level, RandomSource random, BlockPos pos, BlockState hatState, boolean netherWartHat) {
    if (level.getBlockState(pos.below()).is(hatState.getBlock())) {
      this.setBlock(level, pos, hatState);
    } else if ((double)random.nextFloat() < 0.15) {
      this.setBlock(level, pos, hatState);
      if (netherWartHat && random.nextInt(11) == 0) {
        tconTryPlaceWeepingVines(pos, level, random);
      }
    }
  }

  private static void tconTryPlaceWeepingVines(BlockPos pos, LevelAccessor level, RandomSource random) {
    BlockPos.MutableBlockPos below = pos.mutable().move(Direction.DOWN);
    if (level.isEmptyBlock(below)) {
      int len = Mth.nextInt(random, 1, 5);
      if (random.nextInt(7) == 0) {
        len *= 2;
      }
      WeepingVinesFeature.placeWeepingVinesColumn(level, random, below, len, 23, 25);
    }
  }
}
