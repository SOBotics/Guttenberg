package org.sobotics.guttenberg.printers;

import org.sobotics.guttenberg.finders.PlagFinder;

/**
 * Created by bhargav.h on 20-Oct-16.
 */
public interface PostPrinter {
    public String print(PlagFinder finder);
}
