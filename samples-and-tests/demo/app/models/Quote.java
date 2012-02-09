/**
 * Author: OMAROMAN
 * Date: 11/4/11
 * Time: 11:53 AM
 */
package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;

@Entity
@Table(name = "quotes")
public class Quote extends Model {

    // Associations

    @ManyToOne() // Optional, targetEntity for indicating where's the relationship
    @JoinColumn(name = "author_id") // name of the FK field in this table
    // --
    @Required
    public Author author;   // belongs_to_one :author


    // Fields

    @Required
    public String quotation;
}
