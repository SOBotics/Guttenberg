package org.sobotics.guttenberg.printers;

import org.sobotics.guttenberg.entities.PostMatch;
import org.sobotics.guttenberg.finders.PlagFinder;

/**
 * Created by bhargav.h on 20-Oct-16.
 */
public interface PostPrinter {
    public String print(PlagFinder finder);
    public String print(PostMatch match);
}
