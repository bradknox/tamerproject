package org.rlcommunity.environments.mario.sonar;

public interface SoundProducer
{
    public float read(float[] buf, int readRate);
    public void skip(int samplesToSkip, int readRate);
    public boolean isLive();
}