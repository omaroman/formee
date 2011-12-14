/**
 * Author: OMAROMAN
 * Date: 11/4/11
 * Time: 11:52 AM
 */
package models;

import play.data.validation.Min;
import play.data.validation.Required;

import javax.persistence.*;

@Entity
@Table(name = "authors")
public class Author extends AuthorAbs {

    @Required
    @Min(15)
    public int age;   // Just for testing purposes

}
