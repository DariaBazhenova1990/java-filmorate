package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.InternalServerException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

@Repository
public class ReviewRepository extends BaseRepository<Review> {
    // useful = (количество лайков) - (количество дизлайков)
    private static final String SELECT_WITH_USEFUL =
            "SELECT r.id, r.content, r.is_positive, r.user_id, r.film_id, " +
                    "COALESCE(SUM(CASE WHEN rl.is_like THEN 1 WHEN rl.is_like = FALSE THEN -1 ELSE 0 END), 0) AS useful " +
                    "FROM reviews r " +
                    "LEFT JOIN review_likes rl ON r.id = rl.review_id ";
    private static final String GROUP_ORDER_LIMIT =
            "GROUP BY r.id ORDER BY useful DESC, r.id ASC LIMIT ?";

    private static final String FIND_BY_ID_QUERY = SELECT_WITH_USEFUL + "WHERE r.id = ? GROUP BY r.id";
    private static final String FIND_ALL_QUERY = SELECT_WITH_USEFUL + GROUP_ORDER_LIMIT;
    private static final String FIND_BY_FILM_QUERY = SELECT_WITH_USEFUL + "WHERE r.film_id = ? " + GROUP_ORDER_LIMIT;

    private static final String INSERT_QUERY =
            "INSERT INTO reviews(content, is_positive, user_id, film_id) VALUES (?, ?, ?, ?);";
    private static final String UPDATE_QUERY =
            "UPDATE reviews SET content = ?, is_positive = ? WHERE id = ?;";
    private static final String DELETE_QUERY = "DELETE FROM reviews WHERE id = ?;";

    private static final String SET_REACTION_QUERY =
            "MERGE INTO review_likes (review_id, user_id, is_like) KEY (review_id, user_id) VALUES (?, ?, ?);";
    private static final String REMOVE_REACTION_QUERY =
            "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = ?;";

    public ReviewRepository(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        super(jdbc, mapper, Review.class);
    }

    public Review create(Review review) {
        long id = insert(
                INSERT_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId()
        );
        return findById(id).orElseThrow(() -> new InternalServerException("Ошибка при создании отзыва"));
    }

    public Review update(Review review) {
        update(UPDATE_QUERY, review.getContent(), review.getIsPositive(), review.getReviewId());
        return findById(review.getReviewId())
                .orElseThrow(() -> new NotFoundException("Отзыв с id " + review.getReviewId() + " не найден"));
    }

    public boolean deleteById(long id) {
        return delete(DELETE_QUERY, id);
    }

    public Optional<Review> findById(long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public List<Review> findAll(int count) {
        return findMany(FIND_ALL_QUERY, count);
    }

    public List<Review> findByFilmId(long filmId, int count) {
        return findMany(FIND_BY_FILM_QUERY, filmId, count);
    }

    public void setReaction(long reviewId, long userId, boolean isLike) {
        jdbc.update(SET_REACTION_QUERY, reviewId, userId, isLike);
    }

    public void removeReaction(long reviewId, long userId, boolean isLike) {
        jdbc.update(REMOVE_REACTION_QUERY, reviewId, userId, isLike);
    }
}
