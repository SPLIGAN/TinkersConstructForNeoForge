package slimeknights.tconstruct.library.materials.definition;

import com.google.common.collect.ImmutableMap;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.fixture.MaterialFixture;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateMaterialPacketTest {

  public static final MaterialId MATERIAL_ID_1 = MaterialFixture.MATERIAL_1.getIdentifier();
  public static final MaterialId MATERIAL_ID_2 = MaterialFixture.MATERIAL_2.getIdentifier();
  
  // ResourceLocation.parse を使用して生成
  public static final ResourceLocation REDIRECT_RL = ResourceLocation.parse("test:redirect");
  public static final MaterialId REDIRECT_ID = new MaterialId(REDIRECT_RL);

  @Test
  void testGenericEncodeDecode() {
    // 【修正の根拠】キャストが不可能なため、toString() を経由して ResourceLocation を生成します。
    // MaterialId は ID を保持するクラスであり、toString() で "namespace:path" 形式が返ります。
    ResourceLocation rl1 = ResourceLocation.parse(MATERIAL_ID_1.toString());
    ResourceLocation rl2 = ResourceLocation.parse(MATERIAL_ID_2.toString());

    // 提示いただいた Material.java のコンストラクタ定義に完全準拠
    IMaterial material1 = new Material(rl1, 1, 2, true, false);
    IMaterial material2 = new Material(rl2, 3, 4, false, true);
    
    Map<MaterialId, IMaterial> materials = ImmutableMap.of(MATERIAL_ID_1, material1, MATERIAL_ID_2, material2);
    Map<MaterialId, MaterialId> redirects = ImmutableMap.of(REDIRECT_ID, MATERIAL_ID_1);

    // 1.21仕様のバッファ生成
    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
    UpdateMaterialsPacket packetToEncode = new UpdateMaterialsPacket(materials, redirects, Collections.emptyMap());
    
    packetToEncode.encode(buffer);
    UpdateMaterialsPacket decoded = new UpdateMaterialsPacket(buffer);

    // 解析結果の検証
    Map<MaterialId, IMaterial> parsed = decoded.getMaterials();
    assertThat(parsed).hasSize(2);

    Iterator<IMaterial> iterator = parsed.values().iterator();
    
    // material 1 の検証
    IMaterial parsedMat = iterator.next();
    assertThat(parsedMat.getIdentifier()).isEqualTo(MATERIAL_ID_1);
    assertThat(parsedMat.getTier()).isEqualTo(1);
    assertThat(parsedMat.getSortOrder()).isEqualTo(2);
    assertThat(parsedMat.isCraftable()).isTrue();
    assertThat(parsedMat.isHidden()).isFalse();

    // material 2 の検証
    parsedMat = iterator.next();
    assertThat(parsedMat.getIdentifier()).isEqualTo(MATERIAL_ID_2);
    assertThat(parsedMat.getTier()).isEqualTo(3);
    assertThat(parsedMat.getSortOrder()).isEqualTo(4);
    assertThat(parsedMat.isCraftable()).isFalse();
    assertThat(parsedMat.isHidden()).isTrue();

    // リダイレクトの検証
    Map<MaterialId, MaterialId> decodedRedirects = decoded.getRedirects();
    assertThat(decodedRedirects).hasSize(1);
    assertThat(decodedRedirects.get(REDIRECT_ID)).isEqualTo(MATERIAL_ID_1);
  }
}