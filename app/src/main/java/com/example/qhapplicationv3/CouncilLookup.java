package com.example.qhapplicationv3;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public final class CouncilLookup {
    private CouncilLookup() {}

    //Stores all council names for selection
    private static final List<String> COUNCILS = Arrays.asList(
            "Aurukun Shire Council",
            "Balonne Shire Council",
            "Banana Shire Council",
            "Barcaldine Regional Council",
            "Barcoo Shire Council",
            "Blackall-Tambo Regional Council",
            "Boulia Shire Council",
            "Bulloo Shire Council",
            "Bundaberg Regional Council",
            "Burdekin Shire Council",
            "Burke Shire Council",
            "Cairns Regional Council",
            "Carpentaria Shire Council",
            "Cassowary Coast Regional Council",
            "Central Highlands Regional Council",
            "Charters Towers Regional Council",
            "Cherbourg Aboriginal Shire Council",
            "Cloncurry Shire Council",
            "Cook Shire Council",
            "Croydon Shire Council",
            "Diamantina Shire Council",
            "Doomadgee Aboriginal Shire Council",
            "Douglas Shire Council",
            "Etheridge Shire Council",
            "Flinders Shire Council",
            "Fraser Coast Regional Council",
            "Gladstone Regional Council",
            "Gold Coast City Council",
            "Goondiwindi Regional Council",
            "Gympie Regional Council",
            "Hinchinbrook Shire Council",
            "Hope Vale Aboriginal Shire Council",
            "Ipswich City Council",
            "Isaac Regional Council",
            "Kowanyama Aboriginal Shire Council",
            "Livingstone Shire Council",
            "Lockhart River Aboriginal Shire Council",
            "Lockyer Valley Regional Council",
            "Logan City Council",
            "Longreach Regional Council",
            "Mackay Regional Council",
            "Mapoon Aboriginal Shire Council",
            "Maranoa Regional Council",
            "Mareeba Shire Council",
            "McKinlay Shire Council",
            "Moreton Bay City Council",
            "Mornington Shire Council",
            "Mount Isa City Council",
            "Murweh Shire Council",
            "Napranum Aboriginal Shire Council",
            "Noosa Shire Council",
            "North Burnett Regional Council",
            "Northern Peninsual Area Regional Council",
            "Palm Island Aboriginal Shire Council",
            "Paroo Shire Council",
            "Pormpuraaw Aboriginal Shire Council",
            "Quilpie Shire Council",
            "Redland City Council",
            "Richmond Shire Council",
            "Rockhampton Regional Council",
            "Scenic Rim Regional Council",
            "Somerset Regional Council",
            "South Burnett Regional Council",
            "Southern Downs Regional Council",
            "Sunshine Coast Regional Council",
            "Tablelands Regional Council",
            "Toowoomba Regional Council",
            "Torres Shire Council",
            "Torres Strait Island Regional Council",
            "Townsville City Council",
            "Weipa Town",
            "Western Downs Regional Council",
            "Whitsunday Regional Council",
            "Winton Shire Council",
            "Woorabinda Aboriginal Shire Council",
            "Wujal Wujal Aboriginal Shire Council",
            "Yarrabah Aboriginal Shire Council"
    );

    //Returns a list of all council names
    public static List<String> all() {
        return new ArrayList<>(COUNCILS);
    }

    //Fixes any formatting issues when inputting a council name
    public static String toDisplay(String anyForm) {
        if (TextUtils.isEmpty(anyForm)) return "";
        String norm = slug(anyForm);
        for (String c : COUNCILS) {
            if (slug(c).equals(norm)) return c;
        }
        return titleFromSlug(norm);
    }

    //Replaces unsafe text with hyphens
    public static String slug(String s) {
        if (s == null) return "";
        String t = s.trim().toLowerCase();
        t = t.replaceAll("[^a-z0-9]+", "-");
        t = t.replaceAll("^-+|-+$", "");
        return t;
    }

    //Fixes dashed text
    private static String titleFromSlug(String normSlug) {
        if (TextUtils.isEmpty(normSlug)) return "";
        String[] parts = normSlug.split("-");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            if (p.length() == 0) continue;
            String cap = p.substring(0, 1).toUpperCase() + (p.length() > 1 ? p.substring(1) : "");
            if (sb.length() > 0) sb.append(' ');
            sb.append(cap);
        }
        return sb.toString();
    }
}
