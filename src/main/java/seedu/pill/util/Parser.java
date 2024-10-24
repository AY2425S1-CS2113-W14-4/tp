package seedu.pill.util;

import seedu.pill.command.AddItemCommand;
import seedu.pill.command.DeleteItemCommand;
import seedu.pill.command.EditItemCommand;
import seedu.pill.command.FindCommand;
import seedu.pill.command.HelpCommand;
import seedu.pill.command.ListCommand;
import seedu.pill.command.StockCheckCommand;

import seedu.pill.exceptions.ExceptionMessages;
import seedu.pill.exceptions.PillException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

public class Parser {
    private boolean exitFlag = false;
    private final ItemMap items;
    private final Storage storage;

    /**
     * Public constructor for Parser.
     */
    public Parser(ItemMap items, Storage storage) {
        this.items = items;
        this.storage = storage;
    }

    /**
     * Processes the user's command.
     *
     * @param input The user's input command from the scanner.
     */
    public void parseCommand(String input) {
        try {
            String[] splitInput = input.split("\\s+");
            String commandString = splitInput[0].toLowerCase();
            String argument = splitInput.length > 1 ? splitInput[1] : null;
            String flagStr = splitInput.length > 2 ? splitInput[2] : "";
            String arguments = String.join(" ", Arrays.copyOfRange(splitInput, 1, splitInput.length));

            switch (commandString) {
            case "exit":
                this.exitFlag = true;
                break;
            case "add":
                parseAddItemCommand(arguments).execute(this.items, this.storage);
                break;
            case "delete":
                parseDeleteItemCommand(arguments).execute(this.items, this.storage);
                break;
            case "edit":
                parseEditItemCommand(arguments).execute(this.items, this.storage);
                break;
            case "find":
                new FindCommand(arguments).execute(this.items, this.storage);
                break;
            case "help":
                boolean flag = flagStr.equals("-v");
                new HelpCommand(argument, flag).execute(this.items, this.storage);
                break;
            case "list":
                if (splitInput.length > 1) {
                    throw new PillException(ExceptionMessages.TOO_MANY_ARGUMENTS);
                }
                new ListCommand().execute(this.items, this.storage);
                break;
            case "stock-check":
                if (splitInput.length > 2) {
                    throw new PillException(ExceptionMessages.TOO_MANY_ARGUMENTS);
                }
                new StockCheckCommand(argument).execute(this.items, this.storage);
                break;
            default:
                throw new PillException(ExceptionMessages.INVALID_COMMAND);
            }
        } catch (PillException e) {
            PillException.printException(e);
        }
    }

    /**
     * Parses the user input and creates an {@code AddItemCommand} object.
     * The input is expected to contain the item name, quantity, and optional expiry date.
     * If a valid date is found, it must be the last element in the input.
     * Only one date and one quantity are allowed.
     *
     * The method loops through the input array to determine the item name, quantity, and expiry date,
     * applying default values when necessary (e.g., quantity defaults to 1 if not specified).
     *
     * @param arguments A string representing the user's input for adding an item
     * @return An {@code AddItemCommand} containing the parsed item name, quantity, and optional expiry date.
     * @throws PillException If the input format is invalid
     */
    private AddItemCommand parseAddItemCommand(String arguments) throws PillException {
        String[] splitArguments = arguments.split("\\s+");

        if (splitArguments.length == 0) {
            throw new PillException(ExceptionMessages.INVALID_ADD_COMMAND);
        }

        Integer quantityIndex = null;
        Integer dateIndex = null;

        for (int i = 0; i < splitArguments.length; i++) {
            String currentArgument = splitArguments[i];

            if (isValidDate(currentArgument)) {
                if (dateIndex != null) {
                    throw new PillException(ExceptionMessages.INVALID_ADD_COMMAND);
                }
                dateIndex = i;

                if (i != splitArguments.length - 1) {
                    throw new PillException(ExceptionMessages.INVALID_ADD_COMMAND);
                }
            }

            if (isANumber(currentArgument)) {
                quantityIndex = i;
            }
        }

        String itemName;
        String quantityStr = null;
        String expiryDateStr = null;

        if (quantityIndex != null && quantityIndex == splitArguments.length - 1) {
            quantityStr = splitArguments[quantityIndex];
            expiryDateStr = null;
            itemName = buildItemName(splitArguments, 0, quantityIndex);
        } else if (quantityIndex != null && quantityIndex == splitArguments.length - 2
                && isValidDate(splitArguments[quantityIndex + 1])) {
            quantityStr = splitArguments[quantityIndex];
            expiryDateStr = splitArguments[quantityIndex + 1];
            itemName = buildItemName(splitArguments, 0, quantityIndex);
        } else if (dateIndex != null && dateIndex == splitArguments.length - 1) {
            expiryDateStr = splitArguments[dateIndex];
            quantityStr = "1";
            itemName = buildItemName(splitArguments, 0, dateIndex);
        } else {
            quantityStr = "1";
            expiryDateStr = null;
            itemName = buildItemName(splitArguments, 0, splitArguments.length);
        }

        assert !itemName.isEmpty() : "Item name should not be empty";

        assert isANumber(quantityStr) : "Quantity should be a valid number";

        return new AddItemCommand(itemName, parseQuantity(quantityStr), parseExpiryDate(expiryDateStr));
    }

