/**
 * Author: omaroman
 * Date: 12/21/11
 * Time: 3:08 PM
 */

package play.modules.formee;

public enum InputType {

    // HTML4 input types:
    TEXT,
    TEXTAREA,
    PASSWORD,
    HIDDEN,
    CHECKBOX,
    RADIO,
    FILE,
    SELECT,
    OPTION,
    SUBMIT,
    RESET,

    // CUSTOM input types:
    CHECKBOOL,  // A checkbox representing a boolean
    CONCEAL,    // A hidden representing a boolean
    SELECT_LIST,// A list for option elements
    RADIO_LIST, // A list of radios
    CHECKBOX_LIST,  // A list of checkboxes
    TIMESTAMP,  // Date & Time

    // HTML5 input types:
    SEARCH,
    TEL,
    URL,
    EMAIL,
    DATETIME,
    DATE,
    MONTH,
    WEEK,
    TIME,
    DATETIME_LOCAL,
    NUMBER,
    RANGE,
    COLOR
}
