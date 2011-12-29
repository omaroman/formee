/**
 * Author: OMAROMAN
 * Date: 11/10/11
 * Time: 11:19 AM
 */

package models;

import play.data.validation.Required;
import play.data.validation.Unique;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@MappedSuperclass
public class AuthorAbs extends Model {

    // REVERSE ASSOCIATIONS

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY) // name of the variable in the other object that references this object
    public List<Quote> quotes = new ArrayList<Quote>(); // has_many :quotes

    // NORMAL ASSOCIATIONS

    @ManyToOne() // Optional, targetEntity for indicating where's the relationship
    @JoinColumn(name = "category_id") // name of the FK field in this table
    // --
    public Category category = new Category(); // belongs_to_one :category

    // Trick for passing the "category"
    @Transient
    @Required
    public Long category_id;

    // FIELDS

    @Required
    @Unique
    public String first_name;

    @Required
    public String last_name;
}
