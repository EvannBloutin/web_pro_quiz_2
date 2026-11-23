package com.campuslibrary;

import com.campuslibrary.controller.*;
import com.campuslibrary.web.Router;

/** Declares every controller of the application. */
public final class App {
    private App() {}

    public static void registerRoutes(Router r) {
        AuthController.register(r);
        DashboardController.register(r);
        BookController.register(r);
        CategoryController.register(r);
        LoanController.register(r);
        ReservationController.register(r);
        FavoriteController.register(r);
        ReviewController.register(r);
        MemberController.register(r);
        AnnouncementController.register(r);
        ProfileController.register(r);
    }
}
