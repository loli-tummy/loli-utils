package pictures.cunny.loli_utils.utility;

public class RenderWrap {
  private int fadeTime;
  private int breath;

  public RenderWrap(int fadeTime, int breath) {
    this.fadeTime = fadeTime;
    this.breath = breath;
  }

  public int fadeTime() {
    return fadeTime;
  }

  public int fadeTime(int fadeTime) {
    this.fadeTime = fadeTime;
    return fadeTime;
  }

  public int breath() {
    return breath;
  }

  public int breath(int breath) {
    this.breath = breath;
    return breath;
  }
}
