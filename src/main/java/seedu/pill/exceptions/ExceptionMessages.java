package seedu.pill.exceptions;

public enum ExceptionMessages {
    INVALID_COMMAND         ("Invalid command, please try again." +
                                "\nType the 'help' command for a list of valid commands."),
    SAVE_ERROR              ("Error saving to file, please try again."),
    LOAD_ERROR              ("Error loading saved data"),
    INVALID_LINE_FORMAT     ("File corrupted. Ignoring invalid line format..."),
    INVALID_QUANTITY        ("Quantity is invalid, please try again"),
    INVALID_QUANTITY_FORMAT ("Quantity provided is not a number, please try again."),
    INVALID_ADD_COMMAND     ("Invalid Add command format..."),
    INVALID_DELETE_COMMAND  ("Invalid Delete command format..."),
    INVALID_EDIT_COMMAND    ("Invalid Edit command format..."),
    PARSE_DATE_ERROR        ("Date provided is in the wrong format, please try again."),
    TOO_MANY_ARGUMENTS      ("Too many arguments. Please type 'help' for accepted commands.");

    private final String message;

    /**
     * Constructor for ExceptionMessages.
     *
     * @param message The message of the exception.
     */
    ExceptionMessages(String message) {
        this.message = message;
    }

    /**
     * Gets the message of the exception.
     *
     * @return The message of the exception.
     */
    public String getMessage() {
        return this.message;
    }
}
