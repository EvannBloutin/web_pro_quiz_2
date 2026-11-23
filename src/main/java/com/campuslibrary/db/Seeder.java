package com.campuslibrary.db;

import com.campuslibrary.util.Pass;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/** Fills an empty database with demo data (runs once, on first start). */
public final class Seeder {
    private Seeder() {}

    private static final Map<String, Long> CAT = new HashMap<>();
    private static final Map<String, Long> BOOK = new HashMap<>();
    private static final Map<String, Long> USER = new HashMap<>();

    public static void seedIfEmpty() {
        if (Db.count("SELECT COUNT(*) FROM users") > 0) return;
        String hash = Pass.hash("password");
        user("admin", "Rina Hartono", "admin@library.test", hash, true);
        user("demo", "Demo Member", "member@library.test", hash, false);
        user("evann", "Evann Bloutin", "evann@library.test", hash, false);
        user("sari", "Sari Wijaya", "sari@library.test", hash, false);
        user("budi", "Budi Santoso", "budi@library.test", hash, false);

        cat("cs", "Computer Science", "Programming, algorithms and software engineering.", "#4C5BD4");
        cat("ai", "Artificial Intelligence", "Machine learning, search and intelligent agents.", "#7A4BA8");
        cat("sci", "Science", "Physics, chemistry, biology and mathematics.", "#1F8A8A");
        cat("fic", "Fiction", "Novels and short stories.", "#D6456F");
        cat("his", "History", "World and regional history.", "#D98E1F");
        cat("phi", "Philosophy", "Ideas on how to think and how to live.", "#3E8E4F");

        book("clean", "cs", "Clean Code", "Robert C. Martin", "9780132350884", 2008, 3, "A practical guide to writing code that other people can read, change and trust, with many before-and-after examples.");
        book("pragmatic", "cs", "The Pragmatic Programmer", "Andrew Hunt, David Thomas", "9780135957059", 2019, 2, "Short, memorable lessons on craft, tooling and attitude for working developers.");
        book("clrs", "cs", "Introduction to Algorithms", "Thomas H. Cormen et al.", "9780262046305", 2022, 3, "The standard university reference on algorithms, from sorting and graphs to dynamic programming.");
        book("ddia", "cs", "Designing Data-Intensive Applications", "Martin Kleppmann", "9781449373320", 2017, 2, "How databases, queues and distributed systems really work, and the trade-offs behind them.");
        book("refactoring", "cs", "Refactoring", "Martin Fowler", "9780134757599", 2018, 2, "A catalogue of small, safe changes that improve the design of existing code.");
        book("aima", "ai", "Artificial Intelligence: A Modern Approach", "Stuart Russell, Peter Norvig", "9780134610993", 2020, 4, "The most widely used textbook on AI: agents, search, logic, probability and learning.");
        book("dl", "ai", "Deep Learning", "Ian Goodfellow et al.", "9780262035613", 2016, 2, "The mathematics and practice of deep neural networks, written by pioneers of the field.");
        book("prml", "ai", "Pattern Recognition and Machine Learning", "Christopher M. Bishop", "9780387310732", 2006, 1, "A rigorous Bayesian introduction to machine learning. Only one copy, so it is popular.");
        book("time", "sci", "A Brief History of Time", "Stephen Hawking", "9780553380163", 1988, 2, "From the Big Bang to black holes, explained for curious readers without the equations.");
        book("cosmos", "sci", "Cosmos", "Carl Sagan", "9780345539434", 1980, 2, "A journey through the universe and through the history of how we came to understand it.");
        book("gene", "sci", "The Selfish Gene", "Richard Dawkins", "9780198788607", 1976, 2, "Evolution seen from the point of view of genes, and the origin of the idea of the meme.");
        book("1984", "fic", "1984", "George Orwell", "9780451524935", 1949, 3, "A classic dystopia about surveillance, propaganda and the fragility of truth.");
        book("bumi", "fic", "Bumi Manusia", "Pramoedya Ananta Toer", "9789799731234", 1980, 2, "The first novel of the Buru Quartet, set in colonial Java at the start of the twentieth century.");
        book("laskar", "fic", "Laskar Pelangi", "Andrea Hirata", "9789793062792", 2005, 3, "The story of ten children and their teachers in a poor village school on Belitung island.");
        book("hobbit", "fic", "The Hobbit", "J.R.R. Tolkien", "9780547928227", 1937, 2, "Bilbo Baggins leaves his quiet home for a quest involving dwarves, a dragon and a ring.");
        book("sapiens", "his", "Sapiens", "Yuval Noah Harari", "9780062316097", 2015, 2, "A sweeping history of humankind from the Stone Age to the present.");
        book("ggs", "his", "Guns, Germs, and Steel", "Jared Diamond", "9780393317558", 1997, 2, "Why some societies became powerful before others: geography, crops, animals and disease.");
        book("sejarah", "his", "Sejarah Indonesia Modern", "M. C. Ricklefs", "9789793062310", 2008, 1, "A history of Indonesia from around 1200 to the 2000s.");
        book("meditations", "phi", "Meditations", "Marcus Aurelius", "9780812968255", 2002, 2, "Private notes of a Roman emperor on discipline, duty and keeping calm.");

        // loans: (user, book, borrowed N days ago, returned N days ago or -1 if still out)
        loan("evann", "clean", 40, 30); loan("evann", "1984", 35, 24); loan("evann", "dl", 5, -1); loan("evann", "time", 9, -1);
        loan("demo", "sapiens", 17, -1); loan("demo", "refactoring", 60, 50); loan("demo", "ddia", 100, 88);
        loan("sari", "clean", 2, -1); loan("sari", "pragmatic", 11, -1); loan("sari", "aima", 75, 64); loan("sari", "cosmos", 120, 110);
        loan("budi", "prml", 4, -1); loan("budi", "hobbit", 150, 140); loan("budi", "laskar", 45, 36); loan("budi", "gene", 20, 8);
        loan("demo", "1984", 90, 80); loan("evann", "clrs", 130, 118); loan("sari", "1984", 52, 41);

        Db.insert("INSERT INTO reservations(user_id, book_id, status) VALUES (?,?, 'WAITING')", USER.get("demo"), BOOK.get("prml"));

        review("evann", "clean", 4, "Changed how I name things. A few chapters feel dated, but the core ideas hold up.");
        review("sari", "clean", 5, "Every developer should read this once.");
        review("demo", "sapiens", 4, "Easy to read and full of ideas to argue about.");
        review("evann", "1984", 5, "Still scary, still relevant.");
        review("budi", "laskar", 5, "I read it in two evenings. Beautiful.");
        review("sari", "aima", 5, "The reference for our AI course.");
        review("demo", "ddia", 4, "Dense but worth it.");
        review("evann", "time", 4, null);
        review("budi", "hobbit", 5, "A perfect first fantasy novel.");

        fav("evann", "dl"); fav("evann", "aima"); fav("evann", "ddia"); fav("evann", "meditations");
        fav("demo", "sapiens"); fav("sari", "cosmos");

        Db.insert("INSERT INTO announcements(author_id, title, body, pinned) VALUES (?,?,?,?)", USER.get("admin"),
            "Extended opening hours during exam weeks",
            "The library stays open until 22:00 from Monday to Friday during exam weeks. Quiet study rooms can be reserved at the front desk.", true);
        Db.insert("INSERT INTO announcements(author_id, title, body, pinned) VALUES (?,?,?,?)", USER.get("admin"),
            "New shelf: data engineering",
            "Designing Data-Intensive Applications and Refactoring have joined the catalogue. Reserve a copy if all of them are on loan.", false);
    }

