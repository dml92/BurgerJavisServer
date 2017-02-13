/**
 * Diego Moreno Lennon
 * Burger Javi's Server
 */

package com.burgerjavis.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.burgerjavis.entities.Order;

public interface OrderRepository extends CrudRepository<Order, String> {
	
	public List<Order> findByNameIgnoreCase (String name);
	
}