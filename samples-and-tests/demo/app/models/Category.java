/**
 * Author: omaroman
 * Date: 12/29/11
 * Time: 11:49 AM
 */
package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category extends Model {

    // REVERSE ASSOCIATIONS
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY) // name of the variable in the other object that references this object
    public List<Author> author = new ArrayList<Author>(); // has_many :authors
    
    // Fields

    public String name;
    public String grade;
}
