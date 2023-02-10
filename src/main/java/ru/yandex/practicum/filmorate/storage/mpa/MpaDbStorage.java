package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

@Component
public class MpaDbStorage implements MpaStorage {
    JdbcTemplate jdbcTemplate;
    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Mpa getMpa(int mpaId) {
        String sql = "SELECT MPA_NAME FROM RATES_MPA WHERE MPA_ID = ?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sql, mpaId);
        if (userRows.next()) {
            return new Mpa(mpaId,
                    userRows.getString("mpa_name"));
        }
        else return null;
    }
}
