package com.campuslibrary.controller;

import com.campuslibrary.dao.CategoryDao;
import com.campuslibrary.model.Category;
import com.campuslibrary.util.ServiceException;
import com.campuslibrary.web.Ctx;
import com.campuslibrary.web.HttpError;
import com.campuslibrary.web.Router;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.campuslibrary.web.Router.Access.ADMIN;

/** Category management (librarians only). */
public final class CategoryController {
    private CategoryController() {}

    public static void register(Router r) {
        r.get("/categories", ADMIN, c -> { c.set("categories", CategoryDao.all()); c.view("categories/index"); });
        r.get("/categories/new", ADMIN, c -> {
            Map<String, Object> f = new LinkedHashMap<>(); f.put("name", ""); f.put("description", ""); f.put("color", "#4C5BD4");
            c.set("form", f); c.set("mode", "new"); c.view("categories/form");
        });
        r.post("/categories", ADMIN, c -> save(c, 0));
        r.get("/categories/{id}/edit", ADMIN, c -> {
            Category cat = load(c);
            Map<String, Object> f = new LinkedHashMap<>();
            f.put("name", cat.getName()); f.put("description", cat.getDescription() == null ? "" : cat.getDescription()); f.put("color", cat.getColor());
            c.set("form", f); c.set("mode", "edit"); c.set("item", cat); c.view("categories/form");
        });
        r.post("/categories/{id}/update", ADMIN, c -> save(c, load(c).getId()));
        r.post("/categories/{id}/delete", ADMIN, c -> {
            Category cat = load(c);
            try {
                CategoryDao.delete(cat.getId());
                c.flash("success", "Category \u201c" + cat.getName() + "\u201d was deleted.");
            } catch (ServiceException e) { c.flash("error", e.getMessage()); }
            c.redirect("/categories");
        });
    }

    private static Category load(Ctx c) {
        Category cat = CategoryDao.find(c.id());
        if (cat == null) throw new HttpError(404, "This category does not exist.");
        return cat;
    }

    private static void save(Ctx c, long id) throws Exception {
        Map<String, String> errors = new LinkedHashMap<>();
        String name = c.param("name"), desc = c.param("description"), color = c.param("color");
        if (name.isEmpty() || name.length() > 80) errors.put("name", "Enter a name (up to 80 characters).");
        else if (CategoryDao.nameTaken(name, id)) errors.put("name", "Another category already uses this name.");
        if (desc.length() > 255) errors.put("description", "Keep the description under 255 characters.");
        if (!color.matches("#[0-9a-fA-F]{6}")) errors.put("color", "Choose a colour.");
        if (!errors.isEmpty()) {
            c.set("errors", errors); c.set("form", c.form("name", "description", "color"));
            c.set("mode", id == 0 ? "new" : "edit"); if (id != 0) c.set("item", CategoryDao.find(id));
            c.view("categories/form", 422);
            return;
        }
        if (id == 0) { CategoryDao.create(name, desc.isEmpty() ? null : desc, color); c.flash("success", "Category \u201c" + name + "\u201d was created."); }
        else { CategoryDao.update(id, name, desc.isEmpty() ? null : desc, color); c.flash("success", "Category \u201c" + name + "\u201d was updated."); }
        c.redirect("/categories");
    }
}
