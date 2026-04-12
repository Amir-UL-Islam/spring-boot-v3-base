package com.problemfighter.java.oc.copier;

import com.problemfighter.java.oc.common.ObjectCopierException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class ObjectCopierNestedMappingTest {

    @Test
    void copy_should_map_child_collection_items_to_destination_element_type() throws ObjectCopierException {
        OrderDto source = new OrderDto();
        source.orderNo = "ORD-1";
        source.lineItems = new ArrayList<>();

        OrderLineItemDto item = new OrderLineItemDto();
        item.sku = "SKU-1";
        item.qty = 3;
        source.lineItems.add(item);

        ObjectCopier copier = new ObjectCopier();
        Order destination = copier.copy(source, Order.class);

        Assertions.assertNotNull(destination);
        Assertions.assertEquals("ORD-1", destination.orderNo);
        Assertions.assertNotNull(destination.lineItems);
        Assertions.assertEquals(1, destination.lineItems.size());
        Assertions.assertInstanceOf(OrderLineItem.class, destination.lineItems.get(0));
        Assertions.assertEquals("SKU-1", destination.lineItems.get(0).sku);
        Assertions.assertEquals(3, destination.lineItems.get(0).qty);
    }

    @Test
    void copy_should_map_nested_object_with_different_source_destination_types() throws ObjectCopierException {
        CustomerDto source = new CustomerDto();
        source.name = "Amir";
        source.address = new AddressDto();
        source.address.city = "Dhaka";

        ObjectCopier copier = new ObjectCopier();
        Customer destination = copier.copy(source, Customer.class);

        Assertions.assertNotNull(destination);
        Assertions.assertEquals("Amir", destination.name);
        Assertions.assertNotNull(destination.address);
        Assertions.assertInstanceOf(Address.class, destination.address);
        Assertions.assertEquals("Dhaka", destination.address.city);
    }

    static class OrderDto {
        String orderNo;
        List<OrderLineItemDto> lineItems;
    }

    static class Order {
        String orderNo;
        List<OrderLineItem> lineItems;
    }

    static class OrderLineItemDto {
        String sku;
        Integer qty;
    }

    static class OrderLineItem {
        String sku;
        Integer qty;
    }

    static class CustomerDto {
        String name;
        AddressDto address;
    }

    static class Customer {
        String name;
        Address address;
    }

    static class AddressDto {
        String city;
    }

    static class Address {
        String city;
    }
}

