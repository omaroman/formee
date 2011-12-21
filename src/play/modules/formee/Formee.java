/**
 * Author: OMAROMAN
 * Date: 12/16/11
 * Time: 1:19 PM
 */

package play.modules.formee;

import play.data.validation.*;
import play.i18n.Messages;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Formee {

    public static String buildValidationDataString(Field field) throws Exception {
        StringBuilder result = new StringBuilder("{");
        List<String> rules = new ArrayList<String>();
        Map<String, String> messages = new HashMap<String, String>();
        Required required = field.getAnnotation(Required.class);
        if (required != null) {
            rules.add("required:true");
            if (required.message() != null) {
                messages.put("required", Messages.get(required.message()));
            }
        }
        Min min = field.getAnnotation(Min.class);
        if (min != null) {
            rules.add("min:" + Double.toString(min.value()));
            if (min.message() != null) {
                messages.put("min", Messages.get(min.message(), null, min.value()));
            }
        }
        Max max = field.getAnnotation(Max.class);
        if (max != null) {
            rules.add("max:" + Double.toString(max.value()));
            if (max.message() != null) {
                messages.put("max", Messages.get(max.message(), null, max.value()));
            }
        }
        Range range = field.getAnnotation(Range.class);
        if (range != null) {
            rules.add("range:[" + Double.toString(range.min()) + ", " + Double.toString(range.max()) + "]");
            if (range.message() != null) {
                messages.put("range", Messages.get(range.message(), null, range.min(), range.max()));
            }
        }
        MaxSize maxSize = field.getAnnotation(MaxSize.class);
        if (maxSize != null) {
            rules.add("maxlength:" + Integer.toString(maxSize.value()));
            if (maxSize.message() != null) {
                messages.put("maxlength", Messages.get(maxSize.message(), null, maxSize.value()));
            }
        }
        MinSize minSize = field.getAnnotation(MinSize.class);
        if (minSize != null) {
            rules.add("minlength:" + Integer.toString(minSize.value()));
            if (minSize.message() != null) {
                messages.put("minlength", Messages.get(minSize.message(), null, minSize.value()));
            }
        }
        URL url = field.getAnnotation(URL.class);
        if (url != null) {
            rules.add("url:true");
            if (url.message() != null) {
                messages.put("url", Messages.get(url.message()));
            }
        }
        Email email = field.getAnnotation(Email.class);
        if (email != null) {
            rules.add("email:true");
            if (email.message() != null) {
                messages.put("email", Messages.get(email.message()));
            }
        }
        if (rules.size() > 0) {
            boolean first = true;
            for (String rule : rules) {
                if (first) {
                    first = false;
                } else {
                    result.append(",");
                }
                result.append(rule);
            }
        }
        if (messages.size() > 0) {
            result.append(",messages:{");
            boolean first = true;
            for (String key : messages.keySet()) {
                if (first) {
                    first = false;
                } else {
                    result.append(",");
                }
                result.append("\"");
                result.append(key);
                result.append("\"");
                result.append(":");
                result.append("\"");
                result.append(messages.get(key));
                result.append("\"");
            }
            result.append("}");
        }
        result.append("}");
        return result.toString();
    }
}
