

# Pharmacy Inventory & Logistics Ledger (PILL) User Guide

**Version 1.0**

1. [Introduction](#introduction)
2. [Features](#features)
    - [Viewing Help: `help`](#viewing-help-help)
    - [Adding New Item: `add`](#adding-new-item-add)
    - [Listing All Items: `list`](#listing-all-items-list)
    - [Deleting Existing Item: `delete`](#deleting-existing-item-delete)
    - [Exiting the Program: `exit`](#exiting-the-program-exit)
    - [Saving the Data](#saving-the-data)
    - [Editing the Data File](#editing-the-data-file)

## Introduction

Pharmacy Inventory & Logistics Ledger (PILL) is a Command Line Interface (CLI) tool designed to assist in managing and tracking medicinal inventory.



## Features

### Viewing Help: `help`

Displays a list of all available commands and their descriptions.

**Format**: `help`

**Sample Output**:

`> help`

```
help
- Displays a list of all available commands and their descriptions
```

```
add n/NAME q/QUANTITY
- Allows the user to add a new item to the inventory
```

```
list
- Shows all the items that have been added to the inventory
```

```
delete INDEX 
- Removes a specified item index from the inventory
```

```
exit
- Terminates the application
```



---

### Adding New Item: `add`

Adds a new item to the inventory, specifying its name and quantity.

**Format**: `add n/NAME e/QUANTITY`

**Sample Output**:

`> add n/Panadol q/2`

```
Added the following item to the inventory:
1. Panadol: 2 in stock
```



---

### Listing All Items: `list`

Displays a list of all items currently stored in the inventory, including their names and quantities.

**Format**: `list`

**Sample Output**:

`> list`

```
1. Panadol: 2 in stock
2. Ibuprofen: 1 in stock 
```



---

### Deleting Existing Item: `delete`

Deletes an existing item entry in the inventory.

**Format**: `delete n/NAME`

- Delete the items with specified`NAME` .

**Example**:

- `delete n/NAME`  
  deletes the item as referenced in the `list`.

**Sample Output**:

`> delete n/Ibuprofen`

```
Deleted the following item from the inventory: 
	2. Ibuprofen: 1
```



---

### Exiting the Program: `exit`

Exits the program.

**Format**: `exit`

**Sample Output**:

`> exit`

`Exiting the inventory interface. Have a nice day! :)`



---

### Saving the Data

The system automatically saves any changes to the inventory to the hard disk after commands that modify the data (e.g., `add`, `delete`). The data is also saved upon using the `exit` command. There is no need to manually save changes.



---

### Editing the Data File

Inventory data is stored in a `.csv` file. Users can edit this file manually if necessary.