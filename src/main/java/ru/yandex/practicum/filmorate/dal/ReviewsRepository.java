package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ReviewsRepository extends BaseRepository<Review> {
    private static final String INSERT_QUERY =
            "INSERT INTO reviews(content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?);";
    private static final String UPDATE_QUERY =
            "UPDATE reviews SET content = ?, is_positive = ?, user_id = ?, film_id = ? WHERE id = ?;";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM reviews WHERE id = ?;";
    private static final String DELETE_QUERY = "DELETE FROM reviews WHERE id = ?;";
    private static final String ADD_LIKE_DISLIKE_QUERY =
            "MERGE INTO review_likes (review_id, user_id, is_like) KEY (review_id, user_id) VALUES (?, ?, ?);";
    private static final String REMOVE_LIKE_DISLIKE_QUERY =
            "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?;";
    private static final String UPDATE_USEFUL =
            "UPDATE reviews SET useful = ? WHERE id = ?;";

    public ReviewsRepository(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        super(jdbc, mapper, Review.class);
    }


    public Review create(Review review) {
        long id = insert(
                INSERT_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                review.getUseful()
        );
        review.setReviewId(id);

        return findById(review.getReviewId())
                .orElseThrow(() -> new NotFoundException("Отзыв с id " + review.getReviewId() + " не найден"));
    }

    public Optional<Review> findById(long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public Review update(Review review) {
        update(
                UPDATE_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                review.getReviewId()
        );

        return findById(review.getReviewId())
                .orElseThrow(() -> new NotFoundException("Отзыв с id " + review.getReviewId() + " не найден"));
    }

    public boolean delete(long id) {
        int rowsAffected = jdbc.update(DELETE_QUERY, id);
        return rowsAffected > 0;
    }

    public List<Review> getReviews(Long filmId, int count) {
        StringBuilder sql = new StringBuilder("SELECT * FROM reviews ");
        List<Object> params = new ArrayList<>();

        if (filmId != null) {
            sql.append("WHERE film_id = ? ");
            params.add(filmId);
        }

        sql.append("LIMIT ?");
        params.add(count);

        return findMany(sql.toString(), params.toArray());

    }

    public Review addLike(long id, long userId) {
        update(ADD_LIKE_DISLIKE_QUERY, id, userId, true);
        updateUseful(id);
        return findById(id).orElseThrow(() -> new NotFoundException("Отзыв с id " + id + " не найден"));
    }

    public Review addDislike(long id, long userId) {
        update(ADD_LIKE_DISLIKE_QUERY, id, userId, false);
        updateUseful(id);
        return findById(id).orElseThrow(() -> new NotFoundException("Отзыв с id " + id + " не найден"));
    }

    public Review removeLike(long id, long userId) {
        update(REMOVE_LIKE_DISLIKE_QUERY, id, userId);
        updateUseful(id);
        return findById(id).orElseThrow(() -> new NotFoundException("Отзыв с id " + id + " не найден"));
    }

    public Review removeDisLike(long id, long userId) {
        update(REMOVE_LIKE_DISLIKE_QUERY, id, userId);
        updateUseful(id);
        return findById(id).orElseThrow(() -> new NotFoundException("Отзыв с id " + id + " не найден"));
    }

    private void updateUseful(long reviewId) {
        String sql = """
                UPDATE reviews
                SET useful = COALESCE((
                    SELECT SUM(CASE WHEN is_like THEN 1 ELSE -1 END)
                    FROM review_likes
                    WHERE review_id = ?
                ), 0)
                WHERE id = ?
                """;
        jdbc.update(sql, reviewId, reviewId);
    }

}
