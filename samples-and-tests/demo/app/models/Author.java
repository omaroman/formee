/**
 * Author: OMAROMAN
 * Date: 11/4/11
 * Time: 11:52 AM
 */
package models;

import play.data.binding.As;
import play.data.validation.Min;
import play.data.validation.Required;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "authors")
public class Author extends AuthorAbs {

    @Required
    @Min(15)
    public Integer age;   // Just for testing purposes

    public boolean mastermind;
    
    @Transient
    @Required
    public Boolean agree = false;

    // Trick for passing the "category"
    @Transient
    @Required
    public Long category_id;

    @Temporal(TemporalType.TIMESTAMP)
    //@As(value = "dd/MM/yyyy")
    //@As(lang = "es_MX", value = "dd/MM/yyyy")
    public Date birthday;


}
