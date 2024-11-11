package seedu.pill.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import seedu.pill.exceptions.PillException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TreeSet;

//@@author philip1304

class TransactionManagerTest {
    private TransactionManager transactionManager;
    private ItemMap itemMap;

    @BeforeEach
    void setUp() {
        itemMap = new ItemMap();
        transactionManager = new TransactionManager(itemMap);
    }

    @Test
    void createTransaction_incomingTransaction_addsToInventory() throws PillException {
        // Arrange
        String itemName = "Aspirin";
        int quantity = 100;
        LocalDate expiryDate = null;

        // Act
        Transaction transaction = transactionManager.createTransaction(
                itemName,
                quantity,
                expiryDate,
                Transaction.TransactionType.INCOMING,
                "Test incoming",
                null
        );

        // Assert
        assertNotNull(transaction);
        assertEquals(itemName, transaction.getItemName());
        assertEquals(quantity, transaction.getQuantity());
        assertEquals(Transaction.TransactionType.INCOMING, transaction.getType());

        // Verify inventory was updated
        TreeSet<Item> items = itemMap.get(itemName);
        assertEquals(1, items.size());
        assertEquals(quantity, items.first().getQuantity());
    }

    @Test
    void createTransaction_outgoingTransaction_reducesInventory() throws PillException {
        // Arrange - First add inventory
        String itemName = "Aspirin";
        int initialQuantity = 100;
        int decreaseQuantity = 30;
        LocalDate expiryDate = null;

        transactionManager.createTransaction(
                itemName,
                initialQuantity,
                expiryDate,
                Transaction.TransactionType.INCOMING,
                "Initial stock",
                null
        );

        // Act
        Transaction transaction = transactionManager.createTransaction(
                itemName,
                decreaseQuantity,
                expiryDate,
                Transaction.TransactionType.OUTGOING,
                "Test outgoing",
                null
        );

        // Assert
        assertNotNull(transaction);
        assertEquals(itemName, transaction.getItemName());
        assertEquals(decreaseQuantity, transaction.getQuantity());
        assertEquals(Transaction.TransactionType.OUTGOING, transaction.getType());

        // Verify inventory was updated
        TreeSet<Item> items = itemMap.get(itemName);
        assertEquals(1, items.size());
        assertEquals(initialQuantity - decreaseQuantity, items.first().getQuantity());
    }

    @Test
    void createTransaction_insufficientStock_throwsException() {
        // Arrange
        String itemName = "Aspirin";
        int initialQuantity = 50;
        int decreaseQuantity = 100;
        LocalDate expiryDate = null;

        assertThrows(PillException.class, () -> {
            transactionManager.createTransaction(
                    itemName,
                    decreaseQuantity,
                    expiryDate,
                    Transaction.TransactionType.OUTGOING,
                    "Test insufficient",
                    null
            );
        });
    }

    @Test
    void createTransaction_withOrder_associatesCorrectly() throws PillException {
        // Arrange
        Order order = transactionManager.createOrder(Order.OrderType.PURCHASE, new ItemMap(), "Test order");
        String itemName = "Aspirin";
        int quantity = 100;
        LocalDate expiryDate = null;

        // Act
        Transaction transaction = transactionManager.createTransaction(
                itemName,
                quantity,
                expiryDate,
                Transaction.TransactionType.INCOMING,
                "Test with order",
                order
        );

        // Assert
        assertNotNull(transaction);
        assertEquals(order, transaction.getAssociatedOrder());
    }

