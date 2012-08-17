package org.bentokit.flywire.media;

public class MediaItemComparator implements java.util.Comparator<MediaItem> {
    public int compare(MediaItem m1, MediaItem m2) {
        String str1 = m1.getURI().toString();
        // FIXME: at best, lame. Should be configurable.
        return(str1.compareToIgnoreCase(m2.getURI().toString()));
    }
}