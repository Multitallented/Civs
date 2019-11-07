package org.redcastlemedia.multitallented.civs.update;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class UpdateTests {
    @Before
    public void setup() {

    }

    @Test
    public void fixPotionStringShouldWork() {
        List<String> effectList = new ArrayList<>();
        effectList.add("potion:^BLINDNESS.160,^SLOW.80.2,^WEAKNESS.160");
        Update1d5d6.fixPotionDuration(effectList);
        assertEquals("potion:^BLINDNESS.8,^SLOW.4.2,^WEAKNESS.8", effectList.get(0));
    }
}
