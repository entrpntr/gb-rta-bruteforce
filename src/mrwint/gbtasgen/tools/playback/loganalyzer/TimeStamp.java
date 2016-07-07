package mrwint.gbtasgen.tools.playback.loganalyzer;

public class TimeStamp implements Comparable<TimeStamp> {
  public final int scene;
  public final int frame;
  public final int frameCycle;

  public TimeStamp(int scene, int frame, int frameCycle) {
    this.scene = scene;
    this.frame = frame;
    this.frameCycle = frameCycle;
  }

  @Override
  public int compareTo(TimeStamp o) {
    if (scene != o.scene)
      return scene - o.scene;
    if (frame != o.frame)
      return frame - o.frame;
    return frameCycle - o.frameCycle;
  }
}
