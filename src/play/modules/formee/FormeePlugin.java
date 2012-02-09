/**
 * Author: OMAROMAN
 * Date: 12/15/11
 * Time: 1:37 PM
 */

package play.modules.formee;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.db.jpa.GenericModel;
import play.exceptions.ConfigurationException;

/**
 * PLAY! Plugin to do some logic on startup
 */
public class FormeePlugin extends PlayPlugin {

    private FormeeValidation formeeValidation = FormeeValidation.getInstance();

    @Override
    public void onConfigurationRead() {
        String namingCase = Play.configuration.getProperty(FormeeProps.CONFIG_PREFIX + "namingCase", FormeeProps.DEFAULT_NAMING_CASE);
        if (!namingCase.equals(FormeeProps.CAMEL_CASE)) {
            if (!namingCase.equals(FormeeProps.UNDERSCORE_CASE)) {
                if (!namingCase.equals(FormeeProps.PROPER_CASE)) {
                    String error = "===== formee.namingCase property is not configured correctly. Check your application.conf =====";
                    throw new ConfigurationException(error);
                }
            }
        }
        FormeeProps.namingCase = namingCase;
    }

    /**
     * Called at application start (and at each reloading) Time to analyze the
     * models and update the map containing the fields and their validations
     */
    @Override
    public void onApplicationStart() {
        // // System.out.println("StartUp.onApplicationStart()");
        if (Play.mode == Play.Mode.DEV) {
            formeeValidation.setModelFieldValidation(null);
        }

        if (formeeValidation.getModelFieldValidation() != null) {
            // // System.out.println("StartUp.onApplicationStart()" + MapSingleton.getModelFieldValidation());
            return;
        }

        // Model -> Field -> DataValidation
        Map<String, Map<String, String>> modelFieldValidation = new HashMap<String, Map<String, String>>();

        // Get all fields from all classes from @Entity up to Model
        try {
            @SuppressWarnings("rawtypes")
            List<Class> classes = Play.classloader.getAllClasses();
            for (Class<?> c : classes) {
                Class entityModel = c;
                if (c.getAnnotation(Entity.class) != null) {
                    modelFieldValidation.put(entityModel.getName(), new HashMap<String, String>());
                    while (!c.getName().equals(GenericModel.class.getName())) {
                        for (Field f : c.getDeclaredFields()) {
                            String dataValidation = Formee.buildValidationDataString(f);
                            if (!dataValidation.isEmpty()) {
                                modelFieldValidation.get(entityModel.getName()).put(f.getName(), dataValidation);
                            }
                        }
                        c = c.getSuperclass();
                        if (c.getAnnotation(MappedSuperclass.class) == null) {
                            Logger.debug("This message shall never be printed!!!");
                            break;
                        }
                    }
                    if (modelFieldValidation.get(entityModel.getName()).isEmpty()) {
                        modelFieldValidation.remove(entityModel.getName());
                    }
                }    
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        for (String model : modelFieldValidation.keySet()) {
//            for (String field : modelFieldValidation.get(model).keySet()) {
//                String dataValidation = modelFieldValidation.get(model).get(field);
//                Logger.debug("%s - %s - %s", model, field, dataValidation);
//            }
//        }

        formeeValidation.setModelFieldValidation(modelFieldValidation);
//        play.Logger.debug("FORMEE END %s", modelFieldValidation);
    }

    
}

