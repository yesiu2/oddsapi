package pl.wojciechmazurek;

import pl.wojciechmazurek.domain.Match;

import java.util.List;

public interface OddsCrawler {

    List<Match> crawlOdds();
}
