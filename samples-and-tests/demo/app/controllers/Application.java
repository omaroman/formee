package controllers;

import play.cache.Cache;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.*;

import java.util.*;

import models.*;

public class Application extends Controller {

    @Util
    private static List<Category> loadCategories() {
        List<Category> categories = Cache.get("categories", List.class);
        if(categories == null) {
            categories = Category.findAll();
            Cache.set("categories", categories, "30mn");
        }
        return categories;
    }

    public static void index() {
        List<Author> authors = Author.findAll();
        render(authors);
    }

    public static void add_h() {
        Author author = new Author();   // Create an empty obj.
        final List<Category> categories = loadCategories();
        render(author, categories);
    }
    
    public static void create_h(@Valid Author author) {
        if (Validation.hasErrors()) {
            final List<Category> categories = loadCategories();
            render("@add_h", author, categories);
        }
        author.category = Category.findById(author.category_id);
        author.save();
        flash.success("views.author.create.msg");
        Application.index();
    }

    public static void add_v() {
        Author author = new Author();   // Create an empty obj.
        final List<Category> categories = loadCategories();
        render(author, categories);
    }

    public static void create_v(@Valid Author author) {
        if (Validation.hasErrors()) {
            final List<Category> categories = loadCategories();
            render("@add_v", author, categories);
        }
        author.category = Category.findById(author.category_id);
        author.save();
        flash.success("views.author.create.msg");
        Application.index();
    }

    public static void add() {
        Author author = new Author();   // Create an empty obj.
        final List<Category> categories = loadCategories();
        render(author, categories);
    }

    public static void create(@Valid Author author) {
        if (Validation.hasErrors()) {
            final List<Category> categories = loadCategories();
            render("@add", author, categories);
        }
        author.category = Category.findById(author.category_id);
        author.save();
        flash.success("views.author.create.msg");
        Application.index();
    }

    public static void edit(Long id) {
        Author author = Author.findById(id);
        if (author == null) {
            notFound();
        } else {
            // Since category_id field is Transient, it must be set before rendering in order to be comparable.
            // It can also be set in a @PostLoad method
            author.category_id = author.category.id;
            final List<Category> categories = loadCategories();
            render(author, categories);
        }
    }
    
    public static void update(@Valid Author author) {
        if (Validation.hasErrors()) {
            for (play.data.validation.Error error : Validation.errors()) {
                play.Logger.debug(error.message());
            }
            List<Category> categories = loadCategories();
            render("@edit", author, categories);
        }
        author.category = Category.findById(author.category_id);
        author.save();
        flash.success("views.author.update.msg");
        Application.index();
    }
    
    public static void delete(Long id) {
        Author author = Author.findById(id);
        if (author == null) {
            notFound();
        } else {
            author.delete();
        }
        flash.success("views.author.delete.msg");
        index();
    }

}