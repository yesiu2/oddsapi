package pl.wojciechmazurek.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

import java.util.Date;


@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class Match {

    private String homeTeam;

    private String awayTeam;

    private String country;

    private String league;

    private String bookmaker;

    private DateTime date;

    private String homeWin;

    private String awayWin;

    private String draw;
}
