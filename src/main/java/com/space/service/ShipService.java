package com.space.service;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.controller.ShipOrder;

import java.util.List;
import java.util.Date;

public interface ShipService {
    List<Ship> getShips(String name,
                        String planet,
                        ShipType shipType,
                        Long after,
                        Long before,
                        Boolean isUsed,
                        Double minSpeed,
                        Double maxSpeed,
                        Integer minCrewSize,
                        Integer maxCrewSize,
                        Double minRating,
                        Double maxRating);
    Ship saveShip(Ship ship);
    Ship getShip(Long id);
    Ship updateShip(Ship oldShip, Ship newShip) throws IllegalArgumentException;
    void deleteShip(Ship ship);
    List<Ship> sortShips(List<Ship> ships, ShipOrder shipOrder);
    boolean isShipValid(Ship ship);
    List<Ship> getShipsPage(List<Ship> ships, Integer pageNumber, Integer pageSize);
    double calculateRating(Double speed, boolean isUsed, Date prodDate);
}
