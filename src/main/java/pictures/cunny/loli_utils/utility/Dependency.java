package pictures.cunny.loli_utils.utility;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public record Dependency(String dep) {
  public boolean isLoaded() {
    return FabricLoader.getInstance().isModLoaded(dep);
  }

  public ModContainer get() {
    return FabricLoader.getInstance().getModContainer(dep).orElseThrow();
  }
}
