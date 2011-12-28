package tags;

import groovy.lang.Closure;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.*;


import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import play.data.validation.*;
import play.i18n.Messages;
import play.modules.formee.Formee;
import play.modules.formee.FormeeProps;
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
        if (args.get("obj") == null) {
            throw new IllegalArgumentException("There's no 'obj' argument");
        }

        if (args.containsKey("class")) {
            ((Map<Object, Object>) args).put("class", args.remove("class") + " formee");
        } else {
            ((Map<Object, Object>) args).put("class", "formee");
        }

        // Store object into BaseTemplate.layoutData Map, in order to be available from BaseTemplate
        BaseTemplate.layoutData.get().put("_editObject_", args.remove("obj"));

        FastTags._form(args, body, out, template, fromLine);
    }

    @SuppressWarnings("unchecked")
    public static void _input(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
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
        Map.Entry<String, String> modelField = getModelField(args);
        String arg = args.get("arg") != null ? args.get("arg").toString() : null;
        String var = getConventionName(arg, modelField);

        Map<String,Object> fieldMap = new HashMap<String,Object>();
        fieldMap.put("for", args.get("for"));
        fieldMap.put("name", var);
        fieldMap.put("id", var.replace('.', '_'));
        fieldMap.put("flash", Flash.current().get(var));
        fieldMap.put("flashArray", fieldMap.get("flash") != null && !StringUtils.isEmpty(fieldMap.get("flash").toString()) ? fieldMap.get("flash").toString().split(",") : new String[0]);
        fieldMap.put("error", Validation.error(var));
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
        Map.Entry<String, String> modelField = getModelField(args);
        String arg = args.get("arg") != null ? args.get("arg").toString() : null;
        
        String _msg = args.get("msg") != null ? args.get("msg").toString() : null;
        String _for = args.get("for") != null ? args.get("for").toString() : null;
        if (_for == null) {
            throw new IllegalArgumentException("There's neither 'for' argument");
        }
        String msg = _msg != null ? _msg : Messages.get(_for); // if there's 'msg', then use it, otherwise use 'for'
        if (msg.equals(_for)) {
            // In case there's no internationalization message for current field
            String[] tokens = StringUtils.split(msg, '.');
            msg = tokens[tokens.length - 1];
        }

        String id = getConventionName(arg, modelField).replace('.', '_');
        String[] unless = new String[]{"for", "msg"};

        StringBuilder html = new StringBuilder();
        html.append("<label for='").append(id).append("' ");
        html.append(serialize(args, unless));
        html.append(">");
        html.append(Messages.get(msg));
        if (args.get("required") != null) {
            if (Boolean.parseBoolean(args.get("required").toString())) {    // TODO: Check if it's castable -> InvalidArgumentException
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
        String arg = args.get("arg") != null ? args.get("arg").toString() : null;

        String inputElementId = getConventionName(arg, modelField).replace('.', '_');

        String cssClass = "error";
        if (args.containsKey("class")) {
            cssClass = String.format("%s %s", args.get("class"), cssClass);
        }

        String[] unless = new String[]{"class", "for"};  // omit these parameters
        StringBuilder html = new StringBuilder();
        html.append("<span");
        html.append(" class='").append(cssClass).append("'");
        html.append(" for='").append(inputElementId).append("'");  // required in order to work with jquery.validate plug-in
        html.append(" generated='true'");   // required in order to work with jquery.validate plug-in
        html.append(serialize(args, unless));   // except unless
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

//        String passInput = "<input type='%s' data-validate='%s' class='%s' id='%s' name='%s' value='%s' %s/>";
        String passInput = "<input type='%s' data-validate='%s' class='%s' id='%s' name='%s' %s/>";
        passInput = formatHtmlElementAttributes(args, InputType.PASSWORD, passInput, modelField, dataValidation);
        out.print(passInput);
    }

    /**
     * Generates a html input element of type hidden linked to a field in model and validated accordingly.
     */
    public static void _hidden(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
        Map.Entry<String, String> modelField = getModelField(args);
        String input = "<input type='%s' name='%s' value='%s'/>";
        input = formatHtmlElementAttributes(args, InputType.HIDDEN, input, modelField, null);
        out.print(input);
    }

    /**
     * Generates a html input element of type text linked to a field in model and validated accordingly.
     */
     public static void _text(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
        Map.Entry<String, String> modelField = getModelField(args);
        String dataValidation = getDataValidation(modelField);

        String input = "<input type='%s' data-validate='%s' class='%s' id='%s' name='%s' value='%s' %s />";
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

        String input = "<input type='%s' data-validate='%s' class='%s' id='%s' name='%s' value='true' />";
        boolean checked = Boolean.parseBoolean(getDefaultValue(modelField).toString());
        if (checked) {
            ((Map<Object, Object>) args).put("checked", "checked");
        }

        input = formatHtmlElementAttributes(args, InputType.CHECKBOX, input, modelField, dataValidation);
        out.print(input);
    }

    public static void _radio(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
        // There's no sense in implementing a single radio
        // @See radioList
    }

    /**
     * Generates a html input element of type checkbox linked to a field in model and validated accordingly.
     */
    @SuppressWarnings("unchecked")
    public static void _checkbool(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
        Map.Entry<String, String> modelField = getModelField(args);
        String dataValidation = getDataValidation(modelField);

        String input = "<input type='%s' data-validate='%s' class='%s' id='%s' name='%s' value='true' %s/>";
        boolean checked;
        try {
            checked = Boolean.parseBoolean(getDefaultValue(modelField).toString()); // boolean primitive
        } catch (NullPointerException e1) {
            try {
                checked = (Boolean) getDefaultValue(modelField); // Boolean Wrapper
            } catch (NullPointerException e2) {
                // Finally, if checked is not primitive and wrapper is not initialized
                checked = false;
            }
        }
        if (checked) {
            ((Map<Object, Object>) args).put("checked", "checked");
        }

        input = formatHtmlElementAttributes(args, InputType.CHECKBOOL, input, modelField, dataValidation);
        out.print(input);

        // Special case: Creates an input of type hidden
        String hiddenInput = "<input type='%s' name='%s' value='false'/>";
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

        String input = "<input type='%s' data-validate='%s' class='%s' id='%s' name='%s' value='%s' %s />";

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
        String htmlElement = "<input type='checkbox' class='%s' id='%s' name='%s' title='%s' value='%s'/>";
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
        String radioButton = "<input type='radio' class='%s' id='%s' name='%s' title='%s' value='%s'/>";
        String value = (String) args.remove("value");
        String title = (String) args.remove("title");
        List<?> items = (List<?>) args.remove("items");
        String validation = (args.remove("required") == null ? "" : "required");

        if (items.size() == 0) {
            return;
        }
        printTheList(items, items.get(0).getClass(), title, value, radioButton, out, args, validation);
    }

    public static void _selectList(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        // TODO implement the select Element.
//        #{select 'users', items:users, valueProperty:'id', labelProperty:'name', value:5, class:'test', id:'select2' /}

        // PARAMS:
        // arg -> the full qualified name of a field
        // a Collection of Objects (not necessarily Models) (required)
        // valueProperty (required) -> the field from which the value will be gotten
        // labelProperty (required) -> the field from which the label will be named
        // value (optional) -> the default value the select will be showing by default

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
        String arg = args.get("arg") != null ? args.get("arg").toString() : null;

        Object id = args.remove("id");
        Object _class = args.remove("class");
        Object name = args.remove("name");
        Object value = getDefaultValue(modelField);

        if (value == null) {
            value = "";
        }

        if (id == null && modelField != null) {
            id = getConventionName(arg, modelField).replace('.', '_');
        }

        if (name == null && modelField != null) {
            name = getConventionName(arg, modelField);
        }

        if (_class == null) {
            _class = "";
        }

        String[] unless = new String[]{"for"};  // omit these parameters
        String _inputType = inputType.toString().toLowerCase();
        switch (inputType) {
            case PASSWORD:
                // Without value
                return String.format(htmlElement, _inputType, dataValidation, _class, id, name, serialize(args, unless));   // except unless
            case CHECKBOOL:
                // Without value
                _inputType = InputType.CHECKBOX.toString().toLowerCase();
                return String.format(htmlElement, _inputType, dataValidation, _class, id, name, serialize(args, unless));   // except unless
            case CONCEAL:
                // Just name
                _inputType = InputType.HIDDEN.toString().toLowerCase();
                return String.format(htmlElement, _inputType, name);
            case HIDDEN:
                // Just name & value
                return String.format(htmlElement, _inputType, name, value);
            default:
                return String.format(htmlElement, _inputType, dataValidation, _class, id, name, value, serialize(args, unless));    // except unless
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
        String _for = args.get("for") != null ? args.get("for").toString() : null;
        if (_for == null) {
            throw new IllegalArgumentException("There's no 'for' argument");
        }

        String[] tokens = StringUtils.split(_for, '.');
        if (tokens.length < 3) {
            throw new IllegalArgumentException("'for' argument is not of the form: package.Model.field");
        }


        String field = tokens[tokens.length - 1];   // field name is supposed to start with lowercase
        tokens = (String[]) ArrayUtils.remove(tokens, tokens.length - 1);   // remove the last element
        String model = StringUtils.join(tokens, '.');

//        play.Logger.debug("MODEL: %s", model);
//        play.Logger.debug("FIELD: %s", field);

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
        try {
            return fieldValidation.get(field); // get Data Validation
        } catch (NullPointerException e) {
            String error = String.format("for:'%s.%s' -> full qualified name of field doesn't exist", model, field);
            throw new IllegalArgumentException(error);
        }
    }

    private static String getConventionName(String arg, Map.Entry<String, String> modelField) {
        if (arg == null) {
            // From model get last token
            String canonicalModel = modelField.getKey();
            String[] tokens = StringUtils.split(canonicalModel, '.');
            String simpleModel = tokens[tokens.length - 1];

            // Naming Case...
            String simpleModelName = null;
            if (FormeeProps.getNamingCase().equals(FormeeProps.CAMEL_CASE)) {
                simpleModelName = toCamelCase(simpleModel);
            } else if (FormeeProps.getNamingCase().equals(FormeeProps.UNDERSCORE_CASE)) {
                simpleModelName = to_underscore_case(simpleModel);
            } else if (FormeeProps.getNamingCase().equals(FormeeProps.PROPER_CASE)) {
                simpleModelName = ToProperCase(simpleModel);
            } else {
                // This line shall never be reached
                assert false : "This line shall never be reached";
            }
            return String.format("%s.%s", simpleModelName, modelField.getValue());
        } else {
            return String.format("%s.%s", arg, modelField.getValue());
        }
    }

    public static void printTheList(List<?> items, Class<?> objClass, String titleField, String valueField, String htmlElement, PrintWriter out, Map<?, ?> args, Object validation) throws Exception {
        String label = "<label class='sLbl' for='%s'>%s</label>";
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
            titleValue[0] = getFieldValue(obj, titleField).toString();
            titleValue[1] = getFieldValue(obj, valueField).toString();
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

    private static String to_underscore_case(String name) {
        String[] tokens = StringUtils.splitByCharacterTypeCamelCase(name);
        StringBuilder simpleName = new StringBuilder();
        for (int i = 0; i<tokens.length; i++) {
            if (!tokens[i].equals("_")) {
                simpleName.append(tokens[i].toLowerCase());
            } else {
                continue;
            }
            if(i < tokens.length - 1) {
                simpleName.append('_');
            }
        }

        return simpleName.toString();
    }
    
    static String toCamelCase(String name){
        String[] tokens = StringUtils.splitByCharacterTypeCamelCase(name);
        StringBuilder simpleName = new StringBuilder();
        for (int i = 0; i<tokens.length; i++) {
            if (i == 0) {
                simpleName.append(tokens[i].toLowerCase());
                continue;
            }
            simpleName.append(JavaExtensions.capFirst(tokens[i]));
        }

        return simpleName.toString();
    }

    static String ToProperCase(String name) {
        return JavaExtensions.camelCase(name);
    }
}
