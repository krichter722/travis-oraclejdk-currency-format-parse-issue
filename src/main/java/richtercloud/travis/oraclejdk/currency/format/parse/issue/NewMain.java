/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package richtercloud.travis.oraclejdk.currency.format.parse.issue;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author richter
 */
public class NewMain {
    private final static Logger LOGGER = LoggerFactory.getLogger(NewMain.class);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String input = "jfklds f fkd 5 € jkfdls  f ";
        int inputResult = test(input);
        Assert.assertEquals(15,
                inputResult);
        input = "jfklds f fkd 5€ jkfdls  f ";
        inputResult = test(input);
        Assert.assertEquals(1,
                inputResult);
    }

    private static int test(String input) {
        Set<String> inputSplits = new LinkedHashSet<>(Arrays.asList(input.split("[\\s]+")));
        LOGGER.debug(String.format("input split is '%s'", inputSplits));
        Set<Set<String>> inputSplitsPowerSet = Sets.powerSet(inputSplits);
        Set<Entry<Locale, Currency>> results = new HashSet<>();
        int totalCount = inputSplitsPowerSet.size()*Locale.getAvailableLocales().length*Currency.getAvailableCurrencies().size()*2;
        int count = 0;
        Set<String> checked = new HashSet<>();
        for(Set<String> inputSplit : inputSplitsPowerSet) {
            LOGGER.debug(String.format("input split powerset item is '%s'", inputSplit));
            String inputSplitJoin0 = Joiner.on(" ").join(inputSplit);
                //String.join not available in Java < 8
            String inputSplitJoin1 = Joiner.on("").join("", inputSplit);
            LOGGER.trace(String.format("checking input '%s'", inputSplitJoin0));
            LOGGER.trace(String.format("checking input '%s'", inputSplitJoin1));
            for(Locale locale : Locale.getAvailableLocales()) {
                for(Currency currency : Currency.getAvailableCurrencies()) {
                    String currencySymbol = currency.getSymbol(locale);
                    LOGGER.trace(String.format("currency symbol: %s", currencySymbol));
                    if(inputSplitJoin0.contains(currencySymbol)) {
                        inputSplitJoin0 = inputSplitJoin0.replace(currencySymbol, " "+currencySymbol+" ");
                        LOGGER.debug(String.format("replacement result: %s", inputSplitJoin0));
                    }
                    if(inputSplitJoin1.contains(currencySymbol)) {
                        inputSplitJoin1 = inputSplitJoin1.replace(currencySymbol, " "+currencySymbol+" ");
                        LOGGER.debug(String.format("replacement result: %s", inputSplitJoin1));
                    }
                    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);
                    currencyFormat.setCurrency(currency);
                    try {
                        currencyFormat.parse(inputSplitJoin0);
                        results.add(new AbstractMap.SimpleImmutableEntry<>(locale,
                                currency));
                    } catch (ParseException ex) {
                        //skip
                    }
                    try {
                        currencyFormat.parse(inputSplitJoin1);
                        results.add(new AbstractMap.SimpleImmutableEntry<>(locale,
                                currency));
                    } catch (ParseException ex) {
                        //skip
                    }
                    count += 2;
                    if(count % 100 == 0) {
                        LOGGER.info(String.format("progress: %d/%d",
                                count,
                                totalCount));
                    }
                }
            }
        }
        return results.size();
    }
}
