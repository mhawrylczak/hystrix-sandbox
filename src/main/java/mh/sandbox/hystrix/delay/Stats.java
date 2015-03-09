package mh.sandbox.hystrix.delay;


public class Stats {
    public long createdTime = System.currentTimeMillis();
    public long commandStartTime;
    public long commandStopTime;
    public long observedTime;

    @Override
    public String toString() {
        return "Stats{" +
                "createdTime=" + createdTime +
                ", commandStartTime=" + commandStartTime +
                ", commandStopTime=" + commandStopTime +
                ", observedTime=" + observedTime +
                '}';
    }
}
