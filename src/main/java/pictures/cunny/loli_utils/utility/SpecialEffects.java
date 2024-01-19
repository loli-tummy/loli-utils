package pictures.cunny.loli_utils.utility;

import java.util.HashMap;
import java.util.Map;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.entity.player.PlayerEntity;

public class SpecialEffects {
  public static final Map<String, Color> PLAYER_COLORS = new HashMap<>();

  public static boolean hasColor(PlayerEntity entity) {
    return PLAYER_COLORS.containsKey(entity.getUuidAsString());
  }

  public static Color getColor(PlayerEntity entity) {
    return PLAYER_COLORS.getOrDefault(entity.getUuidAsString(), Color.GRAY);
  }
}
