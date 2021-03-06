package tags;

import groovy.lang.Closure;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.data.binding.As;
import play.data.validation.Validation;
import play.i18n.Lang;
import play.i18n.Messages;
import play.modules.formee.FormeeProps;
import play.modules.formee.FormeeValidation;
import play.modules.formee.InputType;
import play.mvc.Scope.Flash;
import play.templates.BaseTemplate;
import play.templates.FastTags;
import play.templates.GroovyTemplate.ExecutableTemplate;
import play.templates.JavaExtensions;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                //_checkbox(args, body, out, template, fromLine);
                // TODO: Implement
                // I guess it doesn't make sense to implement a single radio input
            } else if (type.equals("checkbool")) {
                _checkbool(args, body, out, template, fromLine);
            } else if (type.equals("timestamp")) {
                _timestamp(args, body, out, template, fromLine);
            } else if (type.equals("radio")) {
                // TODO: Implement
                // I guess it doesn't make sense to implement a single radio input
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

    public static void _hbox(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        Object grid = args.get("grid") != null ? args.get("grid") : null;
        if (grid == null) {
            throw new IllegalArgumentException("There's no 'grid' argument");
        }
        int _grid;
        try {
            _grid = Integer.parseInt(grid.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'grid' argument is NaN");
        }

        Object _class = args.remove("class");
        if (_class == null) {
            _class = String.format("hbox grid-%d-12", _grid);
        } else {
            _class = String.format("hbox grid-%d-12 %s", _grid, _class);
        }

        String[] unless = new String[]{"grid", "class"};
        StringBuilder html = new StringBuilder();
        html.append("<div class='").append(_class).append("'");
        html.append(serialize(args, unless));
        html.append(">");
        out.println(html.toString());
        out.println((body == null ? "" : JavaExtensions.toString(body)));
        out.println("</div>");
    }

    public static void _vbox(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        Object grid = args.get("grid") != null ? args.get("grid") : null;
        if (grid == null) {
            throw new IllegalArgumentException("There's no 'grid' argument");
        }
        int _grid;
        try {
            _grid = Integer.parseInt(grid.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'grid' argument is NaN");
        }

        Object _class = args.remove("class");
        if (_class == null) {
            _class = String.format("vbox grid-%d-12", _grid);
        } else {
            _class = String.format("vbox grid-%d-12 %s", _grid, _class);
        }

        String[] unless = new String[]{"grid", "class"};
        StringBuilder html = new StringBuilder();
        html.append("<div class='").append(_class).append("'");
        html.append(serialize(args, unless));
        html.append(">");
        out.println(html.toString());
        out.println((body == null ? "" : JavaExtensions.toString(body)));
        out.println("</div>");
    }

    public static void _clear(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        Object height = args.get("height") != null ? args.get("height") : null;
        int _height = 0;
        if (height != null) {
            try {
                _height = Integer.parseInt(height.toString());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("'grid' argument is NaN");
            }
        }

        StringBuilder html = new StringBuilder();
        html.append("<div class='clear'");
        if (_height != 0) {
            html.append(" style='height:").append(_height).append("px;'");
        }
        html.append("></div>");
        out.println(html.toString());
    }

    @SuppressWarnings("unchecked")
    public static void _submit(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
        Object value = args.get("value") != null ? args.get("value") : null;
        if (value != null) {
            String i18nSubmit = (Messages.get(value));
            ((Map<Object, Object>) args).put("value", i18nSubmit);
        }

        Map.Entry<String, String> modelField = null;    // NO modelField needed by a "submit" type
        String input = "<input type='%s' class='%s' %s/>";
        input = formatHtmlElementAttributes(args, InputType.SUBMIT, input, modelField);
        out.print(input);
    }

    @SuppressWarnings("unchecked")
    public static void _reset(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
        Object value = args.get("value") != null ? args.get("value") : null;
        if (value != null) {
            String i18nSubmit = (Messages.get(value));
            ((Map<Object, Object>) args).put("value", i18nSubmit);
        }

        Map.Entry<String, String> modelField = null;    // NO modelField needed by a "submit" type
        String input = "<input type='%s' class='%s' %s/>";
        input = formatHtmlElementAttributes(args, InputType.RESET, input, modelField);
        out.print(input);
    }
    
    public static void _label(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        Map.Entry<String, String> modelField = getModelField(args);
        String arg = args.get("arg") != null ? args.get("arg").toString() : null;
        
        String _msg = args.get("msg") != null ? args.get("msg").toString() : null;
        String _for = args.get("for") != null ? args.get("for").toString() : null;
        if (_for == null) {
            throw new IllegalArgumentException("There's no 'for' argument");
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

        String inputElementName = getConventionName(arg, modelField);
        String inputElementId = inputElementName.replace('.', '_');

        String cssClass = "error";
        if (args.containsKey("class")) {
            cssClass = String.format("%s %s", args.get("class"), cssClass);
        }

        String[] unless = new String[]{"class", "for"};  // omit these parameters
        StringBuilder html = new StringBuilder();
        html.append("<span");
        html.append(" class='").append(cssClass).append("'");
        html.append(" for='").append(inputElementName).append("'");  // required in order to work with jquery.validate plug-in
        html.append(" generated='true'");   // required in order to work with jquery.validate plug-in
        html.append(serialize(args, unless));   // except unless
        html.append(">");

        out.println(html.toString());
        out.print((body == null ? "" : JavaExtensions.toString(body)));
        out.println("</span>");
    }

    /**
     * Generates a html input element of type password linked to a field in model and validated accordingly.
     */
    public static void _password(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
        Map.Entry<String, String> modelField = getModelField(args);

//        String passInput = "<input type='%s' data-validate='%s' class='%s' id='%s' name='%s' value='%s' %s/>";
        String input = "<input type='%s' data-validate='%s' class='%s' id='%s' name='%s' %s/>";
        input = formatHtmlElementAttributes(args, InputType.PASSWORD, input, modelField);
        out.print(input);
    }

    /**
     * Generates a html input element of type hidden linked to a field in model and validated accordingly.
     */
    public static void _hidden(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
        Map.Entry<String, String> modelField = getModelField(args);
        String input = "<input type='%s' name='%s' value='%s'/>";
        input = formatHtmlElementAttributes(args, InputType.HIDDEN, input, modelField);
        out.print(input);
    }

    /**
     * Generates a html input element of type text linked to a field in model and validated accordingly.
     */
     public static void _text(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
        Map.Entry<String, String> modelField = getModelField(args);

        String input = "<input type='%s' data-validate='%s' class='%s' id='%s' name='%s' value='%s' %s />";
        input = formatHtmlElementAttributes(args, InputType.TEXT, input, modelField);
        out.print(input);
        out.print(body == null ? "" : String.format(JavaExtensions.toString(body), checkForConfirmElement(body, input)));
    }

    /**
     * Generates a html input element of type textarea linked to a field in model and validated accordingly.
     */
     public static void _textarea(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
        Map.Entry<String, String> modelField = getModelField(args);

        String input = "<textarea data-validate='%s' class='%s' id='%s' name='%s' %s>%s";
        input = formatHtmlElementAttributes(args, InputType.TEXTAREA, input, modelField);
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

//        String js = "onchange=\"if($(this).is(':checked')){$(this).attr('checked','true');$(this).val(true)}else{$(this).val(false)};\"";
//        ((Map<Object, Object>) args).put("js", js);

        String input = "<input type='%s' data-validate='%s' class='%s' id='%s' name='%s' value='%s' %s/>";
        boolean checked = Boolean.parseBoolean(getDefaultValue(modelField).toString());
        if (checked) {
            ((Map<Object, Object>) args).put("checked", "checked");
        }

        input = formatHtmlElementAttributes(args, InputType.CHECKBOX, input, modelField);
        out.print(input);
    }

    @SuppressWarnings("unchecked")
    public static void _radio(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
        Map.Entry<String, String> modelField = getModelField(args);

        String input = "<input type='%s' data-validate='%s' class='%s' id='%s' name='%s' value='%s' %s/>";
        boolean checked = Boolean.parseBoolean(getDefaultValue(modelField).toString());
        if (checked) {
            ((Map<Object, Object>) args).put("checked", "checked");
        }

        input = formatHtmlElementAttributes(args, InputType.RADIO, input, modelField);
        out.print(input);
    }

//    @SuppressWarnings("unchecked")
//    private static void _option(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
//        Map.Entry<String, String> modelField = getModelField(args);
//
//        String input = "<option value='%s' %s>%s</option>";
//        boolean checked = Boolean.parseBoolean(getDefaultValue(modelField).toString());
//        if (checked) {
//            ((Map<Object, Object>) args).put("checked", "checked");
//        }
//
//        input = formatHtmlElementAttributes(args, InputType.OPTION, input, modelField);
//        out.print(input);
//    }

    /**
     * Generates a html input element of type checkbox linked to a field in model and validated accordingly.
     */
    @SuppressWarnings("unchecked")
    public static void _checkbool(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
        Map.Entry<String, String> modelField = getModelField(args);
//        String dataValidation = getDataValidation(modelField);

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

        input = formatHtmlElementAttributes(args, InputType.CHECKBOOL, input, modelField);
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

        input = formatHtmlElementAttributes(args, InputType.FILE, input, modelField);
        out.print(input);
    }

    public static void _checkboxList(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {

        // PARAMS:
        // arg -> (implicit optional argument) – name of the model object.
        // for -> the full qualified name of a field
        // items (required) -> a Collection of Objects (not necessarily Models) (required)
        // valueProperty (required) -> the field from which the value will be gotten
        // labelProperty (required) -> the field from which the label will be named
        // value (optional) -> the default value the select will be showing by default

        Map.Entry<String, String> modelField = getModelField(args);
        List<?> items = args.get("items") != null ? (List<?>) args.remove("items") : null;
        if (items == null) {
            throw new IllegalArgumentException("There's no 'items' argument");
        }
        String valueProperty = args.get("valueProperty") != null ? (String) args.remove("valueProperty") : null;
        if (valueProperty == null) {
            throw new IllegalArgumentException("There's no 'valueProperty' argument");
        }
        String labelProperty = args.get("labelProperty") != null ? (String) args.remove("labelProperty") : null;
        if (labelProperty == null) {
            throw new IllegalArgumentException("There's no 'labelProperty' argument");
        }

        String arg = args.get("arg") != null ? args.get("arg").toString() : null;
        String name = getConventionName(arg, modelField);
        Object value = getDefaultValue(modelField);
        Object dataValidation = getDataValidation(modelField);
        out.println("<ul class='formee-list'>");    // <--- the unordered list
        StringBuilder html;
        for (int i = 0; i < items.size(); i++) {
            html = new StringBuilder();
            Object item = items.get(i);
            Object val = getFieldValue(item, valueProperty);    // get value via reflection
            Object label = getFieldValue(item, labelProperty);  // get value via reflection

//            play.Logger.debug("VALUE: %s", value);
//            play.Logger.debug("VAL: %s", val);

            html.append("<li>");
            html.append("<input type='checkbox'");
            if (i == 0) {
                html.append(" data-validate='").append(dataValidation).append("'");
            }
            html.append(" name='").append(name).append("'");
            html.append(" id='").append(name).append('.').append(i).append("'");
            if (value != null && value.toString().equals(val.toString())) {
                html.append(" checked");
            } else {
//                play.Logger.debug("WARNING. Value for %s is null", name);
            }
            html.append("/>");
            html.append("<label for='"); // <--- the label
            html.append(name).append(".").append(i).append("'>");
            html.append(label);
            html.append("</label>"); // <--- the label
            html.append("</li>");
            out.println(html.toString());
        }
        out.println("</ul>");
    }

    public static void _radioList(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {

        // PARAMS:
        // arg -> (implicit optional argument) – name of the model object.
        // for -> the full qualified name of a field
        // items (required) -> a Collection of Objects (not necessarily Models) (required)
        // valueProperty (required) -> the field from which the value will be gotten
        // labelProperty (required) -> the field from which the label will be named
        // value (optional) -> the default value the select will be showing by default

        Map.Entry<String, String> modelField = getModelField(args);
        List<?> items = args.get("items") != null ? (List<?>) args.remove("items") : null;
        if (items == null) {
            throw new IllegalArgumentException("There's no 'items' argument");
        }
        String valueProperty = args.get("valueProperty") != null ? (String) args.remove("valueProperty") : null;
        if (valueProperty == null) {
            throw new IllegalArgumentException("There's no 'valueProperty' argument");
        }
        String labelProperty = args.get("labelProperty") != null ? (String) args.remove("labelProperty") : null;
        if (labelProperty == null) {
            throw new IllegalArgumentException("There's no 'labelProperty' argument");
        }

        String arg = args.get("arg") != null ? args.get("arg").toString() : null;
        String name = getConventionName(arg, modelField);
        Object value = getDefaultValue(modelField);
        Object dataValidation = getDataValidation(modelField);
        out.println("<ul class='formee-list'>");    // <--- the unordered list
        StringBuilder html;
        for (int i = 0; i < items.size(); i++) {
            html = new StringBuilder();
            Object item = items.get(i);
            Object val = getFieldValue(item, valueProperty);    // get value via reflection
            Object label = getFieldValue(item, labelProperty);  // get value via reflection

//            play.Logger.debug("VALUE: %s", value);
//            play.Logger.debug("VAL: %s", val);

            html.append("<li>");
            html.append("<input type='radio'");
            if (i == 0) {
                html.append(" data-validate='").append(dataValidation).append("'");
            }
            html.append(" name='").append(name).append("'");
            html.append(" id='").append(name).append(".").append(i).append("'");
            html.append(" value='").append(val).append("'");
            if (value != null && value.toString().equals(val.toString())) {
                html.append(" checked");
            } else {
//                play.Logger.debug("WARNING. Value for %s is null", name);
            }
            html.append("/>");
            html.append("<label for='"); // <--- the label
            html.append(name).append(".").append(i).append("'>");
            html.append(label);
            html.append("</label>"); // <--- the label
            html.append("</li>");
            out.println(html.toString());
        }
        out.println("</ul>");
    }
    
    public static void _selectList(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
        // Built-in slow tag
        // #{select 'users', items:users, valueProperty:'id', labelProperty:'name', value:5, class:'test', id:'select2' /}

        // PARAMS:
        // arg -> (implicit optional argument) – name of the model object.
        // for -> the full qualified name of a field
        // items (required) -> a Collection of Objects (not necessarily Models) (required)
        // valueProperty (required) -> the field from which the value will be gotten
        // labelProperty (required) -> the field from which the label will be named
        // defaultValue (optional) -> the default value the select will be showing by default

        Map.Entry<String, String> modelField = getModelField(args);
        List<?> items = args.get("items") != null ? (List<?>) args.remove("items") : null;
        if (items == null) {
            throw new IllegalArgumentException("There's no 'items' argument");
        }
        String valueProperty = args.get("valueProperty") != null ? (String) args.remove("valueProperty") : null;
        if (valueProperty == null) {
            throw new IllegalArgumentException("There's no 'valueProperty' argument");
        }
        String labelProperty = args.get("labelProperty") != null ? (String) args.remove("labelProperty") : null;
        if (labelProperty == null) {
            throw new IllegalArgumentException("There's no 'labelProperty' argument");
        }
        String supplementaryLabelProperty = args.get("supplementaryLabelProperty") != null ? (String) args.remove("supplementaryLabelProperty") : null;
//        if (supplementaryLabelProperty == null) {
//            throw new IllegalArgumentException("There's no 'labelProperty' argument");
//        }

        String input = "<select data-validate='%s' class='%s' id='%s' name='%s' %s>";
        input = formatHtmlElementAttributes(args, InputType.SELECT_LIST, input, modelField);
        out.print(input);
        
        if (body != null) {
            out.println(JavaExtensions.toString(body)); // Prints the html-body-code between #{formee.select} and #{/formee.select}
        }
        
        out.println("<option></option>"); // An empty item, required for working with jquery.validate

        Object value = getDefaultValue(modelField);
        StringBuilder html;
        for (Object item : items) {
            html = new StringBuilder();
            Object val = getFieldValue(item, valueProperty);    // get value via reflection
            Object label = getFieldValue(item, labelProperty);  // get value via reflection
            Object supplementaryLabel = null;
            if (supplementaryLabelProperty != null) {
                supplementaryLabel = getFieldValue(item, supplementaryLabelProperty);  // get value via reflection    
            }

//            play.Logger.debug("VALUE: %s", value);
//            play.Logger.debug("VAL: %s", val);

            html.append("<option");
            html.append(" value='").append(val).append("'");
            if (value != null && value.toString().equals(val.toString())) {
                html.append(" selected");
            } else {
//                String arg = args.get("arg") != null ? args.get("arg").toString() : null;
//                String name = getConventionName(arg, modelField);
//                play.Logger.debug("WARNING. Value for %s is null", name);
            }
            html.append(">");
            if (supplementaryLabel != null ) {
                html.append(String.format("%s - %s", label, supplementaryLabel));
            } else {
                html.append(label);
            }
            html.append("</option>");
            out.println(html.toString());
        }
        
        out.println("</select>");
    }

    public static void _timestamp(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws Exception {
        Map.Entry<String, String> modelField = getModelField(args);
        String input = "<input type='%s' data-validate='%s' class='%s' id='%s' name='%s' value='%s' %s />";
        input = formatHtmlElementAttributes(args, InputType.TIMESTAMP, input, modelField);
        out.print(input);
        //out.print(body == null ? "" : String.format(JavaExtensions.toString(body), checkForConfirmElement(body, input)));
    }

    /**
     * Generates a formatted HTML element linked to a certain model field given
     * a string representation for this element.
     * @param args the calling tag attributes
     * @param inputType the html input type element (textare, text, password, hidden, checkbool, etc.)
     * @param htmlElement String representation for this element contains placeHolders (%s) to embedd in it its attributes values.
     * @param modelField the return of this method
     * @return String representing the HTML element after embedding the attributes and the validation in it.
     * @throws Exception -
     */
    private static String formatHtmlElementAttributes(Map<?, ?> args, InputType inputType, String htmlElement, Map.Entry<String, String> modelField) throws Exception {
        String arg = args.get("arg") != null ? args.get("arg").toString() : null;

        Object id = args.remove("id");
        Object _class = args.remove("class");
        Object name = args.remove("name");
        
        Object dataValidation = null;   // Useful when input type is "submit" or "reset"
        if (modelField != null) {
            dataValidation = getDataValidation(modelField);
        }
        if (dataValidation == null) {
            dataValidation = "";
        }
        
        Object value = null;    // Useful when input type is "submit" or "reset"
        if (modelField != null) {
            value = args.get("value");  // In case the value was already provided
            if (value == null) {
                value = getDefaultValue(modelField);
            }
        }
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
            case TEXTAREA:
                // Without input type
                return String.format(htmlElement, dataValidation, _class, id, name, serialize(args, unless), value);    // except unless
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
            case SELECT_LIST:
                // Without input type & value
                return String.format(htmlElement, dataValidation, _class, id, name, serialize(args, unless));
            case RADIO_LIST:
                return "";
            case CHECKBOX_LIST:
                return "";
            case SUBMIT:
                // Just input type & class
                return String.format(htmlElement, _inputType, _class, serialize(args));
            case RESET:
                // Just input type & class
                return String.format(htmlElement, _inputType, _class, serialize(args));
            case TIMESTAMP:
                _inputType = InputType.TEXT.toString().toLowerCase();
            default:
                return String.format(htmlElement, _inputType, dataValidation, _class, id, name, value, serialize(args, unless));    // except unless
        }
    }

    /**
     * Gets the model name & the field name from a map of arguments by the key
     * "field" and puts them in an array.
     * @param args tag attributes
     * @return Map.Entry representing the Model as key and the Field as value
     * @throws IllegalArgumentException if the field argument doesn't contain the model name or the field name
     */
    private static Map.Entry<String, String> getModelField(Map<?,?> args) {
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
    private static Object getDefaultValue(Map.Entry<String, String> modelField) throws Exception {
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
    private static Object getFieldValue(Object obj, String fieldName) throws Exception {
        try {
            Class fieldType = obj.getClass().getField(fieldName).getType();
            if (fieldType.equals(java.util.Date.class)) { // Is of type Date?
                Field f = obj.getClass().getField(fieldName);
                String pattern = getDatePattern(f);
                java.util.Date date = (java.util.Date) obj.getClass().getField(fieldName).get(obj);
                return JavaExtensions.format(date, pattern);
            } else {
                return obj.getClass().getField(fieldName).get(obj);
                //return PropertyUtils.getProperty(obj, fieldName)).getTime(); <--- (BeanUtils) this statement doesn't get the value
            }
        } catch (Exception e) {
            return "";
        }
    }
    
    private static String getDatePattern(Field f) {
        String pattern = Play.configuration.getProperty("date.format", "yyyy-MM-dd");   // Default until declared otherwise
        String lang = Lang.get();    // get the current language for the user
//        play.Logger.debug("CURRENT LANG: %s", lang);

        As as = f.getAnnotation(As.class);
        if (as != null) {   // Field is annotated with @As
            if (!lang.isEmpty()) {    // There's a current lang configured
                if (as.lang().length == 1 && as.lang()[0].equals("*") && as.value().length == 1) {  // No lang specified, but value did
                    pattern = as.value()[0];
                } else {
                    // try to find the current lang from the array
                    if (ArrayUtils.contains(as.lang(), lang)) {
                        int idx = ArrayUtils.indexOf(as.lang(), lang);
                        pattern = as.value()[idx];
                    }
                }
            } else if (as.value().length > 0) {    // There's NO current lang configured
                pattern = as.value()[0];    // get the very first one
            }
        } else {     // Field is NOT annotated with @As
            if (!lang.isEmpty() && Play.langs.contains(lang)) {
                pattern = Play.configuration.getProperty("date.format." + lang, "yyyy-MM-dd");
            }
        }

        return pattern;
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

//    public static void printTheList(List<?> items, Class<?> objClass, String titleField, String valueField, String htmlElement, PrintWriter out, Map<?, ?> args, Object validation) throws Exception {
//        String label = "<label class='sLbl' for='%s'>%s</label>";
//        Object id = args.remove("id");
//        Object _class = args.remove("class");
//        Object name = args.remove("name");
//        boolean horizontal = args.remove("horizontal") != null;
//        String separator = "";
//        if (horizontal) {
//            separator = "&nbsp;&nbsp;&nbsp;&nbsp;";
//        } else {
//            separator = "<br/>";
//        }
//        if (id == null) {
//            id = titleField;
//        }
//        if (name == null) {
//            name = objClass.getSimpleName();
//        }
//        if (_class == null) {
//            _class = objClass.getSimpleName();
//        }
//
//        int i = 1;
//        for (Object obj : items) {
//            String[] titleValue = new String[2];
//            titleValue[0] = getFieldValue(obj, titleField).toString();
//            titleValue[1] = getFieldValue(obj, valueField).toString();
//            // class id title
//            out.print(String.format(label, _class, id.toString() + i, titleValue[0]) + "&nbsp;");
//
//            if (!validation.toString().isEmpty()) {
//                validation = "validate[" + validation + "]";
//            }
//            // valid class id name title value
//            out.println(String.format(htmlElement, validation, _class, id.toString() + i, name, titleValue[0], titleValue[1]));
//            out.println(separator);
//            i++;
//        }
//    }

//    /**
//     * Extracts validation info from the tag attributes {@code arg} that are
//     * valid for inputs of type check box.
//     *
//     * @param args tag attributes
//     * @param isSingle to check if the validation for single checkbox or for a group of checkboxes.
//     * @return String representing the validation extracted.
//     */
//    private static String getCheckBoxValidation(Map<?, ?> args, boolean isSingle) {
//        Object minCheckbox = args.remove("min");
//        Object maxCheckbox = args.remove("max");
//        if (minCheckbox == null) {
//            minCheckbox = "";
//        } else {
//            minCheckbox = "minCheckbox[" + minCheckbox + "]";
//        }
//
//        if (maxCheckbox == null) {
//            maxCheckbox = "";
//        } else {
//            maxCheckbox = ",maxCheckbox[" + maxCheckbox + "]";
//        }
//        if (isSingle) {
//            boolean isRequired = args.remove("required") != null;
//            Object group = args.remove(",group");
//            if (group == null) {
//                group = "";
//            } else {
//                group = "groupRequired[" + group + "]";
//            }
//            minCheckbox = minCheckbox.toString() + group + (isRequired ? "required" : "");
//        }
//        return minCheckbox + maxCheckbox.toString();
//    }

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