    private static void user(String key, String name, String email, String hash, boolean admin) {
        USER.put(key, Db.insert("INSERT INTO users(name, email, password_hash, is_admin) VALUES (?,?,?,?)", name, email, hash, admin));
    }
    private static void cat(String key, String name, String desc, String color) {
        CAT.put(key, Db.insert("INSERT INTO categories(name, description, color) VALUES (?,?,?)", name, desc, color));
    }
    private static void book(String key, String cat, String title, String author, String isbn, int year, int copies, String desc) {
        BOOK.put(key, Db.insert("INSERT INTO books(category_id, title, author, isbn, description, published_year, copies_total, copies_available) VALUES (?,?,?,?,?,?,?,?)",
            CAT.get(cat), title, author, isbn, desc, year, copies, copies));
    }
    private static void loan(String user, String book, int borrowedDaysAgo, int returnedDaysAgo) {
        LocalDate borrowed = LocalDate.now().minusDays(borrowedDaysAgo);
        LocalDate returned = returnedDaysAgo < 0 ? null : LocalDate.now().minusDays(returnedDaysAgo);
        Db.insert("INSERT INTO loans(user_id, book_id, borrowed_at, due_at, returned_at) VALUES (?,?,?,?,?)",
            USER.get(user), BOOK.get(book), borrowed, borrowed.plusDays(14), returned);
        if (returned == null) Db.update("UPDATE books SET copies_available = copies_available - 1 WHERE id = ?", BOOK.get(book));
    }
    private static void review(String user, String book, int rating, String comment) {
        Db.insert("INSERT INTO reviews(user_id, book_id, rating, comment) VALUES (?,?,?,?)", USER.get(user), BOOK.get(book), rating, comment);
    }
    private static void fav(String user, String book) {
        Db.insert("INSERT INTO favorites(user_id, book_id) VALUES (?,?)", USER.get(user), BOOK.get(book));
    }
}
