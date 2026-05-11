package slimeknights.tconstruct.tools.modifiers.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Duplicate of vanilla {@code ApplyBonusCount} bonus formulas (codec + math), which are package-private there.
 * Used by {@link ChrysophiliteBonusFunction} and {@link ModifierBonusLootFunction}.
 */
public final class LootBonusFormulas {
  private LootBonusFormulas() {}

  public interface Formula {
    int calculateNewCount(RandomSource random, int originalCount, int bonusLevel);

    FormulaType type();
  }

  public record FormulaType(ResourceLocation id, Codec<? extends Formula> codec) {}

  private static final Map<ResourceLocation, FormulaType> FORMULA_TYPES = Stream.of(
      BinomialWithBonusCount.TYPE,
      OreDrops.TYPE,
      UniformBonusCount.TYPE
  ).collect(Collectors.toMap(FormulaType::id, Function.identity()));

  public static final Codec<FormulaType> FORMULA_TYPE_CODEC = ResourceLocation.CODEC.comapFlatMap(
      id -> {
        FormulaType type = FORMULA_TYPES.get(id);
        return type != null ? DataResult.success(type) : DataResult.error(() -> "No formula type with id: '" + id + "'");
      },
      FormulaType::id
  );

  public static final MapCodec<Formula> CODEC = ExtraCodecs.dispatchOptionalValue(
      "formula", "parameters", FORMULA_TYPE_CODEC, Formula::type, FormulaType::codec);

  public record BinomialWithBonusCount(int extraRounds, float probability) implements Formula {
    public static final Codec<BinomialWithBonusCount> BINOMIAL_CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
                Codec.INT.fieldOf("extra").forGetter(BinomialWithBonusCount::extraRounds),
                Codec.FLOAT.fieldOf("probability").forGetter(BinomialWithBonusCount::probability))
            .apply(instance, BinomialWithBonusCount::new));

    public static final FormulaType TYPE = new FormulaType(
        ResourceLocation.withDefaultNamespace("binomial_with_bonus_count"), BINOMIAL_CODEC);

    @Override
    public int calculateNewCount(RandomSource random, int originalCount, int bonusLevel) {
      int count = originalCount;
      for (int i = 0; i < bonusLevel + this.extraRounds; i++) {
        if (random.nextFloat() < this.probability) {
          count++;
        }
      }
      return count;
    }

    @Override
    public FormulaType type() {
      return TYPE;
    }
  }

  public record OreDrops() implements Formula {
    public static final Codec<OreDrops> ORE_CODEC = Codec.unit(OreDrops::new);
    public static final FormulaType TYPE = new FormulaType(ResourceLocation.withDefaultNamespace("ore_drops"), ORE_CODEC);

    @Override
    public int calculateNewCount(RandomSource random, int originalCount, int bonusLevel) {
      if (bonusLevel > 0) {
        int extra = random.nextInt(bonusLevel + 2) - 1;
        if (extra < 0) {
          extra = 0;
        }
        return originalCount * (extra + 1);
      }
      return originalCount;
    }

    @Override
    public FormulaType type() {
      return TYPE;
    }
  }

  public record UniformBonusCount(int bonusMultiplier) implements Formula {
    public static final Codec<UniformBonusCount> UNIFORM_CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
                Codec.INT.fieldOf("bonusMultiplier").forGetter(UniformBonusCount::bonusMultiplier))
            .apply(instance, UniformBonusCount::new));

    public static final FormulaType TYPE = new FormulaType(
        ResourceLocation.withDefaultNamespace("uniform_bonus_count"), UNIFORM_CODEC);

    @Override
    public int calculateNewCount(RandomSource random, int originalCount, int bonusLevel) {
      return originalCount + random.nextInt(this.bonusMultiplier * bonusLevel + 1);
    }

    @Override
    public FormulaType type() {
      return TYPE;
    }
  }
}
