package com.megaproject.connecto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationDataProvider {

    // Simulating a database of locations
    // Structure: Parent -> List of Children
    private static final Map<String, List<String>> DATA = new HashMap<>();

    static {
        // Root: Countries
        DATA.put("ROOT", Arrays.asList("India", "USA", "UK", "Australia"));

        // Country -> States
        DATA.put("India", Arrays.asList("Maharashtra", "Karnataka", "Gujarat", "Delhi"));
        DATA.put("USA", Arrays.asList("California", "New York", "Texas"));

        // State -> Districts/Talukas (Simulated)
        DATA.put("Maharashtra", Arrays.asList("Pune", "Mumbai", "Nagpur", "Nashik", "Aurangabad(Chatrapati Sambhajinagar)"));
        DATA.put("Karnataka", Arrays.asList("Bengaluru", "Mysuru", "Hubli"));

        // District -> Cities/Villages (Simulated)
        DATA.put("Pune", Arrays.asList("Wakad", "Hinjewadi", "Baner", "Kothrud", "Hadapsar"));
        DATA.put("Mumbai", Arrays.asList("Andheri", "Bandra", "Juhu"));
        
        // Aurangabad District -> Talukas
        DATA.put("Aurangabad(Chatrapati Sambhajinagar)", Arrays.asList(
            "Aurangabad", "Paithan", "Gangapur", "Vaijapur", "Kannad", 
            "Khultabad", "Sillod", "Soygaon", "Phulambri"
        ));
        
        // Add more specific path requested by user: India -> Maharashtra -> Pune -> Wakad
    }

    public static List<String> getItems(String parent) {
        if (parent == null) {
            return DATA.get("ROOT");
        }
        List<String> items = DATA.get(parent);
        if (items == null) {
            return new ArrayList<>(); // Empty list if no children defined
        }
        return items;
    }

    public static boolean hasChildren(String item) {
        return DATA.containsKey(item) && !DATA.get(item).isEmpty();
    }
}
