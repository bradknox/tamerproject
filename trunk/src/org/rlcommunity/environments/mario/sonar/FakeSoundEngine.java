package org.rlcommunity.environments.mario.sonar;

import org.rlcommunity.environments.mario.sonar.sample.*;


public class FakeSoundEngine extends SonarSoundEngine
{
    public void setListener(SoundListener soundListener)
    {
    }

    public void shutDown()
    {
    }

    public SonarSample loadSample(String resourceName)
    {
        return null;
    }

    public void play(SonarSample sample, SoundSource soundSource, float volume, float priority, float rate)
    {
    }

    public void clientTick(float alpha)
    {
    }

    public void tick()
    {
    }

    public void run()
    {
    }
}