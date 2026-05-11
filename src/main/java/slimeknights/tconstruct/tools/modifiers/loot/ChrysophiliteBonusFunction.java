package slimeknights.tconstruct.tools.modifiers.loot;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.modifiers.traits.skull.ChrysophiliteModifier;

import java.util.List;
import java.util.Set;

/** Loot modifier to boost drops based on the chrysophilite amount */
public class ChrysophiliteBonusFunction extends LootItemConditionalFunction {
  /** Formula to apply */
  private final LootBonusFormulas.Formula formula;
  /** If true, the includes the helmet in the level, if false level is just gold pieces */
  private final boolean includeBase;

  protected ChrysophiliteBonusFunction(List<LootItemCondition> conditions, LootBonusFormulas.Formula formula, boolean includeBase) {
    super(conditions);
    this.formula = formula;
    this.includeBase = includeBase;
  }

  public static final MapCodec<ChrysophiliteBonusFunction> CODEC = RecordCodecBuilder.mapCodec(inst ->
      commonFields(inst).and(inst.group(
              LootBonusFormulas.CODEC.forGetter(f -> f.formula),
              Codec.BOOL.optionalFieldOf("include_base", true).forGetter(f -> f.includeBase)))
          .apply(inst, ChrysophiliteBonusFunction::new));

  /** Creates a generic builder */
  public static Builder<?> builder(LootBonusFormulas.Formula formula, boolean includeBase) {
    return simpleBuilder(conditions -> new ChrysophiliteBonusFunction(conditions, formula, includeBase));
  }

  /** Creates a builder for the binomial with bonus formula */
  public static Builder<?> binomialWithBonusCount(float probability, int extra, boolean includeBase) {
    return builder(new LootBonusFormulas.BinomialWithBonusCount(extra, probability), includeBase);
  }

  /** Creates a builder for the ore drops formula */
  public static Builder<?> oreDrops(boolean includeBase) {
    return builder(new LootBonusFormulas.OreDrops(), includeBase);
  }

  /** Creates a builder for the uniform bonus count */
  public static Builder<?> uniformBonusCount(int bonusMultiplier, boolean includeBase) {
    return builder(new LootBonusFormulas.UniformBonusCount(bonusMultiplier), includeBase);
  }

  @Override
  protected ItemStack run(ItemStack stack, LootContext context) {
    int level = ChrysophiliteModifier.getTotalGold(context.getParamOrNull(LootContextParams.THIS_ENTITY));
    if (!includeBase) {
      level--;
    }
    if (level > 0) {
      stack.setCount(formula.calculateNewCount(context.getRandom(), stack.getCount(), level));
    }
    return stack;
  }

  @Override
  public Set<LootContextParam<?>> getReferencedContextParams() {
    return ImmutableSet.of(LootContextParams.THIS_ENTITY);
  }

  @Override
  public LootItemFunctionType getType() {
    return TinkerModifiers.chrysophiliteBonusFunction.get();
  }
}
