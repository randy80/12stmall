package stmall.domain;

import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.Data;
import stmall.DeliveryApplication;
import stmall.domain.DeliveryCancelled;
import stmall.domain.DeliveryStared;

@Entity
@Table(name = "Delivery_table")
@Data
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String userId;

    private Long orderId;

    private String productName;

    private Integer qty;

    private Long productId;

    private String status;

    private String courier;

    @PostPersist
    public void onPostPersist() {}

    @PostUpdate
    public void onPostUpdate() {
        // DeliveryStared deliveryStared = new DeliveryStared(this);
        // deliveryStared.publishAfterCommit();

        // DeliveryCancelled deliveryCancelled = new DeliveryCancelled(this);
        // deliveryCancelled.publishAfterCommit();
    }

    public static DeliveryRepository repository() {
        DeliveryRepository deliveryRepository = DeliveryApplication.applicationContext.getBean(
            DeliveryRepository.class
        );
        return deliveryRepository;
    }

    public void completeDelivery(
        CompleteDeliveryCommand completeDeliveryCommand
    ) {
        this.setCourier(completeDeliveryCommand.getCouirer());
        this.setStatus("DeliveryComplated");

        DeliveryCompleted deliveryCompleted = new DeliveryCompleted(this);
        deliveryCompleted.publishAfterCommit();
    }

    public void returnDelivery(ReturnDeliveryCommand returnDeliveryCommand) {
        this.setCourier(returnDeliveryCommand.getCourier());
        this.setStatus("DeliveryReturned");

        DeliveryReturned deliveryReturned = new DeliveryReturned(this);
        deliveryReturned.publishAfterCommit();
    }

    public static void prepareDelivery(OrderPlaced orderPlaced) {
        /** Example 1:  new item */
        Delivery delivery = new Delivery();
        delivery.setOrderId(orderPlaced.getId());
        delivery.setProductId(orderPlaced.getProductId());
        delivery.setProductName(orderPlaced.getProductName());
        delivery.setQty(orderPlaced.getQty());
        delivery.setStatus("DeliveryStarted");
        repository().save(delivery);

        DeliveryStared deliveryStared = new DeliveryStared(delivery);
        deliveryStared.publishAfterCommit();
    }

    public static void cancelDelivery(OrderCancelled orderCancelled) {
        repository().findByOrderId(orderCancelled.getId()).ifPresent(delivery->{
            delivery.setStatus("DeleveryCancelled");
            repository().save(delivery);

            DeliveryCancelled deliveryCancelled = new DeliveryCancelled(delivery);
            deliveryCancelled.publishAfterCommit();
         });
    }
}
