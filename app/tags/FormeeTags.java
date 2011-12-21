package tags;

import groovy.lang.Closure;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.*;


import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.validation.*;
import play.i18n.Messages;
import play.modules.formee.FormeeValidation;
import play.modules.formee.InputType;
import play.mvc.Scope.Flash;
import play.templates.BaseTemplate;
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
    @SuppressWarnings("unchecked")
    public static void _form(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        if (args.containsKey("class")) {
            ((Map<Object, Object>) args).put("class", args.remove("class") + " formee");
        } else {
            ((Map<Object, Object>) args).put("class", "formee");
        }
        if (args.get("for") != null) {
            BaseTemplate.layoutData.get().put("_editObject_", args.remove("for"));
        }

        FastTags._form(args, body, out, template, fromLine);
    }

    @SuppressWarnings("unchecked")
    public static void _input(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
        Map.Entry<String, String> modelField = getModelField(args);
        String dataValidation = getDataValidation(modelField);

        String type = (String) args.remove("type");
        if (type == null) {
            throw new IllegalArgumentException("There's no 'type' argument");
        }

        if (type.equals("textarea")) {
            _textarea(args, body, out, template, fromLine);
        } else {
            if (type.equals("text")) {
                _text(args, body, out, template, fromLine);
            } else if (type.equals("password")) {
                _password(args, body, out, template, fromLine);
            } else if (type.equals("hidden")) {
                _hidden(args, body, out, template, fromLine);
            } else if (type.equals("checkbox")) {
                _checkbox(args, body, out, template, fromLine);
            } else if (type.equals("checkbool")) {
                _checkbool(args, body, out, template, fromLine);
            } else if (type.equals("radio")) {
                // TODO: Implement
            } else { // default
                _text(args, body, out, template, fromLine);
            }
        }

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
        Map<String,Object> fieldMap = new HashMap<String,Object>();
        String _arg = args.get("arg").toString();

        Map.Entry<String, String> modelField = getModelField(args);

        if (args.get("model") != null) {
            String field = StringUtils.split(_arg, '.')[1];  // TODO: Enhance this in case nested models
            String model = args.get("model").toString();
            fieldMap.put("fqn", String.format("%s.%s", model, field));
        }
        
        fieldMap.put("name", _arg);
        fieldMap.put("id", _arg.replace('.','_'));
        fieldMap.put("flash", Flash.current().get(_arg));
        fieldMap.put("flashArray", fieldMap.get("flash") != null && !StringUtils.isEmpty(fieldMap.get("flash").toString()) ? fieldMap.get("flash").toString().split(",") : new String[0]);
        fieldMap.put("error", Validation.error(_arg));
        fieldMap.put("errorClass", fieldMap.get("error") != null ? "hasError" : "");

        try {
            fieldMap.put("value", getDefaultValue(modelField));
        } catch (Exception e) {
            fieldMap.put("value", "");
        }

        body.setProperty("field", fieldMap);
        body.call();
    }

    public static void _label(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        // TODO: Check if there's "arg" argument
        // TODO: Throw illegal argument exception
        // TODO: Add for attr
        StringBuilder html = new StringBuilder();
        html.append("<label>");
        html.append(Messages.get(args.get("arg")));
        if (args.get("required") != null) {
            if (Boolean.parseBoolean(args.get("required").toString())) {    // TODO: Check if it's castable
                html.append("<em class='formee-req'>*</em>");
            }
        }
        html.append("</label>");
        out.println(html.toString());
    }

    /**
     * The field tag is a helper, based on the spirit of Don't Repeat Yourself.
     */
    public static void _error(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        Map.Entry<String, String> modelField = getModelField(args);

        String _arg = "";
        if (modelField != null) {
            _arg = String.format("%s.%s", getSimpleModelName(modelField.getKey()).toLowerCase(), modelField.getValue());
        }

        String cssClass = "error";
        if (args.containsKey("class")) {
            cssClass = String.format("%s %s", args.get("class"), cssClass);
        }

        StringBuilder html = new StringBuilder();
        html.append("<span");
        html.append(" for='").append(_arg.replace('.', '_')).append("'");
        html.append(" class='").append(cssClass).append("'");
        html.append(" generated='true'");
        html.append(serialize(args, "class"));
        html.append(">");

        out.println(html.toString());
        out.println(JavaExtensions.toString(body));
        out.println("</span>");
    }

    /**
     * Generates a html input element of type password linked to a field in model and validated accordingly.
     */
    public static void _password(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
        Map.Entry<String, String> modelField = getModelField(args);
        String dataValidation = getDataValidation(modelField);

        String passInput = "<input type='password' data-validate='%s' class='%s' id='%s' name='%s' value='%s' %s/>";
        passInput = formatHtmlElementAttributes(args, InputType.PASSWORD, passInput, modelField, dataValidation);
        out.print(passInput);
    }

    /**
     * Generates a html input element of type hidden linked to a field in model and validated accordingly.
     */
    public static void _hidden(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
        Map.Entry<String, String> modelField = getModelField(args);
        String input = "<input type='hidden' name='%s' value='%s'/>";
        input = formatHtmlElementAttributes(args, InputType.HIDDEN, input, modelField, null);
        out.print(input);
    }

    /**
     * Generates a html input element of type text linked to a field in model and validated accordingly.
     */
     public static void _text(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
        Map.Entry<String, String> modelField = getModelField(args);
        String dataValidation = getDataValidation(modelField);

        String input = "<input type='text' data-validate='%s' class='%s' id='%s' name='%s' value='%s' %s />";
        input = formatHtmlElementAttributes(args, InputType.TEXT, input, modelField, dataValidation);
        out.print(input);
        if (body != null) {
            out.print(String.format(JavaExtensions.toString(body), checkForConfirmElement(body, input)));
        }
    }

    /**
     * Generates a html input element of type textarea linked to a field in model and validated accordingly.
     */
     public static void _textarea(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
        Map.Entry<String, String> modelField = getModelField(args);
        String dataValidation = getDataValidation(modelField);

        String input = "<textarea data-validate='%s' class='%s' id='%s' name='%s' >%s";
        input = formatHtmlElementAttributes(args, InputType.TEXTAREA, input, modelField, dataValidation);
        out.print(input);
        out.print((body == null ? "" : JavaExtensions.toString(body)));
        out.print("</textarea>");
    }

    /**
     * Generates a html input element of type checkbox linked to a field in model and validated accordingly.
     */
    @SuppressWarnings("unchecked")
    public static void _checkbox(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
        Map.Entry<String, String> modelField = getModelField(args);
        String dataValidation = getDataValidation(modelField);

//        String js = "onchange=\"if($(this).is(':checked')){$(this).attr('checked','true');$(this).val(true)}else{$(this).val(false)};\"";
//        ((Map<Object, Object>) args).put("js", js);

        String input = "<input type='checkbox' data-validate='%s' class='%s' id='%s' name='%s' value='true' />";
        boolean checked = Boolean.parseBoolean(getDefaultValue(modelField).toString());
        if (checked) {
            ((Map<Object, Object>) args).put("checked", "checked");
        }

        input = formatHtmlElementAttributes(args, InputType.CHECKBOX, input, modelField, dataValidation);
        out.print(input);
    }

    public static void _radio(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
        // TODO implement radio button element.
    }

    /**
     * Generates a html input element of type checkbox linked to a field in model and validated accordingly.
     */
    @SuppressWarnings("unchecked")
    public static void _checkbool(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
        Map.Entry<String, String> modelField = getModelField(args);
        String dataValidation = getDataValidation(modelField);

        String input = "<input type='checkbox' data-validate='%s' class='%s' id='%s' name='%s' value='true' %s/>";
        boolean checked = Boolean.parseBoolean(getDefaultValue(modelField).toString());
        if (checked) {
            ((Map<Object, Object>) args).put("checked", "checked");
        }

        input = formatHtmlElementAttributes(args, InputType.CHECKBOOL, input, modelField, dataValidation);
        out.print(input);

        // Special case: Creates an input of type hidden
        String hiddenInput = "<input type='hidden' name='%s' value='false'/>";
        hiddenInput = formatHtmlElementAttributes(args, InputType.CONCEAL, hiddenInput, modelField);
        out.print(hiddenInput);
    }

    /**
     * Generates a html input element of type file and supports extra validation
     * like checking for extensions, min size, or max size.
     */
    public static void _file(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
        Map.Entry<String, String> modelField = getModelField(args);
        String dataValidation = getDataValidation(modelField);

        String input = "<input type='file' data-validate='%s' class='%s' id='%s' name='%s' value='%s' %s />";

        Object acceptExtentions = args.remove("accept");// match.
        Object rejectExtentions = args.remove("reject");// match.
        Object minSize = args.remove("min");// minSize.
        Object maxSize = args.remove("max");// maxsize.

        StringBuffer extraValidation = new StringBuffer();
        if (acceptExtentions != null) {
            extraValidation.append(",endsWith[.*?");
            extraValidation.append(acceptExtentions.toString().replace(",","$|"));
            extraValidation.append("$]");
        }
        if (rejectExtentions != null) {
            extraValidation.append(",notEndsWith[.*?");
            extraValidation.append(rejectExtentions.toString().replace(",","$|"));
            extraValidation.append("$]");
        }
        if (minSize != null) {
            extraValidation.append(",minSize[");
            extraValidation.append(minSize);
            extraValidation.append("]");
        }
        if (maxSize != null) {
            extraValidation.append(",maxSize[");
            extraValidation.append(maxSize);
            extraValidation.append("]");
        }

        input = formatHtmlElementAttributes(args, InputType.FILE, input, modelField, dataValidation);
        out.print(input);
    }

    public static void _checkboxList(Map<?, ?> args, Closure body,PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
        String htmlElement = "<input type=\"checkbox\" class=\"%s %s\" id=\"%s\" name=\"%s\" title=\"%s\" value=\"%s\"/>";
        String valueField = (String) args.remove("value");
        String titleField = (String) args.remove("title");
        List<?> items = (List<?>) args.remove("items");
        if (items.size() == 0) {
            return;
        }
        String checkboxListValidation = getCheckBoxValidation(args, false);
        printTheList(items, items.get(0).getClass(), titleField, valueField, htmlElement, out, args, checkboxListValidation);
    }

    public static void _radioList(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
        String radioButton = "<input type=\"radio\" class=\"%s %s\" id=\"%s\" name=\"%s\" title=\"%s\" value=\"%s\"/>";
        String value = (String) args.remove("value");
        String title = (String) args.remove("title");
        List<?> items = (List<?>) args.remove("items");
        String validation = (args.remove("required") == null ? "" : "required");

        if (items.size() == 0) {
            return;
        }
        printTheList(items, items.get(0).getClass(), title, value, radioButton, out, args, validation);
    }

    public static void _select(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        // TODO implement the select Element.
    }



    /**
     * Generates a formatted HTML element linked to a certain model field given
     * a string representation for this element.
     * @param args the calling tag attributes
     * @param htmlElement String representation for this element contains placeHolders (%s) to embedd in it its attributes values.
     * @param modelField the return of this method
     * @param dataValidation -
     * @return String representing the HTML element after embedding the attributes and the validation in it.
     * @throws Exception -
     */
    static String formatHtmlElementAttributes(Map<?, ?> args, InputType inputType, String htmlElement, Map.Entry<String, String> modelField, String dataValidation) throws Exception {
        args.remove("for");
        Object id = args.remove("id");
        Object _class = args.remove("class");
        Object name = args.remove("name");
        Object value = null;
        if (modelField != null) {
            value = getDefaultValue(modelField);
        }
        if (value == null) {
            value = "";
        }

        if (id == null && modelField != null) {
            id = String.format("%s_%s", getSimpleModelName(modelField.getKey()).toLowerCase(), modelField.getValue());
        }

        if (name == null && modelField != null) {
            name = String.format("%s.%s", getSimpleModelName(modelField.getKey()).toLowerCase(), modelField.getValue());
        }

        if (_class == null) {
            _class = (modelField == null ? "nullField" : modelField.getValue());
        }

        switch (inputType) {
            case CHECKBOOL:
                // Without value
                return String.format(htmlElement, dataValidation, _class, id, name, serialize(args));
            case CONCEAL:
                // Just name
                return String.format(htmlElement, name);
            case HIDDEN:
                // Just name & value
                return String.format(htmlElement, name, value);
            default:
                return String.format(htmlElement, dataValidation, _class, id, name, value, serialize(args));
        }
    }

    static String formatHtmlElementAttributes(Map<?, ?> args, InputType inputType, String htmlElement, Map.Entry<String, String> modelField) throws Exception {
        String dataValidation = null;
        return formatHtmlElementAttributes(args, inputType, htmlElement, modelField, dataValidation);
    }

    /**
     * Gets the model name & the field name from a map of arguments by the key
     * "field" and puts them in an array.
     * @param args tag attributes
     * @return Map.Entry representing the Model as key and the Field as value
     * @throws IllegalArgumentException if the field argument doesn't contain the model name or the field name
     */
    static Map.Entry<String, String> getModelField(Map<?,?> args) {
        Object name = args.get("arg");
        if (name == null) {
            throw new IllegalArgumentException("There's no 'type' argument");
        }

        List<String> tokens = new ArrayList<String>(Arrays.asList(StringUtils.split(name.toString(), '.')));
        String field = tokens.remove(tokens.size() - 1);
        String model = StringUtils.join(tokens, '.');
        return new AbstractMap.SimpleEntry<String, String>(model, field);
    }

    /**
     * Gets the value of the field, represented by its name in the
     * {@code modelField[1]}, from the Object specified in the
     * {@code edit} argument.
     *
     * @param modelField
     *            String[] contains the name of the field in [1].
     * @return the value of the field in an object returned from the map of
     *         properties in the current template under the key "_editObject_"
     * @throws Exception - see method {@link #getFieldValue(Object, String)}
     */
    static Object getDefaultValue(Map.Entry<String, String> modelField) throws Exception {
        if (modelField != null && BaseTemplate.layoutData.get().containsKey("_editObject_")) {
            Object obj = BaseTemplate.layoutData.get().get("_editObject_");
            return getFieldValue(obj, modelField.getValue());
        }
        return "";
    }

    /**
     * Uses reflection to get the value of a field, represented by its name,
     * from <code>Object obj</code>
     *
     * @param obj object to get the value of the field from.
     * @param fieldName name of the desired field.
     * @return the value of the field from the object
     * @throws Exception specified by these methods {@link Class#getField(String)} & {@link Field#get(Object)}
     */
    static Object getFieldValue(Object obj, String fieldName) throws Exception {
        try {
            return obj.getClass().getField(fieldName).get(obj);
        } catch (Exception e) {
            return "";
        }
    }

    private static Object checkForConfirmElement(Closure body, String html) {
        if (body != null) {
            return formatConfirmElement(html);
        } else {
            return null;
        }
    }

    private static Object formatConfirmElement(String html) {
        int startIndexOfId = html.indexOf("id=\"") + 4;
        int endIndexOfId = html.indexOf("\"", startIndexOfId);
        String id = html.substring(startIndexOfId, endIndexOfId);
        return html.replace("id=\"" + id + "\"", "id=\"" + id + "Confirm\"");
    }
    
    private static String getDataValidation(Map.Entry<String, String> modelField) {
        String model = modelField.getKey();
        String field = modelField.getValue();
        // Get dataValidation from FormeeValidation -> Map<Model, Map<Field, DataValidation>>
        Map<String, Map<String,String>> classFieldValidation = FormeeValidation.getInstance().getModelFieldValidation();
        Map<String, String> fieldValidation = classFieldValidation.get(model);
        String dataValidation = fieldValidation.get(field);

        return dataValidation;
    }
    
    private static String getSimpleModelName(String model) {
        if (!StringUtils.contains(model, '.')) {
            return model;
        }

        String[] tokens = StringUtils.split(model, '.');
        String simpleModelName = tokens[tokens.length - 1];

        return simpleModelName;
    }

    public static void printTheList(List<?> items, Class<?> objClass, String titleField, String valueField, String htmlElement, PrintWriter out, Map<?, ?> args, Object validation) throws Exception {
        String label = "<label class=\"%sLbl\" for=\"%s\">%s</label>";
        Object id = args.remove("id");
        Object _class = args.remove("class");
        Object name = args.remove("name");
        boolean horizontal = args.remove("horizontal") != null;
        String separator = "";
        if (horizontal) {
            separator = "&nbsp;&nbsp;&nbsp;&nbsp;";
        } else {
            separator = "<br/>";
        }
        if (id == null) {
            id = titleField;
        }
        if (name == null) {
            name = objClass.getSimpleName();
        }
        if (_class == null) {
            _class = objClass.getSimpleName();
        }

        int i = 1;
        for (Object obj : items) {
            String[] titleValue = new String[2];
//            titleValue[0] = VTags.getFieldValue(obj, titleField).toString(); // TODO: Fix it
//            titleValue[1] = VTags.getFieldValue(obj, valueField).toString(); // TODO: Fix it
            // class id title
            out.print(String.format(label, _class, id.toString() + i, titleValue[0]) + "&nbsp;");

            if (!validation.toString().isEmpty()) {
                validation = "validate[" + validation + "]";
            }
            // valid class id name title value
            out.println(String.format(htmlElement, validation, _class, id.toString() + i, name, titleValue[0], titleValue[1]));
            out.println(separator);
            i++;
        }
    }

    /**
     * Extracts validation info from the tag attributes {@code arg} that are
     * valid for inputs of type check box.
     *
     * @param args tag attributes
     * @param isSingle to check if the validation for single checkbox or for a group of checkboxes.
     * @return String representing the validation extracted.
     */
    private static String getCheckBoxValidation(Map<?, ?> args, boolean isSingle) {
        Object minCheckbox = args.remove("min");
        Object maxCheckbox = args.remove("max");
        if (minCheckbox == null) {
            minCheckbox = "";
        } else {
            minCheckbox = "minCheckbox[" + minCheckbox + "]";
        }

        if (maxCheckbox == null) {
            maxCheckbox = "";
        } else {
            maxCheckbox = ",maxCheckbox[" + maxCheckbox + "]";
        }
        if (isSingle) {
            boolean isRequired = args.remove("required") != null;
            Object group = args.remove(",group");
            if (group == null) {
                group = "";
            } else {
                group = "groupRequired[" + group + "]";
            }
            minCheckbox = minCheckbox.toString() + group + (isRequired ? "required" : "");
        }
        return minCheckbox + maxCheckbox.toString();
    }
}
