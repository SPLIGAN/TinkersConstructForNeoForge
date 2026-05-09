package slimeknights.tconstruct.library.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;

/** Helpers related to Tag */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TagUtil {
  /* Helper functions */

  /**
   * Reads a block position from Tag
   * @param parent  Parent tag
   * @param key     Position key
   * @param offset  Amount to offset position by
   * @return  Block position, or null if invalid or missing
   */
  @Nullable
  public static BlockPos readOptionalPos(CompoundTag parent, String key, BlockPos offset) {
    if (parent.contains(key, Tag.TAG_COMPOUND)) {
      CompoundTag posTag = parent.getCompound(key);
      return new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z")).offset(offset);
    }
    return null;
  }

  /** Writes a block position as an XYZ compound (relative to {@code origin}). */
  public static void writeRelativeBlockPos(CompoundTag parent, String key, BlockPos absolute, BlockPos origin) {
    BlockPos rel = absolute.subtract(origin);
    CompoundTag posTag = new CompoundTag();
    posTag.putInt("X", rel.getX());
    posTag.putInt("Y", rel.getY());
    posTag.putInt("Z", rel.getZ());
    parent.put(key, posTag);
  }

  /**
   * Checks if the given tag is a numeric type
   * @param tag  Tag to check
   * @return  True if the type matches
   */
  public static boolean isNumeric(Tag tag) {
    byte type = tag.getId();
    return type == Tag.TAG_BYTE || type == Tag.TAG_SHORT || type == Tag.TAG_INT || type == Tag.TAG_LONG || type == Tag.TAG_FLOAT || type == Tag.TAG_DOUBLE;
  }
}
