package com.space.service;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.controller.ShipOrder;
import com.space.repository.ShipRepository;

import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ShipServiceImpl implements ShipService {
    private ShipRepository shipRepository;

    public ShipServiceImpl() {
    }

    @Autowired
    public ShipServiceImpl(ShipRepository shipRepository) {
        super();
        this.shipRepository = shipRepository;
    }

    @Override
    public List<Ship> getShips(String name, String planet, ShipType shipType, Long after, Long before, Boolean isUsed, Double minSpeed, Double maxSpeed, Integer minCrewSize, Integer maxCrewSize, Double minRating, Double maxRating) {
        final Date dateAfter = after == null ? null : new Date(after);
        final Date dateBefore = before == null ? null : new Date(before);
        List<Ship> ships = new ArrayList<>();
        shipRepository.findAll().forEach((ship) -> {
            if (name != null && !(ship.getName().contains(name))) {
                return;
            }
            if (planet != null && !(ship.getName().contains(planet))) {
                return;
            }
            if (shipType != null && ship.getShipType() != shipType) {
                return;
            }
            if (dateAfter != null && ship.getProdDate().before(dateAfter)) {
                return;
            }
            if (dateBefore != null && ship.getProdDate().after(dateBefore)) {
                return;
            }
            if (isUsed != null && ship.getUsed().booleanValue() != isUsed.booleanValue()) {
                return;
            }
            if (minSpeed != null && ship.getSpeed().compareTo(minSpeed) < 0) {
                return;
            }
            if (maxSpeed != null && ship.getSpeed().compareTo(maxSpeed) > 0) {
                return;
            }
            if (minCrewSize != null && ship.getCrewSize().compareTo(minCrewSize) < 0) {
                return;
            }
            if (maxCrewSize != null && ship.getCrewSize().compareTo(maxCrewSize) > 0) {
                return;
            }
            if (minRating != null && ship.getRating().compareTo(minRating) < 0) {
                return;
            }
            if (maxRating != null && ship.getRating().compareTo(maxRating) > 0) {
                return;
            }

            ships.add(ship);
        });
        return ships;
    }

    @Override
    public Ship saveShip(Ship ship) {
        return shipRepository.save(ship);
    }

    @Override
    public Ship getShip(Long id) {
        return shipRepository.findById(id).orElse(null);
    }

    @Override
    public Ship updateShip(Ship oldShip, Ship newShip) throws IllegalArgumentException {
        boolean isChangeRating = false;

        String name = newShip.getName();
        if (isValidString(name)) {
            oldShip.setName(name);
        } else {
            throw new IllegalArgumentException();
        }

        String planet = newShip.getPlanet();
        if (isValidString(planet)) {
            oldShip.setPlanet(planet);
        } else {
            throw new IllegalArgumentException();
        }

        ShipType shipType = newShip.getShipType();
        if (shipType != null) {
            oldShip.setShipType(shipType);
        } else {
            throw new IllegalArgumentException();
        }

        Date prodDate = newShip.getProdDate();
        if (isValidProdDate(prodDate)) {
            oldShip.setProdDate(prodDate);
            isChangeRating = true;
        } else {
            throw new IllegalArgumentException();
        }

        Boolean isUsed = newShip.getUsed();
        if (isUsed != null) {
            oldShip.setUsed(isUsed);
            isChangeRating = true;
        }

        Double speed = newShip.getSpeed();
        if (isValidSpeed(speed)) {
            oldShip.setSpeed(speed);
            isChangeRating = true;
        } else {
            throw new IllegalArgumentException();
        }

        Integer crewSize = newShip.getCrewSize();
        if (isValidCrewSize(crewSize)) {
            oldShip.setCrewSize(crewSize);
        } else {
            throw new IllegalArgumentException();
        }

        if (isChangeRating) {
            double rating = calculateRating(oldShip.getSpeed(), oldShip.getUsed(), oldShip.getProdDate());
            oldShip.setRating(rating);
        }

        shipRepository.save(oldShip);
        return oldShip;
    }

    @Override
    public void deleteShip(Ship ship) {
        shipRepository.delete(ship);
    }

    @Override
    public List<Ship> sortShips(List<Ship> ships, ShipOrder shipOrder) {
        if (shipOrder != null) {
            ships.sort((ship0, ship1) -> {
               switch (shipOrder) {
                   case ID:
                       return ship0.getId().compareTo(ship1.getId());
                   case SPEED:
                       return ship0.getSpeed().compareTo(ship1.getSpeed());
                   case DATE:
                       return ship0.getProdDate().compareTo(ship1.getProdDate());
                   case RATING:
                       return ship0.getRating().compareTo(ship1.getRating());
                   default:
                       return 0;
               }
            });
        }
        return ships;
    }

    @Override
    public boolean isShipValid(Ship ship) {
        return  ship != null &&
                isValidString(ship.getName()) &&
                isValidString((ship.getPlanet())) &&
                isValidProdDate(ship.getProdDate()) &&
                isValidSpeed(ship.getSpeed()) &&
                isValidCrewSize(ship.getCrewSize());
    }

    @Override
    public List<Ship> getShipsPage(List<Ship> ships, Integer pageNumber, Integer pageSize) {
        Integer page = pageNumber != null ? pageNumber : 0;
        Integer size = pageSize != null ? pageSize : 0;
        int start = page * size;
        int end = start + size;
        if (end > ships.size()) {
            end = ships.size();
        }

        return ships.subList(start, end);
    }

    @Override
    public double calculateRating(Double speed, boolean isUsed, Date prodDate) {
        int yearNow = 3019;
        int yearProd = getYearFromDate(prodDate);
        double k = isUsed ? 0.5 : 1;
        return getRoundedToSecondDigit(80 * speed * k / (yearNow - yearProd + 1));
    }

    private boolean isValidString(String value) {
        int maxStringLength = 50;
        return value != null && !value.isEmpty() && value.length() <= maxStringLength;
    }

    private boolean isValidSpeed(Double speed) {
        double minSpeed = 0.01;
        double maxSpeed = 0.99;
        return speed != null && speed.compareTo(minSpeed) >= 0 && speed.compareTo(maxSpeed) <= 0;
    }

    private boolean isValidCrewSize(Integer crewSize) {
        int minCrewSize = 1;
        int maxCrewSize = 9999;
        return crewSize != null && crewSize.compareTo(minCrewSize) >= 0 && crewSize.compareTo(maxCrewSize) <= 0;
    }

    private boolean isValidProdDate(Date prodDate) {
        Date startProdDate = getDateFromYear(2800);
        Date endProdDate = getDateFromYear(3100);
        return prodDate != null && prodDate.after(startProdDate) && prodDate.before(endProdDate);
    }

    private Date getDateFromYear(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }

    private int getYearFromDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    private double getRoundedToSecondDigit(double value) {
        return Math.round(value * 100) / 100D;
    }
}
