package com.egomaa.order.orderservice.service;

import com.egomaa.order.orderservice.dto.InventoryResponse;
import com.egomaa.order.orderservice.dto.OrderLineItemsDto;
import com.egomaa.order.orderservice.dto.OrderRequest;
import com.egomaa.order.orderservice.model.Order;
import com.egomaa.order.orderservice.model.OrderLineItems;
import com.egomaa.order.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;
    private final LoadBalancerClient loadBalancerClient ;

    public void placeOrder(OrderRequest orderRequest) throws IllegalAccessException {

        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems =
                orderRequest.getOrderLineItemsDto()
                        .stream().map(this::mapToDto)
                        .collect(Collectors.toList());

        order.setOrderLineItems(orderLineItems);

        List<String> skuCodeList = order.getOrderLineItems()
                .stream()
                .map(item -> item.getSkuCode())
                .collect(Collectors.toList());

        ServiceInstance serviceInstance = loadBalancerClient.choose("inventory-service");
        String uri = serviceInstance.getUri().toString();

        System.out.println("the uri is: " + uri);

        // call inventory service , and place order if product is in stock
        InventoryResponse[] inventoryResponses = webClient.get()
                        .uri(uri + "/api/inventory",
                                uriBuilder -> uriBuilder.queryParam("skuCode", skuCodeList).build())
                        .retrieve()
                        .bodyToMono(InventoryResponse[].class)
                        .block();

        boolean allProductsInStock = Arrays.stream(inventoryResponses)
                                .allMatch(InventoryResponse::getIsInStock);

        if (allProductsInStock){
            orderRepository.save(order);
        }else {
            throw new IllegalAccessException("Product is not in stock , please try again later");
        }

    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto){
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }


}
