package com.zergatul.cheatutils.scripting.types;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.PropertyDescription;
import com.zergatul.scripting.type.CustomType;

import java.util.regex.MatchResult;

@CustomType(name = "MatchGroup")
public class MatchGroup {

    private final MatchResult result;
    private final int index;

    public MatchGroup(MatchResult result, int index) {
        this.result = result;
        this.index = index;
    }

    @PropertyDescription("Capture group number; 0 represents the full match.")
    @Getter(name = "index")
    public int getIndex() {
        return index;
    }

    @Getter(name = "length")
    public int getLength() {
        if (0 <= index && index <= result.groupCount()) {
            return result.end(index) - result.start(index);
        } else {
            return -1;
        }
    }

    @PropertyDescription("May return null when an optional group did not match.")
    @Getter(name = "value")
    public String getValue() {
        if (0 <= index && index <= result.groupCount()) {
            return result.group(index);
        } else {
            return "";
        }
    }

    @PropertyDescription("True when the group exists; it may still be unmatched.")
    @Getter(name = "valid")
    public boolean getValid() {
        return 0 <= index && index <= result.groupCount();
    }
}