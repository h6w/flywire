/*
    This file is part of Flywire.

    Flywire is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Flywire is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Flywire.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.bentokit.flywire.event;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author cbpaine
 */

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Locale;

import org.bentokit.flywire.errorutils.ErrorHandler;

public class TimedEventManager implements Runnable
{
    private volatile ArrayList<TimedEvent> registeredListeners;
    private static TimedEventManager theManager;
    private static Thread theManagerThread;
    private volatile int incoming = 0;

    private TimedEventManager()
    {
        registeredListeners = new ArrayList<TimedEvent>();
    }

    public static TimedEventManager getManager()
    {
        if (theManager == null)
        {
            theManager = new TimedEventManager();
            theManagerThread = new Thread(theManager);
            theManagerThread.start();
        }
        return theManager;
    }

    public void addTimedEventListener(TimedEvent event)
    {
        // force thread start if it's not running
        if (theManager == null) getManager();
        synchronized(this)
        {
            if (!registeredListeners.contains(event))
            {
                event.setActive(true);
                registeredListeners.add(event);
                ++incoming;
            }
        }
        if (incoming == 1)
        {
            theManagerThread.interrupt();
        }
    }

    public synchronized void removeTimedEventListener(TimedEvent event)
    {
        event.setActive(false);
        registeredListeners.remove(event);
    }

    public void run()
    {
        ErrorHandler.info("TimedEventManager.run - entry");

        boolean keepGoing = true;

        while (keepGoing)
        {
            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("GMT+10"),Locale.ENGLISH);
            ArrayList<TimedEvent> workingCopy = makeWorkingCopy();
            long sleepMillis = Long.MAX_VALUE;

            for (TimedEvent listenerx : workingCopy)
            {
                TimedEventAdapter listener = (TimedEventAdapter) listenerx;
                Calendar next = listener.getNext();
                //ErrorHandler.info("Now: " + now);
                //ErrorHandler.info("Next:" + next);
                if (now.after(next))
                {
                    if (listener.isActive()) listener.doEvent();
                    next = (Calendar) now.clone();
                    int distancetogo = now.get(listener.getDivideField()) % listener.getDivisor();
                    next.add(listener.getDivideField(), listener.getDivisor() - distancetogo);
                    if (listener.getOffsetField() != -1)
                        next.set(listener.getOffsetField(), listener.getOffset());
                    listener.setNext(next);
                }
                long delta = next.getTimeInMillis() - now.getTimeInMillis();
                if (delta < 0) delta = 0;
                if (delta < sleepMillis) sleepMillis = delta;
                //ErrorHandler.info("Maybe sleep for " + sleepMillis + " ms?");
            }
            if (sleepMillis > 0)
            {
                try
                {
                    //ErrorHandler.info("TimedEventManager.run - sleeping for " + sleepMillis + " ms");
                    Thread.sleep(sleepMillis);
                }
                catch (InterruptedException ie)
                {
                        if (incoming > 0)
                        {
                            synchronized (this)
                            {
                                --incoming;
                            }
                        }
                        else
                        {
                            keepGoing = false;
                            ErrorHandler.info("TimedEventManager.run - terminal interrupt " + ie);
                            ErrorHandler.info("TimedEventManager.run - incoming == " + incoming);
                        }
                }
            }
        }
        ErrorHandler.info("TimedEventManager.run is terminating");
    }

    @SuppressWarnings("unchecked")
    private synchronized ArrayList<TimedEvent> makeWorkingCopy()
    {
        return (ArrayList<TimedEvent>) registeredListeners.clone();
    }
}
