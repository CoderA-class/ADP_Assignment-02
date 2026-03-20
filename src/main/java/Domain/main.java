package Domain;


import Repositorie.OrderItemRepositoryImpl;
import factory.OrderItemFactory;

public class main {
    public static void main(String[] args) {



        OrderItem orderItem = OrderItemFactory.createOrderItem(
                "OI5000",
                "ORD777",
                "PROD100",
                20,
                4500.00
        );

        System.out.println("\nCreated OrderItem:");
        System.out.println(orderItem);


        OrderItemRepositoryImpl repository = OrderItemRepositoryImpl.getRepository();


        repository.create(orderItem);
        System.out.println("\nOrderItem saved to repository.");


        OrderItem readItem = repository.read("OI5000");
        System.out.println("\nRead from repository:");
        System.out.println(readItem);


        OrderItem updatedOrderItem = new OrderItem.OrderItemBuilder()
                .setOrderItemId("OI5000")
                .setOrderId("ORD777")
                .setProductId("PROD100")
                .setQuantity(30)
                .setLineTotal(6000.00)
                .build();

        repository.update(updatedOrderItem);

        System.out.println("\nUpdated OrderItem:");
        System.out.println(repository.read("OI5000"));


        boolean deleted = repository.delete("OI5000");
        System.out.println("\nDeleted OrderItem: " + deleted);


        System.out.println("\nTrying to read deleted item:");
        System.out.println(repository.read("OI5000"));
    }
}