    /**
     * Parses the user input and creates a {@code DeleteItemCommand} object.
     * The input is expected to contain the item name and optionally an expiry date.
     * If a valid date is found, it must be the last element in the input.
     *
     * The method constructs the item name by looping through the input until a valid date is found.
     * Any valid date found is treated as the item's expiry date.
     *
     * @param arguments A string representing the user's input for deleting an item
     * @return A {@code DeleteItemCommand} containing the parsed item name and optional expiry date.
     * @throws PillException If the input format is invalid (e.g., more than one date or no item name provided).
     */
    private DeleteItemCommand parseDeleteItemCommand(String arguments) throws PillException {
        String[] splitArguments = arguments.split("\\s+");

        if (splitArguments.length == 0) {
            throw new PillException(ExceptionMessages.INVALID_DELETE_COMMAND);
        }

        StringBuilder itemNameBuilder = new StringBuilder();
        int currentIndex = 0;
        String expiryDateStr = null;

        while (currentIndex < splitArguments.length) {
            if (isValidDate(splitArguments[currentIndex])) {
                expiryDateStr = splitArguments[currentIndex];
                break;
            }
            if (currentIndex > 0) {
                itemNameBuilder.append(" ");
            }
            itemNameBuilder.append(splitArguments[currentIndex]);
            currentIndex++;
        }

        String itemName = itemNameBuilder.toString().trim();

        assert !itemName.isEmpty() : "Item name should not be empty";

        LocalDate expiryDate = expiryDateStr != null ? parseExpiryDate(expiryDateStr) : null;

        return new DeleteItemCommand(itemName, expiryDate);
    }




