package org.redcastlemedia.multitallented.civs.items;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;

import lombok.Getter;

public class ItemGroupList {

    @Getter
    private String input;
    @Getter
    private final Set<String> groupName = new HashSet<>();
    @Getter
    private String mainGroup = null;
    @Getter
    private String circularDependency = null;

    public void findAllGroupsRecursively(String input) {
        this.input = input;
        int i=0;
        while (input.contains("g:")) {
            String itemGroup = null;
            String params = null;
            String currentKey = null;
            for (String currKey : ConfigManager.getInstance().getItemGroups().keySet()) {
                Pattern pattern = Pattern.compile("g:" + currKey + "(?![_A-Za-z])");
                Matcher matcher = pattern.matcher(input);
                if (matcher.find()) {
                    if (groupName.contains(currKey)) {
                        circularDependency = currKey;
                        return;
                    }
                    currentKey = currKey;
                    groupName.add(currKey);
                    if (mainGroup == null) {
                        mainGroup = currKey;
                    }
                    itemGroup = ConfigManager.getInstance().getItemGroups().get(currKey);
                    params = input.substring(matcher.start());
                    params = params.split(",")[0];
                    params = params.replace("g:" + currKey, "");
                    break;
                }
            }
            if (groupName.isEmpty() || itemGroup == null) {
                return;
            }
            input = buildReplacementString(input, itemGroup, params, currentKey);

            if (i > 1000) {
                Civs.logger.log(Level.SEVERE, "Failed to find group for {0}", input);
                return;
            }
            i++;
        }
    }

    private String buildReplacementString(String input, String itemGroup, String params, String currentKey) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String chunk : itemGroup.split(",")) {
            stringBuilder.append(chunk);
            stringBuilder.append(params);
            stringBuilder.append(",");
        }

        String groupReplacement = stringBuilder.substring(0, stringBuilder.length() - 1);
        this.input = input.replace("g:" + currentKey + params, groupReplacement);
        input = this.input;
        return input;
    }
}
