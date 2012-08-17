package org.bentokit.flywire.plugins.announcements;

import java.util.Calendar;

public interface AnnouncementsListener {
    public void announcementsChanged(Calendar newTime, SelectionList newSelection);
    public void announcementsError(String message);
    public void announcementsNone();
}
