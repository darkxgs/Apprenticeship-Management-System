package com.pvtd.students;

import com.pvtd.students.services.DictionaryService;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class DBDump {
    public static void main(String[] args) {
        try {
            List<String> items = DictionaryService.getCombinedItems(DictionaryService.CAT_PROFESSION);
            Files.write(Paths.get("professions_dump.txt"), items);
            System.out.println("Dumped " + items.size() + " items.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
