package com.vpp.mqttclient.services;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeMap;

@Slf4j
public class PropertiesFromFile {

    public static TreeMap loadPropertiesFromFile(String fileLocation) throws IOException {
         TreeMap<String, String> map = getProperties(fileLocation);
         System.out.println(map);
         return map;
    }

    private static TreeMap<String, String> getProperties(String infile) throws IOException {
        final int lhs = 0;
        final int rhs = 1;

        TreeMap<String, String> map = new TreeMap<String, String>();
        BufferedReader bfr = new BufferedReader(new FileReader(new File(infile)));

        String line;
        while ((line = bfr.readLine()) != null) {
            if (!line.startsWith("#") && !line.isEmpty()) {
                String[] pair = line.trim().split("=");
                map.put(pair[lhs].trim(), pair[rhs].trim());
            }
        }

        bfr.close();

        return(map);
    }
}
