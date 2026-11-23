#!/usr/bin/env bash
# End-to-end smoke test: drives the running site with curl and checks the main features.
# Usage: ./scripts/smoke-test.sh [base-url]      (run it on a FRESH database: delete the data/ folder first)
BASE=${1:-http://localhost:8080}
PASS=0; FAIL=0; J=$(mktemp -d)
ok(){ PASS=$((PASS+1)); printf '  \033[32mPASS\033[0m  %s\n' "$1"; }
ko(){ FAIL=$((FAIL+1)); printf '  \033[31mFAIL\033[0m  %s\n' "$1"; }
check(){ [ "$2" = "$3" ] && ok "$1" || ko "$1 (expected $2, got $3)"; }
has(){ printf '%s' "$3" | grep -qF -- "$2" && ok "$1" || ko "$1 (missing: $2)"; }
hasnt(){ printf '%s' "$3" | grep -qF -- "$2" && ko "$1 (unexpected: $2)" || ok "$1"; }
status(){ local j=$1; shift; curl -s -o /dev/null -w '%{http_code}' -b "$J/$j" -c "$J/$j" "$@"; }
body(){ local j=$1; shift; curl -s -b "$J/$j" -c "$J/$j" "$@"; }
token(){ body "$1" "$BASE${2:-/login}" | grep -o 'name="_csrf" value="[^"]*"' | head -1 | sed 's/.*value="//;s/"$//'; }
login(){ local t; t=$(token "$1" /login); status "$1" --data-urlencode "_csrf=$t" --data-urlencode "email=$2" --data-urlencode "password=${3:-password}" "$BASE/login"; }
# post JAR PATH [curl data args...] : posts with a valid CSRF token, follows the redirect, prints the final page
post(){ local j=$1 p=$2; shift 2; local t; t=$(token "$j" "${TOKEN_PAGE:-/dashboard}"); body "$j" -L --data-urlencode "_csrf=$t" "$@" "$BASE$p"; }
poststatus(){ local j=$1 p=$2; shift 2; local t; t=$(token "$j" "${TOKEN_PAGE:-/dashboard}"); status "$j" --data-urlencode "_csrf=$t" "$@" "$BASE$p"; }
sec(){ printf '\n\033[1m%s\033[0m\n' "$1"; }

sec "1. Authentication"
check "login page is public" 200 "$(status g "$BASE/login")"
check "dashboard redirects guests to login" 302 "$(status g "$BASE/dashboard")"
check "wrong password is refused" 401 "$(login a admin@library.test wrong-password)"
check "admin can log in" 302 "$(login a admin@library.test)"
check "member can log in" 302 "$(login m member@library.test)"
login e evann@library.test >/dev/null; login s sari@library.test >/dev/null; login b budi@library.test >/dev/null
check "POST without CSRF token is refused" 403 "$(status a -X POST --data 'name=x' "$BASE/categories")"

sec "2. Every page renders (librarian)"
for p in /dashboard /books "/books?q=clean" "/books?category=1&available=1&sort=rating&page=1" "/books?page=2" /books/1 /books/new /books/1/edit /categories /categories/new /categories/1/edit /members /members/new /members/2/edit /loans "/loans?status=overdue" /reservations "/reservations?scope=all" /favorites /announcements /announcements/new /profile; do
  check "GET $p" 200 "$(status a "$BASE$p")"
done
check "unknown page gives 404" 404 "$(status a "$BASE/nope")"
check "unknown book gives 404" 404 "$(status a "$BASE/books/9999")"

sec "3. Roles"
check "member cannot open categories" 403 "$(status m "$BASE/categories")"
check "member cannot open members" 403 "$(status m "$BASE/members")"
check "member cannot open the add-book form" 403 "$(status m "$BASE/books/new")"
check "member cannot delete a book" 403 "$(poststatus m /books/1/delete)"
has "dashboard greets the member" "Demo" "$(body m "$BASE/dashboard")"
has "member sees an overdue fine" "fine Rp 3,000" "$(body m "$BASE/dashboard")"

sec "4. Borrowing rules (feature: loans)"
r=$(post m /books/10/borrow); has "borrow a book" "Book borrowed" "$r"
r=$(post m /books/10/borrow); has "cannot borrow the same book twice" "already have this book" "$r"
r=$(post m /books/11/borrow); has "borrow a second book" "Book borrowed" "$r"
r=$(post m /books/15/borrow); has "limit of 3 active loans" "at most 3 active loans" "$r"
has "copies decrease when borrowed" "1 of 2 available" "$(body m "$BASE/books/10")"
loan=$(body m "$BASE/loans?status=active" | grep -o '/loans/[0-9]*/renew' | head -1 | grep -o '[0-9]*')
r=$(TOKEN_PAGE=/loans post m "/loans/$loan/renew"); has "renew a loan (+7 days)" "Loan renewed" "$r"
r=$(post m /loans/5/renew); has "overdue loan cannot be renewed" "overdue loan cannot be renewed" "$r"
r=$(post e /loans/5/return); has "cannot return someone else's loan" "only return your own" "$r"
r=$(post m /loans/5/return); has "return an overdue book" "Book returned" "$r"
has "returned loans keep the late fine" "late Rp 3,000" "$(body m "$BASE/loans?status=returned")"
check "member cannot delete a loan record" 403 "$(poststatus m /loans/1/delete)"

sec "5. Reservations (queue)"
has "demo member already waits for book 8" "Waiting" "$(body m "$BASE/reservations")"
r=$(post s /books/8/reserve); has "reserve a book with no free copy" "joined the queue" "$r"
r=$(post s /books/8/reserve); has "cannot queue twice" "already in the queue" "$r"
r=$(post s /books/9/reserve); has "cannot reserve a book that is available" "copy is available" "$r"
r=$(post b /loans/12/return); has "borrower returns the reserved book" "Book returned" "$r"
has "first in line is ready" "Ready to borrow" "$(body m "$BASE/reservations")"
has "dashboard shows the ready hold" "is ready for you" "$(body m "$BASE/dashboard")"
rid=$(body s "$BASE/reservations" | grep -o '/reservations/[0-9]*/cancel' | head -1 | grep -o '[0-9]*')
r=$(TOKEN_PAGE=/reservations post s "/reservations/$rid/cancel"); has "cancel a reservation" "Reservation cancelled" "$r"

sec "6. Reviews and favorites"
r=$(post e /books/3/review -d rating=4 -d "comment=Great reference"); has "post a review" "Thanks for your review" "$r"
r=$(post e /books/3/review -d rating=5 -d "comment=Even better on re-reading"); has "update the review (one per member)" "review was updated" "$r"
has "average rating is shown" "5.0</strong> from 1 review" "$(body e "$BASE/books/3")"
r=$(post e /books/3/review -d rating=9); has "invalid rating refused" "Choose a rating" "$r"
revid=$(body e "$BASE/books/3" | grep -o '/reviews/[0-9]*/delete' | head -1 | grep -o '[0-9]*')
check "another member cannot delete my review" 403 "$(poststatus s "/reviews/$revid/delete")"
r=$(post e "/reviews/$revid/delete"); has "delete my review" "Review deleted" "$r"
r=$(post e /books/3/favorite); has "add to favorites" "Added to your favorites" "$r"
has "favorites list shows it" "Introduction to Algorithms" "$(body e "$BASE/favorites")"
r=$(post e /books/3/favorite); has "remove from favorites" "Removed from your favorites" "$r"

sec "7. Books and categories (librarian CRUD)"
r=$(post a /books -d "title=Test Book" -d "author=Jane Doe" -d "isbn=9781234567897" -d categoryId=1 -d publishedYear=2024 -d copiesTotal=2 -d "description=Created by the smoke test")
has "create a book" "was added to the catalogue" "$r"
check "duplicate ISBN is refused" 422 "$(poststatus a /books -d "title=Dup" -d "author=X" -d "isbn=9781234567897" -d categoryId=1 -d copiesTotal=1)"
check "empty title is refused" 422 "$(poststatus a /books -d "title=" -d "author=X" -d "isbn=9780000000002" -d categoryId=1 -d copiesTotal=1)"
nb=$(body a "$BASE/books?q=Test+Book" | grep -o '/books/[0-9]*"' | head -1 | grep -o '[0-9]*')
r=$(post a "/books/$nb/update" -d "title=Test Book 2nd ed." -d "author=Jane Doe" -d "isbn=9781234567897" -d categoryId=1 -d publishedYear=2025 -d copiesTotal=5); has "update a book" "was updated" "$r"
has "available copies follow the total" "5 of 5 available" "$(body a "$BASE/books/$nb")"
r=$(post a "/books/$nb/delete"); has "delete a book" "was deleted from the catalogue" "$r"
r=$(post a /categories/1/delete); has "category with books cannot be deleted" "still contains" "$r"
r=$(post a /categories -d "name=Temp shelf" -d "description=Temporary" -d "color=#336699"); has "create a category" "was created" "$r"
cid=$(body a "$BASE/categories" | grep -o '/categories/[0-9]*/delete' | tail -1 | grep -o '[0-9]*')
r=$(post a "/categories/$cid/update" -d "name=Temp shelf 2" -d "description=Renamed" -d "color=#663399"); has "update a category" "was updated" "$r"
r=$(post a "/categories/$cid/delete"); has "delete an empty category" "was deleted" "$r"

sec "8. Members and announcements (librarian CRUD)"
r=$(post a /members -d "name=Test Member" -d "email=test.member@library.test" -d role=member -d password=secret123); has "create an account" "Account created" "$r"
check "duplicate e-mail is refused" 422 "$(poststatus a /members -d "name=Again" -d "email=test.member@library.test" -d role=member -d password=secret123)"
mid=$(body a "$BASE/members?q=test.member" | grep -o '/members/[0-9]*/edit' | head -1 | grep -o '[0-9]*')
r=$(post a "/members/$mid/update" -d "name=Test Member Renamed" -d "email=test.member@library.test" -d role=admin); has "edit an account and its role" "was updated" "$r"
r=$(post a /members/1/delete); has "a librarian cannot delete their own account" "cannot delete your own account" "$r"
r=$(post a /members/4/delete); has "account with active loans cannot be deleted" "active loan" "$r"
r=$(post a "/members/$mid/delete"); has "delete an account" "was deleted" "$r"
r=$(post a /announcements -d "title=<script>alert(1)</script>" -d "body=Escaping test" -d pinned=on); has "publish an announcement" "Announcement published" "$r"
hasnt "HTML in user content is escaped (XSS)" "<script>alert(1)</script>" "$(body a "$BASE/announcements")"
has "escaped text is displayed" "&lt;script&gt;alert(1)&lt;/script&gt;" "$(body a "$BASE/announcements")"
aid=$(body a "$BASE/announcements" | grep -o '/announcements/[0-9]*/delete' | head -1 | grep -o '[0-9]*')
r=$(post a "/announcements/$aid/delete"); has "delete an announcement" "Announcement deleted" "$r"

sec "9. Registration and profile"
em="smoke$(date +%s)@library.test"
check "registration validates input" 422 "$(TOKEN_PAGE=/register poststatus n /register -d name=A -d email=bad -d password=123 -d password2=456)"
t=$(token n /register); r=$(body n -L --data-urlencode "_csrf=$t" -d "name=Smoke Tester" --data-urlencode "email=$em" -d password=password1 -d password2=password1 "$BASE/register")
has "register a new account (auto-login)" "Your account is ready" "$r"
r=$(post n /profile -d "name=Smoke Renamed" --data-urlencode "email=$em"); has "update the profile" "profile was updated" "$r"
r=$(post n /profile/password -d current=wrongpass -d password=newpassword1 -d password2=newpassword1); has "password change needs the current password" "current password is incorrect" "$r"
r=$(post n /profile/password -d current=password1 -d password=newpassword1 -d password2=newpassword1); has "change the password" "password was changed" "$r"
t=$(token n /dashboard); r=$(body n -L --data-urlencode "_csrf=$t" "$BASE/logout"); has "log out" "logged out" "$r"
check "log in with the new password" 302 "$(login n "$em" newpassword1)"

printf '\n\033[1mResult: %d passed, %d failed\033[0m\n' "$PASS" "$FAIL"
[ "$FAIL" -eq 0 ]
