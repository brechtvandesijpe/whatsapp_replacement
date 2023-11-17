package be.kuleuven.Managers;

import be.kuleuven.Instances.Entry;

import java.io.*;
import java.util.*;

public class StateManager {

    public StateManager(){}

    public static void  saveState(File name, List<Entry> entries_AB, List<Entry> entries_BA, Map<String, List<String>> history) throws IOException {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(name))) {
            writeMapListSection(bufferedWriter, "[history]", history);
            writeEntryListSection(bufferedWriter, "[entries_AB]", entries_AB);
            writeEntryListSection(bufferedWriter, "[entries_BA]", entries_BA);

            // TODO: niet goed

        }
    }

    public void restoreState() {

    }

    private static void writeMapListSection(BufferedWriter writer, String sectionHeader, Map<String, List<String>> data) throws IOException {
        writer.append(sectionHeader).append("\n");
        for (Map.Entry<String, List<String>> entry : data.entrySet()) {
            writer.append("* ").append(entry.getKey()).append("\n");
            for (String item : entry.getValue()) {
                writer.append(item).append("\n");
            }
        }
    }

    private static void writeEntryListSection(BufferedWriter writer, String sectionHeader, List<Entry> data) throws IOException {
        writer.append(sectionHeader).append("\n");
        for (Entry entry : data) {
            writer.append("- ").append(entry.getName()).append("\n");
            writer.append(entry.getBulletinEntry().toString()).append("\n");
        }
    }


}
