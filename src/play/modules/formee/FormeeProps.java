/**
 * Author: OMAROMAN
 * Date: 12/27/11
 * Time: 1:52 PM
 */

package play.modules.formee;

public class FormeeProps {

    final static String CONFIG_PREFIX = "formee.";

    public final static String CAMEL_CASE = "camelCase";
    public final static String UNDERSCORE_CASE = "underscore_case";
    public final static String PROPER_CASE = "ProperCase";

    final static String DEFAULT_NAMING_CASE = CAMEL_CASE;

    static String namingCase = null;

    public static String getNamingCase() {
        return namingCase;
    }
}
