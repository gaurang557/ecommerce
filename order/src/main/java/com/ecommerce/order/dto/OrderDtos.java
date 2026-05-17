package com.ecommerce.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OrderDtos {

    public record AddItemRequest(
            @NotNull Integer productId,
            @NotNull @Min(1) Integer quantity) {
    }
}
