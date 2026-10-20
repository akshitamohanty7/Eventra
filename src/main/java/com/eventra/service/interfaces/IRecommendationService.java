package com.eventra.service.interfaces;

import com.eventra.model.Event;

import java.util.List;
import java.util.Map;

/**
 * Service contract for interest-based event recommendation.
 */
public interface IRecommendationService {

    /**
     * Returns a list of up to 5 recommended events for the user, sorted by match score descending.
     * Score = (matching interests / user's total interests) * 100.
     *
     * @param userId Attendee user ID
     * @return List of recommended Event objects (max 5)
     */
    List<Event> getRecommendations(int userId);

    /**
     * Returns a Map of eventId → score for all scoreable events.
     * Used by JSP layer to render the % match badge on each card.
     *
     * @param userId Attendee user ID
     * @return Map of eventId to score (0–100)
     */
    Map<Integer, Integer> getScoreMap(int userId);
}
