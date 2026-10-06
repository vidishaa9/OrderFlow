package com.vidisha.InventoryService.config;

import com.vidisha.InventoryService.entity.Inventory;
import com.vidisha.InventoryService.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeInventory(InventoryRepository inventoryRepository) {


        return args -> {

            if (inventoryRepository.findByProduct("Laptop").isEmpty()) {
                inventoryRepository.save(
                        new Inventory("Laptop", 5)
                );
            }

            if (inventoryRepository.findByProduct("Phone").isEmpty()) {
                inventoryRepository.save(
                        new Inventory("Phone", 10)
                );
            }
        };
    }
}