    /**
     * Parses the user input to create an {@code EditItemCommand} object.
     * The input is expected to contain the item name, the new quantity, and optionally the expiry date.
     * The expiry date, if present, must be the second-to-last element, with the quantity being the last element.
     *
     * The method loops through the input to determine the item name, quantity, and optional expiry date.
     * The quantity is mandatory for editing, while the expiry date is optional.
     *
     * @param arguments A string representing the user's input for editing an item.
     * @return An {@code EditItemCommand} containing the parsed item name, quantity, and optional expiry date.
     * @throws PillException If the input format is invalid (e.g., no quantity provided, or multiple dates found).
     */
    private EditItemCommand parseEditItemCommand(String arguments) throws PillException {
        String[] splitArguments = arguments.split("\\s+");

        if (splitArguments.length == 0) {
            throw new PillException(ExceptionMessages.INVALID_EDIT_COMMAND);
        }

        Integer quantityIndex = null;
        Integer dateIndex = null;

        for (int i = 0; i < splitArguments.length; i++) {
            String currentArgument = splitArguments[i];

            if (isValidDate(currentArgument)) {
                if (dateIndex != null) {
                    throw new PillException(ExceptionMessages.INVALID_EDIT_COMMAND);
                }
                dateIndex = i;

                if (i != splitArguments.length - 1 && i != splitArguments.length - 2) {
                    throw new PillException(ExceptionMessages.INVALID_EDIT_COMMAND);
                }
            }

            if (isANumber(currentArgument)) {
                quantityIndex = i;
            }
        }

        String itemName;
        String quantityStr = null;
        String expiryDateStr = null;

        if (quantityIndex != null && quantityIndex == splitArguments.length - 1) {
            quantityStr = splitArguments[quantityIndex];
            expiryDateStr = null;
            itemName = buildItemName(splitArguments, 0, quantityIndex);
        } else if (quantityIndex != null && quantityIndex == splitArguments.length - 2
                && isValidDate(splitArguments[quantityIndex + 1])) {
            quantityStr = splitArguments[quantityIndex];
            expiryDateStr = splitArguments[quantityIndex + 1];
            itemName = buildItemName(splitArguments, 0, quantityIndex);
        } else {
            throw new PillException(ExceptionMessages.INVALID_EDIT_COMMAND);
        }

        assert !itemName.isEmpty() : "Item name should not be empty";

        assert isANumber(quantityStr) : "Quantity should be a valid number";

        return new EditItemCommand(itemName, parseQuantity(quantityStr), parseExpiryDate(expiryDateStr));
    }

    /**
     * Checks if the provided string represents a valid date in the format {@code YYYY-MM-DD}.
     * This method attempts to parse the string into a {@code LocalDate} object.
     *
     * @param dateStr A string representing the date to be validated.
     * @return {@code true} if the string is a valid date, {@code false} otherwise.
     */
    private boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Constructs the item name by concatenating the elements of the input array from startIndex to endIndex.
     * The method ensures that the item name is built by appending each element with a space between words.
     *
     * @param splitArguments An array of strings representing the user's input.
     * @param startIndex The starting index (inclusive) for building the item name.
     * @param endIndex The ending index (exclusive) for building the item name.
     * @return A string representing the item name, constructed from the input array.
     */
    private String buildItemName(String[] splitArguments, int startIndex, int endIndex) {
        StringBuilder itemNameBuilder = new StringBuilder();
        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                itemNameBuilder.append(" ");
            }
            itemNameBuilder.append(splitArguments[i]);
        }
        return itemNameBuilder.toString().trim();
    }

    /**
     * Parses a string representing an expiry date into a {@code LocalDate} object.
     *
     * @param expiryDateStr A string representing the expiry date in ISO-8601 format (yyyy-MM-dd).
     * @return A {@code LocalDate} object representing the expiry date, or {@code null} if no expiry date is provided.
     * @throws PillException If the expiry date string is not in the correct format.
     */
    private LocalDate parseExpiryDate(String expiryDateStr) throws PillException {
        try {
            if (expiryDateStr == null) {
                return null;
            }
            return LocalDate.parse(expiryDateStr);
        } catch (DateTimeParseException e) {
            throw new PillException(ExceptionMessages.PARSE_DATE_ERROR);
        }
    }

    /**
     * Checks if a given string is a valid integer number.
     *
     * @param s The string to check.
     * @return {@code true} if the string can be parsed into an integer; {@code false} otherwise.
     */
    private boolean isANumber(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Parses the quantity string into an integer.
     *
     * @param quantityStr The string representation of the quantity to parse.
     * @return The parsed quantity as an integer.
     * @throws PillException If the quantity is not a valid positive integer.
     */
    private int parseQuantity(String quantityStr) throws PillException {
        try {
            int quantity = Integer.parseInt(quantityStr);
            assert quantity > 0 : "Quantity must be positive";
            if (quantity <= 0) {
                throw new PillException(ExceptionMessages.INVALID_QUANTITY);
            }
            return quantity;
        } catch (NumberFormatException e) {
            throw new PillException(ExceptionMessages.INVALID_QUANTITY_FORMAT);
        }
    }

    /**
     * Returns an exit flag for the Pill bot to exit.
     *
     * @return The state of exit flag.
     */
    public boolean getExitFlag() {
        return this.exitFlag;
    }
}
