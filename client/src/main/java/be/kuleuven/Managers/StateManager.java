package be.kuleuven.Managers;

import be.kuleuven.Instances.*;
import be.kuleuven.*;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.util.*;

public class StateManager {
    private static final String ENCRYPTION_ALGORITHM = "AES"; // // Symmetric encryption algorithm
    private final UserInterface userInterface;

    public StateManager(UserInterface userInterface){
        this.userInterface = userInterface;
    }

    // ********************** SAVING ****************************
    public void saveState(File name, List<Entry> entries_AB, List<Entry> entries_BA, Map<String, List<String>> history) throws IOException {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(name))) {
            writeMapListSection(bufferedWriter, "[history]", history);
            writeEntryListSection(bufferedWriter, "[entries_AB]", entries_AB);
            writeEntryListSection(bufferedWriter, "[entries_BA]", entries_BA);
        }
    }
    // Write a section for a map of lists to the specified BufferedWriter
    private void writeMapListSection(BufferedWriter writer, String sectionHeader, Map<String, List<String>> data) throws IOException {
        writer.append(sectionHeader).append("\n");
        for (Map.Entry<String, List<String>> entry : data.entrySet()) {
            writer.append("* ").append(entry.getKey()).append("\n");
            for (String item : entry.getValue()) {
                writer.append(item);
            }
        }
    }
    // Write a section for a list of entries to the specified BufferedWriter
    private void writeEntryListSection(BufferedWriter writer, String sectionHeader, List<Entry> data) throws IOException {
        writer.append(sectionHeader).append("\n");
        for (Entry entry : data) {
            writer.append("- ").append(entry.getName()).append("\n");
            writer.append(entry.getBulletinEntry().toString()).append("\n");
        }
    }

    // ********************** RESTORING ****************************

    // Restore the state from a file
    public void restoreState(File inputFile, List<Entry> entries_AB, List<Entry> entries_BA, Map<String, List<String>> history) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            String currentSection = null;

            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("[") && line.endsWith("]")) {
                    // New section found
                    currentSection = line.trim();
                } else if (currentSection != null) {
                    // Within a section
                    switch (currentSection) {
                        case "[history]":
                            processHistoryLine(line, history);
                            break;
                        case "[entries_AB]":
                            processEntryLine(line, entries_AB, bufferedReader, true);
                            break;
                        case "[entries_BA]":
                            processEntryLine(line, entries_BA, bufferedReader, false);
                            break;
                        // Add more sections here if needed @brecht
                    }
                }
            }
        }
        System.out.println("Restored State.");
    }

    // Process a line within the history section
    private void processHistoryLine(String line, Map<String, List<String>> history) {
        if (line.startsWith("* ")) {
            String key = line.substring(2);
            history.put(key, new ArrayList<>());
        } else {
            String activeKey = history.keySet().stream().reduce((first, second) -> second).orElse("");

            // Add a new line if there are already messages for the contact
            if (!history.get(activeKey).isEmpty()) {
                history.get(activeKey).add("");
            }
            // Add the message to the history of the contact
            history.get(activeKey).add(line + "\n");
        }
    }

    // Process a line within the entry section
    private void processEntryLine(String line, List<Entry> entries, BufferedReader bufferedReader, boolean showInGUI) throws IOException {
        if (line.startsWith("- ")) {
            String name = line.substring(2);

            // Read the line with BulletinEntry
            String bulletinEntryLine = bufferedReader.readLine();

            // Parse the BulletinEntry data
            String[] bulletinEntryData = bulletinEntryLine.split(";");
            if (bulletinEntryData.length == 3) {
                String secretKeyString = bulletinEntryData[0];
                SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(secretKeyString), 0, Base64.getDecoder().decode(secretKeyString).length, ENCRYPTION_ALGORITHM);
                int boxNumber = Integer.parseInt(bulletinEntryData[1]);
                byte[] tag = bulletinEntryData[2].getBytes();

                Entry entry = new Entry(name, new BulletinEntry(boxNumber, tag, secretKey));
                entries.add(entry);
                if(showInGUI) {
                    userInterface.getContactListModel().addElement(name);
                }
            } else {
                System.err.println("Invalid BulletinEntry-data: " + bulletinEntryLine);
            }
        }
    }



}
