package de.themoep.clancontrol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * BorderWalker
 * Copyright (C) 2015 Max Lee (https://github.com/Phoenix616/)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
public class BorderWalker {
    Map<Region, Region> walkedRegions = new HashMap<Region, Region>();
    Stack<Region> intersections = new Stack<Region>();
    Region start;
    Region walked;
    
    public BorderWalker(Region region) {
        start = region;
        walked = start;
        walkedRegions.put(start, null);
    }

    public Map<Region, Region> walk() {
        List<Region> borderRegions = walked.getSurroundingRegions(RegionStatus.BORDER, walked.getController());
        if(borderRegions.size() > 2) {
            intersections.push(walked);
        }
        for(Region r : borderRegions) {
            // Make sure this region isn't in the center
            if(!r.checkSurroungings()) {
                // Don't walk into regions we already visited
                if (!walkedRegions.containsKey(r)) {
                    // Associate the next region with the current one
                    walkedRegions.put(r, walked);
                    // Set the current region to the next one
                    walked = r;
                    // Do the next step
                    walk();
                    break;
                }
                if(start == r) {
                    // Associated the start region with the current (last) one
                    walkedRegions.put(start, walked);
                    // (Only gets called if no new region got found and the loop therefor doesn't break)
                }
            }
        }
        if(!intersections.empty() && walkedRegions.get(start) == null) {
            walked = intersections.pop();
            walk();
        }
        return (walkedRegions.get(start) == null) ? null : walkedRegions;
    }
}
