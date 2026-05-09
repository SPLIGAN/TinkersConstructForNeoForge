package slimeknights.tconstruct.world.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

/** Slimy soil; crop sustain checks use vanilla/farm tags on modern versions—see slimy farmland usage if plants fail to attach. */
public class SlimeDirtBlock extends Block {

  public SlimeDirtBlock(Properties properties) {
    super(properties);
  }
}
