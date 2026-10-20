package com.eventra.service.impl;

import com.eventra.dao.impl.EventDAOImpl;
import com.eventra.dao.impl.InterestDAOImpl;
import com.eventra.dao.interfaces.IEventDAO;
import com.eventra.dao.interfaces.IInterestDAO;
import com.eventra.model.Event;
import com.eventra.model.EventStatus;
import com.eventra.model.Interest;
import com.eventra.service.interfaces.IRecommendationService;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Interest-based event recommendation engine.
 * Score = (number of user interests matching event interests / total user interests) * 100.
 * Events the user is already registered for are excluded.
 * Only UPCOMING and ALMOST_FULL events are considered.
 */
public class RecommendationServiceImpl implements IRecommendationService {

    private static final int MAX_RECOMMENDATIONS = 5;

    private final IInterestDAO interestDAO;
    private final IEventDAO eventDAO;

    public RecommendationServiceImpl() {
        this.interestDAO = new InterestDAOImpl();
        this.eventDAO = new EventDAOImpl();
    }

    /** Injection constructor for Mockito tests. */
    public RecommendationServiceImpl(IInterestDAO interestDAO, IEventDAO eventDAO) {
        this.interestDAO = interestDAO;
        this.eventDAO = eventDAO;
    }

    @Override
    public List<Event> getRecommendations(int userId) {
        Map<Integer, Integer> scoreMap = getScoreMap(userId);
        if (scoreMap.isEmpty()) return Collections.emptyList();

        try {
            List<Event> allEvents = eventDAO.findAll();
            return allEvents.stream()
                    .filter(e -> scoreMap.containsKey(e.getId()))
                    .filter(e -> e.getStatus() == EventStatus.UPCOMING
                              || e.getStatus() == EventStatus.ALMOST_FULL)
                    .sorted((a, b) -> scoreMap.getOrDefault(b.getId(), 0)
                                    - scoreMap.getOrDefault(a.getId(), 0))
                    .limit(MAX_RECOMMENDATIONS)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            System.err.println("[RecommendationService.getRecommendations] DB error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public Map<Integer, Integer> getScoreMap(int userId) {
        try {
            List<Interest> userInterests = interestDAO.findByUserId(userId);
            if (userInterests.isEmpty()) return Collections.emptyMap();

            Set<Integer> userInterestIds = userInterests.stream()
                    .map(Interest::getId)
                    .collect(Collectors.toSet());

            List<Event> allEvents = eventDAO.findAll();
            // Map: eventId → score
            Map<Integer, Integer> scoreMap = new HashMap<>();

            for (Event event : allEvents) {
                List<Interest> eventInterests = interestDAO.findByEventId(event.getId());
                if (eventInterests.isEmpty()) continue;

                long matchCount = eventInterests.stream()
                        .map(Interest::getId)
                        .filter(userInterestIds::contains)
                        .count();

                if (matchCount > 0) {
                    int score = (int) Math.round((double) matchCount / userInterestIds.size() * 100);
                    scoreMap.put(event.getId(), Math.min(score, 100));
                }
            }
            return scoreMap;
        } catch (SQLException e) {
            System.err.println("[RecommendationService.getScoreMap] DB error: " + e.getMessage());
            return Collections.emptyMap();
        }
    }
}
