package controllers;

import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.*;

import java.util.*;

import models.*;

public class ApplicationController extends Controller {

    public static void index() {
        List<Author> authors = Author.findAll();
        render(authors);
    }

    public static void add_h() {
        render();
    }
    
    public static void create_h(@Valid Author author) {
        if (Validation.hasErrors()) {
            render("@add_h", author);
        }
        author.save();
        flash.success("views.author.create.msg");
        ApplicationController.index();
    }

    public static void add_v() {
        render();
    }

    public static void create_v(@Valid Author author) {
        if (Validation.hasErrors()) {
            render("@add_v", author);
        }
        author.save();
        flash.success("views.author.create.msg");
        ApplicationController.index();
    }

    public static void add() {
        render();
    }

    public static void create(@Valid Author author) {
        if (Validation.hasErrors()) {
            render("@add", author);
        }
        author.save();
        flash.success("views.author.create.msg");
        ApplicationController.index();
    }

    public static void edit(Long id) {
        Author author = Author.findById(id);
        if (author == null) {
            notFound();
        } else {
            System.out.println("EDIT ---> " + id);
            render(author);
        }
    }
    
    public static void update(@Valid Author author) {
        System.out.println("UPDATE ---> " + author.id);
        if (Validation.hasErrors()) {
            render("@edit", author);
        }
        author.save();
        flash.success("views.author.update.msg");
        ApplicationController.index();
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