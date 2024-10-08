package org.dci.bookhaven.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "shippings")
public class Shipping {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private long id;

    @ManyToOne
    private Address address;

    @Column(name = "shipping_method", nullable = false)
    private String shippingMethod;

    @Column(name = "cost", nullable = false)
    private BigDecimal cost;

    @Column(name = "tracking_number", nullable = true)
    private String trackingNumber;


}
