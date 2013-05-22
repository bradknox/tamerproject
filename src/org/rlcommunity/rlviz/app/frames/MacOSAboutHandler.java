package org.rlcommunity.rlviz.app.frames;

import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.Application;

public class MacOSAboutHandler extends Application {

    public MacOSAboutHandler() {
        addApplicationListener(new AboutBoxHandler());
    }

    class AboutBoxHandler extends ApplicationAdapter {
        @Override
        public void handleAbout(ApplicationEvent event) {
            GenericVizFrame.showAboutBox();
            event.setHandled(true);
        }
        
        @Override
          public void handleQuit(ApplicationEvent ae) {
              ae.setHandled(true);
         }
    }
}