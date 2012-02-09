/**
 * Author: OMAROMAN
 * Date: 12/15/11
 * Time: 1:10 PM
 */

package play.modules.formee;

import java.util.Map;

import play.Play;
import play.Play.Mode;

public class FormeeValidation {

    private Map<String, Map<String, String>> modelFieldValidation;

    // For Singleton pattern
    private volatile static FormeeValidation uniqueInstance;

    /**
     * Private Constructor for Singleton.
     */
    private FormeeValidation() {

    }

    /**
     * Method for get a unique instance of this class (Singleton Pattern)
     * @return - a unique instance of Logger
     */
    public static FormeeValidation getInstance() {
        if (uniqueInstance == null) {
            synchronized (FormeeValidation.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new FormeeValidation();
                }
            }
        }
        return uniqueInstance;
    }

    public void setModelFieldValidation(Map<String, Map<String, String>> modelFieldValidation) {
        if (this.modelFieldValidation == null || Play.mode == Mode.DEV) {
            this.modelFieldValidation = modelFieldValidation;
        }
//		System.out.println("HashMapSingleton.setModelFieldValidation()");
    }

    public Map<String, Map<String, String>> getModelFieldValidation() {
//		System.out.println("HashMapSingleton.getModelFieldValidation()");
        return modelFieldValidation;
    }
}
