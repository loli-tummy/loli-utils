package pictures.cunny.loli_utils.utility;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

public class EntityUtils {
  public static int customY = 1337;

  private static final List<EntityType<?>> collidable =
      List.of(EntityType.ITEM, EntityType.TRIDENT, EntityType.ARROW, EntityType.AREA_EFFECT_CLOUD);

  public static boolean canPlaceIn(Entity entity) {
    return collidable.contains(entity.getType()) || entity.isRemoved() || entity.isSpectator();
  }

  public static void resetY() {
    customY = 1337;
  }

  public static int getY() {
    if (customY != 1337) {
      return customY;
    }

    return mc.player.getBlockY();
  }
}
