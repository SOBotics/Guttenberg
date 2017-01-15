package org.sobotics.guttenberg.printers;

import java.util.List;

import org.sobotics.guttenberg.finders.PlagFinder;

/**
 * Created by bhargav.h on 20-Oct-16.
 */
public class SoBoticsPostPrinter implements PostPrinter {

    public final long roomId = 111347;

    @Override
    public String print(PlagFinder finder) {

        return finder.getTargetAnswer().toString();
    }
}
