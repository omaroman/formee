package views.tags.formee;

import groovy.lang.Closure;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;


import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.validation.*;
import play.i18n.Messages;
import play.libs.I18N;
import play.mvc.Router.ActionDefinition;
import play.mvc.Scope.Flash;
import play.templates.FastTags;
import play.templates.JavaExtensions;
import play.templates.GroovyTemplate.ExecutableTemplate;

@FastTags.Namespace("formee")
public class FormeeTags extends FastTags {

    /**
     * Generates a html form element linked to a controller action
     * @param args tag attributes
     * @param body tag inner body
     * @param out the output writer
     * @param template enclosing template
     * @param fromLine template line number where the tag is defined
     */
    public static void _form(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        ActionDefinition actionDef = (ActionDefinition) args.get("arg");
        if (actionDef == null) {
            actionDef = (ActionDefinition) args.get("action");
        }
        String enctype = (String) args.get("enctype");
        if (enctype == null) {
            enctype = "application/x-www-form-urlencoded";
        }
        if (actionDef.star) {
            actionDef.method = "POST"; // prefer POST for form ....
        }
        if (args.containsKey("method")) {
            actionDef.method = args.get("method").toString();
        }
        String cssClass = "formee";
        if (args.containsKey("class")) {
            cssClass = String.format("%s %s", args.get("class"), cssClass);
        }
        if (!("GET".equals(actionDef.method) || "POST".equals(actionDef.method))) {
            String separator = actionDef.url.indexOf('?') != -1 ? "&" : "?";
            actionDef.url += separator + "x-http-method-override=" + actionDef.method.toUpperCase();
            actionDef.method = "POST";
        }
        String id = args.containsKey("id") ? (String) args.get("id") : "formee__" + UUID.randomUUID();
        out.println("<form class='" + cssClass + "' id='" + id + "' action='" + actionDef.url + "' method='" + actionDef.method.toUpperCase() + "' accept-charset='utf-8' enctype='" + enctype + "' " + serialize(args, "class", "action", "method", "accept-charset", "enctype") + ">");
        if (!("GET".equals(actionDef.method))) {
            _authenticityToken(args, body, out, template, fromLine);
        }
        out.println(JavaExtensions.toString(body));
        out.println("</form>");
    }

    private static String buildValidationDataString(Field f) throws Exception {
        StringBuilder result = new StringBuilder("{");
        List<String> rules = new ArrayList<String>();
        Map<String, String> messages = new HashMap<String, String>();
        Required required = f.getAnnotation(Required.class);
        if (required != null) {
            rules.add("required:true");
            if (required.message() != null) {
                messages.put("required", Messages.get(required.message()));
            }
        }
        Min min = f.getAnnotation(Min.class);
        if (min != null) {
            rules.add("min:" + Double.toString(min.value()));
            if (min.message() != null) {
                messages.put("min", Messages.get(min.message(), null, min.value()));
            }
        }
        Max max = f.getAnnotation(Max.class);
        if (max != null) {
            rules.add("max:" + Double.toString(max.value()));
            if (max.message() != null) {
                messages.put("max", Messages.get(max.message(), null, max.value()));
            }
        }
        Range range = f.getAnnotation(Range.class);
        if (range != null) {
            rules.add("range:[" + Double.toString(range.min()) + ", " + Double.toString(range.max()) + "]");
            if (range.message() != null) {
                messages.put("range", Messages.get(range.message(), null, range.min(), range.max()));
            }
        }
        MaxSize maxSize = f.getAnnotation(MaxSize.class);
        if (maxSize != null) {
            rules.add("maxlength:" + Integer.toString(maxSize.value()));
            if (maxSize.message() != null) {
                messages.put("maxlength", Messages.get(maxSize.message(), null, maxSize.value()));
            }
        }
        MinSize minSize = f.getAnnotation(MinSize.class);
        if (minSize != null) {
            rules.add("minlength:" + Integer.toString(minSize.value()));
            if (minSize.message() != null) {
                messages.put("minlength", Messages.get(minSize.message(), null, minSize.value()));
            }
        }
        URL url = f.getAnnotation(URL.class);
        if (url != null) {
            rules.add("url:true");
            if (url.message() != null) {
                messages.put("url", Messages.get(url.message()));
            }
        }
        Email email = f.getAnnotation(Email.class);
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

    /**
     * The field tag is a helper, based on the spirit of Don't Repeat Yourself.
     * @param args tag attributes
     * @param body tag inner body
     * @param out the output writer
     * @param template enclosing template
     * @param fromLine template line number where the tag is defined
     */
    public static void _field(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        Map<String,Object> field = new HashMap<String,Object>();
        String _arg = args.get("arg").toString();

        // Get a Field obj via reflection
        int offset = _arg.indexOf(':');
        if (offset != -1) {
            String clazz = _arg.substring(0, offset);    // Get the full qualified class name
            _arg = _arg.substring(offset+1);   // remove the full qualified class name
            try {
                Object o = Class.forName(clazz).newInstance();
                Field f = o.getClass().getField(_arg.split("\\.")[1]);
                field.put("validationData", buildValidationDataString(f));  // Fills data-validate
            } catch (Exception e) {
                // DO NOT try to get any field
            }
        }
        
        field.put("name", _arg);
        field.put("id", _arg.replace('.','_'));
//        field.put("idError", _arg.replace('.','_') + "_error");
        field.put("flash", Flash.current().get(_arg));
        field.put("flashArray", field.get("flash") != null && !StringUtils.isEmpty(field.get("flash").toString()) ? field.get("flash").toString().split(",") : new String[0]);
        field.put("error", Validation.error(_arg));
        field.put("errorClass", field.get("error") != null ? "hasError" : "");
        
        String[] pieces = _arg.split("\\.");
        Object obj = body.getProperty(pieces[0]);
        if (obj != null) {
            if(pieces.length > 1) {
                for(int i = 1; i < pieces.length; i+=2) {   // TODO: Submit enhancement
                    try{
                        Field f = obj.getClass().getField(pieces[i]);
                        if (i == (pieces.length-1)) {
                            try{
                                Method getter = obj.getClass().getMethod("get"+JavaExtensions.capFirst(f.getName()));
                                field.put("value", getter.invoke(obj, new Object[0]));
                            }catch(NoSuchMethodException e){
                                field.put("value",f.get(obj).toString());
                            }
                        } else {
                            obj = f.get(obj);
                        }
                    } catch(Exception e) {
                        // if there is a problem reading the field we dont set any value
                    }
                }
            } else {
                field.put("value", obj);
            }
        }
        body.setProperty("field", field);
        body.call();
    }

    /**
     * The field tag is a helper, based on the spirit of Don't Repeat Yourself.
     * @param args tag attributes
     * @param body tag inner body
     * @param out the output writer
     * @param template enclosing template
     * @param fromLine template line number where the tag is defined
     */
    public static void _error(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        String forAttr = (String) args.get("arg");
        
        String tag = "span";    // Error container element

        String cssClass = "error";
        if (args.containsKey("class")) {
            cssClass = String.format("%s %s", args.get("class"), cssClass);
        }
        
        out.println("<" + tag + " for='" + forAttr +"' class='" + cssClass + "' generated='true'" + serialize(args, "class") + ">");
        out.println(JavaExtensions.toString(body));
        out.println("</" + tag + ">");
    }
}
