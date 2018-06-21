package pl.wojciechmazurek;

import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides implementation for odds crawler from http://www.hot-odds.com/Football.
 * It crawls every data that you can see on this webpage. This simple crawler doesn't crawl data from match pages, just
 * this main page for football.
 *
 * Main idea for crawling from HotOdds is:
 * - from whole page get set of tables. One table consist of: country and league name and then list of matches for a league
 * - other loop iteration is an iteration over one table which can have only one match or more.
 *
 * @author Wojciech Mazurek
 *
 */
public class HotOddsCrawler implements OddsCrawler {
    @Override
    public List<Match> crawlOdds() {

        List<Match> matchList = new ArrayList<>();
        try {
            Elements tables = getTablesWithGamesForADay();

            for (int loopIterationForTable = 0; loopIterationForTable < tables.size(); loopIterationForTable++) {

                Elements links = getLinksFromTable(tables, loopIterationForTable);
                String country = getCountryName(links);
                String league = getLeagueName(links);

                Elements games = getListOfGamesFromOneTable(tables, loopIterationForTable);

                /**
                 * Here is the second loop where we create match.
                 */
                for (int loopIterationForGamesList = 0; loopIterationForGamesList < games.size(); loopIterationForGamesList++) {
                    String[] teams = getTeamsForAGame(games, loopIterationForGamesList);

                    Match match = createMatch(country, league, games, loopIterationForGamesList, teams);

                    matchList.add(match);

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return matchList;
    }

    /**
     * Here is the most important method - creating new game from previously gained data.
     * Every game has "bookmaker" field. It's possible future feature, but now we crawl just basic odds from main page.
     * @param country
     * @param league
     * @param games
     * @param loopIterationForGamesList
     * @param teams
     * @return
     */
    private Match createMatch(String country, String league, Elements games, int loopIterationForGamesList, String[] teams) {
        Match match = new Match();
        match.setBookmaker("unknown");
        match.setCountry(country);
        match.setLeague(league);
        setMatchFixture(games, loopIterationForGamesList, match);
        match.setHomeTeam(teams[0]);
        match.setAwayTeam(teams[1]);

        Elements odds = games.get(loopIterationForGamesList).select("div.value");

        setOddsForAGame(match, odds);
        return match;
    }

    private void setOddsForAGame(Match match, Elements odds) {
        match.setHomeWin(odds.get(0).text().substring(0,4));
        match.setDraw(odds.get(1).text().substring(0,4));
        match.setAwayWin(odds.get(2).text().substring(0,4));
    }

    /**
     * Teams for a single match are enclosed in "td.cell-title". Currently format for two teams
     * is: Team1 vs Team2, so we have to split this string in order to have two team names.
     * @param games
     * @param loopIterationForGamesList
     * @return
     */
    private String[] getTeamsForAGame(Elements games, int loopIterationForGamesList) {
        return games.get(loopIterationForGamesList).select("td.cell-title").text().split(" vs ");
    }


    /**
     * As I said - one table can consist of one or more games. One row = one game - hour, teams, odds etc.
     * This rows are enclosed in two kind of elements - tr.row-odd and tr.row-even.
     * @param tables
     * @param loopIterationForTable
     * @return
     */
    private Elements getListOfGamesFromOneTable(Elements tables, int loopIterationForTable) {
        return tables.get(loopIterationForTable).select("tbody > tr.row-odd , tbody > tr.row-even");
    }

    /**
     * We have list of links (Elements). Third link has name of league. Then we cut first two chars
     * in order to have "clean" name. For ex. normally it looks like this "> Lithuania", we cut ">" and space and got
     * "Lithuania".
     * @param links
     * @return
     */
    private String getLeagueName(Elements links) {
        return links.get(2).ownText().substring(2);
    }

    /**
     * We have list of links (Elements). Second link has name of country. Then we cut first two chars
     * in order to have "clean" name. For ex. normally it looks like this "> Lithuania", we cut ">" and space and got
     * "Lithuania".
     * @param links
     * @return
     */
    private String getCountryName(Elements links) {
        return links.get(1).ownText().substring(2);
    }

    /**
     * The easiest way to extract country and league name from table is to firstly extract links from previously extracted tables.
     * @param tables
     * @param loopIterationForTable
     * @return
     */
    private Elements getLinksFromTable(Elements tables, int loopIterationForTable) {
        return tables.get(loopIterationForTable).select("a[href]");
    }


    /**
     * This method is used to get match date. Hours in HH:MM format are in "td.cell-date" element.
     * We split using ":" and got two integers - hours and minutes. In some cases there are some games
     * that are running, so they don't have fixture in cell-date element. Instead of it they got td.cell-score.
     * From that element I extract how many minutes has passed since start, and then I subtract them from DateTime.now()
     *
     * <it's something meme>
     * @param games
     * @param loopIterationForGamesList
     * @param match
     */
    private void setMatchFixture(Elements games, int loopIterationForGamesList, Match match) {
        String[] matchFixture = games.get(loopIterationForGamesList).select("td.cell-date").text().split(":");
        if (!matchFixture[0].equals("")) {
            setHourAndMinuteOfGame(match, matchFixture);
        } else {
            int howManyMinutesPassed = Integer.parseInt(games.get(loopIterationForGamesList).select("td.cell-score").text().substring(0, 2));
            match.setDate(DateTime.now().minusMinutes(howManyMinutesPassed).minuteOfDay().roundFloorCopy());
        }

    }

    /**
     * Setting date for a game. I use here Joda Time library. Maybe I should experiment with TimesZones.
     * Currently games has GMT 0.
     * @param match
     * @param matchFixture
     */
    private void setHourAndMinuteOfGame(Match match, String[] matchFixture) {
        int hour = Integer.parseInt(matchFixture[0]);
        int minute = Integer.parseInt(matchFixture[1]);
        DateTime matchDate = DateTime.now().withHourOfDay(hour).withMinuteOfHour(minute);
        match.setDate(matchDate);
    }


    /**
     * First step to crawl all the odds. This method crawl whole page to Document. Then from whole page we retrieve
     * all the tables (div#MatchesPane). Table is a box with games for a one league in one country - table#HighlitsTable.
     * Then all we have to do is iterate over tables and crawl from then all events. Table can consist of one or more events.
     * @return
     * @throws IOException
     */
    private Elements getTablesWithGamesForADay() throws IOException {
        Elements hotOddsWebPage;
        Document doc = Jsoup.connect("http://www.hot-odds.com/Football").get();

        hotOddsWebPage = doc.select("div#MatchesPane > table#HighlightsTable");
        return hotOddsWebPage;
    }
}
