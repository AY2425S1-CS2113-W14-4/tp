package seedu.pill.util;

import seedu.pill.exceptions.ExceptionMessages;
import seedu.pill.exceptions.PillException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.util.TreeSet;

/**
 * The Storage class handles the storage of ItemMap objects
 * in a file-based system, allowing for saving items and lists
 * of items to a specified text file.
 */
public class Storage {
    private static final String PATH = "./data/";
    private static final String FILE_NAME = "pill.txt";
    private static final String SEPARATOR = ",";

    /**
     * Initializes the storage file and creates the necessary
     * directories if they do not exist.
     *
     * @return The File object representing the storage file.
     * @throws IOException if an I/O error occurs during file creation.
     */
    private static File initializeFile() throws IOException {
        File dir = new File(PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        assert dir.isDirectory();

        File items = new File(dir, FILE_NAME);
        if (!items.exists()) {
            items.createNewFile();
        }
        assert items.isFile();

        return items;
    }

    /**
     * Saves the provided ItemMap to the storage file, overwriting
     * existing content.
     *
     * @param itemMap The {@link ItemMap} containing items to be saved.
     * @throws PillException if an error occurs during the saving process.
     */
    public void saveItemMap(ItemMap itemMap) throws PillException {
        try {
            File file = initializeFile();
            FileWriter fw = new FileWriter(file);

            for (String itemName : itemMap.items.keySet()) {
                TreeSet<Item> itemSet = itemMap.items.get(itemName);
                for (Item item : itemSet) {
                    fw.write(item.getName() + SEPARATOR + item.getQuantity());

                    if (item.getExpiryDate().isPresent()) {
                        fw.write(SEPARATOR + item.getExpiryDate().get().toString());
                    } else {
                        fw.write(SEPARATOR + "");
                    }

                    fw.write(System.lineSeparator());
                }
            }

            fw.close();
        } catch (IOException e) {
            throw new PillException(ExceptionMessages.SAVE_ERROR);
        }
    }



    /**
     * Appends a single item to the storage file.
     *
     * @param item The {@link Item} to be saved.
     * @throws PillException if an error occurs during the saving process.
     */
    public void saveItem(Item item) throws PillException {
        try {
            File file = initializeFile();
            FileWriter fw = new FileWriter(file, true);
            fw.write(item.getName() + SEPARATOR + item.getQuantity());
            if (item.getExpiryDate().isPresent()) {
                fw.write(SEPARATOR + item.getExpiryDate().get());
            }
            fw.write(System.lineSeparator());
            fw.close();
        } catch (IOException e) {
            throw new PillException(ExceptionMessages.SAVE_ERROR);
        }
    }

    /**
     * Loads saved CSV data into an ItemMap
     *
     * @return The ItemMap containing saved items
     */
    public ItemMap loadData() {
        ItemMap loadedItems = new ItemMap();
        try {
            File file = initializeFile();
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                try {
                    String line = scanner.nextLine();
                    Item item = loadLine(line);
                    loadedItems.addItemSilent(item);
                } catch (PillException e) {
                    PillException.printException(e);
                }
            }
            scanner.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return loadedItems;
    }

    /**
     * Returns data in current line as an Item
     * @param line Next line string read by the scanner
     * @return The item present in the line
     * @throws PillException if format of saved data is incorrect
     */
    public Item loadLine(String line) throws PillException {
        Item item;
        String[] data = line.split(SEPARATOR);
        if (data.length == 3) {
            try {
                item = new Item(data[0], Integer.parseInt(data[1]), LocalDate.parse(data[2]));
            } catch (NumberFormatException e) {
                throw new PillException(ExceptionMessages.INVALID_QUANTITY_FORMAT);
            } catch (DateTimeParseException e) {
                throw new PillException(ExceptionMessages.PARSE_DATE_ERROR);
            }
        } else if (data.length == 2) {
            try {
                item = new Item(data[0], Integer.parseInt(data[1]));
            } catch (NumberFormatException e) {
                throw new PillException(ExceptionMessages.INVALID_QUANTITY_FORMAT);
            }
        } else {
            throw new PillException(ExceptionMessages.INVALID_LINE_FORMAT);
        }
        return item;
    }
}