    @Test
    void createOrder_purchaseOrder_createsSuccessfully() {
        // Arrange
        String notes = "Test purchase order";

        // Act
        Order order = transactionManager.createOrder(Order.OrderType.PURCHASE, new ItemMap(), notes);

        // Assert
        assertNotNull(order);
        assertEquals(Order.OrderType.PURCHASE, order.getType());
        assertEquals(notes, order.getNotes());
        assertEquals(Order.OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void createOrder_dispenseOrder_createsSuccessfully() {
        // Arrange
        String notes = "Test dispense order";

        // Act
        Order order = transactionManager.createOrder(Order.OrderType.DISPENSE, new ItemMap(), notes);

        // Assert
        assertNotNull(order);
        assertEquals(Order.OrderType.DISPENSE, order.getType());
        assertEquals(notes, order.getNotes());
        assertEquals(Order.OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void fulfillOrder_purchaseOrder_updatesInventoryAndStatus() throws PillException {
        // Arrange
        Order order = transactionManager.createOrder(Order.OrderType.PURCHASE, new ItemMap(),"Test purchase");
        String itemName = "Aspirin";
        int quantity = 100;
        order.addItem(new Item(itemName, quantity));

        // Act
        transactionManager.fulfillOrder(order);

        // Assert
        assertEquals(Order.OrderStatus.FULFILLED, order.getStatus());
        assertNotNull(order.getFulfillmentTime());

        // Verify inventory was updated
        TreeSet<Item> items = itemMap.get(itemName);
        assertEquals(1, items.size());
        assertEquals(quantity, items.first().getQuantity());
    }

    @Test
    void fulfillOrder_dispenseOrder_updatesInventoryAndStatus() throws PillException {
        // Arrange
        String itemName = "Aspirin";
        int initialQuantity = 100;
        int dispenseQuantity = 30;
        LocalDate expiryDate = null;

        // Add initial stock
        transactionManager.createTransaction(
                itemName,
                initialQuantity,
                expiryDate,
                Transaction.TransactionType.INCOMING,
                "Initial stock",
                null
        );

        // Create and fulfill dispense order
        Order order = transactionManager.createOrder(Order.OrderType.DISPENSE, new ItemMap(),"Test dispense");
        order.addItem(new Item(itemName, dispenseQuantity));

        // Act
        transactionManager.fulfillOrder(order);

        // Assert
        assertEquals(Order.OrderStatus.FULFILLED, order.getStatus());
        assertNotNull(order.getFulfillmentTime());

        // Verify inventory was updated
        TreeSet<Item> items = itemMap.get(itemName);
        assertEquals(1, items.size());
        assertEquals(initialQuantity - dispenseQuantity, items.first().getQuantity());
    }

    @Test
    void fulfillOrder_nonPendingOrder_throwsException() throws PillException {
        // Arrange
        Order order = transactionManager.createOrder(Order.OrderType.PURCHASE, new ItemMap(),"Test order");
        order.fulfill(); // Change status to FULFILLED

        // Act & Assert
        assertThrows(PillException.class, () -> transactionManager.fulfillOrder(order));
    }

    @Test
    void getTransactions_returnsCorrectTransactions() throws PillException {
        // Arrange
        transactionManager.createTransaction("Aspirin", 100, null,
                Transaction.TransactionType.INCOMING, "First", null);
        transactionManager.createTransaction("Bandage", 50, null,
                Transaction.TransactionType.INCOMING, "Second", null);

        // Act
        List<Transaction> transactions = transactionManager.getTransactions();

        // Assert
        assertEquals(2, transactions.size());
        assertEquals("Aspirin", transactions.get(0).getItemName());
        assertEquals("Bandage", transactions.get(1).getItemName());
    }

    @Test
    void getOrders_returnsCorrectOrders() {
        // Arrange
        transactionManager.createOrder(Order.OrderType.PURCHASE, new ItemMap(), "First order");
        transactionManager.createOrder(Order.OrderType.DISPENSE, new ItemMap(), "Second order");

        // Act
        List<Order> orders = transactionManager.getOrders();

        // Assert
        assertEquals(2, orders.size());
        assertEquals(Order.OrderType.PURCHASE, orders.get(0).getType());
        assertEquals(Order.OrderType.DISPENSE, orders.get(1).getType());
    }

    @Test
    void getItemTransactions_returnsCorrectTransactions() throws PillException {
        // Arrange
        String itemName = "Aspirin";
        transactionManager.createTransaction(itemName, 100, null,
                Transaction.TransactionType.INCOMING, "First", null);
        transactionManager.createTransaction("Bandage", 50, null,
                Transaction.TransactionType.INCOMING, "Second", null);
        transactionManager.createTransaction(itemName, 30, null,
                Transaction.TransactionType.OUTGOING, "Third", null);

        // Act
        List<Transaction> transactions = transactionManager.getItemTransactions(itemName);

        // Assert
        assertEquals(2, transactions.size());
        assertTrue(transactions.stream().allMatch(t -> t.getItemName().equals(itemName)));
    }

    @Test
    void getTransactionHistory_returnsTransactionsInTimeRange() throws PillException {
        // Arrange
        LocalDateTime startTime = LocalDateTime.now();

        transactionManager.createTransaction("Aspirin", 100, null,
                Transaction.TransactionType.INCOMING, "First", null);
        transactionManager.createTransaction("Bandage", 50, null,
                Transaction.TransactionType.INCOMING, "Second", null);

        LocalDateTime endTime = LocalDateTime.now();

        // Act
        List<Transaction> transactions = transactionManager.getTransactionHistory(startTime, endTime);

        // Assert
        assertEquals(2, transactions.size());
        assertTrue(transactions.stream()
                .allMatch(t -> !t.getTimestamp().isBefore(startTime) && !t.getTimestamp().isAfter(endTime)));
    }

    @Test
    void fulfillOrder_multipleItems_updatesInventoryCorrectly() throws PillException {
        // Arrange
        Order order = transactionManager.createOrder(Order.OrderType.PURCHASE, new ItemMap(), "Multi-item order");
        order.addItem(new Item("Aspirin", 100));
        order.addItem(new Item("Bandage", 50));
        order.addItem(new Item("Syringe", 25));

        // Act
        transactionManager.fulfillOrder(order);

        // Assert
        assertEquals(Order.OrderStatus.FULFILLED, order.getStatus());

        // Verify inventory for all items
        assertEquals(100, itemMap.get("Aspirin").first().getQuantity());
        assertEquals(50, itemMap.get("Bandage").first().getQuantity());
        assertEquals(25, itemMap.get("Syringe").first().getQuantity());
    }

    @Test
    void createTransaction_withExpiryDate_handlesCorrectly() throws PillException {
        // Arrange
        String itemName = "Aspirin";
        int quantity = 100;
        LocalDate expiryDate = LocalDate.now().plusYears(1);

        // Create item with expiry date
        Item item = new Item(itemName, quantity, expiryDate);
        itemMap.addItem(item);

        // Act
        Transaction transaction = transactionManager.createTransaction(
                itemName,
                50,
                null,
                Transaction.TransactionType.OUTGOING,
                "Test with expiry",
                null
        );

        // Assert
        assertNotNull(transaction);
        assertEquals(50, itemMap.get(itemName).first().getQuantity());
    }

    @Test
    void createTransaction_outgoingPartialQuantity_updatesInventoryCorrectly() throws PillException {
        // Arrange
        String itemName = "Aspirin";
        int initialQuantity = 100;
        int decreaseQuantity = 30;  // Less than initial quantity
        LocalDate expiryDate = null;

        // Add initial stock
        transactionManager.createTransaction(
                itemName,
                initialQuantity,
                expiryDate,
                Transaction.TransactionType.INCOMING,
                "Initial stock",
                null
        );

        // Act
        Transaction transaction = transactionManager.createTransaction(
                itemName,
                decreaseQuantity,
                expiryDate,
                Transaction.TransactionType.OUTGOING,
                "Partial withdrawal",
                null
        );

        // Assert
        assertNotNull(transaction);
        assertEquals(decreaseQuantity, transaction.getQuantity());

        // Verify inventory was partially reduced
        TreeSet<Item> items = itemMap.get(itemName);
        assertEquals(1, items.size());
        assertEquals(initialQuantity - decreaseQuantity, items.first().getQuantity());
    }

    @Test
    void createTransaction_outgoingExactQuantity_removesItem() throws PillException {
        // Arrange
        String itemName = "Aspirin";
        int initialQuantity = 100;
        LocalDate expiryDate = null;

        // Add initial stock
        transactionManager.createTransaction(
                itemName,
                initialQuantity,
                expiryDate,
                Transaction.TransactionType.INCOMING,
                "Initial stock",
                null
        );

        // Act
        Transaction transaction = transactionManager.createTransaction(
                itemName,
                initialQuantity,  // Withdraw exact quantity
                expiryDate,
                Transaction.TransactionType.OUTGOING,
                "Complete withdrawal",
                null
        );

        // Assert
        assertNotNull(transaction);
        assertEquals(initialQuantity, transaction.getQuantity());

        // Verify item was completely removed
        TreeSet<Item> items = itemMap.get(itemName);
        assertTrue(items.isEmpty());
    }

    @Test
    void createTransaction_outgoingMultipleItems_handlesCorrectly() throws PillException {
        // Arrange
        String itemName = "Aspirin";
        LocalDate expiry1 = LocalDate.now().plusMonths(1);
        LocalDate expiry2 = LocalDate.now().plusMonths(2);

        // Add two batches with different expiry dates
        itemMap.addItem(new Item(itemName, 50, expiry1));
        itemMap.addItem(new Item(itemName, 100, expiry2));

        // Act - withdraw amount that spans both items
        Transaction transaction = transactionManager.createTransaction(
                itemName,
                70,  // This will use up the first batch (50) and part of the second (20)
                null,
                Transaction.TransactionType.OUTGOING,
                "Multi-batch withdrawal",
                null
        );

        // Assert
        assertNotNull(transaction);
        assertEquals(70, transaction.getQuantity());

        // Verify inventory state
        TreeSet<Item> items = itemMap.get(itemName);
        assertEquals(1, items.size());  // Only second batch should remain
        Item remainingItem = items.first();
        assertEquals(expiry2, remainingItem.getExpiryDate().get());
        assertEquals(80, remainingItem.getQuantity());  // 100 - 20 = 80
    }

    @Test
    void getTransactionHistory_exactTimeRange_returnsMatchingTransactions() throws PillException {
        // Arrange
        LocalDateTime start = LocalDateTime.now();
        Transaction first = transactionManager.createTransaction(
                "Aspirin", 100, null,
                Transaction.TransactionType.INCOMING, "First", null);
        Transaction second = transactionManager.createTransaction(
                "Bandage", 50, null,
                Transaction.TransactionType.INCOMING, "Second", null);
        LocalDateTime end = LocalDateTime.now();

        // Act
        List<Transaction> transactions = transactionManager.getTransactionHistory(
                first.getTimestamp(), second.getTimestamp());

        // Assert
        assertEquals(2, transactions.size());
        assertTrue(transactions.contains(first));
        assertTrue(transactions.contains(second));
    }

    @Test
    void getTransactionHistory_beforeStartTime_excludesTransactions() throws PillException, InterruptedException {
        // Arrange
        Transaction before = transactionManager.createTransaction(
                "Before", 100, null,
                Transaction.TransactionType.INCOMING, "Before start", null);
        Thread.sleep(100); // Add delay to ensure clear time separation
        LocalDateTime start = LocalDateTime.now();
        Thread.sleep(100); // Add delay to ensure clear time separation
        Transaction during = transactionManager.createTransaction(
                "During", 50, null,
                Transaction.TransactionType.INCOMING, "During range", null);
        LocalDateTime end = LocalDateTime.now();

        // Debug prints
        System.out.println("Before transaction time: " + before.getTimestamp());
        System.out.println("During transaction time: " + during.getTimestamp());
        System.out.println("Start time: " + start);
        System.out.println("End time: " + end);

        // Act
        List<Transaction> transactions = transactionManager.getTransactionHistory(start, end);

        // Debug prints
        System.out.println("Found transactions: " + transactions.size());
        transactions.forEach(t -> System.out.println("Transaction: " + t.getTimestamp()));

        // Assert
        assertEquals(1, transactions.size());
        assertFalse(transactions.contains(before));
        assertTrue(transactions.contains(during));
    }

    @Test
    void getTransactionHistory_exactBoundaryTimes_includesTransactions() throws PillException {
        // Arrange
        Transaction first = transactionManager.createTransaction(
                "First", 100, null,
                Transaction.TransactionType.INCOMING, "Boundary start", null);
        LocalDateTime startAndEnd = first.getTimestamp(); // Use exact timestamp

        // Act
        List<Transaction> transactions = transactionManager.getTransactionHistory(startAndEnd, startAndEnd);

        // Assert
        assertEquals(1, transactions.size());
        assertTrue(transactions.contains(first));
    }

    @Test
    void getTransactionHistory_endBeforeStart_returnsEmptyList() throws PillException {
        // Arrange
        Transaction transaction = transactionManager.createTransaction(
                "Test", 100, null,
                Transaction.TransactionType.INCOMING, "Test", null);
        LocalDateTime later = LocalDateTime.now();
        LocalDateTime earlier = later.minusMinutes(1);

        // Act
        List<Transaction> transactions = transactionManager.getTransactionHistory(later, earlier);

        // Assert
        assertTrue(transactions.isEmpty());
    }
}

//@@author
