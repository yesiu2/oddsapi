package pl.wojciechmazurek

import spock.lang.Specification
import static org.assertj.core.api.Assertions.*
class HotOddsCrawlerSpec extends Specification {

    def shouldCrawlDataFromHotOddsWebPageAndCreateMatches() {

        given: 'hotOddsCrawler'
        HotOddsCrawler hotOddsCrawler = new HotOddsCrawler()

        when: 'we crawl odds and create matches'
        List<Match> matchList = hotOddsCrawler.crawlOdds()

        Match firstMatch = matchList.get(0)
        Match lastMatch = matchList.get(matchList.size() - 1)


        then: 'assertions'

        assertThat(matchList).isNotNull()

        assertThat(firstMatch.country).isNotEmpty()
        assertThat(firstMatch.awayTeam).isNotEmpty()
        assertThat(firstMatch.homeTeam).isNotEmpty()
        assertThat(firstMatch.league).isNotEmpty()
        assertThat(firstMatch.bookmaker).isNotEmpty()
        assertThat(firstMatch.date).isNotNull()
        assertThat(firstMatch.awayWin).isNotEmpty()
        assertThat(firstMatch.homeWin).isNotEmpty()
        assertThat(firstMatch.draw).isNotEmpty()

        assertThat(lastMatch.country).isNotEmpty()
        assertThat(lastMatch.awayTeam).isNotEmpty()
        assertThat(lastMatch.homeTeam).isNotEmpty()
        assertThat(lastMatch.league).isNotEmpty()
        assertThat(lastMatch.bookmaker).isNotEmpty()
        assertThat(lastMatch.date).isNotNull()
        assertThat(lastMatch.awayWin).isNotEmpty()
        assertThat(lastMatch.homeWin).isNotEmpty()
        assertThat(lastMatch.draw).isNotEmpty()



    }
}
