package slimeknights.tconstruct.common.data;

import net.minecraft.advancements.AdvancementRequirements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/** Builds grouped advancement requirement lists for {@link AdvancementRequirements.Strategy}. */
public class CountRequirementsStrategy implements AdvancementRequirements.Strategy {
  private final int[] sizes;

  public CountRequirementsStrategy(int... sizes) {
    this.sizes = sizes;
  }

  @Override
  public AdvancementRequirements create(Collection<String> strings) {
    List<List<String>> requirements = new ArrayList<>();
    List<String> list = new ArrayList<>(strings);
    int nextIndex = 0;
    for (int size : sizes) {
      String[] row = new String[size];
      for (int j = 0; j < size; j++) {
        row[j] = list.get(nextIndex++);
      }
      requirements.add(Arrays.asList(row));
    }
    return new AdvancementRequirements(requirements);
  }
}
