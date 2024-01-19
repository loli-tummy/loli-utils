package pictures.cunny.loli_utils.utility;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.MapColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class MapColorCache {
  protected static Map<Item, MapColor> ITEM_TO_COLOR = new HashMap<>();

  public static void init() {
    Item.BLOCK_ITEMS.forEach((block, item) -> ITEM_TO_COLOR.put(item, block.getDefaultMapColor()));
  }

  public static MapColor getColor(ItemStack stack) {
    return getColor(stack.getItem());
  }

  public static MapColor getColor(Item item) {
    return ITEM_TO_COLOR.getOrDefault(item, MapColor.CLEAR);
  }
}
