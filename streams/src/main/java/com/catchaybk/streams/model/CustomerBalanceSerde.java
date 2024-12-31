package com.catchaybk.streams.model;

public class CustomerBalanceSerde extends JsonSerde<CustomerBalance> {
    private static final CustomerBalanceSerde INSTANCE = new CustomerBalanceSerde();

    public CustomerBalanceSerde() {
        super(CustomerBalance.class);
    }

    public static CustomerBalanceSerde instance() {
        return INSTANCE;
    }
}
